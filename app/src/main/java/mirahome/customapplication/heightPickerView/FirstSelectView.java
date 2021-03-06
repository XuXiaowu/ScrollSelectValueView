package mirahome.customapplication.heightPickerView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.EdgeEffect;
import android.widget.OverScroller;

import com.blankj.ALog;

import mirahome.customapplication.R;

/**
 * Created by xuxiaowu on 2017/11/13.
 */
public class FirstSelectView extends View {

    private static final int DEFAULT_HEIGHT = 600;
    private static final int DEFAULT_WIDTH = 300;
    private static final int MIN_VALUE = 1;
    private static final int TEXT_SIZE = 60; //字体大小
    private static final int TEXT_MARGIN = 40;
    private static final int METRIC_MIN_VALUE = 90; //公制最小值
    private static final int METRIC_MAX_VALUE = 240; //公制最大值
    private static final int INCH_MIN_VALUE = 3; //英制最小值
    private static final int INCH_MAX_VALUE = 7; //英制最大值
    private static final boolean IS_METRIC_UNIT = true; //是否公制单位

    private static final String[] INCH_VALUES = new String[]{"03'", "04'", "05'", "06'", "07'"}; //英制数据

    private float mStartY = 0;//按下时y值

    private int mSelectIndex; //选中的index
    private int mMoveY;
    private int mHeight;
    private int mWidth;
    private int mFlingMinY;
    private int mFlingMaxY;
    private int mLastY;
    private int mItemHeight; //item高度
    private int mTextHeight; //文字的高度
    private int mTextMargin;
    private int mSelectMetricValueIndex; //公制值的index
    private int mSelectInchValueIndex; //英制值的index
    private String[] mMetricValues; //公制数据
    private String[] mValues;

    private boolean mAutoSmoothScrollable;
    private boolean mIsMetricUnit = true; //是否公制单位

    private Paint mTextPaint;
    private VelocityTracker mVelocityTracker;
    private EdgeEffect mTopEdgeEffect;
    private EdgeEffect mBottomEdgeEffect;
    private OverScroller mScroller;
    private ValueSelectListener mValueSelectListener;

    public FirstSelectView(Context context) {
        this(context, null);
    }

    public FirstSelectView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FirstSelectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawValueText(canvas);
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
                mStartY = event.getY();
                if (!mScroller.isFinished()) mScroller.abortAnimation();
                break;
            case MotionEvent.ACTION_MOVE:
                mVelocityTracker.addMovement(event);

                ALog.e("getY" + event.getY());
                mMoveY = (int) (mStartY - event.getY()) + mLastY;
                mMoveY = mMoveY < mFlingMinY ? mFlingMinY : mMoveY;
                mMoveY = mMoveY > mFlingMaxY ? mFlingMaxY : mMoveY;
                scrollTo(0, mMoveY);

