#define _GNU_SOURCE
#define _POSIX_C_SOURCE 200809L

#include <errno.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <unistd.h>
#include <dlfcn.h>
#include "nova_internal.h"

typedef jint (*JNI_CreateJavaVM_t)(JavaVM **pvm, void **penv, JavaVMInitArgs *args);

#ifndef PATH_MAX
#define PATH_MAX 4096
#endif

/* Path to nova-framework DEX jar relative to host_out (out/host/linux-x86/).
 * Soong java_library with hostdex:true produces <name>-hostdex.jar in framework/. */
#define NOVA_FRAMEWORK_REL "framework/nova-framework-hostdex.jar"
/* SDK API stubs (android.jar DEXed) — fallback for any android.* class not
 * overridden by nova-framework.  nova-framework is listed first so its classes
 * take priority; stubs are only consulted for classes we have not implemented. */
#define ANDROID_STUBS_REL  "framework/android-stubs-dex.jar"

static const char *kGlesV2Candidates[] = {
    "/lib/x86_64-linux-gnu/libGLESv2.so.2",
    "/lib/x86_64-linux-gnu/libGLESv2.so",
    "/usr/lib/x86_64-linux-gnu/libGLESv2.so.2",
    "/usr/lib/x86_64-linux-gnu/libGLESv2.so",
    "/lib64/libGLESv2.so.2",
    "/lib64/libGLESv2.so",
    "/usr/lib64/libGLESv2.so.2",
    "/usr/lib64/libGLESv2.so",
};

static int mkdir_p(const char *path) {
    char buf[PATH_MAX];
    size_t len;

    if (!path) return -1;
    len = strlen(path);
    if (len == 0 || len >= sizeof(buf)) return -1;

    memcpy(buf, path, len + 1);
    for (char *p = buf + 1; *p != '\0'; ++p) {
        if (*p == '/') {
            *p = '\0';
            if (mkdir(buf, 0777) != 0 && errno != EEXIST) return -1;
            *p = '/';
        }
    }
    if (mkdir(buf, 0777) != 0 && errno != EEXIST) return -1;
    return 0;
}

static int file_exists(const char *path) {
    return path != NULL && access(path, F_OK) == 0;
}

static int copy_file(const char *src, const char *dst) {
    int in_fd = -1, out_fd = -1;
    char buffer[16384];
    ssize_t read_count;

    in_fd = open(src, O_RDONLY);
    if (in_fd < 0) return -1;

    out_fd = open(dst, O_WRONLY | O_CREAT | O_TRUNC, 0666);
    if (out_fd < 0) { close(in_fd); return -1; }

    while ((read_count = read(in_fd, buffer, sizeof(buffer))) > 0) {
        char *cursor = buffer;
        ssize_t remaining = read_count;
        while (remaining > 0) {
            ssize_t written = write(out_fd, cursor, (size_t)remaining);
            if (written < 0) { close(in_fd); close(out_fd); return -1; }
            cursor += written;
            remaining -= written;
        }
    }

    close(in_fd);
    if (close(out_fd) != 0) return -1;
    return read_count < 0 ? -1 : 0;
}

static int jni_log_and_clear_exception(JNIEnv *env, const char *context) {
    if (!(*env)->ExceptionCheck(env)) return 0;
    fprintf(stderr, "[Nova] Java exception during %s\n", context);
    (*env)->ExceptionDescribe(env);
    (*env)->ExceptionClear(env);
    return -1;
}

static void dirname_inplace(char *path) {
    char *slash = strrchr(path, '/');
    if (slash == NULL) { strcpy(path, "."); return; }
    if (slash == path) { slash[1] = '\0'; return; }
    *slash = '\0';
}

/* Returns directory containing the nova binary (i.e. out/host/linux-x86/bin) */
static void build_bin_dir(char *out, size_t out_size) {
    ssize_t len;
    if (out_size == 0) return;
    len = readlink("/proc/self/exe", out, out_size - 1);
    if (len <= 0 || (size_t)len >= out_size - 1) {
        strncpy(out, ".", out_size - 1);
        out[out_size - 1] = '\0';
        return;
    }
    out[len] = '\0';
    dirname_inplace(out);
}

