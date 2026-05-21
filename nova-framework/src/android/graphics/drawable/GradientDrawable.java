package android.graphics.drawable;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.ViewDebug;

import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;

public class GradientDrawable extends Drawable {
    public static boolean sWrapNegativeAngleMeasurements = false;

    public GradientDrawable() {}

    public GradientDrawable(Orientation orientation, int[] colors) {}

    @Override
    public void draw(Canvas canvas) {}

    public void setColor(int argb) {}

    public void setColors(int[] colors) {}

    public void setCornerRadius(float radius) {}

    public void setCornerRadii(float[] radii) {}

    public float getCornerRadius() { return 0; }

    public float[] getCornerRadii() { return null; }

    public void setSize(int width, int height) {}

    public void setShape(int shape) {}

    public void setGradientType(int gradient) {}

    public void setGradientCenter(float x, float y) {}

    public void setGradientRadius(float radius) {}

    public void setOrientation(Orientation orientation) {}

    @Override
    public void setColorFilter(ColorFilter colorFilter) {}

    @Override
    public int getOpacity() { return PixelFormat.TRANSLUCENT; }

    @Override
    public void setTintList(ColorStateList tint) {}

    @Override
    public void setTintBlendMode(android.graphics.BlendMode blendMode) {}

    @Override
    public ConstantState getConstantState() { return null; }

    public enum Orientation {
        TOP_BOTTOM,
        TR_BL,
        RIGHT_LEFT,
        BR_TL,
        BOTTOM_TOP,
        BL_TR,
        LEFT_RIGHT,
        TL_BR,
    }

    public static final int RECTANGLE = 0;
    public static final int OVAL = 1;
    public static final int LINE = 2;
    public static final int RING = 3;

    public static final int LINEAR_GRADIENT = 0;
    public static final int RADIAL_GRADIENT = 1;
    public static final int SWEEP_GRADIENT = 2;
}
