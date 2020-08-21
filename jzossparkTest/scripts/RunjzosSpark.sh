#!/usr/bin/env bash
:

export YARN_CONF_DIR=/usr/local/Cellar/yarn/0.21.3


SQLFILE=/universal_ledger/SAFRonSpark/data/InitEnv/SimpleTest1.sql

SAFRJAR=/universal_ledger/SAFRonSpark/target/scala-2.11/safronspark_2.11-0.1.0-SNAPSHOT.jar
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
