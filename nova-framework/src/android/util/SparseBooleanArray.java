package android.util;

public class SparseBooleanArray implements Cloneable {
    private int[] mKeys;
    private boolean[] mValues;
    private int mSize;

    public SparseBooleanArray() { this(10); }
    public SparseBooleanArray(int initialCapacity) {
        mKeys = new int[initialCapacity];
        mValues = new boolean[initialCapacity];
        mSize = 0;
    }

    public boolean get(int key) { return get(key, false); }
    public boolean get(int key, boolean valueIfKeyNotFound) {
        int i = indexOfKey(key);
        return i >= 0 ? mValues[i] : valueIfKeyNotFound;
    }

    public void put(int key, boolean value) {
        int i = indexOfKey(key);
        if (i >= 0) { mValues[i] = value; return; }
        i = ~i;
        if (mSize >= mKeys.length) grow();
        System.arraycopy(mKeys, i, mKeys, i + 1, mSize - i);
        System.arraycopy(mValues, i, mValues, i + 1, mSize - i);
        mKeys[i] = key; mValues[i] = value; mSize++;
    }

    public void delete(int key) {
        int i = indexOfKey(key);
        if (i >= 0) { System.arraycopy(mKeys, i + 1, mKeys, i, mSize - i - 1);
                      System.arraycopy(mValues, i + 1, mValues, i, mSize - i - 1);
                      mSize--; }
    }
    public void removeAt(int index) { if (index < mSize) delete(mKeys[index]); }

    public int size() { return mSize; }
    public int keyAt(int index) { return mKeys[index]; }
    public boolean valueAt(int index) { return mValues[index]; }
    public void setValueAt(int index, boolean value) { if (index < mSize) mValues[index] = value; }
    public int indexOfKey(int key) {
        int lo = 0, hi = mSize - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            if (mKeys[mid] < key) lo = mid + 1;
            else if (mKeys[mid] > key) hi = mid - 1;
            else return mid;
        }
        return ~lo;
    }
    public int indexOfValue(boolean value) {
        for (int i = 0; i < mSize; i++) if (mValues[i] == value) return i;
        return -1;
    }
    public void clear() { mSize = 0; }
    public void append(int key, boolean value) { put(key, value); }

    private void grow() {
        int newLen = mKeys.length * 2;
        mKeys = java.util.Arrays.copyOf(mKeys, newLen);
        mValues = java.util.Arrays.copyOf(mValues, newLen);
    }

    @Override public SparseBooleanArray clone() {
        SparseBooleanArray c = new SparseBooleanArray(mSize);
        System.arraycopy(mKeys, 0, c.mKeys, 0, mSize);
        System.arraycopy(mValues, 0, c.mValues, 0, mSize);
        c.mSize = mSize;
        return c;
    }
}
