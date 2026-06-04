#ifndef NOVA_BIONIC_ELF_H
#define NOVA_BIONIC_ELF_H

#include <stdint.h>
#include <stddef.h>

typedef uint8_t   u8;
typedef uint16_t  u16;
typedef uint32_t  u32;
typedef uint64_t  u64;
typedef int32_t   i32;
typedef int64_t   i64;
typedef uintptr_t uptr;

#define EI_NIDENT 16

/* ELF64 header */
typedef struct {
    u8  e_ident[EI_NIDENT];
    u16 e_type;
    u16 e_machine;
    u32 e_version;
    u64 e_entry;
    u64 e_phoff;
    u64 e_shoff;
    u32 e_flags;
    u16 e_ehsize;
    u16 e_phentsize;
    u16 e_phnum;
    u16 e_shentsize;
    u16 e_shnum;
    u16 e_shstrndx;
} Elf64_Ehdr;

/* ELF64 section header */
typedef struct {
    u32 sh_name;
    u32 sh_type;
    u64 sh_flags;
    u64 sh_addr;
    u64 sh_offset;
    u64 sh_size;
    u32 sh_link;
    u32 sh_info;
    u64 sh_addralign;
    u64 sh_entsize;
} Elf64_Shdr;

#define SHT_SYMTAB  2
#define SHT_DYNSYM  11

/* ELF64 program header */
typedef struct {
    u32 p_type;
    u32 p_flags;
    u64 p_offset;
    u64 p_vaddr;
    u64 p_paddr;
    u64 p_filesz;
    u64 p_memsz;
    u64 p_align;
} Elf64_Phdr;

/* ELF64 dynamic entry */
typedef struct {
    i64 d_tag;
    u64 d_val;
} Elf64_Dyn;

/* ELF64 symbol table entry */
typedef struct {
    u32 st_name;
    u8  st_info;
    u8  st_other;
    u16 st_shndx;
    u64 st_value;
    u64 st_size;
} Elf64_Sym;

/* ELF64 rela entry (with explicit addend) */
typedef struct {
    u64 r_offset;
    u64 r_info;
    i64 r_addend;
} Elf64_Rela;

/* ELF64 rel entry (no addend, addend is at r_offset) */
typedef struct {
    u64 r_offset;
    u64 r_info;
} Elf64_Rel;

/* p_type */
#define PT_NULL         0
#define PT_LOAD         1
#define PT_DYNAMIC      2
#define PT_INTERP       3
#define PT_NOTE         4
#define PT_SHLIB        5
#define PT_PHDR         6
#define PT_TLS          7

/* p_flags */
#define PF_X            0x1
#define PF_W            0x2
#define PF_R            0x4

/* d_tag */
#define DT_NULL         0
#define DT_NEEDED       1
#define DT_PLTRELSZ     2
#define DT_PLTGOT       3
#define DT_HASH         4
#define DT_STRTAB       5
#define DT_SYMTAB       6
#define DT_RELA         7
#define DT_RELASZ       8
#define DT_RELAENT      9
#define DT_RELACOUNT    0x6ffffff9
#define DT_STRSZ        10
#define DT_SYMENT       11
#define DT_INIT         12
#define DT_FINI         13
#define DT_SONAME       14
#define DT_RPATH        15
#define DT_SYMBOLIC     16
#define DT_REL          17
#define DT_RELSZ        18
#define DT_RELENT       19
#define DT_DEBUG        21
#define DT_TEXTREL      22
#define DT_PLTREL       23
#define DT_JMPREL       23  /* note: actual value is 23 too (collision) */
#define DT_BIND_NOW     24
#define DT_INIT_ARRAY   25
#define DT_FINI_ARRAY   26
#define DT_INIT_ARRAYSZ 27
#define DT_FINI_ARRAYSZ 28
#define DT_GNU_HASH     0x6ffffef5
#define DT_VERSYM       0x6ffffff0
#define DT_FLAGS_1      0x6ffffffb
#define DT_VERDEF       0x6ffffffc
#define DT_VERDEFNUM    0x6ffffffd
#define DT_VERNEED      0x6ffffffe
#define DT_VERNEEDNUM   0x6fffffff

/* r_info: type */
#define R_X86_64_NONE       0
#define R_X86_64_64         1
#define R_X86_64_GLOB_DAT   6
#define R_X86_64_JUMP_SLOT  7
#define R_X86_64_RELATIVE   8
#define R_X86_64_IRELATIVE  37

#define ELF64_R_TYPE(i)     ((u32)(i))
#define ELF64_R_SYM(i)      ((u32)((i) >> 32))
#define ELF64_R_INFO(s,t)   (((u64)(s) << 32) | (u32)(t))

/* st_info */
#define ELF64_ST_BIND(i)    ((i) >> 4)
#define ELF64_ST_TYPE(i)    ((i) & 0xf)
#define ELF64_ST_INFO(b,t)  (((b) << 4) | ((t) & 0xf))

#define STB_LOCAL          0
#define STB_GLOBAL         1
#define STB_WEAK           2
#define STT_NOTYPE         0
#define STT_OBJECT         1
#define STT_FUNC           2

#endif
