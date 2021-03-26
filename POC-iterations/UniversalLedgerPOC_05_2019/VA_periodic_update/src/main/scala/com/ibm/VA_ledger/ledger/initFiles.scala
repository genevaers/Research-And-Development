package com.ibm.VA_ledger.ledger

/*
 *(c) Copyright IBM Corporation. 2018
 * SPDX-License-Identifier: Apache-2.0
 * By Kip Twitchell
 * //  Created May 2019
 */
//______________________________________________________________________________________________
// Creates the Vendor ID file, overwriting any existing files.
//______________________________________________________________________________________________


import java.sql.{Connection, DriverManager}

import org.apache.log4j._
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.functions.{lit, regexp_replace}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}


object initFiles {


  def apply(inputPOPath: String, fileOutLocation: String): Unit = {



    // connect to the database named "univledger" on the localhost
    val driver = "org.postgresql.Driver"
    val url = "jdbc:postgresql://ulserver/universal_ledger"
    val username = "vagrant"
    val password = "univledger"

    // there's probably a better way to do this
    var connection:Connection = null

    println("connecting to database")

    try {
      // make the connection
//      Class.forName(driver)
      connection = DriverManager.getConnection(url, username, password )

      val statement0 = connection.createStatement()
      val resultSet0 = statement0.execute("CREATE TABLE test.test (coltest varchar(20))")

      val statement1 = connection.createStatement()
      val resultSet1 = statement1.execute("insert into test.test (coltest) values ('It works!')")

      // create the statement, and run the select query
      val statement = connection.createStatement()
      val resultSet = statement.executeQuery("SELECT * from test.test")
      while ( resultSet.next() ) {
//        val host = resultSet.getString("host")
        val result = resultSet.getString("coltest")
        println("test value =  " + result)
      }
      val statement3 = connection.createStatement()
      val resultSet3 = statement3.execute("DROP TABLE test.test")

    } catch {
      case e : Throwable => e.printStackTrace
    }
    connection.close()

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
