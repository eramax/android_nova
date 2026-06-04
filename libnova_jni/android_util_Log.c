#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include "core_jni_helpers.h"

#define LOGGER_ENTRY_MAX_PAYLOAD 4076

static const char* priority_str(jint priority) {
    switch (priority) {
        case 2: return "V";
        case 3: return "D";
        case 4: return "I";
        case 5: return "W";
        case 6: return "E";
        case 7: return "A";
        default: return "?";
    }
}

static jboolean android_util_Log_isLoggable(JNIEnv* env, jobject clazz,
        jstring tag, jint level) {
    (void)clazz; (void)level;
    if (tag == NULL) return JNI_FALSE;
    const char* chars = (*env)->GetStringUTFChars(env, tag, NULL);
    if (!chars) return JNI_FALSE;
    (*env)->ReleaseStringUTFChars(env, tag, chars);
    return JNI_TRUE;
}

static jint android_util_Log_println_native(JNIEnv* env, jobject clazz,
        jint bufID, jint priority, jstring tagObj, jstring msgObj) {
    (void)clazz; (void)bufID;
    const char* tag = NULL;
    const char* msg = NULL;

    if (msgObj == NULL) return -1;

    if (tagObj != NULL)
        tag = (*env)->GetStringUTFChars(env, tagObj, NULL);
    msg = (*env)->GetStringUTFChars(env, msgObj, NULL);

    /* Write to stderr in logcat-compatible format */
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    fprintf(stderr, "%02d-%02d %02d:%02d:%02d.%03ld %5s/%s: %s\n",
            0, 0, 0, 0, 0, (long)(ts.tv_nsec / 1000000),
            priority_str(priority),
            tag ? tag : "NULL",
            msg ? msg : "NULL");

    if (tag != NULL)
        (*env)->ReleaseStringUTFChars(env, tagObj, tag);
    (*env)->ReleaseStringUTFChars(env, msgObj, msg);

    return (jint)(msg ? strlen(msg) : 0);
}

static jint android_util_Log_logger_entry_max_payload_native(JNIEnv* env,
        jobject clazz) {
    (void)env; (void)clazz;
    return LOGGER_ENTRY_MAX_PAYLOAD;
}

static const JNINativeMethod gMethods[] = {
    { "isLoggable",      "(Ljava/lang/String;I)Z",
      (void*)android_util_Log_isLoggable },
    { "println_native",  "(IILjava/lang/String;Ljava/lang/String;)I",
      (void*)android_util_Log_println_native },
    { "logger_entry_max_payload_native", "()I",
      (void*)android_util_Log_logger_entry_max_payload_native },
};

int register_android_util_Log(JNIEnv* env) {
    return RegisterMethodsOrDie(env, "android/util/Log",
                                gMethods, NELEM(gMethods));
}
