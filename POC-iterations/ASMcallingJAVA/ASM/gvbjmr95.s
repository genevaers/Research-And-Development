         TITLE    'INVOKE GVBMR95 '
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
*   This module is called by GvbJavaDaemon to load and execute the
*   specified load module (GVBMR95) and wait for its completion
*   before returning.
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
SAVEAREA DS  18FD              64 bit SAVE AREA
SAVER13  DS    D
SAVER9   DS    D
*
WKDBL3   DS    XL08
         DS    0F
OUTDCB   DS    A               Address of 24 bit getmained DCB (output)
INDCB    DS    A               Address of 24 bit getmained DCB (input)
*
WKCARD   DS   3CL80            PARM CARD
WKPRINT  DS    XL131           PRINT LINE
WKSTAT   DS    XL1
QSTART   EQU   X'80'           Quote started/off if end quote found
         DS   0F
WKREENT  DS    XL256           REENTRANT WORKAREA/PARMLIST
WKDBLWK  DS    XL08            DOUBLE WORK WORKAREA
WKDDEXEC DS    CL8
WKEPARMA DS    A
WKDDPRML DS    H
WKDDPARM DS    CL30
WKRETC   DS    F
GVBMR95E DS    A
WKTOKNRC DS    A                  NAME/TOKEN  SERVICES RETURN CODE
WKTOKNAM DS    XL16               TOKEN NAME
WKTOKN   DS   0XL16               TOKEN VALUE
WKTOKNCTT DS   A                  A(CTT) reserved 16 bytes
         DS    A
         DS    A
         DS    A
WKEXECRC DS    F
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
CTRRETC  DS    XL2
CTRUR70W DS    XL4             Pointer to GVBUR70 workarea
         DS    XL8
CTRLEN   EQU   *-CTRAREA
*
*
PARMSTR  DSECT                         Call control block
PAFUN    DS    CL8                     Function code
PAOPT    DS    CL8                     Option(s)
PATHREAD DS    CL10                    Name of Java thread
PAFLAG1  DS    X
PAFLAG2  DS    X
PASPARE  DS    XL52
PALEN1   DS    D                       Length of data sent from ASM
PALEN2   DS    D                       Length of data received by ASM
PAADDR1  DS    D                       Address of data sent
PAADDR2  DS    D                       Address of data received
PARETC   DS    D                       Return code
PAANCHR  DS    D                       Communications Tensor Table addr
PAATMEM  DS    D                       Thread local 31 bit storage
PAGENPA  DS    10D                     Genparm if called by GVBMR95
PARMLEN  EQU   *-PARMSTR
*
*
GVBJMR95 RMODE 24
GVBJMR95 AMODE 31
*
GVBJMR95 CSECT
*
*        ENTRY LINKAGE
*
         STMG  R14,R12,SAVF4SAG64RS14-SAVF4SA(R13)
         LLGTR R12,R15                   ESTABLISH ...
         USING GVBJMR95,R12              ... ADDRESSABILITY
*
         LGR   R9,R1                     => Parmstr
         USING PARMSTR,R9
*
         LG    R0,PAATMEM                CLEAR MEMORY
         LGHI  R1,DYNLEN
         XGR   R14,R14
         XGR   R15,R15
         MVCL  R0,R14
         LG    R11,PAATMEM
         USING DYNAREA,R11
         STG   R13,SAVER13               CHAIN BACK
         LAY   R15,SAVEAREA
         STG   R15,SAVF4SANEXT-SAVF4SA(,R13) CHAIN FORWARD
         LGR   R13,R11                   NEW SVA
         LG    R0,PAATMEM 
         AGHI  R0,DYNLEN                 PUSH
         STG   R0,PAATMEM
*
         STG   R9,SAVER9                 keep: in order to be careful
*
         sysstate amode64=NO
         sam31
*
         GETMAIN R,LV=INFILEL,LOC=24
         ST    R1,INDCB
         GETMAIN R,LV=OUTFILEL,LOC=24
         ST    R1,OUTDCB
*
*      OPEN DDPARM FILE
         LA    R14,INFILE                COPY MODEL   DCB
         LLGT  R2,INDCB
         MVC   0(INFILEL,R2),0(R14)
         USING IHADCB,R2
         LR    R0,R2                     SET  DCBE ADDRESS IN  DCB
         AGHI  R0,INFILE0
         STY   R0,DCBDCBE
*
         MVC   WKREENT(8),OPENPARM
         OPEN  ((R2),(INPUT)),MODE=31,MF=(E,WKREENT)
         TM    48(R2),X'10'              SUCCESSFULLY OPENED  ??
         JO    MAIN_090                  YES - BYPASS ABEND
         WTO 'GVBJMR95: DDEXEC OPEN FAILED'
         MVC   WKRETC,=F'24'
         J     DONEDONE
