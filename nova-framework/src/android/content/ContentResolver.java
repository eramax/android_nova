package android.content;

public class ContentResolver {
    public static final String SCHEME_CONTENT = "content";
    public ContentResolver() {}
    public android.database.Cursor query(android.net.Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) { return null; }
    public android.database.Cursor query(android.net.Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, android.os.CancellationSignal cancellationSignal) { return null; }
    public android.database.Cursor query(android.net.Uri uri, String[] projection, android.os.Bundle queryArgs, android.os.CancellationSignal cancellationSignal) { return null; }
    public int delete(android.net.Uri uri, String selection, String[] selectionArgs) { return 0; }
    public android.net.Uri insert(android.net.Uri uri, android.content.ContentValues values) { return null; }
    public int update(android.net.Uri uri, android.content.ContentValues values, String selection, String[] selectionArgs) { return 0; }
    public void registerContentObserver(android.net.Uri uri, boolean notifyForDescendants, android.database.ContentObserver observer) {}
    public void unregisterContentObserver(android.database.ContentObserver observer) {}
    public void notifyChange(android.net.Uri uri, android.database.ContentObserver observer) {}
    public boolean takePersistableUriPermission(android.net.Uri uri, int modeFlags) { return false; }
}
