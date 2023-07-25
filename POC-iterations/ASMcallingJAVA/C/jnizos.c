/**********************************************************************
*
* (c) Copyright IBM Corporation 2021.
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
* Java declaration */

#include <stdlib.h>
#include <zOSInfo.h>
#include <unistd.h>
#include <string.h>

/* JNIEXPORT jstring JNICALL Java_zOSInfo_showZos (JNIEnv *, jobject, jstring); */
 
/*========================================================  
 Mainline Code                                 
 ======================================================== */
JNIEXPORT jstring JNICALL
Java_zOSInfo_showZos(JNIEnv *env,jobject obj,jstring jParm)  
{                                                
                                                   
   /* --- Variables ------------------------------------- */
    int rc;                            /* rc from ASMINF  */
    char *passarea;                    /* ptr to parms    */
                                                  
   /* --- Get the parm input, and convert to EBCDIC  ---- */
    const char* msg=(*env)->GetStringUTFChars(env,jParm,0); 
    __atoe((char*) msg);                     
    (*env)->ReleaseStringUTFChars(env, jParm, msg);
   
   /* --- Setup 8 Byte Area to pass (leave space for NULL)*/
    passarea = malloc(10);             /* Get area        */
    memset(passarea, 0, 10);           /* Clear area      */
    strcpy(passarea,msg);              /* Put parm in     */
                                              
   /* --- Load the correct JNI module: 31 or 64 bit  ---- */
    #  ifdef _LP64                             
       rc = ASMINF64(&passarea);                     
    #else                                          
       rc = ASMINF(&passarea);                  
    #endif                                      
                                              
   /* --- If bad return code from HLASM, modify passarea  */
    if (rc > 0) strcpy(passarea, "(invalid)");         
                                              
   /* --- Convert area returned by HLASM back to ASCII  - */
    __etoa((char*) passarea);                        
                                                
   /* --- And return it --------------------------------- */
    return (*env)->NewStringUTF(env, passarea);      
                                             
}                                      /* main            */
