package android.widget;

import android.content.Context;
import android.util.AttributeSet;

public class EditText extends TextView {
    public EditText(Context context) {
        super(context);
    }

    public EditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);
    }

    public void setSelection(int index) {}
    public void setSelection(int start, int stop) {}
    public int getSelectionStart() { return 0; }
    public int getSelectionEnd() { return 0; }
}
