package android.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public abstract class AdapterView<T extends Adapter> extends ViewGroup {
    public static final int INVALID_POSITION = -1;
    public static final long INVALID_ROW_ID = Long.MIN_VALUE;

    public interface OnItemClickListener {
        void onItemClick(AdapterView<?> parent, View view, int position, long id);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id);
    }

    public interface OnItemSelectedListener {
        void onItemSelected(AdapterView<?> parent, View view, int position, long id);
        void onNothingSelected(AdapterView<?> parent);
    }

    private T mAdapter;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private OnItemSelectedListener mOnItemSelectedListener;
    private int mSelectedPosition = INVALID_POSITION;
    private long mSelectedRowId = INVALID_ROW_ID;
    private final DataSetObserver mObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            refreshChildren();
        }

        @Override
        public void onInvalidated() {
            refreshChildren();
        }
    };

    public AdapterView(Context context) {
        super(context);
    }

    public AdapterView(Context context, AttributeSet attrs) {
        super(context);
    }

    public AdapterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context);
    }

    public abstract T getAdapter();

    public abstract void setAdapter(T adapter);

    protected void setAdapterInternal(T adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mObserver);
        }
        mAdapter = adapter;
        if (mAdapter != null) {
            mAdapter.registerDataSetObserver(mObserver);
        }
        refreshChildren();
    }

    protected T getAdapterInternal() {
        return mAdapter;
    }

    protected void refreshChildren() {
        removeAllViews();
        T adapter = mAdapter;
        if (adapter == null) {
            requestLayout();
            invalidate();
            return;
        }
        int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            View child = adapter.getView(i, null, this);
            if (child != null) {
                addView(child);
            }
        }
        requestLayout();
        invalidate();
    }

    public View getSelectedView() {
        return getChildAt(mSelectedPosition);
    }

    public Object getSelectedItem() {
        T adapter = mAdapter;
        return adapter != null && mSelectedPosition >= 0 && mSelectedPosition < adapter.getCount()
                ? adapter.getItem(mSelectedPosition) : null;
    }

    public int getSelectedItemPosition() {
        return mSelectedPosition;
    }

    public long getSelectedItemId() {
        return mSelectedRowId;
    }

    public OnItemClickListener getOnItemClickListener() {
        return mOnItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public OnItemSelectedListener getOnItemSelectedListener() {
        return mOnItemSelectedListener;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        mOnItemSelectedListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
    }

    public boolean performItemClick(View view, int position, long id) {
        if (mOnItemClickListener == null) {
            return false;
        }
        mOnItemClickListener.onItemClick(this, view, position, id);
        return true;
    }

    public void setSelection(int position) {
        mSelectedPosition = position;
        T adapter = mAdapter;
        mSelectedRowId = adapter != null && position >= 0 && position < adapter.getCount()
                ? adapter.getItemId(position) : INVALID_ROW_ID;
        if (mOnItemSelectedListener != null) {
            View selectedView = getSelectedView();
            if (selectedView != null && position >= 0) {
                mOnItemSelectedListener.onItemSelected(this, selectedView, position, mSelectedRowId);
            } else {
                mOnItemSelectedListener.onNothingSelected(this);
            }
        }
    }
}
