#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <errno.h>
#include <dirent.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <dlfcn.h>
#include "bionic_elf.h"
#include "bionic_loader.h"

#define LOG(fmt, ...) do { \
    if (getenv("NOVA_BIONIC_VERBOSE")) \
        fprintf(stderr, "[bionic_loader] " fmt "\n", ##__VA_ARGS__); \
} while (0)

#define MAX_NEEDED 256
#define MAX_LOADED 256

typedef struct bionic_so {
    char            *path;
    int              fd;
    u8              *base;        /* mmap base; runtime addr = base + p_vaddr */
    size_t           size;        /* total mapped size */
    u8              *strtab;      /* runtime address */
    size_t           strsz;
    Elf64_Sym       *symtab;      /* runtime address */
    size_t           syment;
    /* DT_HASH (linear) */
    u32             *hash;
    u32             *buckets;
    u32              nbucket;
    u32             *chains;
    u32              nchain;
    /* DT_GNU_HASH */
    u32             *gnu_bloom;
    u32              gnu_bloom_shift;
    u32             *gnu_buckets;
    u32             *gnu_chains;
    u32              gnu_nbucket;
    u32              gnu_symndx;
    u32              gnu_maskwords;
    /* DT_RELA */
    Elf64_Rela      *rela;
    size_t           relasz;
    size_t           relaent;
    size_t           relacount;  /* 0 = apply all, else skip [relacount..n) */
    size_t           nsym;       /* size of .dynsym in entries (from SHT_DYNSYM) */
    /* DT_JMPREL (PLT relocations) */
    Elf64_Rela      *jmprela;
    size_t           jmprelsz;
    /* DT_NEEDED */
    char            *needed_names[MAX_NEEDED];
    int              n_needed;
    int              refcount;
    struct bionic_so *next;
} bionic_so_t;

static bionic_so_t *g_loaded_head = NULL;
static char g_error[256] = "";
static char g_lib_path[4096] =
    "/mnt/mydata/projects2/0/aosp-full/vendor/nova/aosp-prebuilt/lib64"
    ":/mnt/mydata/projects2/0/aosp-full/vendor/nova/aosp-prebuilt/lib64/bootstrap"
    ":/mnt/mydata/projects2/0/aosp-full/vendor/nova/aosp-prebuilt/apex-flat/com.android.runtime/lib64/bionic"
    ":/mnt/mydata/projects2/0/aosp-full/vendor/nova/aosp-prebuilt/apex-flat/com.android.i18n/lib64";

/* Global symbol index: maps unversioned symbol name to .so path that exports it.
   Built lazily on first resolve_symbol miss. Open-addressing hash, ~1M buckets. */
#define SYM_IDX_BUCKETS (1 << 20)
static struct {
    char *name;
    char *path;
} g_sym_idx[SYM_IDX_BUCKETS];
static int g_sym_idx_count = 0;
static int g_sym_idx_built = 0;

static u32 name_hash(const char *s) {
    u32 h = 5381;
    for (; *s; s++) h = (h << 5) + h + (u8)*s;
    return h;
}

static void sym_idx_add(const char *name, const char *path) {
    u32 h = name_hash(name) % SYM_IDX_BUCKETS;
    for (u32 i = 0; i < SYM_IDX_BUCKETS; i++) {
        u32 idx = (h + i) % SYM_IDX_BUCKETS;
        if (!g_sym_idx[idx].name) {
            g_sym_idx[idx].name = strdup(name);
            g_sym_idx[idx].path = strdup(path);
            g_sym_idx_count++;
            return;
        }
    }
}

static const char *sym_idx_lookup(const char *name) {
    u32 h = name_hash(name) % SYM_IDX_BUCKETS;
    for (u32 i = 0; i < SYM_IDX_BUCKETS; i++) {
        u32 idx = (h + i) % SYM_IDX_BUCKETS;
        if (!g_sym_idx[idx].name) return NULL;
        if (strcmp(g_sym_idx[idx].name, name) == 0) return g_sym_idx[idx].path;
    }
    return NULL;
}

