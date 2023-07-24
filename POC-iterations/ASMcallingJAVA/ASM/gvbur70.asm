         TITLE 'GVBUR70 - Interface for calling Java'
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
*
***********************************************************************
*                                                                     *
*  MODULE DESCRIPTION:                                                *
*                                                                     *
*      - THIS MODULE ALLOWS A SPECIFIED JAVA CLASS AND METHOD TO BE   *
*        CALLED USING THE GVBUR70 INTERFACE.                          *
*                                                                     *
***********************************************************************
                        EJECT
***********************************************************************
*                                                                     *
*           MODULE RETURN CODES AND REGISTER DOCUMENTATION            *
*                                                                     *
***********************************************************************
*                                                                     *
*                                                                     *
*  RETURN CODES:                                                      *
*                                                                     *
*            0  - SUCCESSFUL                                          *
*            4  -                                                     *
*            8  -                                                     *
*           12  -                                                     *
*           16  -                                                     *
*                                                                     *
*  PARAMETERS:                                                        *
*                                                                     *
*        R1:  PARAMETER LIST ADDRESS                                  *
*                                                                     *
*                                                                     *
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
*                                                                     *
*  REGISTER USAGE:                                                    *
*                                                                     *
*        R15 - TEMPORARY WORK    REGISTER                             *
*            - RETURN    CODE                                         *
*                                                                     *
*        R14 - TEMPORARY WORK    REGISTER                             *
*            - RETURN    ADDR                                         *
*                                                                     *
*        R13 - CALLER  SAVE AREA ADDRESS                              *
*        R12 -                                                        *
*        R11 -                                                        *
*        R10 -                                                        *
*        R9  -                                                        *
*        R8  -                                                        *
*        R7  -                                                        *
*        R6  -                                                        *
*        R5  -                                                        *
*        R4  -                                                        *
*        R3  -                                                        *
*        R2  -                                                        *
*                                                                     *
*        R1  - PARAMETER LIST    ADDRESS             (UPON ENTRY)     *
*                                                                     *
*        R0  - TEMPORARY WORK    REGISTER                             *
*                                                                     *
***********************************************************************
*                                                                     *
***********************************************************************
*                                                                     *
***********************************************************************
*                                                                     *
*        REGISTER EQUATES:                                            *
*                                                                     *
***********************************************************************
*
         YREGS
*
***********************************************************************
*                                                                     *
*        REGISTER SAVE AREA OFFSETS:                                  *
*                                                                     *
***********************************************************************
         PRINT GEN
*
         ihapsa ,
*
PARMLIST DSECT
*
PARMADDR DS    A               ADDRESS OF PARAMETER AREA
RECASND  DS    A               ADDRESS OF SEND      AREA
KEYAREC  DS    A               ADDRESS OF RECEIVE   AREA
*
RSABP    EQU   4
RSAFP    EQU   8
RSA14    EQU   12
RSA15    EQU   16
RSA0     EQU   20
RSA1     EQU   24
*
         COPY  GVBUR70            Call Interface dsect
         COPY  GVBUR70W           Work area and internal dsects
*
GVBUR70  RMODE ANY
GVBUR70  AMODE 31
GVBUR70  CSECT
         J     start
*
static   loctr
CODE     loctr
         org   *,256
         USING DYNAREA,R13
*
*        ENTRY LINKAGE
*
START    DS    0H
         STMG  R14,R12,SAVF4SAG64RS14
*
         LLGTR R12,R15                   ESTABLISH ...
         USING GVBUR70,R12               ... ADDRESSABILITY
*
         LLGTR R3,R1              LOAD PARM LIST ADDRESS
         USING PARMLIST,R3
*
         LLGT  R8,PARMADDR        LOAD PARM AREA ADDRESS
         USING UR70STR,R8
*
         LLGTR R13,R13
         LGR   R10,R13            SAVE  CALLER'S  RSA     ADDRESS
*
         LLGT  R13,UR70ANCH       LOAD  WORK AREA POINTER ADDRESS
         LTR   R13,R13            ALLOCATED ???
         JP    CHAIN              YES - BYPASS ALLOCATION
*
***********************************************************************
*  ALLOCATE "GVBXLST" WORKAREA IF NOT ALREADY ALLOCATED (PER THREAD)  *
***********************************************************************
         LLGT  R0,=A(DYNLEN)
         STORAGE OBTAIN,LENGTH=(R0),COND=NO,CHECKZERO=YES
         LLGTR R13,R1
         ST    R13,UR70ANCH       SAVE  WORK AREA ADDRESS (POINTER)
         CIJE  R15,14,A0002       If already zeroed, bypass this...
