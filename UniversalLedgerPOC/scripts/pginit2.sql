--# * (c) Copyright IBM Corporation. 2019
--# * SPDX-License-Identifier: Apache-2.0
--# * By Kip Twitchell

-- '************************************************************'
-- '************* postgreSQL DB test vagrant  user *************'

-- the following are from the postgreSQL script file installing that product:

--PGDATA=/var/lib/pgsql/data
--PGHOST=ulserver
--PGUSER=vagrant
--PGPASSWORD=univledger
--/usr/pgsql-10/bin/psql --version

CREATE TABLE test.test (coltest varchar(20));
insert into test.test (coltest) values ('It works!');
SELECT * from test.test;
DROP TABLE test.test;
\q
