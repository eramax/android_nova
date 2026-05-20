#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include "nova_internal.h"
#include "nova_daemon_client.h"

static void usage(const char *prog) {
    fprintf(stderr, "Usage: %s [options] <apk_path>\n", prog);
    fprintf(stderr, "       %s --package <package.name> [options]\n", prog);
    fprintf(stderr, "       %s --install <apk_path>\n", prog);
    fprintf(stderr, "Options:\n");
    fprintf(stderr, "  -a, --activity CLASS   Main activity class\n");
    fprintf(stderr, "  -t, --title TITLE      Window title\n");
    fprintf(stderr, "  -W, --width W          Window width (default: 960)\n");
    fprintf(stderr, "  -H, --height H         Window height (default: 540)\n");
    fprintf(stderr, "      --standalone       Run without daemon (Phase 0-3 compat)\n");
    fprintf(stderr, "      --package NAME     Resolve APK by package name\n");
    fprintf(stderr, "      --install APK      Install APK into package registry\n");
    fprintf(stderr, "      --daemon-socket P  Daemon socket path (default: auto)\n");
    fprintf(stderr, "  -h, --help             Show this help\n");
}

int main(int argc, char *argv[]) {
    const char *apk_path      = NULL;
    const char *package_name  = NULL;
    const char *activity_class = NULL;
    const char *title         = "Nova";
    const char *daemon_socket = NULL;
    const char *install_path  = NULL;
    int width = 960, height = 540;
    int standalone = 0;

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
        } else if (strcmp(argv[i], "--standalone") == 0) {
            standalone = 1;
        } else if (strcmp(argv[i], "--package") == 0 && i + 1 < argc) {
            package_name = argv[++i];
        } else if (strcmp(argv[i], "--install") == 0 && i + 1 < argc) {
            install_path = argv[++i];
        } else if (strcmp(argv[i], "--daemon-socket") == 0 && i + 1 < argc) {
            daemon_socket = argv[++i];
        } else {
            apk_path = argv[i];
        }
    }

    // ── Install mode ──────────────────────────────────────────────────────────
    if (install_path) {
        return nova_install_apk(install_path);
    }

    // ── Resolve APK path from package name ───────────────────────────────────
    static char resolved_apk[4096];
    if (package_name && !apk_path) {
        if (nova_resolve_package(package_name, resolved_apk, sizeof(resolved_apk)) != 0) {
            fprintf(stderr, "[Nova] Package not found: %s\n", package_name);
            fprintf(stderr, "  Install it with: nova --install /path/to/app.apk\n");
            return 1;
        }
        apk_path = resolved_apk;
        if (!title || strcmp(title, "Nova") == 0)
            title = package_name;
    }

    if (!apk_path) {
        fprintf(stderr, "Error: no APK path or --package specified\n");
        usage(argv[0]);
        return 1;
    }

    printf("Nova Linux Android Runtime\n");
    printf("  APK:    %s\n", apk_path);
    printf("  Window: %dx%d\n", width, height);
    if (package_name) printf("  Package: %s\n", package_name);

    // ── Connect to daemon (optional; graceful fallback) ───────────────────────
    struct nova_daemon_conn *daemon = NULL;
    if (!standalone) {
        const char *sock = daemon_socket ? daemon_socket : nova_daemon_default_socket();
        daemon = nova_daemon_connect(sock);
        if (daemon) {
            printf("[Nova] Connected to nova-daemon (%s)\n", sock);
            const char *win_id = package_name ? package_name : apk_path;
            nova_daemon_register_window(daemon, win_id);
        } else {
            printf("[Nova] Daemon not available — running standalone\n");
        }
    }

    // ── Wayland + EGL + ART (same as Phase 0-3) ──────────────────────────────
    struct nova_state *state = nova_state_create();
    if (!state) {
        fprintf(stderr, "[Nova] Failed to create Wayland state\n");
        nova_daemon_disconnect(daemon);
        return 1;
    }

    struct nova_window *win = nova_window_create(state, width, height, title);
    if (!win) {
        fprintf(stderr, "[Nova] Failed to create Wayland window\n");
        nova_state_destroy(state);
        nova_daemon_disconnect(daemon);
        return 1;
    }

    if (nova_art_init(state, argc, argv) != 0) {
        fprintf(stderr, "[Nova] Failed to initialize ART runtime\n");
        nova_window_destroy(win);
        nova_state_destroy(state);
        nova_daemon_disconnect(daemon);
        return 1;
    }

    struct nova_egl *egl = nova_egl_create(state, win);
    if (!egl) {
        fprintf(stderr, "[Nova] Failed to create EGL context\n");
        nova_art_shutdown(state);
        nova_window_destroy(win);
        nova_state_destroy(state);
        nova_daemon_disconnect(daemon);
        return 1;
    }

    if (nova_art_launch_apk(state, apk_path, activity_class) != 0) {
        fprintf(stderr, "[Nova] Failed to launch APK\n");
        nova_egl_destroy(egl);
        nova_art_shutdown(state);
        nova_window_destroy(win);
        nova_state_destroy(state);
        nova_daemon_disconnect(daemon);
        return 1;
    }

    nova_art_init_render(state, win);

    printf("[Nova] Entering event loop.\n");

    while (!win->closed) {
        if (nova_dispatch(state) < 0) break;
        nova_egl_swap_buffers(egl);
    }

    printf("[Nova] Shutting down.\n");

    if (daemon) nova_daemon_unregister_window(daemon, package_name ? package_name : apk_path);

    nova_egl_destroy(egl);
    nova_art_shutdown(state);
    nova_window_destroy(win);
    nova_state_destroy(state);
    nova_daemon_disconnect(daemon);
    return 0;
}
