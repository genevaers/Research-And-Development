        TITLE 'TSTUR70 - CALL GVBUR70 TO INVOKE JAVA'
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
*                                                                     *
*  MODULE DESCRIPTION     : THIS IS A TEST PROGRAM FOR CALLING        *
*                           GVBUR70.                                  *
*                                                                     *
*                           IT INVOKES A JAVA CLASS AND METHOD.       *
*                                                                     *
*  PARAMETERS             : PGM=TSTUR70                 1 main task   *
*                           PGM=TSTUR70,PARM='TASKS=n'  n sub tasks   *
*                           PGM=TSTUR70,PARM='TASKS=0'  It is sub task*
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
WKSVA    ds  18F               Save area
WKSAVE2  DS  18F               next SVA
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
WKPARM   DS    CL100
WKPLISTA DS    A
WKPLISTL DS    F
WKSUBPA1 DS    A
WKSUBPA2 DS    A
WKSUBPA3 DS    A
WKSUBPL1 DS    F
WKSUBPL2 DS    F
WKSUBPL3 DS    F
WKTASKS  DS    H
         DS    H
WKECBLST DS  20A
WKECBSUB DS  20F
WKTCBSUB DS  20F
WKPL6    DS    PL6
WKDBL1   DS    D
WKDBL2   DS    D
WKDBL3   DS    D
WKEPARMA DS    A
WKDDPRML DS    H
WKDDPARM DS    CL30
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
STATIC   loctr                    set up the static loctr
CODE     loctr                    followed by the code loctr
*
         STM   R14,R12,12(R13)
*
         LLGTR R12,R15            STATIC BASE REGISTER
         LLGTR R9,R1              ORIGINAL PARAMETER LIST
         USING (TSTUR70,code),R12
*
         STORAGE OBTAIN,LENGTH=WORKLEN+8,LOC=24,CHECKZERO=YES
         CIJE  R15,14,A0002       zeroed?
         LR    R10,R1             save address
         LR    R0,R1              ZERO  WORK  AREA
         LGHI  R1,WORKLEN+8
         XR    R14,R14
         XR    R15,R15
         MVCL  R0,R14
         LR    R1,R10             restore pointer to our work area
