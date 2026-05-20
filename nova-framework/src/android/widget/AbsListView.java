package android.widget;

import android.content.Context;
import android.util.AttributeSet;

public abstract class AbsListView extends AdapterView<ListAdapter> {
    public AbsListView(Context context) {
        super(context);
    }

    public AbsListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AbsListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
