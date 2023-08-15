         TITLE    'Establish Communications Tensor Table'
***********************************************************************
*
* (c) Copyright IBM Corporation 2023.
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
*   This module is invoked by the GvbJavaDaemon via JNI to allocate
*   the Communications Tensor Table (header only) and create a name
*   token pair so other components can find it.
*
***********************************************************************
*
         IHASAVER DSECT=YES,SAVER=YES,SAVF4SA=YES,SAVF5SA=YES,TITLE=NO
*
         YREGS
*
*        COPY  GVBJDSCT
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
         DS    XL1
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
*        COMMUNICATIONS TENSOR TABLE DSECTS
*
CTTAREA  DSECT
CTTEYE   DS    CL8
CTTACTR  DS    A               ADDR CTRAREA
CTTNUME  DS    H               NUMBER OF ENTRIES
CTTACTIV DS    X
         DS    X
CTTTECB  DS    F               TERMINATION ECB
CTTGECB  DS    F               GO ECB
CTTGECB2 DS    F               Acknowledge GO
         DS    XL4
CTTLEN   EQU   *-CTTAREA
*
*
CTRAREA  DSECT
CTRECB1  DS    F               ECB JAVA WORKER WAITS ON
CTRECB2  DS    F               ECB ASM  WORKER WAITS ON
CTRCSWRD DS    F               CS CONTROL WORD
CTRREQ   DS    CL4             REQUEST FUNCTION
CTRACLSS DS    D               ADDRESS OF CLASS FIELD (A32) 
CTRAMETH DS    D               ADDRESS OF METHOD FIELD (A32)
CTRLENIN DS    D               LENGTH INPUT AREA
CTRLENOUT DS   D               LENGTH OUTPUT AREA
CTRMEMIN DS    D               ADDR INPUT AREA
CTRMEMOUT DS   D               ADDR OUTPUT AREA
CTRTHRDN DS    H
         DS    XL2
CTRUR70W DS    XL4             Pointer to GVBUR70 workarea
         DS    XL8
CTRLEN   EQU   *-CTRAREA
*
*
PARMSTR  DSECT
PAFUN    DS    CL8
PAOPT    DS    CL8
PACLASS  DS    CL32           
PAMETHOD DS    CL32
PALEN1   DS    D
PALEN2   DS    D
PAADDR1  DS    D
PAADDR2  DS    D
PARETC   DS    D
PAANCHR  DS    D
PARMLEN  EQU   *-PARMSTR
*
*
GVBJMAIN RMODE 24
GVBJMAIN AMODE 31
*
GVBJMAIN CSECT
*
*        ENTRY LINKAGE
*
         STMG  R14,R12,SAVF4SAG64RS14-SAVF4SA(R13)
         LGR   R9,R1                     Parameter structure
         LLGTR R12,R15                   ESTABLISH ...
         USING GVBJMAIN,R12               ... ADDRESSABILITY
*
         GETMAIN R,LV=DYNLEN             GET DYNAMIC STORAGE
         LR    R11,R1                    MOVE GETMAINED ADDRESS TO R11
         USING DYNAREA,11                ADDRESSABILITY TO DSECT
         STG   R13,SAVER13               SAVE CALLER SAVE AREA ADDRESS
         LAY   R15,SAVEAREA              GET ADDRESS OF OWN SAVE AREA
         STG   R15,SAVF4SANEXT-SAVF4SA(,R13) STORE IN CALLER SAVE AREA
         LLGTR R13,R15                   GET ADDRESS OF OWN SAVE AREA
*
         XC    WKRETC,WKRETC
*
*      OPEN MESSAGE FILE
         J     MAIN_096
         LA    R14,OUTFILE               COPY MODEL   DCB
D1       USING IHADCB,OUTDCB
         MVC   OUTDCB(OUTFILEL),0(R14)
         LAY   R0,OUTDCB                 SET  DCBE ADDRESS IN  DCB
         AGHI  R0,OUTFILE0
         STY   R0,D1.DCBDCBE
*
         LAY   R2,OUTDCB
         MVC   WKREENT(8),OPENPARM
         OPEN  ((R2),(EXTEND)),MODE=31,MF=(E,WKREENT)
         TM    48(R2),X'10'              SUCCESSFULLY OPENED  ??
         JO    MAIN_096                  YES - BYPASS ABEND
         WTO 'GVBJMAIN: DDPRINT OPEN FAILED'
         MVC   WKRETC,=F'16'
         J     DONEDONE
*
***********************************************************************
* INITIALIZE CTT AREA                                                 *
***********************************************************************
*
*
MAIN_096 EQU   *
         LGHI  R0,CTRLEN
         MH    R0,=H'99'
         GETMAIN RU,LV=(0),LOC=(ANY)
         LR    R5,R1
         LR    R8,R1
         USING CTRAREA,R5
         LA    R2,99
MAIN_120 EQU   *
         XC    0(CTRLEN,R5),0(R5)
         LA    R5,CTRLEN(,R5)
         BRCT  R2,MAIN_120
         DROP  R5 CTRAREA
*
         LGHI  R0,CTTLEN
         GETMAIN RU,LV=(0),LOC=(ANY)
         ST    R1,WKTOKNCTT
         LLGTR R4,R1
         USING CTTAREA,R4
         MVC   CTTEYE,CTTEYEB
         ST    R8,CTTACTR
         MVC   CTTNUME,=H'99'
         XC    CTTACTIV(2),CTTACTIV
         XC    CTTTECB,CTTTECB
         XC    CTTGECB,CTTGECB
         XC    CTTGECB2,CTTGECB2
*
***********************************************************************
*  CREATE GLOBAL NAME/TOKEN AREA                                      *
***********************************************************************
         XC    WKTOKNRC,WKTOKNRC
         MVC   WKTOKNAM+0(8),GENEVA
         MVC   WKTOKNAM+8(8),TKNNAME
         CALL  IEANTCR,(TOKNLVL2,WKTOKNAM,WKTOKN,TOKNPERS,WKTOKNRC),   +
               MF=(E,WKREENT)
         LTGF  R15,WKTOKNRC       SUCCESSFUL  ???
         JZ    MAIN_140
         WTO 'GVBJMAIN: COMMUNICATIONS TENSOR TABLE NOT ESTABLISHED'
         MVC   WKRETC,=F'20'
         J     DONE
***
*
MAIN_140 EQU   *
         sam64
         sysstate amode64=YES
         USING PARMSTR,R9
         STG   R4,PAANCHR                Address of GVBJXXX
         sysstate amode64=NO
         sam31
         DROP  R4 CTTAREA
         WTO 'GVBJMAIN: COMMUNICATIONS TENSOR TABLE ESTABLISHED'
*
*
*        RETURN TO CALLER
*
DONE     EQU   *                         RETURN TO CALLER
         J     DONEDONE
         LAY   R2,OUTDCB
         MVC   WKREENT(8),OPENPARM
         CLOSE ((R2)),MODE=31,MF=(E,WKREENT)
*
DONEDONE EQU   *                         RETURN TO CALLER
         LG    13,SAVER13                CALLER'S SAVE AREA ADDRESS
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
CTTEYEB  DC    CL8'GVBCTT'
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
