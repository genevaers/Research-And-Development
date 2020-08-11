/*
 * Metadata.scala
 *
 * SAFR on Spark (Prototype)
 * (c) Copyright IBM Corporation. 2015
 * SPDX-License-Identifier: Apache-2.0
 * By Adrash Pannu
 */

package com.ibm.safr.core

import com.ibm.safr.core.Compiler._
import com.ibm.safr.core.SAFR._
import scala.language.postfixOps
import org.apache.spark.sql.types.{StructType, StructField}
import org.apache.spark.sql.SQLContext


class Metadata(sqlstring: String, sqlContext: SQLContext) extends Logger {

  val ltp = new Parser
  val result = ltp.parseAll(ltp.sql, sqlstring)

  val rawParseTree: List[Expr] = result match {
    case ltp.Success(x, _) => result.get.asInstanceOf[List[Expr]]
    case ltp.NoSuccess(err, next) =>
      throw new Exception("Parse failure " +
        "(line " + next.pos.line + ", column " + next.pos.column + s"):\n" +
        err + "\n" +
        next.pos.longString)
  }

  // Check for duplicate object definitions
  val names = rawParseTree.filter(e => e.isInstanceOf[NamedExpr]).map(_.asInstanceOf[NamedExpr].name)
  val dups = names.groupBy(e => e).mapValues(_.length).filter(_._2 > 1)
  if (dups.nonEmpty) {
    logFatal(s"Duplicate object definition(s): " + dups.keys.mkString(", "))
  }

  val logicalFiles: Map[Int, LogicalFile] = rawParseTree.filter(e => e.isInstanceOf[LogicalFile]).
    map(_.asInstanceOf[LogicalFile]).map(lf => (lf.id, lf)).toMap

  // Turn the tables :-) Extract LR information from tables that have LRs defined on them.
  val tableLRs: Seq[LogicalRecord] = for (t <- logicalFiles.values.toSeq if t.lrName.isDefined) yield {
    val df = sqlContext.read.format(t.driver).options(t.options).load()
    val fields = for ((col, idx) <- df.schema.fields.zipWithIndex) yield {
      LRField(nextId, idx, col.name, new com.ibm.safr.core.DataType(sqltosafrtypes(col.dataType.toString), -1, ""))
    }
    LogicalRecord(nextId, t.lrName.get, fields, Seq(), None, None)
  }
  val pickledParseTree = rawParseTree ++ tableLRs


  val mergeFiles: Map[Int, MergeFile] = pickledParseTree.filter(e => e.isInstanceOf[MergeFile]).
    map(_.asInstanceOf[MergeFile]).map(lf => (lf.id, lf)).toMap

  val logicalFilesByName: Map[String, LogicalFile] = pickledParseTree.filter(e => e.isInstanceOf[LogicalFile]).
    map(_.asInstanceOf[LogicalFile]).map(lf => (lf.name, lf)).toMap

  val logicalRecords: Map[Int, LogicalRecord] = pickledParseTree.filter(e => e.isInstanceOf[LogicalRecord]).
    map(_.asInstanceOf[LogicalRecord]).map(lf => (lf.id, lf)).toMap

  val logicalRecordsByName: Map[String, LogicalRecord] = pickledParseTree.filter(e => e.isInstanceOf[LogicalRecord]).
    map(_.asInstanceOf[LogicalRecord]).map(lf => (lf.name, lf)).toMap

  val unresolvedLookups: Map[String, UnresolvedLookupDDL] = pickledParseTree.filter(e => e.isInstanceOf[UnresolvedLookupDDL]).
    map(_.asInstanceOf[UnresolvedLookupDDL]).map(lf => (lf.name, lf)).toMap

  val unresolvedViews: Seq[View] = pickledParseTree.filter(e => e.isInstanceOf[View]).map(_.asInstanceOf[View])

  val configMap: Map[String, String] = {
    pickledParseTree.filter(e => e.isInstanceOf[SetOptionExpr]).
      map(_.asInstanceOf[SetOptionExpr]).map(opt => (opt.key, opt.value)).toMap
  }

  logicalFiles.foreach(e => logDebug(e))

  logicalRecords.foreach(e => logDebug(e))

  unresolvedLookups.foreach(e => logDebug(e))

  unresolvedViews.foreach(e => logDebug(e))

  def getLogicalFile(name: String) = logicalFiles.values.find(e => e.name == name)

  // Compile lookups
  val lookups: Map[String, LookupDDL] = compileLookups(this)

  // Compile external files
  compileMergeFiles(this)

  // Compile views
  val views: Seq[View] = compileViews(this)

  views.foreach(e => logDebug(e))

}

