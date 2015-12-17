package com.jimbo.mycrop;

import android.content.Context;
import android.graphics.*;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 *
 * Created by jimbo on 15-12-15.
 */
public class ShowResultView extends ImageView {
    //记录传过来的坐标点
    private Point[] mPoints;
    private com.jimbo.mycrop.Point[] mDrawPoints;
    //矩形画笔
    private Paint mRectanglePaint;
    //imageView的padding值
    private float mLeftPadding = 0;
    private float mRightPadding = 0;
    private float mTopPadding = 0;
    private float mBottomPadding = 0;
    //rectangle color
    private static final int mRectangleColor = 0xFFED122E;

    private RectF mRectF;

    //缩放比率 viewWidth/bitmapWidth
    private float mScale = -1F;

    public ShowResultView(Context context) {
        super(context);
        init();
    }

    public ShowResultView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ShowResultView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 设置矩形的坐标点集合
     * @param points 坐标点集合
     */
    public void setPoints(Point[] points) {
        mPoints = points;
    }

    private void init() {
        mRectanglePaint = new Paint();
        mRectanglePaint.setColor(mRectangleColor);
        mRectanglePaint.setStrokeWidth(6F);
        mRectanglePaint.setStyle(Paint.Style.STROKE);
        mRectanglePaint.setAntiAlias(true);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mLeftPadding = getPaddingLeft();
        mRightPadding = getPaddingRight();
        mTopPadding = getPaddingTop();
        mBottomPadding = getPaddingBottom();

        if (null == mRectF) {
            mRectF = getBitmapRectF();
            mScale = calculateScale(right-left-mLeftPadding-mRightPadding,
                    bottom-top-mTopPadding-mBottomPadding);
            System.out.println("view--scale:"+mScale);
            calculatePoint();
        }
    }

    static int ii = 0;
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        ii++;
        System.out.println("view--"+ii);
        RectF[] rs = getRectF();
        if (null == rs) {
            return;
        }
        if (rs[0] == null) {
            return;
        }
        for (RectF r : rs) {
            canvas.drawRect(r, mRectanglePaint);
            System.out.println("view--"+r.left+","+r.right+","+r.top+","+r.bottom);

        }
        //canvas.drawRect(new RectF(0F, 0F, 100F, 100F), mRectanglePaint);
    }

    private RectF[] getRectF() {
        RectF[] rectFs = new RectF[mPoints.length/4];
        if (mDrawPoints == null) {
            return null;
        }
        for (int i = 0; i < mDrawPoints.length/4; i++) {
            rectFs[i] = new RectF(mDrawPoints[i*4].x, mDrawPoints[i*4].y,
                    mDrawPoints[i*4+3].x, mDrawPoints[i*4+3].y);
            System.out.println("view--"+i*4+","+(i*4+2));
        }
        return rectFs;
    }

    /**
     * Gets the bounding rectangle of the bitmap within the ImageView.
     */
    private RectF getBitmapRectF() {

        final Drawable drawable = getDrawable();
        if (drawable == null) {
            return new RectF();
        }

        // Get image matrix values and place them in an array.
        final float[] matrixValues = new float[9];
        getImageMatrix().getValues(matrixValues);

        // Extract the scale and translation values from the matrix.
        final float scaleX = matrixValues[Matrix.MSCALE_X];
        final float scaleY = matrixValues[Matrix.MSCALE_Y];
        final float transX = matrixValues[Matrix.MTRANS_X];
        final float transY = matrixValues[Matrix.MTRANS_Y];

        // Get the width and height of the original bitmap.
        final int drawableIntrinsicWidth = drawable.getIntrinsicWidth();
        final int drawableIntrinsicHeight = drawable.getIntrinsicHeight();

        // Calculate the dimensions as seen on screen.
        final int drawableDisplayWidth = Math.round(drawableIntrinsicWidth * scaleX);
        final int drawableDisplayHeight = Math.round(drawableIntrinsicHeight * scaleY);

        // Get the Rect of the displayed image within the ImageView.
        final float left = Math.max(transX, 0);
        final float top = Math.max(transY, 0);
        final float right = Math.min(left + drawableDisplayWidth, getWidth());
        final float bottom = Math.min(top + drawableDisplayHeight, getHeight());

        return new RectF(left, top, right, bottom);
    }

    private void calculatePoint() {
        mDrawPoints = new com.jimbo.mycrop.Point[mPoints.length];
        for (int i = 0; i < mPoints.length; i++) {
            mDrawPoints[i] = new com.jimbo.mycrop.Point(
                    mPoints[i],
                    mScale,
                    mRectF.left+mLeftPadding,
                    mRectF.top+mRightPadding
            );
        }
    }

    //计算图片缩放的比例
    private float calculateScale(float viewWidth, float viewHeight) {
        final Drawable drawable = getDrawable();
        if (null == drawable) {
            return -1F;
        }
        final int bitmapWidth = drawable.getIntrinsicWidth();
        final int bitmapHeight = drawable.getIntrinsicHeight();

        return viewWidth / bitmapWidth < viewHeight / bitmapHeight ?
                viewWidth / bitmapWidth : viewHeight / bitmapHeight;
    }
}
