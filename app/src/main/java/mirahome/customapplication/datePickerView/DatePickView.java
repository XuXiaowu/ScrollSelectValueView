package mirahome.customapplication.datePickerView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import mirahome.customapplication.R;
import mirahome.customapplication.base.AbsViewGroup;

/**
 * Created by xuxiaowu on 2017/11/14.
 * <p>
 * 日期选择器
 */
public class DatePickView extends AbsViewGroup implements MonthPickView.MonthSelectListener, YearPickView.YearSelectListener, DayPickView.DaySelectListener {

    private static final int DEFAULT_YEAR = 1900;
    private static final int DEFAULT_MONTH = 1;
    private static final int DEFAULT_DAY = 1;
    private static final int TEXT_SIZE = 60; //字体大小
    private static final int TEXT_MARGIN = 40;

    private YearPickView mYearPickView;
    private MonthPickView mMonthPickView;
    private DayPickView mDayPickView;
    private View mYearTopMaskView;
    private View mYearBottomMaskView;

    private DateSelectListener mDateSelectListener;

    private Rect mYearRect;
    private Rect mMonthRect;
    private Rect mDayRect;
    private Rect mTopMaskRect;
    private Rect mBottomMaskRect;

    private int mSelectYear;
    private int mSelectMonth;
    private int mSelectDay;
    private int mItemWidth;
    private int mItemHeight;
    private int mTextSize;
    private int mTextColor;
    private int mTextMargin;
    private int mDefaultYear;
    private int mDefaultMonth;
    private int mDefaultDay;

    private String mSelectDate;

    public DatePickView(Context context) {
        super(context);
    }

    public DatePickView(Context context, AttributeSet attr) {
        super(context, attr);

        getAttribute(context, attr);
        setChildViewAttribute();
        computeYearRect();
        computeMonthRect();
        computeDayRect();
        computeTopMaskRect();
        computeBottomMaskRect();
    }

