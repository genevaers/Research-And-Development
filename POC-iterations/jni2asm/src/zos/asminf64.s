R0       EQU   0
R1       EQU   1
R2       EQU   2
R3       EQU   3
R4       EQU   4
R5       EQU   5
R6       EQU   6
R7       EQU   7
R8       EQU   8
R9       EQU   9
R10      EQU   10
R11      EQU   11
R12      EQU   12
R13      EQU   13
R14      EQU   14
R15      EQU   15
*
ASMINF64 CELQPRLG PARMWRDS=1,BASEREG=R10,EXPORT=YES
         LG    R9,0(R1)                R9 -> Input area
* -----------------------------------------------------------
* Check the value of our input area, and get Sysplex/Host
* as required
* -----------------------------------------------------------
         CLC   0(7,R9),=C'SYSPLEX'     Getting sysplex?
         BE    SYSPLEX                 Yes - Go get it
         CLC   0(4,R9),=C'HOST'        Getting sysplex?
         BE    HOST                    Yes - Go get it
         LA    R3,8                    Else Bad Return Code
         B     EXIT
* -----------------------------------------------------------
* Get the Host Name from the SMCA Control Block
* -----------------------------------------------------------
HOST     DS    0H
         LLGT  R2,CVTPTR               R1 -> CVT
         LLGT  R2,CVTSMCA-CVT(R2)      R2 -> SMCA
         MVC   0(L'SMCASID,R9),SMCASID-SMCABASE(R2) SMFID
         B     EXIT0                   And Exit
* -----------------------------------------------------------
* Get the Sysplex Name from the Extended CVT
* -----------------------------------------------------------
SYSPLEX  DS    0H
         LLGT  R2,CVTPTR               R1 -> CVT
         MVC   0(L'ECVTSPLX,R9),ECVTSPLX-ECVT(R2)   Sysplex
         B     EXIT0                   And Exit
* ----------------------------------------------------
* Zero Return Code and Exit
* ----------------------------------------------------
EXIT0    DS    0H
         XR    R3,R3                   Zero Return Code
EXIT     DS    0H
         CELQEPLG                      Return to caller
* -----------------------------------------------------------
* Mapping Macros and DSECTs
* -----------------------------------------------------------
         LTORG
         CVT DSECT=YES                 Map CVT
         IHAECVT                       Map ECVT
         IEESMCA                       Map SMCA
         END