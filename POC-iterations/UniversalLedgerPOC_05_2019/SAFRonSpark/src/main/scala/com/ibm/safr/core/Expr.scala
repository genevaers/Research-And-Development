/**
 * Expressions
 *
 * SAFR on Spark (Prototype)
 * (c) 2015 IBM All Rights Reserved
 */

package com.ibm.safr.core

import com.ibm.safr.core.DataTypeTag._
import com.ibm.safr.core.SAFR._

abstract class Expr extends java.io.Serializable {
  def datatype: DataType = throw new Exception(s"${getClass.getName}: Missing implementation of datatype().")
  def toSQLString: String = toString
}

abstract class NamedExpr extends Expr {
  def id: Int

  def name: String
}

abstract class Literal extends Expr {
  def value: DataValue
}

case class IntegerLitExpr(i: Int) extends Literal {
  override val datatype = new DataType(INT, 0, "")

  override def value: DataValue = i

  override def toString = i.toString
}

case class StringLitExpr(s: String) extends Literal {
  override val datatype = new DataType(CHAR, s.length, "")

  override def value: DataValue = s

  override def toString = s.toString
}

case class ColumnReference(index: Option[Int])
  extends Expr

case class UnresolvedAssignExpr(lhs: Option[Int], rhs: Expr) extends Expr {
  override def datatype = rhs.datatype
  override def toSQLString = if (lhs.isDefined)
    s"COL.${lhs.get + 1} = ${rhs.toSQLString}"
  else
    s"COLUMN = ${rhs.toSQLString}"
}

case class AssignExpr(lhs: Int, rhs: Expr) extends Expr {
  override def datatype = rhs.datatype
  override def toSQLString = s"COL.${lhs + 1} = ${rhs.toSQLString}"
}

case class ArithExpr(lhs: Expr, arithop: String, rhs: Expr) extends Expr {
  override def datatype = lhs.datatype
}

case class UnresolvedFieldExpr(name: String) extends Expr

case class FieldExpr(name: String, ordinal: Int, override val datatype: DataType) extends Expr

case class UnresolvedLookupExpr(lookupName: String) extends Expr

case class LookupExpr(lookupName: String,
                      startEffDateIdx: Option[Int],
                      compiledDDL: Seq[Seq[(Int, Int)]],
                      valueIdx: Int,
                      override val datatype: DataType) extends Expr

case class IfThenElseExpr(condExpr: Expr, thenExpr: Expr, elseExpr: Option[Expr]) extends Expr {
  override def datatype: DataType = {
    if (elseExpr.isDefined) {
      thenExpr.datatype.commonSupertype(elseExpr.get.datatype)
    } else {
      thenExpr.datatype
    }
  }
}

case class RelExpr(lhs: Expr, relop: String, rhs: Expr) extends Expr {
  override def datatype = new DataType(BOOL, 0, "")
}

case class LogicExpr(lhs: Expr, logop: String, rhs: Expr) extends Expr {
  override def datatype = new DataType(BOOL, 1, "")
}

case class OrExpr(lhs: Expr, rhs: Expr) extends Expr {
  override def datatype = new DataType(BOOL, 1, "")
}

case class AndExpr(lhs: Expr, rhs: Expr) extends Expr {
  override def datatype = new DataType(BOOL, 1, "")
}

case class NotExpr(lhs: Expr) extends Expr {
  override def datatype = new DataType(BOOL, 1, "")
}

case class UnresolvedFuncExpr(funcName: String, args: Seq[Expr]) extends Expr

case class MatchFuncExpr(args: Seq[Expr],
                         hashMap: Map[Any, Any],
                         hashMapNotFound: Any,
                         override val datatype: DataType) extends Expr

case class IsSpacesFuncExpr(arg: Expr) extends Expr {
  override def datatype = new DataType(BOOL, 1, "")
}

case class PreviousFieldExpr(name: String, ordinal: Int, override val datatype: DataType) extends Expr

case class WriteSourceExpr(sourceType: String)

case class WriteDestFileExpr(destFile: String)

case class WriteDestExtractExpr(extractId: Int)

case class WriteExitExpr(name: String)

case class UnresolvedWriteExpr(source: String, // ""INPUT" | "DATA" | "VIEW"
                               destType: String, // "FILE" | "EXTRACT"
                               destFile: String,
                               extractId: Int,
                               exit: String) extends Expr {
  override def datatype = new DataType(VOID, 0, "")
}

case class WriteExpr(lfid: Int,
                     extractId: Int,
                     exprNum: Int,
                     isPipe: Boolean) extends Expr {
  override def datatype = new DataType(VOID, 0, "")
}


case class SetOptionExpr(key: String, value: String) extends Expr

class MergeFileInput(val lfName: String, val lrName: String, val keys: Seq[String]) extends Expr {
  override def toString = {
    s"""MergeFileInput(lfName = $lfName, lrName = $lrName, keys = $keys)"""
  }
}

case class StarExpr() extends Expr

case class MergeFileInputResolved(mfinput: MergeFileInput, metadata: Metadata)
  extends MergeFileInput(mfinput.lfName, mfinput.lrName, mfinput.keys) {

  val lf: LogicalFile = metadata.logicalFilesByName(lfName)
  val lr: LogicalRecord = metadata.logicalRecordsByName(lrName)

  // compiledKeys: Sequence of expressions to extract key values out of a record
  val compiledKeys: Seq[Expr] = keys.zipWithIndex.
    map { case (f, idx) =>
    val expr = UnresolvedAssignExpr(Option(idx), UnresolvedFieldExpr(f))
    val coltypes = new Array[DataType](lr.fields.length)
    Compiler.compileExpr(metadata, lr, expr, 0, coltypes)
  }

  override def toString = {
    s"""MergeFileInputResolved(lfName = $lfName, lrName = $lrName, compiledKeys = $compiledKeys)"""
  }
}

case class MergeFile(id: Int, name: String, inputs: Seq[MergeFileInput], options: Map[String, String]) extends NamedExpr
