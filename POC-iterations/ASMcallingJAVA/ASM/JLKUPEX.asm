         TITLE 'JLKUPEX - ASM stub for calling Java loop up exit'
**********************************************************************
*
* (C) COPYRIGHT IBM CORPORATION 2024.
*     Copyright Contributors to the GenevaERS Project.
* SPDX-License-Identifier: Apache-2.0
*
**********************************************************************
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
*  or implied.
*  See the License for the specific language governing permissions
*  and limitations under the License.
*
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
***********************************************************************
*                                                                     *
*  MODULE DESCRIPTION:                                                *
*                                                                     *
*      - THIS MODULE RETURNS THE ADDRESS OF THE SAFR ENVIRONMENT      *
*        INFORMATION                                                  *
*                                                                     *
*                                                                     *
*  SAFR MODULES USED      : NONE                                      *
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
                        EJECT
         COPY GVBX95PA
                        EJECT
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
RSABP    EQU   4
RSAFP    EQU   8
RSA14    EQU   12
RSA15    EQU   16
RSA0     EQU   20
RSA1     EQU   24
*
         COPY  GVBJDSCT
*
*        DYNAMIC WORK AREA
*
DYNAREA  DSECT
*
         IHASAVER DSECT=NO,SAVER=NO,SAVF4SA=YES,TITLE=NO
*
EYEBALL  DS    CL8
         DS    0F
OUTDCB   DS    XL(OUTFILEL)    REENTRANT DCB AND DCBE AREAS
*
WKPRINT  DS    XL131           PRINT LINE
WKJFLAG  DS    XL1             JAVAFLAG
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
WKCLSSMT DS    XL64
WKRETDAT DS    XL4096
DYNLEN   EQU   *-DYNAREA                 DYNAMIC AREA LENGTH
*
*
JLKUPEX  RMODE ANY
JLKUPEX  AMODE 31
JLKUPEX  CSECT
         J     start
         DC    CL8'GVBJ2ENV',CL8'&SYSDATC',CL6'&SYSTIME'
*
static   loctr
CODE     loctr
         org   *,256
         USING DYNAREA,R13
*
*        ENTRY LINKAGE
*
start    DS    0H
         STMG  R14,R12,SAVF4SAG64RS14
*
         LLGTR R12,R15                   ESTABLISH ...
         USING JLKUPEX,R12               ... ADDRESSABILITY
*
         LLGTR R8,R1                     => Genparm
         USING GENPARM,R8
*
         LGR   R10,R13            SAVE  CALLER'S  RSA     ADDRESS
*
         LLGT  R2,GPWORKA         LOAD  WORK AREA POINTER ADDRESS
         LLGT  R13,0(,R2)         LOAD  POINTER   VALUE
         LTR   R13,R13            ALLOCATED ???
         JP    CHAIN              YES - BYPASS ALLOCATION
*
***********************************************************************
*  ALLOCATE "GVBXLST" WORKAREA IF NOT ALREADY ALLOCATED (PER THREAD)  *
***********************************************************************
         LGH   R0,=Y(DYNLEN)
         STORAGE OBTAIN,LENGTH=(R0),COND=NO,CHECKZERO=YES
         LLGTR R13,R1
         ST    R13,0(,R2)         SAVE  WORK AREA ADDRESS (POINTER)
*
         LR    R0,R13             ZERO  WORK AREA
         LHI   R1,DYNLEN
         XR    R14,R14
         XR    R15,R15
         MVCL  R0,R14
*
         MVC   EYEBALL,WORKEYEB
         STG   R13,savf4sanext-DYNAREA(0,r10) fwd POINTER IN OLD
         STG   R10,savf4saprev    SET   BACKWARD POINTER IN NEW
*
         LLGT  R14,GPENVA         OPEN  PHASE ???
         USING GENENV,R14         Doesn't matter about phase
         J     MAINLINE           All phases OP/RD/CL passed to Java
*
***********************************************************************
*  CHAIN REGISTER SAVE AREAS TOGETHER                                 *
*  CHECK FOR CHANGE IN EVENT RECORD ADDRESS                           *
***********************************************************************
CHAIN    DS    0H
         STG   R13,savf4sanext-DYNAREA(0,r10) fwd POINTER IN OLD
         STG   R10,savf4saprev    SET   BACKWARD POINTER IN NEW
*
***********************************************************************
*                                                                     *
***********************************************************************
MAINLINE DS    0H
         LLGT  R7,GPKEYA          LOAD  LOOK-UP   KEY     ADDRESS
* * *    USING LKUPKEY,R7         Logical record ID then key data
*
         LAY   R9,WKRETDAT
         XC    WKRETC,WKRETC
         LLGT  R14,GPENVA         CHECK FOR   CLOSE PHASE
         USING GENENV,R14
         DROP  R14
