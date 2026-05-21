package com.google.android.collect;

import java.util.ArrayList;

public final class Lists {
    private Lists() {}

    public static <E> ArrayList<E> newArrayList() {
        return new ArrayList<>();
    }
}
