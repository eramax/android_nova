package android.animation;

public class ObjectAnimator extends ValueAnimator {
    private Object mTarget;
    private String mPropertyName;

    public static ObjectAnimator ofFloat(Object target, String propertyName, float... values) {
        ObjectAnimator anim = new ObjectAnimator();
        anim.mTarget = target;
        anim.mPropertyName = propertyName;
        return anim;
    }
    public static ObjectAnimator ofFloat(Object target, String propertyName, float value1, float value2) {
        return ofFloat(target, propertyName, new float[]{value1, value2});
    }
    public static ObjectAnimator ofInt(Object target, String propertyName, int... values) {
        ObjectAnimator anim = new ObjectAnimator();
        anim.mTarget = target;
        anim.mPropertyName = propertyName;
        return anim;
    }
    public static ObjectAnimator ofArgb(Object target, String propertyName, int... values) {
        return ofInt(target, propertyName, values);
    }
    public void setTargetObject(Object target) { mTarget = target; }
    public Object getTarget() { return mTarget; }
    public void setProperty(android.util.Property property) {}
    public String getPropertyName() { return mPropertyName; }
    public void setPropertyName(String propertyName) { mPropertyName = propertyName; }
    @Override public ObjectAnimator setDuration(long duration) { super.setDuration(duration); return this; }
}
