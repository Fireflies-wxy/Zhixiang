package com.bnrc.busapp.view.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.bnrc.busapp.R;
import com.bnrc.busapp.util.Permissions.PermissionsHelper;
import com.bnrc.busapp.util.Permissions.info.DialogInfo;
import com.bnrc.busapp.util.Permissions.permission.DangerousPermissions;
import com.bnrc.busapp.util.SPUtils;
import com.bnrc.busapp.view.activity.base.BaseActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";

    protected List<String> permissions = new ArrayList<>();
    private static String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA};
    protected PermissionsHelper permissionsHelper;
    private int TIME = 3;  //3秒

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        setDangerousPermissions(permissions);
        checkPermissions();

    }

    private void checkPermissions(){
        List<String> finalPermissions = checkParams(permissions);
        if (finalPermissions == null || finalPermissions.size() <= 0) {
            allPermissionsGranted();
            return;
        }
        String[] checkPermissions = new String[finalPermissions.size()];
        checkPermissions = finalPermissions.toArray(checkPermissions);
        for (String checkPermission : checkPermissions) {
            Log.e("testpermission","checkPermission: splash"+checkPermission);
        }
        permissionsHelper = new PermissionsHelper(this, checkPermissions,isFirstTime());
        permissionsHelper.setonAllNeedPermissionsGrantedListener(new PermissionsHelper
                .onAllNeedPermissionsGrantedListener() {


            @Override
            public void onAllNeedPermissionsGranted() {
                allPermissionsGranted();
            }

            @Override
            public void onPermissionsDenied() {
                permissionsDenied();
            }

            @Override
            public void hasLockForever() {
                shouldNOTShowRequest();
            }

            @Override
            public void onBeforeRequestFinalPermissions(PermissionsHelper helper) {
                beforeRequestFinalPermissions(helper);
            }
        });

        permissionsHelper.setonNegativeButtonClickListener(new PermissionsHelper.onNegativeButtonClickListener() {


            @Override
            public void negativeButtonClick() {
                onNegativeButtonClick();
            }
        });
        if (permissionsHelper.checkAllPermissions(checkPermissions)) {
            permissionsHelper.onDestroy();
            allPermissionsGranted();//没有需要申请的权限
        } else {
            //申请权限
            permissionsHelper.startRequestNeedPermissions();
            initParams();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        permissionsHelper.onActivityResult(requestCode, resultCode, data);
    }

    private List<String> checkParams(List<String> permissions) {
        if (permissions == null || permissions.size() <= 0) return null;
        List<String> allPermissions = Arrays.asList(PERMISSIONS);
        List<String> finalPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (allPermissions.contains(permission)) {
                finalPermissions.add(permission);
            }
        }
        return finalPermissions;
    }

    protected Boolean isFirstTime() {
        boolean first = (boolean) SPUtils.getData(this, "first", false);
        if (!first){
            SPUtils.putData(this,"first",true);
        }
        return !first;
    }

    protected void allPermissionsGranted() {
        Log.i("testpermission", "allPermissionsGranted: ");
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

    protected void permissionsDenied() {
        Toast.makeText(this, "permissionsDenied", Toast.LENGTH_SHORT).show();
    }

    protected void shouldNOTShowRequest() {
        Toast.makeText(this, "永远不需要展示", Toast.LENGTH_SHORT).show();
    }

    protected void beforeRequestFinalPermissions(PermissionsHelper helper){
        helper.continueRequestPermissions();
    }

    protected DialogInfo setDialogInfo(DialogInfo dialogInfo) {
        dialogInfo.setTitle("权限申请");
        dialogInfo.setContent("本应用需要您许可相应权限才能正常运行!");
        dialogInfo.setPositiveButtonText("去设置");
        dialogInfo.setNegativeButtonText("取消");
        dialogInfo.showDialog(true);
        return dialogInfo;
    }

    private void initParams(){
        permissionsHelper.setParams(setDialogInfo(new DialogInfo()));
    }

    protected void onNegativeButtonClick(){
        Log.d("PermissionsHelper","onNegativeButtonClick");
    }

    protected void setDangerousPermissions(List<String> permissions) {
        permissions.add(DangerousPermissions.STORAGE);
        permissions.add(DangerousPermissions.CAMERA);
        permissions.add(DangerousPermissions.LOCATION);
    }

}
