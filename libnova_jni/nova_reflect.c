#include <jni.h>
#include <stddef.h>
#include "core_jni_helpers.h"

/* Helper to set any object field, including final ones (JNI can bypass
 * the final restriction that Java reflection cannot). */

JNIEXPORT void JNICALL
Java_nova_internal_ReflectHelpers_setObjectField(JNIEnv* env, jclass clazz,
        jobject obj, jclass targetClass, jstring fieldName, jobject value) {
    (void)clazz;
    if (!obj || !fieldName) return;

    const char* name = (*env)->GetStringUTFChars(env, fieldName, NULL);
    if (!name) return;

    jfieldID fid = (*env)->GetFieldID(env, targetClass, name, "Ljava/lang/Object;");
    if (fid) {
        (*env)->SetObjectField(env, obj, fid, value);
    }

    (*env)->ReleaseStringUTFChars(env, fieldName, name);
}
