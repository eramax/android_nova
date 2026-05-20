package android.graphics;

public class Region {
    public enum Op { DIFFERENCE, INTERSECT, UNION, XOR, REVERSE_DIFFERENCE, REPLACE }

    private Rect mBounds = new Rect();
    private boolean mEmpty = true;

    public Region() {}
    public Region(Rect r) { set(r); }
    public Region(int left, int top, int right, int bottom) { set(left, top, right, bottom); }

    public boolean isEmpty() { return mEmpty; }
    public boolean isRect() { return !mEmpty; }
    public boolean isComplex() { return false; }

    public Rect getBounds() { return new Rect(mBounds); }
    public boolean getBounds(Rect r) { r.set(mBounds); return !mEmpty; }

    public void setEmpty() { mEmpty = true; mBounds.setEmpty(); }

    public boolean set(Region r) { mBounds.set(r.mBounds); mEmpty = r.mEmpty; return true; }
    public boolean set(Rect r) { mBounds.set(r); mEmpty = r.isEmpty(); return true; }
    public boolean set(int left, int top, int right, int bottom) {
        mBounds.set(left, top, right, bottom); mEmpty = false; return true;
    }

    public boolean op(Rect r, Op op) { return op(new Region(r), op); }
    public boolean op(int left, int top, int right, int bottom, Op op) {
        return op(new Region(left, top, right, bottom), op);
    }
    public boolean op(Region region, Op op) {
        switch (op) {
            case UNION: case REPLACE:
                if (!region.isEmpty()) { mBounds.union(region.mBounds); mEmpty = false; }
                break;
            case INTERSECT:
                if (!mBounds.intersect(region.mBounds)) setEmpty();
                break;
            case DIFFERENCE:
                break;
            default: break;
        }
        return !mEmpty;
    }
    public boolean op(Region r1, Region r2, Op op) { set(r1); return op(r2, op); }

    public boolean contains(int x, int y) { return !mEmpty && mBounds.contains(x, y); }
    public boolean quickContains(Rect r) { return !mEmpty && mBounds.contains(r); }
    public boolean quickContains(int l, int t, int r, int b) { return contains(l, t); }
    public boolean quickReject(Rect r) { return mEmpty || !Rect.intersects(mBounds, r); }
    public boolean quickReject(Region r) { return mEmpty || r.isEmpty(); }
    public boolean quickReject(int l, int t, int r, int b) { return mEmpty; }

    public void translate(int dx, int dy) { mBounds.offset(dx, dy); }
    public void translate(int dx, int dy, Region dst) { dst.set(this); dst.translate(dx, dy); }

    public boolean union(Rect r) { return op(r, Op.UNION); }
}
