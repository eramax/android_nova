package android.graphics;

public final class RectF {
    public float left;
    public float top;
    public float right;
    public float bottom;

    public RectF() {}

    public RectF(float left, float top, float right, float bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public RectF(Rect r) {
        if (r != null) { left = r.left; top = r.top; right = r.right; bottom = r.bottom; }
    }

    public RectF(RectF r) {
        if (r != null) { left = r.left; top = r.top; right = r.right; bottom = r.bottom; }
    }

    public void set(RectF src) {
        left = src.left; top = src.top; right = src.right; bottom = src.bottom;
    }

    public void set(Rect src) {
        left = src.left; top = src.top; right = src.right; bottom = src.bottom;
    }

    public void inset(float dx, float dy) {
        left += dx; top += dy; right -= dx; bottom -= dy;
    }

    public void union(float x, float y) {
        if (x < left) left = x; if (x > right) right = x;
        if (y < top) top = y; if (y > bottom) bottom = y;
    }

    public float width() { return right - left; }
    public float height() { return bottom - top; }
    public float centerX() { return (left + right) * 0.5f; }
    public float centerY() { return (top + bottom) * 0.5f; }
    public boolean isEmpty() { return left >= right || top >= bottom; }
    public boolean contains(RectF r) { return r != null && left <= r.left && top <= r.top && right >= r.right && bottom >= r.bottom; }
    public boolean intersect(float l, float t, float r, float b) {
        if (l < right && t < bottom && r > left && b > top) {
            if (left < l) left = l; if (top < t) top = t;
            if (right > r) right = r; if (bottom > b) bottom = b;
            return true;
        }
        return false;
    }
    public boolean intersect(RectF r) { return r != null && intersect(r.left, r.top, r.right, r.bottom); }
    public void sort() {
        if (left > right) { float t = left; left = right; right = t; }
        if (top > bottom) { float t = top; top = bottom; bottom = t; }
    }

    public void set(float left, float top, float right, float bottom) {
        this.left = left; this.top = top; this.right = right; this.bottom = bottom;
    }

    public void offset(float dx, float dy) {
        left += dx; top += dy; right += dx; bottom += dy;
    }

    public boolean contains(float x, float y) {
        return left < right && top < bottom && x >= left && x < right && y >= top && y < bottom;
    }

    public void roundOut(Rect dst) {
        dst.set((int)Math.floor(left), (int)Math.floor(top),
                (int)Math.ceil(right), (int)Math.ceil(bottom));
    }

    @Override
    public String toString() {
        return "RectF(" + left + ", " + top + ", " + right + ", " + bottom + ")";
    }
}
