#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/epoll.h>
#include <sys/eventfd.h>
#include <time.h>
#include "core_jni_helpers.h"

#define NOVA_MQ_LOG_TAG "NovaMQ"

struct nova_message_queue {
    int epoll_fd;
    int wake_fd;
};

static jlong android_os_MessageQueue_nativeInit(JNIEnv* env, jclass clazz) {
    (void)clazz;
    struct nova_message_queue* mq = calloc(1, sizeof(*mq));
    if (!mq) return 0;

    mq->epoll_fd = epoll_create1(EPOLL_CLOEXEC);
    if (mq->epoll_fd < 0) {
        fprintf(stderr, "[%s] epoll_create1 failed: %m\n", NOVA_MQ_LOG_TAG);
        free(mq);
        return 0;
    }

    mq->wake_fd = eventfd(0, EFD_NONBLOCK | EFD_CLOEXEC);
    if (mq->wake_fd < 0) {
        fprintf(stderr, "[%s] eventfd failed: %m\n", NOVA_MQ_LOG_TAG);
        close(mq->epoll_fd);
        free(mq);
        return 0;
    }

    struct epoll_event ev;
    memset(&ev, 0, sizeof(ev));
    ev.events = EPOLLIN;
    ev.data.fd = mq->wake_fd;
    if (epoll_ctl(mq->epoll_fd, EPOLL_CTL_ADD, mq->wake_fd, &ev) < 0) {
        fprintf(stderr, "[%s] epoll_ctl add wake_fd failed: %m\n", NOVA_MQ_LOG_TAG);
        close(mq->wake_fd);
        close(mq->epoll_fd);
        free(mq);
        return 0;
    }

    return (jlong)(intptr_t)mq;
}

static void android_os_MessageQueue_nativeDestroy(JNIEnv* env, jclass clazz, jlong ptr) {
    (void)env; (void)clazz;
    struct nova_message_queue* mq = (struct nova_message_queue*)(intptr_t)ptr;
    if (!mq) return;
    close(mq->wake_fd);
    close(mq->epoll_fd);
    free(mq);
}

static void android_os_MessageQueue_nativePollOnce(JNIEnv* env, jobject obj,
        jlong ptr, jint timeoutMillis) {
    (void)env; (void)obj;
    struct nova_message_queue* mq = (struct nova_message_queue*)(intptr_t)ptr;
    if (!mq) return;

    struct epoll_event events[8];
    int nfds = epoll_wait(mq->epoll_fd, events, 8, timeoutMillis);
    if (nfds < 0 && errno != EINTR) {
        fprintf(stderr, "[%s] epoll_wait failed: %m\n", NOVA_MQ_LOG_TAG);
        return;
    }

    for (int i = 0; i < nfds; i++) {
        if (events[i].data.fd == mq->wake_fd && (events[i].events & EPOLLIN)) {
            eventfd_t val;
            eventfd_read(mq->wake_fd, &val);
        }
    }
}

static void android_os_MessageQueue_nativeWake(JNIEnv* env, jclass clazz, jlong ptr) {
    (void)env; (void)clazz;
    struct nova_message_queue* mq = (struct nova_message_queue*)(intptr_t)ptr;
    if (!mq) return;
    eventfd_write(mq->wake_fd, 1);
}

static jboolean android_os_MessageQueue_nativeIsPolling(JNIEnv* env, jclass clazz, jlong ptr) {
    (void)env; (void)clazz; (void)ptr;
    return JNI_TRUE;
}

static void android_os_MessageQueue_nativeSetFileDescriptorEvents(JNIEnv* env, jclass clazz,
        jlong ptr, jint fd, jint events) {
    (void)env; (void)clazz; (void)ptr; (void)fd; (void)events;
}

static void android_os_MessageQueue_nativeSetSkipEpollWaitForZeroTimeout(JNIEnv* env,
        jclass clazz, jlong ptr) {
    (void)env; (void)clazz; (void)ptr;
}

static const JNINativeMethod gMessageQueueMethods[] = {
    {"nativeInit", "()J", (void*)android_os_MessageQueue_nativeInit},
    {"nativeDestroy", "(J)V", (void*)android_os_MessageQueue_nativeDestroy},
    {"nativePollOnce", "(JI)V", (void*)android_os_MessageQueue_nativePollOnce},
    {"nativeWake", "(J)V", (void*)android_os_MessageQueue_nativeWake},
    {"nativeIsPolling", "(J)Z", (void*)android_os_MessageQueue_nativeIsPolling},
    {"nativeSetFileDescriptorEvents", "(JII)V",
     (void*)android_os_MessageQueue_nativeSetFileDescriptorEvents},
    {"nativeSetSkipEpollWaitForZeroTimeout", "(J)V",
     (void*)android_os_MessageQueue_nativeSetSkipEpollWaitForZeroTimeout},
};

int register_android_os_MessageQueue(JNIEnv* env) {
    return RegisterMethodsOrDie(env, "android/os/MessageQueue",
                                gMessageQueueMethods, NELEM(gMessageQueueMethods));
}
