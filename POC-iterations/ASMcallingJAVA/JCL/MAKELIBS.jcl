//CPUSSMVS JOB (ACCT),CLASS=A,MSGCLASS=X,MSGLEVEL=(1,1),NOTIFY=&SYSUID
//*
//         EXPORT SYMLIST=*
//         SET HLQ=<YOUR-TSO-PREFIX>
//         SET MLQ=GVBDEMOJ
//*
//BR14     EXEC PGM=IEFBR14
//ASM      DD  DSN=&HLQ..&MLQ..ASM,
//             DISP=(NEW,CATLG,DELETE),
//             DSNTYPE=LIBRARY,
//             UNIT=SYSDA,
//             SPACE=(TRK,(20,10)),
//             DCB=(DSORG=PO,RECFM=FB,LRECL=80)
//*
//BTCHOBJ  DD  DSN=&HLQ..&MLQ..BTCHOBJ,
//             DISP=(NEW,CATLG,DELETE),
//             DSNTYPE=LIBRARY,
//             UNIT=SYSDA,
//             SPACE=(TRK,(10,5)),
//             DCB=(DSORG=PO,RECFM=FB,LRECL=80)
//*
//COBOL    DD  DSN=&HLQ..&MLQ..COBOL,
//             DISP=(NEW,CATLG,DELETE),
//             DSNTYPE=LIBRARY,
//             UNIT=SYSDA,
//             SPACE=(TRK,(10,5)),
//             DCB=(DSORG=PO,RECFM=FB,LRECL=80)
//*
//COPY     DD  DSN=&HLQ..&MLQ..COPY,
//             DISP=(NEW,CATLG,DELETE),
//             DSNTYPE=LIBRARY,
//             UNIT=SYSDA,
//             SPACE=(TRK,(10,5)),
//             DCB=(DSORG=PO,RECFM=FB,LRECL=80)
//*
//EXP      DD  DSN=&HLQ..&MLQ..EXP,
//             DISP=(NEW,CATLG,DELETE),
//             DSNTYPE=LIBRARY,
//             UNIT=SYSDA,
//             SPACE=(TRK,(10,5)),
//             DCB=(DSORG=PO,RECFM=FB,LRECL=80)
//*
//JCL      DD  DSN=&HLQ..&MLQ..JCL,
//             DISP=(NEW,CATLG,DELETE),
//             DSNTYPE=LIBRARY,
//             UNIT=SYSDA,
//             SPACE=(TRK,(20,10)),
//             DCB=(DSORG=PO,RECFM=FB,LRECL=80)
//*
//LOAD     DD  DSN=&HLQ..&MLQ..LOADLIB,
//             DISP=(NEW,CATLG,DELETE),
//             DSNTYPE=LIBRARY,
//             UNIT=SYSDA,
//             SPACE=(TRK,(50,10)),
//             DCB=(DSORG=PO,RECFM=U,BLKSIZE=4096)
//*
//MAC      DD  DSN=&HLQ..&MLQ..MACLIB,
//             DISP=(NEW,CATLG,DELETE),
//             DSNTYPE=LIBRARY,
//             UNIT=SYSDA,
//             SPACE=(TRK,(10,5)),
//             DCB=(DSORG=PO,RECFM=FB,LRECL=80)
//*
//SYSTSIN  DD  DSN=&HLQ..&MLQ..SYSTSIN,
//             DISP=(NEW,CATLG,DELETE),
//             DSNTYPE=LIBRARY,
//             UNIT=SYSDA,
//             SPACE=(TRK,(10,5)),
//             DCB=(DSORG=PO,RECFM=FB,LRECL=160)
//*                                               
//SYSADATA DD  DSN=&HLQ..&MLQ..SYSADATA,          
//             DISP=(NEW,CATLG,DELETE),           
//             DSNTYPE=LIBRARY,                   
//             UNIT=SYSDA,                        
//             SPACE=(TRK,(10,5)),                
//             DCB=(DSORG=PO,RECFM=VB,LRECL=32756)
//*                                               
//ASMLANGX DD  DSN=&HLQ..&MLQ..ASMLANGX,           
//             DISP=(NEW,CATLG,DELETE),           
//             DSNTYPE=LIBRARY,                   
//             UNIT=SYSDA,                        
//             SPACE=(TRK,(10,5)),                
//             DCB=(DSORG=PO,RECFM=VB,LRECL=8192) 
//