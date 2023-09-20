/**********************************************************************
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
*
*   This program is invoked by Java using JNI and then calls asminf264
*
***********************************************************************   
* Java declaration */

#include <stdlib.h>
#include <zOSInfo2.h>
#include <unistd.h>
#include <string.h>

/* #define GVBDEBUG */

/* JNIEXPORT jstring JNICALL Java_zOSInfo2_showZo2 (JNIEnv *, jobject, jint, jstring, jstring, jstring, jint); */
 
/*========================================================  
 Mainline Code                                 
 ======================================================== */
JNIEXPORT jstring JNICALL
Java_zOSInfo2_showZos2(JNIEnv *env
                     ,jobject obj
                     ,jint    jFid
                     ,jstring jParm
                     ,jstring jOpt
                     ,jstring stringin
                     ,jint    lengthout
                     )  
{                                                

    long ASMINF(char **);
    long ASMINF64(char **);
    long GVBJMEM(char **);

   /* --- Statics ----------------------------------------*/

    static char TheAnchor[8] = {0,0,0,0,0,0,0,0};

   /* --- Variables ------------------------------------- */
    jstring      res;
    long         rc;                   /* rc from ASMINF  */
    long         len1;
    long         len2;
    long         actual_len2 = 0;
    long         fid;
    char        *passarea;             /* ptr to parms    */
    char        *passarea1;
    char        *passarea2;
    const char  *msg; 
    PassStruct  *pStruct;
    Pass2Struct *pStruct2;

    fid = jFid;

   /* ------ Set up control block ------                    */
    passarea = malloc(sizeof(PassStruct));   /* Get area    */
    memset(passarea, 0, sizeof(PassStruct)); /* Clear area  */
    pStruct = (PassStruct*)passarea;
    memcpy(pStruct->anchor, TheAnchor, 8);
    
    #ifdef GVBDEBUG
    printf("pStruct->anchor: %0.2x%0.2x%0.2x%0.2x%0.2x%0.2x%0.2x%0.2x\n", 
            pStruct->anchor[0], pStruct->anchor[1], pStruct->anchor[2], pStruct->anchor[3], 
            pStruct->anchor[4], pStruct->anchor[5], pStruct->anchor[6], pStruct->anchor[7]);
    #endif

    #ifdef GVBDEBUG
    printf("Fid %d\n", fid);
    #endif

   /* --- Get the jparm input, and convert to EBCDIC  ----*/
    msg=(*env)->GetStringUTFChars(env,jParm,0); 
    __atoe((char*) msg);                     
    (*env)->ReleaseStringUTFChars(env, jParm, msg);
    strncpy(pStruct->func, msg, 8);    /* Put parm in     */ 

    #ifdef GVBDEBUG
    printf("Fid %d(%s)\n", fid, pStruct->func);
    #endif

   /* --- Get the Jopt input, and convert to EBCDIC  ---- */
    msg=(*env)->GetStringUTFChars(env,jOpt,0); 
    __atoe((char*) msg);                     
    (*env)->ReleaseStringUTFChars(env, jOpt, msg);
    strncpy(pStruct->opt, msg, 8);     /* Put options in  */
    
    #ifdef GVBDEBUG
    printf("Fid %d(%s-%s)\n", fid, pStruct->func, pStruct->opt);
    #endif

   /* --- Get length of input string (min is 8)      ----*/
    len1=(*env)->GetStringUTFLength(env, stringin);
    if (len1 > 8) {
      pStruct->length1 = len1;
    } else {
      pStruct->length1 = 8;
    }

    #ifdef GVBDEBUG
    printf("Fid %d(%s-%s) pStruct->length1 %d\n", fid, pStruct->func, pStruct->opt,pStruct->length1);
    #endif

   /* --- Get the stringin, and convert to EBCDIC  ----  */
    msg=(*env)->GetStringUTFChars(env, stringin, 0); 
    __atoe((char*) msg);                 
    (*env)->ReleaseStringUTFChars(env, stringin, msg);

   /* --- Get max length of expected output string  ---- */
    len2 = lengthout;
 
    #ifdef GVBDEBUG
    printf("Fid %d(%s-%s) len1 %d (%d) len2 %d\n", fid, pStruct->func, pStruct->opt, len1, pStruct->length1, len2);
    #endif

   /* -- Buffer to give converted input data to ASMINF --*/
    passarea1 = malloc(pStruct->length1);   /* Get area   */
    memset(passarea1, 0, pStruct->length1); /* Clear area */
    strncpy(passarea1, msg, len1);     /* Put parm in     */
    pStruct->addr1   = passarea1;

    #ifdef GVBDEBUG
    printf("Fid %d passarea1 %0.8s\n", fid, passarea1);
    #endif

   /* add prefix for return/reason and suffix x'00' */
   /* add more for Class(32) and Method(32)         */
    actual_len2 = len2 + 8 + 8 + 32 + 32 + 1;
    #ifdef GVBDEBUG
    printf("len2 %d actual_len2 %d\n", len2, actual_len2);
    #endif

    pStruct->length2 = len2; /* this is still the length of the data to be returned after prefix */
    #ifdef GVBDEBUG
    printf("Fid %d actual_len2 %d : pStruct->length2 %d\n", fid, actual_len2, pStruct->length2);
    #endif

   /* --- Place for ASMINF to put the results back ----- */
    passarea2 = malloc(actual_len2);  /* Get area        */
    memset(passarea2, 0, actual_len2);/* Clear area      */
    pStruct->addr2   = passarea2;      /* Use from offset8*/
    pStruct2 = (Pass2Struct*)passarea2;

    #ifdef GVBDEBUG
    printf("Fid %d passarea2 %s\n", fid, passarea2);
    #endif

   /* --- Load the correct JNI module: 31 or 64 bit  ---- */

    #ifdef GVBDEBUG
    printf("fid %d passarea1 %0.8s\n", fid, pStruct->addr1);
    printf("fid %d length1 %d length2 %d addr1 %0.8s addr2 %s\n", fid, pStruct->length1, pStruct->length2, pStruct->addr1, pStruct->addr2 );
    printf("Calling Assembler codex \n");
    #endif

    #ifdef _LP64                             
       rc = GVBJMEM(&passarea);
       #ifdef GVBDEBUG
       printf("GVBJMEM returns %d and thrdmem %d\n", rc, pStruct->thrdmem);
       #endif
    #endif

    #  ifdef _LP64                             
       rc = ASMINF64(&passarea);
       #ifdef GVBDEBUG
       printf("ASMINF64 returns %d for function %s\n", rc, pStruct->func);
       #endif
    #else
       rc = ASMINF(&passarea);
       #ifdef GVBDEBUG
       printf("ASMINF returns %d for function %s\n", rc, pStruct->func);
       #endif
    #endif

    #ifdef GVBDEBUG
    printf("Fid %d(%s) rc=%d\n", fid, pStruct->func, rc);
    #endif

    switch (fid) {
      /* system information function */ 
      case 0:
        if (rc == 0 )
          memcpy(passarea2, "00000000", 8);
        else
          sprintf(passarea2, "%0.8d", rc);
        break;

      /* run gvbmr95 */  
      case 1:
        sprintf(passarea2, "%0.8d", rc);
        break;

      /* wait on event(s) */  
      /* stuff returned data into passarea2 */
      case 2:
        /*printf("WAITMR95 %d\n", rc);*/
        switch (rc) {
          case 0:                            /*Either MR95 post */
            memcpy(passarea2, "00000000", 8);
            break;
          case 2:                             /*Waiting and got */
            memcpy(passarea2, "00000002", 8);
            memcpy(passarea2+8, "POSTEDT ", 8); /*Term post     */
            break;
          case 4:                             /*Waiting and got */
            memcpy(passarea2, "00000004", 8);
            memcpy(passarea2+8, "POSTEDW ", 8); /*work to do..  */
            /*So - Information returns starting at passarea2[16]*/
            /* ASM CODE MUST CHECK IT'S NO LONGER THAN pStruct->length2*/
            /*printf("resultant: %s\n", pStruct2->u.resultant);*/
            break;
          case 6:                             /*Waiting and got */
            memcpy(passarea2, "00000006", 8);
            memcpy(passarea2+8, "POSTEDG ", 8); /* GO posted    */

            #ifdef GVBDEBUG
            printf("passarea2[16-23]: %0.2x%0.2x%0.2x%0.2x %0.2x%0.2x%0.2x%0.2x\n", 
            passarea2[16], passarea2[17], passarea2[18], passarea2[19], 
            passarea2[20], passarea2[21], passarea2[22], passarea2[23]);
            #endif
        
            #ifdef GVBDEBUG
            printf("additions1: %d\n", pStruct2->u.additions1);
            #endif

            printf("Number of MR95 threads: %0.4d\n",(long) pStruct2->u.additions1);
            sprintf(pStruct2->u.resultant,"%0.4d",pStruct2->u.additions1);
            break;
          case 8:
            memcpy(passarea2, "00000008", 8);
            memcpy(passarea2+8, "IEANTRTF", 8); /*Error retrieveTS*/
            break;
          case 12:
            memcpy(passarea2, "00000012", 8);
            memcpy(passarea2+8, "IEANTCRF", 8); /*Error create TS */
            break;
          case 16:
            memcpy(passarea2, "00000016", 8);
            memcpy(passarea2+8, "FILEERR ", 8);   /*File I/O error*/
            break;
          case 20:
            memcpy(passarea2, "00000020", 8);
            memcpy(passarea2+8, "ECBLERR ", 8);   /*ECB list errer*/
            break;
          default:
            sprintf(passarea2, "%0.8d", rc);
            memcpy(passarea2+8, "OTHERERR", 8);   /*Other  */
            break;
        }
        break;

      /* post event(s) */
      case 3:
        sprintf(passarea2, "%0.8d", rc);
        break;

      /* runmain gets communications area */  
      case 4:
        #ifdef GVBDEBUG
        printf("RUNMAIN: %d\n", rc);
        #endif

        sprintf(passarea2, "%0.8d", rc);
        if (rc == 0) {
          memcpy(passarea2+8, "00000000", 8);
          sprintf(passarea2+16,"%0.2x%0.2x%0.2x%0.2x%0.2x%0.2x%0.2x%0.2x", 
                  pStruct->anchor[0], pStruct->anchor[1], pStruct->anchor[2], pStruct->anchor[3], 
                  pStruct->anchor[4], pStruct->anchor[5], pStruct->anchor[6], pStruct->anchor[7]);
          memcpy(TheAnchor, pStruct->anchor, 8);
        }
        else {
          memcpy(passarea2+8, "BADBAD  ", 8); /* -- Error-- */
        }
        break;

      /* who knows ? */
      default:
        printf("Fid %d(%s) returns %d\n", fid, pStruct->func, rc);
        sprintf(passarea2, "%0.8d%8s", rc, pStruct->func);
        break;
    }

   /* --- Convert area returned by HLASM back to ASCII  - */
    __etoa((char*) passarea2);                        
                                                
    free(passarea);
    free(passarea1);
   /* free(passarea2); */

   /* --- And return it --------------------------------- */
   /* return (*env)->NewStringUTF(env, passarea2); */

    res = (*env)->NewStringUTF(env, passarea2);

    free(passarea2);

    return res;

}                                      /* main            */
