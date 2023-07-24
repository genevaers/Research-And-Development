         TITLE 'GVBJINF - interface to ASM service routines'    
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
* ASMINF64 - JNI Interface to ASM service routines (64 bit)                                    
*                                                                               
***********************************************************************         
         YREGS                                                                  
*                                                                               
ASMINF64 CELQPRLG PARMWRDS=1,BASEREG=R10,EXPORT=YES                             
         LG    R9,0(R1)                R9 -> Input area                         
         USING PARMSTR,R9
         LG    R7,PAADDR1              R7 -> Outbound buffer
         LG    R8,PAADDR2              R8 -> Return buffer
* -----------------------------------------------------------                   
* Check the value of our input area, and get Sysplex/Host                       
* as required                                                                   
* -----------------------------------------------------------                   
         CLC   PAFUN(7),=C'SYSINFO'    Getting system info?
         JNE   A0010                   Yes - Go get it 
         CLC   0(4,R7),=C'HOST'        Getting sysplex?
         JE    HOST                    Yes - Go get it                          
         CLC   0(7,R7),=C'SYSPLEX'     Getting sysplex?
         JE    SYSPLEX                 Yes - Go get it
         CLC   0(7,R7),=C'IPLTIME'     Getting IPL date time
         JE    IPLTIME                 Ok
         LA    R3,62
         J     EXIT
A0010    EQU   *
         CLC   PAFUN(7),=C'RUNMAIN'    Obtain CTT from main()
         JE    CDMAIN                  Yes - Go get it                          
         CLC   PAFUN(7),=C'RUNMR95'    Run MR95 from thread
         JE    CDLOAD                  Yes - Go get it                          
         CLC   PAFUN(8),=C'WAITMR95'   Wait on event(s)
         JE    WAITM                   Yes - Go get it                          
         CLC   PAFUN(8),=C'POSTMR95'   Post event(s)
         JE    POST                    Yes - Go get it
         LA    R3,64                   Else Bad Return Code
         J     EXIT
