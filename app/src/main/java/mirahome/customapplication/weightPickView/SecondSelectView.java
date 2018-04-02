package mirahome.customapplication.weightPickView;

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
 * Created by xuxiaowu on 2018/3/2.
 * <p>
 * 第二个View，选中单位
 */
public class SecondSelectView extends View {

    private static final int DEFAULT_HEIGHT = 600;
    private static final int DEFAULT_WIDTH = 300;
    private static final int TEXT_SIZE = 60; //字体大小
    private static final int TEXT_MARGIN = 40;

    private static final String[] VALUES = new String[]{"kg", "ibs"}; //英制数据

    private int mSelectIndex; //选中的index
    private int mMoveY;
    private int mHeight;
    private int mWidth;
    private int mFlingMinY;
    private int mFlingMaxY;
    private int mLastY;
    private int mItemHeight; //item高度
    private int mTextHeight; //文字的高度
    private int mTextMargin; //文字的上下间距

    private float mStartY = 0;//按下时y值
    private boolean mAutoSmoothScrollable;

    private Paint mTextPaint;
    private VelocityTracker mVelocityTracker;
    private EdgeEffect mTopEdgeEffect;
    private EdgeEffect mBottomEdgeEffect;
    private OverScroller mScroller;
    private ValueSelectListener mValueSelectListener;

    public SecondSelectView(Context context) {
        this(context, null);
    }

    public SecondSelectView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SecondSelectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
                mValueSelectListener.onSecondValueSelectedListener(mSelectIndex, VALUES[mSelectIndex]);
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
        mTextMargin = TEXT_MARGIN;

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(TEXT_SIZE);
        mTextPaint.setColor(getResources().getColor(R.color.colorPrimary));

        mScroller = new OverScroller(getContext());

        Rect rect = new Rect();
        String text = VALUES[mSelectIndex];
        mTextPaint.getTextBounds(text, 0, text.length(), rect);
        mTextHeight = rect.height();
        mItemHeight = mTextMargin * 2 + mTextHeight;

        mFlingMinY = -mHeight / 2;
        mFlingMaxY = VALUES.length * mItemHeight - mHeight / 2 - mItemHeight;

        mTopEdgeEffect = new EdgeEffect(getContext());
        mBottomEdgeEffect = new EdgeEffect(getContext());
    }

    /**
     * 缓慢滚动到指定位置
     */
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
     * 绘制文字
     */
    private void drawValueText(Canvas canvas) {
        for (int i = 0; i < VALUES.length; i++) {
            String value = VALUES[i];
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
        String text = VALUES[mSelectIndex];
        mTextPaint.getTextBounds(text, 0, text.length(), rect);
        mTextHeight = rect.height();

        mItemHeight = mTextMargin * 2 + mTextHeight;
        mFlingMinY = -mHeight / 2;
        mFlingMaxY = VALUES.length * mItemHeight - mHeight / 2 - mItemHeight;
        resetScrollPosition();
    }

    /**
     * 重置View到初始位置
     */
    public void resetScrollPosition() {
        int moveY = mFlingMinY + mSelectIndex * mItemHeight;
        scrollTo(0, moveY);
        mScroller.startScroll(0, moveY, 0, 0, 0);
    }

    public void setAttribute(int textSize, int textColor, int textMargin) {
        mTextMargin = textMargin;
        mTextPaint.setTextSize(textSize);
        mTextPaint.setColor(textColor);
    }

    public void setDefaultValueForIndex(int index) {
        mSelectIndex = index;
        resetScrollPosition();
    }

    public String getValueForIndex(int index) {
        return VALUES[index];
    }

    public void setValueSelectListener(ValueSelectListener valueSelectListener) {
        mValueSelectListener = valueSelectListener;
    }

    public void setSelectUnit(boolean isMetric) {
        mSelectIndex = isMetric ? 0 : 1;
        resetScrollPosition();
    }

    public interface ValueSelectListener {

        /**
         * 值选中时的回调
         *
         * @param index 索引
         * @param value 值
         */
        void onSecondValueSelectedListener(int index, String value);
    }

}
