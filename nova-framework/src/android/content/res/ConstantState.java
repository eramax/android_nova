package android.content.res;

import android.content.pm.ActivityInfo.Config;

/** Minimal stub for animation/drawable constant-state compile. */
public abstract class ConstantState<T> {
    public abstract @Config int getChangingConfigurations();

    public abstract T newInstance();
}
