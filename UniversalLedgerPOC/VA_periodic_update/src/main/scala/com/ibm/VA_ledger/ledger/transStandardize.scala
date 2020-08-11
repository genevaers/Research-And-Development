package com.ibm.VA_ledger.ledger

import java.io.{File, PrintWriter}
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import java.util.Calendar
import sys.process._
import com.ibm.VA_ledger.datatypes.Ledger
import com.ibm.VA_ledger.datatypes.Transaction
import com.ibm.VA_ledger.datatypes.UnviJournalAllEntity


//***************************************************************************************************************
//
//  Program:  dataStandarize - Standardize Data into a common journal entry
//
//  This program reads transaction data and produces standardized journals
//
//
//  *(c) Copyright IBM Corporation. 2018
//  * SPDX-License-Identifier: Apache-2.0
//  * By Kip Twitchell
//  Created July 2018
//
//  Change Log:
//  To Do:
//         - Fix Direct VS. Offset Flag
//         - ReConsider Business Event File
//***************************************************************************************************************


object transStandardize {
  def apply(fileInLocation: String, fileOutLocation: String): Unit = {

    println("*" * 100)
    println("                                Data Transformation Module")
    println("Start Time: " + Calendar.getInstance.getTime)
    println("*" * 100)


    if (fileInLocation.length == 0) {
      println("Invalid input file invocation parameters.  Program abort.")
      sys.exit(0)
    }
    if (fileOutLocation.length == 0) {
      println("Invalid output file invocation parameters.  Program abort.")
      sys.exit(0)
    }

    val VAInDataPath = fileInLocation
    val VAOutDataPath = fileOutLocation

    //*****************************************************************************************
    // Open File Name Table
    //*****************************************************************************************
    //
    val fileNames = Source.fromFile(VAInDataPath + "FileList.csv").getLines
    fileNames.drop(1) //Drop header record
    // Schema:  POFileName,PayFileName,Contents,RecFormat,Delimiter,FileCount,FileYear,FileQtr,RecCount,ControlAmtSum
    println("FileName,Type (PO or Purchase),Contents(Expense vs Revenue),RecFormat(VA Format),Delimiter(Text vs Comma),FileCount,FileYear,FileQtr,RecCount,ControlAmtSum")

    // Open Output File and Write Header Record
    val fileName = VAOutDataPath + "JEHeader.csv"
    val outHeader: PrintWriter = new PrintWriter(new File(fileName))

    tranHeader(outHeader)
    outHeader.close()

    var transRead = 0 //counter for number of records read
    var poFilesProcessed = 0 //counter for PO files processed
    var payFilesProcessed = 0 //counter for Payment files processed
    var journalsLinesWritten = 0
    var journalsWritten = 0

    //*****************************************************************************************
    // Process Loop
    //*****************************************************************************************
    //

    // Process each entry in the file name listing
    for (fn <- fileNames) {
      val cols = fn.split(",").map(_.trim)
      val FileName = cols(0)
      val Type = cols(1)
      val Contents = cols(2)
      val RecFormat = cols(3)
      val Delimiter = cols(4)
      val FileCount = cols(5)
      val FileYear = cols(6)
      val FileQtr = cols(7)
      val RecCount = cols(8)
      val ControlAmtSum = cols(9)
      payFilesProcessed += 1
      println("processing file: " + fn)

      var transOutRec: UnviJournalAllEntity = null
      val JrnlDate = (FileQtr.toInt * 3).toString + "/" + "01" + "/" + FileYear
      val fileDelimiter = Delimiter match {
        case "T" => "\t";
        case "C" => ","
      }


      Type match {
        // Test for File types to parse correctly
        case "PO" => processPOs()
        case "Pay" => processPayments()
      }

      def processPOs(): Unit = {
        //*****************************************************************************************
        // Process PO Data
        //*****************************************************************************************
        println("PO File")

        // Open Output File and Write Header Record
        val fileName = VAOutDataPath + "PO_" + FileYear + ".csv"
        val outPOs: PrintWriter = new PrintWriter(new File(fileName))

        // Expense type record structure
        case class poCLS(
                          poNumber: String,
                          poAgency: String,
                          poOrderedDate: String,
                          poJrnlDate: String,
                          poVendorCommodityDesc: String,
                          poQuantityOrdered: BigDecimal,
                          poPrice: BigDecimal,
                          poVendorID: String,
                          poVendorName: String,
                          poVendorAddress: String,
                          poVendorCity: String,
                          poVendorState: String,
                          poVendorPostalCode: String,
                          poVendorLocEmailAddress: String,
                          poNIGPcode: String,
                          poNIGPDescription: String,
                          poUnitOfMeasureCode: String,
                          poUnitOfMeasureDesc: String,
                          poVendorPartNumber: String,
                          poManPartNumber: String,
                                            )
        val poRaw = ArrayBuffer(poCLS("", "", "", "", "", 0, 0, "", "", "","","","","","","","","","",""))

        // Read input records
        val lineIn: Seq[String] = Source.fromFile(VAInDataPath + FileName).getLines.toList.drop(1)
        for (i <- lineIn) {
          val cols = i.split(fileDelimiter).map(_.trim)
          poRaw.+=(poCLS(cols(0), cols(1), cols(2), JrnlDate, cols(3), BigDecimal(cols(4)), BigDecimal(cols(5)),
            cols(6),cols(7),cols(8),cols(9), cols(10), cols(11), cols(12),cols(13),cols(14),cols(15), cols(16),cols(17),cols(18) ))
        }
        //Format output records
        var jrnlID = 0
        for (rec <- poRaw) {
          jrnlID += 1
          journalsWritten += 1

          // Journal Line 1 ("debit" side or original entry)
          transOutRec = UnviJournalAllEntity
            (
    //            transIPID = "0",
    //            transContractID = "0",
    //            transCommitmentID = "0",
    //            transJrnlID = "ID" + jrnlID.toString + JrnlDate,
    //            transJrnlLineID = "1",
    //            transBusinessEventCode = "Procurement",
    //            transSourceSystemID = "\"Payment Sys\"",
    //            transOriginalDocID = "Unknown",
    //            transJrnlDescript = "Procurement for goods or services", //tranVendorName in universal journal
    //            transLegalEntityID = rec.poAgency,
    //            transCenterID = rec.poAgencytranFund, // assign years 03 - 9 to Expense type A record structure
    //            transProjectID = rec.tranObj,
    //            transNominalAccountID = "EXP" + rec.tranProg,
    //            transFiscalPeriod = FileYear,
    //            transAcctDate = JrnlDate,
    //            transTransDate = rec.poOrderedDate,
    //            transTransAmount = rec.poQuantityOrdered * rec.poPrice,
    //            transDirVsOffsetFlg = "D",
    //            transAdjustFlg = "N"
    //          )
              //Universal Journal
              ujIPID = " ",
              ujInstrumentID = " ",
              ujCommitmentID = " ",
              ujJrnlID = " ",
              ujJrnlLineID = " ",
              ujBusEventCD = " ",
              ulSourceID = " ",
              ujProcessID = " ",
              ujOriginalDocID = " ",
              ujJrnlDescript = " ",
    //          ujLedgerID= " "      = "ACTUALS",
    //          ujJrnlType= " "      = "FIN",
    //          ujBookCodeID= " "    = "SHRD-3RD-PARTY",
              ujLegalEntityID = " ",
              ujCenterID = " ",
              ujAffliateEntityID = " ",
              ujAffliateCenterID = " ",
              ujProjectID = " ",
    //          ujProductID= " "  = "0",
              ujAccountID = " ",
    //          ujAltAccountID= " "                 = "0",
    //          ujCurrencyCodeSourceID= " "         = "USD",
    //          ujCurrencyTypeCodeSourceID= " "     = "TXN",
    //          ujCurrencyCodeTargetID= " "         = "USD",
    //          ujCurrencyTypeCodeTargetID= " "     = "BASE-LE",
              ujFiscalPeriod = " ",
              ujAcctDate = " ",
              ujTransDate = " ",
              ujMovementFlg = true,
              ujDirVsOffsetFlg = false,
    //          ujReconcileFlg: Boolean = false,
              ujAdjustFlg = false,
              ujUnitOfMeasure = " ",
              ujUnitPrice = 0, //: BigDecimal,
              ujTransAmount= 0, //: BigDecimal,
              ujStatisticAmount= 0, //: BigDecimal,
    //          ujRuleSetID= " " = " ",
    //          ujRuleID= " " = " ",
              ujExtensionIDSource = " ",
              ujExtnesionSourceType = " ",
    //          ujExtensionIDAuditTrail= " " = " ",
    //          ujExtensionIDClass= " " = " ",
    //          ujExtensionIDDates= " " = " ",
    //          ujExtensionIDCustom= " " = " ",

              //Universal Header
              uhIPID = " ",
              uhInstrumentID = " ",
              uhCommitmentID = " ",
              uhHeaderID = " ",
              uhEntryDate = " ",
              uhCreateDate = " ",
    //          uhUpdateDate= " " = " ",
    //          uhPostedDate= " " = " ",
    //          uhApproverID= " " = " ",
    //          uhCreatorID= " " = " ",
    //          uhCreditHashTotal: BigDecimal = 0,
    //          uhDebitHashTotal: BigDecimal = 0,

              //Commitment
              ucIPID = " ",
              ucInstrumentID = " ",
              ucCommitmentID = " ",
              ucContractType = " ",
              ucContractStattDate = " ",

              //Instrument/Contract
              uiIPID = " ",
              uiInstrumentID = " ",
              uiInstrumentType = " ",
              uiInstrumentStattDate = " ",

              //Invovled Party
              ipIPID = " ",
              ipName = " ",
              ipAddress = " ",
              ipCity = " ",
              ipState = " ",
              ipCountry = " ",
              ipPostalCode = " ",
              ipEmail = " ",
              ipExternalID = " ",
    //          var ipSourceSystemID= " " = " ",
    //          var ipUnintID= " " = " ",
    //          var ipDistrictID= " " = " ",


              //Trading Partners
              tpIPID = " ",
              tpRelatedIPID = " ",

              //Universal Journal Extension
              //Original PO Transaction
              ujepoIPID = " ",
              ujepoInstrumentID = " ",
              ujepoCommitmentID = " ",
              ujepoInstID = " ",
              ujepoJrnlID = " ",
              ujepoJrnlLineID = " ",
              ujepoExtensionIDSource = " ",
              ujepoPONumber = " ",
              ujepoAgency = " ",
              ujepoOrderedDate = " ",
              ujepoVendorCommodityDesc = " ",
              ujepoQuantityOrdered = 0, //: BigDecimal,
              ujepoPrice = 0, //: BigDecimal,
              ujepoVendorID = " ",
              ujepoVendorName = " ",
              ujepoVendorAddress = " ",
              ujepoVendorCity = " ",
              ujepoVendorState = " ",
              ujepoVendorPostalCode = " ",
              ujepoVendorLocEmailAddress = " ",
              ujepoNIGPcode = " ",
              ujepoNIGPDescription = " ",
              ujepoUnitOfMeasureCode = " ",
              ujepoUnitOfMeasureDesc = " ",
              VendorPartNumber = " ",
              ujepoManPartNumber = " ",

              //Original Payment Tran
              ujepyIPID= " ",
              ujepyInstrumentID= " ",
              ujepyCommitmentID= " ",
              ujepyJrnlID= " ",
              ujepyJrnlLineID= " ",
              ujepyExtensionIDSource= " ",
              ujepyAgencyKey= " ",
              ujepyFundDetailKey = " ",
              ujepyObjectKey = " ",
              ujepySubProgramKey = " ",
              ujepyVendorName = " ",
              ujepyAmount = 0 //: BigDecimal
            )
          
          outPOs.write(transOutRec.toString.substring(12, transOutRec.toString.length() - 1) + "\n")
          journalsLinesWritten += 1


          // Journal line 2 ("credit" side or offset)
          transOutRec = Transaction(
            transIPID = "0",
            transContractID = "0",
            transCommitmentID = "0",
            transJrnlID = "ID" + jrnlID.toString + JrnlDate,
            transJrnlLineID = "2", //create journal line 2
            transBusinessEventCode = "Cash Disbursement",
            transSourceSystemID = "\"Payment Sys\"",
            transOriginalDocID = "Unknown",
            transJrnlDescript = "Check to vendor",
            transLegalEntityID = rec.poAgency,
            transCenterID = "0000",
            transProjectID = "0000",
            transNominalAccountID = "0000",
            transFiscalPeriod = FileYear,
            transAcctDate = JrnlDate,
            transTransDate = rec.poOrderedDate,
            transTransAmount = rec.poPrice * rec.poQuantityOrdered * -1, //reverse sign on entry
            transDirVsOffsetFlg = "D",
            transAdjustFlg = "N"
          )
          outPOs.write(transOutRec.toString.substring(12, transOutRec.toString.length() - 1) + "\n")
          journalsLinesWritten += 1

        }
        outPOs.close()
      }


      def processPayments(): Unit = {
        //*****************************************************************************************
        // Process Payments
        //*****************************************************************************************

        Contents match {
          // Test for File types to parse correctly

          case "E" =>
            // Process Expense files
            println("Expense File")

            // Open Output File and Write Header Record
            val fileName = VAOutDataPath + "JE" + FileName.substring(0, 9) + ".csv"
            val outJournals: PrintWriter = new PrintWriter(new File(fileName))

            // Expense type record structure
            case class transECLS(tranAgency: String, tranFund: String, tranObj: String, tranProg: String,
                                 tranVendorName: String, tranDate: String, tranAmt: BigDecimal)
            val transRaw = ArrayBuffer(transECLS("", "", "", "", "", "", 0))

            // Read input records
            val lineIn = Source.fromFile(VAInDataPath + FileName).getLines.toList.drop(1)
            for (i <- lineIn) {
              val cols = i.split(fileDelimiter).map(_.trim)
              RecFormat match {
                case "A" => transRaw.+=(transECLS(cols(0), cols(1), cols(2), cols(3), cols(4), JrnlDate, BigDecimal(cols(5)))) // assign years 03 - 11 to Expense type A record structure
                case "B" => transRaw.+=(transECLS(cols(0), cols(1), cols(2), cols(3), cols(4), cols(6), BigDecimal(cols(5)))) // assign years 12 - 13 to Expense type B record structure
                case "C" => transRaw.+=(transECLS(cols(0), cols(2), cols(3), cols(4), cols(5), cols(6), BigDecimal(cols(1)))) // assign years 14 - 16 to Expense type C record structure
              }
            }

            //Aggregate input data based upon four of five fields
            case class aggTrans(tranAgency: String, tranFund: String, tranObj: String, tranProg: String, tranDate: String)
            implicit object OrderAggTrans extends Ordering[aggTrans] {
              def compare(a: aggTrans, b: aggTrans): Int = {
                if (a.tranAgency == b.tranAgency) {
                  if (b.tranFund == b.tranFund) {
                    if (a.tranObj == b.tranObj) {
                      if (a.tranProg == b.tranProg) {
                        if (a.tranDate == b.tranDate) {
                          0
                        }
                        a.tranDate compare b.tranDate
                      }
                      a.tranProg compare b.tranProg
                    }
                    a.tranObj compare b.tranObj
                  }
                  a.tranFund compare b.tranFund
                }
                a.tranAgency compare b.tranAgency
              }
            }
            var aggAllSmallTBL: mutable.Map[aggTrans, BigDecimal] = mutable.Map.empty

            for (i <- transRaw) {
              transRead += 1
              val w = aggTrans(i.tranAgency, i.tranFund, i.tranObj, i.tranProg, i.tranDate)
              if (aggAllSmallTBL contains w) {
                aggAllSmallTBL.+=(w -> (aggAllSmallTBL(w) + i.tranAmt))
              }
              else aggAllSmallTBL.+=(w -> i.tranAmt)
            }

            //sort the aggregated journal entries
            val sortedAggAllSmallTBL = aggAllSmallTBL.toList.
              sortBy(_._1.tranDate).
              sortBy(_._1.tranProg).
              sortBy(_._1.tranObj).
              sortBy(_._1.tranFund).
              sortBy(_._1.tranAgency).drop(1)

            //Format output records
            var jrnlID = 0
            for (rec <- sortedAggAllSmallTBL) {
              jrnlID += 1
              journalsWritten += 1

              // Journal Line 1 ("debit" side or original entry)
              transOutRec = Transaction(
                transIPID = "0",
                transContractID = "0",
                transCommitmentID = "0",
                transJrnlID = "ID" + jrnlID.toString + JrnlDate,
                transJrnlLineID = "1",
                transBusinessEventCode = "Procurement",
                transSourceSystemID = "\"Payment Sys\"",
                transOriginalDocID = "Unknown",
                transJrnlDescript = "Procurement for goods or services", //tranVendorName in universal journal
                transLegalEntityID = rec._1.tranAgency,
                transCenterID = rec._1.tranFund, // assign years 03 - 9 to Expense type A record structure
                transProjectID = rec._1.tranObj,
                transNominalAccountID = "EXP" + rec._1.tranProg,
                transFiscalPeriod = FileYear,
                transAcctDate = JrnlDate,
                transTransDate = rec._1.tranDate,
                transTransAmount = rec._2,
                transDirVsOffsetFlg = "D",
                transAdjustFlg = "N"
              )
              outJournals.write(transOutRec.toString.substring(12, transOutRec.toString.length() - 1) + "\n")
              journalsLinesWritten += 1


              // Journal line 2 ("credit" side or offset)
              transOutRec = Transaction(
                transIPID = "0",
                transContractID = "0",
                transCommitmentID = "0",
                transJrnlID = "ID" + jrnlID.toString + JrnlDate,
                transJrnlLineID = "2", //create journal line 2
                transBusinessEventCode = "Cash Disbursement",
                transSourceSystemID = "\"Payment Sys\"",
                transOriginalDocID = "Unknown",
                transJrnlDescript = "Check to vendor",
                transLegalEntityID = rec._1.tranAgency,
                transCenterID = "0000",
                transProjectID = "0000",
                transNominalAccountID = "0000",
                transFiscalPeriod = FileYear,
                transAcctDate = JrnlDate,
                transTransDate = rec._1.tranDate,
                transTransAmount = rec._2 * -1, //reverse sign on entry
                transDirVsOffsetFlg = "D",
                transAdjustFlg = "N"
              )
              outJournals.write(transOutRec.toString.substring(12, transOutRec.toString.length() - 1) + "\n")
              journalsLinesWritten += 1

            }
            outJournals.close()


          case "R" =>
            // process Revenue Records
            println("Revenue File")

            // Open Output File and Write Header Record
            val fileName = VAOutDataPath + "JE" + FileName.substring(0, 7) + ".csv"
            val outJournals: PrintWriter = new PrintWriter(new File(fileName)) // changed length from 9 to 7

            // Revenue type record structure
            case class transRCLS(tranAgency: String, tranFund: String, tranSrc: String, tranDate: String, tranAmt: BigDecimal)

            val transRaw = ArrayBuffer(transRCLS("", "", "", "", 0))
            //          var transRaw: Array[transECLS.type] = Array(transECLS)

            // Read input records
            val lineIn = Source.fromFile(VAInDataPath + FileName).getLines.toList.drop(1)
            for (i <- lineIn) {
              val cols = i.split(fileDelimiter).map(_.trim)
              RecFormat match {
                case "A" => transRaw.+=(transRCLS(cols(0), cols(1), cols(2), JrnlDate, BigDecimal(cols(3)))) // assign years 03 - 13 to Revenue type A record structure
                case "C" => transRaw.+=(transRCLS(cols(0), cols(2), cols(3), cols(4), BigDecimal(cols(1)))) // assign years 14 - 16 to Revenue type C record structure
              }
            }

            //Aggregate input data based upon four of five fields
            case class aggTrans(tranAgency: String, tranFund: String, tranSrc: String, tranDate: String)
            implicit object OrderAggTrans extends Ordering[aggTrans] {
              def compare(a: aggTrans, b: aggTrans): Int = {
                if (a.tranAgency == b.tranAgency) {
                  if (b.tranFund == b.tranFund) {
                    if (a.tranSrc == b.tranSrc) {
                      if (a.tranDate == b.tranDate) {
                        0
                      }
                      a.tranDate compare b.tranDate
                    }
                    a.tranSrc compare b.tranSrc
                  }
                  a.tranFund compare b.tranFund
                }
                a.tranAgency compare b.tranAgency
              }
            }
            //          val aggTransOut: mutable.Map[aggTrans] = aggTrans(transRaw.tranAgency,transRaw.tranFund,transRaw.tranObj,transRaw.tranProg,transRaw.tranDate)
            var aggAllSmallTBL: mutable.Map[aggTrans, BigDecimal] = mutable.Map.empty

            for (i <- transRaw) {
              transRead += 1
              val w = aggTrans(i.tranAgency, i.tranFund, i.tranSrc, i.tranDate)
              if (aggAllSmallTBL contains w) {
                aggAllSmallTBL.+=(w -> (aggAllSmallTBL(w) + i.tranAmt))
              }
              else aggAllSmallTBL.+=(w -> i.tranAmt)
            }

            //sort the aggregated journal entries
            val sortedAggAllSmallTBL = aggAllSmallTBL.toList.
              sortBy(_._1.tranDate).
              sortBy(_._1.tranSrc).
              sortBy(_._1.tranFund).
              sortBy(_._1.tranAgency).drop(1)

            //Format output records
            var jrnlID = 0
            for (rec <- sortedAggAllSmallTBL) {
              jrnlID += 1
              journalsWritten += 1

              // Journal Line 1 ("debit" side or original entry)
              transOutRec = Transaction(
                transIPID = "0",
                transContractID = "0",
                transCommitmentID = "0",
                transJrnlID = "ID" + jrnlID.toString + JrnlDate + "REV",
                transJrnlLineID = "1",
                transBusinessEventCode = "Tax Receipt",
                transSourceSystemID = "\"Revenue Sys\"",
                transOriginalDocID = "Unknown",
                transJrnlDescript = "Tax Receipt from Taxpayer", //tranVendorName in universal journal
                transLegalEntityID = rec._1.tranAgency,
                transCenterID = rec._1.tranFund, // assign years 03 - 9 to Expense type A record structure
                transProjectID = " ",
                transNominalAccountID = "REV" + rec._1.tranSrc,
                transFiscalPeriod = FileYear,
                transAcctDate = JrnlDate,
                transTransDate = rec._1.tranDate,
                transTransAmount = rec._2,
                transDirVsOffsetFlg = "D",
                transAdjustFlg = "N"
              )
              outJournals.write(transOutRec.toString.substring(12, transOutRec.toString.length() - 1) + "\n")
              journalsLinesWritten += 1

              // Journal line 2 ("credit" side or offset)
              transOutRec = Transaction(
                transIPID = "0",
                transContractID = "0",
                transCommitmentID = "0",
                transJrnlID = "ID" + jrnlID.toString + JrnlDate + "REV",
                transJrnlLineID = "2", //create journal line 2
                transBusinessEventCode = "Cash Receipt",
                transSourceSystemID = "\"Revenue Sys\"",
                transOriginalDocID = "Unknown",
                transJrnlDescript = "Tax Cash or Check",
                transLegalEntityID = rec._1.tranAgency,
                transCenterID = "0000",
                transProjectID = "0000",
                transNominalAccountID = "0000",
                transFiscalPeriod = FileYear,
                transAcctDate = JrnlDate,
                transTransDate = rec._1.tranDate,
                transTransAmount = rec._2 * -1, //reverse sign on entry
                transDirVsOffsetFlg = "D",
                transAdjustFlg = "N"
              )
              outJournals.write(transOutRec.toString.substring(12, transOutRec.toString.length() - 1) + "\n")
              journalsLinesWritten += 1

            }
            outJournals.close()
        }
        //*****************************************************************************************
        // End of Payment process, before loop to next file
        //*****************************************************************************************
      }
      //*****************************************************************************************
      // End of file process, before loop to next file
      //*****************************************************************************************
    }


    //*****************************************************************************************
    // Print Control Reports
    //*****************************************************************************************

    println("*" * 100)
    println("Files Processed:       " + payFilesProcessed)
    println("Records Read:          " + transRead)
    println("Journal Lines Written: " + journalsLinesWritten)
    println("Journals Written:      " + journalsWritten)
    println("Process End Time:      " + Calendar.getInstance.getTime)

    //*****************************************************************************************
    // End of Program
    //*****************************************************************************************


    //    var addFileCnt = 0

    //*****************************************************************************************
    // Prompt about additional business event transactions
    //
    // Although this prompt could be useful if a user interface was developed to allow
    // users to create new business events; it probably isn't that important to create.
    // The Video and explain for what is produced by the user interace can simply point
    // to existing VA data outputs as examples.  THus this code is probably not needed.
    //*****************************************************************************************
    //    println("Additional Transactions:  Would you like to process your added transaction? (y/n)")
    //    var fileAddTrans: String = "AddTrans.txt"
    //    val procTrans = scala.io.StdIn.readLine().trim.toUpperCase()
    //    if (procTrans.substring(0, 1) == "Q") sys.exit(0)
    //    if (procTrans.substring(0, 1) == "Y") {
    //      addFileCnt += 1
    //      println("The default file name for these transaction is AddTrans.txt")
    //      println("Would you like to change this? (y/n)")
    //      val addTranFiles = scala.io.StdIn.readLine().trim.toUpperCase()
    //      if (addTranFiles.substring(0, 1) == "Q") sys.exit(0)
    //      if (addTranFiles.substring(0, 1) == "Y") {
    //        fileAddTrans = scala.io.StdIn.readLine().trim
    //      }
    //    }
    //*****************************************************************************************
    // Prompt about adjustment file
    //
    // Although I coded this section, I have decided not to use it, as there really isn't much
    // that this program could add to the process.  The records will already be in the
    // journal entry format.  So this code is probably unneeded.
    //*****************************************************************************************

    //    println("Adjustments:  Would you like to process your added adjustments? (y/n)")
    //    var fileAddAdj: String = "AddAdj.txt"
    //    val procAdjs = scala.io.StdIn.readLine().trim.toUpperCase()
    //    if (procAdjs.substring(0, 1) == "Q") sys.exit(0)
    //    if (procAdjs.substring(0, 1) == "Y") {
    //      addFileCnt += 1
    //      println("The default file name for these transaction is Adjustments.txt")
    //      println("Would you like to change this? (y/n)")
    //      val AddAdjFiles = scala.io.StdIn.readLine().trim.toUpperCase()
    //      if (AddAdjFiles.substring(0, 1) == "Q") sys.exit(0)
    //      if (AddAdjFiles.substring(0, 1) == "Y") {
    //        fileAddAdj = scala.io.StdIn.readLine().trim
    //      }
    //    }


    //*****************************************************************************************
    // File name construction
    // this set of collections constructs the file names for the VA data
    // This code was deemed too complex to finish; opted for a simple table of file names.
    //*****************************************************************************************

    //    val qtr = Array("1", "2", "3", "4")
    //    val yrs = Array("03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16")
    //    val fileType = Array("exp", "rev")
    //    val fileNameArray = Array.ofDim[String](
    //      yrs.length * 5 + addFileCnt,           // File Name
    //      yrs.length * 5 + addFileCnt,           // File Type (EA, EB, EC, or RA, RC)
    //      yrs.length * 5 + addFileCnt,           // Fiscal Period (Quarter)
    //      yrs.length * 5 + addFileCnt            // Accounting and Transaction Date (CCYY/MM/01)
    //    )
    //
    //    for (fileCnt <- 0 to (yrs.length * 5)) {
    //      for (yrCnt <- 0 to yrs.length) {
    //        val (quotient, remainder) = fileCnt /% 5
    //        if (remainder != 0) {
    //          // create all quarterly expense entries
    //          fileNameArray(fileCnt)(0) = Array("FY" + yrs(yrCnt) + fileType(1) + ".txt")
    //          // The following adds another dimension to the table for the format of the records to be read
    //          yrCnt match {
    //            // assign years 03 - 9 to Expense type A record structure
    //            case 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 => fileNameArray(fileCnt)(1) = "EA"
    //            // assign years 10 - 11 to Expense type B record structure
    //            case 10 | 11 => fileNameArray(fileCnt)(1) = "EB"
    //            // assign years 12 - 16 to Expense type C record structure
    //            case 12 | 13 | 14 => fileNameArray(fileCnt)(1) = "EC"
    //          }
    //          fileNameArray(fileCnt)(2) = qtr
    //          fileNameArray(fileCnt)(3) = "20" + yrs(yrCnt) + {qtr match {case "1" }}
    //        }
    //        else {
    //          // create revenue entry every 5th entry
    //          for (qtrCnt <- 0 to qtr.length) {
    //            fileNameArray(fileCnt)(0) = "FY" + yrs(yrCnt) + "q" + qtr(qtrCnt) + fileType(0) + ".txt"
    //          }
    //          yrCnt match {
    //            // assign years 03 - 11 to Revenue type A record structure
    //            case 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 => fileNameArray(fileCnt)(1) = "RA"
    //            // assign years 12 - 16 to Revenue type C record structure
    //            case 12 | 13 | 14 | 15 | 16 => fileNameArray(fileCnt)(1) = "RC"
    //          }
    //        }
    //      }
    //    }
    //    // Add Additional Transaction and Adjustment Files to list if Requested above.
    //    var subscript: Int = yrs.length * 5
    //    if (procTrans.substring(0, 1) == "Y") {
    //      fileNameArray(subscript + 1)(0) = fileAddTrans
    //      fileNameArray(subscript + 1)(1) = "TA"
    //    }
    //    if (procAdjs.substring(0, 1) == "Y") {
    //      fileNameArray(subscript + 1)(0) = fileAddAdj
    //      fileNameArray(subscript + 1)(1) = "AA"
    //    }
    //

    //*****************************************************************************************
    // This code can be used to read text files in a Scala Terminal (paste in code), and it will print the line
    // number of the data having the problem.  The problem may be a few rows lower if it is a buffer problem
    //*****************************************************************************************
//
//    import scala.io.Source
//    val instX = Source.fromFile("FY09q1exp.txt").getLines()
//    val instIDLine = instX.buffered
//    var instRead = 0
//
//    def readInst(): Unit = {
//      val e: Array[String] = instIDLine.next.split(',')
//      // if (debugPrint == "Y") println("inst vendor: " + instIDRec.instIDVendorName + " inst row: " + instIDRec)
//      instRead +=1
//      println("line count: " + instRead)
//    }
//    var instEOF = "N"
//
//    while (instEOF == "N") {
//      if (instIDLine.isEmpty) {
//        // if (debugPrint == "Y") println("inst high values. EOF Inst")
//        instEOF = "Y"
//      }
//      else readInst()
//    }
//
//  //*****************************************************************************************
    // These characters were located in the later data files from VA, using the above code.
    // //list was kept to be able to search
    // for new instances of them in later files.
    //*****************************************************************************************
    //+¨
    //«÷
    //«Ù
    //-·
    //†
    //¨
    //¶+
    //*****************************************************************************************

  }
  def tranHeader(outJournals: PrintWriter): Unit = {
    outJournals.write(
      "transIPID," +
        "transContractID," +
        "transCommitmentID," +
        "transJrnlID," +
        "transJrnlLineID," +
        "transBusinessEventCode," +
        "transSourceSystemID," +
        "transOriginalDocID," +
        "transJrnlDescript," +
        "transLedgerID,"  +
        "transJrnlType,"  +
        "transBookCodeID," +
        "transLegalEntityID," +
        "transCenterID," +
        "transProjectID," +
        "transProductID," +
        "transNominalAccountID," +
        "transAltAccountID,"  +
        "transCurrencyCodeSourceID,"  +
        "transCurrencyTypeCodeSourceID," +
        "transCurrencyCodeTargetID," +
        "transCurrencyTypeCodeTargetID," +
        "transFiscalPeriod," +
        "transAcctDate," +
        "transTransDate," +
        "transTransAmount,"  +
        "transUnitOfMeasure," +
        "transUnitPrice," +
        "transStatisticAmount," +
        "tranRuleSetID," +
        "transRuleID," +
        "transDirVsOffsetFlg," +
        "transReconcileFlg," +
        "transAdjustFlg," +
        "transExtensionIDAuditTrail," +
        "transExtensionIDSource,"  +
        "transExtensionIDClass," +
        "transExtensionIDDates,"  +
        "transExtensionIDCustom"
        + "\n")
  }

}
