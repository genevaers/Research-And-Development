        TITLE 'TSTUR70 - CALL GVBUR70 TO INVOKE JAVA'
**********************************************************************
*
* (C) COPYRIGHT IBM CORPORATION 2006, 2017.
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
*  MODULE DESCRIPTION     : THIS MODULE WORKS WITHIN THE "GENEVA"     *
*                           ENVIRONMENT.                              *
*                                                                     *
*                         : THIS MODULE CALLS JAVA CLASS|METHOD       *
*                                                                     *
***********************************************************************
*
***********************************************************************
*                                                                     *
*           MODULE RETURN CODES AND REGISTER DOCUMENTATION            *
*                                                                     *
***********************************************************************
*                                                                     *
*  RETURN CODES:                                                      *
*                                                                     *
*          0   - SUCCESSFUL                                           *
*          4   - WARNING                                              *
*          8   - ERROR                                                *
*         12   - SERIOUS ERROR                                        *
*         16   - CATASTROPHIC FAILURE                                 *
*                                                                     *
*                                                                     *
*  REGISTER USAGE:                                                    *
*                                                                     *
*                                                                     *
         IHASAVER DSECT=YES,SAVER=YES,SAVF4SA=YES,SAVF5SA=YES,TITLE=NO
*
         COPY   GVBUR70        API Dsect for GVBUR70
*
WORKAREA DSECT
         ds    Xl(SAVF4SA_LEN)
*
WKSAVSUB DS  18fd              INTERNAL  SUBROUTINE  REG  SAVE  AREA
*
WKTIMWRK DS   0XL16
WKDBLWK1 DS    D               TEMPORARY DOUBLEWORD  WORK AREA
WKDBLWK2 DS    D               TEMPORARY DOUBLEWORD  WORK AREA
*
WKREENT  DS    XL256           Reentrant workarea
WKDBLWK  DS    XL08            Double work workarea
*
WKUR70A  DS    A               GVBUR70 ADDRESS
UR70LIST DS   0A               PARAMETER  LIST FOR "STGTP90"
UR70PA   DS    A               ADDRESS OF API  STRUCTURE
UR70SNDA DS    A               ADDRESS OF SEND BUFFER
UR70RECA DS    A               ADDRESS OF RECV BUFFER
*
UR70PARM DS    XL(UR70LEN)     API STRUCTURE
*
WKPRINT  DS    XL131           Print line
WKTRACE  DS    CL1             Tracing
WKEOF    DS    CL1
         DS    XL1
         DS    0F
OUTDCB   DS    XL(outfilel)    Reentrant DCB and DCBE areas
*
         DS    0F
WKRETC   DS    F
*
WKSEND   DS    CL1024          SEND BUFFER
WKRECV   DS    CL1024          RECV BUFFER
*
WORKLEN  EQU   (*-WORKAREA)
*
         print off
         SYSSTATE ARCHLVL=2
         COPY  ASMMSP
LEAVE    OPSYN ASM_LEAVE
         asmmrel on
         print on
*
*
***********************************************************************
*                                                                     *
*        REGISTER EQUATES:                                            *
*                                                                     *
***********************************************************************
*
         YREGS
*
         PRINT nogen
*
TSTUR70  RMODE 24
TSTUR70  AMODE 31
TSTUR70  CSECT
         J     CODE
         DC    CL8'TSTUR70',CL8'&SYSDATC',CL6'&SYSTIME'
*
static   loctr                    set up the static loctr
code     loctr                    followed by the code loctr
*
         using savf4sa,r13        map the save area
         stm   R14,R12,12(R13)
*
         llgtr R12,r15            SET   PROGRAM   BASE REGISTERS
         USING (TSTUR70,code),R12
*
         lghi  R0,WORKLEN+8       LOAD   WORK AREA SIZE
         STORAGE OBTAIN,LENGTH=(0),LOC=24,CHECKZERO=YES GET WORKAREA
         CIJE  R15,14,A0002       zeroed?
         lr    r10,r1             save address
         lr    R0,R1              ZERO  WORK  AREA
         lghi  R1,WORKLEN+8
         xr    R14,R14
         xr    R15,R15
         MVCL  R0,R14
         lr    r1,r10             restore pointer
