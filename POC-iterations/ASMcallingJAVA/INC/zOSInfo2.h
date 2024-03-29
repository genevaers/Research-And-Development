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
**********************************************************************/
/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class zOSInfo2 */

#ifndef _Included_zOSInfo2
#define _Included_zOSInfo2
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     zOSInfo2
 * Method:    showZos2
 * Signature: (Ljava/lang/Int;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Int;)Ljava/lang/String;
 */

typedef struct PassStruct
{
    char       func[8];
    char       opt[8];
    char       class[32];
    char       method[32];  
    long       length1;
    long       length2;
    void*      addr1;
    void*      addr2;
    long       retcd;
    char       anchor[8];
    void*      thrdmem;
} PassStruct;

typedef struct Pass1Struct
{
    char*      message;
} Pass1Struct;

typedef struct Pass2Struct
{
    char       returnCD[8];
    char       reasonCD[8];
    union {
        long       additions1;
        char       resultant[1];
    } u;
} Pass2Struct;

JNIEXPORT jstring JNICALL Java_zOSInfo2_showZos2
  (JNIEnv *, jobject, jint, jstring, jstring, jstring, jint);

#ifdef __cplusplus
}
#endif
#endif
