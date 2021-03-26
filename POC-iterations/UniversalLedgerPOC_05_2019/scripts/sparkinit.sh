#!/usr/bin/env bash

# * (c) Copyright IBM Corporation. 2019
# * SPDX-License-Identifier: Apache-2.0
# * By Kip Twitchell
# This script adds Spark to the universal-ledger Scala environment
# Run the initenv.sh first to install Java.

echo '*****************************************************'
echo '************* download and unpack Spark *************'

SparkVersion=2.4.1

cd /home/vagrant

# TODO test for file already downloaded to improve restartability
wget http://archive.apache.org/dist/spark/spark-$SparkVersion/spark-$SparkVersion-bin-hadoop2.7.tgz
tar -xvf spark-$SparkVersion-bin-hadoop2.7.tgz

# Set Spark Home
sudo cp /etc/profile /etc/profile_backup_spark  #Backup the profile file
echo 'export SPARK_HOME=/home/vagrant/spark-'$SparkVersion'-bin-hadoop2.7' | sudo tee -a /etc/profile
export PATH="$SPARK_HOME/bin:$PATH"
source /etc/profile
echo 'Spark home:'
echo $SPARK_HOME
echo 'Path'
echo $PATH
 # Error here.  Path didn't get up dated with Spark Directory.  Not sure what is wrong with the code.

# create spark logging direcotry for Spark Session use
mkdir /tmp/spark-events

#Confirm Spark installation
echo "Spark version:"
(cd $SPARK_HOME/bin/; bash ./spark-submit --version)


echo '*****************************************************************************'
echo '********************* Build SAFRonSpark  **********************'
echo '*****************************************************************************'

(cd /universal_ledger/SAFRonSpark/; sbt package)

echo '*****************************************************************************'
echo '********************* Run initial SAFR on Spark Test;  **********************'
echo '*****************************************************************************'

(bash /universal_ledger/SAFRonSpark/data/InitEnv/RunSAFRonSpark.sh)

echo '************************************************************************************'
echo '*****  Logout of Vagrant ssh and log back in for Path Updates to take effect *******'
echo '************************************************************************************'


# Here is the command for running the spark code, after it has been compiled.
#
# /home/vagrant/spark-2.4.0-bin-hadoop2.7/bin/spark-submit --class com.ibm.univledger.runSpark --master local[*] /universal_ledger/spark/target/scala-2.12/spark_2.12-0.1.0-SNAPSHOT.jar
#  after path update with Spark_home/bin
# spark-submit --class com.ibm.univledger.runSpark --master local[*] /universal_ledger/spark/target/scala-2.12/spark_2.12-0.1.0-SNAPSHOT.jar

