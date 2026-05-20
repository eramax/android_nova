package android.graphics.drawable;

// VectorDrawable extends StateListDrawable so that it can be cast to StateListDrawable
// by RecyclerView and similar code, while also satisfying instanceof VectorDrawable checks.
public class VectorDrawable extends StateListDrawable {
    @Override public int getOpacity() { return android.graphics.PixelFormat.TRANSLUCENT; }
    @Override public int getIntrinsicWidth() { return 24; }
    @Override public int getIntrinsicHeight() { return 24; }
}