MAIN_090 EQU   *
         LA    R4,3
         LA    R5,WKCARD
MAIN_091 EQU   *
         LLGT  R2,INDCB
         LA    R0,WKCARD
         GET   (R2),(R5)
         LA    R5,80(,R5)
         BCT   R4,MAIN_091
MAIN_092 EQU   *
         LLGT  R2,INDCB
         MVC   WKREENT(8),OPENPARM
         CLOSE ((R2)),MODE=31,MF=(E,WKREENT)
*        wto 'GVBJMR95: DDEXEC CARDS READ'
         DROP  R2 IHADCB
*
         CLC   WKCARD(4),=CL4'PGM='
         JE    MAIN_093
         WTO  'GVBJMR95: EXEC CARD NOT FOUND FOR DDEXEC' 
         MVC   WKRETC,=F'20'
         J     DONEDONE
*
MAIN_093 EQU   *
         MVC   WKDDEXEC,SPACES
         MVC   WKDDPARM,SPACES
         XC    WKDDPRML,WKDDPRML
         LA    R1,WKCARD+4
         LA    R2,8                      char max
         LA    R15,WKDDEXEC
MAIN_094 EQU   *
         CLI   0(R1),C' '
         JE    MAIN_098
         CLI   0(R1),0
         JE    MAIN_098
         CLI   0(R1),C','
         JE    MAIN_095
         MVC   0(1,R15),0(R1)
         LA    R15,1(,R15)
         LA    R1,1(,R1)
         BRCT  R2,MAIN_094
*
MAIN_095 EQU   *
         CLC   0(6,R1),=CL6',PARM='
         JNE   MAIN_098
         LA    R1,6(,R1)
         LA    R2,30                     char max
         LA    R15,WKDDPARM
         XR    R0,R0
MAIN_096 EQU   *
         CLI   0(R1),X'7D'               and I quote...
         JNE   MAIN096C
         LA    R1,1(,R1)                 skip and note..
MAIN096A EQU   *
         TM    WKSTAT,QSTART             used as toggle to ignore
         JO    MAIN096B                  comma..
         OI    WKSTAT,QSTART
         J     MAIN096C
MAIN096B EQU   *
         NI    WKSTAT,255-QSTART
MAIN096C EQU   *
         CLI   0(R1),C' '
         JE    MAIN_097
         CLI   0(R1),0
         JE    MAIN_097
         TM    WKSTAT,QSTART             use toggle to ignore comma
         JO    MAIN096D
         CLI   0(R1),C','
         JE    MAIN_097
MAIN096D EQU   *
         MVC   0(1,R15),0(R1)
         LA    R15,1(,R15)
         LA    R1,1(,R1)
         AHI   R0,1
         BRCT  R2,MAIN_096
MAIN_097 EQU   *
         STH   R0,WKDDPRML
*
*      OPEN MESSAGE FILE
MAIN_098 EQU   *
         LA    R14,OUTFILE               COPY MODEL   DCB
         LLGT  R2,OUTDCB
         MVC   0(OUTFILEL,R2),0(R14)
         USING IHADCB,R2
         LR    R0,R2                     SET  DCBE ADDRESS IN  DCB
         AGHI  R0,OUTFILE0
         STY   R0,DCBDCBE
*
         MVC   WKREENT(8),OPENPARM
         OPEN  ((R2),(EXTEND)),MODE=31,MF=(E,WKREENT)
         TM    48(R2),X'10'              SUCCESSFULLY OPENED  ??
         JO    MAIN_099                  YES - BYPASS ABEND
         WTO 'GVBJMR95: DDPRINT OPEN FAILED'
         MVC   WKRETC,=F'16'
         J     DONEDONE
*
***********************************************************************
*  FIND GLOBAL NAME/TOKEN AREA                                        *
***********************************************************************
*
MAIN_099 EQU   *
         MVC   WKPRINT,SPACES
         MVC   WKPRINT(10),=CL10'GVBJMR95: '
         MVC   WKPRINT+10(80),WKCARD
         LLGT  R2,OUTDCB
         LA    R0,WKPRINT
         PUT   (R2),(R0)
***********************************************************************
         MVC   WKTOKNAM+0(8),GENEVA
         MVC   WKTOKNAM+8(8),TKNNAME
         CALL  IEANTRT,(TOKNLVL2,WKTOKNAM,WKTOKN,WKTOKNRC),            X
               MF=(E,WKREENT)
         LTGF  R15,WKTOKNRC       SUCCESSFUL  ???
         JZ    MAIN_140
         WTO 'GVBJMR95: COMMUNICATIONS TENSOR TABLE NOT LOCATED'
         MVC   WKRETC,=F'8'
         J     DONE
