#include "core_jni_helpers.h"
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

/* android.os.SystemProperties — @FastNative/@CriticalNative methods.
 * In-memory property store seeded with defaults that framework classes
 * (Build, DisplayManager, etc.) read during <clinit>.
 * Matches frameworks/base/core/java/android/os/SystemProperties.java. */

#define MAX_PROPS 256
#define MAX_PROP_KEY 128
#define MAX_PROP_VAL 256

static struct {
    char key[MAX_PROP_KEY];
    char val[MAX_PROP_VAL];
} prop_table[MAX_PROPS];
static int prop_count = 0;

__attribute__((constructor))
static void init_props() {
    /* Seed with defaults that android.os.Build.<clinit> reads.
     * These match a typical x86_64 emulator/GSI build. */
    prop_count = 0;

#define SET(k, v) do { \
    strncpy(prop_table[prop_count].key, k, MAX_PROP_KEY - 1); \
    strncpy(prop_table[prop_count].val, v, MAX_PROP_VAL - 1); \
    prop_count++; \
} while(0)

    SET("ro.build.version.sdk", "36");
    SET("ro.build.version.sdk_full", "36.0");
    SET("ro.build.version.codename", "REL");
    SET("ro.build.version.release", "16");
    SET("ro.build.version.release_or_codename", "16");
    SET("ro.build.version.release_or_preview_display", "16");
    SET("ro.build.version.security_patch", "2026-06-05");
    SET("ro.build.version.base_os", "");
    SET("ro.build.version.preview_sdk", "0");
    SET("ro.build.version.preview_sdk_fingerprint", "REL");
    SET("ro.build.version.all_codenames", "REL");
    SET("ro.build.version.min_supported_target_sdk", "23");
    SET("ro.build.date.utc", "1700000000");
    SET("ro.build.fingerprint", "AOSP/aosp_x86_64/aosp_x86_64:16/BP4A.251205.006/12345678:userdebug/test-keys");
    SET("ro.build.type", "userdebug");
    SET("ro.build.tags", "test-keys");
    SET("ro.build.id", "BP4A.251205.006");
    SET("ro.build.display.id", "BP4A.251205.006");
    SET("ro.build.host", "nova-host");
    SET("ro.build.user", "nova-user");
    SET("ro.build.product", "aosp_x86_64");
    SET("ro.build.description", "aosp_x86_64-userdebug 16 BP4A.251205.006 12345678 dev-keys");
    SET("ro.product.name", "aosp_x86_64");
    SET("ro.product.device", "aosp_x86_64");
    SET("ro.product.board", "goldfish_x86_64");
    SET("ro.product.manufacturer", "Google");
    SET("ro.product.model", "AOSP on x86_64");
    SET("ro.product.brand", "AOSP");
    SET("ro.product.cpu.abi", "x86_64");
    SET("ro.product.cpu.abilist", "x86_64");
    SET("ro.product.cpu.abilist64", "x86_64");
    SET("ro.product.cpu.abilist32", "");
    SET("ro.product.first_api_level", "36");
    SET("ro.board.platform", "");
    SET("ro.bootloader", "unknown");
    SET("ro.hardware", "goldfish_x86_64");
    SET("ro.zygote", "zygote64_32");
    SET("ro.serialno", "NOVA12345678");
    SET("ro.boot.serialno", "NOVA12345678");
    SET("ro.boot.vbmeta.device_state", "unlocked");
    SET("ro.boot.verifiedbootstate", "orange");
    SET("ro.boot.flash.locked", "0");
    SET("ro.boot.slot_suffix", "_a");
    SET("ro.boot.wificountrycode", "US");
    SET("dalvik.vm.heapsize", "512m");
    SET("dalvik.vm.heapgrowthlimit", "192m");
    SET("dalvik.vm.heaptargetutilization", "0.75");
    SET("dalvik.vm.heapminfree", "2m");
    SET("dalvik.vm.heapmaxfree", "8m");
    SET("persist.sys.timezone", "America/New_York");
    SET("persist.sys.locale", "en-US");
    SET("persist.sys.language", "en");
    SET("persist.sys.country", "US");
    SET("debug.atrace.tags.enableflags", "0");
    SET("ro.com.android.dateformat", "MM-dd-yyyy");
    SET("ro.com.google.clientidbase", "android-aosp");
    SET("ro.build.version.media_system_version", "36");
    SET("ro.build.version.securize_version", "0");
    SET("ro.opengles.version", "196608");
    SET("ro.kernel.qemu", "1");
    SET("ro.product.system.manufacturer", "Google");
    SET("ro.product.system.brand", "AOSP");
    SET("ro.product.system.name", "aosp_x86_64");
    SET("ro.product.system.device", "aosp_x86_64");

