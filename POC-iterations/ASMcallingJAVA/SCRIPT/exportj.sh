# This is the user profile for Al Sung
# This will be executed every time you log in
. /safr/setup/safrbld.profile
# (put user customizations below...)
export EXINIT="set tabstop=4  notabs number"
export LIBPATH=$LIBPATH:/safr/mf_build/lib
# spb - commented
#export DSNAOINI="/u/mbarret/odbcini"
export PERL5LIB=/safr/perl/lib/perl5/5.6.1:/safr/perl/lib/perl5/site_perl/5.6.1
# Perth export JAVA_HOME=/apc/java800/64bit/usr/lpp/java/J8.0_64
# Perth export PATH=/apc/java800/64bit/usr/lpp/java/J8.0_64/bin:$PATH
export JAVA_HOME=/Java/J8.0_64
# port IBM_JAVA_OPTIONS="-Dfile.encoding=ISO8859-1"
# export JAVA_HOME=/Java/J8064
# port PATH=/zoautil/v00/bin:/jazz/v6.0.6/scmtools/eclipse:/Java/J7.0_64:$PATH
export PATH=/jazz/v6.0.6/scmtools/eclipse:/Java/J8.0_64:$PATH
# export PATH=/jazz/v6.0.1/scmtools/eclipse:/Java/J8064:$PATH
# export SPARK_HOME=/Spark
# export PATH="$PATH":"$SPARK_HOME/bin"
# export PATH="$PATH":"$SPARK_HOME/sbin"
# . /etc/spark/conf/spark-env.sh
# /var/rocket/bin/bash
#
#   this portion dded for sbt to work
#
# export SPARK_HOME=/Spark
# export SPARK_CONF_DIR=/etc/spark/conf
# export SPARK_LOG_DIR=/etc/spark/logs
# export SPARK_WORKER_DIR=/etc/spark/work
# export SPARK_LOCAL_DIRS=/tmp/spark/scratch
# export SPARK_PID_DIR=/tmp/spark/pid
# export SBT_HOME=/u/safrbld/sbt/
# export sbt_home=/u/safrbld/sbt/
# export JAVA_HOME=/Java/J8.0_64
# export PATH=/bin:$JAVA_HOME/bin:/var/rocket/bin:/bin:$PATH:$HOME:
# export PATH=/db2/db2c10/jdbc/bin:.:$PATH
# export MANPATH=$MANPATH:/var/rocket/man/
# export IBM_JAVA_OPTIONS="-Dfile.encoding=ISO8859-1"
# export _CEE_RUNOPTS="FILETAG(AUTOCVT,AUTOTAG) POSIX(ON)"
# export _BPXK_AUTOCVT=ON
# export _TAG_REDIR_ERR=txt
# export _TAG_REDIR_IN=txt
# export _TAG_REDIR_OUT=txt
# export PS1='$LOGNAME':'$PWD':' >'
# export LIBPATH=/usr/lib/java_runtime:/db2/db2c10/base/lib
# export STEPLIB=DSN.V12R1M0.SDSNLOAD
# export CLASSPATH=/usr/include/java_classes/gxljapi.jar:/usr/include/java_classes/gxljosrgImpl.jar:/db2/db2c10/jdbc/classes/db2jcc4.jar
# export _C89_XSUFFIX_HOST="SDSNMACS"
# export _CXX_XSUFFIX_HOST="SDSNMACS"
#
#   end of the sbt portion
#
#   portion to allow use of PYTHON and Z OPEN AUTOMATION
#
# port ZOAU_HOME=/zoautil/v100/bin
# port PYTHON_ENV=python36
#xport PYTHONPATH=/hsstools/python36/python-2017-04-12-py36/pkgs/python-3.6.1-1/lib/python3.6/encodings:$ZOAU_HOME
#xport PYTHONHOME=/hsstools/python36/python-2017-04-12-py36/python36/bin/python
#xport LIBPATH=/hsstools/python36/python-2017-04-12-py36/python36/lib:/zoautil/v100/lib:$LIBPATH
#
#bash
umask 000

