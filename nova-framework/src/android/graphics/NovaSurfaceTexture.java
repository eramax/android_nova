package android.graphics;

/**
 * Nova extension of SurfaceTexture carrying software canvas backing store.
 */
public class NovaSurfaceTexture extends SurfaceTexture {
    public Bitmap novaBitmap;
    public Canvas novaCanvas;

    public NovaSurfaceTexture(int texName) {
        super(texName);
    }

    public NovaSurfaceTexture(boolean singleBufferMode) {
        super(singleBufferMode);
    }
}
