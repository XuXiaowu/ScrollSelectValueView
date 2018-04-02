package mirahome.customapplication.heightPickerView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.blankj.ALog;

import mirahome.customapplication.R;
import mirahome.customapplication.base.AbsViewGroup;

/**
 * Created by xuxiaowu on 2018/3/2.
 */
public class HeightPickView extends AbsViewGroup implements ThirdSelectView.ValueSelectListener, FirstSelectView.ValueSelectListener, SecondSelectView.ValueSelectListener {

    private static final int TEXT_SIZE = 60; //字体大小
    private static final int TEXT_MARGIN = 40;
    private static final int DEFAULT_FIRST_METRIC_INDEX = 80; //第一个View默认公制选中的index
    private static final int DEFAULT_FIRST_INCH_INDEX = 2; //第一个View默认英制选中的index
    private static final int DEFAULT_SECOND_INDEX = 5; //第二个View默认选中的index
    private static final int DEFAULT_THIRD_INDEX = 0; //第三个View默认选中的index
    private static final boolean DEFAULT_IS_METRIC_UNIT = true; //是否公制单位

    private int mItemWidth;
    private int mItemHeight;
    private int mTextSize;
    private int mTextColor;
    private int mTextMargin;
    private int mDefaultFirstMetricIndex;
    private int mDefaultFirstInchIndex;
    private int mDefaultSecondIndex;
    private int mDefaultThirdIndex;
    private String mSelectFirstMetricValue;
    private String mSelectFirstInchValue;
    private String mSelectSecondValue;
    private String mSelectThirdValue;
    private boolean mIsMetricUnit;

    private FirstSelectView mFirstSelectView;
    private SecondSelectView mSecondSelectView;
    private ThirdSelectView mThirdSelectView;
    private View mTopMaskView;
    private View mBottomMaskView;

    private Rect mFirstSelectRect;
    private Rect mSecondSelectRect;
    private Rect mThirdSelectRect;
    private Rect mTopMaskRect;
    private Rect mBottomMaskRect;

    private HeightSelectListener mHeightSelectListener;


    public HeightPickView(Context context) {
        super(context);
    }

    public HeightPickView(Context context, AttributeSet attr) {
        super(context, attr);

        getAttribute(context, attr);
        setChildViewAttribute();

        computeFirstSelectRect();
        computeSecondSelectRect();
        computeThirdSelectRect();
        computeTopMaskRect();
        computeBottomMaskRect();
        setBackgroundResource(R.color.gray5);
        setDefaultValueForIndex();
        initValue();
        mSecondSelectView.setVisibility(mIsMetricUnit ? INVISIBLE : VISIBLE);
    }

