         TITLE    'TELL JAVA HOW MANY THREADS FOR MR95 AND PROCEED'
***********************************************************************
*
* (c) Copyright IBM Corporation 2024.
*     Copyright Contributors to the GenevaERS Project.
* SPDX-License-Identifier: Apache-2.0
*
***********************************************************************
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
*   or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
***********************************************************************
*
* This module is called by GVBMR95 just after the lines shown below
* to indicate to the GvbJavaDaemon how many threads it should start
* in order to service requests made by MR95 threads.
*
* * DISPLAY "THREADS STARTED" MESSAGE                                 
* *********************************************************************
*         LH    R0,THRDCNT         INDICATE HOW   MANY THREADS STARTED
*         L     R14,EXECDADR       LOAD PARAMETER DATA ADDRESS
*         USING EXECDATA,R14
*         if CLI,EXECSNGL,eq,C'1',or,    FIRST THREAD   ONLY MODE  ???
*               CLI,EXECSNGL,eq,C'A'     or ONE   THREAD   MODE ???
*           LHI R0,1               YES - CHANGE  COUNT TO  1
*           STH R0,THRDCNT
*         endif
*         DROP  R14
*         CVD   R0,DBLWORK
*         OI    DBLWORK+L'DBLWORK-1,X'0F'
*         UNPK  errdata(3),DBLWORK
*
***********************************************************************
         IHASAVER DSECT=YES,SAVER=YES,SAVF4SA=YES,SAVF5SA=YES,TITLE=NO
*
         YREGS
*
         COPY  GVBJDSCT
*
*        DYNAMIC WORK AREA
*
DYNAREA  DSECT
*
SAVEAREA DC    18F'0'
SAVER13  DS    D
*
         DS    0F
OUTDCB   DS    XL(OUTFILEL)    REENTRANT DCB AND DCBE AREAS
*
WKPRINT  DS    XL131           PRINT LINE
WKJFLAG  DS    XL1
         DS   0F
WKREENT  DS    XL256           REENTRANT WORKAREA/PARMLIST
WKDBLWK  DS    XL08            DOUBLE WORK WORKAREA
WKDDNAME DS    CL8
WKRETC   DS    F
GVBMR95E DS    A
WKTOKNRC DS    A                  NAME/TOKEN  SERVICES RETURN CODE
WKTOKNAM DS    XL16               TOKEN NAME
WKTOKN   DS   0XL16               TOKEN VALUE
WKTOKNCTT DS   A                  A(CTT)
         DS    A
         DS    A
         DS    A
WKECBLST DS    A                  ADDRESS OF ECB LIST TO WAIT ON
WKECB1   DS    F
WKECB2   DS    F
WKECB3   DS    F
WKECB4   DS    F
WKECBNUM DS    H                  Number in list
WKECBLSZ DS    H
WKENTIDX DS    A
         DS    A
         DS    0F
DYNLEN   EQU   *-DYNAREA                 DYNAMIC AREA LENGTH
*
*
GVBJGO95 RMODE 24
GVBJGO95 AMODE 31
GVBJGO95 CSECT
*
*        ENTRY LINKAGE
*
         STMG  R14,R12,SAVF4SAG64RS14-SAVF4SA(R13)
         LLGTR R12,R15                   ESTABLISH ...
         USING GVBJGO95,R12              ... ADDRESSABILITY
         LLGTR R9,R1
         USING PARMSTR,R9
         LH    R0,PAOPT                  NUMBER OF THREADS
*        dc h'0'
         LR    R9,R0
*        wto 'GVBJGO95: entered'
*
         GETMAIN R,LV=DYNLEN             GET DYNAMIC STORAGE
         LR    R11,R1                    MOVE GETMAINED ADDRESS TO R11
         USING DYNAREA,11                ADDRESSABILITY TO DSECT
         STG   R13,SAVER13               SAVE CALLER SAVE AREA ADDRESS
         LAY   R15,SAVEAREA              GET ADDRESS OF OWN SAVE AREA
         STG   R15,SAVF4SANEXT-SAVF4SA(,R13) STORE IN CALLER SAVE AREA
         LLGTR R13,R15                   GET ADDRESS OF OWN SAVE AREA
