package android.database;

import java.util.ArrayList;

public abstract class Observable<T> {
    protected final ArrayList<T> mObservers = new ArrayList<>();

    public void registerObserver(T observer) {
        if (observer == null) {
            throw new IllegalArgumentException("observer == null");
        }
        if (!mObservers.contains(observer)) {
            mObservers.add(observer);
        }
    }

    public void unregisterObserver(T observer) {
        mObservers.remove(observer);
    }

    public void unregisterAll() {
        mObservers.clear();
    }
}
