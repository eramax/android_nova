#include "core_jni_helpers.h"
#include <time.h>

/* android.os.SystemClock — @CriticalNative methods (no JNIEnv/jclass params).
 * All methods are declared native in AOSP's SystemClock.java with @CriticalNative. */

static jlong uptimeMillis() {
    struct timespec ts;
    if (clock_gettime(CLOCK_MONOTONIC, &ts) == 0) {
        return (jlong)(ts.tv_sec * 1000LL + ts.tv_nsec / 1000000LL);
    }
    return 0;
}

static jlong uptimeNanos() {
    struct timespec ts;
    if (clock_gettime(CLOCK_MONOTONIC, &ts) == 0) {
        return (jlong)(ts.tv_sec * 1000000000LL + ts.tv_nsec);
    }
    return 0;
}

static jlong elapsedRealtime() {
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

static jlong elapsedRealtimeNanos() {
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

static jlong currentThreadTimeMillis() {
    struct timespec ts;
    if (clock_gettime(CLOCK_THREAD_CPUTIME_ID, &ts) == 0) {
        return (jlong)(ts.tv_sec * 1000LL + ts.tv_nsec / 1000000LL);
    }
    return 0;
}

static jlong currentThreadTimeMicro() {
    struct timespec ts;
    if (clock_gettime(CLOCK_THREAD_CPUTIME_ID, &ts) == 0) {
        return (jlong)(ts.tv_sec * 1000000LL + ts.tv_nsec / 1000LL);
    }
    return 0;
}

static jlong currentTimeMicro() {
    struct timespec ts;
    if (clock_gettime(CLOCK_REALTIME, &ts) == 0) {
        return (jlong)(ts.tv_sec * 1000000LL + ts.tv_nsec / 1000LL);
    }
    return 0;
}

/* Must match android_os_SystemClock.cpp from AOSP exactly — only methods
 * that are actually declared native in frameworks/base/core/java/android/os/SystemClock.java.
 * All are @CriticalNative (no JNIEnv/jclass params). */
static const JNINativeMethod gMethods[] = {
    { "uptimeMillis",            "()J", (void*)uptimeMillis },
    { "uptimeNanos",             "()J", (void*)uptimeNanos },
    { "elapsedRealtime",         "()J", (void*)elapsedRealtime },
    { "elapsedRealtimeNanos",    "()J", (void*)elapsedRealtimeNanos },
    { "currentThreadTimeMillis", "()J", (void*)currentThreadTimeMillis },
    { "currentThreadTimeMicro",  "()J", (void*)currentThreadTimeMicro },
    { "currentTimeMicro",        "()J", (void*)currentTimeMicro },
};

int register_android_os_SystemClock(JNIEnv *env) {
    return RegisterMethodsSoft(env, "android/os/SystemClock",
                                gMethods, NELEM(gMethods));
}
