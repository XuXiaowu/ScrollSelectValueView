package mirahome.customapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.WindowManager;
import android.widget.EdgeEffect;
import android.widget.OverScroller;

import com.blankj.ALog;

import java.lang.ref.WeakReference;

/**
 * Created by xuxiaowu on 2017/10/30.
 */

public class SelectValueView extends View {

    private static final int WHAT_RESET_SCROLL = 1;

    /**
     * 默认值
     **/
    private static final int MIN_VALUE = 40;
    private static final int MAX_VALUE = 100; //
    private static final int SCALE_SPACE = 30; //刻度间距
    private static final int SMALL_SCALE_WIDTH = 2; //小刻度先宽度
    private static final int BIG_SCALE_WIDTH = 4; //大刻度先宽度
    private static final int SMALL_SCALE_HEIGHT = 60; //小刻度线高度
    private static final int BIG_SCALE_HEIGHT = 120; //大刻度线高度
    private static final int SCALE_TEXT_MARGIN_TOP = 20; //刻度文字距刻度线的间距
    private static final int SCALE_TEXT_SIZE = 30; //刻度字体大小

    private int mMoveX;
    private int mLastX;
    private int mWidth;
    private int mHeight;
    private int mFlingMinX;
    private int mFlingMaxX;
    private int mContentWidth;
    private int mScaleColor; //刻度线颜色
    private int mScaleTextColor; //刻度文字颜色
    private int mMinValue; //最大值
    private int mMaxValue; //最小值

    private float mStartX = 0;//按下时y值
    private float mCurrentValue;
    private float mSelectValue;
    private float mSmallScaleWidth; //小刻度线宽度
    private float mBigScaleWidth; //大刻度线宽度
    private float mScaleSpaceWidth; //刻度宽度

    private Paint mScaleSmallLinePaint; //刻度线画笔
    private Paint mScaleBigLinePaint; //刻度线画笔
    private Paint mScalePaintText; //刻度文字画笔
    private VelocityTracker mVelocityTracker;
    private EdgeEffect mLeftEdgeEffect;
    private EdgeEffect mRightEdgeEffect;
    private OverScroller mScroller;

    private ValueSelectListener mValueSelectListener;

    private boolean mAutoSmoothScrollable;

    public SelectValueView(Context context) {
        this(context, null);
    }

    public SelectValueView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SelectValueView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        getAttribute(context, attrs);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        mFlingMinX = -mWidth / 2;
        resetScrollPosition();
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawScale(canvas);
        drawScaleText(canvas);
        drawEdgeEffect(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain(); //检查速度测量器，如果为null，获得一个
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mAutoSmoothScrollable = false;
                mStartX = event.getX();
                if (!mScroller.isFinished()) mScroller.abortAnimation();
                break;
            case MotionEvent.ACTION_MOVE:
                mVelocityTracker.addMovement(event);

                mMoveX = (int) (mStartX - event.getX()) + mLastX;
                mMoveX = mMoveX < mFlingMinX ? mFlingMinX : mMoveX;
                mMoveX = mMoveX > mFlingMaxX ? mFlingMaxX : mMoveX;
                scrollTo(mMoveX, 0);

                float currX = mMoveX + Math.abs(mFlingMinX);
                float valueF = currX / mScaleSpaceWidth / 10;
                float currentValue = (float) (Math.round(valueF * 10)) / 10 + mMinValue;
                ALog.e("mSelectValue-- " + currentValue);
                if (currentValue != mCurrentValue && mValueSelectListener != null) {
                    mCurrentValue = currentValue;
                    mValueSelectListener.onValueChangeListener(mCurrentValue);
                }

                if (mLastX == mFlingMinX) {
                    mLeftEdgeEffect.finish();
                    mLeftEdgeEffect.onPull(Math.abs(mLastX * 3));
                }
                if (mLastX == mFlingMaxX) {
                    mRightEdgeEffect.finish();
                    mRightEdgeEffect.onPull(Math.abs(mLastX * 3));
                }
                break;
            case MotionEvent.ACTION_UP:
                mAutoSmoothScrollable = true;
                mLastX = mMoveX;
                mVelocityTracker.computeCurrentVelocity(1000); //当手指立刻屏幕时，获得速度，作为fling的初始速度
                int initialVelocity = (int) mVelocityTracker.getXVelocity(); //获取滑动速度
                doFling(-initialVelocity);   // 由于坐标轴正方向问题，要加负号
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {

        mLastX = mScroller.getCurrX();
        float currX = mLastX + Math.abs(mFlingMinX);
        float valueF = currX / mScaleSpaceWidth / 10;
        float currentValue = (float) (Math.round(valueF * 10)) / 10 + mMinValue;
        int valueI = Math.round(valueF * 10);
        int moveX = (int) (valueI * mScaleSpaceWidth + mFlingMinX);
        if (currentValue != mCurrentValue && mValueSelectListener != null && mAutoSmoothScrollable) {
            mCurrentValue = currentValue;
            mValueSelectListener.onValueChangeListener(mCurrentValue);
        }

        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
            int oldX = getScrollX();

            if (oldX == mFlingMinX) {
                mLeftEdgeEffect.finish();
                mLeftEdgeEffect.onPull(Math.abs(mLastX));
            }

            if (oldX == mFlingMaxX) {
                mRightEdgeEffect.finish();
                mRightEdgeEffect.onPull(Math.abs(mLastX));
            }

        } else {
            if (currentValue != mSelectValue && mValueSelectListener != null && mAutoSmoothScrollable) {
                mSelectValue = currentValue;
                mValueSelectListener.onValueSelectedListener(mSelectValue);
            }

            ALog.e("fk valueF" + currentValue);
            if (mAutoSmoothScrollable) {
                smoothScrollTo(moveX, 0);
                mAutoSmoothScrollable = false;
                ALog.e("fk value" + valueI);
            }
        }
    }