    @Override
    public void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_date_pick_view, this);
        mYearPickView = findViewById(R.id.year_pick_view);
        mMonthPickView = findViewById(R.id.month_pick_view);
        mDayPickView = findViewById(R.id.day_pick_view);
        mYearTopMaskView = findViewById(R.id.top_mask_view);
        mYearBottomMaskView = findViewById(R.id.bottom_mask_view);

        mMonthPickView.setMonthSelectListener(this);
        mYearPickView.setYearSelectListener(this);
        mDayPickView.setDaySelectListener(this);
    }

    @Override
    public void initSize(Context context) {

    }

    @Override
    public void initPadding(Context context) {

    }

    @Override
    public void initRect(Context context) {
        mYearRect = new Rect();
        mMonthRect = new Rect();
        mDayRect = new Rect();
        mTopMaskRect = new Rect();
        mBottomMaskRect = new Rect();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mViewHeight == 0 || mViewWidth == 0) {
            mViewWidth = MeasureSpec.getSize(widthMeasureSpec);
            mViewHeight = MeasureSpec.getSize(heightMeasureSpec);
            mItemWidth = mViewWidth / 3;
            mYearPickView.setSize(mItemWidth, mViewHeight);
            mMonthPickView.setSize(mItemWidth, mViewHeight);
            mDayPickView.setSize(mItemWidth, mViewHeight);
            mItemHeight = mDayPickView.getItemHeight();

            mYearPickView.setSelectYear(mDefaultYear);
            mMonthPickView.setSelectMonth(mDefaultMonth);
            mDayPickView.setSelectDay(mDefaultDay);

            computeYearRect();
            computeMonthRect();
            computeDayRect();
            computeTopMaskRect();
            computeBottomMaskRect();
            requestLayout();
        }
        setMeasuredDimension(mViewWidth, mViewHeight);
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        mYearPickView.layout(mYearRect.left, mYearRect.top, mYearRect.right, mYearRect.bottom);
        mMonthPickView.layout(mMonthRect.left, mMonthRect.top, mMonthRect.right, mMonthRect.bottom);
        mDayPickView.layout(mDayRect.left, mDayRect.top, mDayRect.right, mDayRect.bottom);
        mYearTopMaskView.layout(mTopMaskRect.left, mTopMaskRect.top, mTopMaskRect.right, mTopMaskRect.bottom);
        mYearBottomMaskView.layout(mBottomMaskRect.left, mBottomMaskRect.top, mBottomMaskRect.right, mBottomMaskRect.bottom);
    }

    @Override
    public void onYearSelectedListener(int year) {
        mSelectYear = year;
        mDayPickView.updateDayNumForYear(year);
        updateDate();
    }

    @Override
    public void onMonthSelectedListener(int month) {
        mSelectMonth = month;
        mDayPickView.updateDayNumForMonth(month);
        updateDate();
    }

    @Override
    public void onDaySelectedListener(int day) {
        mSelectDay = day;
        updateDate();
    }

    private void getAttribute(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DatePickView);
        mTextColor = typedArray.getColor(R.styleable.DatePickView_textColor, 0x5071717B);
        mTextSize = (int) typedArray.getDimension(R.styleable.DatePickView_textSize, TEXT_SIZE);
        mTextMargin = (int) typedArray.getDimension(R.styleable.DatePickView_textMargin, TEXT_MARGIN);
        mDefaultYear = typedArray.getInt(R.styleable.DatePickView_defaultYear, DEFAULT_YEAR);
        mDefaultMonth = typedArray.getInt(R.styleable.DatePickView_defaultMonth, DEFAULT_MONTH);
        mDefaultDay = typedArray.getInt(R.styleable.DatePickView_defaultDay, DEFAULT_DAY);
        typedArray.recycle();

        mSelectYear = mDefaultYear;
        mSelectMonth = mDefaultMonth;
        mSelectDay = mDefaultDay;
    }

    private void setChildViewAttribute() {
        mYearPickView.setAttribute(mTextSize, mTextColor, mTextMargin);
        mMonthPickView.setAttribute(mTextSize, mTextColor, mTextMargin);
        mDayPickView.setAttribute(mTextSize, mTextColor, mTextMargin);
    }

    private void updateDate() {
        String month;
        String day;
        if (mSelectMonth < 10) {
            month = "0" + String.valueOf(mSelectMonth);
        } else {
            month = String.valueOf(mSelectMonth);
        }

        if (mSelectDay < 10) {
            day = "0" + String.valueOf(mSelectDay);
        } else {
            day = String.valueOf(mSelectDay);
        }
        String date = mSelectYear + "-" + month + "-" + day;
        if (!date.equals(mSelectDate)) {
            mSelectDate = date;
            mDateSelectListener.onDateSelectedListener(mSelectDate);
        }
    }

    private void computeYearRect() {
        mYearRect.left = 0;
        mYearRect.right = mItemWidth;
        mYearRect.top = 0;
        mYearRect.bottom = mViewHeight;
    }

    private void computeMonthRect() {
        mMonthRect.left = mYearRect.right;
        mMonthRect.right = mMonthRect.left + mItemWidth;
        mMonthRect.top = 0;
        mMonthRect.bottom = mViewHeight;
    }

    private void computeDayRect() {
        mDayRect.left = mMonthRect.right;
        mDayRect.right = mDayRect.left + mItemWidth;
        mDayRect.top = 0;
        mDayRect.bottom = mViewHeight;
    }

    private void computeTopMaskRect() {
        mTopMaskRect.left = 0;
        mTopMaskRect.right = mViewWidth;
        mTopMaskRect.top = 0;
        mTopMaskRect.bottom = (mViewHeight - mItemHeight) / 2;
    }

    private void computeBottomMaskRect() {
        mBottomMaskRect.left = 0;
        mBottomMaskRect.right = mViewWidth;
        mBottomMaskRect.bottom = mViewHeight;
        mBottomMaskRect.top = mBottomMaskRect.bottom - (mViewHeight - mItemHeight) / 2;
    }

    public void setDateSelectListener(DateSelectListener dateSelectListener) {
        mDateSelectListener = dateSelectListener;
    }

    /**
     * 设置选中的日期
     *
     * @param year  年
     * @param month 月
     * @param day   日
     */
    public void setSelectDate(int year, int month, int day) {
        mYearPickView.setSelectYear(year);
        mMonthPickView.setSelectMonth(month);
        mDayPickView.setSelectDay(day);

        mSelectYear = year;
        mSelectMonth = month;
        mSelectDay = day;
    }

    public interface DateSelectListener {

        /**
         * 值选中时的回调
         */
        void onDateSelectedListener(String date);
    }

}
