package android.widget;

public interface ListAdapter extends Adapter {
    default boolean areAllItemsEnabled() { return true; }
    default boolean isEnabled(int position) { return true; }
}
