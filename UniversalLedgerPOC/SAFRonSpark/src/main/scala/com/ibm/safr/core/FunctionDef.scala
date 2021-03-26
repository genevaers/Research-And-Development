/**
 * Function.scala: Metadata for system- and user-defined functions
 *
 * SAFR on Spark (Prototype)
 * (c) Copyright IBM Corporation. 2015
 * SPDX-License-Identifier: Apache-2.0
 * By Adrash Pannu
 *
 */

package com.ibm.safr.core

import com.ibm.safr.core.DataTypeTag._

case class FunctionDef(name: String,
                       argumentTypes: Seq[DataTypeTag],
                       minArgs: Option[Int])


