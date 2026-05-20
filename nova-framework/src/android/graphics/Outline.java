package android.graphics;

public final class Outline {
    public void setRect(int left, int top, int right, int bottom) {}
    public void setRect(Rect rect) {}
    public void setRoundRect(int left, int top, int right, int bottom, float radius) {}
    public void setRoundRect(Rect rect, float radius) {}
    public void setOval(int left, int top, int right, int bottom) {}
    public void setOval(Rect rect) {}
    public void setConvexPath(Path path) {}
    public void setEmpty() {}
    public boolean isEmpty() { return true; }
    public float getRadius() { return 0f; }
    public void setAlpha(float alpha) {}
    public float getAlpha() { return 1f; }
    public boolean canClip() { return false; }
    public boolean getRect(Rect outRect) { return false; }
}
