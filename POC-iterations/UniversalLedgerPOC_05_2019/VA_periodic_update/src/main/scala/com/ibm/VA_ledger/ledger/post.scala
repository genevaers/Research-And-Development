package com.ibm.VA_ledger.ledger
/*
*(c) Copyright IBM Corporation. 2018
* SPDX-License-Identifier: Apache-2.0
* By Kip Twitchell
*/

import java.io.{File, FileNotFoundException, PrintWriter}
import java.util.Calendar

import com.ibm.VA_ledger.datatypes._

import scala.io.Source


//***************************************************************************************************************
//
//  Program:  post - post journal entries produced by transStandardize to a ledger
//
//  This program reads transaction data and produces standardized journals
//
//	Copyright 2018, IBM Corporation.  All rights reserved.
//     Kip Twitchell <finsysvlogger@gmail.com>.
//                   kip.twitchell@us.ibm.com
//
//  Created Nov 2018
//
//  Change Log:
//***************************************************************************************************************


object post {
  def apply(fileInLocation: String, fileOutLocation: String): Unit = {

    println("*" * 100)
    println("                                Post Module")
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
    // Open Journal File Name Table
    //  - rather than opening a specific file like in the Transform, this program gets a list of all files
    //        starting with JE*.* to process
    //*****************************************************************************************
    //

    val jrnlFileNames = getListOfFiles(VAInDataPath, "SortedJE")
    val jrnlFileNamesSorted = jrnlFileNames.sortWith(_.getName < _.getName)

    //-----------------------------------------------------------------------------------------
    // Process Varilables
    //-----------------------------------------------------------------------------------------
    val fileDelimiter =  ","
    var totalJrnlRowsRead = 0 //counter for number of journals read
    var totalLdgrRowsRead = 0 //counter for number of ledger rows read
    var totalLdgrRowsWritten = 0
    var filesProcessed = 0 //counter for files processed

    //*****************************************************************************************
    // File Name Process Loop, loop through every journal file in Output Directory and process all journals
    //*****************************************************************************************
    for (fn <- jrnlFileNamesSorted) {
      filesProcessed += 1
      println("processing file: " + fn)

      //*****************************************************************************************
      // Open Output Files
      //*****************************************************************************************
      // Set ledger file names
      val ledgerYear = "20" + fn.toString.substring(VAOutDataPath.length+10,VAOutDataPath.length+12)
      val ledgerFilePrefix = "LDGR" + ledgerYear
      val ledgerFileName = ledgerFilePrefix + ".csv"

      //-----------------------------------------------------------------------------------------
      // Temporary Output Ledger File--will be renamed to actual ledger file at end of this loop
      //-----------------------------------------------------------------------------------------
      val ledgerFileNameTemp = ledgerFilePrefix + "temp.csv"
      val outLedger: PrintWriter = new PrintWriter(new File(VAOutDataPath + ledgerFileNameTemp ))
      var fileLdgrRowsWritten = 0
      ldgrHeader(outLedger) // write header record

      //*****************************************************************************************
      // Open and Perform Initial Reads
      //*****************************************************************************************
      //-----------------------------------------------------------------------------------------
      // Ledger File
      //-----------------------------------------------------------------------------------------

      var ldgrFileX: Iterator[String] = null
      var ldgrLine: BufferedIterator[String] = null

      var ldgrRec = Ledger("","","","","","","","","","","","","","","","","","","",0,"",0,"","","","","","","","")

      var testLdgrFullKey: String = " "
      var ldgrAmtAccum: BigDecimal = 0
      var fileLdgrRowsRead = 0

      def readLdgr(): Unit = {
        val e: Array[String] = ldgrLine.next.split(fileDelimiter)
          ldgrRec = Ledger(e(0), e(1), e(2), e(3), e(4), e(5), e(6), e(7), e(8), e(9), e(10), e(11),
            e(12), e(13), e(14), e(15), e(16), e(17), e(18),
            BigDecimal(e(19)), // amount
            e(20), BigDecimal(e(21)), //Stat amount
            e(22), e(23), e(24), e(25), e(26), e(27), e(28), e(29)) // )
        testLdgrFullKey =
          ldgrRec.ldgrLedgerID +
          ldgrRec.ldgrJrnlType +
          ldgrRec.ldgrBookCodeID +
          ldgrRec.ldgrLegalEntityID +
          ldgrRec.ldgrCenterID +
          ldgrRec.ldgrProjectID +
          ldgrRec.ldgrProductID +
          ldgrRec.ldgrNominalAccountID +
          ldgrRec.ldgrAltAccountID +
          ldgrRec.ldgrCurrencyCodeSourceID +
          ldgrRec.ldgrCurrencyTypeCodeSourceID +
          ldgrRec.ldgrCurrencyCodeTargetID +
          ldgrRec.ldgrCurrencyTypeCodeTargetID +
          ldgrRec.ldgrLedgerPeriod

        ldgrAmtAccum += ldgrRec.ldgrTransAmount
        // if (debugPrint == "Y") println("bal rec full key: " + testLdgrFullKey)
        fileLdgrRowsRead += 1
      }

      var ldgrEOF = "N"
      def eofLdgr(): Unit = {
        // if (debugPrint == "Y") println("bal high values. EOF bal")
        testLdgrFullKey = "𝖛" * testLdgrFullKey.length //this is a hack, a higher value constant would be good
        ldgrEOF = "Y"
        // if (jrnlEOF == "N") lastBalWriteflg = "Y"  // added ldgrEOF == "Y" statement to eofJrnl routine.  this isn't needed?
      }

      //  If ledger is not there; this code tries to create it.
      try ldgrFileX = Source.fromFile(VAInDataPath + ledgerFileName).getLines().drop(1) //drop header rec on initial get
      catch {
        case e: FileNotFoundException => println("No existing ledger file.  File will be created: " + ledgerFileName)
          eofLdgr()
      }

      try ldgrLine = ldgrFileX.buffered
      catch {
        case e: NullPointerException => println("----------------------------------")
      }
      if (ldgrEOF == "N") {
        if (ldgrLine.isEmpty) eofLdgr()
        else readLdgr()
      }

      //-----------------------------------------------------------------------------------------
      // Journal file
      //-----------------------------------------------------------------------------------------

      var jrnlFileX: Iterator[String] = Source.fromFile(fn.toString).getLines().drop(1) //drop header rec on initial get
      val jrnlLine: BufferedIterator[String] = jrnlFileX.buffered

      var jrnlRec = Transaction(
        "","","","","","","","","","",
        "","","","","","","","","","",
        "","","","","",0,"",0,0,"",
        "","","","","","","","","") // 39 elements
      var testJrnlFullKey: String = " "
      var fileJrnlRowsRead = 0

      def readJrnl(): Unit = {
        val e: Array[String] = jrnlLine.next.split(fileDelimiter)
        jrnlRec = Transaction(e(0), e(1), e(2), e(3), e(4), e(5), e(6), e(7), e(8), e(9),
          e(10), e(11), e(12), e(13), e(14), e(15), e(16), e(17), e(18), e(19),
          e(20), e(21), e(22), e(23), e(24), BigDecimal(e(25)), e(26), BigDecimal(e(27)), BigDecimal(e(28)), e(29), e(30),
          e(31), e(32), e(33), e(34), e(35), e(36), e(37), e(38))
//        try {
          testJrnlFullKey =
              jrnlRec.transLedgerID +
              jrnlRec.transJrnlType +
              jrnlRec.transBookCodeID +
              jrnlRec.transLegalEntityID +
              jrnlRec.transCenterID +
              jrnlRec.transProjectID +
              jrnlRec.transProductID +
              jrnlRec.transNominalAccountID +
              jrnlRec.transAltAccountID +
              jrnlRec.transCurrencyCodeSourceID +
              jrnlRec.transCurrencyTypeCodeSourceID +
              jrnlRec.transCurrencyCodeTargetID +
              jrnlRec.transCurrencyTypeCodeTargetID +
              jrnlRec.transFiscalPeriod
//        }
//        catch {
//          case e: ArrayIndexOutOfBoundsException =>
//            println("Tran Record too short! Trans record: " + fileJrnlRowsRead + " trans: " + jrnlRec)
//            println("Trans ID/Description: " + jrnlRec.transJrnlID + " " + jrnlRec.transJrnlLineID + " " + jrnlRec.transJrnlDescript)
//            throw new ArrayIndexOutOfBoundsException
//        }
        // if (debugPrint == "Y") println("trans key: " + testJrnlFullKey + " trans row: " + jrnlRec)
        fileJrnlRowsRead += 1
      }

      var jrnlEOF = "N"
      def eofJrnl(): Unit = {
        // if (debugPrint == "Y") println("tran high values; EOF tran")
        testJrnlFullKey = "𝖛" * testJrnlFullKey.length  //this is a hack, a higher value constant would be good
        jrnlEOF = "Y"
      }

      if (jrnlLine.isEmpty) eofJrnl()
      else readJrnl()

      //*****************************************************************************************
      // process variables for while loop in file processing
      //*****************************************************************************************
      var swapBal = "N"
      var saveLdgrFullKey: String = testLdgrFullKey
      var saveLdgrFullRecord = ldgrRec.copy()
      var saveLdgrInstID: String = ""
      var saveBalAmtAccum: BigDecimal = 0
      var MMLoopCounter: Int = 0

      //*****************************************************************************************
      // Mainline Program Structure:
      //*****************************************************************************************

      while (jrnlEOF == "N" || ldgrEOF == "N" ) {

          if (testJrnlFullKey == testLdgrFullKey) {
            // if (debugPrint == "Y") {println("==: tran: " + testJrnlFullKey + " Bal: " + testLdgrFullKey + " remaining rec: " )}
            // {println("==: tran: " + testJrnlFullKey + " Bal: " + testLdgrFullKey + " remaining rec: " )}
            ldgrAmtAccum = ldgrAmtAccum + jrnlRec.transTransAmount
            // read next transaction record
            if (jrnlEOF == "N") {
              if (jrnlLine.hasNext) {
                readJrnl()
              }
              else {
                eofJrnl()
              }
            }
          }
          else {
            if (testJrnlFullKey < testLdgrFullKey) {
              // if (debugPrint == "Y") {println("<<: tran: " + testJrnlFullKey + " inst: " + testLdgrFullKey + " Max ID: " + maxBalID.toString)}
              // new bal found.  test if new bal is equal to already read and saved bal record from bal file
              if (swapBal == "Y" && testJrnlFullKey >= saveLdgrFullKey) {
                useSaveBal()
              }
              else {
                if (swapBal == "N") {
                  // save prior vendor
                  swapBal = "Y"
                  saveLdgrFullKey = testLdgrFullKey
//                  saveLdgrInstID = testLdgrInstID
                  saveLdgrFullRecord = ldgrRec
                  saveBalAmtAccum = ldgrAmtAccum
                  ldgrAmtAccum = 0
                }
                formatBalwTran() // build new bal record
              }
            }
            else {
              if (testJrnlFullKey > testLdgrFullKey) {
                // finished with bal record
                // if (debugPrint == "Y") println(">>: after tran: " + testJrnlFullKey + " bal: " + testLdgrFullKey)
                // finish old bal and write to output file
                writeBal()
                // test if new balance is equal to already read and saved balance record from bal file
                if (swapBal == "Y" && (testJrnlFullKey >= saveLdgrFullKey || jrnlEOF == "Y")) {
                  useSaveBal() // then return to test and accumulate trans record for this balance.
                }
                else {
                  if (swapBal == "Y") {
                    formatBalwTran()
                  } // build new bal record without replacing saved balance from read ahead
                  else {
                    // read new balance
                    if (ldgrEOF == "N") {
                      if (ldgrLine.hasNext) {
                        readLdgr()
                      }
                      else {
                        eofLdgr()
                      }
                    }
                  }
                }
              }
              else {
                println("an evaluation error has occurred on the Balance and Trans Files!  Files might be out of sort order")
              }
            }
          }
        MMLoopCounter += 1
      } // End of While Loop on match merge logic

      //*****************************************************************************************
      // Complete fn processing loop for current file name.
      //*****************************************************************************************

      outLedger.close()
      // Rename Ledger File from temp to perm name so it can be used in next period if needed
      new File(VAOutDataPath + ledgerFileNameTemp).renameTo(new File(VAOutDataPath + ledgerFileName))

      totalLdgrRowsWritten += fileLdgrRowsWritten
      totalLdgrRowsRead    += fileLdgrRowsRead
      totalJrnlRowsRead    += fileJrnlRowsRead


      //*****************************************************************************************
      // Common routines within fn loop
      //*****************************************************************************************

      // Used the Saved vendor from prior read
      def useSaveBal(): Unit = {
        swapBal = "N"
        testLdgrFullKey = saveLdgrFullKey
        ldgrRec = saveLdgrFullRecord  // not sure if I can put data back in here????????????????????????????
        ldgrAmtAccum = saveBalAmtAccum
      }

      def formatBalwTran(): Unit = {
        testLdgrFullKey = testJrnlFullKey
        fileLdgrRowsWritten += 1  // used for Ledger ID field as well as file counter

        //val stdBalRecOut = ldgrRecTemplate.copy(
        ldgrRec = new Ledger(
        "",                       // jrnlRec.transIPID
        "",                  //jrnlRec.transContractID
        "",              //jrnlRec.transCommitmentID, ""
        fileLdgrRowsWritten.toString,      //ldgrLdgrlID
        jrnlRec.transSourceSystemID,
        jrnlRec.transLedgerID,
        jrnlRec.transJrnlType,
        jrnlRec.transBookCodeID,
        jrnlRec.transLegalEntityID,
        jrnlRec.transCenterID,
        jrnlRec.transProjectID,
        jrnlRec.transProductID,
        jrnlRec.transNominalAccountID,
        jrnlRec.transAltAccountID,
        jrnlRec.transCurrencyCodeSourceID,
        jrnlRec.transCurrencyTypeCodeSourceID,
        jrnlRec.transCurrencyCodeTargetID,
        jrnlRec.transCurrencyTypeCodeTargetID,
        jrnlRec.transFiscalPeriod,         //  Figure out this value
        jrnlRec.transTransAmount,           //transfer initial journal amount to new balance
        jrnlRec.transUnitOfMeasure,
        jrnlRec.transStatisticAmount,
        jrnlRec.transDirVsOffsetFlg,
        jrnlRec.transReconcileFlg,
        jrnlRec.transAdjustFlg,
          " ",                     //jrnlRec.transExtensionIDAuditTrail, ""
        " ",                       //jrnlRec.transExtensionIDSource, ""
        " ",                       //jrnlRec.transExtensionIDClass, ""
        " ",                     //jrnlRec.transExtensionIDDates, ""
        " "                     //jrnlRec.transExtensionIDCustom, ""
        )
      }

      def writeBal(): Unit = {
        val balSeq: String =
          ldgrRec.ldgrIPID + "," +
          ldgrRec.ldgrContractID + "," +
          ldgrRec.ldgrCommitmentID + "," +
          ldgrRec.ldgrLdgrlID + "," + 
          ldgrRec.ldgrSourceSystemID + "," +
          ldgrRec.ldgrLedgerID + "," +
          ldgrRec.ldgrJrnlType + "," +
          ldgrRec.ldgrBookCodeID + "," +
          ldgrRec.ldgrLegalEntityID + "," +
          ldgrRec.ldgrCenterID + "," +
          ldgrRec.ldgrProjectID + "," +
          ldgrRec.ldgrProductID + "," +
          ldgrRec.ldgrNominalAccountID + "," +
          ldgrRec.ldgrAltAccountID + "," +
          ldgrRec.ldgrCurrencyCodeSourceID + "," +
          ldgrRec.ldgrCurrencyTypeCodeSourceID + "," +
          ldgrRec.ldgrCurrencyCodeTargetID + "," +
          ldgrRec.ldgrCurrencyTypeCodeTargetID + "," +
          ldgrRec.ldgrLedgerPeriod + "," +
          ldgrAmtAccum.toString + "," +
  //        ldgrRec.ldgrTransAmount: BigDecimal,  // accumulated amount from journals 20th element
          ldgrRec.ldgrUnitOfMeasure + "," +
          "0.00" + "," +
  //        ldgrRec.ldgrStatisticAmount: BigDecimal,  // accumulated amount from journals 22nd element
          ldgrRec.ldgrDirVsOffsetFlg + "," +
          ldgrRec.ldgrReconcileFlg + "," +
          ldgrRec.ldgrAdjustFlg + "," +
          ldgrRec.ldgrExtensionIDAuditTrail + "," +
          ldgrRec.ldgrExtensionIDSource + "," +
          ldgrRec.ldgrExtensionIDClass + "," +
          ldgrRec.ldgrExtensionIDDates + "," +
          ldgrRec.ldgrExtensionIDCustom + ","

        outLedger.write(balSeq + "\n")
        // if (debugPrint == "Y") println("Final output bal: " + ldgrRec)
        ldgrAmtAccum = 0
      }

    } // end of fn file loop
    //*****************************************************************************************
    // End of Journal File Name loop; all journal files have been processed
    //*****************************************************************************************


    //*****************************************************************************************
    // Print Control Reports
    //*****************************************************************************************

    println("*" * 100)
    println("Files Processed:               " + filesProcessed)
    println("Journal Records Read:          " + totalJrnlRowsRead)
    println("Ledger Records Read:           " + totalLdgrRowsRead)
    println("Ledger Records Written:        " + totalLdgrRowsWritten)
    println("Process End Time:      " + Calendar.getInstance.getTime)
    println("*" * 100)

    //*****************************************************************************************
    //*****************************************************************************************
    // End of Main def drop through logic
    //*****************************************************************************************
    //*****************************************************************************************

    //*****************************************************************************************
    // Common Routine
    //*****************************************************************************************


    def ldgrHeader(outLedger: PrintWriter): Unit = {
      outLedger.write(
      "ldgrIPID," +
      "ldgrContractID," +
      "ldgrCommitmentID," +
      "ldgrLdgrlID," +
//      "ldgrJrnlLineID," +
//      "ldgrBusinessEventCode," +
      "ldgrSourceSystemID," +
//      "ldgrOriginalDocID," +
//      "ldgrJrnlDescript," +
      "ldgrLedgerID,"  +
      "ldgrJrnlType,"  +
      "ldgrBookCodeID," +
      "ldgrLegalEntityID," +
      "ldgrCenterID," +
      "ldgrProjectID," +
      "ldgrProductID," +
      "ldgrNominalAccountID," +
      "ldgrAltAccountID,"  +
      "ldgrCurrencyCodeSourceID,"  +
      "ldgrCurrencyTypeCodeSourceID," +
      "ldgrCurrencyCodeTargetID," +
      "ldgrCurrencyTypeCodeTargetID," +
      "ldgrLedgerPeriod," +
//      "ldgrAcctDate," +
//      "ldgrTransDate," +
      "ldgrTransAmount,"  +
      "ldgrUnitOfMeasure," +
//      "ldgrUnitPrice," +
      "ldgrStatisticAmount," +
//      "ldgruleSetID," +
//      "ldgrRuleID," +
      "ldgrDirVsOffsetFlg," +
      "ldgrReconcileFlg," +
      "ldgrAdjustFlg," +
      "ldgrExtensionIDAuditTrail," +
      "ldgrExtensionIDSource,"  +
      "ldgrExtensionIDClass," +
      "ldgrExtensionIDDates,"  +
      "ldgrExtensionIDCustom,"
        + "\n")
    }

    //*****************************************************************************************
    // End of Program
    //*****************************************************************************************

  }
}
object getListOfFiles {
  def apply(dir: String, prefix: String): List[File] = {
    val d = new File(dir)
    val sw = List(prefix)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList.filter { file =>
        sw.exists(file.getName.startsWith(_))
      }
    } else {
      List[File]()
    }
  }
}
