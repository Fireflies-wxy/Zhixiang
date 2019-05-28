package com.bnrc.busapp.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bnrc.busapp.R;
import com.bnrc.busapp.ui.ActionSheetDialog;
import com.bnrc.busapp.util.LocationUtil;
import com.bnrc.busapp.util.NetAndGpsUtil;
import com.bnrc.busapp.util.SharedPreferenceUtil;
import com.bnrc.busapp.view.activity.base.BaseActivity;


public class SettingActivity extends BaseActivity {
    private View selectSearchRView = null;
    private View selectAlertRView = null;
    private View selectBatteryModeView = null;
    private View refreshModeView = null;

    private TextView searchRTextView = null;
    private TextView alertRTextView = null;
    private TextView batteryModeTextView = null;
    // private TextView acceptAlertTextView = null;
    private TextView userSetView = null;
    private TextView shareAppTextView = null;
    private TextView feedbackTextView = null;
    private TextView aboutTextView = null;
    private TextView refreshTextView = null;
    // 收集wifi的相关设置
    private TextView tv_scanmethod, tv_scanap, tv_scantime;
    private View mDataViewRadius = null, mDataViewAlertDistance = null,
            mDataViewBattery = null;
    private View mDataViewScanMethod = null, mDataViewScanAp = null,
            mDataViewScanTime = null, mDataViewScanPrecision = null;
    private SharedPreferenceUtil mSharePrefrenceUtil;
    private LocationUtil mLocationUtil;
    private NetAndGpsUtil mNetAndGpsUtil;
    private String[] searchRadius;
    private String[] alertRadius;
    private String[] refreshFrequency;
    private String[] batteryArray;
    RelativeLayout mAdContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mSharePrefrenceUtil = SharedPreferenceUtil.getInstance(this
                .getApplicationContext());
        mLocationUtil = LocationUtil.getInstance(this.getApplicationContext());
        mNetAndGpsUtil = NetAndGpsUtil
                .getInstance(this.getApplicationContext());
        initDataArray();
        searchRTextView = (TextView) findViewById(R.id.searchRTv);
        alertRTextView = (TextView) findViewById(R.id.alertRTv);
        batteryModeTextView = (TextView) findViewById(R.id.batteryModeTv);
        refreshTextView = (TextView) findViewById(R.id.refreshModeTv);
//		userSetView = (TextView) findViewById(R.id.userSetTv);
//		shareAppTextView = (TextView) findViewById(R.id.shareAppTv);
//		feedbackTextView = (TextView) findViewById(R.id.feedbackTv);
//		aboutTextView = (TextView) findViewById(R.id.aboutTv);

        findViewById(R.id.menu_view).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        String searchradius = mSharePrefrenceUtil.getValue("searchRadius",
                "500米");
        String alertradius = mSharePrefrenceUtil
                .getValue("alertRadius", "500米");
        String battery = mSharePrefrenceUtil.getValue("battery", "智能切换");
        String refreshfrequency = mSharePrefrenceUtil.getValue(
                "refreshFrequency", "30秒");

        searchRTextView.setText(searchradius);
        alertRTextView.setText(alertradius);
        batteryModeTextView.setText(battery);
        refreshTextView.setText(refreshfrequency);

        // userSetView.setOnClickListener(new OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        // // Intent intent = null;
        // Intent intent = new Intent(SettingView.this,
        // UserSettingView.class);
        // // ������ͼ
        // startActivity(intent);
        // }
        // });

        searchRTextView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // showDialog(AbConstant.DIALOGBOTTOM, selectSearchRView, 40);
                showSelectDialog("设置搜索半径", searchRadius, "searchRadius",
                        new onChangeText() {

                            @Override
                            public void onChange(String text) {
                                // TODO Auto-generated method stub
                                searchRTextView.setText(text);
                            }

                            @Override
                            public void onCancel() {
                                // TODO Auto-generated method stub

                            }
                        });
            }
        });

        alertRTextView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // showDialog(AbConstant.DIALOGBOTTOM, selectAlertRView, 40);
                showSelectDialog("设置提醒半径", alertRadius, "alertRadius",
                        new onChangeText() {

                            @Override
                            public void onChange(String text) {
                                // TODO Auto-generated method stub
                                alertRTextView.setText(text);
                            }

                            @Override
                            public void onCancel() {
                                // TODO Auto-generated method stub

                            }
                        });

            }
        });

        batteryModeTextView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // showDialog(AbConstant.DIALOGBOTTOM, selectBatteryModeView,
                // 40);
                showSelectDialog("设置电量模式", batteryArray, "battery",
                        new onChangeText() {

                            @Override
                            public void onChange(String text) {
                                // TODO Auto-generated method stub
                                batteryModeTextView.setText(text);
                            }

                            @Override
                            public void onCancel() {
                                // TODO Auto-generated method stub

                            }
                        });

            }

        });

        refreshTextView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // showDialog(AbConstant.DIALOGBOTTOM, refreshModeView, 40);
                showSelectDialog("设置刷新频率", refreshFrequency,
                        "refreshFrequency", new onChangeText() {

                            @Override
                            public void onChange(String text) {
                                // TODO Auto-generated method stub
                                refreshTextView.setText(text);
                            }

                            @Override
                            public void onCancel() {
                                // TODO Auto-generated method stub

                            }
                        });

            }

        });

//		feedbackTextView.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
////				// TODO Auto-generated method stub
////				FeedbackAgent agent = new FeedbackAgent(SettingView.this);
////				agent.startFeedbackActivity();
////				agent.sync();
//			}
//		});
//
//		aboutTextView.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//				// TODO Auto-generated method stub
////				Intent intent = new Intent(SettingView.this,
////						AboutActivity.class);
////				startActivity(intent);
//			}
//		});


    }

    private void initDataArray() {
        searchRadius = getResources().getStringArray(
                R.array.Setting_searchRadius);
        alertRadius = getResources()
                .getStringArray(R.array.Setting_alertRadius);
        refreshFrequency = getResources().getStringArray(
                R.array.Setting_refreshFrequency);
        batteryArray = getResources().getStringArray(R.array.Setting_battery);
    }

    private ActionSheetDialog mDialog;

    private void showSelectDialog(String title, String[] array,
                                  final String key, final onChangeText listener) {
        if (mDialog == null) {
            mDialog = new ActionSheetDialog(this).builder();
            mDialog.setTitle(title);
            for (final String str : array) {
                mDialog = mDialog.addSheetItem(str, ActionSheetDialog.SheetItemColor.Blue,
                        new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int which) {
                                mSharePrefrenceUtil.setKey(key, str);
                                if (key.equalsIgnoreCase("battery")) {
                                    mLocationUtil.initLocation();
                                }
                                listener.onChange(str);
                                mDialog = null;
                            }
                        });
            }
            mDialog.setOnDismissListener(new ActionSheetDialog.onListener() {

                @Override
                public void onDismiss() {
                    // TODO Auto-generated method stub
                    mDialog = null;
                }

            });
        }
        mDialog.show();
    }

    public void onResume() {
        super.onResume();


    }

    public void onPause() {
        super.onPause();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }
}

interface onChangeText {
    void onChange(String text);

    void onCancel();
}