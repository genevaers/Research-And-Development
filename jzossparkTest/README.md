# jzos-sparkTest
#
# (c) Copyright IBM Corporation 2020.  
#     Copyright Contributors to the GenevaERS Project.
# SPDX-License-Identifier: Apache-2.0
#
# ***************************************************************************
#                                                                           
#   Licensed under the Apache License, Version 2.0 (the "License");         
#   you may not use this file except in compliance with the License.        
#   You may obtain a copy of the License at                                 
#                                                                           
#     http://www.apache.org/licenses/LICENSE-2.0                            
#                                                                           
#   Unless required by applicable law or agreed to in writing, software     
#   distributed under the License is distributed on an "AS IS" BASIS,       
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and     
#   limitations under the License.                                          
# ****************************************************************************
This repository contains new POC code started July 2020 at the initiation of the GenevaERS proejct.  You can read more about the scope and objectives of this test [here](https://genevaers.org/2020/07/29/organize-and-commit-to-the-spark-genevaers-poc/)

This code is combining code from the ngsafr POC (which had scala calling jzos) and the Universal Ledger POC (which called Spark) to create a single module which calls jzos from Apache Spark.

This code requires it be run on z/OS as jzos IO does not work locally.