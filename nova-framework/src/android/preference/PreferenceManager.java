package android.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.NovaSharedPreferences;

public class PreferenceManager {
    public static SharedPreferences getDefaultSharedPreferences(Context context) {
        String name = context.getPackageName() + "_preferences";
        return new NovaSharedPreferences(context, name);
    }
}