#undef SET
}

static const char* find_prop(const char* key) {
    if (!key) return NULL;
    for (int i = 0; i < prop_count; i++) {
        if (strcmp(prop_table[i].key, key) == 0)
            return prop_table[i].val;
    }
    return NULL;
}

static jstring native_get(JNIEnv* env, jclass, jstring key, jstring def) {
    if (key == NULL) return def;
    const char* k = (*env)->GetStringUTFChars(env, key, NULL);
    if (!k) return def;
    const char* v = find_prop(k);
    (*env)->ReleaseStringUTFChars(env, key, k);
    if (v) return (*env)->NewStringUTF(env, v);
    return def;
}

static jint native_get_int(JNIEnv* env, jclass, jstring key, jint def) {
    if (key == NULL) return def;
    const char* k = (*env)->GetStringUTFChars(env, key, NULL);
    if (!k) return def;
    const char* v = find_prop(k);
    (*env)->ReleaseStringUTFChars(env, key, k);
    if (v) return (jint)atol(v);
    return def;
}

static jlong native_get_long(JNIEnv* env, jclass, jstring key, jlong def) {
    if (key == NULL) return def;
    const char* k = (*env)->GetStringUTFChars(env, key, NULL);
    if (!k) return def;
    const char* v = find_prop(k);
    (*env)->ReleaseStringUTFChars(env, key, k);
    if (v) return atoll(v);
    return def;
}

static jboolean native_get_boolean(JNIEnv* env, jclass, jstring key, jboolean def) {
    if (key == NULL) return def;
    const char* k = (*env)->GetStringUTFChars(env, key, NULL);
    if (!k) return def;
    const char* v = find_prop(k);
    (*env)->ReleaseStringUTFChars(env, key, k);
    if (v) return (strcmp(v, "1") == 0 || strcmp(v, "true") == 0) ? JNI_TRUE : JNI_FALSE;
    return def;
}

static jlong native_find(JNIEnv* env, jclass, jstring name) {
    (void)env; (void)name;
    return 0;
}

static jstring native_get_from_handle(JNIEnv* env, jclass, jlong handle) {
    (void)env; (void)handle;
    return NULL;
}

static jint native_get_int_from_handle(jlong handle, jint def) {
    (void)handle;
    return def;
}

static jlong native_get_long_from_handle(jlong handle, jlong def) {
    (void)handle;
    return def;
}

static void native_set(JNIEnv* env, jclass, jstring key, jstring val) {
    if (key == NULL || val == NULL) return;
    const char* k = (*env)->GetStringUTFChars(env, key, NULL);
    const char* v = (*env)->GetStringUTFChars(env, val, NULL);
    if (k && v && prop_count < MAX_PROPS) {
        strncpy(prop_table[prop_count].key, k, MAX_PROP_KEY - 1);
        strncpy(prop_table[prop_count].val, v, MAX_PROP_VAL - 1);
        prop_count++;
    }
    if (k) (*env)->ReleaseStringUTFChars(env, key, k);
    if (v) (*env)->ReleaseStringUTFChars(env, val, v);
}

static void native_addChangeCallback(JNIEnv* env, jclass) {
    (void)env;
}

static const JNINativeMethod gMethods[] = {
    { "native_get",            "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
      (void*)native_get },
    { "native_get_int",        "(Ljava/lang/String;I)I",
      (void*)native_get_int },
    { "native_get_long",       "(Ljava/lang/String;J)J",
      (void*)native_get_long },
    { "native_get_boolean",    "(Ljava/lang/String;Z)Z",
      (void*)native_get_boolean },
    { "native_find",           "(Ljava/lang/String;)J",
      (void*)native_find },
    { "native_get",            "(J)Ljava/lang/String;",
      (void*)native_get_from_handle },
    { "native_get_int",        "(JI)I",
      (void*)native_get_int_from_handle },
    { "native_get_long",       "(JJ)J",
      (void*)native_get_long_from_handle },
    { "native_set",            "(Ljava/lang/String;Ljava/lang/String;)V",
      (void*)native_set },
    { "native_addChangeCallback", "()V",
      (void*)native_addChangeCallback },
};

int register_android_os_SystemProperties(JNIEnv *env) {
    return RegisterMethodsSoft(env, "android/os/SystemProperties",
                                gMethods, NELEM(gMethods));
}
