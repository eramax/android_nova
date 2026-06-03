package android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class Button extends TextView {

    private boolean mIsPressed = false;
    private int mPressedColor = 0xFFCCCCCC;

    public Button(Context context) { super(context); init(); }
    public Button(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public Button(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        setClickable(true);
        setFocusable(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsPressed = true;
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                if (mIsPressed) {
                    mIsPressed = false;
                    invalidate();
                    performClick();
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                mIsPressed = false;
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mIsPressed) {
            canvas.drawColor(mPressedColor);
        }
        super.onDraw(canvas);
    }
}
