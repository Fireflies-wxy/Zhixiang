package com.bnrc.busapp.view.activity;

import android.Manifest;
import android.annotation.SuppressLint;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bnrc.busapp.R;
import com.bnrc.busapp.constant.Constants;
import com.bnrc.busapp.database.PCDataBaseHelper;
import com.bnrc.busapp.database.PCUserDataDBHelper;
import com.bnrc.busapp.listener.IPopWindowListener;
import com.bnrc.busapp.model.Child;
import com.bnrc.busapp.model.user.TokenInfo;
import com.bnrc.busapp.network.HttpConstants;
import com.bnrc.busapp.network.RequestCenter;
import com.bnrc.busapp.network.UpdateFromBaidu;
import com.bnrc.busapp.network.VolleyNetwork;
import com.bnrc.busapp.network.listener.DisposeDataListener;
import com.bnrc.busapp.ui.CircleImageView;
import com.bnrc.busapp.ui.LoadingDialog;
import com.bnrc.busapp.ui.MyAlertDialog;
import com.bnrc.busapp.ui.ProgressLoadingDialog;
import com.bnrc.busapp.ui.RTabHost;
import com.bnrc.busapp.ui.SelectPicPopupWindow;
import com.bnrc.busapp.ui.sweetalert.SweetAlertDialog;
import com.bnrc.busapp.util.DownloadUtil;
import com.bnrc.busapp.util.GetToMarket;
import com.bnrc.busapp.util.LocationUtil;
import com.bnrc.busapp.util.NetAndGpsUtil;
import com.bnrc.busapp.util.Permissions.PermissionHelper;
import com.bnrc.busapp.util.Permissions.PermissionInterface;
import com.bnrc.busapp.util.Permissions.PermissionUtil;
import com.bnrc.busapp.util.SharedPreferenceUtil;
import com.bnrc.busapp.view.activity.base.BaseActivity;
import com.bnrc.busapp.view.fragment.BaseFragment;
import com.bnrc.busapp.view.fragment.HomeFragment;
import com.bnrc.busapp.view.fragment.RouteFragment;
import com.joanzapata.iconify.widget.IconTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;


/**
 * 创建首页及其他fragment
 */

public class HomeActivity extends BaseActivity implements View.OnClickListener,IPopWindowListener,PermissionInterface {

    private static SharedPreferenceUtil mSharePrefrenceUtil;
    private static final String TAG = "HomeActivity";

    private FragmentManager fm;
    private List<BaseFragment> fragmentList = new ArrayList<BaseFragment>();
    private BaseFragment mFragment;
    private List<Class<? extends BaseFragment>> classList = null;
    Class<? extends BaseFragment> fragClass = null;
    private RTabHost mTabHost;

    private int mLastIndex = 0;  //初始化时默认加载第一项"首页"
    private TextView tv_toolbar;
    private String[] titleList = {"智享公交","路线查询"};
    private double dbVersion, appVersion;

    private RelativeLayout mHomeLayout;
    private RelativeLayout mRouteLayout;
    private RelativeLayout mArLayout;
    private RelativeLayout mBusCircleLayout;

    private TextView icon_home,tv_home;
    private TextView icon_route,tv_route;
    private CircleImageView icon_ar;
    private TextView tv_ar;
    private TextView icon_message,tv_message;
    private TextView tv_welcome,tv_username;

    private DrawerLayout mDrawerLayout;
    private IconTextView icon_menu,icon_search;

    private CircleImageView user_icon;

    private VolleyNetwork mVolleyNetwork;
    private PCDataBaseHelper mPcDataBaseHelper = null;

    //分享图标
    private TextView icon_quit;
    private RelativeLayout rl_buscircle,rl_subway,rl_about,rl_setting,tv_feedback;

    private Child mChild;
    private RelativeLayout mCanversLayout;// 阴影遮挡图层
    private SelectPicPopupWindow menuWindow;
    private PCUserDataDBHelper mUserDB = null;

    //登录状态
    private String isLogin;

    private LocationUtil mLocationUtil = null;

    private String token,username;

