package android.hardware.input;

import android.view.InputDevice;
import android.os.Handler;

public class InputManagerGlobal {
    private static InputManagerGlobal sInstance;

    public static InputManagerGlobal getInstance() {
        if (sInstance == null) {
            sInstance = new InputManagerGlobal();
        }
        return sInstance;
    }

    public InputDevice getInputDevice(int deviceId) {
        return null;
    }

    public int[] getInputDeviceIds() {
        return new int[0];
    }

    public void registerInputDeviceListener(InputDeviceListener listener, Handler handler) {
    }

    public void unregisterInputDeviceListener(InputDeviceListener listener) {
    }

    public interface InputDeviceListener {
        void onInputDeviceAdded(int deviceId);
        void onInputDeviceChanged(int deviceId);
        void onInputDeviceRemoved(int deviceId);
    }
}