/* Returns out/host/linux-x86 (parent of bin/) */
static void build_host_out_dir(char *out, size_t out_size) {
    build_bin_dir(out, out_size);
    dirname_inplace(out);
}

static int detect_apk_field(const char *aapt2_path, const char *apk_path,
                             const char *line_prefix, const char *field_name,
                             char *out, size_t out_size) {
    char command[PATH_MAX * 2];
    char line[1024];
    FILE *fp;

    snprintf(command, sizeof(command), "\"%s\" dump badging \"%s\" 2>/dev/null",
             aapt2_path, apk_path);
    fp = popen(command, "r");
    if (fp == NULL) {
        fprintf(stderr, "[Nova] Failed to run aapt2 for %s\n", apk_path);
        return -1;
    }

    size_t prefix_len = strlen(line_prefix);
    char needle[64];
    snprintf(needle, sizeof(needle), "%s='", field_name);
    size_t needle_len = strlen(needle);

    while (fgets(line, sizeof(line), fp) != NULL) {
        if (strncmp(line, line_prefix, prefix_len) != 0) continue;
        char *start = strstr(line, needle);
        if (!start) continue;
        start += needle_len;
        char *end = strchr(start, '\'');
        if (!end) continue;
        if ((size_t)(end - start) >= out_size) {
            pclose(fp);
            fprintf(stderr, "[Nova] Field '%s' value too long\n", field_name);
            return -1;
        }
        memcpy(out, start, (size_t)(end - start));
        out[end - start] = '\0';
        pclose(fp);
        return 0;
    }

    pclose(fp);
    fprintf(stderr, "[Nova] Field '%s' not found in %s\n", field_name, apk_path);
    return -1;
}

static int detect_launchable_activity_via_xmltree(const char *apk_path, char *out, size_t out_size) {
    char command[PATH_MAX * 2];
    char line[1024];
    FILE *fp;
    char pending_name[PATH_MAX] = {0};
    char pending_target[PATH_MAX] = {0};
    int in_activity = 0;
    int in_alias = 0;
    int has_launcher = 0;

    snprintf(command, sizeof(command),
             "/usr/bin/aapt dump xmltree \"%s\" AndroidManifest.xml 2>/dev/null",
             apk_path);
    fp = popen(command, "r");
    if (fp == NULL) {
        return -1;
    }

    out[0] = '\0';
    while (fgets(line, sizeof(line), fp) != NULL) {
        if (strstr(line, "E: activity-alias")) {
            if (has_launcher && !out[0]) {
                const char *candidate = in_alias ? pending_target : pending_name;
                if (candidate[0]) {
                    snprintf(out, out_size, "%s", candidate);
                }
            }
            in_activity = 0;
            in_alias = 1;
            has_launcher = 0;
            pending_name[0] = '\0';
            pending_target[0] = '\0';
            continue;
        }

        if (strstr(line, "E: activity") && !strstr(line, "E: activity-alias")) {
            if (has_launcher && !out[0]) {
                const char *candidate = in_alias ? pending_target : pending_name;
                if (candidate[0]) {
                    snprintf(out, out_size, "%s", candidate);
                }
            }
            in_activity = 1;
            in_alias = 0;
            has_launcher = 0;
            pending_name[0] = '\0';
            pending_target[0] = '\0';
            continue;
        }

        if (in_activity || in_alias) {
            char *name = strstr(line, "android:name");
            char *target = strstr(line, "android:targetActivity");
            if (name) {
                char *q1 = strchr(name, '"');
                char *q2 = q1 ? strchr(q1 + 1, '"') : NULL;
                if (q1 && q2 && (size_t)(q2 - q1 - 1) < sizeof(pending_name)) {
                    memcpy(pending_name, q1 + 1, (size_t)(q2 - q1 - 1));
                    pending_name[q2 - q1 - 1] = '\0';
                }
            }
            if (target) {
                char *q1 = strchr(target, '"');
                char *q2 = q1 ? strchr(q1 + 1, '"') : NULL;
                if (q1 && q2 && (size_t)(q2 - q1 - 1) < sizeof(pending_target)) {
                    memcpy(pending_target, q1 + 1, (size_t)(q2 - q1 - 1));
                    pending_target[q2 - q1 - 1] = '\0';
                }
            }
            if (strstr(line, "android.intent.category.LAUNCHER")) {
                has_launcher = 1;
            }
        }
    }

    if (has_launcher && !out[0]) {
        const char *candidate = in_alias ? pending_target : pending_name;
        if (candidate[0]) {
            snprintf(out, out_size, "%s", candidate);
        }
    }

    pclose(fp);
    return out[0] ? 0 : -1;
}

