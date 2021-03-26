/**
 * SAFR.scala: Main Driver Function
 *
 * SAFR on Spark (Prototype)
 * (c) Copyright IBM Corporation. 2015
 * SPDX-License-Identifier: Apache-2.0
 * By Adrash Pannu
 *
 */
package com.ibm.safr.core

import java.io.Serializable

import com.databricks.spark.csv.CsvParser
import com.ibm.safr.core.DataTypeTag._
import com.ibm.safr.core.Compiler._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.types._
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, DataFrameReader, SQLContext}

import scala.collection.mutable.{ArrayBuffer, Queue, WrappedArray, Map => MMap}
import scala.io.Source
import scala.language.postfixOps
import scala.reflect.ClassTag
import org.apache.hadoop.io.compress.GzipCodec



object SAFR extends Logger {
  implicit def RDD2MergeRDDFunctions[T: Ordering : ClassTag](rdd: RDD[(T, _)]): MergeRDDFunctions[T] = MergeRDDFunctions[T](rdd)

  def indent(str: String, indentation: Int) = {
    val lines = str.split("\n")
    (lines.map { line => (" " * indentation) + line }.mkString("\n")) + (if (str.takeRight(1) == "\n") "\n" else "")
  }

  // toIntOrDefault: Attempt to convert a String to an Int. If the input is malformed, return `default`
  def toIntOrDefault(s: String, default: Int = 0): Int = {
    try {
      s.toInt
    }
    catch {
      case e: java.lang.NumberFormatException => default
    }
  }

  // FieldIndex: Index into LogicalRecord `fields`.
  type FieldIndex = Int

  type UberLookupMapType = Map[String, Map[LookupKey, Array[LookupValue]]]

  // dateFormatMap: Translate SAFR date formats to ones that Java understands
  val dateFormatMap = Map(
    "CYM" -> "yyyymm",
    "CYMD" -> "yyyymmdd"
  )

  val datatypeEnumtoString = Map(CHAR -> "CHAR", INT -> "INT", DOUBLE -> "DOUBLE", BOOL -> "BOOLEAN", DATE -> "DATE")

  val datatypeDefaultLength = Map(INT -> 4, DOUBLE -> 8, BOOL -> 1, VOID -> 1, DATE -> 1)

  val datatypeStringToEnum = datatypeEnumtoString.toList.map { case (k, v) => (v, k) }.toMap + ("ALNUM" -> CHAR)

  val sqltosafrtypes = Map("IntegerType" -> INT, "StringType" -> CHAR, "DoubleType" -> DOUBLE,
    "BooleanType" -> BOOL, "DateType" -> DATE)

  val safrtosqltypes = Map(INT -> IntegerType, CHAR -> StringType, DOUBLE -> DoubleType, BOOL -> BooleanType, DATE -> DateType)

  /*
  val functions = Array(
    FunctionDef("COUNT", true, false, Seq(INT))).
    map(e => (e.name, e)).toMap
    */

  private var id: Int = 0

  def nextId = {
    id += 1
    id
  }

  type DataValue = Any

  implicit val ord = new Ordering[DataRow] {
    def compare(a: DataRow, b: DataRow): Int = {
      assert(a.numColumns == b.numColumns)
      var i = 0
      var cmpval = 0
      while (i < a.numColumns && cmpval == 0) {
        assert(a(i).getClass == b(i).getClass)
        val cmpval = (a(i), b(i)) match {
          case (av: String, bv: String) => av compare bv
          case (av: Int, bv: Int) => av compare bv
        }
        i += 1
      }
      cmpval
    }
  }

  def main(args: Array[String]): Unit = {

    println("*** SAFR/Spark ***\n")
    println("*** New Local Test/Spark ***\n")

    // Parse arguments
    if (args.length == 0)
      throw new Exception(s"Invalid invocation.\nUsage: SAFRSpark sql-file")

    // Initialize SparkContext
    val conf = new SparkConf()
      .setAppName("SAFRSpark")
      .set("spark.ui.port", "4050")
      .set("spark.rpc.numRetries", "1")
      //.set("spark.default.parallelism", "1") // TODO: Fix hack to prevent splitting of textFiles under MergeRDDs
      .set("spark.cassandra.connection.host", "127.0.0.1")

    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    // conf.set("spark.kryo.registrationRequired", "true")

    // Temporary kludge: The master property needs to be specified elsewhere.
    conf.setMaster("local[2]")

    logInfo("Using Spark master: " + conf.get("spark.master"))

    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)

