package com.ibm.VA_ledger.ledger

import scala.io.Source

//***************************************************************************************************************
//
//  Program:  FinSysPatterns - Main Fin Sys Pattern Module
//
//  This module is the main program of this project; from it all other modules may be selected
//  This program reads:
//    - prints (to stdout) a simple menu of possible modules to run
//    - accepts an input of which module to run
//    - confirms the data path
//    - it then runs that module
//
//  *(c) Copyright IBM Corporation. 2018
//  * SPDX-License-Identifier: Apache-2.0
//  * By Kip Twitchell
//  Created July 2018
//
//  Change Log:
//***************************************************************************************************************

object VA_ledger {

  def main(args: Array[String]): Unit = {



    println("*" * 100 )
    println("                                Universal Ledger")
    println("                                   Demo System")
    println("                  Using State of Virginia Public Financial Data")
    println("*" * 100 )

    //*****************************************************************************************
    // Initial Menu Prompt
    //*****************************************************************************************

    println("Type q at any prompt to quit the program")

    println("-" * 35 )
    println("Select the one of following programs to run")
    println("   1 -Match POs to Payments:"               )
    println("   2 -PO Vendor ID Assignment: "            )
    println("   3 -Post:"                                )
    println("   4 -Reconciliation: "                     )
    println("   5 -Data Aggregation: "                   )
    println("   6 -Financial Allocation: "               )
    println("   7 -Consolidation and Elimination: "      )
    println("   8 -Forecasting and Budeting:"            )
    println("   9 -Financial Modeling: "                 )
    println("   10-Initialize Process Files: "           )
    println("   11-David Paget-Brown Spark   "           )
    println("-" * 35 )

    println("Which progam would you like to run?")
    val runModule = scala.io.StdIn.readLine().trim.toUpperCase()
    if (runModule.substring(0,1) == "Q") sys.exit(0)
    println("OK, we'll run program number " + runModule + ".")
    println("-" * 35 )

    println("The default location for the input data is ../data/")
    println("Would you like to change this? (y/n)")
    val chgInFiles = scala.io.StdIn.readLine().trim.toUpperCase()
    if (chgInFiles.substring(0,1) == "Q") sys.exit(0)
    var fileInLocation: String = "/VAdata/data/"
    if (chgInFiles == "Y") {
      fileInLocation = scala.io.StdIn.readLine().trim
    }

    println("The default location for the output data is ../data/output/")
    println("Would you like to change this? (y/n)")
    val chgOutFiles = scala.io.StdIn.readLine().trim.toUpperCase()
    if (chgOutFiles.substring(0,1) == "Q") sys.exit(0)
    var fileOutLocation: String = "/VAdata/data/"
    if (chgOutFiles == "Y") {
      fileOutLocation = scala.io.StdIn.readLine().trim
    }

    println("The default year is 03")
    println("Would you like to change this? (y/n)")
    val chgYRFiles = scala.io.StdIn.readLine().trim.toUpperCase()
    if (chgYRFiles.substring(0,1) == "Q") sys.exit(0)
    var inputYear: String = "03"
    if (chgYRFiles == "Y") {
      inputYear = scala.io.StdIn.readLine().trim
    }

    println("The default Quarter is 01.  Enter 05 for Revenue Files")
    println("Would you like to change this? (y/n)")
    val chgQTRFiles = scala.io.StdIn.readLine().trim.toUpperCase()
    if (chgQTRFiles.substring(0,1) == "Q") sys.exit(0)
    var inputQTR: String = "01"
    if (chgQTRFiles == "Y") {
      inputQTR = scala.io.StdIn.readLine().trim
    }

    println("start file handling:")
    println("module assumes input subdirectories of common, payment, poData")

    val inputRefPath =     fileInLocation + "common/"
    val inputPaymentPath = fileInLocation + "payment/"
    val inputPOPath =      fileInLocation + "poData/"
    val outputPath =       fileOutLocation

    val POfileIn = "VA_opendata_FY20" + inputYear + ".txt"

    runModule match {
      case "1" => poToPayMatch(fileInLocation, fileOutLocation)
      case "2" => poVendorID(inputPOPath, fileOutLocation, POfileIn)
//      case "3" => post(fileOutLocation, fileOutLocation) //in this instance, for this program, the input and output are the same place
      case "10" => initFiles(fileInLocation, fileOutLocation)
      case "11" => DPBSpark(fileInLocation, fileOutLocation)
      case _ => println("You have selected an invalid program; not yet developed")
    }

    println("*" * 100 )
    println("                           End of Financial Patterns Processes")
    println("*" * 100 )


  }
}
