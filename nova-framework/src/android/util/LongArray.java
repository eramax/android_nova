package android.util;

public class LongArray {
    private long[] mValues = new long[8];
    private int mSize;

    public LongArray() {
    }

    public void add(long value) {
        ensureCapacity(mSize + 1);
        mValues[mSize++] = value;
    }

    public long get(int index) {
        if (index < 0 || index >= mSize) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return mValues[index];
    }

    public int size() {
        return mSize;
    }

    private void ensureCapacity(int needed) {
        if (needed <= mValues.length) {
            return;
        }
        long[] next = new long[Math.max(needed, mValues.length * 2)];
        System.arraycopy(mValues, 0, next, 0, mSize);
        mValues = next;
    }
}
