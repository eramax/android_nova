package android.widget;

import android.database.DataSetObservable;
import android.database.DataSetObserver;

public abstract class BaseAdapter implements ListAdapter, SpinnerAdapter {
    private final DataSetObservable mObservable = new DataSetObservable();

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mObservable.registerObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mObservable.unregisterObserver(observer);
    }

    public void notifyDataSetChanged() {
        mObservable.notifyChanged();
    }

    public void notifyDataSetInvalidated() {
        mObservable.notifyInvalidated();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return getCount() == 0;
    }
}
