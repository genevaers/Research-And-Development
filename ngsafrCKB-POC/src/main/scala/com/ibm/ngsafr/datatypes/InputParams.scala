package com.ibm.ngsafr.datatypes

import java.time.LocalDateTime

// ngsafr Common Key Buffer in Scala POC
// (c) Copyright IBM Corporation. 2018
// SPDX-License-Identifier: Apache-2.0
// Written by: Sibasis Das

case class InputParams(val entityName: String,
                       val partitionId: String,
                       var parallelReplicationCount: Int,
                       val fileName: String,
                       val keyPosition: Int,
                       val keyLength: Int,
                       val effectiveDatePosition: Int,
                       val effectiveDateLength: Int,
                       var currentIdValue: BigInt,
                       var recordNumber: BigInt,
                       var currentEffectiveDate: LocalDateTime) {


  override def toString: String = "{ ( " + entityName + " )" + " ( " + partitionId + " )" + " ( " +
    parallelReplicationCount + " ) " + " ( " + fileName + " ) " + " ( " + keyPosition + " ) " + " ( " + keyLength + " ) " +
    " ( " + effectiveDatePosition + " ) " + " ( " + effectiveDateLength + " ) " + " ( " + currentIdValue + " ) " +
    " ( " + recordNumber + " ) " + " ( " + currentEffectiveDate + " ) "
}
