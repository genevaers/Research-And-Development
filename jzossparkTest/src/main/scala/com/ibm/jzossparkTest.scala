package com.ibm.jzossparkTest

// (c) Copyright IBM Corporation 2020.  
//     Copyright Contributors to the GenevaERS Project.*
// SPDX-License-Identifier: Apache-2.0
//
// ***************************************************************************
//                                                                           
//   Licensed under the Apache License, Version 2.0 (the "License");         
//   you may not use this file except in compliance with the License.        
//   You may obtain a copy of the License at                                 
//                                                                           
//     http://www.apache.org/licenses/LICENSE-2.0                            
//                                                                           
//   Unless required by applicable law or agreed to in writing, software     
//   distributed under the License is distributed on an "AS IS" BASIS,       
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and     
//   limitations under the License.                                          
// ****************************************************************************

import org.apache.log4j._
import com.ibm.jzos._
import org.apache.spark.{SparkConf, SparkContext}
//import org.apache.spark.sql.SparkSession


case class Agency(agencyCode:String,
                  agencyName:String)

object jzossparkTest {

  def main(args: Array[String]) {

    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    val conf = new SparkConf().setAppName("test").setMaster("local[*]")
    val spark = new SparkContext(conf)

    // for use with spark-sql
    //    val spark = SparkSession
//      .builder
//      .appName("Simple Application")
//      .config("spark.master", "local")
//      .getOrCreate()

    val metricsEnabled = true
    val encoding = ZUtil.getDefaultPlatformEncoding

    val filePaths = Map("inputPath" -> "'NGSAFR.JZOSSPK.", "outputPath" ->
      "'NGSAFR.JZOSSPK.")

    val listOfAgencies = populateAgencyData(filePaths,encoding)

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
    }


    // existing code from spark scratchpad

    println("Agencies: " + listOfAgencies)
//    val list = html.split("\n").filter(_ != "")
//    val rdds = sc.parallelize(list)
//    val counts = list.flatMap(line => line.split(" "))
//    System.exit(0)

  }


}
