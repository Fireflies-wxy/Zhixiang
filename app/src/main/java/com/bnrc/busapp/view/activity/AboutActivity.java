package com.bnrc.busapp.view.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.bnrc.busapp.R;
import com.bnrc.busapp.view.activity.base.BaseActivity;


public class AboutActivity extends BaseActivity {
    String version = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // logoView.setOnClickListener(new View.OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        // finish();
        // }
        // });
        TextView version_val = ((TextView) findViewById(R.id.version_val));

        findViewById(R.id.menu_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(
                    "com.andbase", PackageManager.GET_CONFIGURATIONS);
            version = pinfo.versionName;
            version_val.setText("V" + version);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
