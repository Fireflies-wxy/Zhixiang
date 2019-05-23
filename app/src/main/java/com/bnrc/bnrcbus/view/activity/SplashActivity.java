package com.bnrc.bnrcbus.view.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.bnrc.bnrcbus.R;
import com.bnrc.bnrcbus.util.LocationUtil;
import com.bnrc.bnrcbus.util.Permissions.PermissionHelper;
import com.bnrc.bnrcbus.util.Permissions.PermissionInterface;
import com.bnrc.bnrcbus.util.Permissions.PermissionUtil;
import com.bnrc.bnrcbus.view.activity.base.BaseActivity;

public class SplashActivity extends BaseActivity implements PermissionInterface {

    private int TIME = 1;  //3秒
    private String[] mPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA};
    private PermissionHelper permissionHelper;
    private LocationUtil mLocationUtil = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //初始化
        permissionHelper = new PermissionHelper(this, this);
        //申请权限
        permissionHelper.requestPermissions();
    }

    @Override
    public int getPermissionsRequestCode() {
        return 0;
    }

    @Override
    public String[] getPermissions() {
        return mPermissions;
    }

    @Override
    public void requestPermissionsSuccess() {
        mLocationUtil = LocationUtil.getInstance(this
                .getApplicationContext());
        mLocationUtil.startLocation();

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

    @Override
    public void requestPermissionsFail() {
        StringBuilder sb = new StringBuilder();
        mPermissions = PermissionUtil.getDeniedPermissions(this, mPermissions);
        for (String s : mPermissions) {
            if (s.equals(Manifest.permission.CAMERA)) {
                sb.append("相机权限(用于拍照，视频聊天);\n");
            } else if (s.equals(Manifest.permission.RECORD_AUDIO)) {
                sb.append("麦克风权限(用于发语音，语音及视频聊天);\n");
            } else if (s.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                sb.append("读取权限(用于读取数据);\n");
            }else if (s.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                sb.append("存储(用于存储必要信息，缓存数据);\n");}
        }
        PermissionUtil.PermissionDialog(this, "程序运行需要如下权限：\n" + sb.toString() + "请在应用权限管理进行设置！");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] mPermissions, int[] grantResults) {
        if (permissionHelper.requestPermissionsResult(requestCode, mPermissions, grantResults)) {
            //权限请求结果，并已经处理了该回调
            return;
        }
        super.onRequestPermissionsResult(requestCode, mPermissions, grantResults);
    }
}
