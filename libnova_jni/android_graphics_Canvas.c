#include "core_jni_helpers.h"
#include "softgfx.h"
#include <stddef.h>

static const uint8_t kFont5x7Digits[10][7] = {
    {0x0e,0x11,0x13,0x15,0x19,0x11,0x0e}, /* 0 */
    {0x04,0x0c,0x04,0x04,0x04,0x04,0x0e}, /* 1 */
    {0x0e,0x11,0x01,0x02,0x04,0x08,0x1f}, /* 2 */
    {0x1e,0x01,0x01,0x0e,0x01,0x01,0x1e}, /* 3 */
    {0x02,0x06,0x0a,0x12,0x1f,0x02,0x02}, /* 4 */
    {0x1f,0x10,0x10,0x1e,0x01,0x01,0x1e}, /* 5 */
    {0x0e,0x10,0x10,0x1e,0x11,0x11,0x0e}, /* 6 */
    {0x1f,0x01,0x02,0x04,0x08,0x08,0x08}, /* 7 */
    {0x0e,0x11,0x11,0x0e,0x11,0x11,0x0e}, /* 8 */
    {0x0e,0x11,0x11,0x0f,0x01,0x01,0x0e}, /* 9 */
};

static const uint8_t kFont5x7Upper[26][7] = {
    {0x04,0x0a,0x11,0x11,0x1f,0x11,0x11}, /* A */
    {0x1e,0x11,0x11,0x1e,0x11,0x11,0x1e}, /* B */
    {0x0e,0x11,0x10,0x10,0x10,0x11,0x0e}, /* C */
    {0x1c,0x12,0x11,0x11,0x11,0x12,0x1c}, /* D */
    {0x1f,0x10,0x10,0x1e,0x10,0x10,0x1f}, /* E */
    {0x1f,0x10,0x10,0x1e,0x10,0x10,0x10}, /* F */
    {0x0e,0x11,0x10,0x17,0x11,0x11,0x0f}, /* G */
    {0x11,0x11,0x11,0x1f,0x11,0x11,0x11}, /* H */
    {0x0e,0x04,0x04,0x04,0x04,0x04,0x0e}, /* I */
    {0x07,0x02,0x02,0x02,0x12,0x12,0x0c}, /* J */
    {0x11,0x12,0x14,0x18,0x14,0x12,0x11}, /* K */
    {0x10,0x10,0x10,0x10,0x10,0x10,0x1f}, /* L */
    {0x11,0x1b,0x15,0x15,0x11,0x11,0x11}, /* M */
    {0x11,0x11,0x19,0x15,0x13,0x11,0x11}, /* N */
    {0x0e,0x11,0x11,0x11,0x11,0x11,0x0e}, /* O */
    {0x1e,0x11,0x11,0x1e,0x10,0x10,0x10}, /* P */
    {0x0e,0x11,0x11,0x11,0x15,0x12,0x0d}, /* Q */
    {0x1e,0x11,0x11,0x1e,0x14,0x12,0x11}, /* R */
    {0x0f,0x10,0x10,0x0e,0x01,0x01,0x1e}, /* S */
    {0x1f,0x04,0x04,0x04,0x04,0x04,0x04}, /* T */
    {0x11,0x11,0x11,0x11,0x11,0x11,0x0e}, /* U */
    {0x11,0x11,0x11,0x11,0x11,0x0a,0x04}, /* V */
    {0x11,0x11,0x11,0x15,0x15,0x15,0x0a}, /* W */
    {0x11,0x11,0x0a,0x04,0x0a,0x11,0x11}, /* X */
    {0x11,0x11,0x0a,0x04,0x04,0x04,0x04}, /* Y */
    {0x1f,0x01,0x02,0x04,0x08,0x10,0x1f}, /* Z */
};

static void draw_pixel_rect(struct nova_canvas *canvas, struct nova_paint *paint,
                            int x, int y, int scale) {
    nova_canvas_draw_rect(canvas, (float)x, (float)y, (float)(x + scale), (float)(y + scale), paint);
}

static void draw_glyph(struct nova_canvas *canvas, struct nova_paint *paint,
                       const uint8_t rows[7], int x, int y, int scale) {
    for (int row = 0; row < 7; row++) {
        for (int col = 0; col < 5; col++) {
            if (rows[row] & (1 << (4 - col))) {
                draw_pixel_rect(canvas, paint, x + col * scale, y + row * scale, scale);
            }
        }
    }
}

static jlong initRaster(JNIEnv *env, jobject, jlong bitmapHandle) {
    (void)env;
    return (jlong)(intptr_t)nova_canvas_create((struct nova_bitmap *)(intptr_t)bitmapHandle);
}

static void native_setBitmap(JNIEnv *env, jobject, jlong canvasHandle, jlong bitmapHandle) {
    (void)env;
    nova_canvas_set_bitmap((struct nova_canvas *)(intptr_t)canvasHandle,
                           (struct nova_bitmap *)(intptr_t)bitmapHandle);
}

static void native_drawRect(JNIEnv *env, jobject, jlong canvasHandle,
                             jfloat left, jfloat top, jfloat right, jfloat bottom,
                             jlong paintHandle) {
    (void)env;
    nova_canvas_draw_rect((struct nova_canvas *)(intptr_t)canvasHandle,
                          left, top, right, bottom,
                          (struct nova_paint *)(intptr_t)paintHandle);
}

static void native_drawColor(JNIEnv *env, jobject, jlong canvasHandle, jint color) {
    (void)env;
    nova_canvas_draw_color((struct nova_canvas *)(intptr_t)canvasHandle, (uint32_t)color);
}

