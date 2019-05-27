package com.bnrc.bnrcbus.view.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.bnrc.bnrcbus.R;
import com.bnrc.bnrcbus.util.LocationUtil;
import com.bnrc.bnrcbus.util.Permissions.PermissionHelper;
import com.bnrc.bnrcbus.util.Permissions.PermissionInterface;
import com.bnrc.bnrcbus.util.Permissions.PermissionUtil;
import com.bnrc.bnrcbus.view.activity.base.BaseActivity;

public class SplashActivity extends BaseActivity{
    private static final String TAG = "SplashActivity";

    private int TIME = 3;  //3秒

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                /*
                 * Create an Intent that will start the Main WordPress Activity.
                 */
                Intent intent = new Intent(SplashActivity.this,
                        HomeActivity.class);
                SplashActivity.this.startActivity(intent);
                SplashActivity.this.finish();
            }
        }, TIME * 1000); // 2900 for release
    }

}
