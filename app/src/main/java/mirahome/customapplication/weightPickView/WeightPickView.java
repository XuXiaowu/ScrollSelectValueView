package mirahome.customapplication.weightPickView;

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
 * <p>
 * 身高选择器
 */
public class WeightPickView extends AbsViewGroup implements FirstSelectView.ValueSelectListener, SecondSelectView.ValueSelectListener {

    private static final int TEXT_SIZE = 60; //字体大小
    private static final int TEXT_MARGIN = 40;
    private static final int DEFAULT_FIRST_METRIC_VALUE = 27; //第一个View默认公制选中的值
    private static final int DEFAULT_FIRST_INCH_VALUE = 60; //第一个View默认英制选中的值
    private static final int DEFAULT_SECOND_INDEX = 5; //第二个View默认选中的index
    private static final boolean DEFAULT_IS_METRIC_UNIT = true; //是否公制单位

    private int mItemWidth;
    private int mItemHeight;
    private int mTextSize;
    private int mTextColor;
    private int mTextMargin; //文字的上下间距
    private int mDefaultFirstMetricValue; //默认第一个公制单位值
    private int mDefaultFirstInchValue; //默认第一个英制单位值
    private int mDefaultSecondIndex; //默认第二个选中的index
    private String mFirstMetricValue; //第一个公制单位值
    private String mFirstInchValue; //第一个英制单位值
    private String mSecondValue; //第二个值
    private boolean mIsMetricUnit; //是否选中公制单位

    private FirstSelectView mFirstSelectView;
    private SecondSelectView mSecondSelectView;
    private View mTopMaskView; //上面的遮罩
    private View mBottomMaskView; //下面的遮罩

    private Rect mFirstSelectRect;
    private Rect mSecondSelectRect;
    private Rect mThirdSelectRect;
    private Rect mTopMaskRect;
    private Rect mBottomMaskRect;

    private HeightSelectListener mHeightSelectListener;


    public WeightPickView(Context context) {
        super(context);
    }

    public WeightPickView(Context context, AttributeSet attr) {
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
    }

    @Override
    public void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_weight_pick_view, this);
        mFirstSelectView = findViewById(R.id.first_pick_view);
        mSecondSelectView = findViewById(R.id.second_pick_view);
        mTopMaskView = findViewById(R.id.top_mask_view);
        mBottomMaskView = findViewById(R.id.bottom_mask_view);

        mFirstSelectView.setValueSelectListener(this);
        mSecondSelectView.setValueSelectListener(this);
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
            mViewWidth = MeasureSpec.getSize(widthMeasureSpec);
            mViewHeight = MeasureSpec.getSize(heightMeasureSpec);
            mItemWidth = mViewWidth / 2;
            mFirstSelectView.setSize(mItemWidth, mViewHeight);
            mSecondSelectView.setSize(mItemWidth, mViewHeight);
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
            mFirstMetricValue = value;
        } else {
            mFirstInchValue = value;
        }
        updateCallback();
    }

    /**
     * 值选中时的回调
     *
     * @param value
     */
    @Override
    public void onSecondValueSelectedListener(int index, String value) {
        ALog.e("onValueSelectedListener Second " + value);
        mSecondValue = value;
        mIsMetricUnit = index == 0;
        mFirstSelectView.switchData(mIsMetricUnit);
        updateCallback();
    }

    private void getAttribute(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WeightPickView);
        mTextColor = typedArray.getColor(R.styleable.WeightPickView_weightPickTextColor, 0x5071717B);
        mTextSize = (int) typedArray.getDimension(R.styleable.WeightPickView_weightPickTextSize, TEXT_SIZE);
        mTextMargin = (int) typedArray.getDimension(R.styleable.WeightPickView_weightPickTextMargin, TEXT_MARGIN);
        mIsMetricUnit = typedArray.getBoolean(R.styleable.WeightPickView_weightPickIsMetricUnit, DEFAULT_IS_METRIC_UNIT);
        mDefaultFirstMetricValue = typedArray.getInt(R.styleable.WeightPickView_weightPickDefaultFirstMetricValue, DEFAULT_FIRST_METRIC_VALUE);
        mDefaultFirstInchValue = typedArray.getInt(R.styleable.WeightPickView_weightPickDefaultFirstInchValue, DEFAULT_FIRST_INCH_VALUE);
        mDefaultSecondIndex = typedArray.getInt(R.styleable.WeightPickView_weightPickDefaultSecondIndex, DEFAULT_SECOND_INDEX);
        typedArray.recycle();
    }

    private void setChildViewAttribute() {
        mFirstSelectView.setAttribute(mTextSize, mTextColor, mTextMargin);
        mSecondSelectView.setAttribute(mTextSize, mTextColor, mTextMargin);
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
            String firstValue = mIsMetricUnit ? mFirstMetricValue : mFirstInchValue;
            String value = firstValue + " " + mSecondValue;
            mHeightSelectListener.onWeightSelectedListener(value, mIsMetricUnit);
        }
    }

    private void setDefaultValueForIndex() {
        mFirstSelectView.setDefaultValue(mDefaultFirstMetricValue, mDefaultFirstInchValue);
        mSecondSelectView.setDefaultValueForIndex(mDefaultSecondIndex);
    }

    private void initValue() {
        mFirstMetricValue = String.valueOf(mDefaultFirstMetricValue);
        mFirstInchValue = String.valueOf(mDefaultFirstInchValue);
        mSecondValue = mSecondSelectView.getValueForIndex(mDefaultSecondIndex);
    }

    public void setHeightSelectListener(HeightSelectListener heightSelectListener) {
        mHeightSelectListener = heightSelectListener;
    }

    public void setSelectValue(int firstValue, boolean isMetricUnit) {
        mFirstSelectView.setSelectValue(firstValue, isMetricUnit);
        mSecondSelectView.setSelectUnit(isMetricUnit);
    }

    public interface HeightSelectListener {

        /**
         * 值选中时的回调
         *
         * @param value        值
         * @param isMetricUnit 是公制单位
         */
        void onWeightSelectedListener(String value, boolean isMetricUnit);
    }

}
