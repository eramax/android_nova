// nova_ipc.c — Unix domain socket RPC implementation

#include "nova_ipc.h"

#include <sys/socket.h>
#include <sys/un.h>
#include <sys/select.h>
#include <unistd.h>
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// ── Parcel ────────────────────────────────────────────────────────────────────

static int parcel_grow(nova_parcel *p, uint32_t needed) {
    if (p->len + needed <= p->cap) return 0;
    uint32_t new_cap = p->cap ? p->cap * 2 : 64;
    while (new_cap < p->len + needed) new_cap *= 2;
    uint8_t *buf = realloc(p->data, new_cap);
    if (!buf) return -1;
    p->data = buf;
    p->cap  = new_cap;
    return 0;
}

void nova_parcel_init(nova_parcel *p) { memset(p, 0, sizeof(*p)); }

void nova_parcel_free(nova_parcel *p) { free(p->data); memset(p, 0, sizeof(*p)); }

void nova_parcel_reset_read(nova_parcel *p) { p->pos = 0; }

void nova_parcel_write_u32(nova_parcel *p, uint32_t v) {
    if (parcel_grow(p, 4)) return;
    memcpy(p->data + p->len, &v, 4);
    p->len += 4;
}

void nova_parcel_write_str(nova_parcel *p, const char *s) {
    uint32_t slen = s ? (uint32_t)strlen(s) : 0;
    nova_parcel_write_u32(p, slen);
    if (slen && parcel_grow(p, slen) == 0) {
        memcpy(p->data + p->len, s, slen);
        p->len += slen;
    }
}

void nova_parcel_write_bytes(nova_parcel *p, const void *buf, uint32_t len) {
    if (!len || parcel_grow(p, len)) return;
    memcpy(p->data + p->len, buf, len);
    p->len += len;
}

int nova_parcel_read_u32(nova_parcel *p, uint32_t *out) {
    if (p->pos + 4 > p->len) return -1;
    memcpy(out, p->data + p->pos, 4);
    p->pos += 4;
    return 0;
}

int nova_parcel_read_str(nova_parcel *p, char *buf, uint32_t buf_size) {
    uint32_t slen;
    if (nova_parcel_read_u32(p, &slen)) return -1;
    if (p->pos + slen > p->len) return -1;
    uint32_t copy = slen < buf_size - 1 ? slen : buf_size - 1;
    memcpy(buf, p->data + p->pos, copy);
    buf[copy] = '\0';
    p->pos += slen;
    return 0;
}

int nova_parcel_read_bytes(nova_parcel *p, void *buf, uint32_t len) {
    if (p->pos + len > p->len) return -1;
    memcpy(buf, p->data + p->pos, len);
    p->pos += len;
    return 0;
}

// ── Wire I/O helpers ──────────────────────────────────────────────────────────

static int write_all(int fd, const void *buf, size_t len) {
    const uint8_t *p = buf;
    while (len > 0) {
        ssize_t r = write(fd, p, len);
        if (r <= 0) return -1;
        p   += r;
        len -= (size_t)r;
    }
    return 0;
}

static int read_all(int fd, void *buf, size_t len) {
    uint8_t *p = buf;
    while (len > 0) {
        ssize_t r = read(fd, p, len);
        if (r <= 0) return -1;
        p   += r;
        len -= (size_t)r;
    }
    return 0;
}

// send: [total_len(4)][service_id(4)][txn(4)][data_len(4)][data]
static int send_request(int fd, uint32_t service_id, uint32_t txn, nova_parcel *in) {
    uint32_t data_len = in ? in->len : 0;
    uint32_t total = 12 + data_len;  // service_id + txn + data_len fields + data
    uint8_t hdr[16];
    memcpy(hdr + 0,  &total,      4);
    memcpy(hdr + 4,  &service_id, 4);
    memcpy(hdr + 8,  &txn,        4);
    memcpy(hdr + 12, &data_len,   4);
    if (write_all(fd, hdr, 16)) return -1;
    if (data_len && write_all(fd, in->data, data_len)) return -1;
    return 0;
}

// recv reply: [total_len(4)][status(4)][data_len(4)][data]
static int recv_reply(int fd, uint32_t *status_out, nova_parcel *out) {
    uint8_t hdr[12];
    if (read_all(fd, hdr, 12)) return -1;
    uint32_t total, status, data_len;
    memcpy(&total,    hdr + 0, 4);
    memcpy(&status,   hdr + 4, 4);
    memcpy(&data_len, hdr + 8, 4);
    if (status_out) *status_out = status;
    if (data_len) {
        if (parcel_grow(out, data_len)) return -1;
        if (read_all(fd, out->data, data_len)) return -1;
        out->len = data_len;
    }
    return 0;
}

static int send_reply(int fd, uint32_t status, nova_parcel *out) {
    uint32_t data_len = out ? out->len : 0;
    uint32_t total = 8 + data_len;  // status + data_len + data
    uint8_t hdr[12];
    memcpy(hdr + 0, &total,    4);
    memcpy(hdr + 4, &status,   4);
    memcpy(hdr + 8, &data_len, 4);
    if (write_all(fd, hdr, 12)) return -1;
    if (data_len && write_all(fd, out->data, data_len)) return -1;
    return 0;
}