                if (mLastY == mFlingMinY) {
                    mTopEdgeEffect.finish();
                    mTopEdgeEffect.onPull(Math.abs(mLastY * 3));
                }
                if (mLastY == mFlingMaxY) {
                    mBottomEdgeEffect.finish();
                    mBottomEdgeEffect.onPull(Math.abs(mLastY * 3));
                }
                break;
            case MotionEvent.ACTION_UP:
                mAutoSmoothScrollable = true;
                mLastY = mMoveY;
                mVelocityTracker.computeCurrentVelocity(1000); //当手指立刻屏幕时，获得速度，作为fling的初始速度
                int initialVelocity = (int) mVelocityTracker.getYVelocity(); //获取滑动速度
                doFling(-initialVelocity);   // 由于坐标轴正方向问题，要加负号
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {

        mLastY = mScroller.getCurrY();
        float currY = mLastY + Math.abs(mFlingMinY);
        float valueF = currY / mItemHeight;
        int valueI = Math.round(valueF);
        int moveY = valueI * mItemHeight + mFlingMinY;
        ALog.e("computeScroll valueF" + valueF);
        ALog.e("computeScroll valueI" + valueI);

        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
            int oldY = getScrollY();

            if (oldY == mFlingMinY) {
                mTopEdgeEffect.finish();
                mTopEdgeEffect.onPull(Math.abs(mLastY));
            }

            if (oldY == mFlingMaxY) {
                mBottomEdgeEffect.finish();
                mBottomEdgeEffect.onPull(Math.abs(mLastY));
            }

        } else {
            int index = valueI;
            if (index != mSelectIndex && mValueSelectListener != null && mAutoSmoothScrollable) {
                mSelectIndex = index;
                if (mIsMetricUnit) {
                    mSelectMetricValueIndex = mSelectIndex;
                } else {
                    mSelectInchValueIndex = mSelectIndex;
                }
                mValueSelectListener.onFirstValueSelectedListener(mSelectIndex, mValues[mSelectIndex], mIsMetricUnit);
            }

            if (mAutoSmoothScrollable) {
                smoothScrollTo(0, moveY);
                mAutoSmoothScrollable = false;
            }
        }
    }

    private void init() {
        mWidth = DEFAULT_WIDTH;
        mHeight = DEFAULT_HEIGHT;
        mIsMetricUnit = IS_METRIC_UNIT;

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(TEXT_SIZE);
        mTextPaint.setColor(getResources().getColor(R.color.colorPrimary));

        mScroller = new OverScroller(getContext());

        Rect rect = new Rect();
        String text = String.valueOf(MIN_VALUE);
        mTextPaint.getTextBounds(text, 0, text.length(), rect);
        mTextHeight = rect.height();
        mItemHeight = TEXT_MARGIN + mTextHeight + TEXT_MARGIN;

        initMetricValues();
        mValues = mIsMetricUnit ? mMetricValues : INCH_VALUES;

        mFlingMinY = -mHeight / 2;
        mFlingMaxY = mValues.length * mItemHeight - mHeight / 2 - mItemHeight;

        mTopEdgeEffect = new EdgeEffect(getContext());
        mBottomEdgeEffect = new EdgeEffect(getContext());
    }

    /**
     * 初始化公制数据
     */
    private void initMetricValues() {
        int count = METRIC_MAX_VALUE - METRIC_MIN_VALUE + 1;
        mMetricValues = new String[count];
        for (int i = 0; i < count; i++) {
            mMetricValues[i] = String.valueOf(METRIC_MIN_VALUE + i);
        }
    }

    //缓慢滚动到指定位置
    private void smoothScrollTo(int destX, int destY) {
        int scrollY = getScrollY();
        int delta = destY - scrollY;
        mScroller.startScroll(0, scrollY, 0, delta, 200);  //1000ms内滑动destX，效果就是慢慢滑动
        invalidate();
    }

    private void doFling(int speed) {
        if (mScroller == null) {
            return;
        }
        mScroller.fling(0, mLastY, 0, speed, 0, 0, mFlingMinY, mFlingMaxY);
        invalidate();
    }

    /**
     * 绘制月份文字
     */
    private void drawValueText(Canvas canvas) {
        for (int i = 0; i < mValues.length; i++) {
            String value = mValues[i];
            float textLength = mTextPaint.measureText(value);
            float x = (mWidth - textLength) / 2;
            float y = mItemHeight * i + mTextHeight / 2;
            canvas.drawText(value, x, y, mTextPaint);
        }
    }

    private void drawEdgeEffect(Canvas canvas) {
        //绘制左边发光效果
        if (!mTopEdgeEffect.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.translate(0, -mHeight / 2);
            mTopEdgeEffect.setSize(mWidth, 300);
            if (mTopEdgeEffect.draw(canvas)) {
                postInvalidateOnAnimation();
            }
            canvas.restoreToCount(restoreCount); //还原canvas
        }
        //绘制右边发光效果
        if (!mBottomEdgeEffect.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.translate(0, mFlingMaxY);
            canvas.translate(mWidth, 0);
            canvas.rotate(180);
            canvas.translate(0, -mHeight);
            mBottomEdgeEffect.setSize(mWidth, 300);
            if (mBottomEdgeEffect.draw(canvas)) {
                postInvalidateOnAnimation();
            }
            canvas.restoreToCount(restoreCount); //还原canvas
        }
    }

    public void setSize(int width, int height) {
        mWidth = width;
        mHeight = height;

        Rect rect = new Rect();
        String text = String.valueOf(MIN_VALUE);
        mTextPaint.getTextBounds(text, 0, text.length(), rect);
        mTextHeight = rect.height();

        mItemHeight = mTextMargin * 2 + mTextHeight;
        mFlingMinY = -mHeight / 2;
        mFlingMaxY = mValues.length * mItemHeight - mHeight / 2 - mItemHeight;
        selectValueForIndex();
    }

    public int getItemHeight() {
        return mItemHeight;
    }

    /**
     * 重置View到初始位置
     */
    public void resetScrollPosition() {
        scrollTo(0, mFlingMinY);
        mScroller.startScroll(0, mFlingMinY, 0, 0, 0);
    }

    public void setAttribute(int textSize, int textColor, int textMargin) {
        mTextMargin = textMargin;
        mTextPaint.setTextSize(textSize);
        mTextPaint.setColor(textColor);
    }

    /**
     * 切换数据
     */
    public void switchData(boolean isMetricUnit) {
        if (isMetricUnit != mIsMetricUnit) mIsMetricUnit = isMetricUnit;
        selectValueForIndex();
        invalidate();
    }

    /**
     * 根据index选中指定的值
     *
     * @param metricValueIndex 公制值的index
     * @param inchValueIndex   英制值的index
     * @param isMetricUnit     是否公制单位
     */
    public void selectValueForIndex(int metricValueIndex, int inchValueIndex, boolean isMetricUnit) {
        if (mIsMetricUnit == isMetricUnit && mSelectMetricValueIndex == metricValueIndex && mSelectInchValueIndex == inchValueIndex)
            return;
        mIsMetricUnit = isMetricUnit;
        mSelectMetricValueIndex = metricValueIndex;
        mSelectInchValueIndex = inchValueIndex;
        selectValueForIndex();
        invalidate();
    }

    private void selectValueForIndex() {
        mSelectIndex = mIsMetricUnit ? mSelectMetricValueIndex : mSelectInchValueIndex;
        mValues = mIsMetricUnit ? mMetricValues : INCH_VALUES;
        mFlingMaxY = mValues.length * mItemHeight - mHeight / 2 - mItemHeight;
        int moveY = mSelectIndex * mItemHeight + mFlingMinY;
        scrollTo(0, moveY);
        mScroller.startScroll(0, moveY, 0, 0, 0);
    }

    public void setValueSelectListener(ValueSelectListener valueSelectListener) {
        mValueSelectListener = valueSelectListener;
    }

    public void setDefaultValueForIndex(int metricIndex, int inchIndex) {
        mSelectMetricValueIndex = metricIndex;
        mSelectInchValueIndex = inchIndex;
        selectValueForIndex();
    }

    /**
     * 选中指定值
     *
     * @param value        值
     * @param isMetricUnit 是否公制单位
     */
    public void setSelectValue(int value, boolean isMetricUnit) {
        if (isMetricUnit) {
            if (!(value >= METRIC_MIN_VALUE && value <= METRIC_MAX_VALUE))
                throw new RuntimeException("value overrun");
        } else {
            if (!(value >= INCH_MIN_VALUE && value <= INCH_MAX_VALUE))
                throw new RuntimeException("value overrun");
        }

        if (isMetricUnit) {
            mSelectMetricValueIndex = value - METRIC_MIN_VALUE;
        } else {
            String inchValue = "0" + value + "'";
            for (int i = 0; i < INCH_VALUES.length; i++) {
                if (inchValue.equals(INCH_VALUES[i])) {
                    mSelectInchValueIndex = i;
                    break;
                }
            }
        }
        mIsMetricUnit = isMetricUnit;
        selectValueForIndex();
        invalidate();
    }

    public String getValueForIndex(int index, boolean isMetricUnit) {
        if (isMetricUnit) return mMetricValues[index];
        else return INCH_VALUES[index];
    }

    public interface ValueSelectListener {

        /**
         * 值选中时的回调
         */
        void onFirstValueSelectedListener(int index, String value, boolean isMetricUnit);
    }

}