static int already_indexed(const char *path) {
    /* O(n) check; called once per .so so cost is one-time. */
    for (u32 i = 0; i < SYM_IDX_BUCKETS; i++) {
        if (g_sym_idx[i].path && strcmp(g_sym_idx[i].path, path) == 0) return 1;
    }
    return 0;
}

static void build_sym_index(void);

static void index_one_so(const char *dir, const char *fname) {
    char path[4096];
    snprintf(path, sizeof(path), "%s/%s", dir, fname);
    if (already_indexed(path)) return;
    int fd = open(path, O_RDONLY | O_CLOEXEC);
    if (fd < 0) return;
    struct stat st;
    if (fstat(fd, &st) < 0) { close(fd); return; }
    void *map = mmap(NULL, st.st_size, PROT_READ, MAP_PRIVATE, fd, 0);
    if (map == MAP_FAILED) { close(fd); return; }
    const Elf64_Ehdr *eh = (const Elf64_Ehdr *)map;
    if (memcmp(eh->e_ident, "\x7f""ELF", 4) != 0 || eh->e_ident[4] != 2 || eh->e_machine != 0x3e) {
        munmap(map, st.st_size); close(fd); return;
    }
    const Elf64_Phdr *ph = (const Elf64_Phdr *)((u8 *)map + eh->e_phoff);
    u64 dyn_off = 0;
    for (u16 i = 0; i < eh->e_phnum; i++) {
        if (ph[i].p_type == PT_DYNAMIC) { dyn_off = ph[i].p_offset; break; }
    }
    if (dyn_off == 0) { munmap(map, st.st_size); close(fd); return; }
    const Elf64_Dyn *dyn = (const Elf64_Dyn *)((u8 *)map + dyn_off);
    u64 symtab = 0, strtab = 0, strsz = 0;
    for (; dyn->d_tag != DT_NULL; dyn++) {
        if (dyn->d_tag == DT_SYMTAB) symtab = dyn->d_val;
        else if (dyn->d_tag == DT_STRTAB) strtab = dyn->d_val;
        else if (dyn->d_tag == DT_STRSZ) strsz = dyn->d_val;
    }
    if (!symtab || !strtab || !strsz) { munmap(map, st.st_size); close(fd); return; }
    size_t nsym = 0;
    if (eh->e_shoff && eh->e_shnum) {
        const Elf64_Shdr *sh = (const Elf64_Shdr *)((u8 *)eh + eh->e_shoff);
        for (u16 i = 0; i < eh->e_shnum; i++) {
            if (sh[i].sh_type == SHT_DYNSYM && sh[i].sh_entsize == sizeof(Elf64_Sym)) {
                nsym = sh[i].sh_size / sh[i].sh_entsize;
                break;
            }
        }
    }
    if (!nsym) { munmap(map, st.st_size); close(fd); return; }
    const Elf64_Sym *syms = (const Elf64_Sym *)((u8 *)map + symtab);
    const char *strs = (const char *)((u8 *)map + strtab);
    int added = 0;
    for (size_t i = 1; i < nsym; i++) {
        if (syms[i].st_name >= strsz) continue;
        if (syms[i].st_shndx == 0) continue;  /* undefined */
        if (syms[i].st_value == 0) continue;
        const char *sname = strs + syms[i].st_name;
        if (sname[0] == 0) continue;
        char clean[256];
        size_t sl = strlen(sname);
        if (sl >= sizeof(clean)) sl = sizeof(clean) - 1;
        memcpy(clean, sname, sl);
        clean[sl] = '\0';
        char *at = strchr(clean, '@');
        if (at) *at = '\0';
        sym_idx_add(clean, path);
        added++;
    }
    LOG("[bionic_loader] indexed %s: +%d symbols", path, added);
    munmap(map, st.st_size);
    close(fd);
}

