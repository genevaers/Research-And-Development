**********************************************************************
*
* (C) COPYRIGHT IBM CORPORATION 2023.
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
*                                                           
* ASMINF - Provide z/OS Host or Sysplex Name            
*                                                       
************************************************************************
R0       EQU   0 
R1       EQU   1 
R2       EQU   2 
R3       EQU   3 
R4       EQU   4 
R5       EQU   5 
R6       EQU   6 
R7       EQU   7 
R8       EQU   8 
R9       EQU   9 
R10      EQU   10
R11      EQU   11
R12      EQU   12
R13      EQU   13
R14      EQU   14
R15      EQU   15

ASMINF   EDCXPRLG PARMWRDS=5,BASEREG=R10,EXPORT=YES      
         L     R9,0(R1)                R9 -> Input area   
                                          
* -----------------------------------------------------------
* Check the value of our input area, and get Sysplex/Host  
* as required                                
* -----------------------------------------------------------
         CLC   0(7,R9),=C'SYSPLEX'     Getting sysplex?    
         BE    SYSPLEX                 Yes - Go get it 
         CLC   0(4,R9),=C'HOST'        Getting sysplex?  
         BE    HOST                    Yes - Go get it   
         LA    R3,8                    Else Bad Return Code 
         B     EXIT                      
                                           
* -----------------------------------------------------------
* Get the Host Name from the SMCA Control Block
* -----------------------------------------------------------
HOST     DS    0H                        
         LLGT  R2,CVTPTR               R1 -> CVT     
         LLGT  R2,CVTSMCA-CVT(R2)      R2 -> SMCA     
         MVC   0(L'SMCASID,R9),SMCASID-SMCABASE(R2) SMFID
         B     EXIT0                   And Exit
                                                            
* -----------------------------------------------------------
* Get the Sysplex Name from the Extended CVT
* -----------------------------------------------------------
SYSPLEX  DS    0H                             
         LLGT  R2,CVTPTR               R1 -> CVT  
         LLGT  R2,CVTECVT-CVT(,R2)     R2 -> ECVT          
         MVC   0(L'ECVTSPLX,R9),ECVTSPLX-ECVT(R2)   Sysplex
         B     EXIT0                   And Exit
                                                           
* -----------------------------------------------------------
* Zero Return Code and Exit                  
* -----------------------------------------------------------
EXIT0    DS    0H                                  
         MVC   0(8,R9),=CL8'EVCTSPLX' 
         XR    R3,R3                   Zero Return Code      
EXIT     DS    0H                                            
         EDCXEPLG                      Return to caller
         LTORG 
* -----------------------------------------------------------
* Mapping Macros and DSECTs                 
* -----------------------------------------------------------
         CVT DSECT=YES                 Map CVT
	 IHAECVT                       Map ECVT
         IEESMCA                       Map SMCA
         END
         