*
***********************************************************************
*  FIND GLOBAL NAME/TOKEN AREA                                        *
***********************************************************************
         MVC   WKTOKNAM+0(8),GENEVA
         MVC   WKTOKNAM+8(8),TKNNAME
         CALL  IEANTRT,(TOKNLVL2,WKTOKNAM,WKTOKN,WKTOKNRC),            X
               MF=(E,WKREENT)
         LTGF  R15,WKTOKNRC       SUCCESSFUL  ???
         JZ    MAIN_140
         WTO 'GVBJGO95: COMMUNICATIONS TENSOR TABLE NOT LOCATED'
         MVC   WKRETC,=F'8'
         J     DONE
*
MAIN_140 EQU   *
         LLGT  R4,WKTOKNCTT
         USING CTTAREA,R4
         CLC   CTTEYE,CTTEYEB
         JE    MAIN_114
         WTO 'GVBJGO95: COMMUNICATIONS TENSOR TABLE DOES NOT MATCH'
         MVC   WKRETC,=F'12'
         J     DONE
*
*        ALLOCATE TABLE for REQUEST communication: CTRAREA
*
MAIN_114 EQU   *
         WTO 'GVBJGO95: COMMUNICATIONS TENSOR TABLE LOCATED'
         STH   R9,CTTNUME                FROM NBR MR95 THREADS
         L     R5,CTTACTR
         USING CTRAREA,R5
         LH    R2,CTTNUME
*        WTO 'GVBJGO95: SETTING UP CTR'
*
         XR    R1,R1                     THREAD COUNTER
MAIN_120 EQU   *
         AGHI  R1,1
         XC    CTRECB1,CTRECB1           CLEAR EVERYTHING
         XC    CTRECB2,CTRECB2
         XC    CTRCSWRD,CTRCSWRD
         XC    CTRREQ,CTRREQ
         XC    CTRMEMIN,CTRMEMIN
         XC    CTRMEMOUT,CTRMEMOUT
         STH   R1,CTRTHRDN               SET THREAD NUMBER
         LA    R5,CTRLEN(,R5)
         BRCT  R2,MAIN_120
         DROP  R5 CTRAREA
*        WTO 'GVBJGO95: CTR COMPLETED'
*
         POST  CTTGECB                   POST THE GO ECB
*        WTO 'GVBJGO95: POSTED FOR GO: NUMBER THREADS SET'
*
         WAIT  ECB=CTTGECB2              Wait for acknowledgement
         XC    CTTGECB2,CTTGECB2
*        WTO 'GVBJGO95: ACKNOWLEDGEMENT RECEIVED AND GECB2 RESET'
         MVI   CTTACTIV,X'FF'
         WTO 'GVBJGO95: COMMUNICATIONS TENSOR TABLE ACTIVATED'
         J     A0180
*
A0180    EQU   *
         DROP  R4 CTTAREA
*
         XC    WKRETC,WKRETC
*
*        RETURN TO CALLER
*
DONE     EQU   *                         RETURN TO CALLER
*        wto 'GVBJGO95: RETURNING'
         LG    R13,SAVER13               CALLER'S SAVE AREA ADDRESS
         L     R15,WKRETC
         STG   R15,SAVF4SAG64RS15-SAVF4SA(,R13)
         FREEMAIN R,LV=DYNLEN,A=(11)     FREE DYNAMIC STORAGE
         LMG   R14,R12,SAVF4SAG64RS14-SAVF4SA(R13)
         BR    R14                       RETURN TO CALLER
*
         DS    0D
MVCR14R1 MVC   0(0,R14),0(R1)     * * * * E X E C U T E D * * * *
         DS    0D
