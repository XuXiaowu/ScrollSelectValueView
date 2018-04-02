package mirahome.customapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.blankj.ALog;

import mirahome.customapplication.heightPickerView.HeightPickView;

public class HeightActivity extends AppCompatActivity implements View.OnClickListener {

    private HeightPickView mHeightPickView;
    private EditText mFirstEditView;
    private EditText mSecondEditView;
    private Button mSetBtn;
    private Switch mSwitch;

    private int mFirstValue;
    private int mSecondValue;
    private boolean mIsMetricUnit = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_height);

        mFirstEditView = findViewById(R.id.first_edit_view);
        mSecondEditView = findViewById(R.id.second_edit_view);

        mSetBtn = findViewById(R.id.set_btn);
        mHeightPickView = findViewById(R.id.height_pick_view);
        mSwitch = findViewById(R.id.switch_view);

        mSetBtn.setOnClickListener(this);
        mHeightPickView.setHeightSelectListener(mHeightSelectListener);
        mSwitch.setOnCheckedChangeListener(mOnCheckedChangeListener);
    }

    private HeightPickView.HeightSelectListener mHeightSelectListener = new HeightPickView.HeightSelectListener() {
        @Override
        public void onWeightSelectedListener(String firstValue, String secondValue, String unitValue, boolean isMetricUnit) {
            ALog.e("onValueSelectedListener Weight " + firstValue + " " + secondValue + " " + unitValue + "" + isMetricUnit);
        }
    };

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.set_btn:
                mFirstValue = Integer.parseInt(mFirstEditView.getText().toString());
                mSecondValue = Integer.parseInt(mSecondEditView.getText().toString());
                mHeightPickView.setSelectValue(mFirstValue, mSecondValue, mIsMetricUnit);
                break;
        }
    }

    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            mIsMetricUnit = b;
        }
    };
}