// recv request from a client connection
static int recv_request(int fd, uint32_t *svc_out, uint32_t *txn_out, nova_parcel *in) {
    uint8_t hdr[16];
    if (read_all(fd, hdr, 16)) return -1;
    uint32_t total, svc, txn, data_len;
    memcpy(&total,    hdr + 0,  4);
    memcpy(&svc,      hdr + 4,  4);
    memcpy(&txn,      hdr + 8,  4);
    memcpy(&data_len, hdr + 12, 4);
    *svc_out = svc;
    *txn_out = txn;
    if (data_len) {
        if (parcel_grow(in, data_len)) return -1;
        if (read_all(fd, in->data, data_len)) return -1;
        in->len = data_len;
    }
    return 0;
}

// ── Server ────────────────────────────────────────────────────────────────────

#define MAX_SERVICES 16
#define MAX_CLIENTS  64

typedef struct {
    uint32_t         service_id;
    nova_ipc_handler handler;
    void            *userdata;
} service_entry;

struct nova_ipc_server {
    int           listen_fd;
    char          socket_path[108];
    volatile int  running;
    service_entry services[MAX_SERVICES];
    int           n_services;
};

nova_ipc_server *nova_ipc_server_create(const char *socket_path) {
    nova_ipc_server *srv = calloc(1, sizeof(*srv));
    if (!srv) return NULL;

    strncpy(srv->socket_path, socket_path, sizeof(srv->socket_path) - 1);
    unlink(socket_path);

    srv->listen_fd = socket(AF_UNIX, SOCK_STREAM, 0);
    if (srv->listen_fd < 0) { free(srv); return NULL; }

    struct sockaddr_un addr = { .sun_family = AF_UNIX };
    strncpy(addr.sun_path, socket_path, sizeof(addr.sun_path) - 1);

    if (bind(srv->listen_fd, (struct sockaddr *)&addr, sizeof(addr)) < 0) {
        close(srv->listen_fd); free(srv); return NULL;
    }
    if (listen(srv->listen_fd, 8) < 0) {
        close(srv->listen_fd); free(srv); return NULL;
    }
    return srv;
}

int nova_ipc_server_register(nova_ipc_server *srv, uint32_t service_id,
                              nova_ipc_handler handler, void *userdata) {
    if (srv->n_services >= MAX_SERVICES) return -1;
    srv->services[srv->n_services++] = (service_entry){ service_id, handler, userdata };
    return 0;
}

static void handle_client(nova_ipc_server *srv, int client_fd) {
    nova_parcel in, out;
    nova_parcel_init(&in);
    nova_parcel_init(&out);

    uint32_t svc_id, txn;
    while (recv_request(client_fd, &svc_id, &txn, &in) == 0) {
        nova_parcel_reset_read(&in);
        out.len = 0;

        int handled = 0;
        for (int i = 0; i < srv->n_services; i++) {
            if (srv->services[i].service_id == svc_id) {
                int rc = srv->services[i].handler(txn, &in, &out, srv->services[i].userdata);
                send_reply(client_fd, rc == 0 ? NOVA_IPC_OK : (uint32_t)(-rc), &out);
                handled = 1;
                break;
            }
        }
        if (!handled) {
            uint32_t err = 0xFFFFFFFFu;
            send_reply(client_fd, err, NULL);
        }

        in.len = in.pos = 0;
        out.len = 0;
    }

    nova_parcel_free(&in);
    nova_parcel_free(&out);
}

int nova_ipc_server_run(nova_ipc_server *srv) {
    srv->running = 1;
    int client_fds[MAX_CLIENTS];
    int n_clients = 0;

    while (srv->running) {
        fd_set rfds;
        FD_ZERO(&rfds);
        FD_SET(srv->listen_fd, &rfds);
        int max_fd = srv->listen_fd;
        for (int i = 0; i < n_clients; i++) {
            FD_SET(client_fds[i], &rfds);
            if (client_fds[i] > max_fd) max_fd = client_fds[i];
        }

        struct timeval tv = { .tv_sec = 1, .tv_usec = 0 };
        int ready = select(max_fd + 1, &rfds, NULL, NULL, &tv);
        if (ready < 0) { if (errno == EINTR) continue; break; }
        if (ready == 0) continue;

        if (FD_ISSET(srv->listen_fd, &rfds)) {
            int fd = accept(srv->listen_fd, NULL, NULL);
            if (fd >= 0 && n_clients < MAX_CLIENTS)
                client_fds[n_clients++] = fd;
            else if (fd >= 0)
                close(fd);
        }

        for (int i = 0; i < n_clients; ) {
            if (FD_ISSET(client_fds[i], &rfds)) {
                // Handle one request; on EOF remove client
                nova_parcel in, out;
                nova_parcel_init(&in);
                nova_parcel_init(&out);
                uint32_t svc_id, txn;
                if (recv_request(client_fds[i], &svc_id, &txn, &in) == 0) {
                    nova_parcel_reset_read(&in);
                    int handled = 0;
                    for (int j = 0; j < srv->n_services; j++) {
                        if (srv->services[j].service_id == svc_id) {
                            int rc = srv->services[j].handler(txn, &in, &out,
                                                              srv->services[j].userdata);
                            send_reply(client_fds[i], rc == 0 ? NOVA_IPC_OK : (uint32_t)(-rc), &out);
                            handled = 1;
                            break;
                        }
                    }
                    if (!handled) send_reply(client_fds[i], 0xFFFFFFFFu, NULL);
                    nova_parcel_free(&in);
                    nova_parcel_free(&out);
                    i++;
                } else {
                    nova_parcel_free(&in);
                    nova_parcel_free(&out);
                    close(client_fds[i]);
                    client_fds[i] = client_fds[--n_clients];
                }
            } else {
                i++;
            }
        }
    }

    for (int i = 0; i < n_clients; i++) close(client_fds[i]);
    return 0;
}