* -----------------------------------------------------------                   
* Get the Host Name from the SMCA Control Block                                 
* -----------------------------------------------------------                   
HOST     DS    0H                                                               
         LLGT  R2,CVTPTR               R2 -> CVT                                
         LLGT  R2,CVTSMCA-CVT(R2)      R2 -> SMCA
         MVC   8(L'SMCASID,R8),SMCASID-SMCABASE(R2) SMFID
         J     EXIT0                   And Exit
* -----------------------------------------------------------                   
* Get the Sysplex Name from the Extended CVT                                    
* -----------------------------------------------------------                   
SYSPLEX  DS    0H                                                               
         LLGT  R2,CVTPTR               R2 -> CVT                                
         LLGT  R2,CVTECVT-CVT(,R2)     R2 -> ECVT
         MVC   8(L'ECVTSPLX,R8),ECVTSPLX-ECVT(R2)   Sysplex
         J     EXIT0                   And Exit                                 
* -----------------------------------------------------------                   
* Get the IPL date and time -- must be APF
* -----------------------------------------------------------                   
IPLTIME  DS    0H
         LGHI  R0,WORKLEN
         GETMAIN RU,LV=(0),LOC=(31)
         STG   R13,WKSAV13-WORKAREA(R1)
         LLGTR R13,R1
         USING WORKAREA,R13
*
         testauth fctn=1      set r15 - not zero means not auth
         ltr   r15,r15
         jnz   ipltimey       not APF authorized -- skip
*
         MVC   4(4,R13),=CL4'F4SA'
         LAY   R6,WKREENT
*
         MVC   0(20,R6),=CL20'YYYY-MM-DD HH.MM.SS '
         LGHI  R1,16
         XGR   R15,R15
         ICM   R15,B'1111',X'234'(R1)   R15=>ASCB
         JZ    IPLTIMEX
I0010    EQU   *
         CLC   0(4,R15),=CL4'ASCB'
         JNE   IPLTIMEX
         LGH   R14,X'24'(R15)
         C     R14,=A(1)
         JE    I0014
         LLGT  R15,4(,R15)
         J     I0010
I0014    EQU   *
         MVC   IPLSTCK,X'130'(R15)      IPL TIME
         LLGT  R14,IPLSTCK
         AL    R14,X'130'(R1)           TIME DIFFERENCE
         ST    R14,IPLSTCK
*
         STCKCONV STCKVAL=IPLSTCK,                                     X
               CONVVAL=W_TOD,                                          X
               TIMETYPE=DEC,                                           X
               DATETYPE=YYYYMMDD,                                      X
               MF=(E,CONV_MFL)
*
         OI    W_TOD+3,X'0F'
         XGR   R1,R1
         ICM   R1,15,W_TOD
         ST    R1,W_TOD+12
         MVC   WORKDATE,PATTERN
         ED    WORKDATE,W_TOD+8
         MVC   0(4,R6),WORKDATE+00
         MVC   5(2,R6),WORKDATE+04
         MVC   8(2,R6),WORKDATE+06
         MVC   11(2,R6),WORKDATE+08
         MVC   14(2,R6),WORKDATE+10
         MVC   17(2,R6),WORKDATE+12
         MVC   8(20,R8),0(R6)
IPLTIMEX EQU   *
         LGHI  R0,WORKLEN
         LLGTR R1,R13
         LG    R13,WKSAV13
         FREEMAIN RU,LV=(0),A=(1)
         J     EXIT0
         DROP  R13 WORKAREA
*
IPLTIMEY EQU   *
         MVC   8(8,R8),=CL8'NOTAPF'
         J     EXIT0                   And Exit                                 
* -----------------------------------------------------------                   
* Load a module and invoke in 31 bit mode                                       
* -----------------------------------------------------------                   
CDMAIN   DS    0H
         sysstate amode64=NO                                                    
         sam31                                                                  
*
         LGHI  R0,WORKLEN
         GETMAIN RU,LV=(0),LOC=(31)
         STG   R13,WKSAV13-WORKAREA(R1)
         LLGTR R13,R1
         USING WORKAREA,R13
*
         LLGT  R15,=V(GVBJMAIN)
         LGR   R1,R9                   Points to Parmstruct
         BASR  R14,R15
         LGR   R3,R15
         LGHI  R0,WORKLEN
         LLGTR R1,R13
         LG    R13,WKSAV13
         FREEMAIN RU,LV=(0),A=(1)
         DROP  R13 WORKAREA
*                                                                               
         sam64                                                                  
         sysstate amode64=YES                                                   
         J     EXIT                    And Exit                                 
* -----------------------------------------------------------                   
* Load a module and invoke in 31 bit mode                                       
* -----------------------------------------------------------                   
CDLOAD   DS    0H
         sysstate amode64=NO                                                    
         sam31                                                                  
*
         LGHI  R0,WORKLEN
         GETMAIN RU,LV=(0),LOC=(31)
         STG   R13,WKSAV13-WORKAREA(R1)
         LLGTR R13,R1
         USING WORKAREA,R13
*
         LLGT  R15,=V(GVBJMR95)
         LGR   R1,R9                   Points to Parmstruct
         BASR  R14,R15
         LGR   R3,R15
         LGHI  R0,WORKLEN
         LLGTR R1,R13
         LG    R13,WKSAV13
         FREEMAIN RU,LV=(0),A=(1)
         DROP  R13 WORKAREA
*                                                                               
         sam64                                                                  
         sysstate amode64=YES                                                   
         J     EXIT                    And Exit                                 
* -----------------------------------------------------------                   
* Wait on multiple events                                                       
* -----------------------------------------------------------
WAITM    DS    0H                                                               
         CLC   PAOPT(4),=CL4'TERM'
         JNE   WAIT119
         LGHI  R0,-1                   WAIT on CTTTECB
         J     WAIT18
WAIT119  EQU   *
         CLC   PAOPT(4),=CL4'GO95'
         JNE   WAIT120
         LGHI  R0,0                    WAIT on CTTTECB + CTTGECB
         J     WAIT18
WAIT120  EQU   *
         CLC   PAOPT(4),=CL4'0001'
         JNE   WAIT121
         LGHI  R0,1                    Index of ECB to wait
         J     WAIT18
WAIT121  EQU   *
         CLC   PAOPT(4),=CL4'0002'
         JNE   WAIT122
         LGHI  R0,2                    Index of ECB to wait
         J     WAIT18
WAIT122  EQU   *
         CLC   PAOPT(4),=CL4'0003'
         JNE   WAIT123
         LGHI  R0,3                    Index of ECB to wait
         J     WAIT18
WAIT123  EQU   *
         CLC   PAOPT(4),=CL4'0004'
         JNE   WAIT124
         LGHI  R0,4                    Index of ECB to wait
         J     WAIT18
WAIT124  EQU   *
         CLC   PAOPT(4),=CL4'0005'
         JNE   WAIT125
         LGHI  R0,5                    Index of ECB to wait
         J     WAIT18
WAIT125  EQU   *
         wto 'illegal wait'
         dc    h'0'
WAIT18   EQU   *
         STH   R0,PAOPT+4              Directions to ECB's
*
         sysstate amode64=NO                                                    
         sam31                                                                  
*                                                                               
         LGHI  R0,WORKLEN
         GETMAIN RU,LV=(0),LOC=(31)
         STG   R13,WKSAV13-WORKAREA(R1)
         LLGTR R13,R1
         USING WORKAREA,R13
*
         LLGT  R15,=V(GVBJWAIT)
         LGR   R1,R9                   Points to Parmstruct
         BASR  R14,R15
         LGR   R3,R15 
         LGHI  R0,WORKLEN
         LLGTR R1,R13
         LG    R13,WKSAV13
         FREEMAIN RU,LV=(0),A=(1)
         DROP  R13 WORKAREA
*                                                                               
         sam64                                                                  
         sysstate amode64=YES                                                   
         J     EXIT                    And Exit                                 
* -----------------------------------------------------------                   
* Post for event                                                                
* -----------------------------------------------------------                   
POST     DS    0H
*
         CLC   PAOPT(4),=CL4'WRKT'     Poke all worker threads
         JNE   POST119                   to terminate
         LGHI  R0,-1                   Index of ECBs to post
         J     POST18
POST119  EQU   *
         CLC   PAOPT(4),=CL4'ACKG'
         JNE   POST120
         LGHI  R0,0                    Index of ECB to post
         J     POST18
POST120  EQU   *
         CLC   PAOPT(4),=CL4'0001'
         JNE   POST121
         LGHI  R0,1                    Index of ECB to post
         J     POST18
POST121  EQU   *
         CLC   PAOPT(4),=CL4'0002'
         JNE   POST122
         LGHI  R0,2                    Index of ECB to post
         J     POST18
POST122  EQU   *
         CLC   PAOPT(4),=CL4'0003'
         JNE   POST123
         LGHI  R0,3                    Index of ECB to post
         J     POST18
POST123  EQU   *
         CLC   PAOPT(4),=CL4'0004'
         JNE   POST124
         LGHI  R0,4                   Index of ECB to post
         J     POST18
POST124  EQU   *
         CLC   PAOPT(4),=CL4'0005'
         JNE   POST125
         LGHI  R0,5                   Index of ECB to post
         J     POST18
POST125  EQU   *
         wto 'illegal post'
         dc    h'0'
POST18   EQU   *
         STH   R0,PAOPT+4             Directions to ECB
*
         sysstate amode64=NO                                                    
         sam31 
*
         LGHI  R0,WORKLEN
         GETMAIN RU,LV=(0),LOC=(31)
         STG   R13,WKSAV13-WORKAREA(R1)  
         LLGTR R13,R1
         USING WORKAREA,R13
*
         LLGT  R15,=V(GVBJPOST)
         LGR   R1,R9                   Points to Parmstruct
         BASR  R14,R15
         LGR   R3,R15
         LGHI  R0,WORKLEN
         LLGTR R1,R13
         LG    R13,WKSAV13
         FREEMAIN RU,LV=(0),A=(1)
         DROP  R13 WORKAREA
*                                                                               
         sam64                                                                  
         sysstate amode64=YES                                                   
         J     EXIT                    And Exit                                 
* -----------------------------------------------------------                   
* Zero Return Code and Exit                                                     
* -----------------------------------------------------------                   
EXIT0    DS    0H                                                               
         XR    R3,R3                   Zero Return Code                         
EXIT     DS    0H                                                               
         CELQEPLG                      Return to caller                         
MODE31   EQU   X'8000'
PATTERN  DC    X'202020202020202020202020202020 '
TKNNAME  DC    CL8'GVBJMR95'                                          
GENEVA   DC    CL8'GENEVA'                                            
TOKNPERS DC    F'0'                    TOKEN PERSISTENCE              
TOKNLVL1 DC    A(1)                    NAME/TOKEN  AVAILABILITY  LEVEL
TOKNLVL2 DC    A(2)                    NAME/TOKEN  AVAILABILITY  LEVEL
*
         LTORG                                                                  
* -----------------------------------------------------------                   
* Mapping Macros and DSECTs                                                     
* -----------------------------------------------------------
*
WORKAREA DSECT
WKSAVE   DS    18FD            64 bit savearea
WKSAV13  DS    D
WKRETC   DS    D
         DS    0F                                          
WKTIMWRK DS   0XL16  
WKDBLWRK DS    D     
WKDBLWK2 DS    D     
WKDBLWK3 DS    D     
WKREENT  DS    XL256           RE-ENTRANT PARAMETER   LIST
WKPRINT  DS    XL131           Print line                    
WKTRACE  DS    CL1             Tracing                       
         DS   0F
WKTOKNRC DS    A                  NAME/TOKEN  SERVICES RETURN CODE
WKTOKNAM DS    XL16               TOKEN NAME                      
WKTOKN   DS   0XL16               TOKEN VALUE                     
WKTOKNCTT DS   A                  A(CTT)                          
         DS    A
         DS    A
         DS    A
WKDBLWK  DS    XL08            Double work workarea
WORKDATE DS    CL16    
W_TOD    DS    CL16    
IPLSTCK  DS    D       
CONV_MFL DS    0F      
         STCKCONV MF=L 
CONV_LEN EQU   *-CONV_MFL
WORKLEN  EQU   *-WORKAREA
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
         CEEDSA SECTYPE=XPLINK         Dynamic Save Area
         CEECAA ,
*
         CVT DSECT=YES                 Map CVT                                  
         IHAECVT                       Map ECVT                                 
         IEESMCA                       Map SMCA                                 
         END                                                                    
