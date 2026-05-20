package android.widget;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;

public interface Adapter {
    int IGNORE_ITEM_VIEW_TYPE = -1;
    int NO_SELECTION = Integer.MIN_VALUE;

    int getCount();
    Object getItem(int position);
    long getItemId(int position);
    View getView(int position, View convertView, ViewGroup parent);

    default int getItemViewType(int position) { return 0; }
    default int getViewTypeCount() { return 1; }
    default boolean hasStableIds() { return false; }
    default boolean isEmpty() { return getCount() == 0; }
    default void registerDataSetObserver(DataSetObserver observer) {}
    default void unregisterDataSetObserver(DataSetObserver observer) {}
}
