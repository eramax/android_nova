#include <jni.h>
#include "core_jni_helpers.h"

/* Minimal android.os.Trace native methods for Phase C.
 * Real AOSP: frameworks/base/core/jni/android_os_Trace.cpp */

static jboolean NativeIsTagEnabled(JNIEnv* env, jclass clazz, jlong tag) {
    (void)env; (void)clazz; (void)tag;
    return JNI_FALSE;
}

static void NativeSetAppTracingAllowed(JNIEnv* env, jclass clazz, jboolean allowed) {
    (void)env; (void)clazz; (void)allowed;
}

static void NativeSetTracingEnabled(JNIEnv* env, jclass clazz, jboolean allowed) {
    (void)env; (void)clazz; (void)allowed;
}

static void NativeTraceCounter(JNIEnv* env, jclass clazz, jlong tag, jstring name, jlong value) {
    (void)env; (void)clazz; (void)tag; (void)name; (void)value;
}

static void NativeTraceBegin(JNIEnv* env, jclass clazz, jlong tag, jstring name) {
    (void)env; (void)clazz; (void)tag; (void)name;
}

static void NativeTraceEnd(JNIEnv* env, jclass clazz, jlong tag) {
    (void)env; (void)clazz; (void)tag;
}

static const JNINativeMethod gTraceMethods[] = {
    { "nativeIsTagEnabled", "(J)Z",
      (void*)NativeIsTagEnabled },
    { "nativeSetAppTracingAllowed", "(Z)V",
      (void*)NativeSetAppTracingAllowed },
    { "nativeSetTracingEnabled", "(Z)V",
      (void*)NativeSetTracingEnabled },
    { "nativeTraceCounter", "(JLjava/lang/String;J)V",
      (void*)NativeTraceCounter },
    { "nativeTraceBegin", "(JLjava/lang/String;)V",
      (void*)NativeTraceBegin },
    { "nativeTraceEnd", "(J)V",
      (void*)NativeTraceEnd },
};

int register_android_os_Trace(JNIEnv* env) {
    return RegisterMethodsSoft(env, "android/os/Trace",
                               gTraceMethods, NELEM(gTraceMethods));
}
