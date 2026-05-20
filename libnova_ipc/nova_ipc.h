// nova_ipc.h — Unix domain socket RPC transport for nova-daemon
// Replaces libbinder_rpc (frameworks/native not in partial checkout).
// Same architectural role: service registration, transactions, callbacks.
//
// Wire protocol (little-endian uint32):
//   Request:  [total_len][service_id][txn_code][data_len][data...]
//   Reply:    [total_len][status][data_len][data...]
//   Callback: server connects back to a client-supplied socket path
//             and sends a request on that reverse connection.

#pragma once

#include <stdint.h>
#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

// ── Parcel ────────────────────────────────────────────────────────────────────

typedef struct {
    uint8_t  *data;
    uint32_t  len;
    uint32_t  cap;
    uint32_t  pos;  // read cursor
} nova_parcel;

void     nova_parcel_init(nova_parcel *p);
void     nova_parcel_free(nova_parcel *p);
void     nova_parcel_reset_read(nova_parcel *p);

void     nova_parcel_write_u32(nova_parcel *p, uint32_t v);
void     nova_parcel_write_str(nova_parcel *p, const char *s);  // 4-byte len + bytes (no NUL)
void     nova_parcel_write_bytes(nova_parcel *p, const void *buf, uint32_t len);

int      nova_parcel_read_u32(nova_parcel *p, uint32_t *out);
int      nova_parcel_read_str(nova_parcel *p, char *buf, uint32_t buf_size);
int      nova_parcel_read_bytes(nova_parcel *p, void *buf, uint32_t len);

// ── Server ────────────────────────────────────────────────────────────────────

typedef struct nova_ipc_server nova_ipc_server;

// Service handler: receives in-parcel, writes reply into out-parcel.
// Returns 0 on success, negative on error (reply will have non-zero status).
typedef int (*nova_ipc_handler)(uint32_t txn, nova_parcel *in, nova_parcel *out,
                                void *userdata);

nova_ipc_server *nova_ipc_server_create(const char *socket_path);
int              nova_ipc_server_register(nova_ipc_server *srv, uint32_t service_id,
                                          nova_ipc_handler handler, void *userdata);

// Run the server event loop (blocks until nova_ipc_server_stop is called).
int  nova_ipc_server_run(nova_ipc_server *srv);
void nova_ipc_server_stop(nova_ipc_server *srv);
void nova_ipc_server_destroy(nova_ipc_server *srv);

// ── Client ────────────────────────────────────────────────────────────────────

typedef struct nova_ipc_client nova_ipc_client;

nova_ipc_client *nova_ipc_client_connect(const char *socket_path);

// Send a transaction to the specified service and wait for a reply.
// Returns 0 on success.
int nova_ipc_transact(nova_ipc_client *cli, uint32_t service_id, uint32_t txn,
                      nova_parcel *in, nova_parcel *out);

void nova_ipc_client_destroy(nova_ipc_client *cli);

// ── Callback server (app-side) ────────────────────────────────────────────────
// Lets a daemon call back into the client process.

typedef struct nova_ipc_cb_server nova_ipc_cb_server;

// Start a mini one-shot callback server on a temp socket.
// socket_path_out must be at least 108 bytes.
nova_ipc_cb_server *nova_ipc_cb_server_create(char *socket_path_out);

// Block until one incoming transaction is received; invoke handler; return.
int nova_ipc_cb_server_wait(nova_ipc_cb_server *srv, nova_ipc_handler handler,
                             void *userdata);

void nova_ipc_cb_server_destroy(nova_ipc_cb_server *srv);

// Transaction codes for the IHello service (service_id = NOVA_SVC_HELLO)
#define NOVA_SVC_HELLO       1u
#define NOVA_TXN_HELLO_GREET 1u   // hello(string name) → string greeting
#define NOVA_TXN_HELLO_PING  2u   // ping(string cb_socket) → triggers callback

// Status codes
#define NOVA_IPC_OK            0
#define NOVA_IPC_ERR_CONNECT  -1
#define NOVA_IPC_ERR_SEND     -2
#define NOVA_IPC_ERR_RECV     -3
#define NOVA_IPC_ERR_HANDLER  -4
#define NOVA_IPC_ERR_NOMEM    -5

#ifdef __cplusplus
}
#endif
