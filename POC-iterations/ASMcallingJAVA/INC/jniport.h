#ifndef jniport_h
#define jniport_h

#if defined(WIN32) || defined(_WIN32)

#define JNIEXPORT __declspec(dllexport)
#define JNICALL __stdcall
typedef __int64 jlong;

#else /* WIN32 || _WIN32 */

#define JNIEXPORT 

/*typedef long long jlong;*/
typedef long jlong;

#endif /* WIN32 || _WIN32 */

#ifndef JNICALL
#define JNICALL
#endif

#ifndef JNIEXPORT
#define JNIEXPORT
#endif

#ifndef JNIIMPORT
#define JNIIMPORT
#endif

#ifdef _JNI_IMPLEMENTATION_
#define _JNI_IMPORT_OR_EXPORT_ JNIEXPORT
#else
#define _JNI_IMPORT_OR_EXPORT_ JNIIMPORT
#endif

#endif     /* jniport_h */
