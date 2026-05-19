package android.widget;

import android.content.Context;
import android.util.AttributeSet;

public class CompoundButton extends Button {
    private boolean mChecked;
    private OnCheckedChangeListener mOnCheckedChangeListener;

    public CompoundButton(Context context) {
        super(context);
    }

    public CompoundButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CompoundButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            if (mOnCheckedChangeListener != null) {
                mOnCheckedChangeListener.onCheckedChanged(this, mChecked);
            }
        }
    }

    public void toggle() {
        setChecked(!mChecked);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(CompoundButton buttonView, boolean isChecked);
    }
}
