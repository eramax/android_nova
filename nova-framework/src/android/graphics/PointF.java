package android.graphics;

public class PointF {
    public float x;
    public float y;

    public PointF() {}
    public PointF(float x, float y) { this.x = x; this.y = y; }
    public PointF(Point p) { this.x = p.x; this.y = p.y; }

    public void set(float x, float y) { this.x = x; this.y = y; }
    public void set(PointF p) { this.x = p.x; this.y = p.y; }
    public void negate() { x = -x; y = -y; }
    public void offset(float dx, float dy) { x += dx; y += dy; }
    public boolean equals(float x, float y) { return this.x == x && this.y == y; }
    public static float length(float x, float y) { return (float) Math.sqrt(x * x + y * y); }
    public float length() { return length(x, y); }

    @Override public String toString() { return "PointF(" + x + ", " + y + ")"; }
}
