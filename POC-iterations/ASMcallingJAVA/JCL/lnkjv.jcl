//NCBLNKJ  JOB (ACCT),'LINK JAVA I/F',
//            NOTIFY=&SYSUID.,
//            CLASS=A,
//            MSGLEVEL=(1,1),
//            MSGCLASS=H
//*
//*        SET HLQ=<YOUR-TSO-PREFIX>
//*        SET MLQ=GVBDEMO
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
//*********************************************************************
//*  LINK-EDIT GVBJ2ENV
//*********************************************************************
//*
//GVBJLENV EXEC PGM=IEWL,
// PARM=(XREF,LET,LIST,MAP,RMODE(SPLIT),HOBSET,AC(1),RENT,REUS)
//*
//SYSLIN   DD DISP=SHR,DSN=&LVL1..RTC&RTC..BTCHOBJ(GVBJ2ENV)
//         DD *
 NAME GVBJ2ENV(R)
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
//SYSLMOD  DD DSN=&LVL1..RTC&RTC..GVBLOAD(GVBJ2ENV),
//            DISP=SHR
//*
//SYSPRINT DD SYSOUT=*
//