package mirahome.customapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.ALog;

public class MainActivity extends AppCompatActivity {

    private SelectValueView mSelectValueView;
    private TextView mChangeDataView;
    private TextView mSelectDataView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        mSelectValueView = findViewById(R.id.select_value_view);
        mChangeDataView = findViewById(R.id.change_data_view);
        mSelectDataView = findViewById(R.id.select_data_view);

        mSelectValueView.setValueSelectListener(mValueSelectListener);
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
}
