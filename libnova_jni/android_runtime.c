#include "core_jni_helpers.h"
#include <stddef.h>
#include <stdio.h>

/* Module registration functions — only register JNI for classes whose
 * native methods are actually declared in Nova's Java implementations.
 * Classes loaded from the boot classpath or SDK stubs whose native methods
 * are NOT present in Nova's framework MUST be omitted — RegisterNatives
 * fails if the Java class doesn't declare the native method. */

int register_android_os_SystemClock(JNIEnv *env);
int register_android_os_SystemProperties(JNIEnv *env);
int register_android_content_AssetManager(JNIEnv *env);
int register_android_content_res_ApkAssets(JNIEnv *env);
int register_android_os_MessageQueue(JNIEnv *env);
int register_android_util_Log(JNIEnv *env);
int register_android_os_Trace(JNIEnv *env);
int register_nova_launcher(JNIEnv *env);
int register_nova_canvas_render(JNIEnv *env);

/*
 * gRegJNI[] — central registration table mirroring AOSP's AndroidRuntime.cpp
 *
 * The order should match AOSP's gRegJNI in AndroidRuntime.cpp for clarity,
 * but registration is independent per class.
 */
static const RegJNIProc gRegJNI[] = {
    /* OS-level */
    register_android_os_SystemClock,
    register_android_os_SystemProperties,
    register_android_content_AssetManager,
    register_android_content_res_ApkAssets,
    register_android_os_MessageQueue,
    register_android_util_Log,
    register_android_os_Trace,
    // register_android_os_Binder,
    // register_android_os_Process,

    /* View/Input */
    // register_android_view_MotionEvent,

    /* Graphics */
    // register_android_graphics_Canvas,
    // register_android_graphics_Paint,
    // register_android_graphics_Bitmap,
    // register_android_graphics_BitmapFactory,
    // register_com_android_internal_graphics_NativeUtils,

    /* OpenGL ES 2.0 */
    // register_android_opengl_GLES20,
    // register_android_opengl_GLUtils,

    /* EGL/GL bootstrap */
    // register_com_google_android_gles_jni_EGLImpl,
    // register_com_google_android_gles_jni_GLImpl,

    /* Canvas render & input dispatch */
    register_nova_canvas_render,

    /* JNI bridge for final field access (registers during runtime) */
    // register_nova_launcher,
};

int register_all_jni_stubs(JNIEnv *env) {
    size_t count = sizeof(gRegJNI) / sizeof(gRegJNI[0]);
    int failures = 0;

    for (size_t i = 0; i < count; i++) {
        if (gRegJNI[i] != NULL) {
            if (gRegJNI[i](env) < 0) {
                fprintf(stderr, "[NovaART] Failed to register JNI stub #%zu\n", i);
                failures++;
            }
        }
    }

    if (failures > 0) {
        fprintf(stderr, "[NovaART] JNI registration complete: %d/%zu failed\n",
                failures, count);
    } else {
        printf("[NovaART] All JNI stubs registered (%zu)\n", count);
    }

    return failures > 0 ? -1 : 0;
}