    // Call main routine
    run(sqlContext, args(0))
    // testCKB()

    sc.stop()

  }

  def testCKB() = {

    /*
    val flights = sparkContext.textFile("hdfs://localhost:9000/user/Adarsh/CKB/GL")
    flights.count

    val path = "hdfs://localhost:9000/user/Adarsh/CKB/GL"
    val df = sqlContext.read.format("com.databricks.spark.csv").option("header", "false").load(path)


    val rdds = for (file <- Seq("GL", "SALES")) yield {
      val path = s"hdfs://localhost:9000/user/Adarsh/CKB/${file}"
      val df = sqlContext.read.format("com.databricks.spark.csv").option("header", "false").load(path)
      df.show
      val rdd = df.rdd.map { row => row(0) -> row.toSeq.drop(1) }

      println(s"$path: #partitions = ${rdd.partitions.length}")
      rdd
    }
    val cg = new MergeRDD(rdds)
    // println(cg.toDebugString)
    // val cg = rdds(0).cogroup(rdds(1))
    cg.collect.foreach { case (k, v) => println(s"$k -> ${v.toSeq}") }
        */

  }

  import java.io.File

  def deleteLocalFile(f: File): Unit = {
    if (f.isDirectory)
      f.listFiles().foreach(e => deleteLocalFile(e))

    f.delete()
  }

  def deleteFile(fileName: String) = {
    import org.apache.hadoop.conf.Configuration
    import org.apache.hadoop.fs.FileSystem
    import org.apache.hadoop.fs.Path
    import java.net.URI

    val conf = new Configuration
    val fs = FileSystem.get(new URI(fileName), conf, null)

    if (fs.exists(new Path(fileName))) {
      fs.delete(new Path(fileName), true)
    } else {
      false
    }
  }

  /**
   * run - Main routine that drives the views
   */
  def run(sqlContext: SQLContext, inputFile: String): Unit = {

    logInfo(s"Running $inputFile")

    val sqlString = Source.fromFile(inputFile).getLines().mkString("\n")
    implicit val metadata = new Metadata(sqlString, sqlContext)
    val configMap: Map[String, String] = metadata.configMap

    // Check if we need to sleep for debugging?
    checkSleep(metadata)

    // Determine if we're in "parse only" mode
    val parseOnly = getConfigAsBoolean("PARSE_ONLY")

    // Force linking in CsvParser :-)
    new CsvParser

    if (!parseOnly) {
      // Run views
      logInfo("Running views ...")

      // Make sure we have at most one merge in this job
      assert(metadata.mergeFiles.size <= 1)
      if (metadata.mergeFiles.isEmpty) {
        runViews(sqlContext)
      } else {
        runViews(sqlContext)
        // runCKBWithViews()
      }

      // Check if we need to sleep for debugging?
      checkSleep(metadata)
    }
  }

  def checkSleep(metadata: Metadata): Unit = {
    // Check if we need to sleep for Spark Web UI debugging?
    val sleeptime = getConfigAsInt("SLEEPSECS")(metadata)
    if (sleeptime > 0) {
      println(s"Sleeping for $sleeptime seconds ...")
      Thread sleep sleeptime * 1000
    }
  }

  /**
   * runViews - Main execution engine.
   *
   * CAUTION: When passing closures to Spark, beware of inadvertently pulling in objects that aren't
   * need for task execution (i.e. the "pizza effect")
   */
  def runViews(sqlContext: SQLContext)(implicit metadata: Metadata) = {

    // Aggregate all inputs from all data sources into a list of RDDs.
    // Each RDD returns a tagged DataRowSeq. Spark will parallelize the partitions as it may, but
    // the tags tell us the source of the underlying data.

    val sc = sqlContext.sparkContext
    val configMap: Map[String, String] = metadata.configMap
    val viewCount = metadata.views.length
    val hasMerge = metadata.mergeFiles.nonEmpty

    val inputrddnonCKB: RDD[DataRow] = if (!hasMerge) {
      // Non-CKB scenario

      // Make unique list of data sources
      val uniqueDS = (for (view <- metadata.views;
                           scan <- view.scans if !scan.lf.isPipe) yield {
        // (Filename, LF, PF, LR)
        val tuple = (scan.lf.name, scan.lf.id, scan.lr.id)
        tuple
      }).distinct

      val partrdds = for (d <- uniqueDS) yield {
        logInfo( s"""Preparing data source: $d """)
        // Make local variables to avoid pizza effect
        val lfname = d._1
        val lf = metadata.logicalFiles(d._2)
        val lr = metadata.logicalRecords(d._3)
        lf.openRDD(sqlContext, Option(lr))
      }

      // Make a union RDD out of all children
      if (partrdds.length == 1) partrdds.head else sc.union(partrdds)
    } else null

    val inputrddCKB =
      if (hasMerge) {
        // Get a handle to the merge
        val mf = metadata.mergeFiles.values.head

        val ckbinputs: Seq[RDD[(DataRow, DataRow)]] = for (mfi <- mf.inputs) yield {
          logInfo( s"""Unresolved: $mf""")
          val mfiCompiled = new MergeFileInputResolved(mfi, metadata)

          logInfo( s"""Compiled: $mfiCompiled""")

          val rdd = mfiCompiled.lf.openRDD(sqlContext, Option(mfiCompiled.lr))

          // Extract the key fields
          val keyLength_ = mfi.keys.length
          val mfiCompiled0 = mfiCompiled
          val compiledKeys0: Seq[Expr] = mfiCompiled0.compiledKeys

          val fe = compiledKeys0.head.asInstanceOf[AssignExpr].rhs.asInstanceOf[FieldExpr]

          // Turn each RDD input into a [K,V] RDD
          val rdd2: RDD[(DataRow, DataRow)] = rdd.map(inrow => {
            val numKeyCols = keyLength_
            val outrow = new DataRowMutable(numKeyCols)
            val exprRunner = new ExprRunner(null, null, null, null)

            for (expr <- compiledKeys0)
              exprRunner.evalExpr(expr, inrow, outrow)

            (outrow, inrow)
          })

          // Tag this RDD with the corresponding LF
          rdd2.setName(mfi.lfName)
          rdd2
        }

        // Ensure that each input has the same # of partitions
        val partitionCounts = ckbinputs.map(e => e.partitions.length).toSet
        if (partitionCounts.size != 1) {
          logFatal("Merge inputs have differing partition counts: " +
            ckbinputs.map(e => "%s (%d)".format(e.name, e.partitions.length)).mkString(", "))
        }

        //sparkContext.union(ckbinputs).groupByKey()
        new MergeRDD(ckbinputs)
      }

      else
        null

    if (hasMerge) {
      inputrddCKB.collect.foreach { case (k, v) => println(s"$k -> ${v.toSeq}") }
    }

    val inputrdd = if (hasMerge) inputrddCKB else inputrddnonCKB

    // Aggregate all compiled units from all sources. We don't want to send this as a part
    // of task closure as they may get too big.
    // uberCompiledUnits(LogicalFile.id -> (LogicalFile.id, (CompiledUnit, CompiledUnit)))
    val uberCompiledUnits: Map[FieldIndex, Vector[(FieldIndex, (ExprBlock, ExprBlock), Int)]] =
      (for (view <- metadata.views;
            ds <- view.scans)
        yield {
          (ds.lf.id, (ds.filterBlock, ds.projBlock), view.id)
        }).groupBy(_._1).mapValues(v => v.toVector)

    // On Spark, broadcast the lookup map
    val uberCompiledUnitsHandle_ : Broadcast[Map[FieldIndex, Vector[(FieldIndex, (ExprBlock, ExprBlock), Int)]]] = sc.broadcast(uberCompiledUnits)

    logDebug("\n--- INPUT ROWS --")

    // Build View -> Name map
    val view2NameMap = metadata.views.map(v => v.id -> v.name).toMap
    val view2NameMapHandle_ : Broadcast[Map[FieldIndex, String]] = sc.broadcast(view2NameMap)

    // Build Uber Lookup Map
    val uberLookupMap: Map[String, Map[LookupKey, Array[LookupValue]]] = LookupEngine.buildUberLookupMap(sqlContext, metadata, configMap)

    // Broadcast the lookup map
    val uberLookupMapHandle_ : Broadcast[Map[String, Map[LookupKey, Array[LookupValue]]]] = sc.broadcast(uberLookupMap)

    val accumRowsProcessed = sc.accumulator(0, "Cumulative Rows Processed Internally")
    val accumRowsReadFromDisk = sc.accumulator(0, "Cumulative Rows Read from Disk")

    val rxit: String = if (hasMerge) {
      val mfoptions = metadata.mergeFiles.values.head.options
      mfoptions.getOrElse("RXIT", null)
    } else
      null

    // Construct the output rdd.
    val outputrdd: RDD[Any] = inputrdd.mapPartitions(inputIter => new Iterator[Any] {

      // `inrows` is the merry-go-round that contains the primordial row and any generated rows piped
      // back to other views. Quite conveniently, it is a queue structure. For performance reasons, we could
      // replace this with a bounded array sometime in the future.
      val inrows = Queue[DataRow]()

      // `outrows` is the set of rows emitted out and meant to be written to their respective files.
      val outrows = Queue[DataRow]()

      // Maintain map of LFID -> previous DataRow
      val previousRowMap: MMap[Int, DataRow] = scala.collection.mutable.Map()

      // Construct Expression Runner
      val exprRunner = new ExprRunner(uberLookupMapHandle_, inrows, outrows, previousRowMap)

      // Load class and method for CKB exits, if defined
      val clsInfo =
        if (hasMerge) {
          // Run CKB read exit, if defined
          val rxitCls2: Class[_] = Thread.currentThread().getContextClassLoader.loadClass(rxit)
          logInfo(s"Loaded CKB RXIT class: ${rxitCls2.getName}")

          val rxitObject2 = rxitCls2.newInstance()
          logInfo(s"Instantiated CKB instance: ${rxitCls2.toGenericString}")

          val rxitMethod2 = rxitCls2.getMethod("run", classOf[Array[Iterable[DataRow]]])
          logInfo(s"Loaded CKB RXIT method: ${rxitMethod2.toGenericString}")

          (rxitCls2, rxitObject2, rxitMethod2)
        } else
          (null, null, null)

      val rxitCls = clsInfo._1
      val rxitObject = clsInfo._2
      val rxitMethod: java.lang.reflect.Method = clsInfo._3

      override def hasNext: Boolean = {
        if (outrows.isEmpty) processInputRow()
        outrows.nonEmpty
      }

      override def next(): DataRow = {
        if (hasNext)
          outrows.dequeue()
        else
          throw new Exception("SAFRIterator: next() called on empty iterator.")
      }

      def processInputRow() = {

        // Fetch another input row, if needed
        if (inrows.isEmpty && inputIter.hasNext) {
          if (hasMerge) {
            // Grab the cogroup

            val next0 = inputIter.next()
            val ckb = next0.asInstanceOf[(DataRow, Array[Iterable[DataRow]])]._2

            // Run RXIT if defined
            val cogroup: Array[Iterable[DataRow]] =
              if (rxitObject != null) {
                rxitMethod.invoke(rxitObject, ckb).asInstanceOf[Array[Iterable[DataRow]]]
              } else
                ckb

            logDebug(s"Read from cogroup: ${cogroup.toSeq}")

            for (e <- cogroup) {
              // logDebug(s"Read from cogroup: $e")
              inrows ++= e

              // TODO: This is where we build the CKB-lookup tables
            }
          } else {
            inrows += inputIter.next().asInstanceOf[DataRow]
            accumRowsReadFromDisk += 1
          }
        }

        // Generate output rows and collect them in `outrows`
        while (inrows.nonEmpty) {
          val inrow = inrows.dequeue()

          accumRowsProcessed += 1

          // logDebug(s"INROW(${inrow.inputTag}): ${inrow.toStringAlt}")

          // Who wants this row? It is possible that nobody wants it. This can happen if view V1 writes to
          // a pipe, but there's no corresponding pipe reader.
          val viewMap = uberCompiledUnitsHandle_.value
          val candidateViews: Seq[(FieldIndex, (ExprBlock, ExprBlock), Int)] = viewMap.getOrElse(inrow.inputTag, Seq())

          if (candidateViews.isEmpty)
            logDebug(s"No subscription for row: ${inrow.toStringAlt}")

          var viewNum = 0
          while (viewNum < candidateViews.length) {
            val cv: (FieldIndex, (ExprBlock, ExprBlock), FieldIndex) = candidateViews(viewNum)
            viewNum += 1

            val filterBlock = cv._2._1
            val projBlock = cv._2._2
            var value: DataValue = false
            var recordSelected = true

            // Run record filter
            if (filterBlock != null && filterBlock.exprList.length > 0) {
              var recordFilterExprList: Seq[Expr] = filterBlock.exprList
              // logDebug(s"Running record filter")

              var exprNum = 0
              while (exprNum < recordFilterExprList.length) {
                val expr = recordFilterExprList(exprNum)
                value = exprRunner.evalExpr(expr, inrow, null)
                exprNum += 1
              }
              recordSelected = value.asInstanceOf[Boolean]
            }

            if (recordSelected) {
              // Make an output row based on ExtractColumns
              val outrow: DataRow = new DataRowMutable(projBlock.numColumns, inputTag = -1, outputTag = cv._3)

              // Run column assignments
              val extractColumnsExprList = projBlock.exprList
              var exprNum = 0
              while (exprNum < extractColumnsExprList.length) {
                val expr = extractColumnsExprList(exprNum)
                exprRunner.evalExpr(expr, inrow, outrow)
                exprNum += 1
              }

              // If the view didn't write a row out, let's do so
              val writeCalled = extractColumnsExprList.last.isInstanceOf[WriteExpr]
              if (! writeCalled) {
                outrows += outrow
              }
            }
          }

          // We're done with the current row. Save it as previous.
          // TODO: We must only do this if there's a PREVIOUS expression in the view.
          previousRowMap += (inrow.inputTag -> inrow)
        }
      }
    })

    val viewOutput = getConfigAsString("VIEWOUTPUT")

    val outputAction =
      viewOutput match {
        case "COUNT" =>
          val t0 = System.nanoTime()

          val count = outputrdd.count()

          val t1 = System.nanoTime()

          logInfo("-")
          logInfo(s"Cumulative Rows Read from Disk: ${accumRowsReadFromDisk.value}")
          logInfo(s"Cumulative Rows Processed Internally: ${accumRowsProcessed.value}")
          logInfo("Elapsed time: " + (t1 - t0) / 1000000 + "ms")
          logInfo(s"Output row count: ${count}")

        case null | "" | "COLLECT" =>
          val outputrows = outputrdd.take(100)

          logInfo("--- OUTPUT ROWS ---")
          outputrows.foreach(e => logInfo(s"${e.asInstanceOf[DataRow].toStringAlt}"))

        case _ =>
          import org.apache.spark.TaskContext

          logInfo(s"Deleting $viewOutput")

          if (deleteFile(viewOutput))
            logInfo(s"Output directory deleted: $viewOutput")
          else
            logInfo(s"Output directory could not be deleted, it is likely non-existent: $viewOutput")

          logInfo(s"inputrdd has ${inputrdd.partitions.length} partitions")
          logInfo(s"outputrdd has ${outputrdd.partitions.length} partitions")

          outputrdd.map { row =>
            val row2 = row.asInstanceOf[DataRow]
            val pid = TaskContext.getPartitionId()
            val partno = "part-%05d".format(pid)

            val viewName = if (row2.outputTag < 0) {
              "EXTRACT-" + row2.outputTag.abs
            } else {
              view2NameMapHandle_.value(row2.outputTag)
            }

            (viewName + "/" + partno + ".txt", row2)
          }.saveAsHadoopFile(viewOutput, classOf[String], classOf[DataRow], classOf[RDDMultipleTextOutputFormat])
          // codec = classOf[GzipCodec])
          logInfo(s"Output rows saved under $viewOutput")
      }
  }

  /**
   * Utility methods to grab typed values from the `configMap`
   */
  def getConfigAsBoolean(key: String)(implicit metadata: Metadata): Boolean = {
    metadata.configMap.getOrElse(key, "").toUpperCase match {
      case "Y" | "YES" | "T" | "TRUE" | "1" => true
      case _ => false
    }
  }

  def getConfigAsString(key: String)(implicit metadata: Metadata): String = metadata.configMap.getOrElse(key, null)

  def getConfigAsInt(key: String)(implicit metadata: Metadata): Int = metadata.configMap.getOrElse(key, "0").toInt

}

