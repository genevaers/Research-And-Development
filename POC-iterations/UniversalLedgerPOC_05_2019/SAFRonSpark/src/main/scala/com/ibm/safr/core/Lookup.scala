/*
 * Lookup.scala: Support lookups
 *
 * SAFR on Spark (Prototype)
 * (c) Copyright IBM Corporation. 2015
 * SPDX-License-Identifier: Apache-2.0
 * By Adrash Pannu
 *
 */
package com.ibm.safr.core

import java.text.SimpleDateFormat
import java.util.Date
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext

import org.apache.spark.rdd.RDD
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.{Map => MMap}

import com.ibm.safr.core.SAFR._


// How lookups work:
//
// CREATE LOOKUP syntax:
// (Effective_Date_Start_Only,Lookup Effective_Date_Start_Only
//    steps: List(LookupStep sourceLR: CUSTOMER_LR
//    targetLR: Effective_Date_Start_Only_LR
//    targetLF: Effective_Date_Ref_LF
//   keyFields: List(Field(65,CUSTOMER_ID,DataType(CHAR,10,),0)))
//  )
//
// CREATE LOOKUP Effective_Date_Start_Only (
//  CUSTOMER_LR(CUSTOMER_ID) REFERENCES Effective_Date_Start_Only_LR.Effective_Date_Ref_LF
// )

//
// Lookup logic text syntax:
// COLUMN = {Effective_Date_Start_Only.Customer_Name,{CUSTOMER_DOB_CCYYMMDD}}
//

// Lookups can be loosely thought of as a function:
//   LOOKUP(Parameter1, Parameter2) -> Row

case class LookupDDL(id: Int,
                     name: String,
                     steps: Seq[LookupStepDDL]) {
  val compiledDDL: Seq[Seq[(Int, Int)]] = {
    // Encode lookup steps. For example, consider the following lookup DDL:
    // CREATE LOOKUP SALES_TO_PRODCATEGORY (
    //   SALES_LR.PRODCODE REFERENCES PRODUCT_LF.PRODUCT_LR,
    //   (PRODUCT_LR.PRODCATCODE, SALES_LR.LOCATIONCODE) REFERENCES PRODCATEGORY_LF.PRODCATEGORY_LR
    // )
    // Each step is encoded as a list of tuples: (lr-index, key-field-ordinal)
    // Each lookup is a concatenation of the aforementioned steps.
    // This DDL is compiled as follows:
    //    Lookup SALES_TO_PRODCATEGORY, Encoded step 0: List((0,2))
    //    Lookup SALES_TO_PRODCATEGORY, Encoded step 1: List((1,2), (0,1))
    //    Lookup SALES_TO_PRODCATEGORY, Encoded table: List(List((0,2)), List((1,2), (0,1)))
    // At runtime, an Array `a` of DataRow objects is created. This array has a length of the encoded table list above.
    // `a`(0) will have the primordial DataRow. a(1) is populated after the 1st step, and so on.

    val lrmap = MMap[LogicalRecord, Int]()

    val encodedLookupTable = for ((step, idx) <- steps.zipWithIndex) yield {
      // Ensure that target LRs in each step have key fields.
      if (step.targetLR.indexFields.isEmpty)
        logFatal(s"Lookup $name (step ${idx + 1}): Logical Record ${step.targetLR.name} has no key fields.")

      // Ensure that keyfields match what the LR expects
      if (step.sourceKeyFields.length != step.targetLR.indexFields.length)
        logFatal(s"Lookup $name (step ${idx + 1}): Logical Record ${step.targetLR.name} expects ${step.targetLR.indexFields.length} key fields " +
          s"but ${step.sourceKeyFields.length} specified.")

      step.sourceKeyFields.zip(step.targetLR.indexFields).foreach {
        case ((srclf, srcfld), targetfld) => if (!srcfld.datatype.equals(targetfld.datatype))
          logFatal(s"Lookup $name (step ${idx + 1}): Source key field ${srclf.name}.${srcfld.name}(${srcfld.datatype.toSQLString}) " +
            s"incompatible with target field ${step.targetLR.name}.${targetfld.name}(${targetfld.datatype.toSQLString}).")
      }

      // Add the first LR at index 0
      if (idx == 0)
        lrmap += steps.head.sourceKeyFields.head._1 -> 0

      val encodedStep: Seq[(Int, Int)] = for (keyfield <- step.sourceKeyFields) yield {
        val (lr, field) = keyfield
        if (!lrmap.contains(lr))
          logFatal(s"Lookup $name, step $idx: unresolved reference ${lr.name}.${field.name}")

        lrmap(keyfield._1) -> field.ordinal
      }
      lrmap += step.targetLR -> (idx + 1)
      logDebug(s"Lookup $name, Encoded step $idx: $encodedStep")
      encodedStep
    }
    logDebug(s"Lookup $name, Encoded table: $encodedLookupTable")
    encodedLookupTable
  }
}

