#ifndef NOVA_BIONIC_LOADER_H
#define NOVA_BIONIC_LOADER_H

#include <stdint.h>

void *bionic_dlopen(const char *path, int flags);
void *bionic_dlsym(void *handle, const char *name);
int   bionic_dlclose(void *handle);
const char *bionic_dlerror(void);

void bionic_loader_set_lib_path(const char *colon_separated_paths);

#endif
