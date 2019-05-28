package com.bnrc.busapp.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.bnrc.busapp.R;
import com.bnrc.busapp.view.activity.base.BaseActivity;

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
