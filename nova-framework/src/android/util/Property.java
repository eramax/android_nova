package android.util;

public abstract class Property<T, V> {
    private final String mName;
    private final Class<V> mType;

    public Property(Class<V> type, String name) {
        mType = type;
        mName = name;
    }

    public String getName() { return mName; }
    public Class<V> getType() { return mType; }
    public abstract V get(T object);
    public void set(T object, V value) {}
    public boolean isReadOnly() { return false; }
}