static const char *find_system_gles_library(void) {
    size_t i;
    for (i = 0; i < sizeof(kGlesV2Candidates) / sizeof(kGlesV2Candidates[0]); ++i) {
        if (file_exists(kGlesV2Candidates[i])) return kGlesV2Candidates[i];
    }
    return NULL;
}

static int ensure_symlink_or_copy(const char *src, const char *dst, const char *label) {
    if (file_exists(dst)) return 0;
    if (!src || !file_exists(src)) return 0;

    unlink(dst);
    if (symlink(src, dst) == 0) {
        printf("[Nova] Linked %s -> %s\n", dst, src);
        return 0;
    }
    if (copy_file(src, dst) == 0) {
        printf("[Nova] Copied %s from %s\n", dst, src);
        return 0;
    }
    fprintf(stderr, "[Nova] Failed to stage %s from %s: %s\n", label, src, strerror(errno));
    return -1;
}

static int append_option(JavaVMOption *options, int *count, const char *text) {
    char *copy = strdup(text);
    if (copy == NULL) return -1;
    options[*count].optionString = copy;
    options[*count].extraInfo = NULL;
    (*count)++;
    return 0;
}

static void set_env_default(const char *key, const char *value) {
    const char *existing = getenv(key);
    if (existing == NULL || existing[0] == '\0') setenv(key, value, 1);
}

static int prepend_env_path(const char *key, const char *value) {
    const char *existing;
    char joined[PATH_MAX * 2];

    if (value == NULL || value[0] == '\0') return 0;
    existing = getenv(key);
    if (existing == NULL || existing[0] == '\0') return setenv(key, value, 1);
    if (strstr(existing, value) != NULL) return 0;
    if (snprintf(joined, sizeof(joined), "%s:%s", value, existing) >= (int)sizeof(joined)) {
        errno = ENAMETOOLONG;
        return -1;
    }
    return setenv(key, joined, 1);
}

