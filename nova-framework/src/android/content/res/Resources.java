package android.content.res;

/** Minimal stub for annotation defaults. */
public class Resources {
    public static final int ID_NULL = 0;

    public static class Theme {}

    public static class NotFoundException extends Exception {
        public NotFoundException(String name) {
            super(name);
        }
    }
}
