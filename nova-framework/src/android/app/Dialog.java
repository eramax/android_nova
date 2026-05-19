package android.app;

import android.content.Context;
import android.view.View;
import android.view.Window;

public class Dialog implements android.content.DialogInterface {
    private final Context mContext;
    private boolean mCancelable = true;
    private OnCancelListener mCancelListener;
    private OnDismissListener mDismissListener;

    public Dialog(Context context) {
        mContext = context;
    }

    public Dialog(Context context, int themeResId) {
        mContext = context;
    }

    public Context getContext() { return mContext; }

    public void setContentView(View view) {}
    public void setContentView(int layoutResId) {}

    public void setTitle(CharSequence title) {}
    public void setTitle(int titleId) {}

    public void setCancelable(boolean cancelable) { mCancelable = cancelable; }
    public void setCanceledOnTouchOutside(boolean cancel) {}

    public void setOnCancelListener(OnCancelListener listener) { mCancelListener = listener; }
    public void setOnDismissListener(OnDismissListener listener) { mDismissListener = listener; }
    public void setOnShowListener(OnShowListener listener) {}

    public void show() {}
    public void dismiss() {}
    public void cancel() {}

    public boolean isShowing() { return false; }

    public Window getWindow() { return null; }

    public View findViewById(int id) { return null; }

    public interface OnShowListener {
        void onShow(android.content.DialogInterface dialog);
    }
}
