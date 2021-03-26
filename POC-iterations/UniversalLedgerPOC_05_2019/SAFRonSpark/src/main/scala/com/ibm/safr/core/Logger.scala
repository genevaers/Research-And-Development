/**
 * Logger.scala: Support logging functions
 *
 * SAFR on Spark (Prototype)
 * (c) Copyright IBM Corporation. 2015
 * SPDX-License-Identifier: Apache-2.0
 * By Adrash Pannu
 *
 */
package com.ibm.safr.core

import org.apache.log4j._
import org.apache.log4j.Level._
import scala.collection.immutable.Map
import org.apache.log4j.{Logger => Log4jLogger}

trait Logger {
  private[this] val logger = Logger.getLogger(getClass().getName());

  import org.apache.log4j.Level._

  def logDebug(message: Any) = if (logger.isEnabledFor(DEBUG)) {
    message.toString.split('\n').foreach(logger.debug(_))
  }

  def logInfo(message: Any) = if (logger.isEnabledFor(INFO)) {
    message.toString.split('\n').foreach(logger.info(_))
  }

  def logWarn(message: Any) = if (logger.isEnabledFor(WARN)) {
    message.toString.split('\n').foreach(logger.warn(_))
  }

  def logError(message: Any) = if (logger.isEnabledFor(ERROR)) {
    message.toString.split('\n').foreach(logger.error(_))
  }

  def logFatal(message: Any) = if (logger.isEnabledFor(FATAL)) {
    message.toString.split('\n').foreach(logger.fatal(_))
    throw new Exception(message.toString)
  }
}