static void build_sym_index(void) {
    if (g_sym_idx_built) return;
    g_sym_idx_built = 1;
    LOG("[bionic_loader] building global symbol index from lib_path...");
    char dir[4096];
    const char *p = g_lib_path;
    while (*p) {
        const char *e = strchr(p, ':');
        size_t n = e ? (size_t)(e - p) : strlen(p);
        if (n >= sizeof(dir)) n = sizeof(dir) - 1;
        memcpy(dir, p, n);
        dir[n] = '\0';
        DIR *d = opendir(dir);
        if (d) {
            struct dirent *de;
            while ((de = readdir(d))) {
                size_t nl = strlen(de->d_name);
                if (nl < 4) continue;
                if (strcmp(de->d_name + nl - 3, ".so") != 0) continue;
                index_one_so(dir, de->d_name);
            }
            closedir(d);
        }
        if (!e) break;
        p = e + 1;
    }
    LOG("[bionic_loader] symbol index built: %d entries", g_sym_idx_count);
}

void bionic_loader_set_lib_path(const char *p) {
    strncpy(g_lib_path, p, sizeof(g_lib_path) - 1);
    g_lib_path[sizeof(g_lib_path) - 1] = '\0';
}

const char *bionic_dlerror(void) {
    const char *e = g_error;
    g_error[0] = '\0';
    return e;
}

static void set_error(const char *fmt, ...) {
    va_list ap;
    va_start(ap, fmt);
    vsnprintf(g_error, sizeof(g_error), fmt, ap);
    va_end(ap);
}

/* Helpers */

static u64 dyn_get(const Elf64_Dyn *d, u64 tag, u64 dflt) {
    for (; d->d_tag != DT_NULL; d++) {
        if (d->d_tag == (i64)tag) return d->d_val;
    }
    return dflt;
}

static int find_dynamic_phdr(const Elf64_Ehdr *eh, u64 *out_off) {
    const Elf64_Phdr *ph = (const Elf64_Phdr *)((u8 *)eh + eh->e_phoff);
    for (u16 i = 0; i < eh->e_phnum; i++) {
        if (ph[i].p_type == PT_DYNAMIC) {
            *out_off = ph[i].p_offset;
            return 0;
        }
    }
    return -1;
}

/* Hash lookups */

static u32 gnu_hash(const char *s) {
    u32 h = 5381;
    for (; *s; s++) h = (h << 5) + h + (u8)*s;
    return h;
}

static const Elf64_Sym *hash_lookup(const bionic_so_t *so, const char *name) {
    if (!so->hash) return NULL;
    u32 h = 0;
    for (const u8 *p = (const u8 *)name; *p; p++) h = (h << 4) + *p;
    u32 bucket = h % so->nbucket;
    u32 idx = so->buckets[bucket];
    if (idx == 0) return NULL;
    const char *strtab = (const char *)so->strtab;
    while (idx < so->nchain) {
        const Elf64_Sym *s = &so->symtab[idx];
        if (s->st_name && strcmp(strtab + s->st_name, name) == 0) return s;
        if (so->chains[idx] == 0) break;
        idx = so->chains[idx];
    }
    return NULL;
}

static const Elf64_Sym *gnu_hash_lookup(const bionic_so_t *so, const char *name) {
    if (!so->gnu_bloom) return NULL;
    u32 h = gnu_hash(name);
    u32 bloom_word = so->gnu_bloom[(h / 32) % so->gnu_maskwords];
    u32 mask = (1 << (h % 32)) | (1 << ((h >> so->gnu_bloom_shift) % 32));
    if ((bloom_word & mask) != mask) return NULL;
    u32 bucket = h % so->gnu_nbucket;
    u32 idx = so->gnu_buckets[bucket];
    if (idx == 0) return NULL;
    const char *strtab = (const char *)so->strtab;
    /* Walk bucket chain. Each entry: chain[ndx - gnu_symndx] encodes hash bits
       (low bit = end-of-chain marker). */
    while (idx >= so->gnu_symndx) {
        const Elf64_Sym *s = &so->symtab[idx];
        const char *sname = s->st_name ? (strtab + s->st_name) : "";
        u32 chain_val = so->gnu_chains[idx - so->gnu_symndx];
        /* If chain_val bit 0 == h bit 0, candidate */
        if (((chain_val ^ h) >> 1) == 0) {
            if (sname[0] == name[0] && strcmp(sname, name) == 0) return s;
        }
        if (chain_val & 1) break;
        idx++;
    }
    return NULL;
}

