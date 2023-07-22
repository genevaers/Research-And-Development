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
//*  LINK-EDIT GVBJMR95
//*********************************************************************
//*
//GVBJMR95 EXEC PGM=IEWL,
// PARM=(XREF,LET,LIST,MAP,RMODE(SPLIT),HOBSET,AC(1),RENT,REUS)
//*
//SYSLIN   DD DISP=SHR,DSN=&LVL1..RTC&RTC..BTCHOBJ(GVBJMR95)
//         DD *
 NAME GVBJMR95(R)
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
//SYSLMOD  DD DSN=&LVL1..RTC&RTC..GVBLOAD(GVBJMR95),
//            DISP=SHR
//*
//SYSPRINT DD SYSOUT=*
//*
//*********************************************************************
//*  LINK-EDIT GVBJWAIT
//*********************************************************************
//*
//GVBJWAIT EXEC PGM=IEWL,
// PARM=(XREF,LET,LIST,MAP,RMODE(SPLIT),HOBSET,AC(1),RENT,REUS)
//*
//SYSLIN   DD DISP=SHR,DSN=&LVL1..RTC&RTC..BTCHOBJ(GVBJWAIT)
//         DD *
 NAME GVBJWAIT(R)
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
//SYSLMOD  DD DSN=&LVL1..RTC&RTC..GVBLOAD(GVBJWAIT),
//            DISP=SHR
//*
//SYSPRINT DD SYSOUT=*
//*
//*********************************************************************
//*  LINK-EDIT GVBJPOST
//*********************************************************************
//*
//GVBJPOST EXEC PGM=IEWL,
// PARM=(XREF,LET,LIST,MAP,RMODE(SPLIT),HOBSET,AC(1),RENT,REUS)
//*
//SYSLIN   DD DISP=SHR,DSN=&LVL1..RTC&RTC..BTCHOBJ(GVBJPOST)
//         DD *
 NAME GVBJPOST(R)
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
//SYSLMOD  DD DSN=&LVL1..RTC&RTC..GVBLOAD(GVBJPOST),
//            DISP=SHR
//*
//SYSPRINT DD SYSOUT=*
//*
//*********************************************************************
//*  LINK-EDIT GVBJMAIN
//*********************************************************************
//*
//GVBJMAIN EXEC PGM=IEWL,
// PARM=(XREF,LET,LIST,MAP,RMODE(SPLIT),HOBSET,AC(1),RENT,REUS)
//*
//SYSLIN   DD DISP=SHR,DSN=&LVL1..RTC&RTC..BTCHOBJ(GVBJMAIN)
//         DD *
 NAME GVBJMAIN(R)
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
//SYSLMOD  DD DSN=&LVL1..RTC&RTC..GVBLOAD(GVBJMAIN),
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
//*********************************************************************
//*  LINK-EDIT GVBJLENV
//*********************************************************************
//*
//GVBJLENV EXEC PGM=IEWL,
// PARM=(XREF,LET,LIST,MAP,RMODE(SPLIT),HOBSET,AC(1),RENT,REUS)
//*
//SYSLIN   DD DISP=SHR,DSN=&LVL1..RTC&RTC..BTCHOBJ(GVBJ2ENV)
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
//*
//*********************************************************************
//*  LINK-EDIT ASMADA8
//*********************************************************************
//*
//ASMADA8  EXEC PGM=IEWL,
// PARM=(XREF,LET,LIST,MAP,RMODE(SPLIT),HOBSET,REUS)
//*
//SYSLIN   DD DISP=SHR,DSN=&LVL1..RTC&RTC..BTCHOBJ(ASMADA8)
//         DD *
 NAME ASMADA8(R)
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
//SYSLMOD  DD DSN=&LVL1..RTC&RTC..GVBLOAD(ASMADA8),
//            DISP=SHR
//*
//SYSPRINT DD SYSOUT=*
//*
