package android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class TextView extends View {
    private CharSequence mText = "";
    private int mTextColor = 0xff000000;

    public TextView(Context context) { super(context); }
    public TextView(Context context, AttributeSet attrs) { super(context); }

    public void setText(CharSequence text) { mText = text != null ? text : ""; }
    public void setText(int resId) {
        if (getContext() != null) {
            setText(getContext().getResources().getText(resId));
        }
    }
    public CharSequence getText() { return mText; }
    public void setHint(CharSequence hint) {}
    public void setHint(int resId) {}
    public void setTextSize(float size) {}
    public void setTextSize(int unit, float size) {}
    public void setTextColor(int color) { mTextColor = color; }
    public void setTypeface(android.graphics.Typeface tf) {}
    public void setTypeface(android.graphics.Typeface tf, int style) {}
    public void setGravity(int gravity) {}
    public void setInputType(int type) {}
    public void setSingleLine(boolean singleLine) {}
    public void setSingleLine() {}
    public void setMaxLines(int maxLines) {}
    public void setLines(int lines) {}
    public void setEllipsize(android.text.TextUtils.TruncateAt where) {}
    public void setCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom) {}
    public void setCompoundDrawablesWithIntrinsicBounds(int left, int top, int right, int bottom) {}
    public void setCompoundDrawablePadding(int pad) {}
    public float getTextSize() { return 14f; }
    public android.text.Editable getEditableText() { return null; }
    public void addTextChangedListener(android.text.TextWatcher watcher) {}
    public void removeTextChangedListener(android.text.TextWatcher watcher) {}
    public void setMovementMethod(android.text.method.MovementMethod movement) {}
    public void setTransformationMethod(android.text.method.TransformationMethod method) {}
    public void setOnEditorActionListener(OnEditorActionListener l) {}
    public boolean onEditorAction(int actionCode) { return false; }

    public interface OnEditorActionListener {
        boolean onEditorAction(TextView v, int actionId, android.view.KeyEvent event);
    }

    public enum BufferType { NORMAL, SPANNABLE, EDITABLE }

    public void setText(CharSequence text, BufferType type) {}

    @Override
    protected void onDraw(Canvas canvas) {
        if (canvas == null || mText == null || mText.length() == 0) {
            return;
        }
        Paint paint = new Paint();
        paint.setColor(mTextColor);
        canvas.drawText(mText.toString(), 8f, 20f, paint);
    }
}
