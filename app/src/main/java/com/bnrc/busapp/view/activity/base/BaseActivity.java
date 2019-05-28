
package com.bnrc.busapp.view.activity.base;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.bnrc.busapp.R;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.bnrc.busapp.service.AlertService;
import com.bnrc.busapp.ui.MyAlertDialog;
import com.bnrc.busapp.ui.LoadingDialog;


public class BaseActivity extends AppCompatActivity {

    private LoadingDialog mLoading;
    private Intent mAlertIntent, mScanWifiIntent;
    private LayoutInflater mInflater;


    private static final String TAG = "BaseActivity";

    protected AlertService.AlertBinder myAlertBinder;


    protected Handler mThreadHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            Log.d(TAG, "mThreadHandler。。。。。。");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mAlertIntent = new Intent(this, AlertService.class);
        bindService(mAlertIntent, mAlertConnection, BIND_AUTO_CREATE);
       // mScanWifiIntent = new Intent(this, ScanWifiService.class);
       // bindService(mScanWifiIntent, mScanWifiConnection, BIND_AUTO_CREATE);
    }


    public synchronized LoadingDialog showLoading() {
        if (mLoading == null) {
            mLoading = new LoadingDialog(this, R.layout.view_tips_loading);
            mLoading.setCancelable(false);
            mLoading.setCanceledOnTouchOutside(true);
        }
        if (!this.isFinishing() && this.mLoading != null)
            mLoading.show();
        return mLoading;
    }

    public synchronized void dismissLoading() {

        if (!this.isFinishing() && this.mLoading != null
                && this.mLoading.isShowing()) {
            mLoading.dismiss();
            mLoading = null;
        }
    }


    private ServiceConnection mAlertConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            Log.i(TAG, "mAlertConnection");
            myAlertBinder = (AlertService.AlertBinder) service;
            myAlertBinder.setListener(mListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            //bindService(mAlertIntent, mAlertConnection, BIND_AUTO_CREATE);
        }
    };

    private AlertService.onAlertListener mListener = new AlertService.onAlertListener() {

        @Override
        public void onCancelAlert() {
            // TODO Auto-generated method stub
            if (mAlertDialog != null)
                mAlertDialog.dismiss();
        }

        @Override
        public void onAlert() {
            // TODO Auto-generated method stub
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    showAlertDialog();

                }
            });
        }
    };

    protected void startAlertService() {
        Log.i("AlertTest", "startAlertService: ");
        if (myAlertBinder != null)
            myAlertBinder.startScanAlert();
        else {
            bindService();
            mThreadHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    startAlertService();
                }
            }, 3 * 1000);
        }
    }

    protected void bindService() {
        bindService(mAlertIntent, mAlertConnection, BIND_AUTO_CREATE);
    }

    private MyAlertDialog mAlertDialog;

    public MyAlertDialog showAlertDialog() {
        if (mAlertDialog != null)
            mAlertDialog.dismiss();
        String stationName = myAlertBinder.getAlertStationName();
        mAlertDialog = new MyAlertDialog(this).builder()
                .setTitle(stationName + "站").setMsg("即将到达 ，请注意下车！")
                .setNegativeButton("确认", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myAlertBinder.stopAlert();
                    }
                });
        mAlertDialog.setCancelable(false);
        if(!BaseActivity.this.isFinishing())
            mAlertDialog.show();
        return mAlertDialog;
    }


}