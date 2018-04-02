package mirahome.customapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import mirahome.customapplication.datePickerView.DatePickView;
import mirahome.customapplication.datePickerView.DayPickView;
import mirahome.customapplication.datePickerView.MonthPickView;
import mirahome.customapplication.valuePickerView.SelectValueView;
import mirahome.customapplication.datePickerView.YearPickView;

public class DateAndValueActivity extends AppCompatActivity implements View.OnClickListener {

    private SelectValueView mSelectValueView;
    private TextView mChangeDataView;
    private TextView mSelectDataView;
    private TextView mSelectDateView;
    private EditText mYearEditView;
    private EditText mMonthEditView;
    private EditText mDayEditView;
    private Button mSetBtn;
    private YearPickView mYearSelectView;
    private MonthPickView mMonthSelectView;
    private DayPickView mDaySelectView;
    private DatePickView mDateSelectView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_and_value);

        initView();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.set_btn:
                int year = Integer.parseInt(mYearEditView.getText().toString());
                int month = Integer.parseInt(mMonthEditView.getText().toString());
                int day = Integer.parseInt(mDayEditView.getText().toString());
                mDateSelectView.setSelectDate(year, month, day);
                break;
        }
    }

    private void initView() {
        mSelectValueView = findViewById(R.id.select_value_view);
        mChangeDataView = findViewById(R.id.change_data_view);
        mSelectDataView = findViewById(R.id.select_data_view);
        mSelectDateView = findViewById(R.id.select_date_view);

        mYearEditView = findViewById(R.id.year_edit_view);
        mMonthEditView = findViewById(R.id.month_edit_view);
        mDayEditView = findViewById(R.id.day_edit_view);
        mSetBtn = findViewById(R.id.set_btn);
        mYearSelectView = findViewById(R.id.year_pick_view);
        mMonthSelectView = findViewById(R.id.month_pick_view);
        mDaySelectView = findViewById(R.id.day_pick_view);
        mDateSelectView = findViewById(R.id.date_select_view);

        mSelectValueView.setValueSelectListener(mValueSelectListener);
        mSetBtn.setOnClickListener(this);
        mDateSelectView.setDateSelectListener(mDateSelectListener);
    }


    private SelectValueView.ValueSelectListener mValueSelectListener = new SelectValueView.ValueSelectListener() {
        @Override
        public void onValueChangeListener(float value) {
            mChangeDataView.setText(String.valueOf(value));
        }

        @Override
        public void onValueSelectedListener(float value) {
            mSelectDataView.setText(String.valueOf(value));
        }
    };

    private DatePickView.DateSelectListener mDateSelectListener = new DatePickView.DateSelectListener() {
        @Override
        public void onDateSelectedListener(String date) {
            mSelectDateView.setText(date);
        }
    };
}
