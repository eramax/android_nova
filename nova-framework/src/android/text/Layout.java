package android.text;

public abstract class Layout {
    public enum Alignment { ALIGN_NORMAL, ALIGN_OPPOSITE, ALIGN_CENTER, ALIGN_LEFT, ALIGN_RIGHT }

    public static final int DIR_LEFT_TO_RIGHT = 1;
    public static final int DIR_RIGHT_TO_LEFT = -1;

    public static final Directions DIRS_ALL_LEFT_TO_RIGHT = new Directions(new int[]{0, 0x3FFFFFFF << 2});
    public static final Directions DIRS_ALL_RIGHT_TO_LEFT = new Directions(new int[]{0, (0x3FFFFFFF << 2) | 1});

    public static final class Directions {
        public final int[] mDirections;
        public Directions(int[] dirs) { mDirections = dirs; }
    }

    public abstract int getWidth();
    public abstract int getHeight();
    public abstract int getLineCount();
    public abstract int getLineTop(int line);
    public abstract int getLineDescent(int line);
    public abstract int getLineStart(int line);
    public abstract int getEllipsisStart(int line);
    public abstract int getEllipsisCount(int line);
    public abstract boolean getLineContainsTab(int line);
    public abstract Directions getLineDirections(int line);
    public abstract int getParagraphDirection(int line);

    public int getLineBottom(int line) { return getLineTop(line + 1); }
    public int getLineBaseline(int line) { return getLineBottom(line) - getLineDescent(line); }
    public float getLineMax(int line) { return getWidth(); }
    public float getLineWidth(int line) { return getWidth(); }
    public int getLineEnd(int line) {
        if (line + 1 >= getLineCount()) {
            CharSequence text = getText();
            return text != null ? text.length() : 0;
        }
        return getLineStart(line + 1);
    }

    public CharSequence getText() { return ""; }
    public TextPaint getPaint() { return new TextPaint(); }

    public void draw(android.graphics.Canvas canvas) {}
    public void draw(android.graphics.Canvas canvas, android.graphics.Path highlight,
            android.graphics.Paint highlightPaint, int cursorOffsetVertical) {}

    public int getLineForOffset(int offset) { return 0; }
    public float getLineLeft(int line) { return 0f; }
    public float getLineRight(int line) { return getWidth(); }
    public int getOffsetForHorizontal(int line, float horiz) { return 0; }
    public float getPrimaryHorizontal(int offset) { return 0f; }
}
