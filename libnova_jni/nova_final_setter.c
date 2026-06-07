#include <jni.h>
#include <string.h>

/* JNI can read/write final fields that Java reflection can't.
 * Used by Launcher to access Activity.mFragments (declared final).
 * Registered via register_nova_launcher() during startup. */

static jmethodID method_attachHost;

JNIEXPORT jobject JNICALL
Java_nova_internal_Launcher_getObjectField(JNIEnv* env, jclass clazz,
        jobject obj, jstring fieldName) {
    (void)clazz;
    if (!obj || !fieldName) return NULL;

    jclass objClass = (*env)->GetObjectClass(env, obj);
    const char* name = (*env)->GetStringUTFChars(env, fieldName, NULL);

    jfieldID fid = (*env)->GetFieldID(env, objClass, name,
        "Landroid/app/FragmentController;");
    if (!fid) {
        fid = (*env)->GetFieldID(env, objClass, name, "Ljava/lang/Object;");
    }

    (*env)->ReleaseStringUTFChars(env, fieldName, name);
    return fid ? (*env)->GetObjectField(env, obj, fid) : NULL;
}

static const JNINativeMethod gMethods[] = {
    { "getObjectField",
      "(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;",
      (void*)Java_nova_internal_Launcher_getObjectField },
};

int register_nova_launcher(JNIEnv* env) {
    jclass clazz = (*env)->FindClass(env, "nova/internal/Launcher");
    if (!clazz) return -1;
    return (*env)->RegisterNatives(env, clazz, gMethods, 1);
}
