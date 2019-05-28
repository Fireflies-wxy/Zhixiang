package com.bnrc.busapp.view.fragment;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bnrc.busapp.R;
import com.bnrc.busapp.constant.Constants;
import com.bnrc.busapp.ui.LoadingDialog;

public class BaseFragment extends Fragment{

    private static final String TAG = BaseFragment.class.getSimpleName();

    protected Activity mContext;

    private LoadingDialog mLoading;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.i(TAG, this.getClass().getSimpleName() + "onCreateView");

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * 申请指定的权限.
     */
    public void requestPermission(int code, String... permissions) {

        Log.i(TAG,"requestPermission");

        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(permissions, code);
        }
    }

    /**
     * 判断是否有指定的权限
     */
    public boolean hasPermission(String... permissions) {

        for (String permisson : permissions) {
            if (ContextCompat.checkSelfPermission(getActivity(), permisson)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Constants.HARDWEAR_CAMERA_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    doOpenCamera();
                }
                break;
            case Constants.WRITE_READ_EXTERNAL_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    doWriteSDCard();
                }
                break;
        }
    }

    public void doOpenCamera() {

    }

    public void doWriteSDCard() {

    }

    // 刷新实时数据
    public void refresh() {

    }

    // 刷新实时数据
    public void refreshConcern() {

    }

    public synchronized LoadingDialog showLoading() {
        if (mLoading == null) {
            mLoading = new LoadingDialog(getActivity(), R.layout.view_tips_loading);
            mLoading.setCancelable(false);
            mLoading.setCanceledOnTouchOutside(true);
        }
        if (!getActivity().isFinishing() && this.mLoading != null)
            mLoading.show();
        return mLoading;
    }

    public synchronized void dismissLoading() {

        if (!getActivity().isFinishing() && this.mLoading != null
                && this.mLoading.isShowing()) {
            mLoading.dismiss();
            mLoading = null;
        }
    }


}