package com.bnrc.bnrcbus.view.activity;

import android.annotation.SuppressLint;

import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.bnrc.bnrcbus.R;
import com.bnrc.bnrcbus.constant.Constants;
import com.bnrc.bnrcbus.database.PCUserDataDBHelper;
import com.bnrc.bnrcbus.listener.IPopWindowListener;
import com.bnrc.bnrcbus.model.Child;
import com.bnrc.bnrcbus.model.user.TokenInfo;
import com.bnrc.bnrcbus.network.RequestCenter;
import com.bnrc.bnrcbus.network.UpdateFromBaidu;
import com.bnrc.bnrcbus.network.VolleyNetwork;
import com.bnrc.bnrcbus.network.listener.DisposeDataListener;
import com.bnrc.bnrcbus.ui.CircleImageView;
import com.bnrc.bnrcbus.ui.LoadingDialog;
import com.bnrc.bnrcbus.ui.MyAlertDialog;
import com.bnrc.bnrcbus.ui.RTabHost;
import com.bnrc.bnrcbus.ui.SelectPicPopupWindow;
import com.bnrc.bnrcbus.util.GetToMarket;
import com.bnrc.bnrcbus.util.LocationUtil;
import com.bnrc.bnrcbus.util.NetAndGpsUtil;
import com.bnrc.bnrcbus.util.SharedPreferenceUtil;
import com.bnrc.bnrcbus.view.activity.base.BaseActivity;
import com.bnrc.bnrcbus.view.fragment.BaseFragment;
import com.bnrc.bnrcbus.view.fragment.HomeFragment;
import com.bnrc.bnrcbus.view.fragment.RouteFragment;
import com.joanzapata.iconify.widget.IconTextView;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;


/**
 * 创建首页及其他fragment
 */

public class HomeActivity extends BaseActivity implements View.OnClickListener,IPopWindowListener {

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

    private RelativeLayout mHomeLayout;
    private RelativeLayout mRouteLayout;
    private RelativeLayout mArLayout;
    private RelativeLayout mBusCircleLayout;
    private RelativeLayout mMessageLayout;

    private TextView icon_home,tv_home;
    private TextView icon_route,tv_route;
    private CircleImageView icon_ar;
    private TextView tv_ar;
    private TextView icon_message,tv_message;
    private TextView tv_welcome,tv_username;

    private DrawerLayout mDrawerLayout;
    private IconTextView icon_menu,icon_search;

    private CircleImageView user_icon;

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

    private MyAlertDialog mVersionDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mNetAndGpsUtil = NetAndGpsUtil.getInstance(getApplicationContext());
        init();
    }

    private void init(){
        //初始化页面中所有控件
        initView();
        initFragments();
        initTabHost();
        initLocationUtil();
        initDB();

        startAlertService();
//
//        checkVersion();
    }


    private void initLocationUtil(){
        mLocationUtil = LocationUtil.getInstance(this
                .getApplicationContext());
        if(mLocationUtil==null)
            mLocationUtil.startLocation(null);
    }

    private void initDB(){
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
                        SubwayActivity.class);
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

//    private void showShare() {
//        OnekeyShare oks = new OnekeyShare();
//        //关闭sso授权
//        oks.disableSSOWhenAuthorize();
//
//        // 分享时Notification的图标和文字  2.5.9以后的版本不调用此方法
//        //oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
//        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
//        oks.setTitle(getString(R.string.share));
//        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
//        oks.setTitleUrl("http://app.mi.com/details?id=com.bnrc.busapp&ref=search");
//        // text是分享文本，所有平台都需要这个字段
//        oks.setText("智享公交app，智能出行靠谱助手！\n下载地址：http://app.mi.com/details?id=com.bnrc.busapp&ref=search");
//        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
//        oks.setImagePath("http://file.market.xiaomi.com/thumbnail/PNG/l114/AppStore/07eab64b6d2d448020c25697708eef0e7aed08009");//确保SDcard下面存在此张图片
//        // url仅在微信（包括好友和朋友圈）中使用
//        oks.setUrl("http://app.mi.com/details?id=com.bnrc.busapp&ref=search");
//        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
//        oks.setComment("我是测试评论文本");
//        // site是分享此内容的网站名称，仅在QQ空间使用
//        oks.setSite(getString(R.string.app_name));
//        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
//        oks.setSiteUrl("http://sharesdk.cn");
//
//        // 启动分享GUI
//        oks.show(this);
//    }

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
        doLogin();
    }

//    public void checkVersion(){
//        int dbver = mSharePrefrenceUtil.getValue("");
//    }

//    private void showDBVersionDialog() {
//        mVersionDialog = new MyAlertDialog(this).builder().setTitle("数据库更新")
//                .setMsg("数据库不是最新版本，请更新数据库")
//                .setPositiveButton("确认", new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        updateDatabase(VolleyNetwork.beijingdbURL, "pc.db");
//                    }
//                }).setNegativeButton("取消", new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        // startAlertService();
//                        exitProcess();
//                    }
//                });
//        mVersionDialog.setCanceledOnTouchOutside(false);
//        mVersionDialog.setCancelable(false);
//        mVersionDialog.show();
//    }
//
//    private void showAppVersionDialog(String msg,final String url) {
//        mVersionDialog = new MyAlertDialog(this).builder().setTitle("应用版本更新")
//                .setMsg(msg).setPositiveButton("前往市场", new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        GetToMarket gu = new GetToMarket();
//                        Intent i = gu.getIntent(HomeActivity.this);
//                        boolean b = gu.judge(HomeActivity.this, i);
//                        if (b == false) {
//                            startActivity(i);
//                        } else
//                            Toast.makeText(HomeActivity.this, "请前往应用商店更新应用！",
//                                    Toast.LENGTH_LONG).show();
//                    }
//                })/*.setNegativeButton("取消", new OnClickListener() {
//					@Override
//					public void onClick(View v) {
//					}
//				})*/
//                .setNegativeButton("下载APK", new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
////						Log.d(TAG, "onClick: ");
//                        UpdateFromBaidu update = new UpdateFromBaidu(HomeActivity.this);
//                        update.showNoticeDialog(true, "检测到新版本，立即更新吗？", url,getFilesDir().getPath());
//                        Log.d(TAG, "onClick: filepath"+getFilesDir().getPath());
//                        /**/}
//                });
//        mVersionDialog.setCanceledOnTouchOutside(false);
//        //mVersionDialog.setCancelable(false);
//        mVersionDialog.show();
//    }
}
