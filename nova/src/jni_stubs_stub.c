/* Minimal stub for register_all_jni_stubs.
 * Phase 1 replaces this with the full JNI registration table from libnova_android. */
#include <jni.h>

int register_all_jni_stubs(JNIEnv *env) {
    (void)env;
    return 0;
}