static const Elf64_Sym *sym_lookup(const bionic_so_t *so, const char *name) {
    /* Linear scan only - bionic gnu_hash tables are non-standard and crash.
       Strip bionic versioning (@LIBGLESV1_CM, @LIBC, @LIBC_N, etc.) from
       the candidate names since bionic's exported symbols carry versions
       but undefined imports reference unversioned names. */
    if (!so->symtab || !so->strtab) return NULL;
    const char *strtab = (const char *)so->strtab;
    /* Use nsym from SHT_DYNSYM if known, else 16K cap */
    u32 cap = so->nsym;
    if (cap == 0) cap = 0x4000;
    char n0 = name[0];
    size_t namelen = strlen(name);
    for (u32 i = 1; i < cap; i++) {
        const Elf64_Sym *s2 = &so->symtab[i];
        if (s2->st_name == 0 || s2->st_name >= so->strsz) continue;
        const char *sname = strtab + s2->st_name;
        if (sname[0] != n0) continue;
        /* Compare up to namelen chars or first '@' */
        const char *at = strchr(sname, '@');
        size_t slen = at ? (size_t)(at - sname) : strlen(sname);
        if (slen == namelen && memcmp(sname, name, namelen) == 0) return s2;
    }
    return NULL;
}

/* Load a .so file into memory and populate bionic_so_t. Returns 0 on success. */
static int load_so_file(const char *path, bionic_so_t *so) {
    int fd = open(path, O_RDONLY | O_CLOEXEC);
    if (fd < 0) { set_error("open %s: %s", path, strerror(errno)); return -1; }
    struct stat st;
    if (fstat(fd, &st) < 0) { set_error("fstat %s: %s", path, strerror(errno)); close(fd); return -1; }
    void *map = mmap(NULL, st.st_size, PROT_READ, MAP_PRIVATE, fd, 0);
    if (map == MAP_FAILED) { set_error("mmap %s: %s", path, strerror(errno)); close(fd); return -1; }
    const Elf64_Ehdr *eh = (const Elf64_Ehdr *)map;
    if (memcmp(eh->e_ident, "\x7f""ELF", 4) != 0 || eh->e_ident[4] != 2) {
        set_error("%s: not ELF64", path);
        munmap(map, st.st_size); close(fd); return -1;
    }
    if (eh->e_machine != 0x3e) {
        set_error("%s: not x86_64", path);
        munmap(map, st.st_size); close(fd); return -1;
    }
    const Elf64_Phdr *ph = (const Elf64_Phdr *)((u8 *)map + eh->e_phoff);
    u64 lo = ~(u64)0, hi = 0;
    int n_load = 0;
    for (u16 i = 0; i < eh->e_phnum; i++) {
        if (ph[i].p_type == PT_LOAD) {
            u64 end = ph[i].p_vaddr + ph[i].p_memsz;
            if (ph[i].p_vaddr < lo) lo = ph[i].p_vaddr;
            if (end > hi) hi = end;
            n_load++;
        }
    }
    if (n_load == 0) {
        set_error("%s: no PT_LOAD", path);
        munmap(map, st.st_size); close(fd); return -1;
    }
    size_t total = hi - lo;
    size_t pagesize = 4096;
    size_t mapsz = (total + pagesize - 1) & ~(pagesize - 1);
    void *base = mmap(NULL, mapsz, PROT_NONE, MAP_PRIVATE | MAP_ANONYMOUS | MAP_NORESERVE, -1, 0);
    if (base == MAP_FAILED) {
        set_error("mmap base: %s", strerror(errno));
        munmap(map, st.st_size); close(fd); return -1;
    }
    LOG("loading %s at %p size 0x%zx lo=0x%llx hi=0x%llx n_load=%d",
        path, base, mapsz, (unsigned long long)lo, (unsigned long long)hi, n_load);
    for (u16 i = 0; i < eh->e_phnum; i++) {
        if (ph[i].p_type != PT_LOAD) continue;
        u64 vaddr = ph[i].p_vaddr;
        u64 memsz = ph[i].p_memsz;
        u64 filesz = ph[i].p_filesz;
        u64 foff = ph[i].p_offset;
        u64 seg_lo = vaddr - lo;
        u64 seg_hi = seg_lo + memsz;
        u64 page_lo = seg_lo & ~(pagesize - 1);
        u64 page_hi = (seg_hi + pagesize - 1) & ~(pagesize - 1);
        size_t seg_pagesz = page_hi - page_lo;
        int prot = 0;
        if (ph[i].p_flags & PF_R) prot |= PROT_READ;
        if (ph[i].p_flags & PF_W) prot |= PROT_WRITE;
        if (ph[i].p_flags & PF_X) prot |= PROT_EXEC;
        int writable = prot | PROT_WRITE;
        u8 *pdst = (u8 *)base + page_lo;
        if (getenv("NOVA_BIONIC_TRACE")) fprintf(stderr, "[bionic_loader]   seg %d: vaddr=0x%llx seg_lo=0x%llx seg_hi=0x%llx page_lo=0x%llx page_hi=0x%llx seg_pagesz=0x%zx prot=%d\n", i, (unsigned long long)vaddr, (unsigned long long)seg_lo, (unsigned long long)seg_hi, (unsigned long long)page_lo, (unsigned long long)page_hi, seg_pagesz, prot);
        if (mprotect(pdst, seg_pagesz, writable) < 0) {
            set_error("mprotect(W) %s phdr[%d]: %s", path, i, strerror(errno));
            munmap(base, mapsz); munmap(map, st.st_size); close(fd); return -1;
        }
        if (filesz > 0) memcpy((u8 *)base + seg_lo, (u8 *)map + foff, filesz);
        if (memsz > filesz) memset((u8 *)base + seg_lo + filesz, 0, memsz - filesz);
        if (mprotect(pdst, seg_pagesz, prot) < 0) {
            set_error("mprotect(F) %s phdr[%d]: %s", path, i, strerror(errno));
            munmap(base, mapsz); munmap(map, st.st_size); close(fd); return -1;
        }
    }
    if (getenv("NOVA_BIONIC_TRACE")) fprintf(stderr, "[bionic_loader]   all PT_LOADs done, parsing PT_DYNAMIC\n");
    /* Save what we need from eh/ph BEFORE releasing the file mapping. */
    u64 dyn_vaddr = 0;
    int found_dyn = 0;
    for (u16 i = 0; i < eh->e_phnum; i++) {
        if (ph[i].p_type == PT_DYNAMIC) { dyn_vaddr = ph[i].p_vaddr; found_dyn = 1; break; }
    }
    /* Try to find SHT_DYNSYM section to get symbol count */
    size_t dynsym_entries = 0;
    if (eh->e_shoff != 0 && eh->e_shnum > 0) {
        const Elf64_Shdr *sh = (const Elf64_Shdr *)((u8 *)eh + eh->e_shoff);
        for (u16 i = 0; i < eh->e_shnum; i++) {
            if (sh[i].sh_type == SHT_DYNSYM && sh[i].sh_entsize == sizeof(Elf64_Sym)) {
                dynsym_entries = sh[i].sh_size / sh[i].sh_entsize;
                break;
            }
        }
    }
    munmap(map, st.st_size);
    if (!found_dyn) {
        set_error("%s: no PT_DYNAMIC", path);
        munmap(base, mapsz); close(fd); return -1;
    }
    Elf64_Dyn *dyn = (Elf64_Dyn *)(base + (dyn_vaddr - lo));

    so->path = strdup(path);
    so->fd = fd;
    so->base = base;
    so->size = mapsz;
    so->syment = dyn_get(dyn, DT_SYMENT, sizeof(Elf64_Sym));
    u64 strtab = dyn_get(dyn, DT_STRTAB, 0);
    so->strtab = strtab ? (u8 *)(base + (strtab - lo)) : NULL;
    so->strsz = dyn_get(dyn, DT_STRSZ, 0);
    u64 symtab = dyn_get(dyn, DT_SYMTAB, 0);
    so->symtab = symtab ? (Elf64_Sym *)(base + (symtab - lo)) : NULL;
    u64 hash_addr = dyn_get(dyn, DT_HASH, 0);
    if (hash_addr) {
        u32 *h = (u32 *)(base + (hash_addr - lo));
        so->nbucket = h[0];
        so->nchain  = h[1];
        so->buckets = h + 2;
        so->chains  = h + 2 + so->nbucket;
        so->hash = h;
    }
    u64 gh_addr = dyn_get(dyn, DT_GNU_HASH, 0);
    if (gh_addr) {
        u32 *gh = (u32 *)(base + (gh_addr - lo));
        so->gnu_nbucket = gh[0];
        so->gnu_symndx = gh[1];
        so->gnu_maskwords = gh[2];
        so->gnu_bloom_shift = gh[3];
        so->gnu_bloom = gh + 4;
        so->gnu_buckets = gh + 4 + so->gnu_maskwords;
        so->gnu_chains = gh + 4 + so->gnu_maskwords + so->gnu_nbucket;
    }
    u64 rela_addr = dyn_get(dyn, DT_RELA, 0);
    so->relasz = dyn_get(dyn, DT_RELASZ, 0);
    so->relaent = dyn_get(dyn, DT_RELAENT, sizeof(Elf64_Rela));
    so->rela = rela_addr ? (Elf64_Rela *)(base + (rela_addr - lo)) : NULL;
    so->relacount = dyn_get(dyn, DT_RELACOUNT, 0);
    u64 jmprel_addr = dyn_get(dyn, DT_JMPREL, 0);
    size_t jmprelsz = dyn_get(dyn, DT_PLTRELSZ, 0);
    so->jmprela = (jmprel_addr && jmprelsz) ? (Elf64_Rela *)(base + (jmprel_addr - lo)) : NULL;
    so->jmprelsz = jmprelsz;
    so->n_needed = 0;
    for (Elf64_Dyn *d = dyn; d->d_tag != DT_NULL; d++) {
        if (d->d_tag == DT_NEEDED) {
            if (so->n_needed >= MAX_NEEDED) break;
            const char *n = (const char *)so->strtab + d->d_val;
            so->needed_names[so->n_needed++] = strdup(n);
        }
    }
    so->nsym = dynsym_entries;
    LOG("  strtab=%p symtab=%p nsym=%zu nbucket=%d nchain=%d gnu_nbucket=%d relasz=0x%zx n_needed=%d",
        so->strtab, so->symtab, so->nsym, so->nbucket, so->nchain, so->gnu_nbucket, so->relasz, so->n_needed);
    return 0;
}

