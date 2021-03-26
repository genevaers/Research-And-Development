--# * (c) Copyright IBM Corporation. 2019
--# * SPDX-License-Identifier: Apache-2.0
--# * By Kip Twitchell

-- '***************************************************'
-- '************* initialize postgreSQL DB *************'

-- the following are from the postgreSQL script file installing that product:

--PGDATA=/var/lib/pgsql/data
--PGHOST=ulserver
--PGUSER=vagrant
--PGPASSWORD=univledger
--/usr/pgsql-10/bin/psql --version

CREATE USER vagrant PASSWORD 'univledger';
CREATE DATABASE univledger;
GRANT ALL ON DATABASE univledger TO vagrant;
CREATE SCHEMA test;
GRANT ALL ON SCHEMA test TO vagrant;
GRANT ALL ON ALL TABLES IN SCHEMA test TO vagrant;
\q
