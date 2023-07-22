//JZOSMR95 JOB (ACCT),'JZOS JAVA&MR95',
//          NOTIFY=&SYSUID.,
//          CLASS=A,
//          MSGLEVEL=(1,1),
//          MSGCLASS=H
//*
//         EXPORT SYMLIST=*
//*
//*        SET HLQ=<YOUR-TSO-PREFIX>
//         SET HLQ=GEBT
//*        SET MLQ=GVBDEMO
//         SET MLQ=RTC22589
//*
//*        JCLLIB ORDER=AJV.V11R0M0.PROCLIB
//         JCLLIB ORDER=GEBT.RTC22964.EXITS.JCL
//*
//JOBLIB   DD DISP=SHR,DSN=AJV.V11R0M0.SIEALNKE
//*        DD DISP=SHR,DSN=GEBT.NBEESLE.GVBLOAD
//         DD DISP=SHR,DSN=GEBT.RTC22964.GVBLOAD
//         DD DISP=SHR,DSN=GEBT.LATEST.GVBLOAD
//*
//*********************************************************************
//* Copy dll JNIzOS64 to accessible location
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
 OPUT  'NBEESLE.PRIVATE.LOADLIB(JNIASM)' -
       '/safr/mf_build/lib/JNIzOS64'
//*
//*
//*********************************************************************
//* DELETE THE FILE(S) CREATED IN NEXT STEP
//*********************************************************************
//*
//PSTEP700 EXEC PGM=IDCAMS
//*
//SYSPRINT DD SYSOUT=*
//*
//SYSIN    DD *,SYMBOLS=EXECSYS
 /* VIEW DATA SETS: */

 DELETE  &HLQ..&MLQ..PASS1E1.OUTPUT01 PURGE
 DELETE  &HLQ..&MLQ..PASS1E1.OUTPUT02 PURGE
 DELETE  &HLQ..&MLQ..PASS1E1.OUTPUT03 PURGE
 DELETE  GEBT.RTC10539.PASS1E1.OUTPUT06 PURGE
 DELETE  &HLQ..&MLQ..PASS1E1.F0010724 PURGE
 DELETE  &HLQ..&MLQ..PASS1E1.F0010897 PURGE
 DELETE  GEBT.RTC10539.PASS1E1.F0010901 PURGE


 /* DATA SETS  GOING TO FORMAT PHASE */

 DELETE  &HLQ..&MLQ..PASS1E1.FILE001.EXT PURGE
 DELETE  &HLQ..&MLQ..PASS1E1.FILE001.SXT PURGE
 DELETE  &HLQ..&MLQ..PASS1E1.FILE002.EXT PURGE
 DELETE  &HLQ..&MLQ..PASS1E1.FILE002.SXT PURGE
 DELETE  &HLQ..&MLQ..PASS1E1.FILE003.EXT PURGE
 DELETE  &HLQ..&MLQ..PASS1E1.FILE003.SXT PURGE
 DELETE  &HLQ..&MLQ..PASS1E1.SYSMDUMP PURGE

 IF LASTCC > 0 THEN        /* IF OPERATION FAILED,     */    -
     SET MAXCC = 0          /* PROCEED AS NORMAL ANYWAY */

//*
//*The following DDs can/should be present in the calling JCL
//*
//*********************************************************************
//*   DELETE DATA SETS
//*********************************************************************
//*
//* DELETE    EXEC PGM=IDCAMS
//*
//* SYSPRINT  DD SYSOUT=*,DCB=(LRECL=133,BLKSIZE=12901,RECFM=FBA)
//*
//* SYSIN     DD *
//* DELETE  GEBT.FTEST.NEW.JMR91.JLT PURGE
//* IF LASTCC > 0  THEN        /* IF OPERATION FAILED,     */    -
//*     SET MAXCC = 0          /* PROCEED AS NORMAL ANYWAY */
//*
//* DELETE  GEBT.FTEST.NEW.JMR91.VDP PURGE
//* IF LASTCC > 0  THEN        /* IF OPERATION FAILED,     */    -
//*     SET MAXCC = 0          /* PROCEED AS NORMAL ANYWAY */
//*
//* DELETE  GEBT.FTEST.NEW.JMR91.XLT PURGE
//* IF LASTCC > 0  THEN        /* IF OPERATION FAILED,     */    -
//*     SET MAXCC = 0          /* PROCEED AS NORMAL ANYWAY */
//* DELETE  GEBT.FTEST.BIGASS.ALIGN.MR91LOG PURGE
//* IF LASTCC > 0  THEN        /* IF OPERATION FAILED,     */    -
//*     SET MAXCC = 0          /* PROCEED AS NORMAL ANYWAY */
//*********************************************************************
//*
//*******************************************************************
//* Licensed Materials - Property of IBM
//* 5655-DGJ
//* Copyright IBM Corp. 1997, 2021
//* STATUS = HJVBB00
//*
//* Batch job to run the Java VM calling gvbmr95e
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
// JAVACLS='GvbJavaDaemon'
//STDENV DD *
# This is a shell script which configures
# any environment variables for the Java JVM.
# Variables must be exported to be seen by the launcher.

