package com.jimbo.mycrop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * 不规则的四边形裁剪
 * <p/>
 * Created by jimbo on 15-12-5.
 */
public class CropImageView extends ImageView {

    private final static String TAG = "jimbo.CropImageView";

    private static final int ERROR_HIGHLIGHT_COLOR = 0xFFFFFF00;
    private static final int DEFAULT_HIGHLIGHT_COLOR = 0xFF00FFFF;
    private static final float CRICLE_RADIUS_DP = 13F;
    private static final float OUTLINE_DP = 2F;

    //事件区别
    private static final int EVENT_NONE             = 1;
    private static final int EVENT_LEFT_TOP         = 1 << 1;
    private static final int EVENT_RIGHT_TOP        = 1 << 2;
    private static final int EVENT_LEFT_BOTTOM      = 1 << 3;
    private static final int EVENT_RIGHT_BOTTOM     = 1 << 4;
    private static final int EVENT_MOVE_LEFT        = 1 << 5;
    private static final int EVENT_MOVE_RIGHT       = 1 << 6;
    private static final int EVENT_MOVE_TOP         = 1 << 7;
    private static final int EVENT_MOVE_BOTTOM      = 1 << 8;

    //记录事件
    private int mMotionEvent = EVENT_NONE;

    //图片缩放 viewWidth/bitmapWidth viewHeight/bitmapHeight
    private float mVaryScale;
    //图片四个顶点 被转化之后
    private android.graphics.Point mLeftTopOrigin;
    private android.graphics.Point mRightTopOrigin;
    private android.graphics.Point mLeftBottomOrigin;
    private android.graphics.Point mRightBottomOrigin;
    //顶点圆的画笔
    private Paint mCriclePaint;
    private Paint mWaiCrilePaint;
    //四边形区域的画笔
    private Paint mQuadrilateralPaint;
    //图片矩形
    private RectF mImageRectF;
    //顶点半径
    //private float pointRadius;

    private Context viewContext;

    //屏幕上的点
    private Point mLeftTopScreen;
    private Point mRightTopScreen;
    private Point mLeftBottomScreen;
    private Point mRightBottomScreen;
    private Point mLeftScreen;
    private Point mRightScreen;
    private Point mTopScreen;
    private Point mBottomScreen;

    private float lastX;
    private float lastY;

    private float currentX;
    private float currentY;

    private boolean isSetPoints = false;

    private Bitmap mBitmap;

    //放大镜效果
    private Path mMagnifierPath;
    private Matrix mMatrix;

    private static final float MAGNIFIER_RADIUS = 80F;
    private static final float MAGNIFIER_MULTIPLE = 2F;

    private boolean isShowMagnifier = false;

    public CropImageView(Context context) {
        super(context);
        this.viewContext = context;
        init();
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.viewContext = context;
        init();
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.viewContext = context;
        init();
    }

    /***
     * 在调用setImageBitmap()之前必须先调用此方法来初始化顶点
     * 可能会抛出参数不合法的异常
     *
     * @param points 初始的四个顶点的位置
     */
    public void setPoints(android.graphics.Point[] points) {
        isSetPoints = true;
        if (null == points || points.length != 4) {
            throw new IllegalArgumentException("your points array is illegal," +
                    "it is should be four member");
        }
        mLeftTopOrigin = points[0];
        mRightTopOrigin = points[1];
        mLeftBottomOrigin = points[2];
        mRightBottomOrigin = points[3];
    }

    /**
     * 返回经过用户调整后的坐标值
     *
     * @return 四个顶点值
     */
    public android.graphics.Point[] getPoints() throws Exception {

        if (!isSetPoints) {
            throw new Exception("your should use getPoints() before setPoints()");
        }

        if (null == mLeftTopScreen) {
            return null;
        }
        android.graphics.Point[] points = new android.graphics.Point[4];
        points[0] = new android.graphics.Point(
                Math.round((mLeftTopScreen.x - mImageRectF.left - getPaddingLeft()) / mVaryScale),
                Math.round((mLeftTopScreen.y - mImageRectF.top - getPaddingTop()) / mVaryScale)
        );
        points[2] = new android.graphics.Point(
                Math.round((mLeftBottomScreen.x - mImageRectF.left - getPaddingLeft()) / mVaryScale),
                Math.round((mLeftBottomScreen.y - mImageRectF.top - getPaddingTop()) / mVaryScale)
        );
        points[1] = new android.graphics.Point(
                Math.round((mRightTopScreen.x - mImageRectF.left - getPaddingLeft()) / mVaryScale),
                Math.round((mRightTopScreen.y - mImageRectF.top - getPaddingTop()) / mVaryScale)
        );
        points[3] = new android.graphics.Point(
                Math.round((mRightBottomScreen.x - mImageRectF.left - getPaddingLeft()) / mVaryScale),
                Math.round((mRightBottomScreen.y - mImageRectF.top - getPaddingTop()) / mVaryScale)
        );
        return points;
    }

