#ifndef NOVA_EGL_H
#define NOVA_EGL_H

/* nova_internal.h is exported by libnova_android's export_include_dirs */
#include "nova_internal.h"

struct nova_egl *nova_egl_create(struct nova_state *state, struct nova_window *win);
void nova_egl_destroy(struct nova_egl *egl);
void nova_egl_swap_buffers(struct nova_egl *egl);
struct nova_egl *nova_egl_get_active(void);
void nova_egl_resize_window(int width, int height);

#endif /* NOVA_EGL_H */
