#!/usr/bin/env bash

# This script adds PostgrSQL DB to the universal-ledger environment
# *
# * (c) Copyright IBM Corporation. 2019
# * SPDX-License-Identifier: Apache-2.0
# * By Kip Twitchell

echo '*************************************************************'
echo '************* download and unpack postgreSQL DB *************'

 # The following are commented out as the Vagrant Box chosen already has these installed
cd /home/vagrant

# Install Extra Packages for Enterprise Linux (EPEL)
sudo yum -y install https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm

# Install Repository
#sudo yum -y install https://download.postgresql.org/pub/repos/yum/10/redhat/rhel-7-x86_64/pgdg-centos10-10-2.noarch.rpm
sudo yum -y install https://download.postgresql.org/pub/repos/yum/reporpms/EL-7-x86_64/pgdg-redhat-repo-latest.noarch.rpm

# Install Client
sudo yum -y install postgresql10

# Install server packages:
sudo yum -y install postgresql10-server

# Initialize DB and enable auto start
sudo /usr/pgsql-10/bin/postgresql-10-setup initdb
#sudo systemctl enable postgresql-10
sudo systemctl start postgresql-10

# Set initial environment variables
sudo cp /etc/profile /etc/profile_backup_postgres  #Backup the profile file
echo 'export PGDATA=/var/lib/pgsql/data' | sudo tee -a /etc/profile
echo 'export PGHOST=ulserver' | sudo tee -a /etc/profile
#echo 'export PGUSER=vagrant' | sudo tee -a /etc/profile
#echo 'export PGPASSWORD=univledger' | sudo tee -a /etc/profile
export PATH="/usr/pgsql-10/bin:$PATH"
source /etc/profile
echo 'PG Data:'
echo $PGDATA
echo 'Path'
echo $PATH

# Set modify postgreSQL configuration files to allow access from VM host
sudo cp /var/lib/pgsql/10/data/postgresql.conf /var/lib/pgsql/10/data/postgresql.conf_backup  #Backup the file
echo $'listen_addresses = \'*\'' | sudo tee -a /var/lib/pgsql/10/data/postgresql.conf
sudo cp /var/lib/pgsql/10/data/pg_hba.conf /var/lib/pgsql/10/data/pg_hba.conf_backup  #Backup the file
echo 'host all all all trust' | sudo tee -a /var/lib/pgsql/10/data/pg_hba.conf
sudo systemctl restart postgresql-10

#
#Confirm postgreSQL installation
echo "!!!!!!!!!!!!!!!!!!!"
echo "postgreSQL version:"
/usr/pgsql-10/bin/psql --version
echo "!!!!!!!!!!!!!!!!!!!"

echo '****************************************************************'
echo '************* see additional commands in bottom of *************'
echo '************* /universal_ledger/script/postgreSQLinit.sh *************'
echo '*************      to complete db installation     *************'
echo '****************************************************************'
echo '#              execute the following commands at the Vagrant Linux Prompt                                         '
echo '#sudo su - postgres                            # Logs in as postgres user ID to access databasae                  '
echo '#psql -f /universal_ledger/scripts/pginit1.sql       # runs script establishing initial user                            '
echo '#                                                                                                                 '
echo '#exit                                          # logs out of user postgres                                        '
echo '#psql -d univledger -f /universal_ledger/scripts/pginit2.sql  # test new users capabilities to make tables and drop them '
