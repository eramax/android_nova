package android.widget;

import android.content.Context;
import android.util.AttributeSet;

public class Spinner extends AbsSpinner {
    public static final int MODE_DIALOG  = 0;
    public static final int MODE_DROPDOWN = 1;

    public Spinner(Context context) {
        super(context);
    }

    public Spinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Spinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Spinner(Context context, int mode) {
        super(context);
    }

    public int getDropDownHorizontalOffset() { return 0; }
    public int getDropDownVerticalOffset() { return 0; }
    public int getDropDownWidth() { return 0; }
    public android.graphics.drawable.Drawable getPopupBackground() { return null; }
    public CharSequence getPrompt() { return null; }
    public void setDropDownHorizontalOffset(int offset) {}
    public void setDropDownVerticalOffset(int offset) {}
    public void setDropDownWidth(int width) {}
    public void setPopupBackgroundDrawable(android.graphics.drawable.Drawable background) {}
    public void setPrompt(CharSequence prompt) {}
    public void setSelection(int position) {}

    @Override public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {}
}