*
         LR    R0,R13             ZERO  WORK AREA
         LHI   R1,DYNLEN
         XR    R14,R14
         XR    R15,R15
         MVCL  R0,R14
*
A0002    EQU   *
         MVC   EYEBALL,WORKEYEB
         STG   R13,savf4sanext-DYNAREA(0,R10) fwd POINTER IN OLD
         STG   R10,savf4saprev    SET   BACKWARD POINTER IN NEW
         J     MAINLINE
*
***********************************************************************
*  CHAIN REGISTER SAVE AREAS TOGETHER                                 *
*  CHECK FOR CHANGE IN EVENT RECORD ADDRESS                           *
***********************************************************************
CHAIN    DS    0H
         STG   R13,savf4sanext-DYNAREA(0,R10) fwd POINTER IN OLD
         STG   R10,savf4saprev    SET   BACKWARD POINTER IN NEW
*
***********************************************************************
*                                                                     *
***********************************************************************
MAINLINE DS    0H
         XC    WKRETC,WKRETC
*
***********************************************************************
*  FIND GLOBAL NAME/TOKEN AREA                                        *
***********************************************************************
*
         LLGT  R4,WKTOKNCTT
         LTR   R4,R4
         JP    MAIN_114
*
***********************************************************************
*  Call to Java interface                                             *
***********************************************************************
*
         MVC   WKTOKNAM+0(8),GENEVA
         MVC   WKTOKNAM+8(8),TKNNAME
         CALL  IEANTRT,(TOKNLVL2,WKTOKNAM,WKTOKN,WKTOKNRC),            X
               MF=(E,WKREENT)
         LTGF  R15,WKTOKNRC       SUCCESSFUL  ???
         JZ    MAIN_140
         WTO 'GVBUR70 : COMMUNICATIONS TENSOR TABLE NOT LOCATED'
         MVC   WKRETC,=F'20'
         J     DONE
*
MAIN_140 EQU   *
         LLGT  R4,WKTOKNCTT
         USING CTTAREA,R4
         CLC   CTTEYE,CTTEYEB
         JE    MAIN_114
         WTO 'GVBUR70 : COMMUNICATIONS TENSOR TABLE DOES NOT MATCH'
         MVC   WKRETC,=F'24'
         J     DONE
*
*        LOCATE TABLE ENTRY for REQUEST communication: CTRAREA
*
MAIN_114 EQU   *
         LLGT  R5,CTTACTR
         USING CTRAREA,R5
*
*        CHECK FOR FUNCTION
*
         CLC   UR70FUN,=CL8'CALL'
         JE    A0100
         CLC   UR70FUN,=CL8'INIT'
         JE    A0200
*
         MVC   WKRETC,=F'16'
         J     DONE
*
***********************************************************************
*  Call Java                                                          *
***********************************************************************
A0100    EQU   *
         LLGT  R7,RECASND        LOAD  SEND BUFFER ADDRESS
         LLGT  R9,KEYAREC        LOAD  RECEIVE BUFFER ADDRESS
*
         ICM   R0,B'1111',WKATCB Already stored here ?
         JP    A0102
*
         USING PSA,R0
         L     R0,PSATOLD
         ST    R0,WKATCB
         LH    R6,CTTNUME        NUMBER OF CTR SLOTS AVAILABLE
         XR    R0,R0             CTR INDEX STARTS AT ZERO
CSLOOP   EQU   *
         XR    R2,R2
         L     R3,WKATCB
         CS    R2,R3,CTRCSWRD
         JZ    A0104
         AGHI  R0,1
         LA    R5,CTRLEN(,R5)
         BRCT  R6,CSLOOP
         J     MAIN_116
         DROP  R0
*
A0102    EQU   *                 R0 = CTR IDX
         LH    R2,WKICTR         CTR INDEX (STARTING AT ZERO)
         MH    R2,=Y(CTRLEN)     Offset required
         AR    R5,R2             Point to our selected CTR slot
         J     A0106
*
A0104    EQU   *
         STH   R0,WKICTR         R0 = IDX; R5 -> SELECTED CTR SLOT
