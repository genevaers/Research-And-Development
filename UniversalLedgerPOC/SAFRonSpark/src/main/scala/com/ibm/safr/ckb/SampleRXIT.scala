package com.ibm.safr.ckb

/*
 * Sample Reaad Exit: present data to spark
 *
 * SAFR on Spark (Prototype)
 * (c) Copyright IBM Corporation. 2015
 * SPDX-License-Identifier: Apache-2.0
 * By Adrash Pannu
 *
 */

import com.ibm.safr.core.{Logger, DataRow}

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.WrappedArray

class SampleRXIT extends Logger {
  def run(ckb: Array[Iterable[DataRow]]): Array[Iterable[DataRow]] = {
    logInfo("SampleRXIT: doit()")

    ckb
    /*
    // Just for fun, delete all but the first row in each input
    val ckb2 = for (input <- ckb) yield {
      ArrayBuffer(input.head)
    }
    ckb2.asInstanceOf[Array[Iterable[DataRow]]]
    */

  }
}