/* Resolve a symbol: first glibc, then all loaded bionic .so, then lazy-load from index. */
static void *resolve_symbol(const char *name) {
    static void *libc_handle = NULL;
    if (!libc_handle) libc_handle = dlopen("libc.so.6", RTLD_LAZY | RTLD_GLOBAL);
    if (libc_handle) {
        dlerror();
        void *s = dlsym(libc_handle, name);
        if (s) return s;
    }
    static void *libm_handle = NULL;
    if (!libm_handle) libm_handle = dlopen("libm.so.6", RTLD_LAZY | RTLD_GLOBAL);
    if (libm_handle) {
        dlerror();
        void *s = dlsym(libm_handle, name);
        if (s) return s;
    }
    for (bionic_so_t *p = g_loaded_head; p; p = p->next) {
        const Elf64_Sym *sym = sym_lookup(p, name);
        if (sym && sym->st_shndx != 0) {
            return (void *)(uptr)(p->base + sym->st_value);
        }
    }
    /* B.0.b.2 lazy loader: build a global symbol index on first miss and use it
       to find a .so we haven't loaded yet that exports the symbol. */
    build_sym_index();
    const char *path = sym_idx_lookup(name);
    if (path) {
        LOG("[bionic_loader] lazy-loading %s for %s", path, name);
        if (bionic_dlopen(path, 0)) {
            for (bionic_so_t *p = g_loaded_head; p; p = p->next) {
                const Elf64_Sym *sym = sym_lookup(p, name);
                if (sym && sym->st_shndx != 0) {
                    return (void *)(uptr)(p->base + sym->st_value);
                }
            }
        }
    }
    return NULL;
}

