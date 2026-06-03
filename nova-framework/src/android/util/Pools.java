package android.util;
public final class Pools {
    public interface Pool<T> { T acquire(); boolean release(T instance); }
    public static class SimplePool<T> implements Pool<T> {
        public SimplePool(int maxPoolSize) {}
        public T acquire() { return null; }
        public boolean release(T instance) { return false; }
    }
    public static class SynchronizedPool<T> extends SimplePool<T> {
        public SynchronizedPool(int maxPoolSize) { super(maxPoolSize); }
        @Override public T acquire() { return null; }
        @Override public boolean release(T instance) { return false; }
    }
}