*
MAIN_140 EQU   *
*        wto 'GVBJMR95: finding CTT'
         LLGT  R4,WKTOKNCTT
         USING CTTAREA,R4
         CLC   CTTEYE,CTTEYEB
         JE    MAIN_114
         WTO 'GVBJMR95: COMMUNICATIONS TENSOR TABLE DOES NOT MATCH'
         MVC   WKRETC,=F'12'
         J     DONE
*
MAIN_114 EQU   *                  LOAD AND EXECUTE GVBMR95 OR SOMETHING
         LOAD  EPLOC=WKDDEXEC,ERRET=G0010
         OILH  R0,MODE31
         ST    R0,GVBMR95E
         J     MAIN_115
G0010    EQU   *
         WTO 'GVBJMR95: APPLICATION CANNOT BE LOADED'
         MVC   WKRETC,=F'28'
         J     DONE
*
MAIN_115 EQU   *
         LAY   R1,WKDDPRML
         ST    R1,WKEPARMA
         LAY   R1,WKEPARMA
         L     R15,GVBMR95E         This may not store 64 bit registers
         BASR  R14,R15                                 only 31 bit !!!!
         ST    R15,WKEXECRC
*
*        ALLOW A FEW SECONDS FOR IN FLIGHT COMMANDS TO COMPLETE AND THE
*        GvbJavaDaemon TO TIDY UP EACH WORKER THREAD.
*
         STIMER WAIT,BINTVL=FIVESEC
*
MAIN_200 EQU   *
         MVC   WKPRINT,SPACES
         MVC   WKPRINT(10),=CL10'GVBJMR95: '
         MVC   WKPRINT+10(19),=CL19'POSTING TERMINATION'
         LLGT  R2,OUTDCB
         LA    R0,WKPRINT
         PUT   (R2),(R0)
MAIN_201 EQU   *
*
         POST  CTTTECB                   TERMINATION ECB
         DROP  R4 CTTAREA
*
         MVC   WKPRINT,SPACES
         MVC   WKPRINT(60),=CL60'GVBJMR95: XXXXXXXX EXECUTION COMPLETEDx
               . RETURN CODE XXXXXX   '
         MVC   WKPRINT+10(8),WKDDEXEC
         LLGF  R15,WKEXECRC
         CVD   R15,WKDBL3
         MVC   WKPRINT+52(6),NUMMSK+6
         MVI   WKPRINT+52,C' '
         ED    WKPRINT+52(6),WKDBL3+5
         LLGT  R2,OUTDCB
         LA    R0,WKPRINT
         PUT   (R2),(R0)
         J     MAIN_204
*
MAIN_204 EQU   *
         XC    WKRETC,WKRETC
*
*        RETURN TO CALLER
*
DONE     EQU   *                         RETURN TO CALLER
         LLGT  R2,OUTDCB
         MVC   WKREENT(8),OPENPARM
         CLOSE ((R2)),MODE=31,MF=(E,WKREENT)
*
DONEDONE EQU   *                         RETURN TO CALLER
         LLGT  R2,INDCB
         FREEMAIN R,LV=INFILEL,A=(2)
         LLGT  R2,OUTDCB
         FREEMAIN R,LV=OUTFILEL,A=(2)
*
         sam64
         sysstate amode64=YES
*
         LG    R9,SAVER9                        Restore our only 64 bit
*                              register out of the abundance of caution
*
         LLGF  R15,WKRETC
         LG    R13,SAVER13
         STG   R15,SAVF4SAG64RS15-SAVF4SA(,R13)
         LG    R0,PAATMEM
         AGHI  R0,-DYNLEN                POP
         STG   R0,PAATMEM
*
         LMG   R14,R12,SAVF4SAG64RS14-SAVF4SA(R13)
         BR    R14                       RETURN TO CALLER
*
         DS    0D
MVCR14R1 MVC   0(0,R14),0(R1)     * * * * E X E C U T E D * * * *
         DS    0D
CLCR1R14 CLC   0(0,R1),0(R14)     * * * * E X E C U T E D * * * *
*
*        CONSTANTS
*
FIVESEC  DC    F'500'
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
INFILE   DCB   DSORG=PS,DDNAME=DDEXEC,MACRF=(GM),DCBE=INFDCBE,         X
               RECFM=FB,LRECL=80
INFILE0  EQU   *-INFILE
INFDCBE  DCBE  RMODE31=BUFF,EODAD=MAIN_092
INFILEL  EQU   *-INFILE
*
*
SPACES   DC    CL256' '
XHEXFF   DC 1024X'FF'
*
*
         LTORG ,
*
NUMMSK   DC    XL12'402020202020202020202120'
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
