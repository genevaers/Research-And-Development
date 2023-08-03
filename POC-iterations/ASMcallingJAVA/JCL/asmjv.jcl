//ASMUR70T  JOB (ACCT),'BUILD MR95 JAVA BITS',
//            NOTIFY=&SYSUID.,
//            CLASS=A,
//            MSGLEVEL=(1,1),
//            TIME=(0,45),
//            MSGCLASS=X
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
//****************************************************************
//*  ASSEMBLE GVBUR70, TSTUR70 AND GVBJLENV MODULES
//****************************************************************
//ASMP1    PROC
//ASM      EXEC PGM=ASMA90,
// PARM=(NODECK,OBJECT,ADATA,'SYSPARM(RELEASE)','OPTABLE(ZS7)',
// 'PC(GEN),FLAG(NOALIGN),SECTALGN(256),GOFF,LIST(133)')
//*
//SYSIN    DD DISP=SHR,DSN=&LVL1..RTC&RTC..ASM(&MEMBER)
//*
//SYSLIB   DD DISP=SHR,DSN=&LVL1..RTC&RTC..MAC
//         DD DISP=SHR,DSN=SYS1.MACLIB
//         DD DISP=SHR,DSN=SYS1.MODGEN
//         DD DISP=SHR,DSN=CEE.SCEEMAC
//*
//SYSLIN   DD DSN=&LVL1..RTC&RTC..BTCHOBJ(&MEMBER),
//            DISP=SHR
//*
//SYSUT1   DD DSN=&&SYSUT1,
//            UNIT=SYSDA,
//            SPACE=(1024,(300,300),,,ROUND),
//            BUFNO=1
//*
//SYSADATA DD DISP=SHR,DSN=&LVL1..RTC&RTC..SYSADATA(&MEMBER)
//*
//SYSPRINT DD SYSOUT=*
//*YSPRINT DD DSN=&LVL1..RTC&RTC..LISTASM(&MEMBER),
//*           DISP=SHR
//*
//*       E X T R A C T   S T E P
//*
//EXTRACT  EXEC PGM=ASMLANGX,PARM='&MEMBER (ASM LOUD ERROR'
//* ARM='GVBXLEU (ASM LOUD ERROR'
//STEPLIB  DD   DISP=SHR,DSN=ASM.SASMMOD2
//SYSADATA DD   DISP=SHR,DSN=&LVL1..RTC&RTC..SYSADATA(&MEMBER)
//ASMLANGX DD   DISP=SHR,DSN=&LVL1..RTC&RTC..ASMLANGX
//*
//         PEND
//*
//ASMUR70  EXEC ASMP1,MEMBER=GVBUR70
//ASMUR70T EXEC ASMP1,MEMBER=TSTUR70
//ASMJLENV EXEC ASMP1,MEMBER=GVBJLENV
//ASMJGO95 EXEC ASMP1,MEMBER=GVBJGO95
//