    @Override
    public void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_height_pick_view, this);
        mFirstSelectView = findViewById(R.id.first_pick_view);
        mSecondSelectView = findViewById(R.id.second_pick_view);
        mThirdSelectView = findViewById(R.id.third_pick_view);
        mTopMaskView = findViewById(R.id.top_mask_view);
        mBottomMaskView = findViewById(R.id.bottom_mask_view);

        mFirstSelectView.setValueSelectListener(this);
        mSecondSelectView.setValueSelectListener(this);
        mThirdSelectView.setValueSelectListener(this);
    }

    @Override
    public void initSize(Context context) {

    }

    @Override
    public void initPadding(Context context) {

    }

    @Override
    public void initRect(Context context) {
        mFirstSelectRect = new Rect();
        mSecondSelectRect = new Rect();
        mThirdSelectRect = new Rect();
        mTopMaskRect = new Rect();
        mBottomMaskRect = new Rect();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mViewHeight == 0 || mViewWidth == 0) {
            mViewWidth = View.MeasureSpec.getSize(widthMeasureSpec);
            mViewHeight = View.MeasureSpec.getSize(heightMeasureSpec);
            mItemWidth = mViewWidth / 3;
            mFirstSelectView.setSize(mItemWidth, mViewHeight);
            mSecondSelectView.setSize(mItemWidth, mViewHeight);
            mThirdSelectView.setSize(mItemWidth, mViewHeight);
            mItemHeight = mFirstSelectView.getItemHeight();

            computeFirstSelectRect();
            computeSecondSelectRect();
            computeThirdSelectRect();
            computeTopMaskRect();
            computeBottomMaskRect();
        }
        setMeasuredDimension(mViewWidth, mViewHeight);
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        mFirstSelectView.layout(mFirstSelectRect.left, mFirstSelectRect.top, mFirstSelectRect.right, mFirstSelectRect.bottom);
        mSecondSelectView.layout(mSecondSelectRect.left, mSecondSelectRect.top, mSecondSelectRect.right, mSecondSelectRect.bottom);
        mThirdSelectView.layout(mThirdSelectRect.left, mThirdSelectRect.top, mThirdSelectRect.right, mThirdSelectRect.bottom);
        mTopMaskView.layout(mTopMaskRect.left, mTopMaskRect.top, mTopMaskRect.right, mTopMaskRect.bottom);
        mBottomMaskView.layout(mBottomMaskRect.left, mBottomMaskRect.top, mBottomMaskRect.right, mBottomMaskRect.bottom);
    }

    /**
     * 值选中时的回调
     *
     * @param index
     * @param value
     * @param isMetricUnit
     */
    @Override
    public void onFirstValueSelectedListener(int index, String value, boolean isMetricUnit) {
        ALog.e("onValueSelectedListener First " + index + " " + value + " " + isMetricUnit);
        if (isMetricUnit) {
            mSelectFirstMetricValue = value;
        } else {
            mSelectFirstInchValue = value;
        }
        updateCallback();
    }

    /**
     * 值选中时的回调
     *
     * @param value
     */
    @Override
    public void onSecondValueSelectedListener(String value) {
        ALog.e("onValueSelectedListener Second " + value);
        mSelectSecondValue = value;
        updateCallback();
    }

    /**
     * 值选中时的回调
     *
     * @param index 索引
     * @param value 值
     */
    @Override
    public void onThirdValueSelectedListener(int index, String value) {
        ALog.e("onValueSelectedListener Third " + index + " " + value);
        mIsMetricUnit = index == 0;
        mFirstSelectView.switchData(mIsMetricUnit);
        mSecondSelectView.setVisibility(index == 1 ? VISIBLE : INVISIBLE);
        mSelectThirdValue = value;
        updateCallback();
    }

    private void getAttribute(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HeightPickView);
        mTextColor = typedArray.getColor(R.styleable.HeightPickView_heightPickTextColor, 0x5071717B);
        mTextSize = (int) typedArray.getDimension(R.styleable.HeightPickView_heightPickTextSize, TEXT_SIZE);
        mTextMargin = (int) typedArray.getDimension(R.styleable.HeightPickView_heightPickTextMargin, TEXT_MARGIN);
        mIsMetricUnit = typedArray.getBoolean(R.styleable.HeightPickView_heightPickIsMetricUnit, DEFAULT_IS_METRIC_UNIT);
        mDefaultFirstMetricIndex = typedArray.getInt(R.styleable.HeightPickView_heightPickDefaultFirstMetricIndex, DEFAULT_FIRST_METRIC_INDEX);
        mDefaultFirstInchIndex = typedArray.getInt(R.styleable.HeightPickView_heightPickDefaultFirstInchIndex, DEFAULT_FIRST_INCH_INDEX);
        mDefaultSecondIndex = typedArray.getInt(R.styleable.HeightPickView_heightPickDefaultSecondIndex, DEFAULT_SECOND_INDEX);
        mDefaultThirdIndex = typedArray.getInt(R.styleable.HeightPickView_heightPickDefaultThirdIndex, DEFAULT_THIRD_INDEX);
        typedArray.recycle();
    }

    private void setChildViewAttribute() {
        mFirstSelectView.setAttribute(mTextSize, mTextColor, mTextMargin);
        mSecondSelectView.setAttribute(mTextSize, mTextColor, mTextMargin);
        mThirdSelectView.setAttribute(mTextSize, mTextColor, mTextMargin);
    }

    private void computeFirstSelectRect() {
        mFirstSelectRect.left = 0;
        mFirstSelectRect.right = mItemWidth;
        mFirstSelectRect.top = 0;
        mFirstSelectRect.bottom = mViewHeight;
    }

    private void computeSecondSelectRect() {
        mSecondSelectRect.left = mFirstSelectRect.right;
        mSecondSelectRect.right = mSecondSelectRect.left + mItemWidth;
        mSecondSelectRect.top = 0;
        mSecondSelectRect.bottom = mViewHeight;
    }

    private void computeThirdSelectRect() {
        mThirdSelectRect.left = mSecondSelectRect.right;
        mThirdSelectRect.right = mThirdSelectRect.left + mItemWidth;
        mThirdSelectRect.top = 0;
        mThirdSelectRect.bottom = mViewHeight;
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

    private void updateCallback() {
        if (mHeightSelectListener != null) {
            String firstValue = mIsMetricUnit ? mSelectFirstMetricValue : mSelectFirstInchValue;
            String secondValue = "";
            if (!mIsMetricUnit) {
                firstValue = firstValue.substring(0, firstValue.length() - 1);
                secondValue = mSelectSecondValue.substring(0, mSelectSecondValue.length() - 1);
            }
            mHeightSelectListener.onWeightSelectedListener(firstValue, secondValue, mSelectThirdValue, mIsMetricUnit);
        }
    }

    private void setDefaultValueForIndex() {
        mFirstSelectView.setDefaultValueForIndex(mDefaultFirstMetricIndex, mDefaultFirstInchIndex);
        mSecondSelectView.setDefaultValueForIndex(mDefaultSecondIndex);
        mThirdSelectView.setDefaultValueForIndex(mDefaultThirdIndex);
    }

    private void initValue() {
        mSelectFirstMetricValue = mFirstSelectView.getValueForIndex(mDefaultFirstMetricIndex, true);
        mSelectFirstInchValue = mFirstSelectView.getValueForIndex(mDefaultFirstInchIndex, false);
        mSelectSecondValue = mSecondSelectView.getValueForIndex(mDefaultSecondIndex);
        mSelectThirdValue = mThirdSelectView.getValueForIndex(mDefaultThirdIndex);
    }

    public void setHeightSelectListener(HeightSelectListener heightSelectListener) {
        mHeightSelectListener = heightSelectListener;
    }

    public void setSelectValue(int firstValue, int secondValue, boolean isMetricUnit) {
        mFirstSelectView.setSelectValue(firstValue, isMetricUnit);
        mSecondSelectView.setVisibility(isMetricUnit ? GONE : VISIBLE);
        mThirdSelectView.setSelectUnit(isMetricUnit);
        mIsMetricUnit = isMetricUnit;

        if (isMetricUnit) {
            mSelectFirstMetricValue = String.valueOf(firstValue);
        } else {
            mSelectFirstInchValue = String.valueOf(firstValue) + "'";
            mSelectSecondValue = String.valueOf(secondValue) + "\"";
            mSecondSelectView.setSelectValue(secondValue);
        }
    }

    public interface HeightSelectListener {

        /**
         * 值选中时的回调
         *
         * @param firstValue   第一个值
         * @param secondValue  第二个值
         * @param unitValue    第三个值(单位)
         * @param isMetricUnit 是否公制单位
         */
        void onWeightSelectedListener(String firstValue, String secondValue, String unitValue, boolean isMetricUnit);
    }


}
