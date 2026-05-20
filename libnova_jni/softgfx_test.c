#include "softgfx.h"

#include <stdint.h>
#include <stdio.h>

/*
 * Forward declare the clipping API the Java Canvas bridge needs.
 * This test intentionally fails to link until the implementation exists.
 */
void nova_canvas_clip_rect(struct nova_canvas *canvas, float left, float top,
                           float right, float bottom);

static int expect_pixel(uint32_t actual, uint32_t expected, const char *label) {
    if (actual != expected) {
        fprintf(stderr, "[FAIL] %s expected 0x%08x got 0x%08x\n", label, expected, actual);
        return 1;
    }
    return 0;
}

int main(void) {
    struct nova_bitmap *dst = nova_bitmap_create(8, 4, 1, 1);
    struct nova_bitmap *src = nova_bitmap_create(8, 4, 1, 1);
    struct nova_canvas *canvas;
    uint32_t *pixels;
    int failures = 0;

    if (!dst || !src) {
        fprintf(stderr, "[FAIL] bitmap allocation failed\n");
        return 1;
    }

    canvas = nova_canvas_create(dst);
    if (!canvas) {
        fprintf(stderr, "[FAIL] canvas allocation failed\n");
        return 1;
    }

    nova_bitmap_clear(dst, 0xffffffffu);
    nova_bitmap_clear(src, 0xff00ff00u);

    nova_canvas_save(canvas, 0);
    nova_canvas_translate(canvas, 4.0f, 0.0f);
    nova_canvas_clip_rect(canvas, 0.0f, 0.0f, 4.0f, 4.0f);
    nova_canvas_blit_bitmap(canvas, src, -2, 0);
    nova_canvas_restore(canvas);

    pixels = nova_bitmap_pixels(dst);
    failures += expect_pixel(pixels[2], 0xffffffffu, "pixel[2]");
    failures += expect_pixel(pixels[3], 0xffffffffu, "pixel[3]");
    failures += expect_pixel(pixels[4], 0xff00ff00u, "pixel[4]");
    failures += expect_pixel(pixels[7], 0xff00ff00u, "pixel[7]");

    nova_canvas_destroy(canvas);
    nova_bitmap_destroy(src);
    nova_bitmap_destroy(dst);

    if (failures != 0) {
        return 1;
    }

    printf("[PASS] nova softgfx clipping\n");
    return 0;
}
