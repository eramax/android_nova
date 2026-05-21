package android.graphics;

public class Canvas {
    public static final int ALL_SAVE_FLAG = 31;

    private long mNativeCanvasWrapper;

    public Canvas() {}

    public Canvas(Bitmap bitmap) {
        setBitmap(bitmap);
    }

    public void setBitmap(Bitmap bitmap) {
        long bitmapHandle = bitmap != null ? bitmap.getNativeInstance() : 0L;
        if (mNativeCanvasWrapper == 0L) {
            mNativeCanvasWrapper = initRaster(bitmapHandle);
        } else {
            native_setBitmap(mNativeCanvasWrapper, bitmapHandle);
        }
    }

    public void drawRect(float left, float top, float right, float bottom, Paint paint) {
        native_drawRect(mNativeCanvasWrapper, left, top, right, bottom,
                paint != null ? paint.getNativeInstance() : 0L);
    }

    public void drawRect(int left, int top, int right, int bottom, Paint paint) {
        drawRect((float) left, (float) top, (float) right, (float) bottom, paint);
    }

    public void drawRect(Rect rect, Paint paint) {
        if (rect != null) {
            drawRect((float) rect.left, (float) rect.top, (float) rect.right, (float) rect.bottom, paint);
        }
    }

    public int save() {
        return native_save(mNativeCanvasWrapper, ALL_SAVE_FLAG);
    }

    public int save(int saveFlags) {
        return native_save(mNativeCanvasWrapper, saveFlags);
    }

    public int saveLayer(RectF bounds, Paint paint, int saveFlags) {
        return native_save(mNativeCanvasWrapper, saveFlags);
    }

    public int saveLayer(RectF bounds, Paint paint) {
        return native_save(mNativeCanvasWrapper, ALL_SAVE_FLAG);
    }

    public int saveLayer(float left, float top, float right, float bottom, Paint paint, int saveFlags) {
        return native_save(mNativeCanvasWrapper, saveFlags);
    }

    public int saveLayer(float left, float top, float right, float bottom, Paint paint) {
        return native_save(mNativeCanvasWrapper, ALL_SAVE_FLAG);
    }

    public int saveLayerAlpha(RectF bounds, int alpha, int saveFlags) {
        return native_save(mNativeCanvasWrapper, saveFlags);
    }

    public int saveLayerAlpha(float left, float top, float right, float bottom, int alpha, int saveFlags) {
        return native_save(mNativeCanvasWrapper, saveFlags);
    }

    public int saveLayerAlpha(float left, float top, float right, float bottom, int alpha) {
        return native_save(mNativeCanvasWrapper, ALL_SAVE_FLAG);
    }

    public void restore() {
        native_restore(mNativeCanvasWrapper);
    }

    public void restoreToCount(int saveCount) {
        native_restore(mNativeCanvasWrapper);
    }

    public int getSaveCount() { return 1; }

    public void translate(float dx, float dy) {
        native_translate(mNativeCanvasWrapper, dx, dy);
    }

    public boolean clipRect(float left, float top, float right, float bottom) {
        return native_clipRect(mNativeCanvasWrapper, left, top, right, bottom);
    }

    public int getWidth() {
        return native_getWidth(mNativeCanvasWrapper);
    }

    public int getHeight() {
        return native_getHeight(mNativeCanvasWrapper);
    }

    public void drawColor(int color) {
        native_drawColor(mNativeCanvasWrapper, color);
    }

    public void drawColor(int color, PorterDuff.Mode mode) {
        drawColor(color);
    }

    public void drawColor(long color) {
        drawColor((int) color);
    }

    public void drawText(String text, float x, float y, Paint paint) {
        if (text != null) {
            native_drawText(mNativeCanvasWrapper, text, x, y,
                    paint != null ? paint.getNativeInstance() : 0L);
        }
    }

    public void drawBitmap(Bitmap bitmap, float left, float top, Paint paint) {
        if (bitmap != null) {
            native_drawBitmap(mNativeCanvasWrapper, bitmap.getNativeInstance(), left, top,
                    paint != null ? paint.getNativeInstance() : 0L);
        }
    }

    public void drawCircle(float cx, float cy, float radius, Paint paint) {
        native_drawCircle(mNativeCanvasWrapper, cx, cy, radius,
                paint != null ? paint.getNativeInstance() : 0L);
    }

    public void drawLine(float startX, float startY, float stopX, float stopY, Paint paint) {
        native_drawLine(mNativeCanvasWrapper, startX, startY, stopX, stopY,
                paint != null ? paint.getNativeInstance() : 0L);
    }

