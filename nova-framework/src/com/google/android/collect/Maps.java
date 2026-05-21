package com.google.android.collect;

import java.util.HashMap;

public final class Maps {
    private Maps() {}

    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<>();
    }
}
