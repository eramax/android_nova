package com.android.internal.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;

public class ArrayUtils {
    public static boolean isEmpty(int[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(long[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean contains(int[] array, int value) {
        if (array == null) return false;
        for (int element : array) {
            if (element == value) return true;
        }
        return false;
    }

    public static boolean contains(long[] array, long value) {
        if (array == null) return false;
        for (long element : array) {
            if (element == value) return true;
        }
        return false;
    }

    public static boolean contains(Object[] array, Object value) {
        if (array == null) return false;
        for (Object element : array) {
            if (element != null && element.equals(value)) return true;
            if (element == null && value == null) return true;
        }
        return false;
    }

    public static int indexOf(int[] array, int value) {
        if (array == null) return -1;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) return i;
        }
        return -1;
    }

    public static <T> int indexOf(T[] array, T value) {
        if (array == null) return -1;
        for (int i = 0; i < array.length; i++) {
            if (array[i] != null && array[i].equals(value)) return i;
            if (array[i] == null && value == null) return i;
        }
        return -1;
    }

    public static <T> T[] appendElement(Class<T> kind, T[] array, T element) {
        int end;
        if (array != null) {
            end = array.length;
            T[] result = (T[]) Array.newInstance(kind, end + 1);
            System.arraycopy(array, 0, result, 0, end);
            result[end] = element;
            return result;
        } else {
            T[] result = (T[]) Array.newInstance(kind, 1);
            result[0] = element;
            return result;
        }
    }

    public static int[] appendInt(int[] cur, int val) {
        if (cur == null) return new int[]{val};
        final int N = cur.length;
        int[] ret = new int[N + 1];
        System.arraycopy(cur, 0, ret, 0, N);
        ret[N] = val;
        return ret;
    }

    public static long[] appendLong(long[] cur, long val) {
        if (cur == null) return new long[]{val};
        final int N = cur.length;
        long[] ret = new long[N + 1];
        System.arraycopy(cur, 0, ret, 0, N);
        ret[N] = val;
        return ret;
    }

    public static String[] appendString(String[] cur, String val) {
        if (cur == null) return new String[]{val};
        final int N = cur.length;
        String[] ret = new String[N + 1];
        System.arraycopy(cur, 0, ret, 0, N);
        ret[N] = val;
        return ret;
    }

    public static <T> T[] newUnpaddedArray(Class<T> clazz, int minLen) {
        return (T[]) Array.newInstance(clazz, minLen);
    }

    public static int[] newUnpaddedIntArray(int minLen) {
        return new int[minLen];
    }

    public static long[] newUnpaddedLongArray(int minLen) {
        return new long[minLen];
    }

    public static Object[] newUnpaddedObjectArray(int minLen) {
        return new Object[minLen];
    }

    public static <T> T[] removeElement(Class<T> kind, T[] array, T element) {
        if (array == null) return null;
        int length = array.length;
        int i = indexOf(array, element);
        if (i < 0) return array;
        T[] result = (T[]) Array.newInstance(kind, length - 1);
        System.arraycopy(array, 0, result, 0, i);
        System.arraycopy(array, i + 1, result, i, length - i - 1);
        return result;
    }

    public static long total(long[] a) {
        long sum = 0;
        if (a != null) {
            for (long v : a) sum += v;
        }
        return sum;
    }

    public static int[] convertToIntArray(List<Integer> list) {
        if (list == null) return null;
        int[] result = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    public static int indexOf(long[] array, long value) {
        if (array == null) return -1;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) return i;
        }
        return -1;
    }
}
