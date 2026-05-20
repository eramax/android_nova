// nova_ipc_test.c — P4-T1 integration test for libnova_ipc
//
// Tests:
//   1. IHello.greet("world") → "Hello, world!"
//   2. IHello.ping(cb_socket) → daemon connects to cb_socket and sends "pong"
//
// Architecture: fork() into daemon + client; both share a socket path.

#include "nova_ipc.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/wait.h>

#define TEST_SOCKET "/tmp/nova-ipc-test.sock"

// ── IHello service implementation (daemon side) ───────────────────────────────

static int ihello_handler(uint32_t txn, nova_parcel *in, nova_parcel *out, void *userdata) {
    (void)userdata;
    char buf[256];

    if (txn == NOVA_TXN_HELLO_GREET) {
        // greet(string name) → "Hello, <name>!"
        if (nova_parcel_read_str(in, buf, sizeof(buf))) return -1;
        char reply[280];
        snprintf(reply, sizeof(reply), "Hello, %s!", buf);
        nova_parcel_write_str(out, reply);
        return 0;
    }

    if (txn == NOVA_TXN_HELLO_PING) {
        // ping(string cb_socket_path) → connect to cb_socket, send "pong" callback
        if (nova_parcel_read_str(in, buf, sizeof(buf))) return -1;

        // Connect to client's callback socket
        nova_ipc_client *cb_cli = nova_ipc_client_connect(buf);
        if (!cb_cli) {
            fprintf(stderr, "[daemon] Failed to connect to callback socket: %s\n", buf);
            return -1;
        }

        nova_parcel cb_in, cb_out;
        nova_parcel_init(&cb_in);
        nova_parcel_init(&cb_out);
        nova_parcel_write_str(&cb_in, "pong");

        int rc = nova_ipc_transact(cb_cli, NOVA_SVC_HELLO, NOVA_TXN_HELLO_GREET,
                                   &cb_in, &cb_out);
        nova_parcel_free(&cb_in);
        nova_parcel_free(&cb_out);
        nova_ipc_client_destroy(cb_cli);

        if (rc != 0) return -1;

        // Write acknowledgement in reply
        nova_parcel_write_str(out, "callback_sent");
        return 0;
    }

    return -1;
}

// ── Daemon process ────────────────────────────────────────────────────────────

static void run_daemon(void) {
    nova_ipc_server *srv = nova_ipc_server_create(TEST_SOCKET);
    if (!srv) {
        fprintf(stderr, "[daemon] Failed to create server\n");
        exit(1);
    }
    nova_ipc_server_register(srv, NOVA_SVC_HELLO, ihello_handler, NULL);
    fprintf(stderr, "[daemon] Listening on %s\n", TEST_SOCKET);
    nova_ipc_server_run(srv);  // runs until parent kills us
    nova_ipc_server_destroy(srv);
    exit(0);
}

// ── Callback handler (client side for ping test) ──────────────────────────────

static char g_callback_value[256];

static int client_cb_handler(uint32_t txn, nova_parcel *in, nova_parcel *out, void *ud) {
    (void)ud;
    if (txn == NOVA_TXN_HELLO_GREET) {
        nova_parcel_read_str(in, g_callback_value, sizeof(g_callback_value));
        nova_parcel_write_str(out, "ack");
        return 0;
    }
    return -1;
}

// ── Test runner ───────────────────────────────────────────────────────────────

#define PASS(fmt, ...) do { printf("[PASS] " fmt "\n", ##__VA_ARGS__); } while(0)
#define FAIL(fmt, ...) do { printf("[FAIL] " fmt "\n", ##__VA_ARGS__); failures++; } while(0)

static int failures = 0;