*
         wto 'JLKUPEX: calling Java lookup method'
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
*  JAVA VERSION                                                       *
***********************************************************************
*
         MVC   WKTOKNAM+0(8),GENEVA
         MVC   WKTOKNAM+8(8),TKNNAME
         CALL  IEANTRT,(TOKNLVL2,WKTOKNAM,WKTOKN,WKTOKNRC),            X
               MF=(E,WKREENT)
         LTGF  R15,WKTOKNRC       SUCCESSFUL  ???
         JZ    MAIN_140
         WTO 'JLKUPEX: COMMUNICATIONS TENSOR TABLE NOT LOCATED'
         MVC   WKRETC,=F'8'
         J     DONE
*
MAIN_140 EQU   *
         LLGT  R4,WKTOKNCTT
         USING CTTAREA,R4
         CLC   CTTEYE,CTTEYEB
         JE    MAIN_114
         WTO 'JLKUPEX: COMMUNICATIONS TENSOR TABLE DOES NOT MATCH'
         MVC   WKRETC,=F'12'
         J     DONE
*
*        LOCATE TABLE ENTRY for REQUEST communication: CTRAREA
*
MAIN_114 EQU   *
         LLGT  R5,CTTACTR
         USING CTRAREA,R5
         LGH   R3,CTTNUME
*
         LLGT  R6,GPENVA          LOAD ENV  INFO POINTER ADDRESS
         USING GENENV,R6
*
         XGR   R2,R2
         ICM   R2,B'0011',GPTHRDNO
         JNP   MAIN_117
         CR    R2,R3
         JH    MAIN_116
*
         BCTR  R2,0              Minus 1 as index start at 1
         MH    R2,=Y(CTRLEN)     Offset required
         AR    R5,R2             Point to individual CTR
*
         LGHI  R0,64             Key length (arbitrary for now)
         STG   R0,CTRLENOUT      Key Data being sent...
         LGHI  R0,256            Data length (arbitrary for now)
         STG   R0,CTRLENIN       Data being returned...
*
         MVC   CTRREQ,=CL4'MR95' Called by MR95
*
         LAY   R1,WKCLSSMT       use 64 bytes of this field
         MVC   0(32,R1),=CL32'MyClassB' Java class to call
         MVC   32(32,R1),=CL32'MethodY' and (hard coded) method
         STG   R1,CTRACLSS       ADDRESS OF CLASS NAME
         AGHI  R1,32
         STG   R1,CTRAMETH       ADDRESS OF METHOD NAME
*
*   R8 => GENPARM which contains pointer to key value (after view#)
         STG   R8,CTRMEMOUT      WAY OUT(going to Java.)
         STG   R9,CTRMEMIN       WAY IN (to be received)
*
*
         POST  CTRECB1           POST A REQUEST ECB
*
*
         WAIT  1,ECB=CTRECB2
         XC    CTRECB2,CTRECB2
*
*
         LG    R9,CTRMEMIN
         L     R14,GPBLOCKA       LOAD RESULT    POINTER ADDRESS
         STG   R9,0(,R14)
*
         LHI   R0,16
         L     R14,GPBLKSIZ
         ST    R0,0(,R14)         ---> NOT SURE IF THIS IS NEEDED
*
         LH    R15,CTRJRETC       ZERO RETURN CODE
         ST    R15,WKRETC
         J     DONE
*
MAIN_116 EQU   *
         WTO 'JLKUPEX: GPTHRDNO EXCEEDS MAXIMUM'
         MVC   WKRETC,=F'4'
         J     DONE
*
MAIN_117 EQU   *
         WTO 'JLKUPEX: GPTHRDNO LESS THAN MINIMUM'
         MVC   WKRETC,=F'4'
         J     DONE
*
***********************************************************************
*  RETURN TO CALLER (SAFR)                                            *
***********************************************************************
DONE     EQU   *                  RETURN TO CALLER
         LLGT  R15,WKRETC         LOAD RETURN CODE
         LLGT  R14,GPRTNCA        LOAD RETURN CODE  ADDRESS
         ST    R15,0(,R14)
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
CTTEYEB  DC    CL8'GVBCTTAB'
WORKEYEB DC    CL8'JLKUPEX '
TKNNAME  DC    CL8'GVBJMR95'
GENEVA   DC    CL8'GENEVA'
TOKNPERS DC    F'0'
TOKNLVL1 DC    A(1)
TOKNLVL2 DC    A(2)
*
OUTFILE  DCB   DSORG=PS,DDNAME=DDPRINT,MACRF=(PM),DCBE=OUTFDCBE,       X
               RECFM=FB,LRECL=131
OUTFILE0 EQU   *-OUTFILE
OUTFDCBE DCBE  RMODE31=BUFF
OUTFILEL EQU   *-OUTFILE
*
         END
