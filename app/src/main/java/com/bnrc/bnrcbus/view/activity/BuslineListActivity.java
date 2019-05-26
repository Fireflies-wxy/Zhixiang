package com.bnrc.bnrcbus.view.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.bnrc.bnrcbus.R;
import com.bnrc.bnrcbus.adapter.HorizontalListViewAdapter;
import com.bnrc.bnrcbus.constant.Constants;
import com.bnrc.bnrcbus.database.PCDataBaseHelper;
import com.bnrc.bnrcbus.database.PCUserDataDBHelper;
import com.bnrc.bnrcbus.model.Child;
import com.bnrc.bnrcbus.network.VolleyNetwork;
import com.bnrc.bnrcbus.ui.SelectPicPopupWindow;
import com.bnrc.bnrcbus.util.AnimationUtil;
import com.bnrc.bnrcbus.util.LocationUtil;
import com.bnrc.bnrcbus.util.MyCipher;
import com.bnrc.bnrcbus.util.NetAndGpsUtil;
import com.bnrc.bnrcbus.view.activity.base.BaseActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BuslineListActivity extends BaseActivity {
    private static final String TAG = BuslineListActivity.class
            .getSimpleName();
    public PCDataBaseHelper mDataBaseManager = null;
    public PCUserDataDBHelper mUserDataBaseHelper = null;
    private NetAndGpsUtil mNetAndGpsUtil;
    private int StationID;
    private int LineID;
    private int OfflineID;
    private int Sequence;
    private String LineName = "";
    private String StationName = "";
    private String StartStation = "";
    private String EndStation = "";
    private String FullName = "???";
    private String startTime = "???";
    private String endTime = "???";
    private String loadingInfo = "  正在加载...";
    private String rtInfo = "";
    private List<Child> mStations = null;
    private Map<Integer, Integer> mHasRtStationList = null;
    private TimerTask task;
    private Timer timer;
    private TextView mViewInMap;
    private LinearLayout mChangeDirecLayout, mAlertLayout, mConcernLayout,
            mCorrectMistakeLayout, mRefreshLayout;
    private ImageView mChangeDirecBtn, mAlertBtn, mConcernBtn, mOnOffBtn;
    private TextView mStartTime, mEndTime, mRtInfo, mOnOff;
    private HorizontalListViewAdapter mBuslineAdapter;
    private ListView mBuslineListView;
    private Child mSelectedChild;
    private ProgressDialog pd = null;
    private RelativeLayout mCanversLayout;// 阴影遮挡图层
    private SelectPicPopupWindow menuWindow;
    // 定义Handler对象
    private Handler mHandler = new Handler();
    private VolleyNetwork mVolleyNetwork;
    //	private AbTitleBar mAbTitleBar;
    private LocationUtil mLocationUtil;
    private CoordinateConverter mCoordConventer;

    private TextView tv_busline_title,busline_menu_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_buslinelist);
        tv_busline_title = findViewById(R.id.tv_busline_title);
        busline_menu_view = findViewById(R.id.busline_menu_view);
        busline_menu_view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.busline_menu_view:
                        finish();
                        break;
                }
            }
        });

        Intent intent = getIntent();
        LineID = intent.getIntExtra("LineID", 0);
        StationID = intent.getIntExtra("StationID", 0);
        Sequence = intent.getIntExtra("Sequence", 0);
        mDataBaseManager = PCDataBaseHelper
                .getInstance(BuslineListActivity.this);
        mUserDataBaseHelper = PCUserDataDBHelper
                .getInstance(BuslineListActivity.this);
        mNetAndGpsUtil = NetAndGpsUtil
                .getInstance(this.getApplicationContext());

        initview();
        setListener();
        mBuslineAdapter = new HorizontalListViewAdapter(this, mStations,
                R.layout.horizontallistview_item_parallel, new String[] {
                "itemsTitle", "isCurStation" }, new int[] {
                R.id.iv_bus, R.id.tv_stationName });
        mBuslineListView.setAdapter(mBuslineAdapter);
        mHasRtStationList = new HashMap<Integer, Integer>();
        mVolleyNetwork = VolleyNetwork.getInstance(this);
        mLocationUtil = LocationUtil.getInstance(this.getApplicationContext());
        mCoordConventer = new CoordinateConverter();
    }

    private void updateLineInfo() {
//		mAbTitleBar.setTitleText(FullName);
        tv_busline_title.setText(FullName);
//		mStartTime.setText(formatString(R.string.startTime, startTime));
//		mEndTime.setText(formatString(R.string.endTime, endTime));
    }

    private void initview() {
        mChangeDirecLayout = findViewById(R.id.lLayout_changeDirec);
        mAlertLayout = findViewById(R.id.lLayout_addAlert);
        mConcernLayout = findViewById(R.id.lLayout_addFav);
        mRefreshLayout = findViewById(R.id.lLayout_refresh);
        mChangeDirecBtn =  findViewById(R.id.iv_changeDirec);
        mOnOffBtn =  findViewById(R.id.iv_onoff);
        mOnOff =  findViewById(R.id.tv_onoff);
        mAlertBtn =  findViewById(R.id.iv_addAlert);
        mConcernBtn =  findViewById(R.id.iv_addFav);
        mCorrectMistakeLayout =  findViewById(R.id.lLayout_correct);
        mViewInMap =  findViewById(R.id.iv_map);
//		mStartTime =  findViewById(R.id.tv_startTime);
//		mEndTime =  findViewById(R.id.tv_endTime);
        mRtInfo =  findViewById(R.id.tv_rtInfo);
        mCanversLayout =  findViewById(R.id.rlayout_shadow);
        mBuslineListView =  findViewById(R.id.mBuslineListView);
        mBuslineListView.setVisibility(View.INVISIBLE);
        mRtInfo.setText(Html.fromHtml(loadingInfo));
        tv_busline_title = findViewById(R.id.tv_busline_title);
        busline_menu_view = findViewById(R.id.busline_menu_view);


        if (mUserDataBaseHelper.IsFavStation(LineID, StationID))
            mConcernBtn.setImageResource(R.drawable.icon_horizon_like);
        else
            mConcernBtn.setImageResource(R.drawable.icon_horizon_dislike);

        if (mUserDataBaseHelper.IsAlertOpenBusline(LineID, StationID))
            mAlertBtn.setImageResource(R.drawable.icon_horizon_alarm);
        else
            mAlertBtn.setImageResource(R.drawable.icon_horizon_disalarm);

        if (mVolleyNetwork.lineList == LineID) {
            mOnOffBtn.setImageResource(R.drawable.icon_offbus);
            mOnOff.setText("下 车");
        } else {
            mAlertBtn.setImageResource(R.drawable.icon_onbus);
            mOnOff.setText("上 车");
        }
    }

    private String formatString(int id, String info) {
        return String.format(getResources().getString(id), info);
    }

    private void setListener() {
        mChangeDirecLayout.setOnClickListener(mClickListener);
        mAlertLayout.setOnClickListener(mClickListener);
        mConcernLayout.setOnClickListener(mClickListener);
        mRefreshLayout.setOnClickListener(mClickListener);
        mCorrectMistakeLayout.setOnClickListener(mClickListener);
        mViewInMap.setOnClickListener(mClickListener);
        busline_menu_view.setOnClickListener(mClickListener);
        mBuslineListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {
                // TODO Auto-generated method stub
                showLoading();
                rtInfo = "???";
                mRtInfo.setText(Html.fromHtml(loadingInfo));
                mBuslineAdapter.setSelectIndex(position);
                mBuslineAdapter.notifyDataSetChanged();
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        if (position < 2)
                            mBuslineListView.setSelection(0);
                        else
                            mBuslineListView.setSelection(position - 2);

                    }
                });
                Sequence = position + 1;
                StationID = mStations.get(position).getStationID();
                StationName = mStations.get(position).getStationName();
                if (mUserDataBaseHelper.IsFavStation(LineID, StationID))
                    mConcernBtn.setImageResource(R.drawable.icon_horizon_like);
                else
                    mConcernBtn
                            .setImageResource(R.drawable.icon_horizon_dislike);
                if (mUserDataBaseHelper.IsAlertOpenBusline(LineID, StationID))
                    mAlertBtn.setImageResource(R.drawable.icon_horizon_alarm);
                else
                    mAlertBtn
                            .setImageResource(R.drawable.icon_horizon_disalarm);
                // getRtParam();
                getSeverInfo();
            }
        });

    }

    private void showSelectPopWindow(Child child) {
        menuWindow = new SelectPicPopupWindow(BuslineListActivity.this,
                child, mPopItemListener);
        menuWindow.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {// 点击消失
                mCanversLayout.setVisibility(View.GONE);
            }
        });
        menuWindow.showAtLocation(
                BuslineListActivity.this.findViewById(R.id.rLayout),
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        menuWindow.setFocusable(true);
        menuWindow.setOutsideTouchable(false);
        menuWindow.update();
        mCanversLayout.setVisibility(View.VISIBLE);
    }

    private OnClickListener mClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch (v.getId()) {
                case R.id.busline_menu_view:
                    finish();
                    break;
                case R.id.lLayout_addAlert:
                    if (mStations == null || Sequence > mStations.size())
                        return;
                    mSelectedChild = mStations.get(Sequence - 1);
                    if (mUserDataBaseHelper.IsAlertOpenBusline(LineID, StationID)) {
                        mUserDataBaseHelper.closeAlertBusline(mSelectedChild);
                        mAlertBtn
                                .setImageResource(R.drawable.icon_horizon_disalarm);
                    } else {
                        mUserDataBaseHelper.openAlertBusline(mSelectedChild);
                        mAlertBtn.setImageResource(R.drawable.icon_horizon_alarm);

                    }
                    break;
                case R.id.lLayout_addFav:
                    if (mStations == null || Sequence > mStations.size())
                        return;
                    mSelectedChild = mStations.get(Sequence - 1);
                    int Type = PCUserDataDBHelper.getInstance(
                            BuslineListActivity.this).IsWhichKindFavInfo(
                            LineID, StationID);
                    mSelectedChild.setType(Type);
                    showSelectPopWindow(mSelectedChild);
                    break;
                case R.id.lLayout_changeDirec:
                    Log.i(TAG, "mChangeDirec");
                    changeDirection();
                    break;
                case R.id.iv_map:
                    Log.i(TAG, "mViewInMap");
                    Intent intent = new Intent(BuslineListActivity.this,
                            BuslineMapActivity.class);
                    intent.putExtra("LineName", LineName);
                    intent.putExtra("StartStation", StartStation);
                    intent.putExtra("EndStation", EndStation);
                    intent.putExtra("LineID", LineID);
                    intent.putExtra("OfflineID", OfflineID);
                    startActivity(intent);
                    AnimationUtil
                            .activityZoomAnimation(BuslineListActivity.this);
                    break;
                case R.id.lLayout_correct:
//                    Intent corrIntent = new Intent(BuslineListActivity.this,
//                            CorrectMistakeActivity.class);
//                    startActivity(corrIntent);
//                    AnimationUtil
//                            .activityZoomAnimation(BuslineListActivity.this);
                    break;
                case R.id.lLayout_refresh:
                    // showLoading();
                    // getSeverInfo();
                    if (mOnOff.getText().toString().equalsIgnoreCase("上 车")) {
                        mOnOff.setText("下 车");
                        mOnOffBtn.setImageResource(R.drawable.icon_offbus);
                        mVolleyNetwork.lineList = LineID;
                        mVolleyNetwork.startPostMessage();

                    } else {
                        mOnOff.setText("上 车");
                        mOnOffBtn.setImageResource(R.drawable.icon_onbus);
                        mVolleyNetwork.lineList = 0;
                        mVolleyNetwork.stopPostMessage();
                    }
                default:
                    break;
            }
        }
    };

    // 为弹出窗口实现监听类
    private OnClickListener mPopItemListener = new OnClickListener() {

        public void onClick(View v) {
            // Map<String, Object> record = new HashMap<String, Object>();
            Child child = mSelectedChild;
            child.setEndStation(EndStation);
            int LineID = child.getLineID();
            int StationID = child.getStationID();
            switch (v.getId()) {
                case R.id.iv_work:
                    child.setType(Constants.TYPE_WORK);
                    mUserDataBaseHelper.addFavRecord(child);
                    break;
                case R.id.iv_home:
                    child.setType(Constants.TYPE_HOME);
                    mUserDataBaseHelper.addFavRecord(child);
                    break;
                case R.id.iv_other:
                    child.setType(Constants.TYPE_OTHER);
                    mUserDataBaseHelper.addFavRecord(child);
                    break;
                case R.id.iv_del:
                    mUserDataBaseHelper.cancelFav(LineID, StationID);
                    child.setType(Constants.TYPE_NONE);
                    break;
                case R.id.btn_cancel:
                    break;
                default:
                    break;
            }
            menuWindow.dismiss();
            if (mUserDataBaseHelper.IsFavStation(LineID, StationID))
                mConcernBtn.setImageResource(R.drawable.icon_horizon_like);
            else
                mConcernBtn.setImageResource(R.drawable.icon_horizon_dislike);

        }
    };

    private void getRtParam() {
        mHasRtStationList.clear();
        mBuslineAdapter.notifyDataSetChanged();

        if (!mNetAndGpsUtil.isNetworkAvailable()) {
            Log.i(TAG, "getRtParam !isNetworkConnected(this)");
            rtInfo = "暂无网络";
            Toast.makeText(getApplicationContext(),"暂无网络，请尝试刷新",Toast.LENGTH_SHORT).show();
            Log.i(TAG, "等待发车  " + 403);
            mRtInfo.setText(Html.fromHtml(rtInfo));
            dismissLoading();
            return;
        }

        Log.i(TAG, "getRtParam");
        if (OfflineID > 0) {
            try {
                getRtInfo(OfflineID, Sequence);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                // Log.i(TAG, "getRtParam: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "未开通");
            rtInfo = "未开通";
            mRtInfo.setText(Html.fromHtml(rtInfo));
            dismissLoading();
        }
    }

    private void getSeverInfo() {
        mHasRtStationList.clear();
        mBuslineAdapter.notifyDataSetChanged();
        if (!mNetAndGpsUtil.isNetworkAvailable()) {
            rtInfo = "暂无网络";
            Toast.makeText(getApplicationContext(),"暂无网络，请尝试刷新",Toast.LENGTH_SHORT).show();
            Log.i(TAG, "等待发车  " + 431);
            mRtInfo.setText(Html.fromHtml(rtInfo));
            dismissLoading();
            return;
        }
        Log.i(TAG, "getSeverInfo");
        mVolleyNetwork.getAllBusesWithLineAndOneStation(LineID, StationID,
                new VolleyNetwork.requestListener() {

                    @Override
                    public void onSuccess(JSONObject data) {
                        // TODO Auto-generated method stub
                        try {
                            Log.i(TAG, "Data= " + data.toString());
                            JSONArray arr = data.getJSONArray("dt");
                            if (arr != null) {
                                Log.i(TAG, "ARR!=NULL： " + arr.toString());
                                int size = arr.length();
                                for (int i = 0; i < size; i++) {
                                    JSONObject json = arr.getJSONObject(i);
                                    int nextStationNum = json.getInt("Nsn");
                                    int nextStationDistance = json
                                            .getInt("Nsd");
                                    if (mHasRtStationList
                                            .containsKey(nextStationNum - 1)) {
                                        int amount = mHasRtStationList
                                                .get(nextStationNum - 1);
                                        mHasRtStationList.put(
                                                nextStationNum - 1, ++amount);
                                    } else {
                                        mHasRtStationList.put(
                                                nextStationNum - 1, 1);
                                    }
                                }
                            }
                            JSONObject latest = data.getJSONObject("lt");
                            if (latest != null) {
                                Log.i(TAG, "latest!=NULL");
                                Log.i(TAG,latest.toString());
                                double StationDistance = latest.getInt("Sd") / 1000.0;
                                int StationArrivingTime = latest.getInt("St");
                                int StationArrivingNum = latest.getInt("Nsn");
                                String distance = "<font color=\"red\">"
                                        + StationDistance + "</font>" + "公里";
                                String stationNum = "<font color=\"red\">"
                                        + (Sequence - StationArrivingNum + 1)
                                        + "</font>" + "站";
                                String stationName = "<font color=\"red\">"
                                        + StationName + "</font>";
                                if (StationArrivingTime <= 10) {

                                    rtInfo = "即将到站，请注意上车！";

                                } else {
                                    int tmp = StationArrivingTime / 60;
                                    String time = "";
                                    if (tmp <= 0)
                                        time = "<font color=\"red\">"
                                                + StationArrivingTime
                                                + "</font>" + "秒钟";
                                    else
                                        time = "<font color=\"red\">" + tmp
                                                + "</font>" + "分钟";
                                    rtInfo = "距" + stationName + "    "
                                            + stationNum + "    " + distance
                                            + "    " + time;
                                }
                            } else {
                                if (Sequence == 1)
                                    rtInfo = "起始站";
                                else
                                    rtInfo = "等待发车";
                                Log.i(TAG, "等待发车  " + 500);
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            if (Sequence == 1)
                                rtInfo = "起始站";
                            else
                                rtInfo = "等待发车";
                            Log.i(TAG, "等待发车  " + 506);
                        } finally {
                            mHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    // TODO Auto-generated method stub
                                    mBuslineAdapter
                                            .updateBusAmount(mHasRtStationList);
                                    mRtInfo.setText(Html.fromHtml(rtInfo));
                                    mBuslineAdapter.notifyDataSetChanged();
                                    dismissLoading();
                                }
                            });
                        }
                    }

                    @Override
                    public void onNotAccess() {
                        // TODO Auto-generated method stub
                        rtInfo = "未开通";
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                mRtInfo.setText(Html.fromHtml(rtInfo));
                                mBuslineAdapter.notifyDataSetChanged();
                                dismissLoading();
                            }
                        });
                        // getRtParam();
                    }

                    @Override
                    public void onFormatError() {
                        // TODO Auto-generated method stub
                        getRtParam();
                    }

                    @Override
                    public void onDataNA(String url) {
                        // TODO Auto-generated method stub
                        try {
                            getRtInfo(url, Sequence);
                        } catch (UnsupportedEncodingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNetError() {
                        // TODO Auto-generated method stub
                        getRtParam();
                    }
                });
    }

    private void getRtInfo(final int offlineID, final int sequence)
            throws JSONException, UnsupportedEncodingException {
        String Url = "http://bjgj.aibang.com:8899/bus.php?city="
                + URLEncoder.encode("北京", "utf-8") + "&id=" + offlineID
                + "&no=" + sequence + "&type=2&encrypt=1&versionid=2";
        // Log.i("Volley", "url " + Url);
        // 创建okHttpClient对象
        OkHttpClient mOkHttpClient = new OkHttpClient();
        // 创建一个Request
        final Request request = new Request.Builder()
                .url(Url).build();
        // new call
        Call call = mOkHttpClient.newCall(request);
        // 请求加入调度
        call.enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException arg1) {
                // TODO Auto-generated method stub
                // Log.i(TAG, "onFailure: " + arg0.body().toString());
                // Log.d("OKHTTP", "VolleyError: " + arg0.body().toString());
                if (sequence == 1)
                    rtInfo = "起始站";
                else
                    rtInfo = "等待发车";
                Log.i(TAG, "等待发车  " + 639);
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        mRtInfo.setText(Html.fromHtml(rtInfo));
                        dismissLoading();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response res)
                    throws IOException {
                // TODO Auto-generated method stub
                // Log.i(TAG, "onResponse: " + arg0.body().string());
                try {
                    String response = res.body().string();
                    // Log.i("OKHTTP", "response " + response);
                    JSONObject responseJson = XML.toJSONObject(response);
                    JSONObject rootJson = responseJson.getJSONObject("root");
                    int status = rootJson.getInt("status");
                    if (status != 200) {
                        // Log.i(TAG, child.getBuslineFullName()
                        // + " 暂无实时公交信息");
                        if (sequence == 1)
                            rtInfo = "起始站";
                        else
                            rtInfo = "等待发车";
                        Log.i(TAG, "等待发车  " + 585);
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                mRtInfo.setText(Html.fromHtml(rtInfo));
                                mBuslineAdapter.notifyDataSetChanged();
                                dismissLoading();
                            }
                        });
                        return;
                    }
                    JSONObject dataJson = rootJson.getJSONObject("data");
                    JSONArray busJsonArray = null;
                    if (dataJson.toString().indexOf("[") > 0) {
                        busJsonArray = (JSONArray) dataJson.get("bus");
                        busJsonArray = dataJson.getJSONArray("bus");
                    } else {
                        JSONObject busJsonObject = dataJson
                                .getJSONObject("bus");
                        busJsonArray = new JSONArray("["
                                + busJsonObject.toString() + "]");
                    }
                    dealRtInfo(busJsonArray, sequence);

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    // Log.i("OKHTTP", "JSONException: " + e.getMessage());

                    if (sequence == 1)
                        rtInfo = "起始站";
                    else
                        rtInfo = "等待发车";
                    Log.i(TAG, "等待发车  " + 617);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            mRtInfo.setText(Html.fromHtml(rtInfo));
                            dismissLoading();
                        }
                    });
                    e.printStackTrace();
                }

            }

        });
    }

    private void getRtInfo(String Url, final int sequence)
            throws JSONException, UnsupportedEncodingException {
        // 创建okHttpClient对象
        OkHttpClient mOkHttpClient = new OkHttpClient();
        // 创建一个Request
        final Request request = new Request.Builder()
                .url(Url).build();
        // new call
        Call call = mOkHttpClient.newCall(request);
        // 请求加入调度
        call.enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException arg1) {
                // TODO Auto-generated method stub
                // Log.i(TAG, "onFailure: " + arg0.body().toString());
                if (sequence == 1) {
                    rtInfo = "起始站";
                } else
                    rtInfo = "等待发车";
                Log.i(TAG, "等待发车  " + 745);
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        mRtInfo.setText(Html.fromHtml(rtInfo));
                        dismissLoading();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response res)
                    throws IOException {
                // TODO Auto-generated method stub
                // Log.i(TAG, "onResponse: " + arg0.body().string());
                try {
                    String response = res.body().string();
                    // Log.i("OKHTTP", "response " + response);
                    JSONObject responseJson = XML.toJSONObject(response);
                    JSONObject rootJson = responseJson.getJSONObject("root");
                    int status = rootJson.getInt("status");
                    if (status != 200) {
                        // Log.i(TAG, child.getBuslineFullName()
                        // + " 暂无实时公交信息");
                        if (sequence == 1) {
                            rtInfo = "起始站";
                        } else
                            rtInfo = "等待发车";
                        Log.i(TAG, "等待发车  " + 691);
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                mRtInfo.setText(Html.fromHtml(rtInfo));
                                mBuslineAdapter.notifyDataSetChanged();
                                dismissLoading();
                            }
                        });
                        return;
                    }
                    JSONObject dataJson = rootJson.getJSONObject("data");
                    JSONArray busJsonArray = null;
                    if (dataJson.toString().indexOf("[") > 0) {
                        busJsonArray = (JSONArray) dataJson.get("bus");
                        busJsonArray = dataJson.getJSONArray("bus");
                    } else {
                        JSONObject busJsonObject = dataJson
                                .getJSONObject("bus");
                        busJsonArray = new JSONArray("["
                                + busJsonObject.toString() + "]");
                    }
                    dealRtInfo(busJsonArray, sequence);

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    if (sequence == 1) {
                        rtInfo = "起始站";
                    } else
                        rtInfo = "等待发车";
                    Log.i(TAG, "等待发车  " + 723);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            mRtInfo.setText(Html.fromHtml(rtInfo));
                            dismissLoading();
                        }
                    });
                    e.printStackTrace();
                }
            }
        });
    }

    private void dealRtInfo(JSONArray json, int sequence) {
        try {
            try {
                rtInfo = "等待发车";
                Log.i(TAG, "等待发车  " + 781);
                int count = json.length();
                Log.i(TAG, "busJsonArray_count: " + count);
                JSONObject uploadJson = new JSONObject();
                JSONArray uploadData = new JSONArray();
                uploadJson.put("c", "beijing");
                uploadJson.put("dt", uploadData);
                int max = 0;
                Map<String, Object> map = new HashMap<String, Object>();
                for (int j = 0; j < count; j++) {
                    JSONObject busJson = (JSONObject) json.get(j);
                    JSONObject uplodaItem = new JSONObject();
                    MyCipher mCiper = new MyCipher("aibang"
                            + busJson.getString("gt"));

                    String nextStationName = mCiper.decrypt(busJson
                            .getString("ns"));// nextStationName
                    int nextStationNum = Integer.parseInt(mCiper
                            .decrypt(busJson.getString("nsn")));// nextStationNum
                    int id = busJson.getInt("id");
                    String nextStationDistance = busJson.getString("nsd");// nextStationDistance
                    String nextStationTime = busJson.getString("nst");// nextStationTime

                    String stationDistance = mCiper.decrypt(busJson
                            .getString("sd"));// stationDistance
                    String stationArrivingTime = mCiper.decrypt(busJson
                            .getString("st"));
                    String st_c = null;
                    if (isNumeric(stationArrivingTime))
                        // st_c = TimeStampToDate(Long.parseLong(st),
                        // "HH:mm");// station_arriving_time
                        st_c = String.valueOf(TimeStampToDelTime(Long
                                .parseLong(stationArrivingTime)));// station_arriving_time
                    else
                        st_c = "-1";
                    String x = mCiper.decrypt(busJson.getString("x"));
                    String y = mCiper.decrypt(busJson.getString("y"));
                    Log.i(TAG,
                            "next_station_name: " + nextStationName + "\n"
                                    + "next_station_num: " + nextStationNum
                                    + "\n" + "next_station_distance: "
                                    + nextStationDistance + "\n"
                                    + "next_station_arriving_time: "
                                    + nextStationTime + "\n"
                                    + "station_distance: " + stationDistance
                                    + "\n" + "station_arriving_time: "
                                    + stationArrivingTime + "   " + st_c + "\n"
                                    + " currentTime "
                                    + System.currentTimeMillis());
                    if (nextStationNum <= sequence && nextStationNum > max) {
                        map.clear();
                        map.put("nextStationNum", nextStationNum);
                        map.put("stationArrivingTime", stationArrivingTime);
                        map.put("stationDistance", stationDistance);
                        max = nextStationNum;
                    }
                    uplodaItem.put("LID", LineID);
                    uplodaItem
                            .put("BID", LineID + String.format("%02d", j + 1));
                    uplodaItem.put("Nsn", nextStationNum);
                    uplodaItem.put("Nsd", nextStationDistance);
                    LatLng latLngBaidu = mCoordConventer
                            .from(CoordinateConverter.CoordType.COMMON)
                            .coord(new LatLng(Double.parseDouble(y), Double
                                    .parseDouble(x))).convert();
                    uplodaItem.put("Lat", latLngBaidu.latitude);
                    uplodaItem.put("Lon", latLngBaidu.longitude);
                    uplodaItem.put("T", System.currentTimeMillis() / 1000);
                    uploadData.put(uplodaItem);

                    if (mHasRtStationList.containsKey(nextStationNum - 1)) {
                        int amount = mHasRtStationList.get(nextStationNum - 1);
                        mHasRtStationList.put(nextStationNum - 1, ++amount);
                    } else {
                        mHasRtStationList.put(nextStationNum - 1, 1);
                    }
                }
                mVolleyNetwork.upLoadRtInfo(uploadJson, new VolleyNetwork.upLoadListener() {

                    @Override
                    public void onSuccess() {
                        // TODO Auto-generated method stub
                        Log.i(TAG, " 上传成功");
                    }

                    @Override
                    public void onFail() {
                        // TODO Auto-generated method stub
                        Log.i(TAG, " 上传失败");
                    }
                });
                if (map.size() > 0) {
                    int nextStationNum = (Integer) map.get("nextStationNum");
                    String stationArrivingTime = map.get("stationArrivingTime")
                            .toString();
                    String stationDistance = map.get("stationDistance")
                            .toString();
                    Log.e(TAG, "nextStationNum: " + nextStationNum
                            + " stationArrivingTime: " + stationArrivingTime
                            + " stationDistance： " + stationDistance);
                    // 未开通
                    if (stationArrivingTime == null || nextStationNum == 0) {
                        rtInfo = "未发车";
                    }
                    // 起点站
                    else if (sequence == 1) {
                        rtInfo = "起点站";
                    }
                    // 到站
                    else if (nextStationNum <= sequence) {
                        if (isNumeric(stationArrivingTime)) {
                            if (nextStationNum == sequence) {
                                // 已到站
                                if (Integer.parseInt(stationArrivingTime) < 10) {
                                    rtInfo = "已到站，请抓紧上车！";
                                }
                                // 即将到站
                                else if (Integer.parseInt(stationDistance) < 10) {
                                    rtInfo = "即将到站，请注意上车！";
                                } else {
                                    int nstime = TimeStampToDelTime(Long
                                            .parseLong(stationArrivingTime));// 计算还有几分钟
                                    String distance = "<font color=\"red\">"
                                            + Integer.parseInt(stationDistance)
                                            / 1000.0 + "</font>" + "公里";
                                    String time = "<font color=\"red\">"
                                            + nstime + "</font>" + "分钟";
                                    String stationNum = "<font color=\"red\">"
                                            + (sequence - nextStationNum + 1)
                                            + "</font>" + "站";
                                    String stationName = "<font color=\"red\">"
                                            + StationName + "</font>";
                                    if (nstime <= 0) {
                                        rtInfo = "即将到站，请注意上车！";

                                    } else {
                                        rtInfo = "距" + stationName + "    "
                                                + stationNum + "    "
                                                + distance + "    " + time;
                                    }
                                }
                            } else {
                                int nstime = TimeStampToDelTime(Long
                                        .parseLong(stationArrivingTime));// 计算还有几分钟
                                String distance = "<font color=\"red\">"
                                        + Integer.parseInt(stationDistance)
                                        / 1000.0 + "</font>" + "公里";
                                String time = "<font color=\"red\">" + nstime
                                        + "</font>" + "分钟";
                                String stationNum = "<font color=\"red\">"
                                        + (sequence - nextStationNum + 1)
                                        + "</font>" + "站";
                                String stationName = "<font color=\"red\">"
                                        + StationName + "</font>";
                                if (nstime <= 0) {
                                    rtInfo = "距" + stationName + "    "
                                            + stationNum + "    " + distance;
                                } else {
                                    rtInfo = "距" + stationName + "    "
                                            + stationNum + "    " + distance
                                            + "    " + time;
                                }
                            }
                        }
                    } else {
                        rtInfo = "未发车";
                    }
                }
            } catch (SQLException sqle) {
                throw sqle;
            }

        } catch (JSONException e) {
            // Log.e("JSON exception", e.getMessage());
            e.printStackTrace();
        } finally {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    mBuslineAdapter.updateBusAmount(mHasRtStationList);
                    mRtInfo.setText(Html.fromHtml(rtInfo));
                    mBuslineAdapter.notifyDataSetChanged();
                    dismissLoading();
                }
            });
        }
    }

    public String TimeStampToDate(Long timestampString, String formats) {
        if (timestampString < 0)
            return "0";
        String date = new java.text.SimpleDateFormat(formats)
                .format(new java.util.Date(timestampString * 1000));
        return date;
    }

    public boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("-?[0-9]+.*[0-9]*");

        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    public int TimeStampToDelTime(Long timestampString) {
        if (timestampString < 0)
            return (int) 0;
        double delTime = (timestampString * 1000 - System.currentTimeMillis()) / 1000 / 60.0;
        return (int) Math.ceil(delTime);
    }

    public void onResume() {
        super.onResume();
//		MobclickAgent.onPageStart("SplashScreen"); // ͳ��ҳ��
//		MobclickAgent.onResume(this); // ͳ��ʱ��
        exeuteTask();

    }

    @Override
    public void onPause() {
        super.onPause();
//		MobclickAgent.onPageEnd("SplashScreen");
//		MobclickAgent.onPause(this);
        // task.cancel();
        // timer.cancel();
        if (mTask != null)
            mTask.cancel(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onRestart() {
        super.onRestart();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    private List<Child> loadBuslineData(int LineID) {

        if (mStations == null)
            mStations = new ArrayList<Child>();
        List<Child> tmp = mDataBaseManager.acquireStationsWithBuslineID(LineID);
        if (tmp.size() > 0) {
            mStations = tmp;
            if (Sequence > mStations.size())
                Sequence = 1;
            StationID = mStations.get(Sequence - 1).getStationID();
            LineName = mStations.get(Sequence - 1).getLineName();
            StationName = mStations.get(Sequence - 1).getStationName();
            Map<String, Object> LineInfo = mDataBaseManager
                    .acquireLineInfoWithLineID(LineID);
            if (LineInfo != null && LineInfo.size() > 0) {
                startTime = LineInfo.get("StartTime").toString();
                endTime = LineInfo.get("EndTime").toString();
                StartStation = LineInfo.get("StartStation").toString();
                EndStation = LineInfo.get("EndStation").toString();
                FullName = LineInfo.get("LineName").toString() + " ("
                        + LineInfo.get("StartStation").toString() + " - "
                        + LineInfo.get("EndStation").toString() + ")";
                OfflineID = Integer.parseInt(LineInfo.get("OfflineID")
                        .toString());
            }
            return mStations;
        } else
            return null;
    }

    private void changeDirection() {
        if (LineID % 10 == 0)
            LineID += 2;
        else
            LineID -= 2;
        OfflineID = 0;
        exeuteTask();
    }

    private loadDataBaseTask mTask = null;

    private void exeuteTask() {
        if (mTask != null)
            mTask.cancel(true);
        mTask = new loadDataBaseTask(this);
        mTask.execute();

    }

    class loadDataBaseTask extends AsyncTask<Integer, Integer, List<Child>> {
        // 后面尖括号内分别是参数（线程休息时间），进度(publishProgress用到)，返回值 类型

        private Context mContext = null;

        public loadDataBaseTask(Context context) {
            this.mContext = context;
        }

        /*
         * 第一个执行的方法 执行时机：在执行实际的后台操作前，被UI 线程调用
         * 作用：可以在该方法中做一些准备工作，如在界面上显示一个进度条，或者一些控件的实例化，这个方法可以不用实现。
         *
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            Log.d(TAG, "onPreExecute");
            super.onPreExecute();
        }

        /*
         * 执行时机：在onPreExecute 方法执行后马上执行，该方法运行在后台线程中 作用：主要负责执行那些很耗时的后台处理工作。可以调用
         * publishProgress方法来更新实时的任务进度。该方法是抽象方法，子类必须实现。
         *
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override
        protected List<Child> doInBackground(Integer... params) {
            // TODO Auto-generated method stub
            Log.d(TAG, "doInBackground");
            // for (int i = 0; i <= 100; i++) {
            // mProgressBar.setProgress(i);
            publishProgress();

            // try {
            // Thread.sleep(params[0]);
            // } catch (InterruptedException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
            // }
            return loadBuslineData(LineID);
        }

        /*
         * 执行时机：这个函数在doInBackground调用publishProgress时被调用后，UI
         * 线程将调用这个方法.虽然此方法只有一个参数,但此参数是一个数组，可以用values[i]来调用
         * 作用：在界面上展示任务的进展情况，例如通过一个进度条进行展示。此实例中，该方法会被执行100次
         *
         * @see android.os.AsyncTask#onProgressUpdate(Progress[])
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onProgressUpdate");
            // mTextView.setText(values[0] + "%");
            showLoading();
            super.onProgressUpdate(values);
        }

        /*
         * 执行时机：在doInBackground 执行完成后，将被UI 线程调用 作用：后台的计算结果将通过该方法传递到UI
         * 线程，并且在界面上展示给用户 result:上面doInBackground执行后的返回值，所以这里是"执行完毕"
         *
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(List<Child> result) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onPostExecute");
            if (result == null) {
                Toast.makeText(getApplicationContext(), "对不起，没有反方向线路!",
                        Toast.LENGTH_SHORT).show();
                dismissLoading();
                return;
            }
            updateLineInfo();
            rtInfo = "???";
            mBuslineListView.setVisibility(View.VISIBLE);
            mRtInfo.setText(Html.fromHtml(loadingInfo));
            mBuslineAdapter.updateData(mStations);
            mBuslineAdapter.setSelectIndex(Sequence - 1);
            mBuslineAdapter.notifyDataSetChanged();
            getSeverInfo();
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    if ((Sequence - 1) < 1)
                        mBuslineListView.setSelection(0);
                    else
                        mBuslineListView.setSelection(Sequence - 3);
                }
            });
            if (mUserDataBaseHelper.IsFavStation(LineID, StationID))
                mConcernBtn.setImageResource(R.drawable.icon_horizon_like);
            else
                mConcernBtn.setImageResource(R.drawable.icon_horizon_dislike);
            if (mUserDataBaseHelper.IsAlertOpenBusline(LineID, StationID))
                mAlertBtn.setImageResource(R.drawable.icon_horizon_alarm);
            else
                mAlertBtn.setImageResource(R.drawable.icon_horizon_disalarm);
        }

    }

}
