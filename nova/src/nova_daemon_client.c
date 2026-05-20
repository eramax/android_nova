// nova_daemon_client.c — package registry + daemon connection implementation

#include "nova_daemon_client.h"
#include "nova_ipc.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/stat.h>
#include <errno.h>
#include <libgen.h>

// ── Packages directory ────────────────────────────────────────────────────────

static void get_packages_dir(char *out, int size) {
    const char *env = getenv("NOVA_PACKAGES_DIR");
    if (env) {
        snprintf(out, size, "%s", env);
        return;
    }
    const char *home = getenv("HOME");
    snprintf(out, size, "%s/.local/share/nova/packages", home ? home : "/tmp");
}

static void ensure_dir(const char *path) {
    // Recursively create directory (best-effort)
    char tmp[4096];
    snprintf(tmp, sizeof(tmp), "%s", path);
    for (char *p = tmp + 1; *p; p++) {
        if (*p == '/') {
            *p = '\0';
            mkdir(tmp, 0755);
            *p = '/';
        }
    }
    mkdir(tmp, 0755);
}

int nova_resolve_package(const char *package_name, char *out_path, int out_size) {
    char dir[4096];
    get_packages_dir(dir, sizeof(dir));
    snprintf(out_path, out_size, "%s/%s.apk", dir, package_name);
    if (access(out_path, R_OK) == 0) return 0;
    // Also try without .apk extension as a directory (split APK future use)
    return -1;
}

int nova_install_apk(const char *apk_path) {
    // Extract package name via aapt
    char cmd[4096];
    snprintf(cmd, sizeof(cmd),
             "aapt dump badging '%s' 2>/dev/null | grep \"^package:\" | "
             "sed \"s/.*name='\\([^']*\\)'.*/\\1/\"",
             apk_path);

    FILE *fp = popen(cmd, "r");
    if (!fp) {
        fprintf(stderr, "[Nova] Failed to run aapt\n");
        return -1;
    }
    char pkg[256] = {0};
    if (!fgets(pkg, sizeof(pkg), fp)) {
        pclose(fp);
        fprintf(stderr, "[Nova] Could not extract package name from %s\n", apk_path);
        return -1;
    }
    pclose(fp);

    // Strip trailing newline/whitespace
    char *end = pkg + strlen(pkg) - 1;
    while (end > pkg && (*end == '\n' || *end == '\r' || *end == ' ')) *end-- = '\0';

    if (!pkg[0]) {
        fprintf(stderr, "[Nova] Empty package name from %s\n", apk_path);
        return -1;
    }

    char dir[4096];
    get_packages_dir(dir, sizeof(dir));
    ensure_dir(dir);

    char dest[4096];
    snprintf(dest, sizeof(dest), "%s/%s.apk", dir, pkg);

    // Copy APK
    char cp_cmd[8192];
    snprintf(cp_cmd, sizeof(cp_cmd), "cp '%s' '%s'", apk_path, dest);
    int rc = system(cp_cmd);
    if (rc != 0) {
        fprintf(stderr, "[Nova] Failed to copy %s → %s\n", apk_path, dest);
        return -1;
    }

    printf("[Nova] Installed %s → %s\n", pkg, dest);
    return 0;
}

// ── Daemon connection ─────────────────────────────────────────────────────────

// Service IDs (must match nova-daemon/src/main.c)
#define NOVA_SVC_WINDOW_POLICY     4u
#define TXN_WP_REGISTER_WINDOW     1u
#define TXN_WP_UNREGISTER_WINDOW   3u

struct nova_daemon_conn {
    nova_ipc_client *cli;
};

const char *nova_daemon_default_socket(void) {
    static char path[128];
    const char *xdg = getenv("XDG_RUNTIME_DIR");
    snprintf(path, sizeof(path), "%s/nova-daemon.sock", xdg ? xdg : "/tmp");
    return path;
}

struct nova_daemon_conn *nova_daemon_connect(const char *socket_path) {
    nova_ipc_client *cli = nova_ipc_client_connect(socket_path);
    if (!cli) return NULL;

    struct nova_daemon_conn *conn = malloc(sizeof(*conn));
    if (!conn) { nova_ipc_client_destroy(cli); return NULL; }
    conn->cli = cli;
    return conn;
}

void nova_daemon_disconnect(struct nova_daemon_conn *conn) {
    if (!conn) return;
    nova_ipc_client_destroy(conn->cli);
    free(conn);
}

int nova_daemon_register_window(struct nova_daemon_conn *conn, const char *window_id) {
    if (!conn || !window_id) return -1;
    nova_parcel in, out;
    nova_parcel_init(&in);
    nova_parcel_init(&out);
    nova_parcel_write_str(&in, window_id);
    int rc = nova_ipc_transact(conn->cli, NOVA_SVC_WINDOW_POLICY,
                               TXN_WP_REGISTER_WINDOW, &in, &out);
    nova_parcel_free(&in);
    nova_parcel_free(&out);
    return rc;
}

int nova_daemon_unregister_window(struct nova_daemon_conn *conn, const char *window_id) {
    if (!conn || !window_id) return -1;
    nova_parcel in, out;
    nova_parcel_init(&in);
    nova_parcel_init(&out);
    nova_parcel_write_str(&in, window_id);
    int rc = nova_ipc_transact(conn->cli, NOVA_SVC_WINDOW_POLICY,
                               TXN_WP_UNREGISTER_WINDOW, &in, &out);
    nova_parcel_free(&in);
    nova_parcel_free(&out);
    return rc;
}
