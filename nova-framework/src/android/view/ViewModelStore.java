package android.view;

import java.util.HashMap;

public class ViewModelStore {
    private final HashMap<String, Object> mMap = new HashMap<>();

    public void put(String key, Object viewModel) { mMap.put(key, viewModel); }
    public Object get(String key) { return mMap.get(key); }
    public void clear() { mMap.clear(); }
}
