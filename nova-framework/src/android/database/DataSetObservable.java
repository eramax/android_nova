package android.database;

public class DataSetObservable extends Observable<DataSetObserver> {
    public void notifyChanged() {
        for (int i = mObservers.size() - 1; i >= 0; i--) {
            mObservers.get(i).onChanged();
        }
    }

    public void notifyInvalidated() {
        for (int i = mObservers.size() - 1; i >= 0; i--) {
            mObservers.get(i).onInvalidated();
        }
    }
}
