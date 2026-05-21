package android.graphics;

public class Matrix {
    public static final int MSCALE_X = 0;
    public static final int MSKEW_X  = 1;
    public static final int MTRANS_X = 2;
    public static final int MSKEW_Y  = 3;
    public static final int MSCALE_Y = 4;
    public static final int MTRANS_Y = 5;
    public static final int MPERSP_0 = 6;
    public static final int MPERSP_1 = 7;
    public static final int MPERSP_2 = 8;

    private final float[] mValues = new float[9];

    public Matrix() { reset(); }
    public Matrix(Matrix src) { set(src); }

    public void reset() {
        mValues[0]=1; mValues[1]=0; mValues[2]=0;
        mValues[3]=0; mValues[4]=1; mValues[5]=0;
        mValues[6]=0; mValues[7]=0; mValues[8]=1;
    }
    public void set(Matrix src) { if (src != null) System.arraycopy(src.mValues, 0, mValues, 0, 9); }
    public boolean isIdentity() { return mValues[0]==1&&mValues[4]==1&&mValues[8]==1&&mValues[1]==0&&mValues[2]==0&&mValues[3]==0&&mValues[5]==0&&mValues[6]==0&&mValues[7]==0; }
    public void getValues(float[] values) { System.arraycopy(mValues, 0, values, 0, 9); }
    public void setValues(float[] values) { System.arraycopy(values, 0, mValues, 0, 9); }
    public boolean preTranslate(float dx, float dy) { return true; }
    public boolean postTranslate(float dx, float dy) { return true; }
    public boolean preScale(float sx, float sy) { return true; }
    public boolean postScale(float sx, float sy) { return true; }
    public boolean preRotate(float degrees) { return true; }
    public boolean postRotate(float degrees) { return true; }
    public void setTranslate(float dx, float dy) { reset(); mValues[2]=dx; mValues[5]=dy; }
    public void setScale(float sx, float sy) { reset(); mValues[0]=sx; mValues[4]=sy; }
    public void setScale(float sx, float sy, float px, float py) { reset(); mValues[0]=sx; mValues[4]=sy; mValues[2]=px*(1-sx); mValues[5]=py*(1-sy); }
    public void setRotate(float degrees) {}
    public void setRotate(float degrees, float px, float py) {}
    public void setSkew(float kx, float ky) {}
    public void setSkew(float kx, float ky, float px, float py) {}
    public void setConcat(Matrix a, Matrix b) {}
    public boolean invert(Matrix inverse) { return false; }
    public boolean mapRect(RectF rect) { return true; }
    public boolean mapRect(RectF dst, RectF src) { if (dst != null && src != null) { dst.set(src); } return true; }
    public void mapPoints(float[] pts) {}
    public void mapPoints(float[] dst, float[] src) { if (dst != null && src != null) System.arraycopy(src, 0, dst, 0, Math.min(dst.length, src.length)); }
    public float mapRadius(float radius) { return radius; }
    public boolean setRectToRect(RectF src, RectF dst, ScaleToFit stf) { return true; }
    public boolean preConcat(Matrix other) { return true; }
    public boolean postConcat(Matrix other) { return true; }
    public boolean preSkew(float kx, float ky) { return true; }
    public boolean postSkew(float kx, float ky) { return true; }
    public enum ScaleToFit { FILL, START, CENTER, END }
}
