package android.util;

import android.content.res.Configuration;

/** Minimal MergedConfiguration stub for Nova host runtime. */
public final class MergedConfiguration {
    private Configuration mGlobalConfig = new Configuration();
    private Configuration mOverrideConfig = new Configuration();

    public MergedConfiguration() {
    }

    public Configuration getGlobalConfiguration() {
        return mGlobalConfig;
    }

    public Configuration getOverrideConfiguration() {
        return mOverrideConfig;
    }

    public void setConfiguration(Configuration global, Configuration override) {
        mGlobalConfig = global != null ? global : new Configuration();
        mOverrideConfig = override != null ? override : new Configuration();
    }
}
