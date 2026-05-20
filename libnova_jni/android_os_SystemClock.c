#include "core_jni_helpers.h"
#include <time.h>

/* android.os.SystemClock — implemented with clock_gettime */
static jlong now(JNIEnv *env, jclass clazz) {
    struct timespec ts;
    if (clock_gettime(CLOCK_REALTIME, &ts) == 0) {
        return (jlong)(ts.tv_sec * 1000LL + ts.tv_nsec / 1000000LL);
    }
    return 0;
}

static jlong uptimeMillis(JNIEnv *env, jclass clazz) {
    struct timespec ts;
    if (clock_gettime(CLOCK_MONOTONIC, &ts) == 0) {
        return (jlong)(ts.tv_sec * 1000LL + ts.tv_nsec / 1000000LL);
    }
    return 0;
}

static jlong elapsedRealtime(JNIEnv *env, jclass clazz) {
    struct timespec ts;
#ifdef CLOCK_BOOTTIME
    if (clock_gettime(CLOCK_BOOTTIME, &ts) == 0) {
        return (jlong)(ts.tv_sec * 1000LL + ts.tv_nsec / 1000000LL);
    }
#endif
    if (clock_gettime(CLOCK_MONOTONIC, &ts) == 0) {
        return (jlong)(ts.tv_sec * 1000LL + ts.tv_nsec / 1000000LL);
    }
    return 0;
}

static jlong elapsedRealtimeNanos(JNIEnv *env, jclass clazz) {
    struct timespec ts;
#ifdef CLOCK_BOOTTIME
    if (clock_gettime(CLOCK_BOOTTIME, &ts) == 0) {
        return (jlong)(ts.tv_sec * 1000000000LL + ts.tv_nsec);
    }
#endif
    if (clock_gettime(CLOCK_MONOTONIC, &ts) == 0) {
        return (jlong)(ts.tv_sec * 1000000000LL + ts.tv_nsec);
    }
    return 0;
}

static jlong currentThreadTimeMillis(JNIEnv *env, jclass clazz) {
    struct timespec ts;
    if (clock_gettime(CLOCK_THREAD_CPUTIME_ID, &ts) == 0) {
        return (jlong)(ts.tv_sec * 1000LL + ts.tv_nsec / 1000000LL);
    }
    return 0;
}

static jlong currentThreadTimeMicro(JNIEnv *env, jclass clazz) {
    struct timespec ts;
    if (clock_gettime(CLOCK_THREAD_CPUTIME_ID, &ts) == 0) {
        return (jlong)(ts.tv_sec * 1000000LL + ts.tv_nsec / 1000LL);
    }
    return 0;
}

static jlong currentTimeMicro(JNIEnv *env, jclass clazz) {
    struct timespec ts;
    if (clock_gettime(CLOCK_REALTIME, &ts) == 0) {
        return (jlong)(ts.tv_sec * 1000000LL + ts.tv_nsec / 1000LL);
    }
    return 0;
}

static const JNINativeMethod gMethods[] = {
    { "now",                     "()J", (void*)now },
    { "uptimeMillis",            "()J", (void*)uptimeMillis },
    { "elapsedRealtime",         "()J", (void*)elapsedRealtime },
    { "elapsedRealtimeNanos",    "()J", (void*)elapsedRealtimeNanos },
    { "currentThreadTimeMillis", "()J", (void*)currentThreadTimeMillis },
    { "currentThreadTimeMicro",  "()J", (void*)currentThreadTimeMicro },
    { "currentTimeMicro",        "()J", (void*)currentTimeMicro },
};

int register_android_os_SystemClock(JNIEnv *env) {
    return RegisterMethodsOrDie(env, "android/os/SystemClock",
                                 gMethods, sizeof(gMethods) / sizeof(gMethods[0]));
}