int nova_art_init(struct nova_state *state, int argc, char *argv[]) {
    (void)argc; (void)argv;
    char host_out[PATH_MAX];
    char android_root[PATH_MAX];
    char android_art_root[PATH_MAX];
    char android_i18n_root[PATH_MAX];
    char android_tzdata_root[PATH_MAX];
    char android_data[PATH_MAX];
    char native_lib_root[PATH_MAX];
    char framework_jar[PATH_MAX];
    char image_path[PATH_MAX];
    char bootclasspath[PATH_MAX * 4];
    char bootclasspath_locations[2048];
    char libart_path[PATH_MAX];
    JavaVMOption options[16];
    int option_count = 0;
    int i;

    state->libart_handle = NULL;
    state->jvm = NULL;
    state->env = NULL;

    /* Resolve paths relative to nova binary location:
     * nova binary:     out/host/linux-x86/bin/nova
     * host_out:        out/host/linux-x86
     * android_root:    out/host/linux-x86 (APEX layout: apex/com.android.art, etc.) */
    build_host_out_dir(host_out, sizeof(host_out));

    snprintf(android_root, sizeof(android_root), "%s", host_out);
    snprintf(android_art_root, sizeof(android_art_root), "%s/apex/com.android.art", host_out);
    /* i18n and tzdata are NOT under apex/ in the ART host build output */
    snprintf(android_i18n_root, sizeof(android_i18n_root), "%s/com.android.i18n", host_out);
    snprintf(android_tzdata_root, sizeof(android_tzdata_root), "%s/com.android.tzdata", host_out);
    snprintf(android_data, sizeof(android_data), "%s/nova-data", host_out);
    snprintf(native_lib_root, sizeof(native_lib_root), "%s/nova-data/native-libs", host_out);
    snprintf(framework_jar, sizeof(framework_jar), "%s/" NOVA_FRAMEWORK_REL, host_out);
    snprintf(libart_path, sizeof(libart_path), "%s/lib64/libart.so", host_out);

    set_env_default("ANDROID_ROOT", android_root);
    set_env_default("ANDROID_ART_ROOT", android_art_root);
    set_env_default("ANDROID_I18N_ROOT", android_i18n_root);
    set_env_default("ANDROID_TZDATA_ROOT", android_tzdata_root);
    set_env_default("ANDROID_DATA", android_data);

    if (mkdir_p(android_data) != 0) {
        fprintf(stderr, "[Nova] Failed to create ANDROID_DATA at %s\n", android_data);
        return -1;
    }
    if (mkdir_p(native_lib_root) != 0) {
        fprintf(stderr, "[Nova] Failed to create native lib dir at %s\n", native_lib_root);
        return -1;
    }

    /* Prepend native lib dir and ART lib dir to LD_LIBRARY_PATH */
    char art_lib64[PATH_MAX];
    snprintf(art_lib64, sizeof(art_lib64), "%s/lib64", host_out);
    if (prepend_env_path("LD_LIBRARY_PATH", native_lib_root) != 0 ||
        prepend_env_path("LD_LIBRARY_PATH", art_lib64) != 0) {
        fprintf(stderr, "[Nova] Failed to set LD_LIBRARY_PATH\n");
        return -1;
    }

    /* Stage host GLES as libGLESv3.so in native-libs */
    char gles3_dst[PATH_MAX];
    snprintf(gles3_dst, sizeof(gles3_dst), "%s/libGLESv3.so", native_lib_root);
    const char *gles_src = find_system_gles_library();
    ensure_symlink_or_copy(gles_src, gles3_dst, "libGLESv3.so");

    /* Load libart.so */
    state->libart_handle = dlopen(libart_path, RTLD_NOW | RTLD_GLOBAL);
    if (!state->libart_handle) {
        /* Fall back to searching LD_LIBRARY_PATH */
        state->libart_handle = dlopen("libart.so", RTLD_NOW | RTLD_GLOBAL);
    }
    if (!state->libart_handle) {
        fprintf(stderr, "[Nova] libart.so not found: %s\n", dlerror());
        fprintf(stderr, "[Nova] Tried: %s\n", libart_path);
        return -1;
    }

    JNI_CreateJavaVM_t JNI_CreateJavaVM = dlsym(state->libart_handle, "JNI_CreateJavaVM");
    if (!JNI_CreateJavaVM) {
        fprintf(stderr, "[Nova] JNI_CreateJavaVM not found in libart.so: %s\n", dlerror());
        dlclose(state->libart_handle);
        state->libart_handle = NULL;
        return -1;
    }

    /* Build bootclasspath from APEX jars */
    snprintf(bootclasspath, sizeof(bootclasspath),
             "%s/apex/com.android.art/javalib/core-oj.jar:"
             "%s/apex/com.android.art/javalib/core-libart.jar:"
             "%s/apex/com.android.art/javalib/okhttp.jar:"
             "%s/apex/com.android.art/javalib/bouncycastle.jar:"
             "%s/apex/com.android.art/javalib/apache-xml.jar:"
             "%s/apex/com.android.i18n/javalib/core-icu4j.jar:"
             "%s/apex/com.android.conscrypt/javalib/conscrypt.jar",
             host_out, host_out, host_out, host_out, host_out, host_out, host_out);
    snprintf(bootclasspath_locations, sizeof(bootclasspath_locations),
             "%s/apex/com.android.art/javalib/core-oj.jar:"
             "%s/apex/com.android.art/javalib/core-libart.jar:"
             "%s/apex/com.android.art/javalib/okhttp.jar:"
             "%s/apex/com.android.art/javalib/bouncycastle.jar:"
             "%s/apex/com.android.art/javalib/apache-xml.jar:"
             "%s/apex/com.android.i18n/javalib/core-icu4j.jar:"
             "%s/apex/com.android.conscrypt/javalib/conscrypt.jar",
             host_out, host_out, host_out, host_out, host_out, host_out, host_out);

    snprintf(image_path, sizeof(image_path), "%s/apex/com.android.art/framework/boot.art",
             host_out);
    if (!file_exists(image_path)) {
        /* No pre-built image; ART will JIT-compile */
        snprintf(image_path, sizeof(image_path), "/non/existent/nova.art");
    }

    char arg[PATH_MAX * 2];

    if (append_option(options, &option_count, "-Xcompiler-option --compiler-filter=verify") != 0 ||
        append_option(options, &option_count, "-Xmx256m") != 0 ||
        append_option(options, &option_count, "-verbose:class") != 0) {
        fprintf(stderr, "[Nova] Failed to allocate JVM option\n");
        return -1;
    }

    snprintf(arg, sizeof(arg), "-Ximage:%s", image_path);
    if (append_option(options, &option_count, arg) != 0) goto opt_fail;

    snprintf(arg, sizeof(arg), "-Xbootclasspath:%s", bootclasspath);
    if (append_option(options, &option_count, arg) != 0) goto opt_fail;

    snprintf(arg, sizeof(arg), "-Xbootclasspath-locations:%s", bootclasspath_locations);
    if (append_option(options, &option_count, arg) != 0) goto opt_fail;

    {
        char stubs_jar[PATH_MAX];
        snprintf(stubs_jar, sizeof(stubs_jar), "%s/" ANDROID_STUBS_REL, host_out);

        if (file_exists(framework_jar) && file_exists(stubs_jar)) {
            /* nova-framework first — its classes shadow the SDK stubs */
            snprintf(arg, sizeof(arg), "-Djava.class.path=%s:%s", framework_jar, stubs_jar);
        } else if (file_exists(framework_jar)) {
            snprintf(arg, sizeof(arg), "-Djava.class.path=%s", framework_jar);
        } else if (file_exists(stubs_jar)) {
            snprintf(arg, sizeof(arg), "-Djava.class.path=%s", stubs_jar);
        } else {
            fprintf(stderr, "[Nova] WARNING: no framework jars found\n");
            arg[0] = '\0';
        }
        if (arg[0] && append_option(options, &option_count, arg) != 0) goto opt_fail;
        if (!file_exists(framework_jar))
            fprintf(stderr, "[Nova] WARNING: nova-framework jar not found at %s\n", framework_jar);
    }

    {
        JavaVMInitArgs args;
        args.version = JNI_VERSION_1_6;
        args.nOptions = option_count;
        args.options = options;
        args.ignoreUnrecognized = JNI_TRUE;

        jint ret = JNI_CreateJavaVM(&state->jvm, (void**)&state->env, &args);
        for (i = 0; i < option_count; ++i) {
            free(options[i].optionString);
            options[i].optionString = NULL;
        }
        if (ret < 0) {
            fprintf(stderr, "[Nova] JNI_CreateJavaVM failed: %d\n", ret);
            dlclose(state->libart_handle);
            state->libart_handle = NULL;
            state->jvm = NULL;
            state->env = NULL;
            return -1;
        }
    }

    printf("[Nova] ART initialized (JavaVM: %p, JNIEnv: %p)\n",
           (void*)state->jvm, (void*)state->env);
    printf("  ANDROID_ROOT=%s\n", getenv("ANDROID_ROOT"));
    printf("  ANDROID_ART_ROOT=%s\n", getenv("ANDROID_ART_ROOT"));
    printf("  ANDROID_DATA=%s\n", getenv("ANDROID_DATA"));
    printf("  LD_LIBRARY_PATH=%s\n", getenv("LD_LIBRARY_PATH"));

    if (state->env) {
        register_all_jni_stubs(state->env);
    }

    return 0;

opt_fail:
    fprintf(stderr, "[Nova] Failed to allocate JVM option\n");
    for (i = 0; i < option_count; ++i) free(options[i].optionString);
    dlclose(state->libart_handle);
    state->libart_handle = NULL;
    return -1;
}

