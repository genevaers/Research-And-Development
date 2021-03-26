#!/usr/bin/env bash

# This script adds Derby DB to the universal-ledger Scala environment
# *
# * (c) Copyright IBM Corporation. 2019
# * SPDX-License-Identifier: Apache-2.0
# * By Kip Twitchell


echo '*****************************************************'
echo '************* download and unpack derby DB *************'

cd /home/vagrant
wget http://mirrors.ibiblio.org/apache//db/derby/db-derby-10.14.2.0/db-derby-10.14.2.0-bin.tar.gz
tar -xvf db-derby-10.14.2.0-bin.tar.gz

# Set Derby Home
sudo cp /etc/profile /etc/profile_backup_derby  #Backup the profile file
echo 'export DERBY_HOME=/home/vagrant/db-derby-10.14.2.0-bin' | sudo tee -a /etc/profile
export PATH="$DERBY_HOME/bin:$PATH"
source /etc/profile
# add a source statement to profile to consistently load the Derby Classpath file
echo "source $DERBY_HOME/bin/setEmbeddedCP" | sudo tee -a /etc/profile
source /etc/profile
echo 'Derby home:'
echo $DERBY_HOME
echo 'Path'
echo $PATH
echo "Class Path"
echo $CLASSPATH

#Confirm Derby installation
echo "Derby version:"
java org.apache.derby.tools.sysinfo