    private void getAttribute(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SelectValueView);
        mScaleColor = typedArray.getColor(R.styleable.SelectValueView_scaleColor, 0x5071717B);
        mScaleTextColor = typedArray.getColor(R.styleable.SelectValueView_scaleTextColor, 0x000000);
        mSmallScaleWidth = typedArray.getDimension(R.styleable.SelectValueView_smallScaleWidth, SMALL_SCALE_WIDTH);
        mBigScaleWidth = typedArray.getDimension(R.styleable.SelectValueView_bigScaleWidth, BIG_SCALE_WIDTH);
        mScaleSpaceWidth = typedArray.getDimension(R.styleable.SelectValueView_scaleSpaceWidth, SCALE_SPACE);
        mMinValue = typedArray.getInt(R.styleable.SelectValueView_minValue, MIN_VALUE);
        mMaxValue = typedArray.getInt(R.styleable.SelectValueView_maxValue, MAX_VALUE);
        typedArray.recycle();
    }

    private void init() {
        //小刻度线画笔
        mScaleSmallLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScaleSmallLinePaint.setStrokeWidth(mSmallScaleWidth);
        mScaleSmallLinePaint.setColor(mScaleColor);

        //大刻度线画笔
        mScaleBigLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScaleBigLinePaint.setStrokeWidth(mBigScaleWidth);
        mScaleBigLinePaint.setColor(Color.parseColor("#5071717B"));

        //刻度文字画笔
        mScalePaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScalePaintText.setTextSize(SCALE_TEXT_SIZE);
        mScalePaintText.setColor(mScaleTextColor);

        mScroller = new OverScroller(getContext());

        mContentWidth = (int) (mScaleSpaceWidth * 10 * (mMaxValue - mMinValue));
        mFlingMaxX = mContentWidth - getDisplayWidth() / 2;

        mLeftEdgeEffect = new EdgeEffect(getContext());
        mRightEdgeEffect = new EdgeEffect(getContext());

    }

    //缓慢滚动到指定位置
    private void smoothScrollTo(int destX, int destY) {
        int scrollX = getScrollX();
        int delta = destX - scrollX;
        mScroller.startScroll(scrollX, 0, delta, 0, 100);  //1000ms内滑动destX，效果就是慢慢滑动
        invalidate();
    }

    private void doFling(int speed) {
        if (mScroller == null) {
            return;
        }
        mScroller.fling(mLastX, 0, speed, 0, mFlingMinX, mFlingMaxX, 0, 0);
        invalidate();
    }

    public void setValueSelectListener(ValueSelectListener valueSelectListener) {
        mValueSelectListener = valueSelectListener;
    }

    private void drawScale(Canvas canvas) {
        int num = mMaxValue - mMinValue;
        for (int i = 0; i < num; i++) {
            float startX = i * mScaleSpaceWidth * 10;
            float stopX = i * mScaleSpaceWidth * 10;
            canvas.drawLine(startX, 0, stopX, BIG_SCALE_HEIGHT, mScaleBigLinePaint);

            for (int j = 1; j < 10; j++) {
                float x = startX + j * mScaleSpaceWidth;
                canvas.drawLine(x, 0, x, SMALL_SCALE_HEIGHT, mScaleSmallLinePaint);
            }
        }
    }

    private void drawScaleText(Canvas canvas) {

        for (int i = mMinValue; i < mMaxValue + 1; i++) {
            String text = String.valueOf(i);
            float textLength = mScaleSmallLinePaint.measureText(text);
            float x = (i - mMinValue) * mScaleSpaceWidth * 10 - textLength;
            float y = BIG_SCALE_HEIGHT + SCALE_TEXT_SIZE + SCALE_TEXT_MARGIN_TOP;
            canvas.drawText(text, x, y, mScalePaintText);
        }
    }

    private void drawEdgeEffect(Canvas canvas) {
        //绘制左边发光效果
        if (!mLeftEdgeEffect.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.translate(-mWidth / 2, 0);
            canvas.translate(mHeight / 2, mHeight / 2);
            canvas.rotate(-90);
            canvas.translate(-mHeight / 2, -mHeight / 2);
            mLeftEdgeEffect.setSize(mHeight, 300);
            if (mLeftEdgeEffect.draw(canvas)) {
                postInvalidateOnAnimation();
            }
            canvas.restoreToCount(restoreCount); //还原canvas
        }
        //绘制右边发光效果
        if (!mRightEdgeEffect.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.translate(mContentWidth, 0);
            canvas.rotate(90);
            canvas.translate(0, mFlingMinX);
            mRightEdgeEffect.setSize(mHeight, 300);
            if (mRightEdgeEffect.draw(canvas)) {
                postInvalidateOnAnimation();
            }
            canvas.restoreToCount(restoreCount); //还原canvas
        }
    }

    private int getDisplayWidth() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        return width;
    }

    private void resetScrollPosition() {
        scrollTo(mFlingMinX, 0);
        mScroller.startScroll(mFlingMinX, 0, 0, 0, 0);
    }

    public interface ValueSelectListener {

        /**
         * 值改变时的回调
         */
        void onValueChangeListener(float value);

        /**
         * 值选中时的回调
         */
        void onValueSelectedListener(float value);
    }

}
