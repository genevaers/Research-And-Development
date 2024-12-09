//BLDUR70C JOB (ACCT),CLASS=A,MSGCLASS=X,MSGLEVEL=(1,1),NOTIFY=&SYSUID
//*
//********************************************************************
//*
//* (C) COPYRIGHT IBM CORPORATION 2024.
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
//* BUILD COBOL IVP PROGRAM TESTUR70 TO CALL JAVA CLASS METHOD
//*********************************************************************
//*
//         EXPORT SYMLIST=*
//*
//         SET HLQ=<YOUR-TSO-PREFIX>
//         SET MLQ=GVBDEMOJ
//*
//*********************************************************************
//*   COMPILE AND LINK COBOL TEST PROGRAM TESTUR70 
//*********************************************************************
//*
//JOBLIB   DD DISP=SHR,DSN=GEBT.SYSXX.LOAD
//*
//*********************************************************************
//*   COMPILE COBOL PROGRAM
//*********************************************************************
//*
//COB      EXEC PGM=IGYCRCTL,
// PARM='LIB,APOST,ARITH(EXTEND),LIST',
// REGION=4096K
//*
//STEPLIB  DD DISP=SHR,DSN=CEE.SCEERUN
//         DD DISP=SHR,DSN=IGY.V4R2M0.SIGYCOMP
//*
//SYSIN    DD DSN=&HLQ..&MLQ..COBOL(TESTUR70),DISP=SHR
//*
//SYSLIB   DD DSN=&HLQ..&MLQ..COPY,DISP=SHR
//*
//SYSUT1   DD UNIT=SYSDA,SPACE=(CYL,(5,1),RLSE)
//SYSUT2   DD UNIT=SYSDA,SPACE=(CYL,(5,1),RLSE)
//SYSUT3   DD UNIT=SYSDA,SPACE=(CYL,(5,1),RLSE)
//SYSUT4   DD UNIT=SYSDA,SPACE=(CYL,(5,1),RLSE)
//SYSUT5   DD UNIT=SYSDA,SPACE=(CYL,(5,1),RLSE)
//SYSUT6   DD UNIT=SYSDA,SPACE=(CYL,(5,1),RLSE)
//SYSUT7   DD UNIT=SYSDA,SPACE=(CYL,(5,1),RLSE)
//*
//SYSLIN   DD DSN=&&OBJECT,
//            DISP=(NEW,PASS),
//            UNIT=SYSDA,
//            SPACE=(TRK,(25,10),RLSE),
//            RECFM=FB,LRECL=80,BLKSIZE=2960
//*
//SYSPRINT DD SYSOUT=*
//*
//SYSTERM  DD SYSOUT=*
//SYSUDUMP DD SYSOUT=*
//*
//*********************************************************************
//*   ABEND IF A PRIOR PROCESS HAS FAILED
//*
//*   NOTE: CHECK FOR ERROR MESSAGE ON THE PRIOR STEP
//*         FOR PROBLEM DETERMINATION
//*********************************************************************
//*
//ABNDCOB  EXEC PGM=GVBUT99,
//            PARM='1099',
//            COND=(8,GT,COB)
//**********************************************************************
//*  LINK-EDIT
//**********************************************************************
//*
//LINK     EXEC PGM=IEWL,
// PARM=(XREF,LET,LIST,MAP,AMODE(31),RMODE(ANY),REUS(RENT))
//*
//SYSLIN   DD DISP=SHR,DSN=&&OBJECT
//*
//SYSLIB   DD DISP=SHR,DSN=CEE.SCEERUN
//         DD DISP=SHR,DSN=CEE.SCEELKED
//         DD DISP=SHR,DSN=CEE.SCEELIB
//         DD DISP=SHR,DSN=SYS1.CSSLIB
//         DD DISP=SHR,DSN=SYS1.LINKLIB
//         DD DISP=SHR,DSN=DSN.V11R1M0.SDSNLOAD
//*
//SYSUT1   DD DSN=&&SYSUT1,
//            UNIT=SYSDA,
//            SPACE=(1024,(120,120),,,ROUND),
//            BUFNO=1
//*
//SYSLMOD  DD DSN=&HLQ..&MLQ..LOADLIB(TESTUR70),
//            DISP=SHR
//*
//SYSPRINT DD SYSOUT=*
//*
//*********************************************************************
//*   ABEND IF A PRIOR PROCESS HAS FAILED
//*
//*   NOTE: CHECK FOR ERROR MESSAGE ON THE PRIOR STEP
//*         FOR PROBLEM DETERMINATION
//*********************************************************************
//*
//ABNDLINK EXEC PGM=GVBUT99,
//            PARM='1099',
//            COND=(8,GT,LINK)
//
