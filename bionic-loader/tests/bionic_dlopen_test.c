#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "bionic_loader.h"

int main(int argc, char **argv) {
    if (argc < 2) {
        fprintf(stderr, "Usage: %s <path-to-bionic.so>\n", argv[0]);
        return 2;
    }
    bionic_loader_set_lib_path(
        "/mnt/mydata/projects2/0/aosp-full/vendor/nova/aosp-prebuilt/lib64"
        ":/mnt/mydata/projects2/0/aosp-full/vendor/nova/aosp-prebuilt/lib64/bootstrap"
        ":/mnt/mydata/projects2/0/aosp-full/vendor/nova/aosp-prebuilt/apex-flat/com.android.runtime/lib64/bionic"
        ":/mnt/mydata/projects2/0/aosp-full/vendor/nova/aosp-prebuilt/apex-flat/com.android.i18n/lib64"
        ":/mnt/mydata/projects2/0/aosp-full/vendor/nova/aosp-prebuilt/apex-flat/com.android.adbd/lib64"
        ":/mnt/mydata/projects2/0/aosp-full/vendor/nova/aosp-prebuilt/apex-flat/com.android.appsearch/lib64"
        ":/mnt/mydata/projects2/0/aosp-full/vendor/nova/aosp-prebuilt/apex-flat/com.android.bt/lib64"
        ":/mnt/mydata/projects2/0/aosp-full/vendor/nova/aosp-prebuilt/apex-flat/com.android.configinfrastructure/lib64"
        ":/mnt/mydata/projects2/0/aosp-full/vendor/nova/aosp-prebuilt/apex-flat/com.android.conscrypt/lib64"
        ":/mnt/mydata/projects2/0/aosp-full/vendor/nova/aosp-prebuilt/apex-flat/com.android.media/lib64"
        ":/mnt/mydata/projects2/0/aosp-full/vendor/nova/aosp-prebuilt/apex-flat/com.android.media.swcodec/lib64"
        ":/mnt/mydata/projects2/0/aosp-full/vendor/nova/aosp-prebuilt/apex-flat/com.android.nfcservices/lib64"
        ":/mnt/mydata/projects2/0/aosp-full/vendor/nova/aosp-prebuilt/apex-flat/com.android.os.statsd/lib64"
        ":/mnt/mydata/projects2/0/aosp-full/vendor/nova/aosp-prebuilt/apex-flat/com.android.profiling/lib64"
        ":/mnt/mydata/projects2/0/aosp-full/vendor/nova/aosp-prebuilt/apex-flat/com.android.resolv/lib64"
        ":/mnt/mydata/projects2/0/aosp-full/vendor/nova/aosp-prebuilt/apex-flat/com.android.tethering/lib64"
        ":/mnt/mydata/projects2/0/aosp-full/vendor/nova/aosp-prebuilt/apex-flat/com.android.uprobestats/lib64"
        ":/mnt/mydata/projects2/0/aosp-full/vendor/nova/aosp-prebuilt/apex-flat/com.android.uwb/lib64"
        ":/mnt/mydata/projects2/0/aosp-full/vendor/nova/aosp-prebuilt/apex-flat/com.android.virt/lib64"
        ":/mnt/mydata/projects2/0/aosp-full/vendor/nova/aosp-prebuilt/apex-flat/com.android.wifi/lib64"
    );
    fprintf(stderr, "bionic_dlopen(%s)...\n", argv[1]);
    void *h = bionic_dlopen(argv[1], 0);
    const char *e = bionic_dlerror();
    if (e) fprintf(stderr, "dlerror: %s\n", e);
    if (!h) {
        fprintf(stderr, "FAIL: bionic_dlopen returned NULL\n");
        return 1;
    }
    fprintf(stderr, "OK: bionic_dlopen returned %p\n", h);
    /* Try a common symbol */
    void *sym = bionic_dlsym(h, "JNI_OnLoad");
    fprintf(stderr, "JNI_OnLoad -> %p\n", sym);
    if (argc > 2) {
        sym = bionic_dlsym(h, argv[2]);
        fprintf(stderr, "%s -> %p\n", argv[2], sym);
    }
    bionic_dlclose(h);
    return 0;
}