static void native_drawText(JNIEnv *env, jobject, jlong canvasHandle, jstring text, jfloat x, jfloat y, jlong paintHandle) {
    struct nova_canvas *canvas = (struct nova_canvas *)(intptr_t)canvasHandle;
    const struct nova_paint *paint = (const struct nova_paint *)(intptr_t)paintHandle;
    struct nova_paint *glyphPaint;
    const char *chars;
    int pen_x;
    int pen_y;

    if (!env || !canvas || !text) {
        return;
    }

    chars = (*env)->GetStringUTFChars(env, text, NULL);
    if (!chars) {
        return;
    }

    glyphPaint = nova_paint_clone(paint);
    if (!glyphPaint) {
        glyphPaint = nova_paint_create();
    }

    pen_x = (int)x;
    pen_y = (int)y - 14;
    if (pen_y < 0) {
        pen_y = 0;
    }

    for (const char *p = chars; *p; p++) {
        unsigned char ch = (unsigned char)*p;
        uint32_t color = paint ? nova_paint_get_color(paint) : 0xff000000u;
        nova_paint_set_color(glyphPaint, color);
        if (ch >= 'a' && ch <= 'z') {
            ch = (unsigned char)(ch - ('a' - 'A'));
        }

        if (ch >= '0' && ch <= '9') {
            draw_glyph(canvas, glyphPaint, kFont5x7Digits[ch - '0'], pen_x, pen_y, 2);
        } else if (ch >= 'A' && ch <= 'Z') {
            draw_glyph(canvas, glyphPaint, kFont5x7Upper[ch - 'A'], pen_x, pen_y, 2);
        } else if (ch == ' ') {
            /* no-op */
        } else if (ch == '.') {
            draw_pixel_rect(canvas, glyphPaint, pen_x + 4, pen_y + 12, 2);
        } else if (ch == '-') {
            for (int dx = 0; dx < 8; dx += 2) {
                draw_pixel_rect(canvas, glyphPaint, pen_x + dx, pen_y + 6, 2);
            }
        } else {
            static const uint8_t unknown[7] = {0x1f,0x11,0x01,0x02,0x04,0x00,0x04};
            draw_glyph(canvas, glyphPaint, unknown, pen_x, pen_y, 2);
        }
        pen_x += 12;
    }

    nova_paint_destroy(glyphPaint);
    (*env)->ReleaseStringUTFChars(env, text, chars);
}

static void native_drawBitmap(JNIEnv *env, jobject, jlong canvasHandle, jlong bitmapHandle, jfloat left, jfloat top, jlong paintHandle) {
    (void)env;
    (void)paintHandle;
    nova_canvas_blit_bitmap((struct nova_canvas *)(intptr_t)canvasHandle,
                            (const struct nova_bitmap *)(intptr_t)bitmapHandle,
                            (int)left, (int)top);
}

static void native_drawCircle(JNIEnv *env, jobject, jlong canvasHandle, jfloat cx, jfloat cy, jfloat radius, jlong paintHandle) {
    (void)env;
    (void)canvasHandle;
    (void)cx;
    (void)cy;
    (void)radius;
    (void)paintHandle;
}

static void native_drawLine(JNIEnv *env, jobject, jlong canvasHandle, jfloat startX, jfloat startY, jfloat stopX, jfloat stopY, jlong paintHandle) {
    (void)env;
    (void)canvasHandle;
    (void)startX;
    (void)startY;
    (void)stopX;
    (void)stopY;
    (void)paintHandle;
}

static jint native_save(JNIEnv *env, jobject, jlong canvasHandle, jint saveFlags) {
    (void)env;
    return nova_canvas_save((struct nova_canvas *)(intptr_t)canvasHandle, saveFlags);
}

static void native_restore(JNIEnv *env, jobject, jlong canvasHandle) {
    (void)env;
    nova_canvas_restore((struct nova_canvas *)(intptr_t)canvasHandle);
}

static void native_translate(JNIEnv *env, jobject, jlong canvasHandle, jfloat dx, jfloat dy) {
    (void)env;
    nova_canvas_translate((struct nova_canvas *)(intptr_t)canvasHandle, dx, dy);
}

static jint native_getWidth(JNIEnv *env, jobject, jlong canvasHandle) {
    (void)env;
    return nova_canvas_width((struct nova_canvas *)(intptr_t)canvasHandle);
}

static jint native_getHeight(JNIEnv *env, jobject, jlong canvasHandle) {
    (void)env;
    return nova_canvas_height((struct nova_canvas *)(intptr_t)canvasHandle);
}

static const JNINativeMethod gMethods[] = {
    { "initRaster",    "(J)J",                         (void*)initRaster },
    { "native_setBitmap", "(JJ)V",                     (void*)native_setBitmap },
    { "native_drawRect",  "(JFFFFJ)V",                 (void*)native_drawRect },
    { "native_drawColor", "(JI)V",                     (void*)native_drawColor },
    { "native_drawText",  "(JLjava/lang/String;FFJ)V", (void*)native_drawText },
    { "native_drawBitmap", "(JJFFJ)V",                 (void*)native_drawBitmap },
    { "native_drawCircle", "(JFFFJ)V",                 (void*)native_drawCircle },
    { "native_drawLine", "(JFFFFJ)V",                  (void*)native_drawLine },
    { "native_save",      "(JI)I",                     (void*)native_save },
    { "native_restore",   "(J)V",                      (void*)native_restore },
    { "native_translate", "(JFF)V",                    (void*)native_translate },
    { "native_getWidth",  "(J)I",                      (void*)native_getWidth },
    { "native_getHeight", "(J)I",                      (void*)native_getHeight },
};

int register_android_graphics_Canvas(JNIEnv *env) {
    return RegisterMethodsOrDie(env, "android/graphics/Canvas",
                                 gMethods, sizeof(gMethods) / sizeof(gMethods[0]));
}