void nova_art_shutdown(struct nova_state *state) {
    if (state->jvm) {
        (*state->jvm)->DestroyJavaVM(state->jvm);
        state->jvm = NULL;
        state->env = NULL;
    }
    if (state->libart_handle) {
        dlclose(state->libart_handle);
        state->libart_handle = NULL;
    }
}

int nova_art_launch_apk(struct nova_state *state, const char *apk_path, const char *activity_class) {
    char host_out[PATH_MAX];
    char aapt2_path[PATH_MAX];
    char detected_activity[PATH_MAX];
    char detected_package[PATH_MAX];
    const char *resolved_activity = activity_class;
    jclass launcher_class;
    jmethodID launch_method;
    jstring apk_string, activity_string, package_string;

    if (!state || !state->env || !apk_path) return -1;

    build_host_out_dir(host_out, sizeof(host_out));
    snprintf(aapt2_path, sizeof(aapt2_path), "%s/bin/aapt2", host_out);

    if (!file_exists(aapt2_path)) {
        fprintf(stderr, "[Nova] aapt2 not found at %s\n", aapt2_path);
        return -1;
    }

    if (resolved_activity == NULL || resolved_activity[0] == '\0') {
        if (detect_apk_field(aapt2_path, apk_path, "launchable-activity:", "name",
                             detected_activity, sizeof(detected_activity)) != 0) {
            if (detect_launchable_activity_via_xmltree(apk_path, detected_activity,
                                                       sizeof(detected_activity)) != 0) {
                fprintf(stderr, "[Nova] Could not detect launchable activity in %s\n", apk_path);
                return -1;
            }
        }
        resolved_activity = detected_activity;
    }

    if (detect_apk_field(aapt2_path, apk_path, "package:", "name",
                         detected_package, sizeof(detected_package)) != 0) {
        return -1;
    }

    printf("[Nova] Package: %s  Activity: %s\n", detected_package, resolved_activity);

    launcher_class = (*state->env)->FindClass(state->env, "nova/internal/Launcher");
    if (!launcher_class || jni_log_and_clear_exception(state->env, "FindClass Launcher") != 0)
        return -1;

    launch_method = (*state->env)->GetStaticMethodID(
        state->env, launcher_class, "launch",
        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
    if (!launch_method || jni_log_and_clear_exception(state->env, "GetStaticMethodID launch") != 0)
        return -1;

    apk_string = (*state->env)->NewStringUTF(state->env, apk_path);
    activity_string = (*state->env)->NewStringUTF(state->env, resolved_activity);
    package_string = (*state->env)->NewStringUTF(state->env, detected_package);

    if (!apk_string || !activity_string || !package_string) {
        (*state->env)->ExceptionClear(state->env);
        return -1;
    }

    (*state->env)->CallStaticVoidMethod(state->env, launcher_class, launch_method,
                                        apk_string, activity_string, package_string);
    if (jni_log_and_clear_exception(state->env, "Launcher.launch") != 0) return -1;

    return 0;
}

int nova_art_init_render(struct nova_state *state, struct nova_window *win) {
    if (!state || !state->env || !win) return -1;

    jclass canvas_render_class = (*state->env)->FindClass(state->env, "nova/internal/CanvasRender");
    if (!canvas_render_class) return -1;

    jmethodID set_state = (*state->env)->GetStaticMethodID(state->env, canvas_render_class,
                                                           "setRenderState", "(J)V");
    jmethodID set_window = (*state->env)->GetStaticMethodID(state->env, canvas_render_class,
                                                            "setRenderWindow", "(J)V");
    if (!set_state || !set_window) {
        (*state->env)->DeleteLocalRef(state->env, canvas_render_class);
        return -1;
    }

    (*state->env)->CallStaticVoidMethod(state->env, canvas_render_class, set_state,
                                       (jlong)(intptr_t)state);
    (*state->env)->CallStaticVoidMethod(state->env, canvas_render_class, set_window,
                                       (jlong)(intptr_t)win);
    (*state->env)->DeleteLocalRef(state->env, canvas_render_class);
    return 0;
}
