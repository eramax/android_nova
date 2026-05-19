package android.content.res;
import android.graphics.Color;
public class ColorStateList {
    private final int mDefaultColor;
    public ColorStateList(int[][] states, int[] colors) {
        mDefaultColor = colors != null && colors.length > 0 ? colors[colors.length - 1] : Color.BLACK;
    }
    public static ColorStateList valueOf(int color) {
        return new ColorStateList(new int[][]{new int[0]}, new int[]{color});
    }
    public int getDefaultColor() { return mDefaultColor; }
    public int getColorForState(int[] stateSet, int defaultColor) { return mDefaultColor; }
    public boolean isStateful() { return false; }
}
