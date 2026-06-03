package android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class EditText extends TextView {

    private int mMaxLength = -1;
    private String mHint;

    public EditText(Context context) { super(context); init(); }
    public EditText(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public EditText(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        setFocusable(true);
        setClickable(true);
    }

    public void setHint(CharSequence hint) { mHint = hint != null ? hint.toString() : null; }
    public CharSequence getHint() { return mHint; }

    public void setSelection(int index) {}
    public void setSelection(int start, int stop) {}
    public int getSelectionStart() { return 0; }
    public int getSelectionEnd() { return 0; }

    public void selectAll() {}

    public Editable getText() { return null; }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            String text = getTextString();
            if (text.length() > 0) {
                setText(text.substring(0, text.length() - 1));
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return true;
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return true;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = 1;
        return null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isFocused()) {
            Paint paint = new Paint();
            paint.setColor(0xFF000000);
            paint.setStrokeWidth(2);
            canvas.drawLine(mPaddingLeft + 10, mPaddingTop, mPaddingLeft + 10, mPaddingTop + 20, paint);
        }
    }
}
