// nova-daemon — Android system service bus for the Nova Linux runtime
//
// Phase 4: multi-process architecture.
// The daemon runs as a background process and exposes Android system services
// (PackageManager, ActivityManager, WindowPolicyService) to app processes via
// libnova_ipc (Unix domain socket RPC).
//
// Gate test command:  nova-daemon [--socket-path PATH]
// Default socket:     $XDG_RUNTIME_DIR/nova-daemon.sock

#include "nova_ipc.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>

// Service IDs (shared with client stubs)
#define NOVA_SVC_PACKAGE_MANAGER   2u
#define NOVA_SVC_ACTIVITY_MANAGER  3u
#define NOVA_SVC_WINDOW_POLICY     4u

// ── PackageManager service ────────────────────────────────────────────────────

#define TXN_PM_GET_PACKAGES        1u
#define TXN_PM_RESOLVE_ACTIVITY    2u

static int pm_handler(uint32_t txn, nova_parcel *in, nova_parcel *out, void *ud) {
    (void)in; (void)ud;
    if (txn == TXN_PM_GET_PACKAGES) {
        // Return empty list for now
        nova_parcel_write_u32(out, 0);
        return 0;
    }
    if (txn == TXN_PM_RESOLVE_ACTIVITY) {
        char pkg[256] = {0};
        nova_parcel_read_str(in, pkg, sizeof(pkg));
        // Stub: return empty result
        nova_parcel_write_u32(out, 0);
        return 0;
    }
    return -1;
}

// ── ActivityManager service ───────────────────────────────────────────────────

#define TXN_AM_START_ACTIVITY    1u
#define TXN_AM_GET_RUNNING_TASKS 2u

static int am_handler(uint32_t txn, nova_parcel *in, nova_parcel *out, void *ud) {
    (void)in; (void)ud;
    if (txn == TXN_AM_START_ACTIVITY) {
        char pkg[256] = {0};
        nova_parcel_read_str(in, pkg, sizeof(pkg));
        printf("[nova-daemon] ActivityManager: startActivity(%s)\n", pkg);
        nova_parcel_write_u32(out, 0);  // result=OK
        return 0;
    }
    if (txn == TXN_AM_GET_RUNNING_TASKS) {
        nova_parcel_write_u32(out, 0);  // empty
        return 0;
    }
    return -1;
}

// ── WindowPolicyService ───────────────────────────────────────────────────────

#define TXN_WP_REGISTER_WINDOW   1u
#define TXN_WP_FOCUS_WINDOW      2u
#define TXN_WP_UNREGISTER_WINDOW 3u

static int wp_handler(uint32_t txn, nova_parcel *in, nova_parcel *out, void *ud) {
    (void)ud;
    char window_id[64] = {0};
    if (txn == TXN_WP_REGISTER_WINDOW) {
        nova_parcel_read_str(in, window_id, sizeof(window_id));
        printf("[nova-daemon] WindowPolicy: registerWindow(%s)\n", window_id);
        nova_parcel_write_u32(out, 0);
        return 0;
    }
    if (txn == TXN_WP_FOCUS_WINDOW) {
        nova_parcel_read_str(in, window_id, sizeof(window_id));
        printf("[nova-daemon] WindowPolicy: focusWindow(%s)\n", window_id);
        nova_parcel_write_u32(out, 0);
        return 0;
    }
    if (txn == TXN_WP_UNREGISTER_WINDOW) {
        nova_parcel_read_str(in, window_id, sizeof(window_id));
        printf("[nova-daemon] WindowPolicy: unregisterWindow(%s)\n", window_id);
        nova_parcel_write_u32(out, 0);
        return 0;
    }
    return -1;
}

// ─────────────────────────────────────────────────────────────────────────────

static nova_ipc_server *g_server = NULL;

static void on_signal(int sig) {
    (void)sig;
    if (g_server) nova_ipc_server_stop(g_server);
}

int main(int argc, char *argv[]) {
    const char *socket_path = NULL;

    for (int i = 1; i < argc; i++) {
        if ((strcmp(argv[i], "--socket-path") == 0 || strcmp(argv[i], "-s") == 0)
                && i + 1 < argc) {
            socket_path = argv[++i];
        }
    }

    if (!socket_path) {
        const char *xdg = getenv("XDG_RUNTIME_DIR");
        static char default_path[128];
        snprintf(default_path, sizeof(default_path), "%s/nova-daemon.sock",
                 xdg ? xdg : "/tmp");
        socket_path = default_path;
    }

    printf("[nova-daemon] Starting. Socket: %s\n", socket_path);

    g_server = nova_ipc_server_create(socket_path);
    if (!g_server) {
        fprintf(stderr, "[nova-daemon] Failed to create IPC server on %s\n", socket_path);
        return 1;
    }

    nova_ipc_server_register(g_server, NOVA_SVC_PACKAGE_MANAGER,  pm_handler, NULL);
    nova_ipc_server_register(g_server, NOVA_SVC_ACTIVITY_MANAGER, am_handler, NULL);
    nova_ipc_server_register(g_server, NOVA_SVC_WINDOW_POLICY,    wp_handler, NULL);

    signal(SIGTERM, on_signal);
    signal(SIGINT,  on_signal);

    printf("[nova-daemon] Services registered. Waiting for connections.\n");
    nova_ipc_server_run(g_server);

    printf("[nova-daemon] Shutting down.\n");
    nova_ipc_server_destroy(g_server);
    return 0;
}
