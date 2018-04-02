package mirahome.customapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Main1Activity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);

        findViewById(R.id.data_piker_btn).setOnClickListener(this);
        findViewById(R.id.height_piker_btn).setOnClickListener(this);
        findViewById(R.id.weight_piker_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        Intent intent = new Intent();
        switch (id) {
            case R.id.data_piker_btn:
                intent.setClass(this, DateAndValueActivity.class);
                startActivity(intent);
                break;
            case R.id.weight_piker_btn:
                intent.setClass(this, WeightActivity.class);
                startActivity(intent);
                break;
            case R.id.height_piker_btn:
                intent.setClass(this, HeightActivity.class);
                startActivity(intent);
                break;
        }
    }
}
