         TITLE 'GVBJMEM - obtain 31 bit thread local storage'
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
* GVBJMEM - Get Address of or obtain (first time) thread local storage
*                                                                               
***********************************************************************
         YREGS
*
         COPY  GVBJDSCT
*                                                                               
GVBJMEM  CELQPRLG PARMWRDS=1,BASEREG=R10,EXPORT=YES,DSASIZE=160+WORKLEN
         LG    R9,0(R1)                R9 -> Input area                         
         USING PARMSTR,R9
         LAY   R8,MYWORK-THEDSA(,R4)
         USING WORKAREA,R8
*
* -----------------------------------------------------------                   
* Retrieve token
* -----------------------------------------------------------                   
FINDMEM  DS    0H
         XC    WKTOKNRC,WKTOKNRC
         MVC   WKTOKNAM+0(8),GENEVA
         MVC   WKTOKNAM+8(8),TKNNAME
         CALL  IEAN4RT,(TOKNLVL1,WKTOKNAM,WKTOKN,WKTOKNRC),            X
               MF=(E,WKREENT)
         LTGF  R15,WKTOKNRC       SUCCESSFUL  ???
         JZ    FINDMEM2
*         WTO 'GVBJMEM : THREAD LOCAL STORAGE NOT YET FOUND'
         J     GETMEM
FINDMEM2 EQU   *
         LLGT  R0,WKTOKMEM
         STG   R0,PAATMEM
         LTR   R0,R0
         JZ    FINDMEM3
*         WTO 'GVBJMEM : THREAD LOCAL STORAGE IS GOOD'
         J     EXIT0
FINDMEM3 EQU   *
         WTO 'GVBJMEM : THREAD LOCAL STORAGE ANCHOR IS BAD'
         LGHI  R3,8
         J     EXIT
* -----------------------------------------------------------                   
* Obtain 31 bit memory
* -----------------------------------------------------------                   
GETMEM   DS    0H
         LGHI  R0,1024+16         Memory plus header
         STORAGE OBTAIN,LENGTH=(0),LOC=(ANY),CHECKZERO=YES
         MVC   0(6,R1),=CL6'GVBMEM'
         MVC   6(10,R1),PATHREAD
         LA    R1,16(,R1)
         STG   R1,PAATMEM
         CIJE  R15,14,GETMEM10
         LGR   R0,R1
         LGHI  R1,1024-16         Clear everything but header
         XGR   R14,R14
         XGR   R15,R15
         MVCL  R0,R14
GETMEM10 EQU   *
*
*         WTO 'GVBJMEM : MEMORY OBTAINED'
*
         XC    WKTOKNRC,WKTOKNRC
         MVC   WKTOKNAM+0(8),GENEVA
         MVC   WKTOKNAM+8(8),TKNNAME
         MVC   WKTOKMEM,PAATMEM+4
         CALL  IEAN4CR,(TOKNLVL1,WKTOKNAM,WKTOKN,TOKNPERS,WKTOKNRC),   +
               MF=(E,WKREENT)
         LTGF  R15,WKTOKNRC       SUCCESSFUL  ???
         JZ    GETMEM20
         WTO 'GVBJMEM : Thread local storage not persisted'
         LGHI  R3,12
         J     EXIT
GETMEM20 EQU   *
*         WTO 'GVBJMEM : Thread local storage is persisted'
* -----------------------------------------------------------                   
* Zero Return Code and Exit                                                     
* -----------------------------------------------------------                   
EXIT0    DS    0H                                                               
         XGR   R3,R3                   Zero Return Code                         
EXIT     DS    0H                                                               
         CELQEPLG                      Return to caller                         
*
TKNNAME  DC    CL8'THRDMEM'                                          
GENEVA   DC    CL8'GENEVA'                                            
TOKNPERS DC    F'0'                    TOKEN PERSISTENCE              
TOKNLVL1 DC    A(1)                    TASK AVAILABILITY  LEVEL
TOKNLVL2 DC    A(2)                    JOB  AVAILABILITY  LEVEL
*
         LTORG                                                                  
* -----------------------------------------------------------                   
* Mapping Macros and DSECTs                                                     
* -----------------------------------------------------------
*
WORKAREA DSECT
WKREENT  DS    XL256              RE-ENTRANT PARAMETER   LIST
WKTOKNRC DS    A                  NAME/TOKEN  SERVICES RETURN CODE
WKTOKNAM DS    XL16               TOKEN NAME                      
WKTOKN   DS   0XL16               TOKEN VALUE                     
WKTOKMEM DS    A                  A(Thread memory)                          
         DS    A
         DS    A
         DS    A
WORKLEN  EQU   *-WORKAREA
*
*
THEDSA   CEEDSA SECTYPE=XPLINK         Dynamic Save Area
MYWORK   DS    XL(WORKLEN)
         CEECAA ,
*
         CVT DSECT=YES                 Map CVT                                  
         IHAECVT                       Map ECVT                                 
         IEESMCA                       Map SMCA                                 
         END                                                                    
