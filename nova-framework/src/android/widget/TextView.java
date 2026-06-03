package android.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.MovementMethod;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class TextView extends View {

    private CharSequence mText = "";
    private TextPaint mTextPaint;
    private int mTextColor = Color.BLACK;
    private float mTextSize = 15;
    private int mGravity = Gravity.TOP | Gravity.START;
    private int mMaxLines = Integer.MAX_VALUE;
    private int mMinLines = 1;
    private int mMaxEms = -1;
    private int mMinEms = -1;
    private int mMaxWidth = Integer.MAX_VALUE;
    private int mMinWidth = 0;
    private int mMaxHeight = Integer.MAX_VALUE;
    private int mMinHeight = 0;
    private TextUtils.TruncateAt mEllipsize = null;
    private InputFilter[] mFilters = new InputFilter[0];

    public TextView(Context context) { super(context); init(); }
    public TextView(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public TextView(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }
    public TextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); init(); }

    private void init() {
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
    }

    public CharSequence getText() { return mText; }
    public String getTextString() { return mText != null ? mText.toString() : ""; }

    public void setText(CharSequence text) {
        mText = text != null ? text : "";
        requestLayout();
        invalidate();
    }

    public void setText(int resid) {
        Resources res = getResources();
        if (res != null) setText(res.getString(resid));
    }

    public final void setText(int resid, TextView.BufferType type) { setText(resid); }

    public void setTextColor(int color) {
        mTextColor = color;
        mTextPaint.setColor(color);
        invalidate();
    }

    public void setTextColor(ColorStateList colors) {
        setTextColor(colors.getDefaultColor());
    }

    public int getCurrentTextColor() { return mTextColor; }

    public void setTextSize(float size) { setTextSize(TypedValue.COMPLEX_UNIT_SP, size); }

    public void setTextSize(int unit, float size) {
        mTextSize = TypedValue.applyDimension(unit, size, getResources().getDisplayMetrics());
        mTextPaint.setTextSize(mTextSize);
        requestLayout();
        invalidate();
    }

    public float getTextSize() { return mTextSize; }

    public void setTypeface(Typeface tf) {
        mTextPaint.setTypeface(tf);
        invalidate();
    }

    public Typeface getTypeface() { return null; }

    public void setPaintFlags(int flags) { mTextPaint.setFlags(flags); }
    public int getPaintFlags() { return mTextPaint.getFlags(); }

    public void setGravity(int gravity) { mGravity = gravity; }
    public int getGravity() { return mGravity; }

    public void setMaxLines(int maxLines) { mMaxLines = maxLines; }
    public int getMaxLines() { return mMaxLines; }

    public void setMinLines(int minLines) { mMinLines = minLines; }
    public int getMinLines() { return mMinLines; }

    public void setLines(int lines) { mMaxLines = lines; mMinLines = lines; }

    public void setMaxEms(int maxems) { mMaxEms = maxems; }
    public int getMaxEms() { return mMaxEms; }

    public void setMinEms(int minems) { mMinEms = minems; }
    public int getMinEms() { return mMinEms; }

    public void setEms(int ems) { mMaxEms = ems; mMinEms = ems; }

    public void setMaxWidth(int maxpixels) { mMaxWidth = maxpixels; }
    public int getMaxWidth() { return mMaxWidth; }

    public void setMinWidth(int minpixels) { mMinWidth = minpixels; }
    public int getMinWidth() { return mMinWidth; }

    public void setMaxHeight(int maxpixels) { mMaxHeight = maxpixels; }
    public int getMaxHeight() { return mMaxHeight; }

    public void setMinHeight(int minpixels) { mMinHeight = minpixels; }
    public int getMinHeight() { return mMinHeight; }

    public void setSingleLine(boolean singleLine) { if (singleLine) mMaxLines = 1; }
    public boolean isSingleLine() { return mMaxLines == 1; }

    public void setEllipsize(TextUtils.TruncateAt where) { mEllipsize = where; }
    public TextUtils.TruncateAt getEllipsize() { return mEllipsize; }

    public void setInputType(int type) {}

    public void setFilters(InputFilter[] filters) { mFilters = filters; }
    public InputFilter[] getFilters() { return mFilters; }

    public void setTransformationMethod(TransformationMethod method) {}
    public TransformationMethod getTransformationMethod() { return null; }

    public void setMovementMethod(MovementMethod method) {}

    public void append(CharSequence text) {
        setText(mText + text.toString());
    }

    public int getLineHeight() {
        return (int) mTextSize + 4;
    }

    public int getLineCount() { return 1; }

    public void addTextChangedListener(TextWatcher watcher) {}
    public void removeTextChangedListener(TextWatcher watcher) {}

    public Editable getEditableText() { return null; }

    public void setSelectAllOnFocus(boolean selectAllOnFocus) {}

    public int getAutofillType() { return AUTOFILL_TYPE_TEXT; }
    public String[] getAutofillHints() { return null; }

    public void setError(CharSequence error) {}
    public void setError(CharSequence error, Drawable icon) {}

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        String text = getTextString();
        float textWidth = 0;
        if (text.length() > 0) textWidth = mTextPaint.measureText(text);
        int desiredWidth = (int) Math.ceil(textWidth) + mPaddingLeft + mPaddingRight;
        int desiredHeight = getLineHeight() + mPaddingTop + mPaddingBottom;
        setMeasuredDimension(
            resolveSize(Math.max(desiredWidth, getSuggestedMinimumWidth()), widthMeasureSpec),
            resolveSize(Math.max(desiredHeight, getSuggestedMinimumHeight()), heightMeasureSpec));
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        String text = getTextString();
        if (text.length() == 0) return;
        int x = mPaddingLeft;
        int y = mPaddingTop + (int) Math.abs(mTextPaint.ascent());
        canvas.drawText(text, x, y, mTextPaint);
    }

    public InputConnection onCreateInputConnection(EditorInfo outAttrs) { return null; }
    public void onEditorAction(int actionCode) {}

    public void setIncludeFontPadding(boolean includeFontPadding) {}
    public boolean getIncludeFontPadding() { return true; }

    public enum BufferType {
        NORMAL, SPANNABLE, EDITABLE
    }
}
