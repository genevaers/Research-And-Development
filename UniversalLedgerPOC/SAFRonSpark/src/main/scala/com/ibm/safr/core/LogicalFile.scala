package com.ibm.safr.core

/*
 * LogicalFile.scala: Support container structures
 *
 * SAFR on Spark (Prototype)
 * (c) Copyright IBM Corporation. 2015
 * SPDX-License-Identifier: Apache-2.0
 * By Adrash Pannu
 *
 */

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrameReader, Encoders, SQLContext}
import com.ibm.safr.core.SAFR._
//import com.apache.spark.implicts._


// LogicalFile
case class LogicalFile(
                        id: Int,
                        name: String,
                        lrName: Option[String],
                        driver: String,
                        options: Map[String, String]
                        ) extends NamedExpr {
  def isPipe = driver == "PIPE" || driver == "TOKEN"

  /**
   * openRDD: Turn a LogicalFile into an RDD. An LF can be "logically partitioned (i.e.
   * the path contains a pattern of the kind [0-10]). The pattern is expanded and a
   * set of individual Dataframes/RDDs are created and unioned together. LFs that don't
   * contain these patterns can be physically partitioned owing to HDFS splits, etc. Those
   * LFs are not unioned.
   */
  def openRDD(sqlContext: SQLContext, lr: Option[LogicalRecord]): RDD[DataRow] = {
    val path = options.getOrElse("path", "")
    val extrx = """^.*\[(\d+)-(\d+)\].*$""".r
    val reprx = """\[(\d+)-(\d+)\]""".r

    // Does the "path" option contain a pattern?
    val range = Option(path) collect { case extrx(from, to) => (from.toInt, to.toInt) }

    // Extract partition range
    val (from, to) = if (range.isDefined) (range.get._1, range.get._2) else (0, 0)

    // Expand the range to generate individual Dataframes/RDDs. This loop works
    // correctly if no range was specified (i.e. no replacement is done).
    val rdds = for (i <- from to to) yield {
      val newpath = reprx.replaceFirstIn(path, i.toString)

      // println(s"--- Opening path: $newpath")

      val newoptions = if (path != "") options + ("path" -> newpath) else options

      val reader0: DataFrameReader =
        if (name.endsWith("parquet"))
          sqlContext.read
        else
          sqlContext.read.format(driver).options(newoptions)

      val reader = if (lr.isDefined) reader0.schema(lr.get.toSQLSchema) else reader0

      val df = if (name.endsWith("parquet")) reader.load(newoptions("path")) else reader.load()
      val rdd: RDD[DataRow] = df.rdd.map(row => new DataRowImmutable(row, id))

      if (from != to) rdd.coalesce(1) else rdd
    }

    val retrdd = if (rdds.length == 1) rdds.head else sqlContext.sparkContext.union(rdds)
    retrdd.setName(name)
    retrdd
  }
}
