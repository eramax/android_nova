package android.graphics;

public final class PorterDuffColorFilter extends ColorFilter {
    private final int mColor;
    private final PorterDuff.Mode mMode;

    public PorterDuffColorFilter(int color, PorterDuff.Mode mode) {
        mColor = color;
        mMode = mode;
    }

    public int getColor() { return mColor; }
    public PorterDuff.Mode getMode() { return mMode; }
}
