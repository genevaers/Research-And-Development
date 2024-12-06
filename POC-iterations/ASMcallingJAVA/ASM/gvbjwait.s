         TITLE    'WAIT FOR REQUEST FROM APPLICATION OR TERMINATION '
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
*   This module is called by the GvbJavaDaemon to wait on events
*   to communicate with assembler/3GL/etc code executing in separate
*   threads in the same address space, for example GVBMR95.
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
SAVEAREA DS  18FD              64 bit save area
SAVER13  DS    D
*
         DS    0F
OUTDCB   DS    A               Address of 24 bit getmained DCB
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
WKECBLST DS   0XL16               ECB ADDR LIST TO WAIT ON
WKAECB1  DS    F
WKAECB2  DS    F
WKAECB3  DS    F
WKAECB4  DS    F
WKECBNUM DS    H                  Number in list
WKECBLSZ DS    H
WKENTIDX DS    A
         DS    A
         DS    0F
DYNLEN   EQU   *-DYNAREA                 DYNAMIC AREA LENGTH
*
GVBJWAIT RMODE 24
GVBJWAIT AMODE 31
*
GVBJWAIT CSECT
*
*        ENTRY LINKAGE
*
         STMG  R14,R12,SAVF4SAG64RS14-SAVF4SA(R13)
         LLGTR R12,R15                   ESTABLISH ...
         USING GVBJWAIT,R12              ... ADDRESSABILITY
*
         LGR   R9,R1                     => Parmstr
         USING PARMSTR,R9
         LGH   R2,PAOPT+4                Directions to ECB's for WAIT
         LG    R4,PAANCHR                Maybe we have this already ?
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
         ST    R2,WKENTIDX               Directions for ECB(s)
         XC    PALEN2,PALEN2             Clear this until we know
*
*      OPEN MESSAGE FILE
         J     MAIN_096
*
         sysstate amode64=NO
         sam31
*
         GETMAIN R,LV=OUTFILEL,LOC=24
         ST    R1,OUTDCB
*
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
         JO    MAIN_095                  YES - BYPASS ABEND
         WTO 'GVBJWAIT: DDPRINT OPEN FAILED'
         MVC   WKRETC,=F'16'
         sam64
         J     DONEDONE                  Go 31 bit Amode
MAIN_095 EQU   *
         sam64
         sysstate amode64=YES
*
***********************************************************************
*  FIND GLOBAL NAME/TOKEN AREA                                        *
***********************************************************************
*
MAIN_096 EQU   *
         LTR   R4,R4
         JP    MAIN_142
         MVC   WKTOKNAM+0(8),GENEVA
         MVC   WKTOKNAM+8(8),TKNNAME
         CALL  IEAN4RT,(TOKNLVL2,WKTOKNAM,WKTOKN,WKTOKNRC),            X
               MF=(E,WKREENT)
         LTGF  R15,WKTOKNRC       SUCCESSFUL  ???
         JZ    MAIN_140
         WTO 'GVBJWAIT: COMMUNICATIONS TENSOR TABLE NOT LOCATED'
         MVC   WKRETC,=F'8'
         J     DONE
*
MAIN_140 EQU   *
         LLGT  R4,WKTOKNCTT
MAIN_142 EQU   *
         USING CTTAREA,R4
         CLC   CTTEYE,CTTEYEB
         JE    MAIN_114
         WTO 'GVBJWAIT: COMMUNICATIONS TENSOR TABLE DOES NOT MATCH'
         MVC   WKRETC,=F'12'
         J     DONE
*
*        ALLOCATE TABLE for REQUEST communication: CTRAREA
*
MAIN_114 EQU   *
         LLGT  R5,CTTACTR
         USING CTRAREA,R5
*
         XC    WKECBLST,WKECBLST
*
* DETERMINE WHICH ECB's to WAIT ON
*
         J     MAIN_116
         sysstate amode64=NO
         sam31
         USING CTTAREA,R4
         MVC   WKPRINT,SPACES
         MVC   WKPRINT(10),=CL10'GVBJWAIT: '
         MVC   WKPRINT+10(28),=CL28'WAIT ON ECB with directions '
         LLGT  R1,WKENTIDX
         AHI   R1,1              Add one as index starts -1
         SLL   R1,2
         LAY   R15,OPTTABLE
         AR    R15,R1
         MVC   WKPRINT+38(4),0(R15)
         LLGT  R2,OUTDCB
         LA    R0,WKPRINT
         PUT   (R2),(R0)
         sam64
         sysstate amode64=YES
*
MAIN_116 EQU   *                 Waiting for REQUEST
         XGR   R2,R2
         ICM   R2,B'1111',WKENTIDX
         JM    A0130
         JZ    A0140
