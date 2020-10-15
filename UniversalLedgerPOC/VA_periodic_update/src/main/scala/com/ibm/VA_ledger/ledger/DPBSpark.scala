package com.ibm.VA_ledger.ledger

/*
 *(c) Copyright IBM Corporation. 2018
 * SPDX-License-Identifier: Apache-2.0
 * By Kip Twitchell
 * //  Created May 2019
 */
//______________________________________________________________________________________________
// Created for David Paget-Brown Spark work.  Currently creates Parquet Files.
//______________________________________________________________________________________________


import java.sql.{Connection, DriverManager}

import org.apache.log4j._
import org.apache.spark.sql.{SaveMode, SparkSession}


object DPBSpark {


  def apply(inputPOPath: String, fileOutLocation: String): Unit = {



    //______________________________________________________________________________________________
    //  Establish Spark Environment
    //______________________________________________________________________________________________
//    Logger.getLogger("org").setLevel(Level.INFO)
    Logger.getLogger("org").setLevel(Level.ERROR)

//    val conf: SparkConf =
//      new SparkConf()
//        .setMaster("local[*]")
//        .setAppName("VA PO Vendor ID Assign")
//        .set("spark.driver.host", "localhost")
//    val sc: SparkContext = new SparkContext(conf)

    val sc = SparkSession
      .builder()
      .appName("VA PO Vendor ID Assign")
      .config("spark.eventLog.enabled",true)
      .config("spark.master", "local")
      .getOrCreate()

    import sc.implicits._

    //______________________________________________________________________________________________
    //  File Handling Environment Creation
    //______________________________________________________________________________________________

    val outputPath =       fileOutLocation

    println("Creating Initial Vendor ID Parquet File")

    val defaultVendorID = List("999999999999")
    val vendorDF = sc.sparkContext.parallelize(defaultVendorID).toDF
    vendorDF.write.mode(SaveMode.Overwrite).parquet(fileOutLocation + "vendorID.parquet")

    System.exit(0)

  }

}
