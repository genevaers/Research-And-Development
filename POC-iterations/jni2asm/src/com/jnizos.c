/* Java declaration
   ------------------------------------------------------ */
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
