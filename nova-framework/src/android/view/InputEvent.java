package android.view;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class InputEvent implements Parcelable {
    public static final int PARCEL_TOKEN_MOTION_EVENT = 1;
    public static final int PARCEL_TOKEN_KEY_EVENT = 2;

    protected int mSeq;

    InputEvent() {}

    public abstract int getDeviceId();

    public abstract int getSource();

    public void setSource(int source) {}

    public abstract int getDisplayId();

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {}

    public final InputEvent copy() {
        return null;
    }

    public void recycle() {}

    public void recycleIfNeededAfterDispatch() {}

    public long getEventTime() {
        return 0;
    }

    public long getDownTime() {
        return 0;
    }

    public long getEventTimeNanos() { return 0; }

    public void setId(int id) { mSeq = id; }

    public int getId() { return mSeq; }

    public int getSequenceNumber() { return mSeq; }

    public void setDisplayId(int displayId) {}

    public boolean isFromSource(int source) { return false; }

    public int getAction() { return 0; }

    public void markAsHandled() {}
}