    private NetAndGpsUtil mNetAndGpsUtil;

    private MyAlertDialog mVersionDialog,mAlertDialog;

    private PermissionHelper permissionHelper;

    private String[] mPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA};

    private ProgressLoadingDialog mProgressLoadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        permissionHelper = new PermissionHelper(this, this);
        permissionHelper.requestPermissions();
    }

    private void init(){
        //初始化页面中所有控件
        initLocationUtil();
        initDB();

        initView();
        initFragments();
        initTabHost();
        mNetAndGpsUtil = NetAndGpsUtil.getInstance(getApplicationContext());
        mVolleyNetwork = VolleyNetwork
                .getInstance(this.getApplicationContext());
        startAlertService();

        checkVersion();

        doLogin();
    }

    private void initProgressDialog(){
        mProgressLoadingDialog = new ProgressLoadingDialog(
                HomeActivity.this, R.style.Translucent_NoTitle);
        mProgressLoadingDialog.setCancelable(false);
        mProgressLoadingDialog.show();
    }

    private void initLocationUtil(){
        mLocationUtil = LocationUtil.getInstance(this
                .getApplicationContext());
        if(mLocationUtil==null)
            mLocationUtil.startLocation(null);
    }

    private void initDB(){
        mPcDataBaseHelper = PCDataBaseHelper
                .getInstance(getApplicationContext());
        mUserDB = PCUserDataDBHelper.getInstance(HomeActivity.this);
    }

    private void initView(){
        mHomeLayout = findViewById(R.id.home_layout_view);
        mArLayout = findViewById(R.id.ar_layout_view);
        mArLayout.setOnClickListener(this);
        mBusCircleLayout = findViewById(R.id.buscircle_layout_view);

        icon_home = findViewById(R.id.home_image_view);
        icon_ar = findViewById(R.id.ar_image_view);

        tv_home = findViewById(R.id.home_tv_view);
        tv_ar = findViewById(R.id.ar_tv_view);

        tv_toolbar = findViewById(R.id.tv_home_title);//标题文字

        mDrawerLayout = findViewById(R.id.drawer_layout);
        icon_menu = findViewById(R.id.menu_view);
        icon_search = findViewById(R.id.search_view);
        icon_search.setOnClickListener(this);

        user_icon = findViewById(R.id.icon_user);
        user_icon.setOnClickListener(this);

        tv_welcome = findViewById(R.id.tv_welcome);
        tv_username = findViewById(R.id.tv_username);

        rl_buscircle = findViewById(R.id.menu_service);
        rl_buscircle.setOnClickListener(this);
        rl_subway = findViewById(R.id.menu_railway);
        rl_subway.setOnClickListener(this);
        rl_setting = findViewById(R.id.menu_setting);
        rl_setting.setOnClickListener(this);
//        tv_feedback = findViewById(R.id.tv_feedback);
//        tv_feedback.setOnClickListener(this);
        rl_about = findViewById(R.id.menu_about);
        rl_about.setOnClickListener(this);

        icon_quit = findViewById(R.id.quit_image_view);
        icon_quit.setOnClickListener(this);

        mSharePrefrenceUtil = SharedPreferenceUtil.getInstance(this);

    }

    private void initFragments(){
        fragmentList.clear();
        classList = new ArrayList<>();
        classList.add(HomeFragment.class);
        classList.add(RouteFragment.class);

        fm = getSupportFragmentManager();
        FragmentTransaction transcation = fm.beginTransaction();

        fragClass = classList.get(mLastIndex);
        mFragment = createFragmentByClass(fragClass);
        transcation.replace(R.id.content_layout, mFragment).commitAllowingStateLoss();
    }

    private void doLogin(){
        username = mSharePrefrenceUtil.getValue("username","");
        token = mSharePrefrenceUtil.getValue("token","");
        isLogin = mSharePrefrenceUtil.getValue("isLogin","");
        if(isLogin.equals("true")){
            RequestCenter.checkToken(username,token,new DisposeDataListener() {
                @Override
                public void onSuccess(Object responseObj) {
                    TokenInfo info = (TokenInfo) responseObj;
                    if(info.code.equals("201")){
                        mSharePrefrenceUtil.setKey("isLogin","true");
                        tv_welcome.setText("欢迎您");
                        icon_quit.setVisibility(View.VISIBLE);
                        tv_username.setVisibility(View.VISIBLE);
                        tv_username.setText(mSharePrefrenceUtil.getValue("username","unknown"));
                    }else if(info.code.equals("401")){
                        Toast.makeText(HomeActivity.this.getApplicationContext(),"登录状态已过期",Toast.LENGTH_SHORT);
                        tv_welcome.setText("未登录");
                        icon_quit.setVisibility(View.INVISIBLE);
                        tv_username.setVisibility(View.INVISIBLE);
                    }else {
                        Log.i("logintest", "code: unknown");
                        icon_quit.setVisibility(View.INVISIBLE);
                        tv_username.setVisibility(View.INVISIBLE);
                    }
                }

                @Override
                public void onFailure(Object reasonObj) {
                    Log.i("logintest", "failed");
                }
            });
        }else {
            tv_welcome.setText("未登录");
            icon_quit.setVisibility(View.INVISIBLE);
            tv_username.setVisibility(View.INVISIBLE);
        }
    }


    private BaseFragment createFragmentByClass(
            Class<? extends BaseFragment> fragClass) {
        BaseFragment frag = null;
        try {
            try {
                Constructor<? extends BaseFragment> cons = null;
                cons = fragClass.getConstructor();
                frag = cons.newInstance();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        } catch (Throwable e) {
            throw new RuntimeException("Can not create instance for class "
                    + fragClass.getName(), e);
        }
        return frag;
    }

    private void initTabHost() {
        mTabHost = findViewById(R.id.tab_host);

        mTabHost.setQQTabHostListener(new RTabHost.RTabHostListener() {

            @Override
            public void onTabSelected(int index) {
                if(index>1)
                    index--;  //"-1" 是为了忽略"公交AR"这一项，此项不算做开启fragment
                selectTab(index);
                tv_toolbar.setText(titleList[index]);
            }
        });

        mTabHost.selectTab(mLastIndex);
    }

    private void selectTab(int index) {
        if (mLastIndex == index) {
            return;
        }
        mLastIndex = index;
        selectFragment(index);
    }

    private void selectFragment(int index) {

        FragmentTransaction transcation = getSupportFragmentManager().beginTransaction();
        fragClass = classList.get(index);
        mFragment = createFragmentByClass(fragClass);
        transcation.replace(R.id.content_layout, mFragment).commit();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void hideFragment(Fragment fragment, FragmentTransaction ft){
        if(fragment != null)
            ft.hide(fragment);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onClick(View view) {

        switch(view.getId()){
            case R.id.ar_layout_view:
                Intent arIntent = new Intent(HomeActivity.this,
                        ARActivity.class);
                startActivity(arIntent);
                break;
            case R.id.icon_user:
                if(tv_welcome.getText().toString().equals("未登录")){
                    Intent loginIntent = new Intent(HomeActivity.this,
                            LoginActivity.class);
                    startActivity(loginIntent);
                }
                break;
            case R.id.quit_image_view:
                showQuitDialog();
                break;
            case R.id.menu_service:
                Intent circleIntent = new Intent(HomeActivity.this,
                        BusCircleActivity.class);
                startActivity(circleIntent);
                break;
            case R.id.menu_railway:
                Intent subwayIntent = new Intent(HomeActivity.this,
                        SubWayActivity.class);
                startActivity(subwayIntent);
                break;
            case R.id.menu_setting:
                Intent settingIntent = new Intent(HomeActivity.this,
                        SettingActivity.class);
                startActivity(settingIntent);
                break;
//                case R.id.tv_feedback:
//                    break;
            case R.id.menu_about:
                Intent aboutInt = new Intent(HomeActivity.this, AboutActivity.class);
                startActivity(aboutInt);
                break;
//                showShare();

        }

    }

    public void showQuitDialog(){
        final AlertDialog.Builder quitDialog = new AlertDialog.Builder(HomeActivity.this);
        quitDialog.setTitle("注销提醒");
        quitDialog.setMessage("确定要退出登录么？");
        quitDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSharePrefrenceUtil.setKey("isLogin","false");
                doLogin();
            }
        });
        quitDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        quitDialog.show();
    }

    public void openDrawerLayout(View view){
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    public void onPopClick(Child child) {
        mChild = child;
        mCanversLayout = (RelativeLayout) findViewById(R.id.rlayout_shadow);
        menuWindow = new SelectPicPopupWindow(HomeActivity.this, mChild,
                mPopItemListener);
        menuWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {// 点击消失
                mCanversLayout.setVisibility(View.GONE);
            }
        });
        menuWindow.showAtLocation(
                HomeActivity.this.findViewById(R.id.drawer_layout), Gravity.BOTTOM
                        | Gravity.CENTER_HORIZONTAL, 0, 0);
        menuWindow.setFocusable(true);
        menuWindow.setOutsideTouchable(false);
        menuWindow.update();
        mCanversLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoginClick() {

    }

    @Override
    public LoadingDialog showLoading() {
        return null;
    }

    @Override
    public void dismissLoading() {

    }

    // 为弹出窗口实现监听类
    private View.OnClickListener mPopItemListener = new View.OnClickListener() {

        public void onClick(View v) {
            int LineID = mChild.getLineID();
            int StationID = mChild.getStationID();
            switch (v.getId()) {
                case R.id.iv_work:
                    mChild.setType(Constants.TYPE_WORK);
                    mUserDB.addFavRecord(mChild);
                    break;
                case R.id.iv_home:
                    mChild.setType(Constants.TYPE_HOME);
                    mUserDB.addFavRecord(mChild);
                    break;
                case R.id.iv_other:
                    mChild.setType(Constants.TYPE_OTHER);
                    mUserDB.addFavRecord(mChild);
                    break;
                case R.id.iv_del:
                    mUserDB.cancelFav(LineID, StationID);
                    mChild.setType(Constants.TYPE_NONE);
                    break;
                case R.id.btn_cancel:
                    break;
                default:
                    break;
            }
            menuWindow.dismiss();
            mFragment.refresh();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

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
        init();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionHelper.requestPermissionsResult(requestCode,permissions,grantResults);
        permissionHelper.requestPermissions();
    }

    public MyAlertDialog showAlertDialog() {
        if (mAlertDialog != null)
            mAlertDialog.dismiss();
        String stationName = myAlertBinder.getAlertStationName();
        mAlertDialog = new MyAlertDialog(HomeActivity.this).builder()
                .setTitle(stationName + "站").setMsg("即将到达 ，请注意下车！")
                .setNegativeButton("确认", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myAlertBinder.stopAlert();
                    }
                });
        // mAlertDialog.setCancelable(false);
        mAlertDialog.show();
        return mAlertDialog;
    }


    public void checkVersion() {
        final double localVersion = Double.parseDouble(mSharePrefrenceUtil
                .getValue("DBVersion", "2.3"));  //localVersion：本地数据库ver，本地记录
        PackageManager manager;
        PackageInfo info = null;
        manager = this.getPackageManager();
        try {
            info = manager.getPackageInfo(this.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        final double localAppVersion = Double.parseDouble(info.versionName);  //本地appVer
        Log.i("VERSION", localVersion + "before getuploadinfo" + info.versionName);
        mVolleyNetwork.getUploadInfo(new VolleyNetwork.appVersionListener() {

            @Override
            public void onSuccess(JSONObject obj) {
                // TODO Auto-generated method stub
                try {
                    double dVersion = Double.parseDouble(obj.getString("V"));
                    Log.i("VERSION", "dVersion: "+dVersion);
                    double aVersion = Double.parseDouble(obj.getString("appV"));
                    Log.i("VERSION", "aVersion: "+aVersion);
                    String Info = obj.getString("Info");  //这里记录着更新的信息
                    Log.i("VERSION", "msg: "+Info);
                    String url = obj.getString("url");
                    if (localVersion < dVersion)
                        showDBVersionDialog();
                    Log.i(TAG, "localAppVersion: " + localAppVersion
                            + " aVersion: " + aVersion);
                    if (localAppVersion < aVersion)
                        showAppVersionDialog(Info,url);
                    dbVersion = dVersion;
                    appVersion = aVersion;
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail() {
                // TODO Auto-generated method stub
                startAlertService();
                Log.d(TAG, "onFail: getversioninfo");
            }
        });
    }

    private void showDBVersionDialog() {
        mVersionDialog = new MyAlertDialog(this).builder().setTitle("数据库更新")
                .setMsg("数据库不是最新版本，请更新数据库")
                .setPositiveButton("确认", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        updateDatabase(VolleyNetwork.beijingdbURL, "pc.db");
                        initProgressDialog();
                        new Thread(new Runnable(){
                            @Override
                            public void run() {
                                downFile(HttpConstants.BEIJINGDB_URL);
                            }
                        }).start();

                    }
                }).setNegativeButton("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // startAlertService();
                        exitProcess();
                    }
                });
        mVersionDialog.setCanceledOnTouchOutside(false);
        mVersionDialog.setCancelable(false);
        mVersionDialog.show();
    }

    private void exitProcess() {
        finish();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }, 400);

    }

    private void showAppVersionDialog(String msg,final String url) {
        mVersionDialog = new MyAlertDialog(this).builder().setTitle("应用版本更新")
                .setMsg(msg).setPositiveButton("前往市场", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GetToMarket gu = new GetToMarket();
                        Intent i = gu.getIntent(HomeActivity.this);
                        boolean b = gu.judge(HomeActivity.this, i);
                        if (b == false) {
                            startActivity(i);
                        } else
                            Toast.makeText(HomeActivity.this, "请前往应用商店更新应用！",
                                    Toast.LENGTH_LONG).show();
                    }
                })/*.setNegativeButton("取消", new OnClickListener() {
					@Override
					public void onClick(View v) {
					}
				})*/
                .setNegativeButton("下载APK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//						Log.d(TAG, "onClick: ");
                        UpdateFromBaidu update = new UpdateFromBaidu(HomeActivity.this);
                        update.showNoticeDialog(true, "检测到新版本，立即更新吗？", url,getFilesDir().getPath());
                        Log.d(TAG, "onClick: filepath"+getFilesDir().getPath());
                        /**/}
                });
        mVersionDialog.setCanceledOnTouchOutside(false);
        //mVersionDialog.setCancelable(false);
        mVersionDialog.show();
    }

    /**
     * 文件下载
     */
    private void downFile(String url) {
        DownloadUtil.get().downloadDB(url, Environment.getExternalStorageDirectory().getAbsolutePath(), "pc.db",
                new DownloadUtil.OnDownloadListener() {
                    @Override
                    public void onDownloadSuccess(File file) {
                        if (mProgressLoadingDialog != null) {
                            mProgressLoadingDialog.dismiss();
                            mProgressLoadingDialog = null;
                        }
                        mPcDataBaseHelper.copyFile(file.getAbsolutePath(), "pc.db");
                        mSharePrefrenceUtil.setKey("UPDATEFAV", "no");
                        mSharePrefrenceUtil.setKey("UPDATEALERT", "no");
                        mSharePrefrenceUtil.setKey("DBVersion", dbVersion+"");
                        Looper.prepare();
                        Toast.makeText(HomeActivity.this.getApplicationContext(),"数据库更新完成",Toast.LENGTH_SHORT).show();
                        Looper.loop();
                        //下载完成进行相关逻辑操作
                    }

                    @Override
                    public void onDownloading(int progress) {
                        mProgressLoadingDialog.setProgress(progress);
                    }

                    @Override
                    public void onDownloadFailed(Exception e) {
                        //下载异常进行相关提示操作
                        startAlertService();
                        Looper.prepare();
                        Toast.makeText(HomeActivity.this.getApplicationContext(),"更新失败，请检查网络",Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                });


    }

}
