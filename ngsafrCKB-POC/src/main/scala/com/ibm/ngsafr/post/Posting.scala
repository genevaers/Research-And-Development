package com.ibm.ngsafr.post

import com.ibm.jzos._
import com.ibm.ngsafr.datatypes._

import scala.collection.immutable.SortedMap
import scala.collection.mutable.ListBuffer
import scala.util.Try


// ngsafr Common Key Buffer in Scala POC
// (c) Copyright IBM Corporation. 2018
// SPDX-License-Identifier: Apache-2.0
// Written by: Sibasis Das

//  Updates made for jzos
//  1 - arg passed in for parallelReplicationCount of inputParams
//  2 - Path name updated
//  3 - File name formed from concatnation of Path + inputParms fileName + parallelReplicationCount + a constant
//  4 - commented out Balance and Vendor Update files

object Posting {

  private var listOfVendorHistoryObjectWithLowestKey: List[Vendor] = List.empty[Vendor]
  private var listOfVendorHistoryObject: List[Vendor] = List.empty[Vendor]

  private var listOfVendorUpdateObjectWithLowestKey: List[VendorUpdate] = List.empty[VendorUpdate]
  private var listOfVendorUpdateObject: List[VendorUpdate] = List.empty[VendorUpdate]

  private var listOfBalanceObjectWithLowestKey: List[Balances] = List.empty[Balances]
  private var listOfBalanceObject: List[Balances] = List.empty[Balances]

  private var listOfTransactionObjectWithLowestKey: List[Transaction] = List.empty[Transaction]
  private var listOfTransactionObject: List[Transaction] = List.empty[Transaction]

  private var lowestCurrentIdKey = InputParams

  private var lengthOfTheVendorHistoryCurrentData: Int = _
  private var lengthOfTheVendorUpdateCurrentData: Int = _
  private var lengthOfTheBalanceHistoryCurrentData: Int = _
  private var lengthOfTheTransactionCurrentData: Int = _

  private var totalNumberOfAgencyDataRows:Int = _
  private var totalNumberOfRefObjectDataRows:Int = _