*
A0106    EQU   *
         sam64
         sysstate amode64=YES
         LLGT  R0,UR70LSND
         STG   R0,CTRLENOUT      Length of data being sent...
         LLGT  R0,UR70LRCV
         STG   R0,CTRLENIN       Length of data being returned...
         LAY   R1,UR70CLSS       use 32 bytes of this field
         STG   R1,CTRACLSS       ADDRESS OF CLASS NAME
         LAY   R1,UR70METH
         STG   R1,CTRAMETH       ADDRESS OF METHOD NAME
         STG   R7,CTRMEMOUT      WAY OUT(going to Java.)
         STG   R9,CTRMEMIN       WAY IN (to be received)
         sysstate amode64=NO
         sam31
*
         POST  CTRECB1           POST A REQUEST ECB
*
*        WTO 'GVBJPOST : REQUEST POSTED TO JAVA'
*
         WAIT  1,ECB=CTRECB2     WAIT FOR RESPONSE TO HAPPEN
         XC    CTRECB2,CTRECB2
*
         sam64
         sysstate amode64=YES
         LLGT  R0,CTRLENOUT      Amount of data actually returned
         ST    R0,UR70LRET
         sysstate amode64=NO
         sam31
*
*        WTO 'GVBJPOST : RESPONSE RECEIVED TO REQUEST'
*
         J     DONE
*
MAIN_116 EQU   *
         WTO 'GVBUR70 : SPECIFIED NUMBER THREADS EXCEEDED'
         MVC   WKRETC,=F'12'
         J     DONE
*
***********************************************************************
*  Set up communications area for requested number of threads         *
*  And acknoledge GvbDaemon with a handshake..                        *
***********************************************************************
A0200    EQU   *
         LH    R6,CTTNUME                Ensure this not already done
         CIJE  R6,99,A0200A              Should be init max (99)
         J     MAIN_220                  Unexpected value, go
*
A0200A   EQU   *
         LGH   R6,UR70OPNT               FROM THREADS REQUIRED
         CIJL  R6,1,MAIN_222             Must be between 1
         CIJH  R6,99,MAIN_224            and 99.
         STH   R6,CTTNUME                SET# ACTUAL THREADS REQUIRED
*
         WTO 'GVBUR70 : SETTING UP CTR NOW'
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
         BRCT  R6,MAIN_120
         WTO 'GVBUR70 : CTR COMPLETED'
*
         POST  CTTGECB                   POST THE GO ECB
         WTO 'GVBUR70 : POSTED FOR GO, NUMBER THREADS SET'
*
         WAIT  ECB=CTTGECB2              Wait for acknowledgement
         XC    CTTGECB2,CTTGECB2
         WTO 'GVBUR70 : ACKNOWLEDGEMENT RECEIVED AND GECB2 RESET'
         MVI   CTTACTIV,X'FF'
         WTO 'GVBUR70 : REQUEST TABLE SET ACTIVE'
         J     DONE
*
MAIN_220 EQU   *
         WTO 'GVBUR70 : INIT function attempted more than once'
         MVC   WKRETC,=F'4'
         J     DONE
*
MAIN_222 EQU   *
         WTO 'GVBUR70 : INIT function with NUMT < 1  attempted'
         MVC   WKRETC,=F'8'
         J     DONE
*
MAIN_224 EQU   *
         WTO 'GVBUR70 : INIT function with NUMT > 99  attempted'
         MVC   WKRETC,=F'8'
         J     DONE
*
***********************************************************************
*  RETURN TO CALLER (SAFR)                                            *
***********************************************************************
DONE     EQU   *                  RETURN TO CALLER
         LLGT  R15,WKRETC         LOAD RETURN CODE
         ST    R15,UR70RETC
*
*        RETURN TO CALLER
*
         LG    R13,SAVF4SAPREV    CALLER'S SAVE AREA ADDRESS
*
         LLGT  R15,WKRETC
         STG   R15,SAVF4SAG64RS15-SAVF4SA(,R13)
*
         LMG   R14,R12,SAVF4SAG64RS14-SAVF4SA(R13)
         BSM   0,R14              RETURN
*
static   loctr
         LTORG ,
*
CTTEYEB  DC    CL8'GVBCTT'
WORKEYEB DC    CL8'GVBUR70'
TKNNAME  DC    CL8'GVBJMR95'
GENEVA   DC    CL8'GENEVA'
TOKNPERS DC    F'0'
TOKNLVL1 DC    A(1)
TOKNLVL2 DC    A(2)
*
         END