A0002    EQU   *
*
         MVC   0(l'EYEBALL,R1),EYEBALL
         ahi   r1,l'EYEBALL       move pointer past
         drop  r13
         USING WORKAREA,R1
         using savf4sa,WORKAREA
         llgtr r13,r13
         stg   r13,savf4saprev    save caller's r13 in our SVA
         mvc   savf4said(4),=a(savf4said_value) set 'F4SA' in area
         llgtr R13,r1             Get new workarea into r13
         J     MAINLINE           BEGIN
*
*
CHAIN    ds    0h
         stg   r13,savf4saprev    save caller's r13 in our SVA
         llgtr R13,r1             Get new workarea into r13
         drop  r1
         using WORKAREA,r13
         using savf4sa,WORKAREA
*
***********************************************************************
MAINLINE DS    0H
***********************************************************************
         XC    WKRETC,WKRETC
*
***********************************************************************
*  OPEN MESSAGE FILE                                                  *
***********************************************************************
*      open message file
         LA    R14,outfile               COPY MODEL   DCB
d1       using ihadcb,outdcb
         MVC   outdcb(outfilel),0(R14)
         lay   R0,outdcb                 SET  DCBE ADDRESS IN  DCB
         aghi  R0,outfile0
         sty   R0,d1.DCBDCBE
*
         LAY   R2,OUTDCB
         MVC   WKREENT(8),OPENPARM
         OPEN  ((R2),(OUTPUT)),MODE=31,MF=(E,WKREENT)
*
         LOAD  EPLOC=LINKNAME,ERRET=A0004
         OILH  R0,MODE31
         ST    R0,WKUR70A
         J     A0006
A0004    EQU   *
         WTO 'TSTUR70: UNABLE TO LOAD GVBUR70'
         MVC   WKRETC,=F'16'
         J     DONE
***********************************************************************
A0006    EQU   *
         LA    R3,UR70PARM
         USING UR70STR,R3
         ST    R3,UR70LIST
         LAY   R0,WKSEND
         ST    R0,UR70LIST+04
         LAY   R0,WKRECV
         ST    R0,UR70LIST+08
         OI    UR70LIST,X'80'
*
         MVC   WKPRINT,SPACES
         MVC   WKPRINT(32),=CL32'TSTUR70: CALLING SETT (#threads)'
         LA    R2,OUTDCB
         LA    R0,WKPRINT
         PUT   (R2),(R0)
*
         XC    UR70ANCH,UR70ANCH
         MVC   UR70FUN,=CL8'INIT'             Set number of threads
         MVC   UR70OPNT,=H'1'                 Just one in this case
         XC    UR70RETC,UR70RETC
*
         LAY   R1,UR70LIST
         L     R15,WKUR70A
         BASR  R14,R15
         LTR   R15,R15
         JZ    A0008
         DC    H'0'
A0008    EQU   *
         ICM   R15,B'1111',UR70RETC
         JZ    A0010
         DC    H'0'
*
A0010    EQU   *
         MVC   WKPRINT,SPACES
         MVC   WKPRINT(33),=CL33'TSTUR70: CALLED SETT SUCCESSFULLY'
         LA    R2,OUTDCB
         LA    R0,WKPRINT
         PUT   (R2),(R0)
***********************************************************************
         MVC   WKPRINT,SPACES
         MVC   WKPRINT(32),=CL32'TSTUR70: CALLING MyClass Method1'
         LA    R2,OUTDCB
         LA    R0,WKPRINT
         PUT   (R2),(R0)
*
         MVC   UR70FUN,=CL8'CALL'
         MVC   UR70OPT,SPACES
         MVC   UR70CLSS,=CL32'MyClass'
         MVC   UR70METH,=cl32'Method1'
         MVC   UR70LSND,SNDLEN
         MVC   UR70LRCV,RECLEN
         XC    UR70RETC,UR70RETC
         MVC   WKSEND(10),=CL10'0123456789'
*
         LAY   R1,UR70LIST
         L     R15,WKUR70A
         BASR  R14,R15
         LTR   R15,R15
         JZ    A0011
         DC    H'0'
A0011    EQU   *
         ICM   R15,B'1111',UR70RETC
         JZ    A0012
         DC    H'0'
*
A0012    EQU   *
         MVC   WKPRINT,SPACES
         MVC   WKPRINT(09),=CL09'TSTUR70: '
         MVC   WKPRINT+09(22),WKRECV
         LA    R2,OUTDCB
         LA    R0,WKPRINT
         PUT   (R2),(R0)
***********************************************************************
A0016    EQU   *
         MVC   WKPRINT,SPACES
         MVC   WKPRINT(31),=CL31'TSTUR70: CALLING JAVA COMPLETED'
         LA    R2,OUTDCB
         LA    R0,WKPRINT
         PUT   (R2),(R0)
*
DONE     DS    0H
         LLGT  R15,WKRETC
*
         lg    r13,savf4saprev         restore caller save area
         st    r15,16(,r13)
         lm    r14,r12,12(r13)         restore caller's registers
         BR    R14              RETURN
*
STATIC   LOCTR
*
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
*                                                                     *
*        C O N S T A N T S                                            *
*                                                                     *
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
*
STATIC   LOCTR
         ds    0d
MVCR14R1 MVC   0(0,R14),0(R1)     * * * * E X E C U T E D * * * *
         ds    0d
CLCR1R14 CLC   0(0,R1),0(R14)     * * * * E X E C U T E D * * * *
*
         DS   0D
MODE31   equ   X'8000'
         DS   0D
OPENPARM DC    XL8'8000000000000000'
*
EYEBALL  DC    CL8'GVBUR70'
SNDLEN   DC    F'10'
RECLEN   DC    F'22'
LINKNAME DC    CL8'GVBUR70'
SPACES   DC    CL256' '
*
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
*                                                                     *
*        D A T A   C O N T R O L   B L O C K S                        *
*                                                                     *
* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
*
outfile  DCB   DSORG=PS,DDNAME=UR70PRNT,MACRF=(PM),DCBE=outfdcbe,      x
               RECFM=FB,LRECL=131
outfile0 EQU   *-outfile
outfdcbe DCBE  RMODE31=BUFF
outfilel EQU   *-outfile
*
         LTORG ,
         DS   0F
         DCBD  DSORG=PS
*
         IHADCBE
         END
