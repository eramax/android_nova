package com.android.internal.util;

import java.lang.reflect.Array;

public class ArrayUtils {
    private ArrayUtils() {}

    @SuppressWarnings("unchecked")
    public static <T> T[] appendElement(Class<T> kind, T[] array, T element) {
        int len = array != null ? array.length : 0;
        T[] out = (T[]) Array.newInstance(kind, len + 1);
        if (len > 0) {
            System.arraycopy(array, 0, out, 0, len);
        }
        out[len] = element;
        return out;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] removeElement(Class<T> kind, T[] array, T element) {
        if (array == null || array.length == 0) {
            return array;
        }
        int index = -1;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == element || (array[i] != null && array[i].equals(element))) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            return array;
        }
        T[] out = (T[]) Array.newInstance(kind, array.length - 1);
        if (index > 0) {
            System.arraycopy(array, 0, out, 0, index);
        }
        if (index < array.length - 1) {
            System.arraycopy(array, index + 1, out, index, array.length - index - 1);
        }
        return out;
    }
}