*
**       CLI   CTTACTIV,X'FF'
**       JNE   A0160
         LTR   R5,R5
         JZ    A0170
         BCTR  R2,0              Minus 1 as index starts at 1
         MH    R2,=Y(CTRLEN)     Offset required
         AR    R5,R2
*
         LAY   R1,WKECBLST       ASSIGN ECBLIST -- Waiting for work
         LAY   R0,CTRECB1
         ST    R0,0(,R1)
         OI    0(R1),X'80'
         J     A0180
*
A0130    EQU   *                 Waiting for TERM
         LAY   R1,WKECBLST       ASSIGN ECBLIST
         LAY   R0,CTTTECB
         ST    R0,0(,R1)
         OI    0(R1),X'80'
         J     A0180
*
A0140    EQU   *                 Waiting for GO or TERM
         LAY   R1,WKECBLST       ASSIGN ECBLIST
         LAY   R0,CTTTECB
         ST    R0,0(,R1)
         LAY   R0,CTTGECB
         ST    R0,4(,R1)
         OI    4(R1),X'80'
         J     A0180
*
A0160    EQU   *
         WTO 'GVBJWAIT: TABLE NO LONGER ACTIVE'
         MVC   WKRETC,=F'20'
         J     MAIN_200
*
A0170    EQU   *
         WTO 'GVBJWAIT: TABLE NOT YET ACTIVE'
         MVC   WKRETC,=F'20'
         J     MAIN_200
*
*        WAIT FOR SOMETHING TO DO OR END TO COME
*
A0180    EQU   *
         LAY   R1,WKECBLST
         WAIT  1,ECBLIST=(1)
*
* FIND OUT WHICH ECB POSTED US
*
         TM    CTTTECB,X'40'
         JO    A0022
         TM    CTTGECB,X'40'
         JO    A0020
         TM    CTRECB1,X'40'
         JO    A0024
         WTO 'GVBJWAIT: NOT POSTED BY ANY ECB'
         MVC   WKRETC,=F'24'
         J     DONE
*
A0020    EQU   *                  Post by GO ECB
         XC    CTTGECB,CTTGECB    Clear ECB
         LGH   R0,CTTNUME         Return number threads here for now
         LG    R1,PAADDR2         Return buffer. Use first 8 bytes
         STG   R0,16(,R1)         Give number of threads to Java (FD)
         WTO 'GVBJWAIT: POSTED BY GO ECB'
         MVC   WKRETC,=F'6'
         J     MAIN_200
*
A0022    EQU   *                 Posted by TERM ECB
         MVI   CTTACTIV,X'00'
         XC    CTTTECB,CTTTECB   Don't clear ECB ||||||||||||||||||||
         WTO 'GVBJWAIT: Posted by termination ECB and request table set+
                inactive'
         MVC   WKRETC,=F'2'
         J     MAIN_200
*
A0024    EQU   *                  Posted by REQUEST ECB
         XC    CTRECB1,CTRECB1    Clear ECB
         CLI   CTTACTIV,X'FF'     IS REQUEST TABLE STILL ACTIVE ?
         JE    A0025              YES, GO
         WTO 'GVBJWAIT: WAITING FOR REQUESTS BUT GOT TERMINATION'
         MVC   WKRETC,=F'2'
         J     MAIN_200
*
A0025    EQU   *                  Point at "return" buffer"
         LG    R14,PAADDR2
         LG    R1,CTRACLSS
         MVC   16(32,R14),0(R1)
         LG    R1,CTRAMETH
         MVC   48(32,R14),0(R1)
*
         LG    R15,CTRLENOUT             LENGTH of available memory-in 
         STG   R15,PALEN2                LENGTH
*
         LG    R1,CTRMEMOUT              Incoming memory used later...
         CLC   CTRREQ,=CL4'MR95'         Called by GVBMR95 ?
         JNE   A0026                     No, go
*
* This is specifically for a GVBMR95 lookup exit using GVBX95PA only
*
         MVC   8(8,R14),=CL8'MR95WORK'   REASON CODE
         LLGT  R0,00(,R1)                Populate 10 addresses for
         STG   R0,PAGENPA+00             GVBX95PA which includes the
         LLGT  R0,04(,R1)                key, i.e. data
         STG   R0,PAGENPA+08
         LLGT  R0,08(,R1)
         STG   R0,PAGENPA+16
         LLGT  R0,12(,R1)
         STG   R0,PAGENPA+24
         LLGT  R0,16(,R1)
         STG   R0,PAGENPA+32
         LLGT  R0,20(,R1)
         STG   R0,PAGENPA+40
         LLGT  R0,24(,R1)
         STG   R0,PAGENPA+48
         LLGT  R0,28(,R1)
         STG   R0,PAGENPA+56
         LLGT  R0,32(,R1)
         STG   R0,PAGENPA+64
         LLGT  R0,36(,R1)
         STG   R0,PAGENPA+72
*         wto 'GVBJWAIT: called by GVBMR95'
         J     A0027