CLCR1R14 CLC   0(0,R1),0(R14)     * * * * E X E C U T E D * * * *
*
*
*        STATICS
*
*
*        CONSTANTS
*
H1       DC    H'1'
H4       DC    H'4'
H255     DC    H'255'
F04      DC    F'04'
F40      DC    F'40'
F4096    DC    F'4096'
CTTEYEB  DC    CL8'GVBCTTAB'
TKNNAME  DC    CL8'GVBJMR95'
GENEVA   DC    CL8'GENEVA'
TOKNPERS DC    F'0'                    TOKEN PERSISTENCE
TOKNLVL1 DC    A(1)                    NAME/TOKEN  AVAILABILITY  LEVEL
TOKNLVL2 DC    A(2)                    NAME/TOKEN  AVAILABILITY  LEVEL
*
         DS   0D
MODE31   EQU   X'8000'
         DS   0D
OPENPARM DC    XL8'8000000000000000'
*
OUTFILE  DCB   DSORG=PS,DDNAME=DDPRINT,MACRF=(PM),DCBE=OUTFDCBE,       X
               RECFM=FB,LRECL=131
OUTFILE0 EQU   *-OUTFILE
OUTFDCBE DCBE  RMODE31=BUFF
OUTFILEL EQU   *-OUTFILE
*
SPACES   DC    CL256' '
XHEXFF   DC 1024X'FF'
*
*
         LTORG ,
*
NUMMSK   DC    XL12'402020202020202020202021'
*
*******************************************************
*                 UNPACKED NUMERIC TRANSLATION MATRIX
*******************************************************
*                    0 1 2 3 4 5 6 7 8 9 A B C D E F
*
TRTACLVL DC    XL16'00D500000000000000C5000000000000'  00-0F
         DC    XL16'D9000000000000000000000000000000'  10-1F
         DC    XL16'E4000000000000000000000000000000'  20-2F
         DC    XL16'00000000000000000000000000000000'  30-3F
         DC    XL16'C3000000000000000000000000000000'  40-4F
         DC    XL16'00000000000000000000000000000000'  50-5F
         DC    XL16'00000000000000000000000000000000'  60-6F
         DC    XL16'00000000000000000000000000000000'  70-7F
         DC    XL16'C1000000000000000000000000000000'  80-8F
         DC    XL16'00000000000000000000000000000000'  90-9F
         DC    XL16'00000000000000000000000000000000'  A0-AF
         DC    XL16'00000000000000000000000000000000'  B0-BF
         DC    XL16'00000000000000000000000000000000'  C0-CF
         DC    XL16'00000000000000000000000000000000'  D0-DF
         DC    XL16'00000000000000000000000000000000'  E0-EF
         DC    XL16'00000000000000000000000000000000'  F0-FF
*
TRTTBLU  DC    XL16'08080808080808080808080808080808'  00-0F
         DC    XL16'08080808080808080808080808080808'  10-1F
         DC    XL16'08080808080808080808080808080808'  20-2F
         DC    XL16'08080808080808080808080808080808'  30-3F
         DC    XL16'08080808080808080808080808080808'  40-4F
         DC    XL16'08080808080808080808080808080808'  50-5F
         DC    XL16'08080808080808080808080808080808'  60-6F
         DC    XL16'08080808080808080808080808080808'  70-7F
         DC    XL16'08080808080808080808080808080808'  80-8F
         DC    XL16'08080808080808080808080808080808'  90-9F
         DC    XL16'08080808080808080808080808080808'  A0-AF
         DC    XL16'08080808080808080808080808080808'  B0-BF
         DC    XL16'08080808080808080808080808080808'  C0-CF
         DC    XL16'08080808080808080808080808080808'  D0-DF
         DC    XL16'08080808080808080808080808080808'  E0-EF
         DC    XL16'00000000000000000000080808080808'  F0-FF
*
         DS   0F
         DCBD  DSORG=PS
*
         IHADCBE
*
JFCBAR   DSECT
         IEFJFCBN LIST=YES
*
         CVT   DSECT=YES
*
         IHAPSA
*
         END