    private native long initRaster(long bitmapHandle);
    private native void native_setBitmap(long canvasHandle, long bitmapHandle);
    private native void native_drawRect(long canvasHandle, float left, float top, float right, float bottom, long paintHandle);
    private native void native_drawColor(long canvasHandle, int color);
    private native void native_drawText(long canvasHandle, String text, float x, float y, long paintHandle);
    private native void native_drawBitmap(long canvasHandle, long bitmapHandle, float left, float top, long paintHandle);
    private native void native_drawCircle(long canvasHandle, float cx, float cy, float radius, long paintHandle);
    private native void native_drawLine(long canvasHandle, float startX, float startY, float stopX, float stopY, long paintHandle);
    private native int native_save(long canvasHandle, int saveFlags);
    private native void native_restore(long canvasHandle);
    private native void native_translate(long canvasHandle, float dx, float dy);
    private native boolean native_clipRect(long canvasHandle, float left, float top, float right, float bottom);
    private native int native_getWidth(long canvasHandle);
    private native int native_getHeight(long canvasHandle);

    public void drawRoundRect(RectF rect, float rx, float ry, Paint paint) {}
    public void drawRoundRect(float left, float top, float right, float bottom, float rx, float ry, Paint paint) {}
    public void drawOval(RectF oval, Paint paint) {}
    public void drawOval(float left, float top, float right, float bottom, Paint paint) {}
    public void drawArc(RectF oval, float startAngle, float sweepAngle, boolean useCenter, Paint paint) {}
    public void drawArc(float left, float top, float right, float bottom, float startAngle, float sweepAngle, boolean useCenter, Paint paint) {}
    public void drawPath(Path path, Paint paint) {}
    public void drawPoints(float[] pts, Paint paint) {}
    public void drawPoints(float[] pts, int offset, int count, Paint paint) {}
    public void drawPoint(float x, float y, Paint paint) {}
    public void drawLines(float[] pts, Paint paint) {}
    public void drawLines(float[] pts, int offset, int count, Paint paint) {}
    public void drawText(CharSequence text, int start, int end, float x, float y, Paint paint) {}
    public void drawText(char[] text, int index, int count, float x, float y, Paint paint) {}
    public void drawText(String text, int start, int end, float x, float y, Paint paint) {}
    public void drawTextRun(CharSequence text, int start, int end, int contextStart, int contextEnd, float x, float y, boolean isRtl, Paint paint) {}
    public void drawTextRun(char[] text, int index, int count, int contextIndex, int contextCount, float x, float y, boolean isRtl, Paint paint) {}
    public void drawBitmap(Bitmap bitmap, Rect src, Rect dst, Paint paint) {}
    public void drawBitmap(Bitmap bitmap, Rect src, RectF dst, Paint paint) {}
    public void drawBitmap(Bitmap bitmap, Matrix matrix, Paint paint) {}
    public void drawBitmap(int[] colors, int offset, int stride, float x, float y, int width, int height, boolean hasAlpha, Paint paint) {}
    public void drawPicture(Picture picture) {}
    public void concat(Matrix matrix) {}
    public void rotate(float degrees) {}
    public void rotate(float degrees, float px, float py) {}
    public void scale(float sx, float sy) {}
    public void scale(float sx, float sy, float px, float py) {}
    public void skew(float sx, float sy) {}
    public boolean clipPath(Path path) { return true; }
    public boolean clipPath(Path path, Region.Op op) { return true; }
    public boolean clipRegion(Region region) { return true; }
    public boolean clipRegion(Region region, Region.Op op) { return true; }
    public boolean clipOutPath(Path path) { return true; }
    public boolean clipOutRect(RectF rect) { return true; }
    public boolean clipOutRect(float left, float top, float right, float bottom) { return true; }
    public boolean clipRect(RectF rect) { return true; }
    public boolean clipRect(Rect rect) { return true; }
    public boolean clipRect(int left, int top, int right, int bottom) { return true; }
    public void setMatrix(Matrix matrix) {}
    public void getMatrix(Matrix ctm) {}
    public Matrix getMatrix() { return new Matrix(); }
    public boolean quickReject(float left, float top, float right, float bottom, EdgeType type) { return false; }
    public boolean quickReject(int left, int top, int right, int bottom) { return false; }
    public boolean quickReject(RectF rect, EdgeType type) { return false; }
    public boolean quickReject(Path path, EdgeType type) { return false; }
    public enum EdgeType { BW, AA }
    public boolean isHardwareAccelerated() { return false; }

    public static final int CLIP_SAVE_FLAG = 0x02;
    public static final int MATRIX_SAVE_FLAG = 0x01;
}
