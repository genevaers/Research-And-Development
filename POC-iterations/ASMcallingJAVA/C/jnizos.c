/**********************************************************************
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
*
***********************************************************************
*
*   This program is invoked by Java using JNI and then calls asminf64
*
***********************************************************************   
* Java declaration */

#include <stdlib.h>
#include <zOSInfo.h>
#include <unistd.h>
#include <string.h>

/* #define GVBDEBUG -- this is actually set in DEFS of the build script makegvbdlld */

/* JNIEXPORT jbyteArray JNICALL Java_zOSInfo_showZos (JNIEnv *, jobject, jint, jstring, jstring, jbyteArray, jint); */
 
/*========================================================  
 Mainline Code                                 
 ======================================================== */
JNIEXPORT jbyteArray JNICALL
Java_zOSInfo_showZos(JNIEnv   *env
                    ,jobject    obj
                    ,jint       jFid
                    ,jstring    jParm
                    ,jstring    jOpt
                    ,jbyteArray arrayin
                    ,jint       methodRc
                    )
{

    long ASMINF64(char **);
    long GVBJMEM(char **);

   /* --- Statics ----------------------------------------*/

    static char TheAnchor[8] = {0,0,0,0,0,0,0,0};
    const char* TheFunctions = "RUNMR95 WAITMR95POSTMR95RUNMAIN ";

   /* --- Variables ------------------------------------- */
    jbyteArray   ret;
    jsize        len1;
    jbyte*       bufferPtr;
    long         rc;                   /* rc from ASMINF  */
    long         fid;
    char        *passarea;             /* ptr to parms    */
    char        *passarea2;            /* ptr to ret header*/
    const char  *msg; 
    PassStruct  *pStruct;
    Pass2Struct *pStruct2;
    Genenv      *pEnva;
    char         genparm_digits[8] = {0};
    int          fIndex;

    fid = jFid;
    fIndex = (fid - 1)*8;    /* index to function text name */

   /* ------ Set up control block primary ------            */
    passarea = malloc(sizeof(PassStruct));   /* Get area    */
    memset(passarea, 0, sizeof(PassStruct)); /* Clear area  */
    pStruct = (PassStruct*)passarea;

    memcpy(pStruct->anchor, TheAnchor, 8);   /* sorage anchor (static) */
    pStruct->retcd = methodRc;  /* if rc being sent back, i.e. on post */

    /* ----- Get character representation of function -----*/
    memcpy(pStruct->func, TheFunctions+fIndex, 8);

    /* --- Get the jparm input, and convert to EBCDIC  ----*/
    msg=(*env)->GetStringUTFChars(env,jParm,0); 
    __atoe((char*) msg);                     
    (*env)->ReleaseStringUTFChars(env, jParm, msg);
    memcpy(pStruct->thread, msg, 10);        /* Put parm in     */

    /* --- Get the Jopt input, and convert to EBCDIC  ---- */
    msg=(*env)->GetStringUTFChars(env,jOpt,0); 
    __atoe((char*) msg);                     
    (*env)->ReleaseStringUTFChars(env, jOpt, msg);
    memcpy(pStruct->opt, msg, 8);     /* Put options in  */
    pStruct->flag1   = pStruct->opt[4];
    pStruct->flag2   = pStruct->opt[5];

    /* --- Get addressability to byte arrayin --- */
    bufferPtr = (*env)->GetByteArrayElements(env, arrayin, NULL);
    len1      = (*env)->GetArrayLength(env, arrayin);
    pStruct->length1 = len1;
    pStruct->addr1   = bufferPtr;

    /* --- Get header portion of return data ---- */
    passarea2 = malloc(sizeof(Pass2Struct) + 1); /*leave a null at end*/
    memset(passarea2, 0, sizeof(Pass2Struct) + 1);
    pStruct->addr2   = passarea2;
    pStruct->length2 = sizeof(Pass2Struct);
    pStruct2 = (Pass2Struct*)passarea2;

/*    #ifdef GVBDEBUG */
      printf("test\n");
      printf("%0.10s:Fid %d(%0.8s-%0.4s) pStruct->length1 %d pStruct->length2 %d Common Anchor: %0.2x%0.2x%0.2x%0.2x%0.2x%0.2x%0.2x%0.2x Flag1: %c Flag2: %c\n",
              pStruct->thread, fid,pStruct->func, pStruct->opt, pStruct->length1, pStruct->length2,
              pStruct->anchor[0], pStruct->anchor[1], pStruct->anchor[2], pStruct->anchor[3], 
              pStruct->anchor[4], pStruct->anchor[5], pStruct->anchor[6], pStruct->anchor[7],
              pStruct->flag1, pStruct->flag2);
/*    #endif */

    #ifdef _LP64                             
       rc = GVBJMEM(&passarea);
       #ifdef GVBDEBUG
         printf("%0.10s:Fid %d(%0.8s-%0.4s) GVBJMEM rc: %d, thread local memory: %p\n",
                pStruct->thread, fid, pStruct->func, pStruct->opt, rc, pStruct->thrdmem);
       #endif
    #else
       printf("There is no 31 bit version of GVBJMEM available\n");
    #endif
    
    if (rc > 0) {
      printf("%0.10s:Fid %d(%0.8s-%0.4s) SEVERE ERROR. GVBJMEM rc: %d\n", pStruct->thread, fid, pStruct->func, pStruct->opt, rc);
    }

    #ifdef _LP64                             
       rc = ASMINF64(&passarea);
       #ifdef GVBDEBUG
       printf("%0.10s:Fid %d(%0.8s-%0.4s) ASMINF64 rc: %d\n", pStruct->thread, fid, pStruct->func, pStruct->opt, rc);
       #endif
    #else
       printf("There is no 31 bit version of ASMINF available\n");
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
        if (0 != rc) {
          printf("%0.10s:Fid %d(%0.8s-%0.4s) SEVERE ERROR. Main program initiation returns rc: %d\n",
                  pStruct->thread, fid, pStruct->func, pStruct->opt, rc);
        }
        break;

      /* wait on event(s) */
      case 2:
        switch (rc) {
          case 0:                            /*Either MR95 post */
            memcpy(passarea2, "00000000", 8);
            break;

          case 2:                             /*Waiting and got */
            memcpy(passarea2, "00000002", 8); /*termination post*/
            memcpy(passarea2+8, "POSTEDT ", 8);
            #ifdef GVBDEBUG
              printf("%0.10s:Fid %d(%0.8s-%0.4s) POST for terminate received\n", pStruct->thread, fid, pStruct->func, pStruct->opt);
            #endif
            break;

          case 4:                             /*Waiting and got */
            memcpy(passarea2, "00000004", 8); /* work to do     */
/*          memcpy(passarea2+8, "XXXXYYYY", 8); indicates nature*/
            #ifdef GVBDEBUG
              printf("%0.10s:Fid %d(%0.8s-%0.4s) POST for work received from: %0.4s\n",
                pStruct->thread, fid, pStruct->func, pStruct->opt, pStruct2->reasonCD);
            #endif
            break;

          case 6:                             /*Waiting and got posted to proceed */
            #ifdef GVBDEBUG
              printf("%0.10s:Fid %d(%0.8s-%0.4s) POST for GO recieved. Passarea2(16-23): %0.2x%0.2x%0.2x%0.2x %0.2x%0.2x%0.2x%0.2x Additions1: %d\n",
              pStruct->thread, fid, pStruct->func, pStruct->opt,
              passarea2[16], passarea2[17], passarea2[18], passarea2[19], 
              passarea2[20], passarea2[21], passarea2[22], passarea2[23],
              pStruct2->u.additions1);
            #endif
            memcpy(passarea2, "00000006", 8); /* GvbJavaDaemon posted to GO with 'n' threads */
            memcpy(passarea2+8, "POSTEDG ", 8);
            printf("%0.10s:Fid %d(%0.8s-%0.4s) Posted for GO with %d Java thread(s) requested\n",
                    pStruct->thread, fid, pStruct->func, pStruct->opt, (long) pStruct2->u.additions1);
            sprintf(pStruct2->u.resultant,"%0.4d",pStruct2->u.additions1);
            break;

          case 8:
            printf("%0.10s:Fid %d(%0.8s-%0.4s) SEVERE ERROR from WAIT (IEANTRT) rc: %d\n",
                    pStruct->thread, fid, pStruct->func, pStruct->opt, rc);
            memcpy(passarea2, "00000008", 8);
            memcpy(passarea2+8, "IEANTRTF", 8); /*Error retrieveTS*/
            break;

          case 12:
            printf("%0.10s:Fid %d(%0.8s-%0.4s) SEVERE ERROR from WAIT (CTT does not match) rc: %d\n",
                    pStruct->thread, fid, pStruct->func, pStruct->opt, rc);
            memcpy(passarea2, "00000012", 8);
            memcpy(passarea2+8, "IEANTCRF", 8); /*Error create TS */
            break;

          case 16:
            printf("%0.10s:Fid %d(%0.8s-%0.4s) SEVERE ERROR from WAIT (I/O error) rc: %d\n",
                    pStruct->thread, fid, pStruct->func, pStruct->opt, rc);
            memcpy(passarea2, "00000016", 8);
            memcpy(passarea2+8, "FILEERR ", 8);   /*File I/O error*/
            break;

          case 20:
            printf("%0.10s:Fid %d(%0.8s-%0.4s) SEVERE ERROR from WAIT (CTT table not active) rc: %d\n",
                    pStruct->thread, fid, pStruct->func, pStruct->opt, rc);
            memcpy(passarea2, "00000020", 8);
            memcpy(passarea2+8, "ECBLERR ", 8);   /*ECB list error*/
            break;

          case 24:
            printf("%0.10s:Fid %d(%0.8s-%0.4s) SEVERE ERROR from WAIT (ECB list error) rc: %d\n",
                    pStruct->thread, fid, pStruct->func, pStruct->opt, rc);
            memcpy(passarea2, "00000020", 8);
            memcpy(passarea2+8, "ECBLERR ", 8);   /*ECB list error*/
            break;

          default:
            printf("%0.10s:Fid %d(%0.8s-%0.4s) SEVERE ERROR from WAIT (unknown) rc: %d\n",
                    pStruct->thread, fid, pStruct->func, pStruct->opt, rc);
            sprintf(passarea2, "%0.8d", rc);
            memcpy(passarea2+8, "OTHERERR", 8);   /*Other  */
            break;
        }
        break;

      /* post event(s) */
      case 3:
        #ifdef GVBDEBUG
          printf("%0.10s:Fid %d(%0.8s-%0.4s) POST performed rc: %d, (Method Rc: %d)\n",
                  pStruct->thread, fid, pStruct->func, pStruct->opt, rc, pStruct->retcd);
        #endif
        sprintf(passarea2, "%0.8d", rc);
        if (0 != rc) {
          printf("%0.10s:Fid %d(%0.8s-%0.4s) SEVERE error from POST rc: %d\n", pStruct->thread, fid, pStruct->func, pStruct->opt, rc);
        }
        break;

      /* runmain gets communications area and returns anchor address of CTT */  
      case 4:
        #ifdef GVBDEBUG
          printf("%0.10s:Fid %d(%0.8s-%0.4s) RUNMAIN returns the common anchor rc: %d\n", pStruct->thread, fid, pStruct->func, pStruct->opt, rc);
        #endif

        sprintf(passarea2, "%0.8d", rc);
        if (rc == 0) {
          memcpy(passarea2+8, "00000000", 8);
          sprintf(passarea2+16,"%0.2x%0.2x%0.2x%0.2x%0.2x%0.2x%0.2x%0.2x", 
                  pStruct->anchor[0], pStruct->anchor[1], pStruct->anchor[2], pStruct->anchor[3], 
                  pStruct->anchor[4], pStruct->anchor[5], pStruct->anchor[6], pStruct->anchor[7]);
          memcpy(TheAnchor, pStruct->anchor, 8);

          #ifdef GVBDEBUG
            printf("%0.10s:Fid %d(%0.8s-%0.4s) Common anchor: %0.2x%0.2x%0.2x%0.2x%0.2x%0.2x%0.2x%0.2x\n", 
            pStruct->thread, fid, pStruct->func, pStruct->opt,
            pStruct->anchor[0], pStruct->anchor[1], pStruct->anchor[2], pStruct->anchor[3], 
            pStruct->anchor[4], pStruct->anchor[5], pStruct->anchor[6], pStruct->anchor[7]);
          #endif
        }
        else {
          memcpy(passarea2+8, "BADBAD  ", 8); /* -- Error-- */
          printf("%0.10s:Fid %d(%0.8s-%0.4s) SEVERE ERROR from RUNMAIN rc: %d\n", pStruct->thread, fid, pStruct->func, pStruct->opt, rc);
        }
        break;

      /* unknown */
      default:
        printf("%0.10s:Fid %d(%0.8s-%0.4s) SEVERE ERROR unknow function id, rc: %d\n", pStruct->thread, fid, pStruct->func, pStruct->opt, rc);
        sprintf(passarea2, "%0.8d%8s", rc, pStruct->func);
        break;
    }

    (*env)->ReleaseByteArrayElements(env, arrayin, bufferPtr, JNI_ABORT);

   /* ---- Deal with various WAIT outcomes and return byte array ----- */
   /* --- pStruct->length2 now contains length of actual sent data --- */
   /* --- pStruct->addr2 now contains address of actual sent data  --- */
   if ( 2 == fid ) {                          /* --- wait function --- */

     /* ------ GVBMR95 logic path to invoke Java method ------ */
     if (0 == strncmp(pStruct2->reasonCD, "MR95", 4)) {
       #ifdef GVBDEBUG
         printf("%0.10s:Fid %d(%0.8s-%0.4s) WAIT returns rc: %d data length: %d l'passarea2: %d l'genparm_digits: %d caller: %0.4s\n",
         pStruct->thread,fid,pStruct->func,pStruct->opt,rc,pStruct->length2,sizeof(Pass2Struct),sizeof(genparm_digits),pStruct2->reasonCD);
       #endif

       ret = (*env)->NewByteArray (env, sizeof(Pass2Struct) + sizeof(genparm_digits) + pStruct->length2);

       /* --- assign gp fields ---------------------------- */
       pEnva = (Genenv*) pStruct->genparms.gpenva;
       memcpy(pStruct2->gpphase, pEnva->phase, 2);
       memset(pStruct2->gpdatetime, 64, 16);
       memcpy(pStruct2->gpdatetime, pEnva->prdatetime, 14);
       memcpy(pStruct2->gpstartupdata, pStruct->genparms.gpstarta, 32);

       __etoa((char*) passarea2);

       memcpy(&genparm_digits[2], pEnva->thrdno, 2);
       memcpy(&genparm_digits[4], pEnva->view, 4);

       /* This is a request from MR95 to invoke a user exit */
       if ( pStruct->length2 > 0 ) {
         (*env)->SetByteArrayRegion (env, ret, 0                                           , sizeof(Pass2Struct), (void *)passarea2);
         (*env)->SetByteArrayRegion (env, ret, sizeof(Pass2Struct)                         , sizeof(genparm_digits), (void *)genparm_digits);
         (*env)->SetByteArrayRegion (env, ret, sizeof(Pass2Struct) + sizeof(genparm_digits), pStruct->length2, (void *)pStruct->genparms.gpkeya);
       }
       else {
         (*env)->SetByteArrayRegion (env, ret, 0, sizeof(Pass2Struct), (void *)passarea2); 
       }
     }

     /* ------ GVBUR70 logic path to invoke Java method ------ */
     else {
       if (0 == strncmp(pStruct2->reasonCD, "UR70", 4)) {
         #ifdef GVBDEBUG 
           printf("%0.10s:Fid %d(%0.8s-%0.4s) WAIT returns rc: %d, data length: %d, l'passarea2: %d, caller: %0.4s\n",
                   pStruct->thread, fid, pStruct->func, pStruct->opt, rc, pStruct->length2, sizeof(Pass2Struct), pStruct2->reasonCD);
         #endif

         ret = (*env)->NewByteArray (env, sizeof(Pass2Struct) + pStruct->length2);
         __etoa((char*) passarea2);

         if ( pStruct->length2 > 0 ) {
           (*env)->SetByteArrayRegion (env, ret, 0, sizeof(Pass2Struct), (void *)passarea2);
           (*env)->SetByteArrayRegion (env, ret, sizeof(Pass2Struct), pStruct->length2, (void *)pStruct->addr2);
         }
         else {
           (*env)->SetByteArrayRegion (env, ret, 0, sizeof(Pass2Struct), (void *)passarea2);
         }
       }
       /* ------ All other waits, for example thread termination or posted to GO indicating 'n' threads ------ */
       else {
         ret = (*env)->NewByteArray (env, sizeof(Pass2Struct) + pStruct->length2);
         __etoa((char*) passarea2);
         (*env)->SetByteArrayRegion (env, ret, 0, sizeof(Pass2Struct), (void *)passarea2);
       }
     }
   }

   /* ------------- Deal with all functions other than wait -------------- */
   /* --- pStruct->length2 still contains length of Pass2Struct header --- */
   /* --- pStruct->addr2 still contains address of Pass2Struct header  --- */
   else {
     ret = (*env)->NewByteArray (env, sizeof(Pass2Struct));
     __etoa((char*) passarea2);
     (*env)->SetByteArrayRegion (env, ret, 0, sizeof(Pass2Struct), (void *)passarea2);
   }

    free(passarea2);
    free(passarea);

    return ret;

}
