/*
 *
 * SAFR on Spark (Prototype)
 * (c) Copyright IBM Corporation. 2015
 * SPDX-License-Identifier: Apache-2.0
 * By Adrash Pannu
 *
 */
//import org.apache.spark._
//import RDDExtension._
//
//object TeeRDDTest {
//  def main(args: Array[String]) {
//    val fileName = "hdfs://localhost:9000/user/Adarsh/airports.csv"
//    val conf = new SparkConf()
//      .setAppName("Test App")
//      .setMaster("local[2]")
//      .set("spark.logLineage", "true")
//      // .set("spark.hadoop.outputCommitCoordination.enabled", "false")
//    val sc = new SparkContext(conf)
//
//    // Input record: <"SJC","San Jose International","San Jose","CA","USA",37.36186194,-121.9290089>
//    val rdd = sc.textFile(fileName, 2)
//      .map(x => x.split(","))
//      .map(x => (x(0), x(1)))
//      .tee("hdfs://localhost:9000/tmp/airports/san.txt", x => x._2.startsWith( """"San"""))
//      //.sortByKey()
//      .tee("hdfs://localhost:9000/tmp/airports/los.txt", x => x._2.startsWith( """"Los"""))
//      .map(e => e._2)
//
//    val writers = TeeRDDUtil.getWriters(rdd)
//
//    rdd.collect()
//
//    for (w <- writers) {
//      w.commitJob()
//    }
//  }
//}
