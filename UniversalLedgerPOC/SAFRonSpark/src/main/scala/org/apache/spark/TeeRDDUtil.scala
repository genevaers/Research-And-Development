//package org.apache.spark
//
//import scala.collection.mutable.ListBuffer
//import org.apache.spark.RDDExtension.TeeRDD
//import org.apache.spark.rdd.RDD
//
//import scala.collection.mutable.{HashSet, Stack}
//
// (c) Copyright IBM Corporation 2020.  
//     Copyright Contributors to the GenevaERS Project.*
// SPDX-License-Identifier: Apache-2.0
//
// ***************************************************************************
//                                  *                                         
//   Licensed under the Apache License, Version 2.0 (the "License");         
//   you may not use this file except in compliance with the License.        
//   You may obtain a copy of the License at                                 
//                                                                           
//     http://www.apache.org/licenses/LICENSE-2.0                            
//                                                                           
//   Unless required by applicable law or agreed to in writing, software     
//   distributed under the License is distributed on an "AS IS" BASIS,       
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and     
//   limitations under the License.                                          
// ***************************************************************************
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
