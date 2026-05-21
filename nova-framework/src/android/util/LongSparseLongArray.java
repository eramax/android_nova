package android.util;

import java.util.ArrayList;

public class LongSparseLongArray {
    private final ArrayList<Long> mKeys = new ArrayList<>();
    private final ArrayList<Long> mValues = new ArrayList<>();

    public LongSparseLongArray() {
    }

    public LongSparseLongArray(int initialCapacity) {
    }

    public void clear() {
        mKeys.clear();
        mValues.clear();
    }

    public int indexOfKey(long key) {
        return mKeys.indexOf(key);
    }

    public long valueAt(int index) {
        return mValues.get(index);
    }

    public void put(long key, long value) {
        int index = indexOfKey(key);
        if (index >= 0) {
            mValues.set(index, value);
            return;
        }
        mKeys.add(key);
        mValues.add(value);
    }
}
