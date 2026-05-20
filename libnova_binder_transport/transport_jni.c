// transport_jni.c — JNI glue between NovaBinderTransport.java and libnova_ipc
//
// The "handle" passed back to Java is a pointer to a nova_ipc_client cast to jlong.

#include "nova_ipc.h"

#include <jni.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>

// ── nativeConnect ─────────────────────────────────────────────────────────────

JNIEXPORT jlong JNICALL
Java_nova_internal_NovaBinderTransport_nativeConnect(JNIEnv *env, jclass cls,
                                                      jstring socket_path_jstr) {
    (void)cls;
    const char *path = (*env)->GetStringUTFChars(env, socket_path_jstr, NULL);
    if (!path) return 0L;

    nova_ipc_client *cli = nova_ipc_client_connect(path);
    (*env)->ReleaseStringUTFChars(env, socket_path_jstr, path);

    if (!cli) {
        fprintf(stderr, "[NovaBinderTransport] Connect failed\n");
        return 0L;
    }
    return (jlong)(uintptr_t)cli;
}

// ── nativeTransact ────────────────────────────────────────────────────────────

JNIEXPORT jbyteArray JNICALL
Java_nova_internal_NovaBinderTransport_nativeTransact(JNIEnv *env, jclass cls,
                                                       jlong handle,
                                                       jint service_id, jint txn,
                                                       jbyteArray req_bytes) {
    (void)cls;
    nova_ipc_client *cli = (nova_ipc_client *)(uintptr_t)handle;
    if (!cli) return NULL;

    nova_parcel in, out;
    nova_parcel_init(&in);
    nova_parcel_init(&out);

    if (req_bytes) {
        jsize req_len = (*env)->GetArrayLength(env, req_bytes);
        if (req_len > 0) {
            jbyte *req_data = (*env)->GetByteArrayElements(env, req_bytes, NULL);
            if (req_data) {
                nova_parcel_write_bytes(&in, req_data, (uint32_t)req_len);
                (*env)->ReleaseByteArrayElements(env, req_bytes, req_data, JNI_ABORT);
            }
        }
    }

    int rc = nova_ipc_transact(cli, (uint32_t)service_id, (uint32_t)txn, &in, &out);
    nova_parcel_free(&in);

    if (rc != 0) {
        nova_parcel_free(&out);
        return NULL;
    }

    jbyteArray result = (*env)->NewByteArray(env, (jsize)out.len);
    if (result && out.len > 0)
        (*env)->SetByteArrayRegion(env, result, 0, (jsize)out.len, (const jbyte *)out.data);

    nova_parcel_free(&out);
    return result;
}

// ── nativeClose ───────────────────────────────────────────────────────────────

JNIEXPORT void JNICALL
Java_nova_internal_NovaBinderTransport_nativeClose(JNIEnv *env, jclass cls, jlong handle) {
    (void)env; (void)cls;
    nova_ipc_client *cli = (nova_ipc_client *)(uintptr_t)handle;
    nova_ipc_client_destroy(cli);
}
