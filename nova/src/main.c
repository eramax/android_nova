#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "nova_internal.h"

static void usage(const char *prog) {
    fprintf(stderr, "Usage: %s [options] <apk_path>\n", prog);
    fprintf(stderr, "Options:\n");
    fprintf(stderr, "  -a, --activity CLASS   Main activity class\n");
    fprintf(stderr, "  -t, --title TITLE      Window title\n");
    fprintf(stderr, "  -W, --width W          Window width (default: 960)\n");
    fprintf(stderr, "  -H, --height H         Window height (default: 540)\n");
    fprintf(stderr, "  -h, --help             Show this help\n");
}

int main(int argc, char *argv[]) {
    const char *apk_path = NULL;
    const char *activity_class = NULL;
    const char *title = "Nova";
    int width = 960, height = 540;

    for (int i = 1; i < argc; i++) {
        if (strcmp(argv[i], "-h") == 0 || strcmp(argv[i], "--help") == 0) {
            usage(argv[0]);
            return 0;
        } else if ((strcmp(argv[i], "-a") == 0 || strcmp(argv[i], "--activity") == 0) && i + 1 < argc) {
            activity_class = argv[++i];
        } else if ((strcmp(argv[i], "-t") == 0 || strcmp(argv[i], "--title") == 0) && i + 1 < argc) {
            title = argv[++i];
        } else if ((strcmp(argv[i], "-W") == 0 || strcmp(argv[i], "--width") == 0) && i + 1 < argc) {
            width = atoi(argv[++i]);
        } else if ((strcmp(argv[i], "-H") == 0 || strcmp(argv[i], "--height") == 0) && i + 1 < argc) {
            height = atoi(argv[++i]);
        } else {
            apk_path = argv[i];
        }
    }

    if (!apk_path) {
        fprintf(stderr, "Error: no APK path specified\n");
        usage(argv[0]);
        return 1;
    }

    printf("Nova Linux Android Runtime\n");
    printf("  APK:    %s\n", apk_path);
    printf("  Window: %dx%d\n", width, height);

    struct nova_state *state = nova_state_create();
    if (!state) {
        fprintf(stderr, "[Nova] Failed to create Wayland state\n");
        return 1;
    }

    struct nova_window *win = nova_window_create(state, width, height, title);
    if (!win) {
        fprintf(stderr, "[Nova] Failed to create Wayland window\n");
        nova_state_destroy(state);
        return 1;
    }

    if (nova_art_init(state, argc, argv) != 0) {
        fprintf(stderr, "[Nova] Failed to initialize ART runtime\n");
        nova_window_destroy(win);
        nova_state_destroy(state);
        return 1;
    }

    struct nova_egl *egl = nova_egl_create(state, win);
    if (!egl) {
        fprintf(stderr, "[Nova] Failed to create EGL context\n");
        nova_art_shutdown(state);
        nova_window_destroy(win);
        nova_state_destroy(state);
        return 1;
    }

    if (nova_art_launch_apk(state, apk_path, activity_class) != 0) {
        fprintf(stderr, "[Nova] Failed to launch APK\n");
        nova_egl_destroy(egl);
        nova_art_shutdown(state);
        nova_window_destroy(win);
        nova_state_destroy(state);
        return 1;
    }

    nova_art_init_render(state, win);

    printf("[Nova] Entering event loop.\n");

    while (!win->closed) {
        if (nova_dispatch(state) < 0) break;
        nova_egl_swap_buffers(egl);
    }

    printf("[Nova] Shutting down.\n");

    nova_egl_destroy(egl);
    nova_art_shutdown(state);
    nova_window_destroy(win);
    nova_state_destroy(state);
    return 0;
}
