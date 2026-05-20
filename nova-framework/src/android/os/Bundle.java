package android.os;

public final class Bundle extends BaseBundle implements Cloneable, Parcelable {

    public static final Bundle EMPTY = new Bundle();

    public Bundle() {}
    public Bundle(Bundle b) { super(b); }
    public Bundle(int capacity) { super(capacity); }

    public static Bundle forPair(String key, String value) {
        Bundle b = new Bundle(); b.putString(key, value); return b;
    }

    public void putAll(Bundle bundle) { if (bundle != null) mMap.putAll(bundle.mMap); }

    public void putParcelable(String key, Parcelable value) { mMap.put(key, value); }
    public void putBinder(String key, IBinder value)  { mMap.put(key, value); }
    public void putBundle(String key, Bundle value)   { mMap.put(key, value); }
    public <T extends Parcelable> void putParcelableArray(String key, T[] value) { mMap.put(key, value); }
    public <T extends Parcelable> void putParcelableArrayList(String key, java.util.ArrayList<T> value) { mMap.put(key, value); }

    public Bundle getBundle(String key) { Object v = mMap.get(key); return v instanceof Bundle ? (Bundle)v : null; }
    public IBinder getBinder(String key) { Object v = mMap.get(key); return v instanceof IBinder ? (IBinder)v : null; }
    @SuppressWarnings("unchecked")
    public <T extends Parcelable> T getParcelable(String key) { return (T) mMap.get(key); }
    public Parcelable[] getParcelableArray(String key) { Object v = mMap.get(key); return v instanceof Parcelable[] ? (Parcelable[])v : null; }
    @SuppressWarnings("unchecked")
    public <T extends Parcelable> java.util.ArrayList<T> getParcelableArrayList(String key) { return (java.util.ArrayList<T>) mMap.get(key); }

    @Override
    public Bundle clone() { return new Bundle(this); }

    @Override
    public String toString() { return "Bundle[" + mMap.toString() + "]"; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {}

    public static final Parcelable.Creator<Bundle> CREATOR = new Parcelable.Creator<Bundle>() {
        @Override public Bundle createFromParcel(Parcel in) { return new Bundle(); }
        @Override public Bundle[] newArray(int size) { return new Bundle[size]; }
    };
}
