#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* Logging */
void log_unimplemented_jni(const char *method) {
    fprintf(stderr, "[NovaART] Unimplemented JNI stub: %s\n", method);
}

/* Registration helper — wraps JNI RegisterNatives */
int RegisterMethodsOrDie(JNIEnv *env, const char *className,
                          const JNINativeMethod *methods, int numMethods) {
    jclass clazz = (*env)->FindClass(env, className);
    if (!clazz) {
        fprintf(stderr, "[NovaART] Failed to find class: %s\n", className);
        /* FindClass may have thrown ClassNotFoundException — clear it to
         * avoid ART aborting with "No pending exception expected" later. */
        if ((*env)->ExceptionCheck(env)) {
            (*env)->ExceptionClear(env);
        }
        return -1;
    }
    int ret = (*env)->RegisterNatives(env, clazz, methods, numMethods);
    if (ret < 0) {
        fprintf(stderr, "[NovaART] Failed to register natives for: %s\n", className);
        /* RegisterNatives throws NoSuchMethodError on mismatch — clear it so
         * ART doesn't abort later with "No pending exception expected". */
        if ((*env)->ExceptionCheck(env)) {
            (*env)->ExceptionClear(env);
        }
    }
    return ret;
}

/* Soft registration: tries each method individually, logs failures,
 * but continues.  Returns count of successful registrations
 * (negative if class not found). */
int RegisterMethodsSoft(JNIEnv *env, const char *className,
                         const JNINativeMethod *methods, int numMethods) {
    jclass clazz = (*env)->FindClass(env, className);
    if (!clazz) {
        fprintf(stderr, "[NovaART] Failed to find class: %s\n", className);
        if ((*env)->ExceptionCheck(env)) {
            (*env)->ExceptionClear(env);
        }
        return -1;
    }
    int ok = 0;
    for (int i = 0; i < numMethods; i++) {
        int r = (*env)->RegisterNatives(env, clazz, &methods[i], 1);
        if (r == 0) {
            ok++;
        } else {
            fprintf(stderr, "[NovaART] Skipped native %s.%s (signature mismatch)\n",
                    className, methods[i].name);
            if ((*env)->ExceptionCheck(env)) {
                (*env)->ExceptionClear(env);
            }
        }
    }
    (*env)->DeleteLocalRef(env, clazz);
    return ok;
}

/* FindClass helper */
jclass FindClassOrDie(JNIEnv *env, const char *className) {
    jclass clazz = (*env)->FindClass(env, className);
    if (!clazz) {
        fprintf(stderr, "[NovaART] FindClassOrDie failed: %s\n", className);
    }
    return clazz;
}

/* Default return stubs */
jboolean return_default_boolean(void) {
    return JNI_FALSE;
}

jint return_default_int(void) {
    return 0;
}

jfloat return_default_float(void) {
    return 0.0f;
}

jdouble return_default_double(void) {
    return 0.0;
}

jobject return_null_object(void) {
    return NULL;
}

jlong return_zero_handle(void) {
    /* For native handles: return 0 (caller handles NPE) */
    return 0;
}
