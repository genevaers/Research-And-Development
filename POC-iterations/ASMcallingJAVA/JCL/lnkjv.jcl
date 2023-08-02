//NLNKUR70  JOB (ACCT),'LINK JAVA BITS',
//            NOTIFY=&SYSUID.,
//            CLASS=A,
//            MSGLEVEL=(1,1),
//            MSGCLASS=H
//*
//*        SET HLQ=<YOUR-TSO-PREFIX>
//*        SET MLQ=GVBDEMO
//*
//*********************************************************************
//*  LINK-EDIT GVBJ2ENV
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