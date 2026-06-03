#include "nova_text.h"

#include <ft2build.h>
#include FT_FREETYPE_H
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>

int nova_text_init(struct nova_text_state *state) {
    if (!state) return -1;
    memset(state, 0, sizeof(*state));
    if (FT_Init_FreeType(&state->library) != 0) {
        fprintf(stderr, "nova_text: Failed to init FreeType\n");
        return -1;
    }
    state->initialized = 1;
    return 0;
}

void nova_text_shutdown(struct nova_text_state *state) {
    if (!state) return;
    if (state->face) FT_Done_Face(state->face);
    if (state->library) FT_Done_FreeType(state->library);
    memset(state, 0, sizeof(*state));
}

int nova_text_load_font(struct nova_text_state *state, const char *path, float size) {
    if (!state || !state->initialized || !path) return -1;
    if (state->face) { FT_Done_Face(state->face); state->face = NULL; }
    if (FT_New_Face(state->library, path, 0, &state->face) != 0) {
        fprintf(stderr, "nova_text: Failed to load font: %s\n", path);
        return -1;
    }
    FT_Set_Pixel_Sizes(state->face, 0, (int)size);
    return 0;
}

void nova_text_render_to_bitmap(uint32_t *pixels, int width, int height,
                                struct nova_text_state *state,
                                const char *text, int x, int y,
                                uint32_t color, float size) {
    if (!pixels || !state || !state->face || !text) return;

    FT_Set_Pixel_Sizes(state->face, 0, (int)size);
    int pen_x = x;

    for (const char *p = text; *p; p++) {
        FT_UInt glyph_idx = FT_Get_Char_Index(state->face, (FT_ULong)(unsigned char)*p);
        if (glyph_idx == 0) continue;

        if (FT_Load_Glyph(state->face, glyph_idx, FT_LOAD_RENDER) != 0) continue;
        if (FT_Render_Glyph(state->face->glyph, FT_RENDER_MODE_NORMAL) != 0) continue;

        FT_Bitmap *glyph = &state->face->glyph->bitmap;
        int gx = pen_x + state->face->glyph->bitmap_left;
        int gy = y - state->face->glyph->bitmap_top;

        for (unsigned int row = 0; row < glyph->rows; row++) {
            for (unsigned int col = 0; col < glyph->width; col++) {
                int px = gx + col;
                int py = gy + row;
                if (px < 0 || px >= width || py < 0 || py >= height) continue;
                unsigned char alpha = glyph->buffer[row * glyph->pitch + col];
                if (alpha == 0) continue;
                uint32_t src_r = (color >> 16) & 0xFF;
                uint32_t src_g = (color >> 8) & 0xFF;
                uint32_t src_b = color & 0xFF;
                uint32_t dst = pixels[py * width + px];
                uint32_t dst_r = (dst >> 16) & 0xFF;
                uint32_t dst_g = (dst >> 8) & 0xFF;
                uint32_t dst_b = dst & 0xFF;
                uint32_t a = alpha;
                uint32_t inv_a = 255 - a;
                uint32_t out_r = (src_r * a + dst_r * inv_a) / 255;
                uint32_t out_g = (src_g * a + dst_g * inv_a) / 255;
                uint32_t out_b = (src_b * a + dst_b * inv_a) / 255;
                pixels[py * width + px] = (0xFF << 24) | (out_r << 16) | (out_g << 8) | out_b;
            }
        }
        pen_x += state->face->glyph->advance.x >> 6;
    }
}

float nova_text_measure(struct nova_text_state *state, const char *text, float size) {
    if (!state || !state->face || !text) return 0;
    FT_Set_Pixel_Sizes(state->face, 0, (int)size);
    float total = 0;
    for (const char *p = text; *p; p++) {
        FT_UInt glyph_idx = FT_Get_Char_Index(state->face, (FT_ULong)(unsigned char)*p);
        if (glyph_idx == 0) continue;
        if (FT_Load_Glyph(state->face, glyph_idx, FT_LOAD_DEFAULT) != 0) continue;
        total += state->face->glyph->advance.x >> 6;
    }
    return total;
}