    /**
     *
     * @return 是否可以裁剪
     */
    public boolean isRightStatus() {
        return isNoCrop();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mVaryScale = calculateScale(right-left-getPaddingLeft()-getPaddingRight(),
                bottom-top-getPaddingTop()-getPaddingBottom());
        if (null == mBitmap) {
            mBitmap = ((BitmapDrawable)getDrawable()).getBitmap();
        }
        //Log.d(TAG, "缩放比率为:" + mVaryScale);
        if (null == mImageRectF) {
            mImageRectF = getBitmapRectF();
        }
        print("left:"+mImageRectF.left+",top"+mImageRectF.top
            +",width"+mImageRectF.width()+",height:"+mImageRectF.height());

        print("paddingLeft"+getPaddingLeft()+",paddingTop"+getPaddingTop());

        if (!isSetPoints) {
            //
            mLeftTopOrigin = new android.graphics.Point(0, 0);
            mRightTopOrigin = new android.graphics.Point(mBitmap.getWidth(),0);
            mLeftBottomOrigin = new android.graphics.Point(0, mBitmap.getHeight());
            mRightBottomOrigin = new android.graphics.Point(mBitmap.getWidth(), mBitmap.getHeight());
            isSetPoints = true;
        }

        //print("bitmap--left:" + mImageRectF.left + ",top:" + mImageRectF.top);

        if (null == mLeftBottomScreen) {
            float leftPadding = mImageRectF.left + getPaddingLeft();
            float topPadding = mImageRectF.top + getPaddingTop();
            //计算缩放
            mLeftTopScreen = new Point(mLeftTopOrigin, mVaryScale,
                    leftPadding, topPadding);
            mRightTopScreen = new Point(mRightTopOrigin, mVaryScale,
                    leftPadding, topPadding);
            mLeftBottomScreen = new Point(mLeftBottomOrigin, mVaryScale,
                    leftPadding, topPadding);
            mRightBottomScreen = new Point(mRightBottomOrigin, mVaryScale,
                    leftPadding, topPadding);

            print("初始用户操作点:left:"+mLeftTopScreen.x+",right:"+mRightTopScreen.x
                +",top:"+mLeftTopScreen.y+",bottom:"+mLeftBottomScreen.y);

            mLeftScreen = calculateCenterPoint(mLeftTopScreen, mLeftBottomScreen);
            mRightScreen = calculateCenterPoint(mRightTopScreen, mRightBottomScreen);
            mTopScreen = calculateCenterPoint(mLeftTopScreen, mRightTopScreen);
            mBottomScreen = calculateCenterPoint(mLeftBottomScreen, mRightBottomScreen);
            print("x:" + mLeftBottomOrigin.x + ",y:" + mLeftBottomOrigin.y);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        drawCroper(canvas);
        if (isShowMagnifier) {
            drawMagnifier(canvas, 0, 0);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        setRightPaint();
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                mMotionEvent = getEvent(event.getX(), event.getY());
                //print("事件为:" + mMotionEvent);
                currentX = lastX = event.getX();
                currentY = lastY = event.getY();
               //sShowMagnifier = true;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                //isShowMagnifier = false;
                mMotionEvent = EVENT_NONE;
                if (isNoCrop()) {
                    setErrorPaint();
                    invalidate();
                    Toast.makeText(getContext(), "不能裁剪", Toast.LENGTH_SHORT).show();
//                    setRightPaint();
                } else {
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                currentX = event.getX();
                currentY = event.getY();
                float x = event.getX() - lastX;
                float y = event.getY() - lastY;
                if (mMotionEvent == 1) {
                    return true;
                }
                print("x:"+x+", y:"+y);
                movePoint(x, y);

                lastX = event.getX();
                lastY = event.getY();
                break;
        }

        return true;
    }

    private void drawMagnifier(Canvas canvas, float x, float y) {
        //if (mLeftTopScreen.x - MAGNIFIER_RADIUS - 10F < mImageRectF)
//        canvas.translate(lastX-MAGNIFIER_RADIUS, lastY-MAGNIFIER_RADIUS);
        canvas.translate(0, 0);
        canvas.clipPath(mMagnifierPath);
//        canvas.translate(0,
//                0);
//        canvas.translate(MAGNIFIER_RADIUS-mLeftTopScreen.x*MAGNIFIER_MULTIPLE,
//                MAGNIFIER_RADIUS-mLeftTopScreen.y*MAGNIFIER_MULTIPLE);
        canvas.translate(0+mLeftTopScreen.x,0+mLeftTopScreen.y);
        canvas.drawBitmap(mBitmap, mMatrix, null);
    }

    //边界检测
    private boolean isOverring(float x, float y) {

        if ((mMotionEvent & EVENT_LEFT_TOP) != 0) {
            if (checkPointOver(mLeftTopScreen, x, y)) {
                return true;
            }
        } else if ((mMotionEvent & EVENT_LEFT_BOTTOM) != 0) {
            if (checkPointOver(mLeftBottomScreen, x, y)) {
                return true;
            }
        } else if ((mMotionEvent & EVENT_RIGHT_BOTTOM) != 0) {
            if (checkPointOver(mRightBottomScreen, x, y)) {
                return true;
            }
        } else if ((mMotionEvent & EVENT_RIGHT_TOP) != 0) {
            if (checkPointOver(mRightTopScreen, x, y)) {
                return true;
            }
        } else if ((mMotionEvent & EVENT_MOVE_LEFT) != 0) {
            if (checkPointOver(mLeftTopScreen, mLeftBottomScreen, x, y)) {
                return true;
            }
        } else if ((mMotionEvent & EVENT_MOVE_RIGHT) != 0) {
            if (checkPointOver(mRightBottomScreen, mRightTopScreen, x, y)) {
                return true;
            }
        } else if ((mMotionEvent & EVENT_MOVE_TOP) != 0) {
            if (checkPointOver(mLeftTopScreen, mRightTopScreen, x, y)) {
                return true;
            }
        } else if ((mMotionEvent & EVENT_MOVE_BOTTOM) != 0) {
            if (checkPointOver(mLeftBottomScreen, mRightBottomScreen, x, y)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkPointOver(Point p, float x, float y) {
        return p.x + x <= mImageRectF.left || p.x + x >= mImageRectF.right
                || p.y + y <= mImageRectF.top || p.y + y >= mImageRectF.bottom;
    }

    private boolean checkPointOver(Point p1, Point p2, float x, float y) {
        return p1.x + x <= mImageRectF.left || p1.x + x >= mImageRectF.right
                || p1.y + y <= mImageRectF.top || p1.y + y >= mImageRectF.bottom
                || p2.x + x <= mImageRectF.left || p2.x + x >= mImageRectF.right
                || p2.y + y <= mImageRectF.top || p2.y + y >= mImageRectF.bottom;
    }

    //做一些初始化工作 初始画笔等等
    private void init() {
        //圆画笔初始化
        mCriclePaint = new Paint();
        mCriclePaint.setColor(DEFAULT_HIGHLIGHT_COLOR);
        mCriclePaint.setStyle(Paint.Style.FILL);
        mCriclePaint.setAlpha(90);
//        mCriclePaint.setARGB(125, 50, 50, 50);
        mCriclePaint.setAntiAlias(true);

        mWaiCrilePaint = new Paint();
        mWaiCrilePaint.setStyle(Paint.Style.STROKE);
        mWaiCrilePaint.setStrokeWidth(6F);
        mWaiCrilePaint.setColor(DEFAULT_HIGHLIGHT_COLOR);
        mWaiCrilePaint.setAntiAlias(true);

        //四边画笔初始化
        mQuadrilateralPaint = new Paint();
//        mQuadrilateralPaint.setARGB(125, 50, 50, 50);
        mQuadrilateralPaint.setColor(DEFAULT_HIGHLIGHT_COLOR);
        mQuadrilateralPaint.setStyle(Paint.Style.STROKE);
        mQuadrilateralPaint.setAntiAlias(true);
        mQuadrilateralPaint.setStrokeWidth(dpToPx(OUTLINE_DP));

        //pointRadius = dpToPx(CRICLE_RADIUS_DP)*4F;
        //放大镜效果初始化
        mMagnifierPath = new Path();
        mMagnifierPath.addCircle(MAGNIFIER_RADIUS, MAGNIFIER_RADIUS,
                MAGNIFIER_RADIUS, Path.Direction.CW);
        mMatrix = new Matrix();
        mMatrix.setScale(MAGNIFIER_MULTIPLE, MAGNIFIER_MULTIPLE);
    }

    private void setErrorPaint() {
        mQuadrilateralPaint.setColor(ERROR_HIGHLIGHT_COLOR);
    }

    private void setRightPaint() {
        mQuadrilateralPaint.setColor(DEFAULT_HIGHLIGHT_COLOR);
    }

    private void movePoint(float x, float y) {
        print("last-x:" + mLeftTopScreen.x + ",y:" + mLeftTopScreen.y);
        if ((mMotionEvent & EVENT_LEFT_TOP) != 0) {
            mLeftTopScreen.x += x;
            mLeftTopScreen.y += y;
            maintainIn(mLeftTopScreen);
            mLeftScreen = calculateCenterPoint(mLeftTopScreen, mLeftBottomScreen);
            mTopScreen = calculateCenterPoint(mLeftTopScreen, mRightTopScreen);
        } else if ((mMotionEvent & EVENT_LEFT_BOTTOM) != 0) {
            mLeftBottomScreen.x += x;
            mLeftBottomScreen.y += y;
            maintainIn(mLeftBottomScreen);
            mBottomScreen = calculateCenterPoint(mLeftBottomScreen, mRightBottomScreen);
            mLeftScreen = calculateCenterPoint(mLeftBottomScreen, mLeftTopScreen);
        } else if ((mMotionEvent & EVENT_RIGHT_BOTTOM) != 0) {
            mRightBottomScreen.x += x;
            mRightBottomScreen.y += y;
            maintainIn(mRightBottomScreen);
            mRightScreen = calculateCenterPoint(mRightBottomScreen, mRightTopScreen);
            mBottomScreen = calculateCenterPoint(mRightBottomScreen, mLeftBottomScreen);
        } else if ((mMotionEvent & EVENT_RIGHT_TOP) != 0) {
            mRightTopScreen.x += x;
            mRightTopScreen.y += y;
            maintainIn(mRightTopScreen);
            mTopScreen = calculateCenterPoint(mRightTopScreen, mLeftTopScreen);
            mRightScreen = calculateCenterPoint(mRightTopScreen, mRightBottomScreen);
        } else if ((mMotionEvent & EVENT_MOVE_LEFT) != 0) {
            float tan = getTan(mLeftTopScreen, mRightTopScreen);
            mLeftTopScreen.x += x;
            mLeftTopScreen.y += x*tan;
            tan = getTan(mLeftBottomScreen, mRightBottomScreen);
            mLeftBottomScreen.x += x;
            mLeftBottomScreen.y += x*tan;
            maintainIn(mLeftTopScreen);
            maintainIn(mLeftBottomScreen);
            mLeftScreen = calculateCenterPoint(mLeftTopScreen, mLeftBottomScreen);
            mTopScreen = calculateCenterPoint(mLeftTopScreen, mRightTopScreen);
            mBottomScreen = calculateCenterPoint(mRightBottomScreen, mLeftBottomScreen);
        } else if ((mMotionEvent & EVENT_MOVE_RIGHT) != 0) {
//            mRightScreen.x += x;
//            mRightScreen.y += y;
            float tan = getTan(mRightTopScreen, mLeftTopScreen);
            mRightTopScreen.x += x;
            mRightTopScreen.y += x*tan;
            tan = getTan(mRightBottomScreen, mLeftBottomScreen);
            mRightBottomScreen.x += x;
            mRightBottomScreen.y += x*tan;
            maintainIn(mRightTopScreen);
            maintainIn(mRightBottomScreen);
            mRightScreen = calculateCenterPoint(mRightTopScreen, mRightBottomScreen);
            mTopScreen = calculateCenterPoint(mLeftTopScreen, mRightTopScreen);
            mBottomScreen = calculateCenterPoint(mRightBottomScreen, mLeftBottomScreen);
        } else if ((mMotionEvent & EVENT_MOVE_TOP) != 0) {
//            mTopScreen.x += x;
//            mTopScreen.y += y;
            float tan = getTan(mLeftBottomScreen, mLeftTopScreen);
            mLeftTopScreen.x += y/tan;
            mLeftTopScreen.y += y;
            tan = getTan(mRightBottomScreen, mRightTopScreen);
            mRightTopScreen.x += y/tan;
            mRightTopScreen.y += y;
            maintainIn(mLeftTopScreen);
            maintainIn(mRightTopScreen);
            mTopScreen = calculateCenterPoint(mLeftTopScreen, mRightTopScreen);
            mLeftScreen = calculateCenterPoint(mLeftBottomScreen, mLeftTopScreen);
            mRightScreen = calculateCenterPoint(mRightBottomScreen, mRightTopScreen);
        } else if ((mMotionEvent & EVENT_MOVE_BOTTOM) != 0) {
//            mBottomScreen.x += x;
//            mBottomScreen.y += y;
            float tan = getTan(mLeftBottomScreen, mLeftTopScreen);
            mLeftBottomScreen.x += y/tan;
            mLeftBottomScreen.y += y;
            tan = getTan(mRightBottomScreen, mRightTopScreen);
            mRightBottomScreen.x += y/tan;
            mRightBottomScreen.y += y;
            maintainIn(mLeftBottomScreen);
            maintainIn(mRightBottomScreen);
            mBottomScreen = calculateCenterPoint(mLeftBottomScreen, mRightBottomScreen);
            mLeftScreen = calculateCenterPoint(mLeftBottomScreen, mLeftTopScreen);
            mRightScreen = calculateCenterPoint(mRightBottomScreen, mRightTopScreen);
        }
        print("now-x:" + mLeftTopScreen.x + ",y:" + mLeftTopScreen.y);
        invalidate();
    }

    private void maintainIn(Point p) {
        if (p.x < mImageRectF.left+getPaddingLeft()) {
            p.x = mImageRectF.left+getPaddingLeft();
        }
        if (p.x > mImageRectF.right+getPaddingLeft()) {
            p.x = mImageRectF.right+getPaddingLeft();
        }
        if (p.y < mImageRectF.top+getPaddingTop()) {
            p.y = mImageRectF.top+getPaddingTop();
        }
        if (p.y > mImageRectF.bottom+getPaddingTop()) {
            p.y = mImageRectF.bottom+getPaddingTop();
        }
    }

    private boolean isNoCrop() {
        return mLeftTopScreen.x > mRightTopScreen.x
                || mLeftTopScreen.y > mLeftBottomScreen.y
                || mRightTopScreen.y > mRightBottomScreen.y;
    }

    private float getTan(Point p1, Point p2) {
        return (p2.y - p1.y) / (p2.x - p1.x);
    }

    private int getEvent(float x, float y) {
        int event = 1;

        print("event-x:" + x + ",y:" + y);
        if (isEvent(mLeftTopScreen, x, y)) {
            event |= EVENT_LEFT_TOP;
        }
        if (isEvent(mLeftBottomScreen, x, y)) {
            event |= EVENT_LEFT_BOTTOM;
        }
        if (isEvent(mRightTopScreen, x, y)) {
            event |= EVENT_RIGHT_TOP;
        }
        if (isEvent(mRightBottomScreen, x, y)) {
            event |= EVENT_RIGHT_BOTTOM;
        }

        if (isEvent(mLeftScreen, x, y)) {
            event |= EVENT_MOVE_LEFT;
            isShowMagnifier = false;
        }
        if (isEvent(mRightScreen, x, y)) {
            event |= EVENT_MOVE_RIGHT;
            isShowMagnifier = false;
        }
        if (isEvent(mTopScreen, x, y)) {
            event |= EVENT_MOVE_TOP;
            isShowMagnifier = false;
        }
        if (isEvent(mBottomScreen, x, y)) {
            event |= EVENT_MOVE_BOTTOM;
            isShowMagnifier = false;
        }

        return event;
    }

    private boolean isEvent(Point p, float x, float y) {
        float hysteresis = dpToPx(20F);
        return Math.abs(p.x - x) < hysteresis &&
                Math.abs(p.y - y) < hysteresis;
    }

    private void drawCroper(Canvas canvas) {

        canvas.drawCircle(mLeftTopScreen.x,
                mLeftTopScreen.y, dpToPx(CRICLE_RADIUS_DP)+2,
                mWaiCrilePaint);
        canvas.drawCircle(mRightTopScreen.x,
                mRightTopScreen.y, dpToPx(CRICLE_RADIUS_DP)+2,
                mWaiCrilePaint);
        canvas.drawCircle(mLeftBottomScreen.x,
                mLeftBottomScreen.y, dpToPx(CRICLE_RADIUS_DP)+2,
                mWaiCrilePaint);
        canvas.drawCircle(mRightBottomScreen.x,
                mRightBottomScreen.y, dpToPx(CRICLE_RADIUS_DP)+2,
                mWaiCrilePaint);

        canvas.drawCircle(mLeftTopScreen.x,
                mLeftTopScreen.y, dpToPx(CRICLE_RADIUS_DP),
                mCriclePaint);
        canvas.drawCircle(mRightTopScreen.x,
                mRightTopScreen.y, dpToPx(CRICLE_RADIUS_DP),
                mCriclePaint);
        canvas.drawCircle(mLeftBottomScreen.x,
                mLeftBottomScreen.y, dpToPx(CRICLE_RADIUS_DP),
                mCriclePaint);
        canvas.drawCircle(mRightBottomScreen.x,
                mRightBottomScreen.y, dpToPx(CRICLE_RADIUS_DP),
                mCriclePaint);

        canvas.drawCircle(mLeftScreen.x, mLeftScreen.y, dpToPx(CRICLE_RADIUS_DP)+2,
                mWaiCrilePaint);
        canvas.drawCircle(mRightScreen.x, mRightScreen.y, dpToPx(CRICLE_RADIUS_DP)+2,
                mWaiCrilePaint);
        canvas.drawCircle(mTopScreen.x, mTopScreen.y, dpToPx(CRICLE_RADIUS_DP)+2,
                mWaiCrilePaint);
        canvas.drawCircle(mBottomScreen.x, mBottomScreen.y, dpToPx(CRICLE_RADIUS_DP)+2,
                mWaiCrilePaint);

        canvas.drawCircle(mLeftScreen.x, mLeftScreen.y, dpToPx(CRICLE_RADIUS_DP),
                mCriclePaint);
        canvas.drawCircle(mRightScreen.x, mRightScreen.y, dpToPx(CRICLE_RADIUS_DP),
                mCriclePaint);
        canvas.drawCircle(mTopScreen.x, mTopScreen.y, dpToPx(CRICLE_RADIUS_DP),
                mCriclePaint);
        canvas.drawCircle(mBottomScreen.x, mBottomScreen.y, dpToPx(CRICLE_RADIUS_DP),
                mCriclePaint);

        canvas.drawLine(mLeftTopScreen.x, mLeftTopScreen.y,
                mLeftBottomScreen.x, mLeftBottomScreen.y,
                mQuadrilateralPaint);
        canvas.drawLine(mLeftTopScreen.x, mLeftTopScreen.y,
                mRightTopScreen.x, mRightTopScreen.y,
                mQuadrilateralPaint);
        canvas.drawLine(mRightBottomScreen.x, mRightBottomScreen.y,
                mLeftBottomScreen.x, mLeftBottomScreen.y,
                mQuadrilateralPaint);
        canvas.drawLine(mRightBottomScreen.x, mRightBottomScreen.y,
                mRightTopScreen.x, mRightTopScreen.y,
                mQuadrilateralPaint);
    }

    private Point calculateCenterPoint(Point p1, Point p2) {
        return new Point((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
    }

    //计算图片缩放的比例
    private float calculateScale(int viewWidth, int viewHeight) {
        final Drawable drawable = getDrawable();
        if (null == drawable) {
            return -1F;
        }
        final int bitmapWidth = drawable.getIntrinsicWidth();
        final int bitmapHeight = drawable.getIntrinsicHeight();

        print("viewWidth:" + viewWidth + ",bitmapWidth:" + bitmapWidth);
        print("viewHeight:" + viewHeight + ",bitmapHeight:" + bitmapHeight);

        return (float) viewWidth / bitmapWidth < (float) viewHeight / bitmapHeight ?
                (float) viewWidth / bitmapWidth : (float) viewHeight / bitmapHeight;
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

    private float dpToPx(float dp) {
        return dp * viewContext.getResources().getDisplayMetrics().density;
    }

    private void print(String s) {
        Log.d(TAG, s);
    }
}