*
A0026    EQU   *                         data was sent using GVBUR70
         MVC   8(8,R14),=CL8'UR70WORK'   REASON CODE
         STG   R1,PAADDR2                just replace PAADDR2 pointer
*         wto 'GVBJWAIT: called by GVBUR70'
*
A0027    EQU   *
         MVC   WKRETC,=F'4'              == REQUEST FROM MAIN PROGRAM
*
MAIN_200 EQU   *
         DROP  R5 CTRAREA
         DROP  R4 CTTAREA
*
         J     DONE
         sysstate amode64=NO
         sam31
         MVC   WKPRINT,SPACES
         MVC   WKPRINT(10),=CL10'GVBJWAIT: '
         MVC   WKPRINT+10(09),=CL9'COMPLETED'
*
         LLGT  R2,OUTDCB
         LA    R0,WKPRINT
         PUT   (R2),(R0)
         sam64
         sysstate amode64=YES
         J     DONE
*
*        RETURN TO CALLER
*
DONE     EQU   *                         RETURN TO CALLER
         J     DONEDONE
         sysstate amode64=NO
         sam31
         LLGT  R2,OUTDCB
         MVC   WKREENT(8),OPENPARM
         CLOSE ((R2)),MODE=31,MF=(E,WKREENT)
         FREEMAIN R,LV=OUTFILEL,A=(2)
         sam64
         sysstate amode64=YES
*
DONEDONE EQU   *                         RETURN TO CALLER
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
TOKNLVL1 DC    A(1)                    NAME/TOKEN  -- subtask
TOKNLVL2 DC    A(2)                    NAME/TOKEN  -- address space
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
*
         LTORG ,
*
NUMMSK   DC    XL12'402020202020202020202021'
*
OPTTABLE DC    CL4'WRKT'
         DC    CL4'ACKG'
         DC    CL4'0001'
         DC    CL4'0002'
         DC    CL4'0003'
         DC    CL4'0004'
         DC    CL4'0005'
         DC    CL4'0006'
         DC    CL4'0007'
         DC    CL4'0008'
         DC    CL4'0009'
         DC    CL4'0010'
         DC    CL4'0011'
         DC    CL4'0012'
         DC    CL4'0013'
         DC    CL4'0014'
         DC    CL4'0015'
         DC    CL4'0016'
         DC    CL4'0017'
         DC    CL4'0018'
         DC    CL4'0019'
         DC    CL4'0020'
         DC    CL4'0021'
         DC    CL4'0022'
         DC    CL4'0023'
         DC    CL4'0024'
         DC    CL4'0025'
         DC    CL4'0026'
         DC    CL4'0027'
         DC    CL4'0028'
         DC    CL4'0029'
         DC    CL4'0030'
         DC    CL4'0031'
         DC    CL4'0032'
         DC    CL4'0033'
         DC    CL4'0034'
         DC    CL4'0035'
         DC    CL4'0036'
         DC    CL4'0037'
         DC    CL4'0038'
         DC    CL4'0038'
         DC    CL4'0040'
         DC    CL4'0041'
         DC    CL4'0042'
         DC    CL4'0043'
         DC    CL4'0044'
         DC    CL4'0045'
         DC    CL4'0046'
         DC    CL4'0047'
         DC    CL4'0048'
         DC    CL4'0049'
         DC    CL4'0050'
         DC    CL4'0051'
         DC    CL4'0052'
         DC    CL4'0053'
         DC    CL4'0054'
         DC    CL4'0055'
         DC    CL4'0056'
         DC    CL4'0057'
         DC    CL4'0058'
         DC    CL4'0059'
         DC    CL4'0060'
         DC    CL4'0061'
         DC    CL4'0062'
         DC    CL4'0063'
         DC    CL4'0064'
         DC    CL4'0065'
         DC    CL4'0066'
         DC    CL4'0067'
         DC    CL4'0068'
         DC    CL4'0069'
         DC    CL4'0070'
         DC    CL4'0071'
         DC    CL4'0072'
         DC    CL4'0073'
         DC    CL4'0074'
         DC    CL4'0075'
         DC    CL4'0076'
         DC    CL4'0077'
         DC    CL4'0078'
         DC    CL4'0079'
         DC    CL4'0080'
         DC    CL4'0081'
         DC    CL4'0082'
         DC    CL4'0083'
         DC    CL4'0084'
         DC    CL4'0085'
         DC    CL4'0086'
         DC    CL4'0087'
         DC    CL4'0088'
         DC    CL4'0089'
         DC    CL4'0090'
         DC    CL4'0091'
         DC    CL4'0092'
         DC    CL4'0093'
         DC    CL4'0094'
         DC    CL4'0095'
         DC    CL4'0096'
         DC    CL4'0097'
         DC    CL4'0098'
         DC    CL4'0099'
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