static int apply_rela(bionic_so_t *so, const Elf64_Rela *rela) {
    u32 type = ELF64_R_TYPE(rela->r_info);
    u32 symi = ELF64_R_SYM(rela->r_info);
    const char *name = "";
    if (symi > 0 && so->symtab) {
        const Elf64_Sym *s = &so->symtab[symi];
        if (s->st_name && so->strtab) name = (const char *)so->strtab + s->st_name;
    }
    char clean[256];
    strncpy(clean, name, sizeof(clean) - 1);
    clean[sizeof(clean) - 1] = '\0';
    char *at = strchr(clean, '@');
    if (at) *at = '\0';
    u64 *p = (u64 *)(so->base + rela->r_offset);
    switch (type) {
        case R_X86_64_NONE: break;
        case R_X86_64_RELATIVE: *p = (u64)so->base + rela->r_addend; break;
        case R_X86_64_GLOB_DAT:
        case R_X86_64_JUMP_SLOT:
            if (clean[0]) {
                void *symval = resolve_symbol(clean);
                if (symval) *p = (u64)symval;
                else { LOG("  unresolved: %s in %s (zeroed)", clean, so->path); *p = 0; }
            } else *p = 0;
            break;
        case R_X86_64_64:
            if (clean[0]) {
                void *symval = resolve_symbol(clean);
                if (symval) *p = (u64)symval + rela->r_addend;
                else { LOG("  unresolved R_64: %s (zeroed)", clean); *p = 0; }
            } else *p = (u64)so->base + rela->r_addend;
            break;
        case R_X86_64_IRELATIVE: {
            /* ifunc: call the resolver at base+addend, store the result */
            u64 (*resolver)(void) = (u64 (*)(void))(uptr)(so->base + rela->r_addend);
            *p = resolver();
            break;
        }
        case 16: /* R_X86_64_DTPMOD64 */
        case 17: /* R_X86_64_DTPOFF64 */
        case 18: /* R_X86_64_TPOFF64 */
        case 19: /* R_X86_64_TPREL64 */
            /* TLS relocations - stub for now: zero out */
            LOG("  TLS relocation type %d at offset 0x%llx (zeroed, TLS not yet supported)", type, (unsigned long long)rela->r_offset);
            *p = 0;
            break;
        default:
            LOG("  unhandled relocation type %d at offset 0x%llx (zeroed)", type, (unsigned long long)rela->r_offset);
            *p = 0;
            break;
    }
    return 0;
}

