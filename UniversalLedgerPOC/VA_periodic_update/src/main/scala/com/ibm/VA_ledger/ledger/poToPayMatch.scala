package com.ibm.VA_ledger.ledger

/*
  *(c) Copyright IBM Corporation. 2018
  * SPDX-License-Identifier: Apache-2.0
  * By Kip Twitchell
  *  Created July 2017
*/
//______________________________________________________________________________________________
//  Process VA Expenses and PO Data
//  This program uses the raw VA Data to create a large, denormalized record structure including:
//  -- The reference files for the Agency, Fund, Object, and Program
//  -- Payment data
//  -- PO Data
//  the year to be processed is hard coded in the variable below
//  and the program is set to process all quarters for that year
//  the PO join should be changed in the last parameter to "left_outer" if all payments are to be processed
//  This system drops POs that are not matched to a payment in some way
//______________________________________________________________________________________________


import org.apache.log4j._
import org.apache.spark.sql.functions.{lit, regexp_replace}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

object poToPayMatch {


  def apply(fileInLocation: String, fileOutLocation: String): Unit = {

    //______________________________________________________________________________________________
    //  Establish Spark Environment
    //______________________________________________________________________________________________
//    Logger.getLogger("org").setLevel(Level.INFO)
    Logger.getLogger("org").setLevel(Level.ERROR)

    val sc = SparkSession
      .builder()
      .appName("VA PO Data Assignment")
      .config("spark.eventLog.enabled",true)
      .config("spark.master", "local")
      .getOrCreate()

    //______________________________________________________________________________________________
    //  File Handling Environment Creation
    //______________________________________________________________________________________________

    println("start file handling:")
    println("module assumes input subdirectories of common, payment, poData")

    val inputRefPath =     fileInLocation + "common/"
    val inputPaymentPath = fileInLocation + "payment/"
    val inputPOPath =      fileInLocation + "poData/"
    val outputPath =       fileOutLocation

    // for years 03 - 13, change file names to CSV below, and delimiter to /t
    val inputYear = "03"
    val inputQtr = "1"
//    val processAllQtrs = "Y"

    //______________________________________________________________________________________________
    //  Read Reference Files
    //______________________________________________________________________________________________

    println("start read data:")

    val agencyRef: DataFrame = sc.read
      .format("csv").option("header","true")
      .option("delimiter","\t")
      .load(inputRefPath + "aRefAgencyTab.txt")
    println("agency: " + agencyRef.count())
//    println("agency " + agencyRef.schema)

    val fundRef: DataFrame = sc.read
      .format("csv").option("header","true")
//      .option("delimiter","\t")
      .load(inputRefPath + "aRefFundCSV.txt")
    println("fund: " + fundRef.count())
//    println("fund " + fundRef.schema)

    val objectRef: DataFrame = sc.read
      .format("csv").option("header","true")
      //      .option("delimiter","\t")
      .load(inputRefPath + "aRefObjectCSV.txt")
    println("object: " + objectRef.count())
//    println("object " + objectRef.schema)

    val programRef: DataFrame = sc.read
      .format("csv").option("header","true")
      //      .option("delimiter","\t")
      .load(inputRefPath + "aRefProgramCSV.txt")
    println("program: " + programRef.count())
//    println("program " + programRef.schema)

    //______________________________________________________________________________________________
    //  Read Payment Data Files
    //______________________________________________________________________________________________

    def readPayments (payDate:String) : DataFrame = {

      val paymentstemp: DataFrame = sc.read
        .format("csv").option("header", "true")
//        .option("delimiter", ",")
//        .load(inputPaymentPath + payDate + "exp.csv")

      // used for files before 2014
              .option("delimiter", "\t")
              .load(inputPaymentPath + payDate + "exp.txt")
      println("payments: " + payDate + " " + paymentstemp.count())
      //    println("payments " +payments.schema)

      //    val paymentsDT = payments.withColumn("PAY_DATE", lit(payDate))
      val paymentsDTtemp: DataFrame = paymentstemp
        .withColumn("PAY_DATE", lit(payDate))
        .withColumn("PAY_YEAR", lit("20" + inputYear))
        .withColumn("PAY_QTR", lit(inputQtr))

      println("payments dates: " + payDate + " " + paymentsDTtemp.count())
      //    println("paymentsDT " +paymentsDT.schema)

      paymentsDTtemp}

//    if (processAllQtrs == "Y") {
      val payDate1 = "FY" + inputYear + "q1"
      val paymentsQtr1: DataFrame = readPayments(payDate1)
      val payDate2 = "FY" + inputYear + "q2"
      val paymentsQtr2: DataFrame = readPayments(payDate2)
      val payDate3 = "FY" + inputYear + "q3"
      val paymentsQtr3: DataFrame = readPayments(payDate3)
      val payDate4 = "FY" + inputYear + "q4"
      val paymentsQtr4: DataFrame = readPayments(payDate4)
      val paymentsDT: DataFrame = paymentsQtr1.union(paymentsQtr2).union(paymentsQtr3).union(paymentsQtr4)
//    }
//    else {
//      val payDate = "FY" + inputYear + "q" + inputQtr
//      val paymentsDT = readPayments(payDate)
//    }
    //______________________________________________________________________________________________
    //  Read PO Data Files
    //______________________________________________________________________________________________

    val pos: DataFrame = sc.read
      .format("csv").option("header","true")
      .option("delimiter","\t")
      .load(inputPOPath + "VA_opendata_FY20" + inputYear + ".txt")
//      .limit(100000)
    println("POs: " + pos.count())
//    println("pos " + pos.schema)

    val posExtend = pos
      .withColumn("POS_YEAR",lit("20"+inputYear))
      .withColumn("EXTENDED", pos.col("QUANTITYORDERED") * pos.col("PRICE"))

    //______________________________________________________________________________________________
    //  Begin Join Reference Data Processes
    //______________________________________________________________________________________________

    println("starting Referenece Joins:")

    //  Join Agency
    val agencyPayments = paymentsDT.join(agencyRef,"AGY_AGENCY_KEY")
//    println(agencyPayments.show)
//    println("agencyPayments " + agencyPayments.schema)

    //  Join Fund
    val AFPayments = agencyPayments.join(fundRef,"FNDDTL_FUND_DETAIL_KEY")
//    println(AFPayments.show)
//    println("agencyFundPayments " + AFPayments.schema)

    //  Join Object
    val AFOPayments = AFPayments.join(objectRef,"OBJ_OBJECT_KEY")
//    println(AFOPayments.show)
//    println("agencyFundObjectPayments " + AFOPayments.schema)

    //  Join Program
    val AFOPPayments = AFOPayments.join(programRef,"SPRG_SUB_PROGRAM_KEY")
//    println(AFOPPayments.show)
//    println("agencyFundObjectProgramPayments " + AFOPPayments.schema)

    //______________________________________________________________________________________________
    //  Begin Join PO Data Processes
    //______________________________________________________________________________________________

    println("starting PO Joins:")

    //  Join PO Data
//    val combined = posExtend.join(AFOPPayments,posExtend("VENDORNAME") <=> AFOPPayments("VENDOR_NAME")
//      && posExtend("AGENCY") <=> AFOPPayments("AGY_AGENCY_NAME")
//      && posExtend("EXTENDED") <=> AFOPPayments("AMOUNT"))

//  USE this statement if all payments should be listed.
//    val combined = AFOPPayments.join(posExtend,AFOPPayments("VENDOR_NAME") <=> posExtend("VENDORNAME")
//      && AFOPPayments("AGY_AGENCY_NAME") <=> posExtend("AGENCY")
//      &&  AFOPPayments("AMOUNT") <=> posExtend("EXTENDED"), "left_outer")

//  This statement only lists payments with matching POs.
    val combined: DataFrame = AFOPPayments.join(posExtend,AFOPPayments("VENDOR_NAME") <=> posExtend("VENDORNAME")
      && AFOPPayments("AGY_AGENCY_NAME") <=> posExtend("AGENCY")
      &&  AFOPPayments("AMOUNT") <=> posExtend("EXTENDED"))

    println(combined.show)

    //______________________________________________________________________________________________
    //  Replace comma's from text fields before writing the CSV file.
    //  This also moves the VendorCommodityDescription field to the end of the file, in case the parsing is not all complete
    //______________________________________________________________________________________________

    println("starting data cleansing:")

    val editCombined = combined.withColumn("VENDORCOMMODITYDESCEDIT", regexp_replace(combined("VENDORCOMMODITYDESC"), "\\,", ".")).drop("VENDORCOMMODITYDESC")

//    println(editCombined.show)

    //______________________________________________________________________________________________
    //  Write out Result Set
    //______________________________________________________________________________________________

    println("starting write:")

    editCombined
      .repartition(1)
      .write
        .format("csv")
        .mode(SaveMode.Overwrite)
        .option("header", "true")
  //      .option("delimiter", "\t")
        .save(outputPath + "20" + inputYear + "_Combined_Pay_PO")
    ;

    //    pos.select(
// Element      Data Type                Default Value     KEY col Description
    val instSchema: StructType = StructType(Seq(
          StructField("instID",StringType,true),                         // "0",               Instrument ID or Vendor ID from PO Data
          StructField("instEffectDate",StringType,true),                 // "0000-00-00",      Record Effective Start Date
          StructField("instEffectTime",StringType,true),                 // "0",               Record Effective Start Time
          StructField("instEffectEndDate",StringType,true),              // "9999-99-99",      Record Effective End Date
          StructField("instEffectEndTime",StringType,true),              // "9",               Record Effective End Time
          StructField("instHolderName",StringType,true),                 // "Vendor Name",     Name of Instrument Holder
          StructField("instVendorAddress",StringType,true),              // "345 High St."    Vendor Address from PO Data
          StructField("instVendorCity",StringType,true),                 // "Dallas"          Vendor city from PO Data
          StructField("instVendorState",StringType,true),                // "TX"              Vendor State from PO Data
          StructField("instVendorPostalCode",StringType,true),           // "75904"           Vendor ZIP Code from PO Data
          StructField("instVendorEmail",StringType,true),                // "tom@vendor.com"  Email address of vendor
          StructField("instTypeID",StringType,true),                     // "EXP",             Vendor (EXP) or REV Record
          StructField("instAuditTrail",StringType,true)                  // "0,                Timestamp Updated
        ))

//    val newVendor: List[String] = pos.foreach(x => pos("VENDORID") + "0000-00-00")

//    val add_n = udf((x: String, y: String) => x + y)

//    for
//    pos =
//      pos("VENDORID"),
//      "0000-00-00",
//      "0" +
//      "9999-99-99" +
//      "9" +
//      pos("VENDORNAME") +
//      pos("VENDORADDRESS") +
//      pos("VENDORCITY") +
//      pos("VENDORSTATE") +
//      pos("VENDORPOSTALCODE") +
//      pos("VENDORLOC_EMAILADDRESS") +
//      "EXP" +
//      "0"
//    ]


//    val vendorRDD = sc.sparkContext.makeRDD[RDD](newVendor)

//    val newVendor = sc.sparkContext.createDataFrame(vendorRDD, instSchema)

//          .format("csv")
//          .option("header", "true")
//          .option("delimiter", "\t")
//          .save("/Users/ktwitchell001/workspace/VADataDemo/VAPOData/VA_opendata_FY2006_Vendors.txt");


    //    pos.select("VENDORID",
    //      "VENDORNAME",
    //      "VENDORADDRESS",
    //      "VENDORCITY",
    //      "VENDORSTATE",
    //      "VENDORPOSTALCODE",
    //      "VENDORLOC_EMAILADDRESS").write
    //      .format("csv")
    //      .option("header", "true")
    //      .option("delimiter", "\t")
    //      .save("/Users/ktwitchell001/workspace/VADataDemo/VAPOData/VA_opendata_FY2006_Vendors.txt");
    //
    //


    // Element      Data Type                Default Value     KEY col Description
    val transSchema: StructType = StructType(Seq(
    StructField("instID",StringType,true),                     // "0",         X  1  Instrument ID
    StructField("transjrnlID",StringType,true),                     // "0",            2  Business Event / Journal ID
    StructField("transjrnlLineID",StringType,true),                 // "1",            3  Jrnl Line No
    StructField("transjrnlDescript",StringType,true),               // " ",            4  Journal / Event Description
    StructField("transledgerID",StringType,true),                   // "ACTUALS",   X  5  Ledger
    StructField("transjrnlType",StringType,true),                   // "FIN",       X  6  Jrnl Type
    StructField("transbookCodeID",StringType,true),            // "SHRD-3RD-PARTY", X  7  Book-code / Basis
    StructField("translegalEntityID",StringType,true),              // "STATE-OF-VA"X  8  Legal Entity (CO. / Owner)
    StructField("transcenterID",StringType,true),                   // "0",         X  9  Center ID
    StructField("transprojectID",StringType,true),                  // "0",         X  10 Project ID
    StructField("transproductID",StringType,true),                  // "0",         X  11 Product / Material ID
    StructField("transaccountID",StringType,true),                  // "0",         X  12 Nominal Account
    StructField("transcurrencyCodeSourceID",StringType,true),       // "USD",       X  13 Curr. Code Source
    StructField("transcurrencyTypeCodeSourceID",StringType,true),   // "TXN",       X  14 Currency Type Code Source
    StructField("transcurrencyCodeTargetID",StringType,true),       // "USD",       X  15 Curr. Code Target
    StructField("transcurrencyTypeCodeTargetID",StringType,true),   // "BASE-LE",   X  16 Currency Type Code Target
    StructField("transtransAmount",DoubleType,true),                // "0",            17 Transaction Amount
//      StructField("transtransAmount",StringType,true),                // "0",            17 Transaction Amount
    StructField("transfiscalPeriod",StringType,true),               // "0",         X  18 Fiscal Period
    StructField("transacctDate",StringType,true),                   // "0",               Acctg. Date
    StructField("transtransDate",StringType,true),                  // "0",               Transaction Date
    StructField("transdirVsOffsetFlg",StringType,true),             // "O",               Direct vs. Offset Flag
    StructField("transreconcileFlg",StringType,true),               // "N",               Reconciliable Flag
    StructField("transadjustFlg",StringType,true),                  // "N",               Adjustment Flag
    StructField("transmovementFlg",StringType,true),                // "N",               Movement Flag
    StructField("transunitOfMeasure",StringType,true),              // " ",               Unit of Measure
    StructField("transstatisticAmount",StringType,true),            // " ",               Statistical Amount
    StructField("transextensionIDAuditTrail",StringType,true),      // " ",               Audit Trial Extension ID
    StructField("transextensionIDSource",StringType,true),          // " ",               Source Extension ID
    StructField("transextensionIDClass",StringType,true),           // " ",               Classification Extension ID
    StructField("transextensionIDDates",StringType,true),           // " ",               Date Extension ID
    StructField("transextensionIDCustom",StringType,true)          // " "                Other Customization Ext ID
    ))

//    val transactions = sc.read.schema(transSchema).csv("/Users/ktwitchell001/workspace/VADataDemo/VAJoinTest/C1Data.txtInstIDCombo.txt")
//    val transactions = sc.read.schema(transSchema).csv("/Users/ktwitchell001/workspace/VADataDemo/VAJoinTest/C1Data.txtInstIDCombo.txt")
//    println(transactions.count())
//    println(transactions.schema)

//    val inst = sc.read.schema(instSchema).csv("/Users/ktwitchell001/workspace/VADataDemo/VAJoinTest/VATestInstTBL.txtInstIDCombo.txt")
//    val inst = sc.read.schema(instSchema).csv("/Users/ktwitchell001/workspace/VADataDemo/VAJoinTest/VATestInstTBL.txtInstIDCombo.txt")
//    println(inst.count())
//    transactions.show()
//    inst.show()

//    val combined = transactions.join(inst,"instID").orderBy("instID")
//    val combined = transactions.join(inst,"instID")

    // Group Combined Entity by Legal Entity and Instrument Type

//    val aggOutput = combined.groupBy("translegalEntityID","instTypeID").sum("transtransAmount")
//    aggOutput.show(200)
//
//    aggOutput.write.csv("/Users/ktwitchell001/workspace/VADataDemo/VAJoinTest/SparkCombined.txtInstIDCombo.txt")
    //    aggOutput.write.csv("/Users/ktwitchell001/workspace/VADataDemo/VAJoinTest/SparkCombined.txtInstIDCombo.txt")

    System.exit(0)

  }

}
