package mirahome.customapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.blankj.ALog;

import mirahome.customapplication.weightPickView.WeightPickView;

public class WeightActivity extends AppCompatActivity implements View.OnClickListener {

    private WeightPickView mWeightPickView;
    private Switch mSwitch;
    private EditText mEditText;
    private Button mSetBtn;
    private boolean mIsMetricUnit = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight);

        mWeightPickView = findViewById(R.id.weight_pick_view);
        mSwitch = findViewById(R.id.switch_view);
        mEditText = findViewById(R.id.first_edit_view);
        mSetBtn = findViewById(R.id.set_btn);

        mSetBtn.setOnClickListener(this);
        mSwitch.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mWeightPickView.setHeightSelectListener(mHeightSelectListener);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.set_btn:
                String value = mEditText.getText().toString();
                mWeightPickView.setSelectValue(Integer.parseInt(value), mIsMetricUnit);
                break;
        }
    }

    private WeightPickView.HeightSelectListener mHeightSelectListener = new WeightPickView.HeightSelectListener() {
        @Override
        public void onWeightSelectedListener(String value, boolean isMetricUnit) {
            ALog.e("mHeightSelectListener- " + value + " " + isMetricUnit);
        }
    };

    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            mIsMetricUnit = b;
        }
    };
}
