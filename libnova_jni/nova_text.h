#ifndef NOVA_TEXT_H
#define NOVA_TEXT_H

#include <stdint.h>

struct FT_LibraryRec_;
struct FT_FaceRec_;

struct nova_text_state {
    struct FT_LibraryRec_ *library;
    struct FT_FaceRec_ *face;
    int initialized;
};

int nova_text_init(struct nova_text_state *state);
void nova_text_shutdown(struct nova_text_state *state);
int nova_text_load_font(struct nova_text_state *state, const char *path, float size);
void nova_text_render_to_bitmap(uint32_t *pixels, int width, int height,
                                struct nova_text_state *state,
                                const char *text, int x, int y,
                                uint32_t color, float size);
float nova_text_measure(struct nova_text_state *state, const char *text, float size);

#endif
