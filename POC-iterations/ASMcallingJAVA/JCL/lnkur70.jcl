//NCBLNKJ  JOB (ACCT),'LINK JAVA STUFF',                                JOB22334
//            NOTIFY=&SYSUID.,
//            CLASS=A,
//            MSGLEVEL=(1,1),
//            MSGCLASS=H
//*
//         SET LVL1=GEBT
//         SET RTC=22964
//*
//*********************************************************************
//*  LINK-EDIT GVBUR70
//*********************************************************************
//*
//GVBUR70  EXEC PGM=IEWL,
// PARM=(XREF,LET,LIST,MAP,RMODE(SPLIT),HOBSET,AC(1))
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
