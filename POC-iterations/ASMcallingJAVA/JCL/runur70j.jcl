//JZOSCOBL JOB (ACCT),'JZOS UR70TEST',                                  JOB20790
//          NOTIFY=&SYSUID.,
//          CLASS=A,
//          MSGLEVEL=(1,1),
//          MSGCLASS=H
//*
//*********************************************************************
//* RUN GVBUR70 TEST PROGRAM TESTUR70 TO CALL JAVA CLASS METHOD
//*********************************************************************
//*
//         EXPORT SYMLIST=*
//*
//*        SET HLQ=<YOUR-TSO-PREFIX>
//*        SET MLQ=GVBDEMO
//*
//         JCLLIB ORDER=&LVL1..RTC&RTC..JCL
//*
//JOBLIB   DD DISP=SHR,DSN=AJV.V11R0M0.SIEALNKE
//         DD DISP=SHR,DSN=&LVL1..RTC&RTC..GVBLOAD
//         DD DISP=SHR,DSN=CEE.SCEERUN
//*
//*********************************************************************
//* Copy dll JNIzOS64 to ZFS location
//*********************************************************************
//*
//COPYDLL  EXEC PGM=IKJEFT1A
//*
//SYSEXEC  DD DISP=SHR,DSN=SYS1.SBPXEXEC
//*
//ISPWRK1  DD DISP=(NEW,DELETE,DELETE),
//            UNIT=VIO,
//            SPACE=(TRK,(10000,10000)),
//            DCB=(LRECL=256,BLKSIZE=2560,RECFM=FB)
//*
//ISPLOG   DD DUMMY                              ISPF LOG FILE
//*
//ISPPROF  DD DISP=(NEW,DELETE,DELETE),          ISPF PROFILE
//            UNIT=SYSDA,
//            DCB=(DSORG=PO,RECFM=FB,LRECL=80),
//            SPACE=(TRK,(5,5,5))
//*
//ISPPLIB  DD DSN=ISP.SISPPENU,
//            DISP=SHR                           ISPF PANELS
//*
//ISPMLIB  DD DSN=ISP.SISPMENU,
//            DISP=SHR                           ISPF MENUS
//         DD DSN=SYS1.SBPXMENU,
//            DISP=SHR                           ISPF MENUS
//*
//ISPTLIB  DD DISP=(NEW,DELETE,DELETE),          ISPF TABLES (INPUT)
//            UNIT=SYSDA,
//            SPACE=(TRK,(5,5,5)),
//            DCB=(DSORG=PO,RECFM=FB,LRECL=80)
//         DD DSN=ISP.SISPTENU,
//            DISP=SHR
//*
//ISPSLIB  DD DSN=ISP.SISPSLIB,
//            DISP=SHR                           JCL SKELETONS
//*
//ISPLLIB  DD DISP=SHR,DSN=SYS1.LINKLIB
//         DD DISP=SHR,DSN=ISP.SISPLOAD
//*
//SYSTSPRT DD SYSOUT=*
//*
//ISPTABL  DD DUMMY                              ISPF TABLES (OUTPUT)
//*
//ISPFTTRC DD SYSOUT=*,RECFM=VB,LRECL=259        TSO OUTPUT
//*
//SYSTSIN  DD *
 OPUT  '&LVL1..RTC&RTC..GVBLOAD(JNIASM)' -
       '/safr/mf_build/lib/JNIzOS64'
//*
//*******************************************************************
//*
//* Batch job to run the Java VM calling TSTUR70
//*
//* Tailor the proc and job for your installation:
//* 1.) Modify the Job card per your installation's requirements
//* 2.) Modify the PROCLIB card to point to this PDS
//* 3.) edit JAVA_HOME to point the location of the SDK
//* 4.) edit APP_HOME to point the location of your app (if any)
//* 5.) Modify the CLASSPATH as required to point to your Java code
//* 6.) Modify JAVACLS and ARGS to launch desired Java class
//*
//*******************************************************************
//JAVA EXEC PROC=JVMPRC16,
// JAVACLS='GvbJavaDaemon2'
//STDENV DD *
# This is a shell script which configures
# any environment variables for the Java JVM.
# Variables must be exported to be seen by the launcher.

. /etc/profile
. /u/<user-id>/jcallhlasmprofile2

LIBPATH=/lib:/usr/lib:"${JAVA_HOME}"/bin
LIBPATH="$LIBPATH":"${JAVA_HOME}"/lib
LIBPATH="$LIBPATH":"${JAVA_HOME}"/lib/j9vm
LIBPATH="$LIBPATH":/safr/mf_build/lib
export LIBPATH="$LIBPATH":
# LIBPATH="$LIBPATH":"//&LVL1..RTC&RTC..GVBLOAD"
# Customize your CLASSPATH here


# Add Application required jars to end of CLASSPATH
for i in $(find $APP_HOME -type f);do
    CLASSPATH="$CLASSPATH":"$i"
    done
export CLASSPATH="$CLASSPATH":

# Set JZOS specific options
# Use this variable to specify encoding for DD STDOUT and STDERR
#export JZOS_OUTPUT_ENCODING=Cp1047
# Use this variable to prevent JZOS from handling MVS operator commands
#export JZOS_ENABLE_MVS_COMMANDS=false
# Use this variable to supply additional arguments to main
#export JZOS_MAIN_ARGS=""

# Configure JVM options
IJO="-Xms16m -Xmx128m"
# Uncomment the following to aid in debugging "Class Not Found" problems
#IJO="$IJO -verbose:class"
# Uncomment the following if you want to run with Ascii file encoding..
IJO="$IJO -Dfile.encoding=ISO8859-1"
export IBM_JAVA_OPTIONS="$IJO "

//*
//STDOUT   DD SYSOUT=*
//STDERR   DD SYSOUT=*
//*
//*******************************************************************
//* EXEC CARD FOR MAIN TSTUR70 PROGRAM CALLING JAVA
//*******************************************************************
//*
//DDEXEC   DD *
PGM=TESTUR70
/*
//*
//M456     DD DISP=(SHR,KEEP,KEEP),DSN=&LVL1..RTC&RTC..M456.CLUSTER
//SYSIN    DD DUMMY
//SYSPRINT DD SYSOUT=*
//SYSOUT   DD SYSOUT=*
//SYSTERM  DD SYSOUT=*
//DDPRINT  DD SYSOUT=*
//CEEDUMP  DD SYSOUT=*
//*
//IDIOFF   DD DUMMY
//SYSUDUMP DD SYSOUT=*
//
