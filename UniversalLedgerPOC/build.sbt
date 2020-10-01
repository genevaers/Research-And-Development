import sbt.Keys.licenses

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

lazy val commonSettings = Seq(
  organization := "com.ibm.univledger",

  version := "0.1.0-SNAPSHOT",

  scalaVersion := "2.11.8"

)

//name := "universal_ledger"

lazy val root = Project(id="universal_ledger", base = file("."))

lazy val SAFRonSpark = (project in file("SAFRonSpark"))
  .settings(
    commonSettings,
    // other settings
  )


lazy val VA_periodic_update = (project in file("VA_periodic_update"))
  .settings(
    commonSettings,
    // other settings
  )
