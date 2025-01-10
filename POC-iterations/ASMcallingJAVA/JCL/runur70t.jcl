//RUNIVPJA JOB (ACCT),CLASS=A,MSGCLASS=X,MSGLEVEL=(1,1),NOTIFY=&SYSUID
//*
//*********************************************************************
//*
//* (C) COPYRIGHT IBM CORPORATION 2024.
//*    Copyright Contributors to the GenevaERS Project.
//*SPDX-License-Identifier: Apache-2.0
//*
//*********************************************************************
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
//*********************************************************************
//* RUN GVBUR70 IVP ASSEMBLER PROGRAM TSTUR70 TO CALL JAVA CLASS METHOD
//*********************************************************************
//*
//         EXPORT SYMLIST=*
//*
//         SET HLQ=<YOUR-TSO-PREFIX>
//         SET MLQ=GVBDEMOJ
//*
//         JCLLIB ORDER=AJV.V11R0M0.PROCLIB
//*
//JOBLIB   DD DISP=SHR,DSN=AJV.V11R0M0.SIEALNKE
//         DD DISP=SHR,DSN=&HLQ..&MLQ..LOADLIB
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
//SYSTSIN  DD *,SYMBOLS=EXECSYS
 OPUT  '&HLQ..&MLQ..LOADLIB(GVBJDLL)' -
       '/u/<your-user-id>/DllLib/GVBJDLL'
//*
//*********************************************************************
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
//*********************************************************************
//TSTUR70 EXEC PROC=JVMPRC16,
// JAVACLS='GvbJavaDaemon'
//STDENV DD *
# This is a shell script which configures
# any environment variables for the Java JVM.
# Variables must be exported to be seen by the launcher.

. /etc/profile
. /u/<your-user-id>/ASMcallingJAVAprofile

LIBPATH=/lib:/usr/lib:"${JAVA_HOME}"/bin
LIBPATH="$LIBPATH":"${JAVA_HOME}"/lib
LIBPATH="$LIBPATH":"${JAVA_HOME}"/lib/j9vm
LIBPATH="$LIBPATH":/u/<your-user-id>/DllLib
export LIBPATH="$LIBPATH":
# LIBPATH="$LIBPATH":"//&HLQ..&MLQ..LOADLIB"
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
//*********************************************************************
//* EXEC CARD FOR TSTUR70 IVP PROGRAM MAKING CALLS TO JAVA
//* 
//* FLAG1: U => General API i.e. GVBUR70
//* 
//* FLAG2: 0 => Transport data as-is (byte[] hex array, no translation)
//*        1 => Convert SEND buffer to ASCII string for Java method call
//*             and convert its reply to EBCDIC for RECEIVE buffer
//* 
//*********************************************************************
//*
//DDEXEC   DD *
PGM=TSTUR70,PARM='TASKS=20,NCALL=32767,FLAGS=U0'
/*
//*
//SYSIN    DD DUMMY
//UR70PRNT DD SYSOUT=*
//SYSPRINT DD SYSOUT=*
//SYSOUT   DD SYSOUT=*
//SYSTERM  DD SYSOUT=*
//DDPRINT  DD SYSOUT=*
//CEEDUMP  DD SYSOUT=*
//*
//IDIOFF   DD DUMMY
//SYSUDUMP DD SYSOUT=*
//
