package android.view;

/** Minimal InsetsState stub for Nova host runtime. */
public final class InsetsState implements android.os.Parcelable {
    public InsetsState() {
    }

    public InsetsState(InsetsState copy) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(android.os.Parcel dest, int flags) {
    }

    public static final android.os.Parcelable.Creator<InsetsState> CREATOR =
            new android.os.Parcelable.Creator<InsetsState>() {
        @Override
        public InsetsState createFromParcel(android.os.Parcel in) {
            return new InsetsState();
        }

        @Override
        public InsetsState[] newArray(int size) {
            return new InsetsState[size];
        }
    };
}