A0002    EQU   *
*
         MVC   0(l'EYEBALL,R1),EYEBALL
         AHI   R1,l'EYEBALL       move pointer past
         ST    R13,4(,R1)         save caller's r13 in our SVA
         ST    R1,8(,R13)         save our SVA in caller's
         LLGTR R13,R1             Our workarea into r13
         J     MAINLINE           BEGIN
*
*
CHAIN    DS    0H <-- never comes here
         ST    R13,4(,R1)         save caller's r13 in our SVA
         ST    R1,8(,R13)         save our SVA in caller's
         LLGTR R13,R1             Get new workarea into r13
         USING WORKAREA,R13
*
***********************************************************************
MAINLINE DS    0H
***********************************************************************
         XC    WKRETC,WKRETC
*
***********************************************************************
*  OPEN MESSAGE FILE                                                  *
***********************************************************************
*      open message file (DD SYSOUT=*)
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
***********************************************************************
*  DISCOVER PARAMETERS SUPPLIED                                       *
***********************************************************************
*
         ST    R9,WKPLISTA        SAVE PARAMETER LIST ADDRESS ADDR
         L     R8,0(,R9)          => PARM LIST
         LA    R8,0(,R8)
         LGH   R15,0(,R8)         == parm length
         LTR   R15,R15
         JZ    A000109
*
         ST    R15,WKPLISTL       store it
         BCTR  R15,0
         LA    R1,2(,R8)          == parm value
         EXRL  R15,MVCPARM
         LA    R0,WKPARM
*
*
A00010   EQU   *
         LA    R8,WKPARM          first sub parameter
         ST    R8,WKSUBPA1
         L     R1,WKPLISTL
         LR    R15,R8
A000102  EQU   *
         CLI   0(R8),C','
         JE    A000103
         LA    R8,1(,R8)
         BRCT  R1,A000102
*
         LR    R0,R8              no comma found, we're done
         SR    R0,R15
         ST    R0,WKSUBPL1
         J     A000109
*
A000103  EQU   *
         LR    R0,R8
         SR    R0,R15
         ST    R0,WKSUBPL1
*
         LA    R8,1(,R8)          account for 1st comma
         BCTR  R1,0
*
         ST    R8,WKSUBPA2
         LR    R15,R8
*
A000104  EQU   *
         CLI   0(R8),C','         next comma ?
         JE    A000105
         LA    R8,1(,R8)
         BRCT  R1,A000104
*
         LR    R0,R8              no comma found, we're done
         SR    R0,R15
         ST    R0,WKSUBPL2
         J     A000109
*
A000105  EQU   *
         LR    R0,R8
         SR    R0,R15
         ST    R0,WKSUBPL2
*
         LA    R8,1(,R8)          account for 2nd comma
         BCTR  R1,0
*
         LTR   R1,R1
         JZ    A000109
*
         ST    R8,WKSUBPA3
         LR    R15,R8
*
A000106  EQU   *
         CLI   0(R8),C','         there shouldn't be any more commas
         JE    A000108
         LA    R8,1(,R8)
         BRCT  R1,A000106
*
         LR    R0,R8              no comma found, we're done
         SR    R0,R15
         ST    R0,WKSUBPL3
         J     A000109
*
A000108  EQU   *
         wto 'TSTUR70 : too many sub parameters or commas'
         MVC   WKRETC,=F'8'
         J     DONE
*
*
A000109  EQU   *
         MVC   WKPRINT,SPACES
         MVC   WKPRINT(27),=CL27'TSTUR70: INPUT PARAMETERS: '
         MVC   WKPRINT+27(100),WKPARM
         LA    R2,OUTDCB
         LA    R0,WKPRINT
         PUT   (R2),(R0)
*
         L     R1,WKSUBPA1
         L     R2,WKSUBPL1
         MVC   WKTASKS,=H'1'      default value
         CLC   0(5,R1),=CL5'TASKS'
         JNE   A00120
         JAS   R14,SUBTASKS
         CLC   WKTASKS,=H'20'                  To many subtasks..?
         JNH   A00120                          Set lower
         MVC   WKTASKS,=H'20'
         wto 'TSTUR70 : too many tasks -- now set to TASKS=20'
A00120   EQU   *
*
***********************************************************************
*  LOAD GVBUR70                                                       *
***********************************************************************
         LOAD  EPLOC=LINKNAME,ERRET=A0004
         OILH  R0,MODE31
         ST    R0,WKUR70A
         J     A0006
A0004    EQU   *
         WTO 'TSTUR70 : UNABLE TO LOAD GVBUR70'
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
***********************************************************************
* What's our mission: single/multi threaded, or subtask ?
***********************************************************************
         CLC   WKTASKS,=H'0'                   Is this a subtask ?
         JE    A0010A                          No, Don't do open call
*                                              Or start subtasks
         MVC   WKPRINT,SPACES
         MVC   WKPRINT(32),=CL32'TSTUR70: CALLING INIT (#threads)'
         LA    R2,OUTDCB
         LA    R0,WKPRINT
         PUT   (R2),(R0)
*
         XC    UR70ANCH,UR70ANCH
         MVC   UR70FUN,=CL8'INIT'             Set number of threads
         LH    R0,WKTASKS                     Number of subtasks..
         STH   R0,UR70OPNT                    Number of subtasks needed
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
         MVC   WKPRINT(33),=CL33'TSTUR70: CALLED INIT SUCCESSFULLY'
         LA    R2,OUTDCB
         LA    R0,WKPRINT
         PUT   (R2),(R0)
*
***********************************************************************
         CLC   WKTASKS,=H'1'                   Is this a mother..?
         JE    A0010A                          No, Don't start subtasks
*
         WTO 'TSTUR70 : STARTING SUBTASKS'
         MVC   WKDDPRML,=H'7'
         MVC   WKDDPARM,=CL30'TASKS=0'
         LAY   R0,WKDDPRML
         ST    R0,WKEPARMA
*
         LH    R5,WKTASKS
         LAY   R6,WKTCBSUB
         LAY   R7,WKECBSUB
A0010C   DS    0H
         LAY   R1,WKEPARMA          start the subtasks (passed to subt)
*
         basr  r9,0
         USING *,R9
* * * *  ATTACH EP=TSTUR70,         entry point of subtask             +
               SVAREA=YES,                                             +
               ECB=(7)
*
         MVC   WKREENT(IHB0034L),IHB0034
         LA    R15,IHB0034
         ST    R7,8(,R15)
         OI    8(R15),X'80'
         SVC   42                   Attach subtask
*
         ST    R1,0(,R6)
*
         LA    R6,4(,R6)
         LA    R7,4(,R7)
         BRCT  R5,A0010C
         DROP  R9
*
         LA    R1,WKECBLST          build list of ECB addresses
         LA    R2,WKECBSUB
         LH    R5,WKTASKS
A0010D   EQU   *
         LA    R0,0(,R2)
         ST    R0,0(,R1)
         LA    R1,4(,R1)
         LA    R2,4(,R2)
         BRCT  R5,A0010D
         S     R1,=A(4)
         OI    0(R1),X'80'
*
         WTO 'TSTUR70 : WAITING FOR SUBTASKS TO COMPLETE'
         LH    R5,WKTASKS           wait for all to complete
A0010E   EQU   *
         LAY   R1,WKECBLST
         WAIT  1,ECBLIST=(1)
         WTO 'TSTUR70 : SUBTASK has COMPLETED'
         BRCT  R5,A0010E
*
         WTO 'TSTUR70 : DETACHING SUBTASKS'
*
         LAY   R4,WKTCBSUB
         LH    R5,WKTASKS           detach them
A0010F   EQU   *
         DETACH (4)
         LA    R4,4(,R4)
         WTO 'TSTUR70 : SUBTASK is DETACHED'
         BRCT  R5,A0010F
         WTO 'TSTUR70 : ALL SUBTASKS HAVE COMPLETED'
*
         J     DONE                 Now we're done
*
***********************************************************************
A0010A   EQU   *
         WTO 'TSTUR70 : ABOUT TO CALL JAVA METHOD'
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
         WTO 'TSTUR70 : HAS CALLED JAVA METHOD'
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
         L     R13,4(,R13)             restore caller save area
         ST    R15,16(,R13)
         LM    R14,R12,12(R13)         restore caller's registers
         BR    R14                     RETURN
*
***********************************************************************
*
* TASKS= sub parameter (R1 => subparameter, R2 = length)
*
SUBTASKS DS    0H
         STM   R14,R12,WKSAVE2+12
*
         CLC   0(6,R1),=CL6'TASKS='
         JNE   A000170
         CHI   R2,7                    Too little
         JL    A000170
         CHI   R2,8                    Too much
         JH    A000170
         LA    R4,6(,R1)
         LR    R3,R2
         AHI   R3,-6
A000190  EQU   *                       Check for numerics
         CLI   0(R4),C'0'
         JL    A000170
         CLI   0(R4),C'9'
         JH    A000170
         LA    R4,1(,R4)
         BRCT  R3,A000190
*
         LA    R1,6(,R1)               First digit of number

         AHI   R2,-7                   Number length - 1 =L2
         OY    R2,=Xl4'00000070'       Set L1 in pack's L1L2
         EXRL  R2,EXEPACK
         CVB   R0,WKDBL1
         STH   R0,WKTASKS
*
         wto 'TSTUR70 : TASKS subtasks parameter specified'
         J     A000180
A000170  EQU   *
         wto 'TSTUR70 : error in TASKS sub parameter'
A000180  EQU   *
*
         LM    R14,R12,WKSAVE2+12
         BR    R14
*
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
MVCPARM  MVC   WKPARM(0),0(R1)    EXECUTED <<< <<< <<< <<<
*
EXEPACK  PACK  WKDBL1(0),0(0,R1)
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
IHB0034  DS    0F
         DC    A(*+28)
         DC    A(0)
         DC    A(X'80000000')
         DC    A(0)
         DC    A(0)
         DC    A(0)
         DC    AL2(0)
         DC    AL1(0)
         DC    AL1(0)
         DC    CL8'TSTUR70'
         DC    A(0)
         DC    A(0)
         DC    A(0)
         DC    A(0)
         DC    AL1(0)
         DC    AL1(0)
         DC    AL2(72)
         DC    A(0)
         DC    AL1(0)
         DC    AL1(1)
         DC    XL10'00'
IHB0034L EQU   *-IHB0034
*
         LTORG ,
         DS   0F
         DCBD  DSORG=PS
*
         IHADCBE
         END
