# For Java build
# This will be executed every time you log in
. /safr/setup/safrbld.profile
# (put user customizations below...)
export EXINIT="set tabstop=4  notabs number"
export LIBPATH=$LIBPATH:/safr/mf_build/lib
export PERL5LIB=/safr/perl/lib/perl5/5.6.1:/safr/perl/lib/perl5/site_perl/5.6.1
export JAVA_HOME=/Java/J8.0_64
export PATH=/jazz/v6.0.6/scmtools/eclipse:/Java/J8.0_64:$PATH
umask 000