static void run_tests(void) {
    // Give daemon a moment to start listening
    usleep(50000);

    nova_ipc_client *cli = NULL;
    for (int i = 0; i < 20; i++) {
        cli = nova_ipc_client_connect(TEST_SOCKET);
        if (cli) break;
        usleep(50000);
    }
    if (!cli) { FAIL("Could not connect to daemon"); return; }

    // ── Test 1: greet ──────────────────────────────────────────────────────
    {
        nova_parcel in, out;
        nova_parcel_init(&in);
        nova_parcel_init(&out);
        nova_parcel_write_str(&in, "world");

        int rc = nova_ipc_transact(cli, NOVA_SVC_HELLO, NOVA_TXN_HELLO_GREET, &in, &out);
        if (rc != 0) {
            FAIL("greet() transact returned %d", rc);
        } else {
            char reply[256] = {0};
            nova_parcel_reset_read(&out);
            nova_parcel_read_str(&out, reply, sizeof(reply));
            if (strcmp(reply, "Hello, world!") == 0)
                PASS("greet(\"world\") → \"%s\"", reply);
            else
                FAIL("greet expected \"Hello, world!\", got \"%s\"", reply);
        }
        nova_parcel_free(&in);
        nova_parcel_free(&out);
    }

    // ── Test 2: callback (daemon → client) ────────────────────────────────
    {
        char cb_path[108];
        nova_ipc_cb_server *cb_srv = nova_ipc_cb_server_create(cb_path);
        if (!cb_srv) { FAIL("Could not create callback server"); goto done; }

        nova_parcel in, out;
        nova_parcel_init(&in);
        nova_parcel_init(&out);
        nova_parcel_write_str(&in, cb_path);  // tell daemon where to call back

        // Send ping in background (we need to accept callback before reply)
        // Use a second connection so we can interleave.
        nova_ipc_client *cli2 = nova_ipc_client_connect(TEST_SOCKET);
        if (!cli2) { FAIL("Second connection failed"); nova_parcel_free(&in); nova_parcel_free(&out); nova_ipc_cb_server_destroy(cb_srv); goto done; }

        // Fork: child sends ping, parent waits for callback
        fflush(stdout);
        pid_t pid = fork();
        if (pid == 0) {
            // child: send ping request (no stdout needed)
            fclose(stdout);
            nova_ipc_transact(cli2, NOVA_SVC_HELLO, NOVA_TXN_HELLO_PING, &in, &out);
            exit(0);
        }

        // parent: wait for callback from daemon
        g_callback_value[0] = '\0';
        int rc = nova_ipc_cb_server_wait(cb_srv, client_cb_handler, NULL);
        waitpid(pid, NULL, 0);

        if (rc != 0) {
            FAIL("callback server wait returned %d", rc);
        } else if (strcmp(g_callback_value, "pong") == 0) {
            PASS("ping() → daemon called back with \"%s\"", g_callback_value);
        } else {
            FAIL("callback expected \"pong\", got \"%s\"", g_callback_value);
        }

        nova_parcel_free(&in);
        nova_parcel_free(&out);
        nova_ipc_client_destroy(cli2);
        nova_ipc_cb_server_destroy(cb_srv);
    }

done:
    nova_ipc_client_destroy(cli);
}

// ─────────────────────────────────────────────────────────────────────────────

int main(void) {
    printf("nova_ipc_test — P4-T1: libnova_ipc integration test\n");
    printf("  socket: %s\n", TEST_SOCKET);

    fflush(stdout);  // flush before fork so daemon child doesn't duplicate buffered output

    pid_t daemon_pid = fork();
    if (daemon_pid < 0) {
        perror("fork");
        return 1;
    }
    if (daemon_pid == 0) {
        fclose(stdout);  // daemon doesn't need stdout
        run_daemon();
        return 0;  // not reached
    }

    run_tests();

    kill(daemon_pid, SIGTERM);
    waitpid(daemon_pid, NULL, 0);
    unlink(TEST_SOCKET);

    if (failures == 0) {
        printf("\nAll tests PASSED.\n");
        return 0;
    } else {
        printf("\n%d test(s) FAILED.\n", failures);
        return 1;
    }
}
