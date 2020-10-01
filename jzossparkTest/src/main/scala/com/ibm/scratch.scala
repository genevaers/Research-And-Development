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
import org.apache.spark.{SparkConf, SparkContext}
//import org.apache.spark.sql.SparkSession

object scratch {



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

    val html = scala.io.Source.fromURL("https://www.lds.org/scriptures/bofm/ether/1.1-43?lang=eng").mkString
    println("html: " + html)

    val counts: Array[String] = html.split(" ")
    println("Word Count: " + counts)
//    val list = html.split("\n").filter(_ != "")
//    val rdds = sc.parallelize(list)
//    val counts = list.flatMap(line => line.split(" "))
//    System.exit(0)

  }


}
