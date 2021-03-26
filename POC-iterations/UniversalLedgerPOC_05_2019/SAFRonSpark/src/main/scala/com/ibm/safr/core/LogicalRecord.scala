package com.ibm.safr.core

/**
 * LogicalRecord.scala: Support record structures
 *
 * SAFR on Spark (Prototype)
 * (c) Copyright IBM Corporation. 2015
 * SPDX-License-Identifier: Apache-2.0
 * By Adrash Pannu
 *
 */

import com.ibm.safr.core.SAFR._
import org.apache.spark.sql.types.{StructField, StructType}

// LogicalRecord
case class LogicalRecord(id: Int,
                         name: String,
                         fields: Seq[LRField],
                         indexFields: Seq[LRField],
                         startEffDate: Option[LRField],
                         stopEffDate: Option[LRField]) extends NamedExpr {

  val fieldMapByName = fields.map(f => (f.name, f)).toMap
  val fieldMapById = fields.map(f => (f.id, f)).toMap

  def getFieldIndex(id: Int): FieldIndex = fields.indexWhere(field => field.id == id)
  def getFieldIndex(name: String): FieldIndex = fields.indexWhere(field => field.name == name)
  def getFieldById(id: Int): LRField = fieldMapById(id)

  def apply(fieldName: String): LRField = {
    val retval = fieldMapByName.getOrElse(fieldName, null)
    if (retval == null)
      logFatal(s"Logical Record $name has no field named $fieldName.")
    retval
  }

  def apply(index: Int): LRField = fields(index)

  def isDated = startEffDate.isDefined || stopEffDate.isDefined

  override def toString: String =
    s"""LogicalRecord($id, $name)\n  ${fields.map(_.toString).mkString("\n  ")}\n  $indexFields\n"""

  override def toSQLString: String = {
    val keystr =
      if (indexFields.length > 0)
        "KEY (" + indexFields.map(_.toSQLString).mkString(", ") + ")"
      else
        ""

    s"""CREATE LOGICAL RECORD $name (\n  ${fields.map(_.toSQLString).mkString(",\n  ")}\n)\n$keystr\n """
  }

  def toSQLSchema = {
    StructType(fields.map(f => StructField(f.name, safrtosqltypes(f.datatype.datatype), false)))
  }
}

// Field
case class LRField(id: FieldIndex, ordinal: Int, name: String, datatype: DataType) {
  def toSQLString: String = s"""$name\t${datatype.toSQLString}"""
}

// IndexField
case class LRIndexField(
                         field: LRField,
                         isStartEffectiveDate: Boolean
                         ) {
  def toSQLString: String = {
    s"""${field.name}""" + (if (isStartEffectiveDate) """ AS START EFFECTIVE DATE""" else "")
  }
}



