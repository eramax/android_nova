package android.text;

import android.graphics.Paint;

public class TextPaint extends Paint {
    public int baselineShift;
    public int linkColor;
    public int[] drawableState;
    public float density = 1.0f;
    public boolean underlineText;

    public TextPaint() { super(); }
    public TextPaint(int flags) { super(flags); }
    public TextPaint(Paint p) { super(p); }

    public void set(TextPaint tp) {}
}
