package android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public final class Space extends View {
    public Space(Context context) {
        super(context);
        setVisibility(INVISIBLE);
    }

    public Space(Context context, AttributeSet attrs) {
        super(context, attrs);
        setVisibility(INVISIBLE);
    }

    public Space(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);
        setVisibility(INVISIBLE);
    }
}
