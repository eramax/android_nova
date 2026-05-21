package android.view;

/** Minimal SurfaceControl stub for Nova host runtime. */
public class SurfaceControl {
    public static class Transaction {}

    public SurfaceControl() {
    }

    public void release() {
    }

    public static final class Builder {
        public Builder() {
        }

        public Builder setName(String name) {
            return this;
        }

        public SurfaceControl build() {
            return new SurfaceControl();
        }
    }
}
