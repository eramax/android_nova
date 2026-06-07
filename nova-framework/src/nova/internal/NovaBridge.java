package nova.internal;

/** JNI bridge for operations Java reflection can't do (read final fields). */
public class NovaBridge {
    public static native Object getObjectField(Object obj, String fieldName);
}
