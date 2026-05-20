package com.android.tools.r8;

import java.util.ArrayList;

public class RecordTag {
    public static int m(Object tag, ArrayList<Object> fields, Object values, int hashCode, int depth) {
        return hashCode;
    }
    public static boolean equals(Object a, Object b, Object fields) {
        return a == b;
    }
    public static int hashCode(Object obj, Object fields) {
        return obj != null ? obj.hashCode() : 0;
    }
    public static String toString(Object obj, Object fields) {
        return obj != null ? obj.toString() : "null";
    }
}
