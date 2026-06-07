package nova.internal;

/**
 * JNI bridge for accessing final/private fields that Java reflection
 * cannot access.  Used by Launcher to init Activity internals.
 */
public class NovaReflect {
    public static native Object getFinalObjectField(Object obj, Class<?> targetClass, String fieldName);
    public static native void setFinalObjectField(Object obj, Class<?> targetClass, String fieldName, Object value);
}
