package com.bnrc.busapp.view.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bnrc.busapp.R;
import com.bnrc.busapp.model.comment.RateInfo;
import com.bnrc.busapp.network.RequestCenter;
import com.bnrc.busapp.network.listener.DisposeDataListener;
import com.bnrc.busapp.util.SharedPreferenceUtil;

public class RateActivity extends AppCompatActivity {

    private static final String TAG = "RateActivity";

    private View mContentView;
    private RadioGroup mRgCarStatus, mRgWaitStatus;
    private int StationID;
    private int LineID;
    private String  StationName;
    private String LineName;
    private int busStatusRate;
    private int stationStatusRate;
    private Button mButton;
    private Intent intent;
    private TextView tv_bus_status,tv_station_status,icon_back;
    private int uid;

    private SharedPreferenceUtil  mSharedPreferenceUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);
        mSharedPreferenceUtil = SharedPreferenceUtil.getInstance(this);
        initView();
    }


    public void initView() {
        mRgCarStatus =findViewById(R.id.rg_bus_status);
        mRgWaitStatus = findViewById(R.id.rg_station_status);
        tv_bus_status = findViewById(R.id.tv_bus_status);
        tv_station_status = findViewById(R.id.tv_station_status);
        icon_back = findViewById(R.id.icon_back);
        icon_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        intent = getIntent();
        LineID = intent.getIntExtra("LineID",0);
        StationID = intent.getIntExtra("StationID",0);
        LineName = intent.getStringExtra("LineName");
        StationName = intent.getStringExtra("StationName");
        mButton = findViewById(R.id.btn_sunmit);

        tv_bus_status.setText("线路: "+LineName);
        tv_station_status.setText("站点: "+StationName);

        uid = Integer.parseInt(mSharedPreferenceUtil.getValue("uid","1"));

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RequestCenter.submitRate(uid,LineID,busStatusRate,StationID,stationStatusRate, new DisposeDataListener() {
                    @Override
                    public void onSuccess(Object responseObj) {
                        Log.i(TAG, "onSuccess: ");
                        RateInfo info = (RateInfo) responseObj;
                        Log.i(TAG, "onSuccess: "+info.code);
                        if(info.code == 201){
                            Toast.makeText(RateActivity.this.getApplicationContext(),"评分成功，感谢您的反馈。",Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        Toast.makeText(RateActivity.this.getApplicationContext(),"评分成功，感谢您的反馈。",Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onFailure(Object reasonObj) {
                        Log.i(TAG, "onFailure: ");
                        Toast.makeText(RateActivity.this.getApplicationContext(),"评分失败",Toast.LENGTH_SHORT).show();
                    }
                });


            }
        });

        mRgCarStatus.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.rbtn_bus_status_low:
                        busStatusRate = 1;
                        break;
                    case R.id.rbtn_bus_status_mid:
                        busStatusRate = 2;
                        break;
                    case R.id.rbtn_bus_status_high:
                        busStatusRate = 3;
                        break;
                }
            }
        });

        mRgWaitStatus.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.rbtn_station_status_low:
                        stationStatusRate = 1;
                        break;
                    case R.id.rbtn_station_status_mid:
                        stationStatusRate = 2;
                        break;
                    case R.id.rbtn_station_status_high:
                        stationStatusRate = 3;
                        break;
                }
            }
        });

    }


}
