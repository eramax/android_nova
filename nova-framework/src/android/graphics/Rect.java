package android.graphics;

public class Rect {
    public int left;
    public int top;
    public int right;
    public int bottom;

    public Rect() {
    }

    public Rect(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public Rect(Rect src) {
        if (src != null) {
            this.left = src.left;
            this.top = src.top;
            this.right = src.right;
            this.bottom = src.bottom;
        }
    }

    public void set(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public void set(Rect src) {
        if (src != null) {
            set(src.left, src.top, src.right, src.bottom);
        }
    }

    public final int width() {
        return right - left;
    }

    public final int height() {
        return bottom - top;
    }

    public final boolean isEmpty() {
        return left >= right || top >= bottom;
    }

    public final int centerX() {
        return (left + right) >> 1;
    }

    public final int centerY() {
        return (top + bottom) >> 1;
    }

    public void offset(int dx, int dy) {
        left += dx;
        top += dy;
        right += dx;
        bottom += dy;
    }

    public void offsetTo(int newLeft, int newTop) {
        right += newLeft - left;
        bottom += newTop - top;
        left = newLeft;
        top = newTop;
    }

    public void inset(int dx, int dy) {
        left += dx;
        top += dy;
        right -= dx;
        bottom -= dy;
    }

    public void inset(int leftDelta, int topDelta, int rightDelta, int bottomDelta) {
        left += leftDelta;
        top += topDelta;
        right -= rightDelta;
        bottom -= bottomDelta;
    }

    public void inset(Insets insets) {
        left += insets.left;
        top += insets.top;
        right -= insets.right;
        bottom -= insets.bottom;
    }

    public boolean contains(int x, int y) {
        return x >= left && x < right && y >= top && y < bottom;
    }

    public boolean contains(int left, int top, int right, int bottom) {
        return this.left <= left && this.top <= top
                && this.right >= right && this.bottom >= bottom;
    }

    public boolean contains(Rect r) {
        return contains(r.left, r.top, r.right, r.bottom);
    }

    public boolean intersect(int left, int top, int right, int bottom) {
        if (this.left < right && left < this.right && this.top < bottom && top < this.bottom) {
            if (this.left < left) this.left = left;
            if (this.top < top) this.top = top;
            if (this.right > right) this.right = right;
            if (this.bottom > bottom) this.bottom = bottom;
            return true;
        }
        return false;
    }

    public boolean intersect(Rect r) {
        return intersect(r.left, r.top, r.right, r.bottom);
    }

    public void intersectUnchecked(Rect other) {
        left = Math.max(left, other.left);
        top = Math.max(top, other.top);
        right = Math.min(right, other.right);
        bottom = Math.min(bottom, other.bottom);
    }

    public boolean setIntersect(Rect a, Rect b) {
        if (a.left < b.right && b.left < a.right && a.top < b.bottom && b.top < a.bottom) {
            left = Math.max(a.left, b.left);
            top = Math.max(a.top, b.top);
            right = Math.min(a.right, b.right);
            bottom = Math.min(a.bottom, b.bottom);
            return true;
        }
        return false;
    }

    public void union(int left, int top, int right, int bottom) {
        if (left < right && top < bottom) {
            if (this.left > left) this.left = left;
            if (this.top > top) this.top = top;
            if (this.right < right) this.right = right;
            if (this.bottom < bottom) this.bottom = bottom;
        }
    }

    public void union(Rect r) {
        union(r.left, r.top, r.right, r.bottom);
    }

    public void union(int x, int y) {
        if (x < left) left = x;
        else if (x > right) right = x;
        if (y < top) top = y;
        else if (y > bottom) bottom = y;
    }

    public void scale(float scale) {
        if (scale != 1.0f) {
            left = (int) (left * scale + 0.5f);
            top = (int) (top * scale + 0.5f);
            right = (int) (right * scale + 0.5f);
            bottom = (int) (bottom * scale + 0.5f);
        }
    }

    public void setEmpty() {
        left = right = top = bottom = 0;
    }

    public String toString() {
        return "Rect(" + left + ", " + top + ", " + right + ", " + bottom + ")";
    }

    public String toShortString() {
        return "[" + left + "," + top + "][" + right + "," + bottom + "]";
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rect r = (Rect) o;
        return left == r.left && top == r.top && right == r.right && bottom == r.bottom;
    }

    public int hashCode() {
        int result = left;
        result = 31 * result + top;
        result = 31 * result + right;
        result = 31 * result + bottom;
        return result;
    }
}
