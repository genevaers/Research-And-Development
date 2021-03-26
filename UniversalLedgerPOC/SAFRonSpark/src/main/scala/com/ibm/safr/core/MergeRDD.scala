/*
 * MergeRDD.scala - Support cogrouping of pre-sorted RDDs
 *
 * SAFR on Spark (Prototype)
 * (c) Copyright IBM Corporation. 2015
 * SPDX-License-Identifier: Apache-2.0
 * By Adrash Pannu
 */

package com.ibm.safr.core

import org.apache.spark.rdd.RDD
import org.apache.spark._
import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag
import java.io.{IOException, ObjectOutputStream}

case class MergeRDDFunctions[K: Ordering : ClassTag](rdd: RDD[(K, _)]) {
  def merge(rdds: Seq[RDD[(K, _)]]) = {
    new MergeRDD[K](rdds)
  }
}

class MergePartition(val idx: Int,
                     val parentParts: Array[Partition])
  extends Partition with Serializable {
  override val index: Int = idx

  override def hashCode(): Int = idx
}

class MergeRDD[K: Ordering : ClassTag](var rdds: Seq[RDD[_ <: Product2[K, _]]])
  extends RDD[(K, Array[Iterable[_]])](rdds.head.context, Nil) {

  val b = implicitly[Ordering[K]]

  // Ensure that each input has the same # of partitions
  val partitionCounts = rdds.map(e => e.partitions.length).toSet
  if (partitionCounts.size != 1) {
    throw new Exception("Merge inputs have differing partition counts: " +
      rdds.map(e => "%s (%d)".format(e.name, e.partitions.length)).mkString(", "))
  }

  override def getDependencies: Seq[Dependency[_]] = {
    rdds.map { rdd: RDD[_] =>
      new OneToOneDependency(rdd)
    }
  }

  override def getPartitions: Array[Partition] = {
    val array = new Array[Partition](rdds.head.partitions.length)
    for (i <- array.indices) {
      array(i) = new MergePartition(i, rdds.zipWithIndex.map { case (rdd, j) => rdd.partitions(i) }.toArray)
    }
    array
  }

  override def compute(s: Partition, context: TaskContext): Iterator[(K, Array[Iterable[_]])] = {
    val part: MergePartition = s.asInstanceOf[MergePartition]

    val iters: Array[BufferedIterator[Pair[K, _]]] = for ((p, idx) <- part.parentParts.zipWithIndex) yield {
      parent[(K, _)](idx).iterator(p, context).buffered
    }

    new Iterator[(K, Array[Iterable[_]])] {
      def lowestKey() = {
        val keys = iters.flatMap { it => if (it.hasNext) Seq(it.head._1) else Seq() }
        val lk = if (keys.isEmpty)
          None
        else {
          keys.min
        }
        lk
      }

      def hasNext = lowestKey() != None

      def next(): (K, Array[Iterable[_]]) = {
        val lowkey = lowestKey().asInstanceOf[K]
        val values = for ((it, idx) <- iters.zipWithIndex) yield {
          val ab = new ArrayBuffer[Any](16)
          while (it.hasNext && it.head._1 == lowkey) {
            val (k, v) = it.next()
            ab += v
          }
          ab.toSeq.toIterable
        }
        (lowkey, values)
      }
    }
  }

  override def clearDependencies() {
    super.clearDependencies()
    rdds = null
  }
}