static int apply_relocations(bionic_so_t *so) {
    if (so->rela && so->relasz) {
        size_t n = so->relasz / so->relaent;
        size_t skip_to = (so->relacount && so->relacount < n) ? so->relacount : n;
        LOG("  applying %zu/%zu .rela.dyn relocations in %s (relacount=%zu)",
            skip_to, n, so->path, so->relacount);
        for (size_t i = 0; i < skip_to; i++) {
            apply_rela(so, &so->rela[i]);
            if ((i & 0xff) == 0 && i > 0) LOG("    [progress] %zu/%zu .rela.dyn", i, skip_to);
        }
    }
    if (so->jmprela && so->jmprelsz) {
        size_t n = so->jmprelsz / sizeof(Elf64_Rela);
        LOG("  applying %zu .rela.plt relocations in %s", n, so->path);
        for (size_t i = 0; i < n; i++) {
            apply_rela(so, &so->jmprela[i]);
            if ((i & 0xff) == 0 && i > 0) LOG("    [progress] %zu/%zu .rela.plt", i, n);
        }
    }
    return 0;
}

static char *find_in_path(const char *name) {
    char tmp[4096];
    const char *p = g_lib_path;
    while (*p) {
        const char *e = strchr(p, ':');
        size_t n = e ? (size_t)(e - p) : strlen(p);
        if (n >= sizeof(tmp) - 256) n = sizeof(tmp) - 256;
        memcpy(tmp, p, n);
        tmp[n] = '/';
        size_t name_len = strlen(name);
        if (n + 1 + name_len >= sizeof(tmp)) { if (!e) break; p = e + 1; continue; }
        memcpy(tmp + n + 1, name, name_len);
        tmp[n + 1 + name_len] = '\0';
        if (access(tmp, R_OK) == 0) return strdup(tmp);
        if (!e) break;
        p = e + 1;
    }
    return NULL;
}

