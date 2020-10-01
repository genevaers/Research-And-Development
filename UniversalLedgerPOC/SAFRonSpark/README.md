# SAFRonSpark
Scalable Architecture for Financial Reporting on Spark Prototype

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

Pre-requisites include 
    Java 1.8+
    Scala 2.10+
    Spark 1.6+ (note, code not compabible with latest Spark version)
    
The following unit test case is include to test the installation:

* Simple test case definition for SAFR on Spark (UnitTest1.sql) -  update path names of the data files below
* Data files (Fact.csv, DimDeptCode.csv) - Put these somewhere on the file system (including HDFS)
* Shell script to run testcase(UnitTest1.sh) - Please update path names. Right now, this uses YARN.

