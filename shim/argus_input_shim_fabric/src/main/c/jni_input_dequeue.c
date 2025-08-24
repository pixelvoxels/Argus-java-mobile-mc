#include <jni.h>
#include <string.h>
#include <stdint.h>
#include "argus_engine.h"       // must declare argus_engine_dequeue_input_c(...)
#include "libargus_input.h"     // argus_input_event_t

JNIEXPORT jint JNICALL
Java_com_argus_input_ArgusNative_nativeDequeue(
        JNIEnv* env, jclass clazz, jlong handle, jobject outBuf)
{
    (void)clazz;
    argus_engine_t* eng = (argus_engine_t*)(uintptr_t)handle;
    if (!eng || !outBuf) return 0;

    void* dst = (*env)->GetDirectBufferAddress(env, outBuf);
    if (!dst) return 0;

    argus_input_event_t e;
    if (!argus_engine_dequeue_input_c(eng, &e)) return 0;

    memcpy(dst, &e, sizeof(e));   // struct is ~48–64 bytes
    return 1;
}