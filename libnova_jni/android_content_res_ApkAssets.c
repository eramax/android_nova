#include <jni.h>
#include <stddef.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include "core_jni_helpers.h"

/*
 * Minimal ApkAssets native methods for Phase C.
 * Matches frameworks/base/core/jni/android_content_res_ApkAssets.cpp
 */

static jlong NativeLoad(JNIEnv* env, jclass clazz,
        jint fd, jstring path, jint flags, jobject assetsProvider) {
    (void)env; (void)clazz; (void)fd; (void)path; (void)flags; (void)assetsProvider;
    void* handle = malloc(8);
    if (!handle) return 0;
    memset(handle, 0, 8);
    return (jlong)(intptr_t)handle;
}

static jlong NativeLoadEmpty(JNIEnv* env, jclass clazz,
        jint flags, jobject assetsProvider) {
    (void)env; (void)clazz; (void)flags; (void)assetsProvider;
    void* handle = malloc(8);
    if (!handle) return 0;
    memset(handle, 0, 8);
    return (jlong)(intptr_t)handle;
}

static jlong NativeLoadFromFd(JNIEnv* env, jclass clazz,
        jint fd, jstring friendlyName, jint flags, jobject assetsProvider) {
    (void)env; (void)clazz; (void)fd; (void)friendlyName;
    (void)flags; (void)assetsProvider;
    void* handle = malloc(8);
    if (!handle) return 0;
    memset(handle, 0, 8);
    return (jlong)(intptr_t)handle;
}

static jlong NativeLoadFromFdOffset(JNIEnv* env, jclass clazz,
        jint fd, jstring friendlyName, jlong offset, jlong length,
        jint flags, jobject assetsProvider) {
    (void)env; (void)clazz; (void)fd; (void)friendlyName;
    (void)offset; (void)length; (void)flags; (void)assetsProvider;
    void* handle = malloc(8);
    if (!handle) return 0;
    memset(handle, 0, 8);
    return (jlong)(intptr_t)handle;
}

static void NativeDestroy(JNIEnv* env, jclass clazz, jlong ptr) {
    (void)env; (void)clazz;
    free((void*)(intptr_t)ptr);
}

static jstring NativeGetAssetPath(JNIEnv* env, jclass clazz, jlong ptr) {
    (void)env; (void)clazz; (void)ptr;
    return NULL;
}

static jstring NativeGetDebugName(JNIEnv* env, jclass clazz, jlong ptr) {
    (void)env; (void)clazz; (void)ptr;
    return NULL;
}

static jlong NativeGetStringBlock(JNIEnv* env, jclass clazz, jlong ptr) {
    (void)env; (void)clazz; (void)ptr;
    return 0;
}

/* @CriticalNative — no JNIEnv/jclass */
static jint NativeIsUpToDate(jlong ptr) {
    (void)ptr;
    return 1; /* always up to date */
}

static jlong NativeOpenXml(JNIEnv* env, jclass clazz, jlong ptr, jstring path) {
    (void)env; (void)clazz; (void)ptr; (void)path;
    return 0;
}

static jobject NativeGetOverlayableInfo(JNIEnv* env, jclass clazz,
        jlong ptr, jstring path) {
    (void)env; (void)clazz; (void)ptr; (void)path;
    return NULL;
}

static jboolean NativeDefinesOverlayable(JNIEnv* env, jclass clazz, jlong ptr) {
    (void)env; (void)clazz; (void)ptr;
    return JNI_FALSE;
}

static const JNINativeMethod gApkAssetsMethods[] = {
    { "nativeLoad",
      "(ILjava/lang/String;ILandroid/content/res/loader/AssetsProvider;)J",
      (void*)NativeLoad },
    { "nativeLoadEmpty",
      "(ILandroid/content/res/loader/AssetsProvider;)J",
      (void*)NativeLoadEmpty },
    { "nativeLoadFd",
      "(ILjava/io/FileDescriptor;Ljava/lang/String;ILandroid/content/res/loader/AssetsProvider;)J",
      (void*)NativeLoadFromFd },
    { "nativeLoadFdOffsets",
      "(ILjava/io/FileDescriptor;Ljava/lang/String;JJILandroid/content/res/loader/AssetsProvider;)J",
      (void*)NativeLoadFromFdOffset },
    { "nativeDestroy", "(J)V",
      (void*)NativeDestroy },
    { "nativeGetAssetPath", "(J)Ljava/lang/String;",
      (void*)NativeGetAssetPath },
    { "nativeGetDebugName", "(J)Ljava/lang/String;",
      (void*)NativeGetDebugName },
    { "nativeGetStringBlock", "(J)J",
      (void*)NativeGetStringBlock },
    { "nativeIsUpToDate", "(J)I",
      (void*)NativeIsUpToDate },
    { "nativeOpenXml", "(JLjava/lang/String;)J",
      (void*)NativeOpenXml },
    { "nativeGetOverlayableInfo",
      "(JLjava/lang/String;)Landroid/content/om/OverlayableInfo;",
      (void*)NativeGetOverlayableInfo },
    { "nativeDefinesOverlayable", "(J)Z",
      (void*)NativeDefinesOverlayable },
};

int register_android_content_res_ApkAssets(JNIEnv* env) {
    return RegisterMethodsSoft(env, "android/content/res/ApkAssets",
                               gApkAssetsMethods, NELEM(gApkAssetsMethods));
}
