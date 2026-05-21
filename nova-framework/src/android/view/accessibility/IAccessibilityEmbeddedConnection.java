package android.view.accessibility;

import android.os.IBinder;
import android.os.IInterface;

public interface IAccessibilityEmbeddedConnection extends IInterface {
    abstract class Stub implements IAccessibilityEmbeddedConnection, IBinder {
        @Override
        public IBinder asBinder() {
            return this;
        }
    }
}
