package android.graphics;

public class Path {
    public enum Direction {
        CW,
        CCW
    }

    public Path() {}

    public Path(Path src) {}

    public void reset() {}

    public void rewind() {}

    public void set(Path src) {}

    public void moveTo(float x, float y) {}

    public void rMoveTo(float dx, float dy) {}

    public void lineTo(float x, float y) {}

    public void rLineTo(float dx, float dy) {}

    public void quadTo(float x1, float y1, float x2, float y2) {}

    public void rQuadTo(float dx1, float dy1, float dx2, float dy2) {}

    public void cubicTo(float x1, float y1, float x2, float y2, float x3, float y3) {}

    public void rCubicTo(float x1, float y1, float x2, float y2, float x3, float y3) {}

    public void arcTo(RectF oval, float startAngle, float sweepAngle, boolean forceMoveTo) {}

    public void arcTo(RectF oval, float startAngle, float sweepAngle) {}

    public void close() {}

    public void addRect(RectF rect, Direction dir) {}

    public void addRect(float left, float top, float right, float bottom, Direction dir) {}

    public void addOval(RectF oval, Direction dir) {}

    public void addCircle(float x, float y, float radius, Direction dir) {}

    public void addArc(RectF oval, float startAngle, float sweepAngle) {}

    public void addRoundRect(RectF rect, float rx, float ry, Direction dir) {}

    public void addRoundRect(RectF rect, float[] radii, Direction dir) {}

    public void addPath(Path src, float dx, float dy) {}

    public void addPath(Path src) {}

    public void addPath(Path src, Matrix matrix) {}

    public void offset(float dx, float dy, Path dst) {}

    public void offset(float dx, float dy) {}

    public void setLastPoint(float dx, float dy) {}

    public void transform(Matrix matrix, Path dst) {}

    public void transform(Matrix matrix) {}

    public boolean isEmpty() {
        return true;
    }

    public boolean isRect(RectF rect) {
        return false;
    }

    public void computeBounds(RectF bounds, boolean exact) {
        if (bounds != null) {
            bounds.set(0, 0, 0, 0);
        }
    }

    public boolean isConvex() {
        return true;
    }
}
