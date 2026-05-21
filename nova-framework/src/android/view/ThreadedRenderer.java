package android.view;

import android.content.Context;
import android.graphics.HardwareRenderer;
import android.graphics.RecordingCanvas;
import android.graphics.RenderNode;

/**
 * No-op ThreadedRenderer stub for Nova software rendering path.
 */
public final class ThreadedRenderer extends HardwareRenderer {
    public ThreadedRenderer(Context context, boolean translucent, String name) {
        super(translucent);
    }

    public RecordingCanvas beginRecording(int width, int height) {
        return null;
    }

    public void endRecording() {
    }

    public void drawRenderNode(RenderNode renderNode) {
    }

    public void registerAnimatingRenderNode(RenderNode node) {
    }

    public void setLightCenter(float lightX, float lightY, float lightZ) {
    }

    public void setOpaque(boolean opaque) {
    }

    public void setEnabled(boolean enabled) {
    }

    public boolean isEnabled() {
        return false;
    }

    public static ThreadedRenderer create(Context context, boolean translucent, String name) {
        return new ThreadedRenderer(context, translucent, name);
    }
}
