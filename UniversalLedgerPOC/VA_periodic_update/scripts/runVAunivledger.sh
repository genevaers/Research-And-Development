#!/usr/bin/env bash
:
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
export YARN_CONF_DIR=/usr/local/Cellar/yarn/0.21.3

#SAFRJAR=/universal_ledger/SAFRonSpark/target/scala-2.11/safronspark_2.11-0.1.0-SNAPSHOT.jar
VAULJAR=/universal_ledger/VA_periodic_update/target/scala-2.12/va_periodic_update_2.12-0.1.0-SNAPSHOT.jar # sbt atrifact
#VAULJAR=/universal_ledger/VA_periodic_update/target/scala-2.12/va_periodic_update.jar # Intellij artifact
LOG4J=/universal_ledger/SAFRonSpark/src/main/resources/log4j.properties
log4j_setting="-Dlog4j.configuration=file:${LOG4J}"

# Remove previous output file.
#rm -r /universal_ledger/SAFRonSpark/data/InitEnv/OUTPUTDIR

echo --- Running VA Universal Ledger
#echo --- Running $SQLFILE

#stand alone mode, without yarn
#$SPARK_HOME/bin/spark-submit --master local[2] \

#executed in yarn mode
$SPARK_HOME/bin/spark-submit --master yarn-client \
				     --conf "spark.driver.extraJavaOptions=-Xss4m" \
				     --conf "spark.driver.extraJavaOptions=${log4j_setting}" \
                     --conf "spark.executor.extraJavaOptions=${log4j_setting}" \
                     --conf spark.driver.extraClassPath=/universal_ledger/lib/postgresql-42.2.5.jar \
				     --files $LOG4J \
				     --jars $VAULJAR  \
 				     --class com.ibm.VA_ledger.ledger.VA_ledger $VAULJAR
