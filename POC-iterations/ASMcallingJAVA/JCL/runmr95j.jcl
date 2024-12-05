//RUNMR95J  JOB (ACCT),'JAVA&MR95E',
//          NOTIFY=&SYSUID.,
//          CLASS=A,
//          MSGLEVEL=(1,1),
//          MSGCLASS=H
//********************************************************************
//*
//* (C) COPYRIGHT IBM CORPORATION 2023.
//*    Copyright Contributors to the GenevaERS Project.
//*SPDX-License-Identifier: Apache-2.0
//*
//********************************************************************
//*
//*  Licensed under the Apache License, Version 2.0 (the "License");
//*  you may not use this file except in compliance with the License.
//*  You may obtain a copy of the License at
//*
//*     http://www.apache.org/licenses/LICENSE-2.0
//*
//*  Unless required by applicable law or agreed to in writing, software
//*  distributed under the License is distributed on an "AS IS" BASIS,
//*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
//*  or implied.
//*  See the License for the specific language governing permissions
//*  and limitations under the License.
//*
//******************************************************************
//* Run GVBDEMO Extract job that will call JAVA exit(s)
//*********************************************************************
//*
//         EXPORT SYMLIST=*
//*
//*        SET HLQ=<YOUR-TSO-PREFIX>
//*        SET MLQ=GVBDEMO 
//*
//         JCLLIB ORDER=&LVL1..RTC&RTC..JCL
//*
//JOBLIB   DD DISP=SHR,DSN=&LVL1..RTC&RTC..GVBLOAD
//*
//*********************************************************************
//* Copy dll JNIASM to accessible location for Java LIBPATH
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
//*
//*********************************************************************
//* DELETE THE FILE(S) CREATED IN EXTRACT STEP
//*********************************************************************
//*
//PSTEP700 EXEC PGM=IDCAMS
//*
//SYSPRINT DD SYSOUT=*
//*
//SYSIN    DD *,SYMBOLS=EXECSYS
 /* VIEW DATA SETS: */

 DELETE  &HLQ..&MLQ..PASS1E1.F0010903 PURGE
 DELETE  &HLQ..&MLQ..PASS1E1.SYSMDUMP PURGE

 IF LASTCC > 0 THEN        /* IF OPERATION FAILED,     */    -
     SET MAXCC = 0          /* PROCEED AS NORMAL ANYWAY */

//*******************************************************************
//*
//* Batch job to run the Java VM calling GVBMR95(E)
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
//PSTEP705 EXEC PROC=JVMPRC16,
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

//*******************************************************************
//* EXEC CARDS FOR MAIN GVBMR95 PROGRAM
//*******************************************************************
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
//MR95VDP  DD DSN=&HLQ..&MLQ..PASS1C1.VDP,
//            DISP=SHR
//
//EXTRLTBL DD DSN=&HLQ..&MLQ..PASS1C1.XLT,
//            DISP=SHR
//
//EXTRREH  DD DSN=&HLQ..&MLQ..PASS1D1.REH,
//            DISP=SHR
//*
//*        <<< INPUT GENEVAERS REFERENCE WORK FILES >>>
//*                                                                 %%%
//*            NONE
//*
//*        <<< INPUT SOURCE FILES >>>
//*                                                                 %%%
//CUSTOMER  DD DISP=SHR,DSN=&HLQ..&MLQ..GVBDEMO.CUSTOMER
//*
//*        <<< OUTPUT EXTRACT VIEW FILES >>>
//*                                                                 %%%
//F0010903 DD DSN=&HLQ..&MLQ..PASS1E1.F0010903,
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
