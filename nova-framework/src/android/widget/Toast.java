package android.widget;

import android.content.Context;
import android.util.Log;
import android.view.View;

public class Toast {
    private static final String TAG = "NovaToast";
    public static final int LENGTH_SHORT = 0;
    public static final int LENGTH_LONG  = 1;

    private final Context mContext;
    private CharSequence mText;
    private int mDuration;
    private View mView;

    public Toast(Context context) {
        mContext = context;
    }

    public static Toast makeText(Context context, CharSequence text, int duration) {
        Toast t = new Toast(context);
        t.mText = text;
        t.mDuration = duration;
        return t;
    }

    public static Toast makeText(Context context, int resId, int duration) {
        String text;
        try {
            text = context.getResources().getString(resId);
        } catch (Exception e) {
            text = "(toast:" + resId + ")";
        }
        return makeText(context, text, duration);
    }

    public void show() {
        Log.i(TAG, "[Toast] " + mText);
    }

    public void cancel() {}

    public void setDuration(int duration) { mDuration = duration; }
    public int getDuration() { return mDuration; }
    public void setText(CharSequence s) { mText = s; }
    public void setText(int resId) {}
    public void setView(View view) { mView = view; }
    public View getView() { return mView; }
    public void setMargin(float horizontalMargin, float verticalMargin) {}
    public void setGravity(int gravity, int xOffset, int yOffset) {}
    public int getGravity() { return 0; }
    public int getXOffset() { return 0; }
    public int getYOffset() { return 0; }
}
