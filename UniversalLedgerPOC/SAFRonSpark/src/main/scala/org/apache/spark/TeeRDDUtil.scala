//package org.apache.spark
//
//import scala.collection.mutable.ListBuffer
//import org.apache.spark.RDDExtension.TeeRDD
//import org.apache.spark.rdd.RDD
//
//import scala.collection.mutable.{HashSet, Stack}
//
//object TeeRDDUtil {
//
//  def getWriters(rdd: RDD[_]): List[SparkHadoopWriter] = {
//    val visited = new HashSet[RDD[_]]
//    val waitingForVisit = new Stack[RDD[_]]
//    val writers = new ListBuffer[SparkHadoopWriter]
//
//    waitingForVisit.push(rdd)
//
//    while (!waitingForVisit.isEmpty) {
//      val r = waitingForVisit.pop()
//      r match {
//        case tee: TeeRDD[_]#MyTeeRDD[_] => writers += tee.getWriter
//        case _ => ()
//      }
//      for (dep <- r.dependencies) {
//        waitingForVisit.push(dep.rdd)
//      }
//    }
//    writers.toList
//  }
//
//}