. /etc/profile
. /u/nbeesle/jcallhlasmprofile2

LIBPATH=/lib:/usr/lib:"${JAVA_HOME}"/bin
LIBPATH="$LIBPATH":"${JAVA_HOME}"/lib
LIBPATH="$LIBPATH":"${JAVA_HOME}"/lib/j9vm
LIBPATH="$LIBPATH":/safr/mf_build/lib
export LIBPATH="$LIBPATH":
# LIBPATH="$LIBPATH":"//NBEESLE.PRIVATE.LOADLIB"
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
//DDEXEC   DD *
PGM=GVBMR95E
/*
//DDPARM   DD *
/*
//*
//STDOUT   DD SYSOUT=*
//STDERR   DD SYSOUT=*
//*
//*        <<< INPUT GENEVAERS FILES >>>
//*
//EXTRPARM DD *
*
*   STANDARD OPTIONS
*-------------------
*
*RUN_DATE=20170105                      DEFAULT: (CURRENT DATE)
*FISCAL_DATE_DEFAULT=20161231           DEFAULT: RUN_DATE
*FISCAL_DATE_OVERRIDE=1:20160731        DEFAULT: FISCAL_DATE_DEFAULT
*
*DISK_THREAD_LIMIT=2                    DEFAULT: 9999
*TAPE_THREAD_LIMIT=10                   DEFAULT: 9999
*
*IO_BUFFER_LEVEL=8                      DEFAULT: 4
*OPTIMIZE_PACKED_OUTPUT=N               DEFAULT: Y
*PAGE_FIX_IO_BUFFERS=N                  DEFAULT: Y
*TREAT_MISSING_VIEW_OUTPUTS_AS_DUMMY=Y  DEFAULT: N
*ABEND_ON_CALCULATION_OVERFLOW=N        DEFAULT: Y
*
*   DEBUGGING OPTIONS
*--------------------
*
*TRACE=Y                                DEFAULT: N
*DUMP_LT_AND_GENERATED_CODE=Y           DEFAULT: N
*SOURCE_RECORD_LIMIT=100                DEFAULT: (NO LIMIT)
*ABEND_ON_LOGIC_TABLE_ROW_NBR=57        DEFAULT: (NO ABEND)
*ABEND_ON_MESSAGE_NBR=149               DEFAULT: (NO ABEND)
*EXECUTE_IN_PARENT_THREAD=A             DEFAULT: N
*                                           1=1ST UNIT, A=ALL UNITS
//*
//EXTRTPRM DD *
*TRACE KEYWORDS:
*VIEW=,FROMREC=,THRUREC=,FROMLTROW=,THRULTROW=,LTFUNC=,DDNAME=
*VPOS=,VLEN=,VALUE=
*
*TRACE EXAMPLES:
*VIEW=4418,LTFUNC=WR
*VPOS=17,VLEN=10,VALUE=24CXA01501,VIEW=3424
*VIEW=302
//*
//EXTRENVV DD *
$SUBSYS=DM12
$QUAL=SDATRT01
//*
//*MR95VDP  DD DSN=&HLQ..&MLQ..PASS1C1.VDP,
//*            DISP=SHR
//*
//*EXTRLTBL DD DSN=&HLQ..&MLQ..PASS1C1.XLT,
//*            DISP=SHR
//*
//*EXTRREH  DD DSN=&HLQ..&MLQ..PASS1D1.REH,
//*            DISP=SHR
//*
//MR95VDP  DD DSN=GEBT.RTC10539.PASS1C1.VDP,
//            DISP=SHR
//*
//EXTRLTBL DD DSN=GEBT.RTC10539.PASS1C1.XLT,
//            DISP=SHR
//*
//EXTRREH  DD DSN=GEBT.RTC10539.PASS1D1.REH,
//            DISP=SHR
//*
//*        <<< INPUT GENEVAERS REFERENCE WORK FILES >>>
//*                                                                 %%%
//REFR001  DD DISP=SHR,DSN=GEBT.RTC10539.PASS1D1.FILE001.RED
//REFR002  DD DISP=SHR,DSN=GEBT.RTC10539.PASS1D1.FILE002.RED
//REFR003  DD DISP=SHR,DSN=GEBT.RTC10539.PASS1D1.FILE003.RED
//REFR004  DD DISP=SHR,DSN=GEBT.RTC10539.PASS1D1.FILE004.RED
//REFR005  DD DISP=SHR,DSN=GEBT.RTC10539.PASS1D1.FILE005.RED
//*
//*        <<< INPUT SOURCE FILES >>>
//*                                                                 %%%
//CUSTOMER  DD DISP=SHR,DSN=GEBT.RTC22589.CUSTOMER
//*                                                                 %%%
//*           FILES READ BY GVBXR6 GVBXR7 GVBXR8 GVBXR9             %%%
//*                                                                 %%%
//CUSTNAM6 DD DISP=SHR,DSN=&HLQ..&MLQ..CUSTNAME
//CUSTNAM8 DD DISP=(SHR,KEEP,KEEP),DSN=&HLQ..&MLQ..CUSTNAME.CLUSTER
//CUSTNAM7 DD DISP=SHR,DSN=&HLQ..&MLQ..CUSTNAME
//CUSTNAM9 DD DISP=(SHR,KEEP,KEEP),DSN=&HLQ..&MLQ..CUSTNAME.CLUSTER
//*
//*        <<< OUTPUT FILES GOING TO FORMAT PHASE >>>
//*
//EXTR001  DD DSN=&HLQ..&MLQ..PASS1E1.FILE001.EXT,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=(SYSDA,10),
//            SPACE=(TRK,(1,1000),RLSE),
//            DCB=(DSORG=PS,RECFM=VB,LRECL=8192)
//*
//SORT001  DD DSN=&HLQ..&MLQ..PASS1E1.FILE001.SXT,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,
//            SPACE=(TRK,(1,1),RLSE),
//            DCB=(DSORG=PS,RECFM=FB,LRECL=80)
//*
//EXTR002  DD DSN=&HLQ..&MLQ..PASS1E1.FILE002.EXT,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=(SYSDA,10),
//            SPACE=(TRK,(1,1000),RLSE),
//            DCB=(DSORG=PS,RECFM=VB,LRECL=8192)
//*
//SORT002  DD DSN=&HLQ..&MLQ..PASS1E1.FILE002.SXT,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,
//            SPACE=(TRK,(1,1),RLSE),
//            DCB=(DSORG=PS,RECFM=FB,LRECL=80)
//*
//EXTR003  DD DSN=&HLQ..&MLQ..PASS1E1.FILE003.EXT,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=(SYSDA,10),
//            SPACE=(TRK,(1,1000),RLSE),
//            DCB=(DSORG=PS,RECFM=VB,LRECL=8192)
//*
//SORT003  DD DSN=&HLQ..&MLQ..PASS1E1.FILE003.SXT,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,
//            SPACE=(TRK,(1,1),RLSE),
//            DCB=(DSORG=PS,RECFM=FB,LRECL=80)
//*
//*        <<< OUTPUT EXTRACT VIEW FILES >>>
//*                                                                 %%%
//OUTPUT06 DD DSN=GEBT.RTC10539.PASS1E1.OUTPUT06,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,
//            SPACE=(TRK,(100,100),RLSE),
//            DCB=(DSORG=PS,RECFM=VB,LRECL=1004)
//*                                                                 %%%
//F0010901 DD DSN=GEBT.RTC10539.PASS1E1.F0010901,
//            DISP=(NEW,CATLG,DELETE),
//            UNIT=SYSDA,
//            SPACE=(TRK,(100,100),RLSE),
//            DCB=(DSORG=PS,RECFM=VB,LRECL=1004)
//*
//DDPRINT  DD SYSOUT=*
//SYSPRINT DD SYSOUT=*
//EXTRRPT  DD SYSOUT=*
//EXTRLOG  DD SYSOUT=*
//EXTRTRAC DD SYSOUT=*
//MERGRPT  DD SYSOUT=*
//SNAPDATA DD SYSOUT=*
//SYSOUT   DD SYSOUT=*
//CEEDUMP  DD SYSOUT=*
//*
//IDIOFF   DD DUMMY
//*
//SYSMDUMP DD DSN=&HLQ..&MLQ..PASS1E1.SYSMDUMP,
//            DISP=(NEW,DELETE,CATLG),
//            UNIT=SYSDA,
//            SPACE=(TRK,(1000,1000),RLSE),
//            DCB=(DSORG=PS,RECFM=FBS,LRECL=4160)
//
