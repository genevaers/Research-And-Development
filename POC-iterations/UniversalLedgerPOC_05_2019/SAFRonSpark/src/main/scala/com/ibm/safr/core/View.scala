/*
 * Query: Data structures to represent queries
 *
 * SAFR on Spark (Prototype)
 * (c) Copyright IBM Corporation. 2015
 * SPDX-License-Identifier: Apache-2.0
 * By Adrash Pannu
 *
 */
package com.ibm.safr.core

import scala.collection.mutable.ArrayBuffer
import com.ibm.safr.core.SAFR._

// QueryNode: Represents a logical node in a query graph
sealed trait QueryNode {
  def children: Seq[QueryNode]

  def scans(): Seq[ScanNode] = {
    val nodes = ArrayBuffer.empty[ScanNode]
    scans0(nodes)
    nodes
  }

  def scans0(nodes: ArrayBuffer[ScanNode]): Unit = {
    this match {
      case sc: ScanNode => nodes += sc
      case _ => for (node <- children) node.scans0(nodes)
    }
  }

  def toSQLString: String

  def toString(indentation: Int): String

  override def toString: String = toString(0)
}

case class UnionNode(children: Seq[QueryNode]) extends QueryNode {
  override def toSQLString =
    s"""${children.map(_.toSQLString).mkString("\n\nUNION\n\n")}"""

  def toString(indentation: Int): String = {
    val prefix = " " * indentation
    val sb = new StringBuilder()

    sb.append(prefix + "*UnionNode" + "\n")
    for (child <- children)
      sb.append(child.toString(indentation + 4))

    sb.toString()
  }
}

case class AggNode(child: QueryNode, groupingCols: Seq[Int]) extends QueryNode {
  val children = Seq(child)

  override def toSQLString = s"""AggNode"""

  def toString(indentation: Int): String = {
    val sb = new StringBuilder()

    sb.append(" " * indentation)

    val prefix1 = " " * indentation
    val prefix = " " * (indentation + 4)

    sb.append(prefix1 + "*AggNode" + "\n")
    sb.append(prefix + "groupingCols: {" + groupingCols.mkString(",") + "}")
    sb.append("\n")
    sb.append(children.map(child => child.toString(indentation + 4)).mkString(""))
    sb.append("\n")
    sb.toString
  }
}

case class ScanNode(id: Int,
                    lf: LogicalFile,
                    lr: LogicalRecord,
                    filterBlock: ExprBlock,
                    projBlock: ExprBlock,
                    projSchema: Seq[DataType]) extends QueryNode {
  val children = Seq()

  override def toSQLString = "ScanNode TODO"

  override def toString(indentation: Int): String = {
    indent(s"*ScanNode (LF = ${lf.name}, LR = ${lr.name})\n", indentation) +
      indent(s"filter:\n", indentation + 4) +
      indent(filterBlock + "\n", indentation + 8) +
      indent(s"projection (#${projBlock.numColumns}):\n", indentation + 4) +
      indent(projBlock + "\n", indentation + 8)
  }
}

// ColumnProjection
case class ColumnProjection(parseTree: Seq[Expr]) {
  override def toString: String = parseTree.map {expr => expr.toString}.mkString("\n") + "\n"
}

// Scan
case class UnresolvedScanNode(id: Int,
                              lfname: String,
                              lrname: String,
                              filter: Seq[Expr],
                              selectList: Seq[ColumnProjection]) extends QueryNode {
  val children = Seq()

  override def toString: String = toString(0)

  override def toString(indentation: Int): String = {
    indent(s"*UnresolvedScanNode (LF = ${lfname}, LR = ${lrname})\n", indentation) +
      indent(s"filter:\n", indentation + 4) +
      indent(filter + "\n", indentation + 8) +
      indent(s"projection (#${selectList.length}):\n", indentation + 4) +
      indent(selectList + "\n", indentation + 8)
  }

  def toSQLString: String = {
    val whereClause = if (filter != "") ("WHERE " + filter.toString) else ""
    val sqlstr =
      s"""SELECT ${selectList.map(_.toString).mkString("\n,     ")}
          |FROM ${lfname}.${lrname}
          |$whereClause
       """.stripMargin

    sqlstr
  }
}

// View
case class View(id: Int,
                name: String,
                queryNode: QueryNode,
                output0: Option[String] = None) extends NamedExpr {

  def output: String = if (output0.isDefined) output0.get else name

  override def toString: String =
    s"""View($id, $name)\n""" + queryNode.toString(4)

  override def toSQLString: String = {
    s"CREATE VIEW $name\n" +
      s"""${scans.map(_.toSQLString).mkString("\n\nUNION\n\n")}"""
  }

  def scans: Seq[ScanNode] = queryNode.scans
}