void nova_ipc_server_stop(nova_ipc_server *srv) { srv->running = 0; }

void nova_ipc_server_destroy(nova_ipc_server *srv) {
    if (!srv) return;
    close(srv->listen_fd);
    unlink(srv->socket_path);
    free(srv);
}

// ── Client ────────────────────────────────────────────────────────────────────

struct nova_ipc_client {
    int fd;
};

nova_ipc_client *nova_ipc_client_connect(const char *socket_path) {
    int fd = socket(AF_UNIX, SOCK_STREAM, 0);
    if (fd < 0) return NULL;

    struct sockaddr_un addr = { .sun_family = AF_UNIX };
    strncpy(addr.sun_path, socket_path, sizeof(addr.sun_path) - 1);

    if (connect(fd, (struct sockaddr *)&addr, sizeof(addr)) < 0) {
        close(fd); return NULL;
    }

    nova_ipc_client *cli = malloc(sizeof(*cli));
    if (!cli) { close(fd); return NULL; }
    cli->fd = fd;
    return cli;
}

int nova_ipc_transact(nova_ipc_client *cli, uint32_t service_id, uint32_t txn,
                      nova_parcel *in, nova_parcel *out) {
    if (send_request(cli->fd, service_id, txn, in)) return NOVA_IPC_ERR_SEND;
    uint32_t status = 0;
    if (recv_reply(cli->fd, &status, out)) return NOVA_IPC_ERR_RECV;
    return status == NOVA_IPC_OK ? 0 : -(int)status;
}

void nova_ipc_client_destroy(nova_ipc_client *cli) {
    if (!cli) return;
    close(cli->fd);
    free(cli);
}

// ── Callback server ───────────────────────────────────────────────────────────

struct nova_ipc_cb_server {
    int  listen_fd;
    char socket_path[108];
};

nova_ipc_cb_server *nova_ipc_cb_server_create(char *socket_path_out) {
    nova_ipc_cb_server *cb = malloc(sizeof(*cb));
    if (!cb) return NULL;

    snprintf(cb->socket_path, sizeof(cb->socket_path),
             "/tmp/nova-cb-%d.sock", getpid());
    unlink(cb->socket_path);

    cb->listen_fd = socket(AF_UNIX, SOCK_STREAM, 0);
    if (cb->listen_fd < 0) { free(cb); return NULL; }

    struct sockaddr_un addr = { .sun_family = AF_UNIX };
    strncpy(addr.sun_path, cb->socket_path, sizeof(addr.sun_path) - 1);

    if (bind(cb->listen_fd, (struct sockaddr *)&addr, sizeof(addr)) < 0 ||
        listen(cb->listen_fd, 1) < 0) {
        close(cb->listen_fd); free(cb); return NULL;
    }

    strncpy(socket_path_out, cb->socket_path, 107);
    return cb;
}

int nova_ipc_cb_server_wait(nova_ipc_cb_server *srv, nova_ipc_handler handler,
                             void *userdata) {
    int fd = accept(srv->listen_fd, NULL, NULL);
    if (fd < 0) return -1;

    nova_parcel in, out;
    nova_parcel_init(&in);
    nova_parcel_init(&out);

    uint32_t svc_id, txn;
    int rc = -1;
    if (recv_request(fd, &svc_id, &txn, &in) == 0) {
        nova_parcel_reset_read(&in);
        rc = handler(txn, &in, &out, userdata);
        send_reply(fd, rc == 0 ? NOVA_IPC_OK : (uint32_t)(-rc), &out);
    }

    nova_parcel_free(&in);
    nova_parcel_free(&out);
    close(fd);
    return rc;
}

void nova_ipc_cb_server_destroy(nova_ipc_cb_server *cb) {
    if (!cb) return;
    close(cb->listen_fd);
    unlink(cb->socket_path);
    free(cb);
}
