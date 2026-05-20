// nova_daemon_client.h — helpers for nova binary ↔ nova-daemon communication
// and package registry (APK path resolution from package name).

#pragma once

#ifdef __cplusplus
extern "C" {
#endif

// ── Package registry ──────────────────────────────────────────────────────────

// Packages directory: $NOVA_PACKAGES_DIR or $HOME/.local/share/nova/packages/
// APKs are stored as <package-name>.apk

// Resolve package name to APK path.
// Returns 0 on success, -1 if not found.
int nova_resolve_package(const char *package_name, char *out_path, int out_size);

// Copy apk_path into the packages directory, extracting package name via aapt.
// Returns 0 on success.
int nova_install_apk(const char *apk_path);

// ── Daemon connection ─────────────────────────────────────────────────────────

struct nova_daemon_conn;

const char *nova_daemon_default_socket(void);

// Connect to daemon; returns NULL if daemon is not running (not an error).
struct nova_daemon_conn *nova_daemon_connect(const char *socket_path);

void nova_daemon_disconnect(struct nova_daemon_conn *conn);

// Notify WindowPolicyService of window lifecycle.
int nova_daemon_register_window(struct nova_daemon_conn *conn, const char *window_id);
int nova_daemon_unregister_window(struct nova_daemon_conn *conn, const char *window_id);

#ifdef __cplusplus
}
#endif
