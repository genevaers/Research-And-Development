//LNKUR70T  JOB (ACCT),'LINK JAVA PIECES',
//            NOTIFY=&SYSUID.,
//            CLASS=A,
//            MSGLEVEL=(1,1),
//            MSGCLASS=H
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
//*
//*        SET HLQ=<YOUR-TSO-PREFIX>
//*        SET MLQ=GVBDEMO
//*
//*********************************************************************
//*  LINK-EDIT GVBJLENV
//*********************************************************************
//*
//GVBJLENV EXEC PGM=IEWL,
// PARM=(XREF,LET,LIST,MAP,RMODE(SPLIT),HOBSET,AC(1),RENT,REUS)
//*
//SYSLIN   DD DISP=SHR,DSN=&LVL1..RTC&RTC..BTCHOBJ(GVBJLENV)
//         DD *
 NAME GVBJLENV(R)
//*
//SYSLIB   DD DISP=SHR,DSN=&LVL1..RTC&RTC..BTCHOBJ
//         DD DISP=SHR,DSN=CEE.SCEERUN
//         DD DISP=SHR,DSN=CEE.SCEELKED
//         DD DISP=SHR,DSN=CEE.SCEELIB
//         DD DISP=SHR,DSN=SYS1.CSSLIB
//         DD DISP=SHR,DSN=SYS1.LINKLIB
//*
//SYSUT1   DD DSN=&&SYSUT1,
//            UNIT=SYSDA,
//            SPACE=(1024,(120,120),,,ROUND),
//            BUFNO=1
//*
//SYSLMOD  DD DSN=&LVL1..RTC&RTC..GVBLOAD(GVBJLENV),
//            DISP=SHR
//*
//SYSPRINT DD SYSOUT=*
//*********************************************************************
//*  LINK-EDIT GVBUR70
//*********************************************************************
//*
//GVBUR70  EXEC PGM=IEWL,
// PARM=(XREF,LET,LIST,MAP,RMODE(SPLIT),HOBSET,AC(1),RENT,REUS)
//*
//SYSLIN   DD DISP=SHR,DSN=&LVL1..RTC&RTC..BTCHOBJ(GVBUR70)
//         DD *
 NAME GVBUR70(R)
//*
//SYSLIB   DD DISP=SHR,DSN=&LVL1..RTC&RTC..BTCHOBJ
//         DD DISP=SHR,DSN=CEE.SCEERUN
//         DD DISP=SHR,DSN=CEE.SCEELKED
//         DD DISP=SHR,DSN=CEE.SCEELIB
//         DD DISP=SHR,DSN=SYS1.CSSLIB
//         DD DISP=SHR,DSN=SYS1.LINKLIB
//*
//SYSUT1   DD DSN=&&SYSUT1,
//            UNIT=SYSDA,
//            SPACE=(1024,(120,120),,,ROUND),
//            BUFNO=1
//*
//SYSLMOD  DD DSN=&LVL1..RTC&RTC..GVBLOAD(GVBUR70),
//            DISP=SHR
//*
//SYSPRINT DD SYSOUT=*
//*
//*********************************************************************
//*  LINK-EDIT TSTUR70
//*********************************************************************
//*
//TSTUR70  EXEC PGM=IEWL,
// PARM=(XREF,LET,LIST,MAP,RMODE(SPLIT),HOBSET,AC(1),RENT,REUS)
//*
//SYSLIN   DD DISP=SHR,DSN=&LVL1..RTC&RTC..BTCHOBJ(TSTUR70)
//         DD *
 NAME TSTUR70(R)
//*
//SYSLIB   DD DISP=SHR,DSN=&LVL1..RTC&RTC..BTCHOBJ
//         DD DISP=SHR,DSN=CEE.SCEERUN
//         DD DISP=SHR,DSN=CEE.SCEELKED
//         DD DISP=SHR,DSN=CEE.SCEELIB
//         DD DISP=SHR,DSN=SYS1.CSSLIB
//         DD DISP=SHR,DSN=SYS1.LINKLIB
//*
//SYSUT1   DD DSN=&&SYSUT1,
//            UNIT=SYSDA,
//            SPACE=(1024,(120,120),,,ROUND),
//            BUFNO=1
//*
//SYSLMOD  DD DSN=&LVL1..RTC&RTC..GVBLOAD(TSTUR70),
//            DISP=SHR
//*
//SYSPRINT DD SYSOUT=*
//*
//*********************************************************************
//*  LINK-EDIT GVBJGO95
//*********************************************************************
//*
//GVBJGO95 EXEC PGM=IEWL,
// PARM=(XREF,LET,LIST,MAP,RMODE(SPLIT),HOBSET,AC(1),RENT,REUS)
//*
//SYSLIN   DD DISP=SHR,DSN=&LVL1..RTC&RTC..BTCHOBJ(GVBJGO95)
//         DD *
 NAME GVBJGO95(R)
//*
//SYSLIB   DD DISP=SHR,DSN=&LVL1..RTC&RTC..BTCHOBJ
//         DD DISP=SHR,DSN=CEE.SCEERUN
//         DD DISP=SHR,DSN=CEE.SCEELKED
//         DD DISP=SHR,DSN=CEE.SCEELIB
//         DD DISP=SHR,DSN=SYS1.CSSLIB
//         DD DISP=SHR,DSN=SYS1.LINKLIB
//*
//SYSUT1   DD DSN=&&SYSUT1,
//            UNIT=SYSDA,
//            SPACE=(1024,(120,120),,,ROUND),
//            BUFNO=1
//*
//SYSLMOD  DD DSN=&LVL1..RTC&RTC..GVBLOAD(GVBJGO95),
//            DISP=SHR
//*
//SYSPRINT DD SYSOUT=*
//*
//