#!/usr/bin/env bash
:
#(c) Copyright IBM Corporation. 2019
# SPDX-License-Identifier: Apache-2.0
# By Kip Twitchell


#export SPARK_HOME=/Users/ktwitchell001/spark-2-1-0
#export SPARK_HOME=/Users/ktwitchell001/spark-1-6-3 # Set by Spark Setup Script
export YARN_CONF_DIR=/usr/local/Cellar/yarn/0.21.3
#export PATH=$PATH:/Users/ktwitchell001/hadoop-2-8-0/bin # Set by Spark Setup Script


SQLFILE=/universal_ledger/data/VAPOData.sql

SAFRJAR=/universal_ledger/SAFRonSpark/target/scala-2.11/safronspark_2.11-0.1.0-SNAPSHOT.jar
#SAFRJAR=/Users/ktwitchell001/workspace/universal_ledger/SAFRonSpark/SAFRSpark-1.0.jar
LOG4J=/universal_ledger/SAFRonSpark/src/main/resources/log4j.properties
log4j_setting="-Dlog4j.configuration=file:${LOG4J}"


# Remove previous output file.
#rm -r /universal_ledger/SAFRonSpark/data/InitEnv/OUTPUTDIR

echo --- Running $SQLFILE

#stand alone mode, without yarn
#$SPARK_HOME/bin/spark-submit --master local[2] \

#executed in yarn mode
$SPARK_HOME/bin/spark-submit --master yarn-client \
				     --conf "spark.driver.extraJavaOptions=-Xss4m" \
				     --conf "spark.driver.extraJavaOptions=${log4j_setting}" \
                     --conf "spark.executor.extraJavaOptions=${log4j_setting}" \
				     --files $LOG4J \
				     --packages com.databricks:spark-csv_2.11:1.5.0 \
				     --jars $SAFRJAR \
				     --class com.ibm.safr.core.SAFR $SAFRJAR $SQLFILE
