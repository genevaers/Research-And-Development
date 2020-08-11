/*
 *
 * SAFR on Spark (Prototype)
 * (c) Copyright IBM Corporation. 2015
 * SPDX-License-Identifier: Apache-2.0
 * By Adrash Pannu
 *
 */

//// Scratch.
//
//import java.math
//
//import org.apache.spark.{SparkConf, SparkContext}
//import org.apache.spark.sql.{SQLContext}
//
//import scala.math.BigDecimal
//
//object Scratch {
//  def main(args: Array[String]) = {
//    //----------------------------------------------------------------------------------------------------------------//
//    val conf = new SparkConf(true)
//      .setAppName("Scratch")
//      .setMaster("local[2]")
//      .set("spark.cassandra.connection.host", "127.0.0.1")
//
//    //.set("spark.ui.port", "4090")
//    val sc = new SparkContext(conf)
//    val sqlContext = new SQLContext(sc)
//    //----------------------------------------------------------------------------------------------------------------//
//
//    testPartitions(sc, sqlContext)
//  }
//
//  def testCassandraRead(sc: SparkContext, sqlContext: SQLContext) = {
//    import com.datastax.spark.connector._
//
//    val glrdd = sc.cassandraTable("safr", "gl")
//
//    glrdd.collect.foreach(println)
//
//    case class SALES(id: Int, delta: math.BigDecimal)
//
//    val salestextrdd = sc.textFile("hdfs://localhost:9000/user/Adarsh/CKB/SALES").map {row =>
//      val splits = row.split(",")
//      new SALES(splits(0).toInt, new java.math.BigDecimal(splits(1)))
//    }
//
//
//    salestextrdd.collect.foreach(println)
//
//    // val joinrdd = glrdd.joinWithCassandraTable("safr","sales")
//
//    // salestextrdd.joinWithCassandraTable("safr","sales").collect
//  }
//
//  def testDeleteFile(sc: SparkContext, sqlContext: SQLContext) = {
//    val fileName = "hdfs://localhost:9000/user/Adarsh/OUTPUTDIR"
//
//    import org.apache.hadoop.conf.Configuration
//    import org.apache.hadoop.fs.FileSystem
//    import org.apache.hadoop.fs.Path
//    import java.net.URI
//    import collection.JavaConverters._
//
//    val conf = new Configuration
//
//    val fs = FileSystem.get(new URI(fileName), conf, null)
//
//    fs.exists(new Path(fileName))
//
//  }
//
//  def testParquet(sc: SparkContext, sqlContext: SQLContext) = {
//    import org.apache.spark.sql.Row;
//    import org.apache.spark.sql.types.{StructType, StructField, StringType};
//    val schemaString = "id dept name dob email"
//
//    val schema =
//      StructType(
//        schemaString.split(" ").map(fieldName => StructField(fieldName, StringType, true)))
//
//    // "0001HRRichard19630104richardh@company.com"
//    val offsets = Array(4, 2, 7, 8, 20)
//    val factrdd = sc.textFile("hdfs://localhost:9000/user/Adarsh/Fact.tbl").map(row => {
//      var cur = 0
//      val elems = for (o <- offsets) yield {
//        val s = row.substring(cur, cur + o)
//        cur = cur + o
//        s
//      }
//      Row(elems: _*)
//    })
//
//    val factdf = sqlContext.createDataFrame(factrdd, schema)
//
//    factdf.registerTempTable("people")
//
//    val results = sqlContext.sql("SELECT * FROM people")
//
//    results.write.parquet("hdfs://localhost:9000/user/Adarsh/Fact.parquet")
//
//    val df = sqlContext.read.load("hdfs://localhost:9000/user/Adarsh/Fact.parquet")
//
//  }
//
//  def testPartitions(sc: SparkContext, sqlContext: SQLContext) = {
//
//    val path = "hdfs://localhost:9000/user/Adarsh/CKB/GL"
//    val df = sqlContext.read.format("com.databricks.spark.csv").option("header", "false").load(path)
//
//    df.rdd.partitions
//
//    // Get preferred locations
//    df.rdd.partitions.foreach { part => df.rdd.preferredLocations(part) }
//
//    df.rdd.coalesce(1).collect.foreach(println)
//
//    val rdds = for (part <- 1 to 2) {
//      val path = "hdfs://localhost:9000/user/Adarsh/CKB/GL/!!.csv".replaceAll("!!", part.toString)
//      val df = sqlContext.read.format("com.databricks.spark.csv").option("header", "false").load(path)
//      println(s"$path, #partitions = ${df.rdd.partitions.length}")
//      df.show
//    }
//
//
//  }
//
//  def testRegex(sc: SparkContext, sqlContext: SQLContext) = {
//    val path = "hdfs://localhost:9000/user/Adarsh/CKB/GL/[0-9].csv"
//    val rangerex = """^.*\[(\d+)-(\d+)\].*$""".r
//    val range = Option(path) collect { case rangerex(from, to) => (from.toInt, to.toInt) }
//    for ((from, to) <- range) {
//      val repregex = """\[(\d+)-(\d+)\]""".r
//      for (i <- from to to) yield {
//        println (repregex.replaceFirstIn(path, i.toString))
//      }
//    }
//  }
//}
