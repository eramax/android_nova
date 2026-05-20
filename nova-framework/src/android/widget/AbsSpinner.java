package android.widget;

import android.content.Context;
import android.util.AttributeSet;

public abstract class AbsSpinner extends AdapterView<SpinnerAdapter> {
    public AbsSpinner(Context context) {
        super(context);
    }

    public AbsSpinner(Context context, AttributeSet attrs) {
        super(context);
    }

    public AbsSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context);
    }

    @Override public SpinnerAdapter getAdapter() { return null; }
    @Override public void setAdapter(SpinnerAdapter adapter) {}

    public Object getSelectedItem() { return null; }
    public int getSelectedItemPosition() { return AdapterView.INVALID_POSITION; }

    @Override public android.os.Parcelable onSaveInstanceState() { return null; }
    @Override public void onRestoreInstanceState(android.os.Parcelable state) {}
}
