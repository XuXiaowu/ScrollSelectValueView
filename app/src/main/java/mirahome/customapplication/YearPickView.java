package mirahome.customapplication;

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

import java.text.SimpleDateFormat;

/**
 * Created by xuxiaowu on 2017/11/13.
 */

public class YearPickView extends View {

    private static final int DEFAULT_HEIGHT = 500;
    private static final int DEFAULT_WIDTH = 300;
    private static final int MIN_YEAR = 1900;
    private static final int TEXT_SIZE = 60; //字体大小
    private static final int TEXT_MARGIN = 40;

    private float mStartY = 0;//按下时y值

    private int mSelectYear; //选中的年份
    private int mMoveY;
    private int mHeight;
    private int mWidth;
    private int mFlingMinY;
    private int mFlingMaxY;
    private int mCurrentYear;
    private int mLastY;
    private int mItemHeight; //item高度
    private int mTextHeight; //文字的高度
    private int mTextSize;
    private int mTextColor;
    private int mTextMargin;

    private boolean mAutoSmoothScrollable;

    private Paint mTextPaint;
    private VelocityTracker mVelocityTracker;
    private EdgeEffect mTopEdgeEffect;
    private EdgeEffect mBottomEdgeEffect;
    private OverScroller mScroller;
    private YearSelectListener mYearSelectListener;

    public YearPickView(Context context) {
        this(context, null);
    }

    public YearPickView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public YearPickView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundResource(R.color.white);

        init();
        resetScrollPosition();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawYearText(canvas);
        drawCentreLine(canvas);
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
            int year = valueI + MIN_YEAR;
            if (year != mSelectYear && mYearSelectListener != null && mAutoSmoothScrollable) {
                mSelectYear = year;
                mYearSelectListener.onYearSelectedListener(mSelectYear);
            }

            if (mAutoSmoothScrollable) {
                smoothScrollTo(0, moveY);
                mAutoSmoothScrollable = false;
            }
        }
    }

    private void init() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        String date = sdf.format(new java.util.Date());
        mCurrentYear = Integer.parseInt(date);

        mWidth = DEFAULT_WIDTH;
        mHeight = DEFAULT_HEIGHT;
        mTextSize = TEXT_SIZE;
        mTextMargin = TEXT_MARGIN;

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(getResources().getColor(R.color.colorPrimary));

        mScroller = new OverScroller(getContext());

        Rect rect = new Rect();
        String text = String.valueOf(MIN_YEAR);
        mTextPaint.getTextBounds(text, 0, text.length(), rect);
        mTextHeight = rect.height();
        mItemHeight = mTextMargin * 2 + mTextHeight;

        mFlingMinY = -mHeight / 2;
        mFlingMaxY = (mCurrentYear - MIN_YEAR + 1) * mItemHeight - mHeight / 2 - mItemHeight;

        mTopEdgeEffect = new EdgeEffect(getContext());
        mBottomEdgeEffect = new EdgeEffect(getContext());
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
     * 绘制年份文字
     */
    private void drawYearText(Canvas canvas) {
        int count = mCurrentYear - MIN_YEAR;
        for (int i = 0; i < count + 1; i++) {
            String year = String.valueOf(MIN_YEAR + i);
            float textLength = mTextPaint.measureText(year);
            float x = (mWidth - textLength) / 2;
            float y = mItemHeight * i + mTextHeight / 2;
            canvas.drawText(year, x, y, mTextPaint);
        }
    }

    private void drawCentreLine(Canvas canvas) {
        mTextPaint.setStrokeWidth(1);
        canvas.drawLine(0, mHeight / 2, mWidth, mHeight / 2, mTextPaint);
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
        String text = String.valueOf(MIN_YEAR);
        mTextPaint.getTextBounds(text, 0, text.length(), rect);
        mTextHeight = rect.height();

        mItemHeight = mTextMargin * 2 + mTextHeight;
        mFlingMinY = -mHeight / 2;
        mFlingMaxY = (mCurrentYear - MIN_YEAR + 1) * mItemHeight - mHeight / 2 - mItemHeight;
    }

    /**
     * 重置View到初始位置
     */
    public void resetScrollPosition() {
        scrollTo(0, mFlingMinY);
        mScroller.startScroll(0, mFlingMinY, 0, 0, 0);
    }

    public void setAttribute(int textSize, int textColor, int textMargin) {
        mTextSize = textSize;
        mTextColor = textColor;
        mTextMargin = textMargin;
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
        ALog.e("setAttribute");
    }

    /**
     * 选中某一年
     */
    public void setYear(int year) {
        if (year < MIN_YEAR || year > mCurrentYear) {
            throw new RuntimeException("year overrun");
        }
        mSelectYear = year;
        int moveY = (year - MIN_YEAR) * mItemHeight + mFlingMinY;
        scrollTo(0, moveY);
        mScroller.startScroll(0, moveY, 0, 0, 0);
    }

    public void setYearSelectListener(YearSelectListener yearSelectListener) {
        mYearSelectListener = yearSelectListener;
    }

    public interface YearSelectListener {

        /**
         * 值选中时的回调
         */
        void onYearSelectedListener(int year);
    }

}
