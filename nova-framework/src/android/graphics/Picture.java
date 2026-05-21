package android.graphics;

public class Picture {
    public Canvas beginRecording(int width, int height) { return new Canvas(); }
    public void endRecording() {}
    public int getWidth() { return 0; }
    public int getHeight() { return 0; }
    public void draw(Canvas canvas) {}
}
