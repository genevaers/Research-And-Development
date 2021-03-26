/**
 * Runtime.scala: Main runtime engine
 *
 * SAFR on Spark (Prototype)
 * (c) Copyright IBM Corporation. 2015
 * SPDX-License-Identifier: Apache-2.0
 * By Adrash Pannu
 *
 */

package com.ibm.safr.core

import java.util.Date
import scala.collection.mutable.{Map => MMap}
import scala.collection.mutable.{ArrayBuffer, Queue}
import com.ibm.safr.core.SAFR._
import com.ibm.safr.core.LookupEngine._

class ExprBlock(val numColumns: Int, val exprList: Seq[Expr]) extends java.io.Serializable {
  override def toString = {
    exprList.map(e => e.toString).mkString("\n")
  }

  def toString(indentation: Int) = {
    exprList.map(e => e.toString).mkString((" " * indentation) + "\n")
  }
}

/**
 * ExprRunner - Run compiled expressions on a single row
 */
class ExprRunner(val uberLookupHandle: Any,
                 inrows: Queue[DataRow],
                 outrows: Queue[DataRow],
                 previousRowMap: MMap[Int, DataRow]) {
  /**
   * evalExpr - Run a single expression graph. Return `true` if a write to PIPE takes place.
   */
  def evalExpr(expr: Expr, inrow: DataRow, outrow: DataRow): DataValue = {

    val retval = expr match {

      case StringLitExpr(s) => s

      case IntegerLitExpr(i) => i

      case FieldExpr(_, ordinal, _) =>
        inrow.getValue(ordinal)

      case PreviousFieldExpr(_, ordinal, _) =>
        val prevRow = previousRowMap.getOrElse(inrow.inputTag, null)
        if (prevRow != null)
          prevRow.getValue(ordinal)
        else
          null

      case AssignExpr(lhs: Int, rhs: Expr) =>
        val rhsValue = rhs match {
          case fe: FieldExpr =>
            inrow.getValue(fe.ordinal)
          case _ => evalExpr(rhs, inrow, outrow)
        }
        outrow.setValue(lhs, rhsValue)

      case re: RelExpr =>
        evalRelExpr(re, inrow, outrow)

      case we: WriteExpr =>
        // Clone `outrow`
        val outrow2 = new DataRowMutable(we.exprNum + 1, we.lfid, -we.extractId)

        (0 to we.exprNum).foreach { idx => outrow2.setValue(idx, outrow(idx)) }

        if (we.isPipe) {
          inrows += outrow2
          // println(s"WRITE to pipe (${outrow2.inputTag}, ${outrow2.outputTag}): $outrow2")
        } else {
          outrows += outrow2
          // println(s"WRITE to out: $outrow2")
        }

      case IfThenElseExpr(condExpr, thenExpr, elseExpr) =>
        val condValue = evalExpr(condExpr, inrow, outrow).asInstanceOf[Boolean]
        if (condValue)
          evalExpr(thenExpr, inrow, outrow)
        else if (elseExpr.isDefined)
          evalExpr(elseExpr.get, inrow, outrow)

      case LookupExpr(lookupName, startEffDateIdx, compiledDDL, targetFieldIdx, _) =>
        val startEffDate: Option[Date] = startEffDateIdx.map(idx => inrow.getValue(idx).asInstanceOf[Date])
        val targetRow = doLookup(uberLookupHandle, lookupName, inrow, startEffDate, compiledDDL)
        if (targetRow != null)
          targetRow.getValue(targetFieldIdx)
        else
          null

      case fe: MatchFuncExpr =>
        evalMatchFuncExpr(fe, inrow, outrow)

      case IsSpacesFuncExpr(lhs) =>
        val lhsValue = evalExpr(lhs, inrow, outrow).asInstanceOf[String]
        lhsValue != null && lhsValue == " " * lhsValue.length

      case ae: ArithExpr =>
        evalArithExpr(ae, inrow, outrow)

      case OrExpr(lhs, rhs) =>
        val lhsValue = evalExpr(lhs, inrow, outrow)
        if (lhsValue.asInstanceOf[Boolean]) {
          // Short-circuit evaluation of RHS
          true
        } else {
          val rhsValue = evalExpr(rhs, inrow, outrow)
          rhsValue.asInstanceOf[Boolean]
        }

      case AndExpr(lhs, rhs) =>
        val lhsValue = evalExpr(lhs, inrow, outrow)
        if (!lhsValue.asInstanceOf[Boolean]) {
          // Short-circuit evaluation of RHS
          false
        } else {
          val rhsValue = evalExpr(rhs, inrow, outrow)
          rhsValue.asInstanceOf[Boolean]
        }

      case NotExpr(lhs) =>
        val lhsValue = evalExpr(lhs, inrow, outrow)
        !lhsValue.asInstanceOf[Boolean]

    }
    retval
  }

  /**
   * evalRelExpr - Run a relational expression (<, >, etc.)
   */
  def evalRelExpr(expr: RelExpr, inrow: DataRow, outrow: DataRow): DataValue = {
    val (lhs, relop, rhs) = (expr.lhs, expr.relop, expr.rhs)

    val lhsValue = evalExpr(lhs, inrow, outrow)
    val rhsValue = evalExpr(rhs, inrow, outrow)

    val cond = (lhsValue, relop, rhsValue) match {
      case (null, _, _) => relop == "!="
      case (_, _, null) => relop == "!="
      case (l: String, "<", r: String) => l < r
      case (l: String, "<=", r: String) => l <= r
      case (l: String, "=", r: String) => l == r  //TODO: Not sure why this test is failing on upgrade from Spark 1.6 to Spark 2.4.  See SimpleTest1.SQL and change value to retest
      case (l: String, ">", r: String) => l > r
      case (l: String, ">=", r: String) => l >= r
      case (l: String, "!=", r: String) => l != r

      case (l: Int, "<", r: Int) => l < r
      case (l: Int, "<=", r: Int) => l <= r
      case (l: Int, "=", r: Int) => l == r
      case (l: Int, ">", r: Int) => l > r
      case (l: Int, ">=", r: Int) => l >= r
      case (l: Int, "!=", r: Int) => l != r

      case _ =>
        logFatal(s"Unsupported relop. $lhsValue $relop $rhsValue")
        false
    }
    cond
  }

  /**
   * evalArithExpr - Run an arithmetic expression (+, -, etc.)
   */
  def evalArithExpr(expr: ArithExpr, inrow: DataRow, outrow: DataRow): DataValue = {
    val (lhs, arithop, rhs) = (expr.lhs, expr.arithop, expr.rhs)

    val lhsValue = evalExpr(lhs, inrow, outrow)
    val rhsValue = evalExpr(rhs, inrow, outrow)

    val result = arithop match {
      case "+" if lhsValue.isInstanceOf[Int] => lhsValue.asInstanceOf[Int] + rhsValue.asInstanceOf[Int]
      case "-" if lhsValue.isInstanceOf[Int] => lhsValue.asInstanceOf[Int] - rhsValue.asInstanceOf[Int]
      case _ => throw new Exception(s"Unsupported arithop: ${arithop}")
    }
    result
  }

  /**
   * evalFuncExpr - Run a function expression
   */
  def evalMatchFuncExpr(fe: MatchFuncExpr, inrow: DataRow, outrow: DataRow): DataValue = {
    val key = evalExpr(fe.args.head, inrow, outrow)
    fe.hashMap.getOrElse(key, fe.hashMapNotFound)
  }
}
