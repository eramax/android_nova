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
    public void setTextColor(android.content.res.ColorStateList colors) {
        if (colors != null) mTextColor = colors.getDefaultColor();
    }
    public android.content.res.ColorStateList getTextColors() {
        return android.content.res.ColorStateList.valueOf(mTextColor);
    }
    public int getCurrentTextColor() { return mTextColor; }
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
    public android.text.method.TransformationMethod getTransformationMethod() { return null; }
    public android.text.TextUtils.TruncateAt getEllipsize() { return null; }
    public int getMaxLines() { return Integer.MAX_VALUE; }
    public void setMinLines(int minlines) {}
    public int getMinLines() { return 0; }
    public void setMaxLength(int max) {}
    public boolean isSingleLine() { return false; }
    public int getInputType() { return 0; }
    public void setImeOptions(int imeOptions) {}
    public int getImeOptions() { return 0; }
    public void setCompoundDrawablesRelativeWithIntrinsicBounds(int start, int top, int end, int bottom) {}
    public void setCompoundDrawablesRelativeWithIntrinsicBounds(android.graphics.drawable.Drawable start, android.graphics.drawable.Drawable top, android.graphics.drawable.Drawable end, android.graphics.drawable.Drawable bottom) {}
    public void setCompoundDrawablesRelative(android.graphics.drawable.Drawable start, android.graphics.drawable.Drawable top, android.graphics.drawable.Drawable end, android.graphics.drawable.Drawable bottom) {}
    public android.graphics.drawable.Drawable[] getCompoundDrawables() { return new android.graphics.drawable.Drawable[4]; }
    public android.graphics.drawable.Drawable[] getCompoundDrawablesRelative() { return new android.graphics.drawable.Drawable[4]; }
    public void setCompoundDrawableTintList(android.content.res.ColorStateList tint) {}
    public android.content.res.ColorStateList getCompoundDrawableTintList() { return null; }
    public void setCompoundDrawableTintMode(android.graphics.PorterDuff.Mode tintMode) {}
    public android.graphics.PorterDuff.Mode getCompoundDrawableTintMode() { return null; }
    public void setLetterSpacing(float letterSpacing) {}
    public float getLetterSpacing() { return 0f; }
    public void setAllCaps(boolean allCaps) {}
    public boolean setFontVariationSettings(String fontVariationSettings) { return false; }
    public void setTextLocales(android.os.LocaleList locales) {}
    public android.os.LocaleList getTextLocales() { return android.os.LocaleList.getDefault(); }
    public String getFontVariationSettings() { return null; }
    public void setFontFeatureSettings(String fontFeatureSettings) {}
    public String getFontFeatureSettings() { return null; }
    public void setLineHeight(float lineHeight) {}
    public void setLineSpacing(float add, float mult) {}
    public float getLineSpacingExtra() { return 0f; }
    public float getLineSpacingMultiplier() { return 1f; }
    public void setTextScaleX(float size) {}
    public float getTextScaleX() { return 1f; }
    public int length() { return mText != null ? mText.length() : 0; }
    public int getSelectionStart() { return 0; }
    public int getSelectionEnd() { return 0; }
    public boolean hasSelection() { return false; }

    public void setTextAppearance(int resId) {}
    public void setTextAppearance(android.content.Context context, int resId) {}

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int textLength = mText != null ? mText.length() : 0;
        int desiredWidth = getPaddingLeft() + getPaddingRight() + Math.max(24, textLength * 9);
        int desiredHeight = getPaddingTop() + getPaddingBottom() + 24;
        setMeasuredDimension(resolveSizeAndState(desiredWidth, widthMeasureSpec, 0),
                resolveSizeAndState(desiredHeight, heightMeasureSpec, 0));
    }

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