case class LookupStepDDL(sourceKeyFields: Seq[(LogicalRecord, LRField)],
                         targetLR: LogicalRecord,
                         targetLF: LogicalFile) {
  override def toString = {
    s"""LookupStep
       |sourceKey: $sourceKeyFields
        |targetLR:  ${targetLR.name}
        |targetLF:  ${targetLF.name}\n""".stripMargin
  }

  def toSQLString: String = {
    s"""(${sourceKeyFields.map(_.toString).mkString(", ")}) REFERENCES ${targetLF.name}.${targetLR.name}""".stripMargin
  }
}

case class UnresolvedLookupDDL(
                                id: Int,
                                name: String,
                                steps: Seq[UnresolvedLookupStepDDL]
                                ) extends NamedExpr {
  def resolve(metadata: Metadata): LookupDDL = {
    val newsteps: Seq[LookupStepDDL] = steps.map(_.resolve(metadata))
    LookupDDL(id, name, newsteps)
  }

  override def toString =
    s"""Lookup $name\n  steps: $steps\n"""

  override def toSQLString: String = {
    s"""CREATE LOOKUP $name (
                             | ${steps.map(_.toSQLString).mkString(",\n    ")}
        |)
     """.stripMargin
  }
}

case class UnresolvedLookupStepDDL(sourceKey: Seq[(String, String)], // LR, field
                                   targetLFName: String,
                                   targetLRName: String) {

  def resolve(metadata: Metadata): LookupStepDDL = {
    val sourceKeyFields = sourceKey.map { case (lfname, keyname) =>
      val lf = metadata.logicalRecordsByName(lfname)
      lf -> lf(keyname)
    }
    val targetLR = metadata.logicalRecordsByName(targetLRName)
    val targetLF = metadata.logicalFilesByName(targetLFName)

    LookupStepDDL(sourceKeyFields, targetLR, targetLF)
  }

  override def toString = {
    s"""LookupStep
       |sourceKey: $sourceKey
        |targetLR:  $targetLRName
        |targetLF:  $targetLFName\n""".stripMargin

  }

  def toSQLString: String = {
    s"""(${sourceKey.mkString(", ")}) REFERENCES $targetLFName.$targetLRName""".stripMargin

  }
}

case class LookupKey(cols: Seq[DataValue]) {
  override def toString = {
    "(" + cols.mkString(", ") + ")"
  }
}

// cols needs to be a list for hashCode/equals stability

case class LookupValue(row: DataRow, startEffectiveDate: Date, endEffectiveDate: Date) {
  def <(that: LookupValue): Boolean = {
    if (startEffectiveDate != null && that.startEffectiveDate != null)
      startEffectiveDate.before(that.startEffectiveDate)
    else
      false
  }

  def >(that: LookupValue): Boolean = !(this < that)

  override def toString = {
    s"LookupValue($startEffectiveDate - $endEffectiveDate: $row)"
  }
}

object LookupEngine {

  def lookupStepName(lookupName: String, stepIdx: Int) = {
    // Construct a unique lookup name for each step. To make sure the name won't conflict with a legitimate
    // name, let's embed some non-alphanumeric characters.
    lookupName + (if (stepIdx == 0) "" else s"#_$stepIdx")
  }

