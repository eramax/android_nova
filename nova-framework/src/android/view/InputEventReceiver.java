package android.view;

public abstract class InputEventReceiver {
    public InputEventReceiver(InputChannel inputChannel, android.os.Looper looper) {
    }

    public void dispose() {
    }

    public void onInputEvent(InputEvent event) {
        finishInputEvent(event, false);
    }

    public final void finishInputEvent(InputEvent event, boolean handled) {
    }

    public static interface Factory {
        InputEventReceiver createInputEventReceiver(InputChannel inputChannel, android.os.Looper looper);
    }
}
