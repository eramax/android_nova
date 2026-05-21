package android.util;

public class IntArray {
    private int[] mValues = new int[8];
    private int mSize;

    public IntArray() {
    }

    public void add(int value) {
        ensureCapacity(mSize + 1);
        mValues[mSize++] = value;
    }

    public void add(int index, int value) {
        if (index < 0 || index > mSize) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        ensureCapacity(mSize + 1);
        System.arraycopy(mValues, index, mValues, index + 1, mSize - index);
        mValues[index] = value;
        mSize++;
    }

    public int get(int index) {
        if (index < 0 || index >= mSize) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return mValues[index];
    }

    public void set(int index, int value) {
        if (index < 0 || index >= mSize) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        mValues[index] = value;
    }

    public void remove(int index) {
        if (index < 0 || index >= mSize) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        System.arraycopy(mValues, index + 1, mValues, index, mSize - index - 1);
        mSize--;
    }

    public int size() {
        return mSize;
    }

    private void ensureCapacity(int needed) {
        if (needed <= mValues.length) {
            return;
        }
        int[] next = new int[Math.max(needed, mValues.length * 2)];
        System.arraycopy(mValues, 0, next, 0, mSize);
        mValues = next;
    }
}