  def buildUberLookupMap(sqlContext: SQLContext,
                         metadata: Metadata,
                         configMap: Map[String, String]): Map[String, Map[LookupKey, Array[LookupValue]]] = {

    val uberLookupMap: Map[String, Map[LookupKey, Array[LookupValue]]] =
      (for (lookup <- metadata.lookups.values;
            (step, idx) <- lookup.steps.zipWithIndex) yield {

        val name = lookupStepName(lookup.name, idx)

        // Load file into memory
        val lf = step.targetLF
        val lr = step.targetLR

        val rdd: RDD[DataRow] = lf.openRDD(sqlContext, Option(lr))
        val rows: Array[DataRow] = rdd.collect()

        // Build Lookup Tables:
        logDebug(s"---------- BUILD LOOKUP HASHMAP: ${name}")
        rows.foreach(logDebug(_))

        val map = rows.map(row => {
          // Build LookupValue for each row
          val startDate: Date = lr.startEffDate.map(efd => row.getValue(efd.ordinal)).orNull.asInstanceOf[Date]
          val stopDate: Date = lr.stopEffDate.map(efd => row.getValue(efd.ordinal)).orNull.asInstanceOf[Date]
          LookupValue(row, startDate, stopDate)
        }).groupBy(lv => {
          // Make groups based on keys (exclude any effective dates)
          val row = lv.row
          val key = lr.indexFields.map(f => row.getValue(f.ordinal))
          LookupKey(key)
        })

        val sortedMap = if (lr.isDated) {
          map.mapValues(v => {
            // Within each group, sort rows by effective date in descending order
            val sorted = v.sortWith((a, b) => a > b)

            // Fill in `endEffectiveDate`
            var prev = sorted.head
            val res = for (elem <- sorted) yield {
              val lv = if (elem == sorted.head)
                LookupValue(elem.row, elem.startEffectiveDate, new Date(Long.MaxValue))
              else {
                val prevtime = prev.startEffectiveDate.getTime
                val newprevtime = prevtime - (24 * 60 * 60 * 1000) // +1 day, expressed in milliseconds
                val endEffectiveDate = new Date(newprevtime)
                LookupValue(elem.row, elem.startEffectiveDate, endEffectiveDate) // TODO: Add -1 to end date
              }

              prev = elem
              lv
            }
            res
          })
        } else {
          map
        }
        (name, sortedMap)
      }).toMap

    // Print lookup table
    for ((k, v) <- uberLookupMap) {
      logDebug(s"Lookup table: ${k}")

      for ((k2, v2) <- v)
        logDebug(s"$k2 -> ${v2.toSeq}")
    }

    uberLookupMap
  }

  /**
   * doLookup: Perform (effective-dated) lookups
   */
  def doLookup(uberLookupHandle: Any, lookupName: String, inrow: DataRow, lookupDateOpt: Option[Date], compiledDDL: Seq[Seq[(Int, Int)]]): DataRow = {

    val uberLookupMap =
      uberLookupHandle.asInstanceOf[org.apache.spark.broadcast.Broadcast[UberLookupMapType]].
        value.
        asInstanceOf[UberLookupMapType]

    // First pick the exact reference table we're interested in. This needs to be optimized
    // so we avoid this call, and merely reference into an array of maps using a pre-computed id.
    // Then, do the actual lookup into the lookup map. This may select a group of rows in case
    // we're doing effective dated lookups. We'll need to then pick one.

    val rows = new Array[DataRow](compiledDDL.length + 1)
    rows(0) = inrow

    // E.g.: Lookup SALES_TO_PRODCATEGORY, Encoded table: List(List((0,2)), List((1,2), (0,1)))
    for ((step, idx) <- compiledDDL.zipWithIndex if rows(idx) != null) {

      val name = lookupStepName(lookupName, idx)

      // Make the key for this step
      val key = LookupKey(step.map {
        case (rowidx: Int, fldidx: Int) => rows(rowidx).getValue(fldidx)
      })

      val targetValue: Array[LookupValue] = uberLookupMap(name).getOrElse(key, null)

      val targetRow: DataRow = if (targetValue == null)
        null
      else {
        if (lookupDateOpt.isEmpty)
          targetValue.head.row
        else {
          // Perform effective dated lookup
          val lookupDate = lookupDateOpt.get
          val matches = targetValue.filter(lookupValue => {
            lookupValue.startEffectiveDate.before(lookupDate) &&
              lookupValue.endEffectiveDate.after(lookupDate)
          })
          if (matches.length > 1) throw new Exception("More than one match found") // TODO: Ensure this isn't possible!
          else if (matches.isEmpty) null
          else matches.head.row
        }
      }
      rows(idx + 1) = targetRow
    }
    rows.last
  }

}
