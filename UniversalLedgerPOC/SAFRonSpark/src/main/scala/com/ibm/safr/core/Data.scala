/**
 * Datatype.scala
 *
 * SAFR on Spark (Prototype)
 * (c) Copyright IBM Corporation. 2015
 * SPDX-License-Identifier: Apache-2.0
 * By Adrash Pannu
 *
 */

package com.ibm.safr.core

import scala.language.postfixOps
import com.ibm.safr.core.SAFR._

// Data types
object DataTypeTag extends Enumeration {
  type DataTypeTag = Value
  val VOID, CHAR, INT, BOOL, DOUBLE, DATE = Value
}

import com.ibm.safr.core.DataTypeTag._

case class DataType(
                     val datatype: DataTypeTag,
                     val length0: Int,
                     val dateFormat: String
                     ) {

  val length =
    if (datatype == CHAR) length0
    else if (length0 > 0) length0
    else datatypeDefaultLength(datatype)

  def this(e: scala.xml.Node) {
    this(datatypeStringToEnum(e \ "DataType" text),
      (e \ "Length" text).toInt,
      e \ "DateFormat" text)
  }

  def commonSupertype(that: DataType): DataType = {
    assert(equals(that))

    if (strictEquals(that)) this
    else new DataType(datatype, length.max(that.length), dateFormat)
  }

  def equals(that: DataType): Boolean = {
    datatype == that.datatype
    // dateFormat == that.dateFormat
    // length == that.length &&
  }

  def strictEquals(that: DataType): Boolean = {
    datatype == that.datatype
    length == that.length &&
      dateFormat == that.dateFormat
  }

  def toSQLString: String = {
    s"""${datatypeEnumtoString(datatype)}"""
  }
}

/**
 * DataRow
 */
abstract class DataRow(val inputTag: Int = -1, val outputTag: Int = -1) {
  def equals(that: Any): Boolean
  def apply(ordinal: Int): DataValue

  def getValue(ordinal: FieldIndex): DataValue
  def setValue(ordinal: Int, dv: DataValue): Unit
  def numColumns: Int
  def toStringAlt = s"(inputTag=$inputTag, outputTag=$outputTag): $toString"
}

/**
 * DataRowImmutable
 */
class DataRowImmutable(val sqlrow: org.apache.spark.sql.Row, inputTag: Int = -1, outputTag: Int = -1)
  extends DataRow(inputTag, outputTag) {

  override def apply(ordinal: Int): DataValue = sqlrow(ordinal)
  override def numColumns = sqlrow.length

  override def equals(that: Any): Boolean = {
    that match {
      case other: DataRowImmutable =>
        sqlrow.equals(other.sqlrow)
      case _ => false
    }
  }
  override def hashCode = sqlrow.hashCode()

  def getValue(ordinal: FieldIndex): DataValue = sqlrow(ordinal)

  def setValue(ordinal: Int, dv: DataValue): Unit = throw new Exception("Cannot alter DataRowImmutable.")

  override def toString: String = sqlrow.toString()
}

/**
 * DataRowMutable
 */
class DataRowMutable(length: Int, inputTag: Int = -1, outputTag: Int = -1)
  extends DataRow(inputTag, outputTag) {

  val columns: Array[DataValue] = new Array(length)

  override def numColumns = length

  override def equals(that: Any): Boolean = {
    that match {
      case other: DataRowMutable =>
        columns.sameElements(other.columns)
      case _ => false
    }
  }

  override def hashCode = columns.hashCode()

  override def apply(ordinal: Int): DataValue = columns(ordinal)

  def getValue(ordinal: FieldIndex): DataValue = columns(ordinal)

  def setValue(ordinal: Int, dv: DataValue): Unit = columns(ordinal) = dv

  override def toString: String = {
    columns.map(e => if (e == null) "?" else e.toString).mkString(",")
  }
}