static int load_needed(const char *name);

static int load_needed(const char *name) {
    for (bionic_so_t *p = g_loaded_head; p; p = p->next) {
        const char *bn = strrchr(p->path, '/');
        bn = bn ? bn + 1 : p->path;
        if (strcmp(bn, name) == 0) { p->refcount++; return 0; }
    }
    /* Keep @version in the DT_NEEDED name (HIDL vendor .so files have
       android.hidl.X@1.0.so style names). Strip only when no .so with
       the full name is found. */
    char *p = find_in_path(name);
    if (!p) {
        char clean[256];
        strncpy(clean, name, sizeof(clean) - 1);
        clean[sizeof(clean) - 1] = '\0';
        char *at = strchr(clean, '@');
        if (at) *at = '\0';
        if (strcmp(clean, name) != 0) p = find_in_path(clean);
        if (!p) { LOG("DT_NEEDED not found: %s (clean=%s)", name, clean); return -1; }
    }
    bionic_so_t *so = calloc(1, sizeof(*so));
    if (load_so_file(p, so) < 0) { free(so); free(p); return -1; }
    so->refcount = 1;
    so->next = g_loaded_head;
    g_loaded_head = so;
    for (int i = 0; i < so->n_needed; i++) load_needed(so->needed_names[i]);
    apply_relocations(so);
    return 0;
}

void *bionic_dlopen(const char *path, int flags) {
    (void)flags;
    if (!path) return NULL;
    char *abs = path[0] == '/' ? strdup(path) : find_in_path(path);
    if (!abs) { set_error("not found: %s", path); return NULL; }
    for (bionic_so_t *p = g_loaded_head; p; p = p->next) {
        if (strcmp(p->path, abs) == 0) { p->refcount++; free(abs); return p; }
    }
    bionic_so_t *so = calloc(1, sizeof(*so));
    if (load_so_file(abs, so) < 0) { free(so); free(abs); return NULL; }
    so->refcount = 1;
    so->next = g_loaded_head;
    g_loaded_head = so;
    for (int i = 0; i < so->n_needed; i++) load_needed(so->needed_names[i]);
    apply_relocations(so);
    free(abs);
    return so;
}

void *bionic_dlsym(void *handle, const char *name) {
    if (!handle || !name) return NULL;
    bionic_so_t *so = (bionic_so_t *)handle;
    const Elf64_Sym *sym = sym_lookup(so, name);
    if (sym && sym->st_shndx != 0) {
        return (void *)(uptr)(so->base + sym->st_value);
    }
    return NULL;
}

int bionic_dlclose(void *handle) {
    if (!handle) return -1;
    bionic_so_t *so = (bionic_so_t *)handle;
    if (--so->refcount > 0) return 0;
    bionic_so_t **pp = &g_loaded_head;
    while (*pp) { if (*pp == so) { *pp = so->next; break; } pp = &(*pp)->next; }
    munmap(so->base, so->size);
    if (so->fd >= 0) close(so->fd);
    for (int i = 0; i < so->n_needed; i++) free(so->needed_names[i]);
    free(so->path);
    free(so);
    return 0;
}
