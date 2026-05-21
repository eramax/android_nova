package android.util;

import java.util.ArrayList;

public class LongSparseArray<E> {
    private final ArrayList<Long> mKeys = new ArrayList<>();
    private final ArrayList<E> mValues = new ArrayList<>();

    public LongSparseArray() {
    }

    public LongSparseArray(int initialCapacity) {
    }

    public void clear() {
        mKeys.clear();
        mValues.clear();
    }

    public int indexOfKey(long key) {
        return mKeys.indexOf(key);
    }

    public E get(long key) {
        int index = indexOfKey(key);
        return index >= 0 ? mValues.get(index) : null;
    }

    public E valueAt(int index) {
        return mValues.get(index);
    }

    public void put(long key, E value) {
        int index = indexOfKey(key);
        if (index >= 0) {
            mValues.set(index, value);
            return;
        }
        mKeys.add(key);
        mValues.add(value);
    }

    public int size() {
        return mKeys.size();
    }
}