  def main(args: Array[String]): Unit = {

    var parallelReplicationCountArg = ""
    if (args.length > 0) {parallelReplicationCountArg = args(0)}

    val metricsEnabled = true
    val encoding = ZUtil.getDefaultPlatformEncoding

    val filePaths = Map("inputPath" -> "'GEBT.SPK.", "outputPath" ->
      "'GEBT.SPK.")

    val listOfAgencies = populateAgencyData(filePaths,encoding)
    val mapOfAgencies: Map[String, String] = listOfAgencies.map(agency => (agency.agencyCode, agency.agencyName)).toMap
    var masterDataForAccumulatedTransactionsByAgency: Map[String, BigDecimal] = Map.empty[String, BigDecimal]
    var masterDataForAccumulatedTransactionsByAgencyAndDate: Map[(String, String), BigDecimal] = Map.empty[(String, String), BigDecimal]

    val listOfRefObjects = populateObjectData(filePaths,encoding)
    val mapOfObjects: Map[String, String] = listOfRefObjects.map(refObject => (refObject.objectCode, refObject.objectName.trim)).toMap
    var masterDataForAccumulatedTransactionsByRefObject: Map[String, BigDecimal] = Map.empty[String, BigDecimal]

    var masterDataForAccumulatedTransactionsByAgencyAndObject: Map[(String, String), BigDecimal] = Map.empty[(String, String), BigDecimal]
    var masterDataForAccumulatedTransactionsByAgencyAndVendorType: Map[(String, String), BigDecimal] = Map.empty[(String, String), BigDecimal]

    println("#################################################################################")
    println("##########" + "\t" + "Started the CKB")
    println("#################################################################################")
    val t1 = System.currentTimeMillis

    var listParams = setupInputParams()

    val mappedFileNames = listParams.map(listParam => listParam.entityName.concat(listParam.partitionId) ->
      listParam.fileName.concat(parallelReplicationCountArg).concat(".FIXLDATA'")).toMap

    var vendorHistoryCurrentData: Array[Byte] = null
    var vendorUpdateCurrentData: Array[Byte] = null
    var balanceHistoryCurrentData: Array[Byte] = null
    var transactionCurrentData: Array[Byte] = null

    var vendorHistoryFileIterator: RecordReader = null
    var vendorUpdateFileIterator: RecordReader = null
    var balanceHistoryFileIterator: RecordReader = null
    var transactionFileIterator: RecordReader = null

    for (mappedFileName <- mappedFileNames) {
      if (mappedFileName._1.equals("VendorHistory")) {
        println("Vendor History: " + ZFile.getSlashSlashQuotedDSN(filePaths("inputPath") + mappedFileNames("VendorHistory")))
        vendorHistoryFileIterator = RecordReader.newReader(ZFile.getSlashSlashQuotedDSN(filePaths("inputPath") +
          mappedFileNames("VendorHistory")),
          ZFileConstants.FLAG_DISP_SHR)
        vendorHistoryCurrentData = new Array[Byte](vendorHistoryFileIterator.getLrecl)
      } else if (mappedFileName._1.equals("VendorUpdate")) {
        println("Vendor Update: " + ZFile.getSlashSlashQuotedDSN(filePaths("inputPath") + mappedFileNames("VendorUpdate")))
        vendorUpdateFileIterator = RecordReader.newReader(ZFile.getSlashSlashQuotedDSN(filePaths("inputPath") +
          mappedFileNames("VendorUpdate")),
          ZFileConstants.FLAG_DISP_SHR)
        vendorUpdateCurrentData = new Array[Byte](vendorUpdateFileIterator.getLrecl)
      } else if (mappedFileName._1.equals("BalanceHistory")) {
        println("Balance History: " + ZFile.getSlashSlashQuotedDSN(filePaths("inputPath") + mappedFileNames("BalanceHistory")))
        balanceHistoryFileIterator = RecordReader.newReader(ZFile.getSlashSlashQuotedDSN(filePaths("inputPath") +
          mappedFileNames("BalanceHistory")),
          ZFileConstants.FLAG_DISP_SHR)
        balanceHistoryCurrentData = new Array[Byte](balanceHistoryFileIterator.getLrecl)
      } else if (mappedFileName._1.equals("TransactionDaily")) {
        println("Trans Daily: " + ZFile.getSlashSlashQuotedDSN(filePaths("inputPath") + mappedFileNames("TransactionDaily")))
        transactionFileIterator = RecordReader.newReader(ZFile.getSlashSlashQuotedDSN(filePaths("inputPath") +
          mappedFileNames("TransactionDaily")),
          ZFileConstants.FLAG_DISP_SHR)
        transactionCurrentData = new Array[Byte](transactionFileIterator.getLrecl)
      }
    }

    var vendorHistoryCounter: Int = 0
    var vendorUpdateCounter: Int = 0
    var balanceHistoryCounter: Int = 0
    var transactionHistoryCounter: Int = 0

    var accumulatedTransactionsByAgencyCounter: Int = 0
    var accumulatedTransactionsByObjectCounter: Int = 0
    var accumulatedTransactionsByAgencyAndVendorTypeCounter: Int = 0
    var accumulatedTransactionsByAgencyAndYearCounter: Int = 0
    var accumulatedBalancesByAgencyAndObjectCounter: Int = 0

    val vendorHistoryWriter = RecordWriter.newWriter(
      ZFile.getSlashSlashQuotedDSN(filePaths("outputPath") + "VAOVEND.INSTID" + parallelReplicationCountArg + ".FIXLDATA'"),ZFileConstants.FLAG_DISP_SHR)
    val vendorUpdateWriter = RecordWriter.newWriter(
      ZFile.getSlashSlashQuotedDSN(filePaths("outputPath") + "VAOVUPD.INSTID" + parallelReplicationCountArg + ".FIXLDATA'"),ZFileConstants.FLAG_DISP_SHR)
    val balanceHistoryWriter = RecordWriter.newWriter(
      ZFile.getSlashSlashQuotedDSN(filePaths("outputPath") + "VAOBAL.INSTID" + parallelReplicationCountArg + ".FIXLDATA'"), ZFileConstants.FLAG_DISP_SHR)
    val transactionDailyWriter = RecordWriter.newWriter(
      ZFile.getSlashSlashQuotedDSN(filePaths("outputPath") + "VAOTRAN.INSTID" + parallelReplicationCountArg + ".FIXLDATA'"), ZFileConstants.FLAG_DISP_SHR)


    val transactionAccmltedByAgncyReportWriter = RecordWriter.newWriter(
      ZFile.getSlashSlashQuotedDSN(filePaths("outputPath") + "VAOTAGY1.INSTID" + parallelReplicationCountArg + ".FIXLDATA'"),ZFileConstants.FLAG_DISP_SHR)
    val transactionAccmltedByAgncyNotContainingDepartmentReportWriter = RecordWriter.newWriter(
      ZFile.getSlashSlashQuotedDSN(filePaths("outputPath") + "VAOTAGY2.INSTID" + parallelReplicationCountArg + ".FIXLDATA'"),ZFileConstants.FLAG_DISP_SHR)
    val transactionsAccmltedByAgncyAndVendorTypeReportWriter = RecordWriter.newWriter(
      ZFile.getSlashSlashQuotedDSN(filePaths("outputPath") + "VAOTAGY3.INSTID" + parallelReplicationCountArg + ".FIXLDATA'"),ZFileConstants.FLAG_DISP_SHR)
    val transactionsAccmltedByObjectWriter = RecordWriter.newWriter(
      ZFile.getSlashSlashQuotedDSN(filePaths("outputPath") + "VAOTOBJ1.INSTID" + parallelReplicationCountArg + ".FIXLDATA'"),ZFileConstants.FLAG_DISP_SHR)
    val balancesAccmltedByAgencyAndObjectWriter = RecordWriter.newWriter(
      ZFile.getSlashSlashQuotedDSN(filePaths("outputPath") + "VAOBAGOB.INSTID" + parallelReplicationCountArg + ".FIXLDATA'"),ZFileConstants.FLAG_DISP_SHR)
    val transactionsAccmltedByAgncyAndYearReportWriter = RecordWriter.newWriter(
      ZFile.getSlashSlashQuotedDSN(filePaths("outputPath") + "VAOTAGYR.INSTID" + parallelReplicationCountArg + ".FIXLDATA'"),ZFileConstants.FLAG_DISP_SHR)

    val statisticsAccumulatorWriter = RecordWriter.newWriter(
      ZFile.getSlashSlashQuotedDSN(filePaths("outputPath") + "VAOCNTRL.INSTID" + parallelReplicationCountArg + ".FIXLDATA'"),ZFileConstants.FLAG_DISP_SHR)

    var vendorHistoryRecordCount: Int = 0
    val vendorCurrentRecord =  Vendor("", "", "", "", "", "", "", "")

    var freshReadIndicatorForVendorHistoryIs: Boolean = true
    var freshReadIndicatorForVendorUpdateIs: Boolean = true
    var freshReadIndicatorForBalancesIs: Boolean = true
    var freshReadIndicatorForTransactionsIs: Boolean = true

    var reachedEOFForVendorHistory = false
    var reachedEOFForVendorUpdate = false
    var reachedEOFForBalanceHistory = false
    var reachedEOFForTransactions = false

    var vendorHistory = Vendor.apply("", "", "", "", "", "", "", "")
    var vendorUpdate = VendorUpdate.apply("", "", "", "")
    var balanceHistory = Balances.apply("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "")
    var transaction = Transaction.apply("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "")

    var vendorHistoryRow: Array[String] = Array.empty
    var vendorUpdateRow: Array[String] = Array.empty
    var balanceHistoryRow: Array[String] = Array.empty
    var transactionRow: Array[String] = Array.empty

    while (reachedEOFForVendorHistory == false || reachedEOFForVendorUpdate == false || reachedEOFForBalanceHistory == false || reachedEOFForTransactions == false) {
      if (freshReadIndicatorForVendorHistoryIs) {
        vendorHistoryRow = iterateThroughVendorHistory(vendorHistoryCurrentData, vendorHistoryFileIterator, encoding)
        if (!vendorHistoryRow.isEmpty) {
          vendorHistory = Vendor.apply(vendorHistoryRow(0), vendorHistoryRow(1), vendorHistoryRow(2), vendorHistoryRow(3),
            vendorHistoryRow(4), vendorHistoryRow(5), vendorHistoryRow(6), vendorHistoryRow(7))
        }
      }
      if (freshReadIndicatorForVendorUpdateIs) {
        vendorUpdateRow = iterateThroughVendorUpdate(vendorUpdateCurrentData, vendorUpdateFileIterator, encoding)
        if (!vendorUpdateRow.isEmpty || vendorUpdateRow != null) {
          vendorUpdate = VendorUpdate.apply(vendorUpdateRow(0), vendorUpdateRow(1), vendorUpdateRow(2), vendorUpdateRow(3))
        }
      }
      if (freshReadIndicatorForBalancesIs) {
        balanceHistoryRow = iterateThroughBalanceHistory(balanceHistoryCurrentData,balanceHistoryFileIterator,encoding)
        if (!balanceHistoryRow.isEmpty) {
          balanceHistory = Balances.apply(balanceHistoryRow(0), balanceHistoryRow(1), balanceHistoryRow(2), balanceHistoryRow(3),
            balanceHistoryRow(4), balanceHistoryRow(5), balanceHistoryRow(6), balanceHistoryRow(7), balanceHistoryRow(8), balanceHistoryRow(9),
            balanceHistoryRow(10), balanceHistoryRow(11), balanceHistoryRow(12), balanceHistoryRow(13), balanceHistoryRow(14), balanceHistoryRow(15),
            balanceHistoryRow(16), balanceHistoryRow(17), balanceHistoryRow(18))
        }
      }
      if (freshReadIndicatorForTransactionsIs) {
        transactionRow = iterateThroughTransactions(transactionCurrentData,transactionFileIterator,encoding)
        if (!transactionRow.isEmpty) {
          transaction = Transaction.apply(transactionRow(0), transactionRow(1), transactionRow(2), transactionRow(3),
            transactionRow(4), transactionRow(5), transactionRow(6), transactionRow(7), transactionRow(8), transactionRow(9),
            transactionRow(10), transactionRow(11), transactionRow(12), transactionRow(13), transactionRow(14), transactionRow(15),
            transactionRow(16), transactionRow(17), transactionRow(18), transactionRow(19), transactionRow(20), transactionRow(21), transactionRow(22),
            transactionRow(23), transactionRow(24), transactionRow(25), transactionRow(26), transactionRow(27), transactionRow(28),
            transactionRow(29), transactionRow(30))
        }
      }
      //println("********************************************************** " + listParams)
      val vendorHistoryInputParam = listParams.filter(_.entityName.equals("Vendor")).filter(_.partitionId.equals("History")).head
      if (vendorHistory.instinstID != null)
        vendorHistoryInputParam.currentIdValue = BigInt(vendorHistory.instinstID)
      if (reachedEOFForVendorHistory)
        vendorHistoryInputParam.currentIdValue = BigInt(Int.MaxValue)
      if (lengthOfTheVendorHistoryCurrentData == -1) {
        reachedEOFForVendorHistory = true
        //vendorHistoryInputParam.currentIdValue = BigInt(Int.MaxValue)
      }

      val vendorUpdateInputParam = listParams.filter(_.entityName.equals("Vendor")).filter(_.partitionId.equals("Update")).head
      if (vendorUpdate.instinstID != null)
        vendorUpdateInputParam.currentIdValue = BigInt(vendorUpdate.instinstID)
      if (reachedEOFForVendorUpdate)
        vendorUpdateInputParam.currentIdValue = BigInt(Int.MaxValue)
      if (lengthOfTheVendorUpdateCurrentData == -1) {
        reachedEOFForVendorUpdate = true
        //vendorUpdateInputParam.currentIdValue = BigInt(Int.MaxValue)
      }

      val balanceHistoryInputParam = listParams.filter(_.entityName.equals("Balance")).filter(_.partitionId.equals("History")).head
      if (balanceHistory.balinstID != null)
        balanceHistoryInputParam.currentIdValue = BigInt(balanceHistory.balinstID)
      if (reachedEOFForBalanceHistory)
        balanceHistoryInputParam.currentIdValue = BigInt(Int.MaxValue)
      if (lengthOfTheBalanceHistoryCurrentData == -1) {
        reachedEOFForBalanceHistory = true
        //balanceHistoryInputParam.currentIdValue = BigInt(Int.MaxValue)
      }

      val transactionInputParam = listParams.filter(_.entityName.equals("Transaction")).filter(_.partitionId.equals("Daily")).head
      if (transaction.transinstID != null)
        transactionInputParam.currentIdValue = BigInt(transaction.transinstID)
      if (reachedEOFForTransactions)
        transactionInputParam.currentIdValue = BigInt(Int.MaxValue)
      if (lengthOfTheTransactionCurrentData == -1) {
        reachedEOFForTransactions = true
        //transactionInputParam.currentIdValue = BigInt(Int.MaxValue)
      }
      val inputParamWithLowestCurrentIdKey = listParams.minBy(inputParams => inputParams.currentIdValue)
      val mapWithLowestKeyId = listParams.filter(_.currentIdValue == inputParamWithLowestCurrentIdKey.currentIdValue)
        .map(inputParams => (inputParams.entityName.concat(inputParams.partitionId), inputParams.currentIdValue)).toMap
      //println("Map with the mapping of lowest key to the file from which it was read :" + mapWithLowestKeyId)
      //println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$"+ listParams)
      mapWithLowestKeyId.foreach {
        entry => {
          if (entry._1.contains("VendorHistory")) {
            if (vendorHistory.instinstID != null) {
              if (BigInt(vendorHistory.instinstID) == entry._2) {
                populateListOfVendorHistoryObjectsMatchingLowestKey(vendorHistory)
              }
            }
            while (Try {
              BigInt(vendorHistoryRow.head)
            }.getOrElse(-1) == entry._2) {
              vendorHistoryRow = iterateThroughVendorHistory(vendorHistoryCurrentData,vendorHistoryFileIterator,encoding)
              val vendorHistoryForSameID = Vendor.apply(vendorHistoryRow(0), vendorHistoryRow(1), vendorHistoryRow(2), vendorHistoryRow(3),
                vendorHistoryRow(4), vendorHistoryRow(5), vendorHistoryRow(6), vendorHistoryRow(7))
              if (Try {
                BigInt(vendorHistoryRow.head)
              }.getOrElse(-1) == entry._2) {
                if (vendorHistoryForSameID.instinstID != null)
                  populateListOfVendorHistoryObjectsMatchingLowestKey(vendorHistoryForSameID)
              } else {
                if (vendorHistoryForSameID.instinstID != null) {
                  vendorHistory = vendorHistoryForSameID
                  freshReadIndicatorForVendorHistoryIs = false
                  populateListOfVendorHistoryObjects(vendorHistoryForSameID)
                }
              }
            }
          } else {
            freshReadIndicatorForVendorHistoryIs = false
          }
          if (entry._1.contains("VendorUpdate")) {
            if (vendorUpdate.instinstID != null) {
              if (BigInt(vendorUpdate.instinstID) == entry._2) {
                populateListOfVendorUpdateObjectsMatchingLowestKey(vendorUpdate)
              }
            }
            while (Try {
              BigInt(vendorUpdateRow.head)
            }.getOrElse(-1) == entry._2) {
              vendorUpdateRow = iterateThroughVendorUpdate(vendorUpdateCurrentData,vendorUpdateFileIterator,encoding)
              val vendorUpdateForSameID = VendorUpdate.apply(vendorUpdateRow(0), vendorUpdateRow(1), vendorUpdateRow(2), vendorUpdateRow(3))
              if (Try {
                BigInt(vendorUpdateRow.head)
              }.getOrElse(-1) == entry._2) {
                if (vendorUpdateForSameID.instinstID != null)
                  populateListOfVendorUpdateObjectsMatchingLowestKey(vendorUpdateForSameID)
              } else {
                if (vendorUpdateForSameID.instinstID != null) {
                  vendorUpdate = vendorUpdateForSameID
                  freshReadIndicatorForVendorUpdateIs = false
                  populateListOfVendorUpdateObjects(vendorUpdateForSameID)
                }
              }
            }
          } else {
            freshReadIndicatorForVendorUpdateIs = false
          }
          if (entry._1.contains("BalanceHistory")) {
            if (balanceHistory.balinstID != null) {
              if (BigInt(balanceHistory.balinstID) == entry._2) {
                populateListOfBalanceObjectsMatchingLowestKey(balanceHistory)
              }
            }
            while (Try {
              BigInt(balanceHistoryRow.head)
            }.getOrElse(-1) == entry._2) {
              balanceHistoryRow = iterateThroughBalanceHistory(balanceHistoryCurrentData,balanceHistoryFileIterator,encoding)
              val balanceHistoryForSameID = Balances.apply(balanceHistoryRow(0), balanceHistoryRow(1), balanceHistoryRow(2), balanceHistoryRow(3),
                balanceHistoryRow(4), balanceHistoryRow(5), balanceHistoryRow(6), balanceHistoryRow(7), balanceHistoryRow(8), balanceHistoryRow(9),
                balanceHistoryRow(10), balanceHistoryRow(11), balanceHistoryRow(12), balanceHistoryRow(13), balanceHistoryRow(14), balanceHistoryRow(15),
                balanceHistoryRow(16), balanceHistoryRow(17), balanceHistoryRow(18))
              if (Try {
                BigInt(balanceHistoryRow.head)
              }.getOrElse(-1) == entry._2) {
                if (balanceHistoryForSameID != null)
                  populateListOfBalanceObjectsMatchingLowestKey(balanceHistoryForSameID)
              } else {
                if (balanceHistoryForSameID.balinstID != null) {
                  balanceHistory = balanceHistoryForSameID
                  freshReadIndicatorForBalancesIs = false
                  populateListOfBalanceObjects(balanceHistoryForSameID)
                }
              }
            }
          } else {
            freshReadIndicatorForBalancesIs = false
          }
          if (entry._1.contains("TransactionDaily")) {
            if (BigInt(transaction.transinstID) == entry._2) {
              populateListOfTransactionObjectsMatchingLowestKey(transaction)
            }
            while (Try {
              BigInt(transactionRow.head)
            }.getOrElse(-1) == entry._2) {
              transactionRow = iterateThroughTransactions(transactionCurrentData,transactionFileIterator,encoding)
              val transactionForSameID = Transaction.apply(transactionRow(0), transactionRow(1), transactionRow(2), transactionRow(3),
                transactionRow(4), transactionRow(5), transactionRow(6), transactionRow(7), transactionRow(8), transactionRow(9),
                transactionRow(10), transactionRow(11), transactionRow(12), transactionRow(13), transactionRow(14), transactionRow(15),
                transactionRow(16), transactionRow(17), transactionRow(18), transactionRow(19), transactionRow(20), transactionRow(21), transactionRow(22),
                transactionRow(23), transactionRow(24), transactionRow(25), transactionRow(26), transactionRow(27), transactionRow(28),
                transactionRow(29), transactionRow(30))
              if (Try {
                BigInt(transactionRow.head)
              }.getOrElse(-1) == entry._2) {
                if (transactionForSameID.transinstID != null)
                  populateListOfTransactionObjectsMatchingLowestKey(transactionForSameID)
              } else {
                if (transactionForSameID.transinstID != null) {
                  transaction = transactionForSameID
                  freshReadIndicatorForTransactionsIs = false
                  populateListOfTransactionObjects(transactionForSameID)
                }
              }
            }
          } else {
            freshReadIndicatorForTransactionsIs = false
          }
        }
      }

      val listOfVendorForViewProcesing = (listOfVendorHistoryObjectWithLowestKey.filter(vendor1 => listOfVendorHistoryObjectWithLowestKey.exists(v => BigInt(vendor1.instinstID) ==
        inputParamWithLowestCurrentIdKey.currentIdValue)) ::: listOfVendorHistoryObject.filter(vendor2 => listOfVendorHistoryObject.
        exists(v => BigInt(vendor2.instinstID) == inputParamWithLowestCurrentIdKey.currentIdValue))).distinct
      //println(listOfVendorForViewProcesing)
      if (listOfVendorForViewProcesing.nonEmpty) {
        vendorHistoryWriter.write((listOfVendorForViewProcesing + "\n").getBytes)
        if (metricsEnabled)
          vendorHistoryCounter = vendorHistoryCounter + listOfVendorForViewProcesing.size
      }
      // Creating a map of vendorID->VendorType for VendorHistory
      val mapOfVendorHistoryVendorTypeMapping = listOfVendorForViewProcesing.map(vendorHistory => (vendorHistory.instinstID, vendorHistory.instTypeID)).toMap

      listOfVendorHistoryObjectWithLowestKey = listOfVendorHistoryObjectWithLowestKey.filterNot(vendorToDrop =>
        BigInt(vendorToDrop.instinstID) == inputParamWithLowestCurrentIdKey.currentIdValue)
      listOfVendorHistoryObject = listOfVendorHistoryObject.filterNot(vendorToDrop =>
        BigInt(vendorToDrop.instinstID) == inputParamWithLowestCurrentIdKey.currentIdValue)

      val listOfVendorUpdateForViewProcessing = (listOfVendorUpdateObjectWithLowestKey.filter(vendorUpdate1 => listOfVendorUpdateObjectWithLowestKey
        .exists(vUpdLK => BigInt(vendorUpdate1.instinstID) == inputParamWithLowestCurrentIdKey.currentIdValue))
        ::: listOfVendorUpdateObject.filter(vendorUpdate2 => listOfVendorUpdateObject
        .exists(vUpd => BigInt(vendorUpdate2.instinstID) == inputParamWithLowestCurrentIdKey.currentIdValue))).distinct
      //println(listOfVendorUpdateForViewProcessing)
      if (listOfVendorUpdateForViewProcessing.nonEmpty) {
        vendorUpdateWriter.write((listOfVendorUpdateForViewProcessing + "\n").getBytes)
        if (metricsEnabled)
          vendorUpdateCounter = vendorUpdateCounter + listOfVendorForViewProcesing.size
      }
      // Creating a map of vendorID->VendorType for VendorUpdate
      val mapOfVendorUpdateVendorTypeMapping = listOfVendorUpdateForViewProcessing.map(vendorUpdate => (vendorUpdate.instinstID, vendorUpdate.instTypeID)).toMap

      //#########################################################
      // Merging the two maps for VendorHistory and VendorUpdate
      // for vendorId->vendorType
      //########################################################
      val mergedMapOfVendorToVendorTypeMapping = mapOfVendorHistoryVendorTypeMapping ++ mapOfVendorUpdateVendorTypeMapping

      listOfVendorUpdateObjectWithLowestKey = listOfVendorUpdateObjectWithLowestKey.filterNot(vendorUpdateToDrop => BigInt(vendorUpdateToDrop.instinstID) ==
        inputParamWithLowestCurrentIdKey.currentIdValue)
      listOfVendorUpdateObject = listOfVendorUpdateObject.filterNot(vendorUpdateToDrop => BigInt(vendorUpdateToDrop.instinstID) ==
        inputParamWithLowestCurrentIdKey.currentIdValue)

      val listOfBalancesForViewProcessing = (listOfBalanceObjectWithLowestKey.filter(balance1 => listOfBalanceObjectWithLowestKey.exists(bal => BigInt(balance1.balinstID) ==
        inputParamWithLowestCurrentIdKey.currentIdValue)) ::: listOfBalanceObject.filter(balance2 => listOfBalanceObject.
        exists(bal => BigInt(balance2.balinstID) == inputParamWithLowestCurrentIdKey.currentIdValue))).distinct
      //println(listOfBalancesForViewProcessing)
      if (listOfBalancesForViewProcessing.nonEmpty) {
        balanceHistoryWriter.write((listOfBalancesForViewProcessing + "\n").getBytes)
        if (metricsEnabled)
          balanceHistoryCounter = balanceHistoryCounter + listOfBalancesForViewProcessing.size
      }

      //############################################
      //Accumulate Balance data by Agency and Object
      //############################################
      val mapForBalanceObjectsForViewProcessingGroupedByAgencyAndObject: Map[(String, String), Double] = listOfBalancesForViewProcessing
        .groupBy(balanceHist => (balanceHist.ballegalEntityID, balanceHist.balaccountID.drop(3))).mapValues(_.map(_.baltransAmount.toDouble).sum)
      val decimelConversionMapForBalanceObjectsForViewProcessingGroupedByAgencyAndObject = Map[(String, String), BigDecimal]() ++
        mapForBalanceObjectsForViewProcessingGroupedByAgencyAndObject.map { case (k, v) => k -> BigDecimal(v).setScale(2, BigDecimal.RoundingMode.HALF_UP) }

      if (metricsEnabled) {
        accumulatedBalancesByAgencyAndObjectCounter = accumulatedBalancesByAgencyAndObjectCounter +
          decimelConversionMapForBalanceObjectsForViewProcessingGroupedByAgencyAndObject.size
      }

      masterDataForAccumulatedTransactionsByAgencyAndObject =
        insertOrUpdateAgencyAndObjectMappedData(decimelConversionMapForBalanceObjectsForViewProcessingGroupedByAgencyAndObject, masterDataForAccumulatedTransactionsByAgencyAndObject)

      listOfBalanceObjectWithLowestKey = listOfBalanceObjectWithLowestKey.filterNot(balanceToDrop =>
        BigInt(balanceToDrop.balinstID) == inputParamWithLowestCurrentIdKey.currentIdValue)
      listOfBalanceObject = listOfBalanceObject.filterNot(balanceToDrop =>
        BigInt(balanceToDrop.balinstID) == inputParamWithLowestCurrentIdKey.currentIdValue)

      val listOfTransactionsForViewProcessing = listOfTransactionObjectWithLowestKey.filter(transaction1 => listOfTransactionObjectWithLowestKey.exists(trans => BigInt(transaction1.transinstID) ==
        inputParamWithLowestCurrentIdKey.currentIdValue)) ::: listOfTransactionObject.filter(transaction2 => listOfTransactionObject.
        exists(trans => BigInt(transaction2.transinstID) == inputParamWithLowestCurrentIdKey.currentIdValue))
      //println(listOfTransactionsForViewProcessing)
      if (listOfTransactionsForViewProcessing.nonEmpty) {
        //println("list of transactions for view processing: " + listOfTransactionsForViewProcessing + "\n")
        listOfTransactionsForViewProcessing.foreach(trans =>  transactionDailyWriter.write((trans + "\n").getBytes))
        //transactionDailyWriter.write((listOfTransactionsForViewProcessing + "\n").getBytes)
        if (metricsEnabled)
          transactionHistoryCounter = transactionHistoryCounter + listOfTransactionsForViewProcessing.size
      }

      //#######################################
      //Accumulate Transaction data by Agency
      //#######################################
      val mapForTransactionObjectsForViewProcessingGroupedByAgency: Map[String, Double] = listOfTransactionsForViewProcessing.groupBy(_.translegalEntityID)
        .mapValues(_.map(_.transtransAmount.toDouble).sum)

      val decimelConversionMapOfTranObjectsForViewProcessingGroupedByAgency = Map[String, BigDecimal]() ++
        mapForTransactionObjectsForViewProcessingGroupedByAgency.map { case (k, v) => k -> BigDecimal(v).setScale(2, BigDecimal.RoundingMode.HALF_UP) }
      //println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$" + mutableMapOfTranObjectsForViewProcessingGroupedByAgency)

      if (metricsEnabled) {
        accumulatedTransactionsByAgencyCounter = accumulatedTransactionsByAgencyCounter +
          decimelConversionMapOfTranObjectsForViewProcessingGroupedByAgency.size
      }

      masterDataForAccumulatedTransactionsByAgency = insertOrUpdateAgencyMappedData(decimelConversionMapOfTranObjectsForViewProcessingGroupedByAgency,
        masterDataForAccumulatedTransactionsByAgency)

      //#####################################################
      //Accumulate Transacton data by Agency and date
      //#####################################################
      val mapForTransactionObjectsForViewProcessingGroupedByAgencyAndDate: Map[(String, String), Double] = listOfTransactionsForViewProcessing
        .groupBy(transaction => (transaction.translegalEntityID, transaction.transacctDate.take(4)))
        .mapValues(_.map(_.transtransAmount.toDouble).sum)

      val decimelConversionMapOfTranObjectsForViewProcessingByAgencyAndDate = Map[(String, String), BigDecimal]() ++
        mapForTransactionObjectsForViewProcessingGroupedByAgencyAndDate.map { case (k, v) => k -> BigDecimal(v).setScale(2, BigDecimal.RoundingMode.HALF_UP) }

      if (metricsEnabled) {
        accumulatedTransactionsByAgencyAndYearCounter = accumulatedTransactionsByAgencyAndYearCounter +
          decimelConversionMapOfTranObjectsForViewProcessingByAgencyAndDate.size
      }

      masterDataForAccumulatedTransactionsByAgencyAndDate = insertOrUpdateAgencyAndDateMappedData(decimelConversionMapOfTranObjectsForViewProcessingByAgencyAndDate,
        masterDataForAccumulatedTransactionsByAgencyAndDate)

      //#####################################################
      //Accumulate Transaction data by Agency and VendorType
      //#####################################################
      val mapForTransactionObjectsForViewProcessingGroupedByAgencyAndVendorId: Map[(String, String), Double] = listOfTransactionsForViewProcessing
        .groupBy(transaction => (transaction.translegalEntityID, transaction.transinstID)).mapValues(_.map(_.transtransAmount.toDouble).sum)

      val mapForTransactionObjectsForViewProcessingGroupedByAgencyAndVendorType: Map[(String, String), Double] = mapForTransactionObjectsForViewProcessingGroupedByAgencyAndVendorId
        .map { case ((agencyCode, vendorCode), transAmount) => (agencyCode, mergedMapOfVendorToVendorTypeMapping.getOrElse(vendorCode, vendorCode)) -> transAmount }

      val decimelConversionMapOfTranObjectsForViewProcessingGroupedByAgencyAndVendorType = Map[(String, String), BigDecimal]() ++
        mapForTransactionObjectsForViewProcessingGroupedByAgencyAndVendorType.map { case (k, v) => k -> BigDecimal(v).setScale(2, BigDecimal.RoundingMode.HALF_UP) }

      if (metricsEnabled) {
        accumulatedTransactionsByAgencyAndVendorTypeCounter = accumulatedTransactionsByAgencyAndVendorTypeCounter +
          decimelConversionMapOfTranObjectsForViewProcessingGroupedByAgencyAndVendorType.size
      }

      masterDataForAccumulatedTransactionsByAgencyAndVendorType =
        insertOrUpdateAgencyAndVendorMappedData(decimelConversionMapOfTranObjectsForViewProcessingGroupedByAgencyAndVendorType, masterDataForAccumulatedTransactionsByAgencyAndVendorType)

      //#######################################
      //Accumulate Transaction data by Object
      //#######################################
      val mapForTransactionObjectsForViewProcessingGroupedByObject: Map[String, Double] = listOfTransactionsForViewProcessing.groupBy(_.transaccountID.drop(3))
        .mapValues(_.map(_.transtransAmount.toDouble).sum)
      val decimelConversionMapOfTranObjectsForViewProcessingGroupedByRefObject = Map[String, BigDecimal]() ++
        mapForTransactionObjectsForViewProcessingGroupedByObject.map { case (k, v) => k -> BigDecimal(v).setScale(2, BigDecimal.RoundingMode.HALF_UP) }

      if (metricsEnabled) {
        accumulatedTransactionsByObjectCounter = accumulatedTransactionsByObjectCounter +
          decimelConversionMapOfTranObjectsForViewProcessingGroupedByRefObject.size
      }
      masterDataForAccumulatedTransactionsByRefObject = insertOrUpdateObjectMappedData(decimelConversionMapOfTranObjectsForViewProcessingGroupedByRefObject, masterDataForAccumulatedTransactionsByRefObject)


      listOfTransactionObjectWithLowestKey = listOfTransactionObjectWithLowestKey.filterNot(transactionObjectToDrop => BigInt(transactionObjectToDrop.transinstID)
        == inputParamWithLowestCurrentIdKey.currentIdValue)
      listOfTransactionObject = listOfTransactionObject.filterNot(transactionObjectToDrop => BigInt(transactionObjectToDrop.transinstID)
        != inputParamWithLowestCurrentIdKey.currentIdValue)
    }


    // Output file closes
    vendorHistoryWriter.close()
    vendorUpdateWriter.close()
    balanceHistoryWriter.close()
    transactionDailyWriter.close()

    // Input file closes
    vendorHistoryFileIterator.close()
    vendorUpdateFileIterator.close()
    balanceHistoryFileIterator.close()
    transactionFileIterator.close()



    //###################################################
    //Mapping accumulated transactions per Agency Name
    //###################################################
    val mapOfAgenciesToAccumulatedTransactions = masterDataForAccumulatedTransactionsByAgency.map(agency =>
      mapOfAgencies getOrElse(agency._1, "noMatch") match {
        case "noMatch" => agency
        case value => value -> agency._2
      })

    transactionAccmltedByAgncyReportWriter.write((mapOfAgenciesToAccumulatedTransactions + "\n").getBytes)
    transactionAccmltedByAgncyReportWriter.close()

    //##############################################################################
    //Mapping accumulated transactions per Agency Names that do not have department
    //##############################################################################
    val mapOfAgenciesThatDoNotContainTheWordDepartmentInTheirNames = mapOfAgenciesToAccumulatedTransactions.filter(!_._1.contains("Department"))

    transactionAccmltedByAgncyNotContainingDepartmentReportWriter.write((mapOfAgenciesThatDoNotContainTheWordDepartmentInTheirNames + "\n").getBytes)
    transactionAccmltedByAgncyNotContainingDepartmentReportWriter.close()

    //###########################################################
    //Mapping accumulated Transactions per Agency and VendorType
    //###########################################################
    val mapOfAgencyAndVendorTypeToAccumulatedTransactions = masterDataForAccumulatedTransactionsByAgencyAndVendorType
      .map { case ((legalEntityId, vendorType), transAmount) =>
        (mapOfAgencies.getOrElse(legalEntityId, legalEntityId).trim, vendorType.trim) -> transAmount
      }

    transactionsAccmltedByAgncyAndVendorTypeReportWriter.write((mapOfAgencyAndVendorTypeToAccumulatedTransactions + "\n").getBytes)
    transactionsAccmltedByAgncyAndVendorTypeReportWriter.close()

    //###################################################
    //Mapping accumulated transactions per Object Name
    //###################################################
    val mapOfObjectsToAccumulatedTransactions = masterDataForAccumulatedTransactionsByRefObject.map(refObject =>
      mapOfObjects getOrElse(refObject._1.trim().toInt.toString, "noMatch") match {
        case "noMatch" => refObject
        case value => value -> refObject._2
      })

    transactionsAccmltedByObjectWriter.write((mapOfObjectsToAccumulatedTransactions + "\n").getBytes)
    transactionsAccmltedByObjectWriter.close()

    //########################################################
    //Mapping accumulated balances per Agency and Object Name
    //########################################################
    val mapOfAgencyAndObjectNamesToAccumulatedBalances = masterDataForAccumulatedTransactionsByAgencyAndObject
      .map { case ((legalEntityId, accountId), transAmount) => (mapOfAgencies.getOrElse(legalEntityId, legalEntityId).trim,
        mapOfObjects.getOrElse(accountId, accountId).trim) -> transAmount
      }

    balancesAccmltedByAgencyAndObjectWriter.write((mapOfAgencyAndObjectNamesToAccumulatedBalances + "\n").getBytes)
    balancesAccmltedByAgencyAndObjectWriter.close()

    //########################################################
    //Sorting accumulated transactions per Agency and Year
    //########################################################
    val sortedMasterDataOfAccumulatedTransactionByAgencyAndDate = SortedMap(masterDataForAccumulatedTransactionsByAgencyAndDate.toSeq: _*)
    //val sortedMasterDataOfAccumulatedTransactionByAgencyAndDate = ListMap(masterDataForAccumulatedTransactionsByAgencyAndDate.toSeq.sortBy(_._1):_*)

    transactionsAccmltedByAgncyAndYearReportWriter.write((sortedMasterDataOfAccumulatedTransactionByAgencyAndDate + "\n").getBytes)
    transactionsAccmltedByAgncyAndYearReportWriter.close()

    if (metricsEnabled) {
      statisticsAccumulatorWriter.write(("######################################################################################" + "\n").getBytes)
      statisticsAccumulatorWriter.write(("# Execution summary for NGSAFR" + "\n").getBytes)
      statisticsAccumulatorWriter.write(("######################################################################################" + "\n" + "\n" + "\n").getBytes)

      statisticsAccumulatorWriter.write(("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$" + "\n").getBytes)
      statisticsAccumulatorWriter.write(("$$$$ The number of the records read:" + "\n").getBytes)
      statisticsAccumulatorWriter.write(("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$" + "\n").getBytes)
      statisticsAccumulatorWriter.write(("The total count of Agency Data rows read\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t:" + totalNumberOfAgencyDataRows + "\n").getBytes)
      statisticsAccumulatorWriter.write(("The total count of Object Data rows read\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t:" + totalNumberOfRefObjectDataRows + "\n").getBytes)
      statisticsAccumulatorWriter.write(("The total Count of Vendor History Data rows read\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t:" + vendorHistoryCounter + "\n").getBytes)
      statisticsAccumulatorWriter.write(("The total Count of Vendor Update Data rows read\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t:" + vendorUpdateCounter + "\n").getBytes)
      statisticsAccumulatorWriter.write(("The total Count of Balance History Data rows read\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t:" + balanceHistoryCounter + "\n").getBytes)
      statisticsAccumulatorWriter.write(("The total Count of Transaction History Data rows read\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t:" + transactionHistoryCounter + "\n" + "\n" + "\n").getBytes)

      statisticsAccumulatorWriter.write(("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$" + "\n").getBytes)
      statisticsAccumulatorWriter.write(("$$$$ The accumulated records from CKB upon grouping :" + "\n").getBytes)
      statisticsAccumulatorWriter.write(("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$" + "\n").getBytes)
      statisticsAccumulatorWriter.write(("The total no. of rows after CKB data grouped by Agency \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t:" + accumulatedTransactionsByAgencyCounter + "\n").getBytes)
      statisticsAccumulatorWriter.write(("The total no. of rows after CKB data grouped by Agency And Year\t\t\t\t\t\t\t\t\t\t\t\t\t:" + accumulatedTransactionsByAgencyAndYearCounter + "\n").getBytes)
      statisticsAccumulatorWriter.write(("The total no. of rows after CKB data grouped by Agency And VendorType\t\t\t\t\t\t\t\t\t\t\t:" + accumulatedTransactionsByAgencyAndVendorTypeCounter + "\n").getBytes)
      statisticsAccumulatorWriter.write(("The total no. of rows after CKB data grouped by Object\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t:" + accumulatedTransactionsByObjectCounter + "\n").getBytes)
      statisticsAccumulatorWriter.write(("The total no. of rows after CKB data grouped by Agency And Object\t\t\t\t\t\t\t\t\t\t\t\t:" + accumulatedBalancesByAgencyAndObjectCounter + "\n" + "\n" + "\n").getBytes)

      statisticsAccumulatorWriter.write(("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$" + "\n").getBytes)
      statisticsAccumulatorWriter.write(("$$$$ The views based out of accumulated CKB data after performing relevant joins:" + "\n").getBytes)
      statisticsAccumulatorWriter.write(("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$" + "\n").getBytes)
      statisticsAccumulatorWriter.write(("The row count for balances of accumulated transactions by agency\t\t\t\t\t\t\t\t\t\t\t\t:" + mapOfAgenciesToAccumulatedTransactions.size + "\n").getBytes)
      statisticsAccumulatorWriter.write(("The row count for balances of accumulated transactions by agency where agency name doesn't contain Department \t:" + mapOfAgenciesToAccumulatedTransactions.size + "\n").getBytes)
      statisticsAccumulatorWriter.write(("The row count for balances of accumulated transactions by agency and year\t\t\t\t\t\t\t\t\t\t:" + sortedMasterDataOfAccumulatedTransactionByAgencyAndDate.size + "\n").getBytes)
      statisticsAccumulatorWriter.write(("The row count for balances of accumulated transactions by agency and vendor type\t\t\t\t\t\t\t\t:" + mapOfAgencyAndVendorTypeToAccumulatedTransactions.size + "\n").getBytes)
      statisticsAccumulatorWriter.write(("The row count for balances of accumulated transactions by object\t\t\t\t\t\t\t\t\t\t\t\t:" + mapOfObjectsToAccumulatedTransactions.size + "\n").getBytes)
      statisticsAccumulatorWriter.write(("The row count for balances of accumulated balances by agency and object\t\t\t\t\t\t\t\t\t\t\t:" + mapOfAgencyAndObjectNamesToAccumulatedBalances.size + "\n" + "\n" + "\n").getBytes)

      val t3 = System.currentTimeMillis
      statisticsAccumulatorWriter.write(("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$" + "\n").getBytes)
      statisticsAccumulatorWriter.write(("$$$$ The elapsed time\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t:" + (t3 - t1) + " milliseconds" + "\n").getBytes)
      statisticsAccumulatorWriter.write(("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$" + "\n").getBytes)
    }

    statisticsAccumulatorWriter.close()

    /*println("Accumulated transactions by Agency name")
    println(mapOfAgenciesToAccumulatedTransactions)

    println("Agency names that does not contain Department")
    println(mapOfAgenciesThatDoNotContainTheWordDepartmentInTheirNames)

    println("Accumulated transactions by Object")
    println(mapOfObjectsToAccumulatedTransactions)

    println("Accumulated balances by Agency and Object")
    println(mapOfAgencyAndObjectNamesToAccumulatedBalances)

    println("Accumulated transactions by Agency and Vendor type")
    println(mapOfAgencyAndVendorTypeToAccumulatedTransactions)

    println("Accumulated transactions by AgencyCode And Year")
    println(sortedMasterDataOfAccumulatedTransactionByAgencyAndDate)*/
    println("Completed...")
    println("#################################################################################")
    val t2 = System.currentTimeMillis
    println((t2 - t1) + " milliseconds")
  }

  private def populateListOfVendorHistoryObjectsMatchingLowestKey(vendor: => Vendor): List[Vendor]

  = {
    listOfVendorHistoryObjectWithLowestKey = vendor :: listOfVendorHistoryObjectWithLowestKey
    listOfVendorHistoryObjectWithLowestKey
  }

  private def populateListOfVendorHistoryObjects(vendor: => Vendor): List[Vendor]

  = {
    listOfVendorHistoryObject = vendor :: listOfVendorHistoryObject
    listOfVendorHistoryObject
  }

  private def populateListOfVendorUpdateObjectsMatchingLowestKey(vendorUpdate: => VendorUpdate): List[VendorUpdate]

  = {
    listOfVendorUpdateObjectWithLowestKey = vendorUpdate :: listOfVendorUpdateObjectWithLowestKey
    listOfVendorUpdateObjectWithLowestKey
  }

  private def populateListOfVendorUpdateObjects(vendorUpdate: => VendorUpdate): List[VendorUpdate]

  = {
    listOfVendorUpdateObject = vendorUpdate :: listOfVendorUpdateObject
    listOfVendorUpdateObject
  }

  private def populateListOfBalanceObjectsMatchingLowestKey(balance: => Balances): List[Balances]

  = {
    listOfBalanceObjectWithLowestKey = balance :: listOfBalanceObjectWithLowestKey
    listOfBalanceObjectWithLowestKey
  }

  private def populateListOfBalanceObjects(balance: => Balances): List[Balances]

  = {
    listOfBalanceObject = balance :: listOfBalanceObject
    listOfBalanceObject
  }

  private def populateListOfTransactionObjectsMatchingLowestKey(transaction: => Transaction): List[Transaction]

  = {
    listOfTransactionObjectWithLowestKey = transaction :: listOfTransactionObjectWithLowestKey
    listOfTransactionObjectWithLowestKey
  }

  private def populateListOfTransactionObjects(transaction: => Transaction): List[Transaction]

  = {
    listOfTransactionObject = transaction :: listOfTransactionObject
    listOfTransactionObject
  }

  private def setupInputParams(): ListBuffer[InputParams]

  = {
    val listInputParams = ListBuffer[InputParams]()
    val vendorHistoryInputParam =  InputParams("Vendor", "History", 0, "VAVEND.INSTID",
      1, 10, 12, 10, 0, 0, null)
    val vendorUpdateInputParam =  InputParams("Vendor", "Update", 0, "VAVUPD.INSTID",
      1, 10, 0, 0, 0, 0, null)
    val balancesInputParam =  InputParams("Balance", "History", 0, "VABAL.INSTID",
      1, 10, 0, 0, 0, 0, null)
    val transactionInputParam =  InputParams("Transaction", "Daily", 0, "VATRAN.INSTID",
      1, 10, 0, 0, 0, 0, null)


    listInputParams.append(vendorHistoryInputParam)
    listInputParams.append(vendorUpdateInputParam)
    listInputParams.append(balancesInputParam)
    listInputParams.append(transactionInputParam)

    //println(listInputParams.minBy(InputParams => InputParams.currentIdLowKeyValue))
    /* listInputParams.sortBy(InputParams => InputParams.currentIdLowKeyValue).foreach(println)*/

    listInputParams
  }

  def insertOrUpdateAgencyAndDateMappedData(mappedAgencyAndDateData: => Map[(String, String), BigDecimal],
                                            masterDataForAccumulatedTransactionsByAgencyAndDate: => Map[(String, String), BigDecimal]): Map[(String, String), BigDecimal] = {
    val masterAgencyDateDataMap: Map[(String, String), BigDecimal] =
      (mappedAgencyAndDateData foldLeft
        masterDataForAccumulatedTransactionsByAgencyAndDate) ((key, transAmount) => key + (transAmount._1
          -> (transAmount._2 + key.getOrElse(transAmount._1, 0.0))))

    masterAgencyDateDataMap
  }

  def insertOrUpdateAgencyAndVendorMappedData(mappedAccumulatedTransactions: => Map[(String, String), BigDecimal],
                                              masterDataForAccumulatedTransactionsByAgencyAndVendorType: => Map[(String, String), BigDecimal]): Map[(String, String), BigDecimal] = {
    val masterAgencyVendorDataMap: Map[(String, String), BigDecimal] =
      (mappedAccumulatedTransactions foldLeft
        masterDataForAccumulatedTransactionsByAgencyAndVendorType) ((key, transAmount) => key + (transAmount._1
        -> (transAmount._2 + key.getOrElse(transAmount._1, 0.0))))

    masterAgencyVendorDataMap
  }

  def insertOrUpdateAgencyAndObjectMappedData(mappedAccumulatedTransactions: => Map[(String, String), BigDecimal],
                                              masterDataForAccumulatedTransactionsByAgencyAndObject: => Map[(String, String), BigDecimal]): Map[(String, String), BigDecimal] = {
    val masterAgencyObjectDataMap: Map[(String, String), BigDecimal] =
      (mappedAccumulatedTransactions foldLeft
        masterDataForAccumulatedTransactionsByAgencyAndObject ) ((key, transAmount) => key +
        (transAmount._1 -> (transAmount._2 + key.getOrElse(transAmount._1, 0.0))))

    masterAgencyObjectDataMap
  }

  def populateObjectData(filePaths: => Map[String, String], encoding: => String): List[RefObject] = {
    println("Object Reference File: " + ZFile.getSlashSlashQuotedDSN(filePaths("inputPath") + "VAREF.OBJECT.FIXLDATA'"))
    val refObjectFileIterator: RecordReader = RecordReader.newReader(
      ZFile.getSlashSlashQuotedDSN(filePaths("inputPath") + "VAREF.OBJECT.FIXLDATA'"), ZFileConstants.FLAG_DISP_SHR)
    val refObjectCurrentData = new Array[Byte](refObjectFileIterator.getLrecl)
    //var agencyCurrentRecord: Array[String] = new Array[String](3)
    var lengthOfRefObjectCurrentData:Int = 0
    var listOfRefObjects = List[RefObject]()

    while ({lengthOfRefObjectCurrentData = refObjectFileIterator.read(refObjectCurrentData); lengthOfRefObjectCurrentData} >= 0) {
      val refObjectTblLineStrValue = new String(refObjectCurrentData,0,lengthOfRefObjectCurrentData,encoding)
      val refObject = RefObject.apply(refObjectTblLineStrValue.substring(3, 7).trim, refObjectTblLineStrValue.substring(98, 179))
      listOfRefObjects = refObject :: listOfRefObjects
      totalNumberOfRefObjectDataRows += 1
    }
    refObjectFileIterator.close()
    listOfRefObjects.sortBy(_.objectCode)
  }

  def insertOrUpdateObjectMappedData[K, V](mappedAccumulatedTransactions: => Map[String, BigDecimal],
                                           masterObjectDataForAccumulatedTransactionsParams: => Map[String, BigDecimal]): Map[String, BigDecimal] = {
    val masterObjectDataMap: Map[String, BigDecimal] =
      (mappedAccumulatedTransactions foldLeft
         masterObjectDataForAccumulatedTransactionsParams) ((accountId, transAmount) => accountId + (transAmount._1
        -> (transAmount._2 + accountId.getOrElse(transAmount._1, 0.0))))

    masterObjectDataMap
  }

  def populateAgencyData(filePaths: => Map[String, String], encoding: => String): List[Agency] = {
    println("Agency Reference File: " + ZFile.getSlashSlashQuotedDSN(filePaths("inputPath") + "VAREF.AGENCY.FIXLDATA'"))
    val agencyFileIterator: RecordReader = RecordReader.newReader(
      ZFile.getSlashSlashQuotedDSN(filePaths("inputPath") + "VAREF.AGENCY.FIXLDATA'"), ZFileConstants.FLAG_DISP_SHR)
    val agencyCurrentData = new Array[Byte](agencyFileIterator.getLrecl)
    //var agencyCurrentRecord: Array[String] = new Array[String](3)
    var lengthOfAgencyCurrentData:Int = 0
    var listOfAgencyObjects = List[Agency]()

    while ({lengthOfAgencyCurrentData = agencyFileIterator.read(agencyCurrentData); lengthOfAgencyCurrentData} >= 0 ) {
      val agencyTblLineStrValue = new String(agencyCurrentData,0,lengthOfAgencyCurrentData,encoding)
      val agencyObject = Agency.apply(agencyTblLineStrValue.substring(4, 8), agencyTblLineStrValue.substring(8, 88).trim)
      //      agencyCurrentRecord = agencyCurrentData.next().split(",")
      //      val agencyObject = Agency.apply(agencyCurrentRecord(0), agencyCurrentRecord(2))
      listOfAgencyObjects = agencyObject :: listOfAgencyObjects
      totalNumberOfAgencyDataRows += 1
    }
    agencyFileIterator.close()
    listOfAgencyObjects.sortBy(_.agencyCode)
  }

  def insertOrUpdateAgencyMappedData[K, V](mappedAccumulatedTransactions: => Map[String, BigDecimal],
                                           masterAgencyDataForAccumulatedTransactionsParams: => Map[String, BigDecimal]): Map[String, BigDecimal] = {
    val masterAgencyDataMap: Map[String, BigDecimal] =
      (mappedAccumulatedTransactions foldLeft
        masterAgencyDataForAccumulatedTransactionsParams) ((agencyCode, transAmount) => agencyCode + (transAmount._1
        -> (transAmount._2 + agencyCode.getOrElse(transAmount._1, 0.0))))

    masterAgencyDataMap
  }

  def iterateThroughVendorHistory(vendorHistoryCurrentData: => Array[Byte], vendorHistoryFileIterator: => RecordReader, encoding: => String): Array[String] = {

    lengthOfTheVendorHistoryCurrentData = vendorHistoryFileIterator.read(vendorHistoryCurrentData)
    val vendorHistoryCurrentRecord: Array[String] = new Array[String](8)

    if (lengthOfTheVendorHistoryCurrentData != -1) {
      val instTBLLineStrValue = new String(vendorHistoryCurrentData, 0, lengthOfTheVendorHistoryCurrentData, encoding)
      vendorHistoryCurrentRecord(0) = instTBLLineStrValue.substring(0, 10)
      vendorHistoryCurrentRecord(1) = instTBLLineStrValue.substring(11, 21)
      vendorHistoryCurrentRecord(2) = instTBLLineStrValue.substring(22, 23)
      vendorHistoryCurrentRecord(3) = instTBLLineStrValue.substring(24, 34)
      vendorHistoryCurrentRecord(4) = instTBLLineStrValue.substring(35, 36)
      vendorHistoryCurrentRecord(5) = instTBLLineStrValue.substring(37, 67)
      vendorHistoryCurrentRecord(6) = instTBLLineStrValue.substring(68, 71)
      vendorHistoryCurrentRecord(7) = instTBLLineStrValue.substring(72, 73)
    }
    vendorHistoryCurrentRecord
  }

  def iterateThroughVendorUpdate(vendorUpdateCurrentData: => Array[Byte], vendorUpdateFileIterator: => RecordReader, encoding: => String): Array[String] = {

    lengthOfTheVendorUpdateCurrentData = vendorUpdateFileIterator.read(vendorUpdateCurrentData)
    val vendorUpdateCurrentRecord: Array[String] = new Array[String](4)

    if (lengthOfTheVendorUpdateCurrentData != -1) {
      val instTBLLineStrValue = new String(vendorUpdateCurrentData, 0, lengthOfTheVendorUpdateCurrentData, encoding)
      vendorUpdateCurrentRecord(0) = instTBLLineStrValue.substring(0, 10)
      vendorUpdateCurrentRecord(1) = instTBLLineStrValue.substring(11, 51)
      vendorUpdateCurrentRecord(2) = instTBLLineStrValue.substring(52, 55)
      vendorUpdateCurrentRecord(3) = instTBLLineStrValue.substring(56, 66)
    }
    vendorUpdateCurrentRecord
  }

  def iterateThroughBalanceHistory(balanceHistoryCurrentData: => Array[Byte], balanceHistoryFileIterator: => RecordReader, encoding: => String): Array[String] = {

    lengthOfTheBalanceHistoryCurrentData = balanceHistoryFileIterator.read(balanceHistoryCurrentData)
    val balanceHistoryCurrentRecord: Array[String] = new Array[String](19)

    if (lengthOfTheBalanceHistoryCurrentData != -1) {
      val balanceLineStrValue = new String(balanceHistoryCurrentData, 0, lengthOfTheBalanceHistoryCurrentData, encoding)
      balanceHistoryCurrentRecord(0) = balanceLineStrValue.substring(0, 10)
      balanceHistoryCurrentRecord(1) = balanceLineStrValue.substring(11, 21)
      balanceHistoryCurrentRecord(2) = balanceLineStrValue.substring(22, 29)
      balanceHistoryCurrentRecord(3) = balanceLineStrValue.substring(30, 33)
      balanceHistoryCurrentRecord(4) = balanceLineStrValue.substring(34, 48)
      balanceHistoryCurrentRecord(5) = balanceLineStrValue.substring(49, 53)
      balanceHistoryCurrentRecord(6) = balanceLineStrValue.substring(54, 59)
      balanceHistoryCurrentRecord(7) = balanceLineStrValue.substring(60, 65)
      balanceHistoryCurrentRecord(8) = balanceLineStrValue.substring(66, 67)
      balanceHistoryCurrentRecord(9) = balanceLineStrValue.substring(68, 74)
      balanceHistoryCurrentRecord(10) = balanceLineStrValue.substring(75, 78)
      balanceHistoryCurrentRecord(11) = balanceLineStrValue.substring(79, 82)
      balanceHistoryCurrentRecord(12) = balanceLineStrValue.substring(83, 86)
      balanceHistoryCurrentRecord(13) = balanceLineStrValue.substring(87, 94)
      balanceHistoryCurrentRecord(14) = balanceLineStrValue.substring(95, 99)
      balanceHistoryCurrentRecord(15) = balanceLineStrValue.substring(100, 112)
      balanceHistoryCurrentRecord(16) = balanceLineStrValue.substring(113, 115)
      //balanceHistoryCurrentRecord(17) = balanceLineStrValue.substring(116, 117)
      //balanceHistoryCurrentRecord(18) = balanceLineStrValue.substring(119, 120)
    }
    balanceHistoryCurrentRecord
  }

  def iterateThroughTransactions(transactionCurrentData: => Array[Byte], transactionFileIterator: => RecordReader, encoding: => String): Array[String] = {

    lengthOfTheTransactionCurrentData = transactionFileIterator.read(transactionCurrentData)
    val transactionCurrentRecord: Array[String] = new Array[String](31)

    if (lengthOfTheTransactionCurrentData != -1) {
      val transLineStrValue = new String(transactionCurrentData, 0, lengthOfTheTransactionCurrentData, encoding)
      transactionCurrentRecord(0) = transLineStrValue.substring(0, 10)
      transactionCurrentRecord(1) = transLineStrValue.substring(11, 28)
      transactionCurrentRecord(2) = transLineStrValue.substring(29, 31)
      transactionCurrentRecord(3) = transLineStrValue.substring(31, 71)
      transactionCurrentRecord(4) = transLineStrValue.substring(72, 79)
      transactionCurrentRecord(5) = transLineStrValue.substring(80, 83)
      transactionCurrentRecord(6) = transLineStrValue.substring(84, 98)
      transactionCurrentRecord(7) = transLineStrValue.substring(99, 103)
      transactionCurrentRecord(8) = transLineStrValue.substring(103, 109)
      transactionCurrentRecord(9) = transLineStrValue.substring(110, 115)
      transactionCurrentRecord(10) = transLineStrValue.substring(116, 118)
      transactionCurrentRecord(11) = transLineStrValue.substring(118, 124)
      transactionCurrentRecord(12) = transLineStrValue.substring(125, 128)
      transactionCurrentRecord(13) = transLineStrValue.substring(129, 132)
      transactionCurrentRecord(14) = transLineStrValue.substring(133, 136)
      transactionCurrentRecord(15) = transLineStrValue.substring(137, 144)
      transactionCurrentRecord(16) = transLineStrValue.substring(145, 157)
      transactionCurrentRecord(17) = transLineStrValue.substring(158, 165)
      transactionCurrentRecord(18) = transLineStrValue.substring(166, 176)
      transactionCurrentRecord(19) = transLineStrValue.substring(177, 187)
      transactionCurrentRecord(20) = transLineStrValue.substring(188, 189)
      transactionCurrentRecord(21) = transLineStrValue.substring(190, 191)
      transactionCurrentRecord(22) = transLineStrValue.substring(192, 193)
      transactionCurrentRecord(23) = transLineStrValue.substring(194, 195)
      transactionCurrentRecord(24) = transLineStrValue.substring(196, 197)
      transactionCurrentRecord(25) = transLineStrValue.substring(198, 199)
      transactionCurrentRecord(26) = transLineStrValue.substring(200, 201)
      transactionCurrentRecord(27) = transLineStrValue.substring(202, 203)
      transactionCurrentRecord(28) = transLineStrValue.substring(204, 205)
      transactionCurrentRecord(29) = transLineStrValue.substring(206, 207)
      transactionCurrentRecord(30) = transLineStrValue.substring(208, 209)
    }
    transactionCurrentRecord
  }

}
