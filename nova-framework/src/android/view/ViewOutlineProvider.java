package android.view;

import android.graphics.Outline;

public abstract class ViewOutlineProvider {
    public static final ViewOutlineProvider BACKGROUND = new ViewOutlineProvider() {
        @Override public void getOutline(View view, Outline outline) {}
    };
    public static final ViewOutlineProvider BOUNDS = new ViewOutlineProvider() {
        @Override public void getOutline(View view, Outline outline) {}
    };
    public static final ViewOutlineProvider PADDED_BOUNDS = new ViewOutlineProvider() {
        @Override public void getOutline(View view, Outline outline) {}
    };

    public abstract void getOutline(View view, Outline outline);
}
