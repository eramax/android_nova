#include <jni.h>

/* JNI can read/write final fields that Java reflection can't.
 * Used by Launcher to init Activity.mFragments and similar. */

JNIEXPORT jobject JNICALL
Java_nova_internal_NovaReflect_getFinalObjectField(JNIEnv* env, jclass clazz,
        jobject obj, jclass targetClass, jstring fieldName) {
    (void)clazz;
    if (!obj || !fieldName) return NULL;

    const char* name = (*env)->GetStringUTFChars(env, fieldName, NULL);
    jfieldID fid = (*env)->GetFieldID(env, targetClass, name, "Ljava/lang/Object;");
    if (!fid) {
        // Try FragmentController type
        fid = (*env)->GetFieldID(env, targetClass, name,
            "Landroid/app/FragmentController;");
    }
    if (fid) {
        jobject result = (*env)->GetObjectField(env, obj, fid);
        (*env)->ReleaseStringUTFChars(env, fieldName, name);
        return result;
    }
    (*env)->ReleaseStringUTFChars(env, fieldName, name);
    return NULL;
}

JNIEXPORT void JNICALL
Java_nova_internal_NovaReflect_setFinalObjectField(JNIEnv* env, jclass clazz,
        jobject obj, jclass targetClass, jstring fieldName, jobject value) {
    (void)clazz;
    if (!obj || !fieldName) return;

    const char* name = (*env)->GetStringUTFChars(env, fieldName, NULL);
    jfieldID fid = (*env)->GetFieldID(env, targetClass, name, "Ljava/lang/Object;");
    if (!fid) {
        fid = (*env)->GetFieldID(env, targetClass, name,
            "Landroid/app/FragmentController;");
    }
    if (fid) {
        (*env)->SetObjectField(env, obj, fid, value);
    }
    (*env)->ReleaseStringUTFChars(env, fieldName, name);
}
