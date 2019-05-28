package com.bnrc.busapp.view.activity;

import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapLoadedCallback;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.model.LatLng;

import com.baidu.mapapi.utils.CoordinateConverter;
import com.bnrc.busapp.R;

import com.bnrc.busapp.adapter.NearAdapter;
import com.bnrc.busapp.adapter.StationsAdapter;
import com.bnrc.busapp.constant.Constants;

import com.bnrc.busapp.database.PCDataBaseHelper;
import com.bnrc.busapp.database.PCUserDataDBHelper;
import com.bnrc.busapp.listener.IPopWindowListener;
import com.bnrc.busapp.model.Child;
import com.bnrc.busapp.model.Group;
import com.bnrc.busapp.network.MyVolley;
import com.bnrc.busapp.network.VolleyNetwork;
import com.bnrc.busapp.ui.SelectPicPopupWindow;
import com.bnrc.busapp.ui.expandablelistview.SwipeMenu;
import com.bnrc.busapp.ui.expandablelistview.SwipeMenuCreator;
import com.bnrc.busapp.ui.expandablelistview.SwipeMenuExpandableListView;
import com.bnrc.busapp.ui.expandablelistview.SwipeMenuItem;
import com.bnrc.busapp.ui.mapoverlay.OverlayManager;
import com.bnrc.busapp.ui.pullloadmenulistview.IPullRefresh;
import com.bnrc.busapp.ui.pullloadmenulistview.PullLoadMenuListView;
import com.bnrc.busapp.util.AnimationUtil;
import com.bnrc.busapp.util.LocationUtil;
import com.bnrc.busapp.util.MyCipher;
import com.bnrc.busapp.util.NetAndGpsUtil;
import com.bnrc.busapp.util.SharedPreferenceUtil;
import com.bnrc.busapp.view.activity.base.BaseActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

public class StationListActivity extends BaseActivity implements IPopWindowListener {
    private static final String TAG = StationListActivity.class.getSimpleName();
    public PCDataBaseHelper mDataManager = null;
    private NetAndGpsUtil mNetAndGpsUtil;
    private String StationName;
    private List<Group> mGroups = null;
    public LocationUtil mLocationUtil = null;
    private PCUserDataDBHelper mUserDB = null;
    private PullLoadMenuListView mStationListView;
    private Button appBtn = null;
    private StationsAdapter mStationAdapter;
    private loadDataBaseTask mTask;
    private Child mChild;
    public MapView mMapView;
    public BaiduMap mBaiduMap = null;
    List<Overlay> overList = new ArrayList<Overlay>();
    private RelativeLayout mCanversLayout;// 阴影遮挡图层
    private PopupWindow mPopupWindow;// 弹窗对象
    private SelectPicPopupWindow menuWindow;
    // 定义Handler对象
    private Handler mHandler = new Handler();
    private VolleyNetwork mVolleyNetwork;
    private Timer mRefreshTimer;
    private TimerTask mRefreshTask;
    private SharedPreferenceUtil mSharePrefrenceUtil;
    private CoordinateConverter mCoordConventer;

    private TextView station_menu_view,tv_station_title;
    private ImageView image_alert;

    private SwipeMenuExpandableListView.OnGroupExpandListener mOnGroupExpandListener = new SwipeMenuExpandableListView.OnGroupExpandListener() {

        int lastGroupPos = 0;

        @Override
        public void onGroupExpand(int pos) {
            if (mStationAdapter.getChildrenCount(pos) > 0) {
                if (lastGroupPos != pos) {
                    mStationListView.collapseGroup(lastGroupPos);
                    lastGroupPos = pos;
                }
                mStationListView.setSelectedGroup(pos);
            }
        }
    };

    private SwipeMenuCreator mMenuCreator = new SwipeMenuCreator() {
        @Override
        public void create(SwipeMenu menu) {
            switch (menu.getViewType()) {
                case NearAdapter.FAV:
                    SwipeMenuItem item1 = new SwipeMenuItem(StationListActivity.this);
                    // item1.setBackground(getResources().getColor(R.color.blue));
                    item1.setBackground(R.drawable.bg_circle_drawable_notstar);
                    // item1.setIcon(R.drawable.select_star);
                    item1.setWidth(220);
                    item1.setTitleColor(getResources().getColor(R.color.white));
                    item1.setTitleSize(50);
                    item1.setTitle("修改");
                    menu.addMenuItem(item1);
                    break;
                case NearAdapter.NORMAL:
                    SwipeMenuItem item2 = new SwipeMenuItem(StationListActivity.this);
                    // item1.setBackground(getResources().getColor(R.color.colorPrimaryDark));
                    item2.setBackground(R.drawable.bg_circle_drawable);
                    // item2.setIcon(R.drawable.not_select_star);
                    item2.setWidth(220);
                    item2.setTitleColor(getResources().getColor(R.color.white));
                    item2.setTitleSize(50);
                    item2.setTitle("收藏");
                    menu.addMenuItem(item2);
                    break;
            }
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_station_list);

        mLocationUtil = LocationUtil.getInstance(getApplicationContext());
        mLocationUtil.startLocation(null);
        mDataManager = PCDataBaseHelper.getInstance(StationListActivity.this);
        mNetAndGpsUtil = NetAndGpsUtil
                .getInstance(this.getApplicationContext());
        mSharePrefrenceUtil = SharedPreferenceUtil
                .getInstance(getApplicationContext());
        Intent intent = getIntent();
        StationName = intent.getStringExtra("StationName");

        station_menu_view = findViewById(R.id.station_menu_view);
        tv_station_title = findViewById(R.id.tv_station_title);

        station_menu_view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.station_menu_view:
                        finish();
                        break;
                }
            }
        });


        tv_station_title.setText(StationName);

        mUserDB = PCUserDataDBHelper.getInstance(StationListActivity.this);
        mVolleyNetwork = VolleyNetwork.getInstance(this);
        mStationListView = (PullLoadMenuListView) findViewById(R.id.explistview_station);
        mGroups = new ArrayList<Group>();
        mStationAdapter = new StationsAdapter(mGroups, this,
                mStationListView.listView,this);
        mStationListView.setAdapter(mStationAdapter);
        mStationListView.setMenuCreator(mMenuCreator);
        // mStationListView.setOnMenuItemClickListener(mMenuItemClickListener);
        mStationListView.setOnGroupExpandListener(mOnGroupExpandListener);
        // mStationListView.setOnChildClickListener(mOnChildExpandListener);
        mStationListView.setPullToRefreshEnable(true);
        mStationListView
                .setPullRefreshListener(new IPullRefresh.PullRefreshListener() {

                    @Override
                    public void onRefresh() {
                        // TODO Auto-generated method stub
                        MyVolley.sharedVolley(getApplicationContext())
                                .reStart();
                        pullToRefresh();
                    }
                });
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        // 开启交通图
        mBaiduMap.setTrafficEnabled(false);
        mBaiduMap.setOnMapLoadedCallback(new OnMapLoadedCallback() {

            @Override
            public void onMapLoaded() {
                // TODO Auto-generated method stub
                searchStations();
            }
        });
        mMapView.removeViewAt(2);

        image_alert = findViewById(R.id.station_concern_view);
        if (mUserDB.IsAlertStation(StationName)) {
            image_alert.setBackgroundResource(R.drawable.icon_isalert);
        } else {
            image_alert.setBackgroundResource(R.drawable.icon_notalert);
        }
        image_alert.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mGroups == null || mGroups.size() <= 0)
                    return;
                Intent intent = new Intent(StationListActivity.this,
                        AlertSelectListActivity.class);
                // Bundle bundle = new Bundle();
                // bundle.putSerializable("DATA", (Serializable) mGroups);
                // intent.putExtras(bundle);
                intent.putExtra("StationName", StationName);
                startActivity(intent);
                AnimationUtil.activityZoomAnimation(StationListActivity.this);
            }
        });
        mCoordConventer = new CoordinateConverter();
    }

    private void loadDataBase() {
        if (mTask != null)
            mTask.cancel(true);
        mTask = new loadDataBaseTask(this);
        mTask.execute();

    }

    private void pullToRefresh() {
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                mStationListView.stopRefresh();
            }
        }, 3000);
        // getRtParam(mNearGroups);
        getServerInfo(mGroups);

    }

    private List<Group> loadBuslineData() {
        mGroups = mDataManager.acquireAllBranchBusLinesWithStation(StationName);
        return mGroups;
    }

    private MyOverlayManager mOverlayManager;

    private void searchStations() {
        Log.i(TAG, "searchStations");
        mBaiduMap.clear();
        overList.clear();
        mOverlayManager = new MyOverlayManager(mBaiduMap);
        mOverlayManager.clearData();
        com.baidu.mapapi.model.LatLngBounds.Builder localBuilder = new com.baidu.mapapi.model.LatLngBounds.Builder();
        int[] iconArr = { R.drawable.icon_marka, R.drawable.icon_markb,
                R.drawable.icon_markc, R.drawable.icon_markd,
                R.drawable.icon_marke, R.drawable.icon_markf,
                R.drawable.icon_markg, R.drawable.icon_markh,
                R.drawable.icon_marki, R.drawable.icon_markj };
        int groupSize = mGroups.size();
        Log.i(TAG, "groupSize: "+groupSize);
        for (int i = 0; i < groupSize; i++) {
            // 定义Maker坐标点
            Group group = mGroups.get(i);
            Log.i(TAG, mGroups.get(i).getStationName());
            LatLng stationPoint = new LatLng(group.getLatitide(),
                    group.getLongitude());

            // 构建Marker图标
            BitmapDescriptor bitmap = BitmapDescriptorFactory
                    .fromResource(iconArr[i]);
            // 构建MarkerOption，用于在地图上添加Marker
            MarkerOptions option2 = new MarkerOptions().position(stationPoint)
                    .icon(bitmap).perspective(true).zIndex(16)// 设置marker所在层级
                    .draggable(true).title(group.getStationName()); // 设置手势拖拽;
            // overList.add(mBaiduMap.addOverlay(option2));
            // option2.animateType(MarkerAnimateType.grow);
            mOverlayManager.setData(option2);
            localBuilder.include(stationPoint);
        }

        mOverlayManager.addToMap();
        mOverlayManager.zoomToSpan();
        if (groupSize == 1) {
            MapStatusUpdate u = MapStatusUpdateFactory.zoomTo(19.0f);
            mBaiduMap.animateMapStatus(u);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus)
            searchStations();
    }

    private void openRefreshTimertask() {
        mRefreshTask = new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                getServerInfo(mGroups);
            }
        };
        mRefreshTimer = new Timer(true);
        String value = mSharePrefrenceUtil.getValue("refreshFrequency", "30秒");
        int period = Integer.parseInt(value.substring(0, value.indexOf("秒"))
                .toString());
        mRefreshTimer.schedule(mRefreshTask, period * 1000, period * 1000);
    }

    private void cancelRefreshTimertask() {
        mRefreshTask.cancel();
        mRefreshTimer.cancel();
    }

    private void getRtParam(List<Group> groups) {
        Log.i(TAG, "getRtInfo");
        if (!mNetAndGpsUtil.isNetworkAvailable() || groups == null)
            return;
        for (Group group : groups) {
            if (group.getChildrenCount() <= 0)
                continue;
            List<Child> children = group.getChildren();
            for (Child child : children) {
                if (child.getOfflineID() <= 0) {
                    Map<String, String> showText = new HashMap<String, String>();
                    showText.put("itemsText", "<font color=\"grey\">" + "未开通"
                            + "</font>");
                    if (child != null) {
                        child.setRtInfo(showText);
                        child.setRtRank(Child.NOTEXIST);
                        child.setDataChanged(true);
                    }
                    mStationAdapter.notifyDataSetChanged();
                } else {
                    try {
                        getRtInfo(child);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void getServerInfo(List<Group> groups) {
        if (groups == null)
            return;
        for (Group group : groups) {
            if (group.getChildrenCount() <= 0)
                continue;
            List<Child> children = group.getChildren();
            for (final Child child : children) {
                final int LineID = child.getLineID();
                int StationID = child.getStationID();
                Log.i(TAG, "LineID: " + LineID + " ; " + "StationID: "
                        + StationID);
                final int sequence = child.getSequence();
                if (sequence == 1) {
                    Map<String, String> showText = new HashMap<String, String>();
                    showText.put("itemsText", "起点站");
                    child.setRtInfo(showText);
                    child.setRtRank(Child.FIRSTSTATION);
                    child.setDataChanged(true);
                } else
                    mVolleyNetwork.getNearestBusWithLineAndOneStation(LineID,
                            StationID, new VolleyNetwork.requestListener() {

                                @Override
                                public void onSuccess(JSONObject data) {
                                    // TODO Auto-generated method stub
                                    try {
                                        JSONArray arr = null;
                                        if (data.toString().indexOf("[") > 0) {
                                            arr = data.getJSONArray("dt");
                                        } else {
                                            JSONObject busJsonObject = data
                                                    .getJSONObject("dt");
                                            arr = new JSONArray("["
                                                    + busJsonObject.toString()
                                                    + "]");
                                        }
                                        if (arr != null && arr.length() > 0) {
                                            Log.i(TAG, "ARR!=NULL");
                                            int size = arr.length();
                                            List<Map<String, ?>> list = child
                                                    .getRtInfoList();
                                            list.clear();
                                            for (int i = 0; i < size; i++) {
                                                Map<String, String> map = new HashMap<String, String>();
                                                JSONObject json = arr
                                                        .getJSONObject(i);
                                                int distance = json
                                                        .getInt("Sd");
                                                int time = json.getInt("St");
                                                int station = json.getInt("bn");
                                                if (time <= 10) {
                                                    map.put("station", "已经");
                                                    map.put("time", "到站");
                                                } else {
                                                    int tmp = time / 60;
                                                    if (tmp <= 0)
                                                        map.put("station", time
                                                                + " 秒");
                                                    else
                                                        map.put("station", tmp
                                                                + " 分");
                                                    map.put("time", station
                                                            + " 站");
                                                }
                                                list.add(map);
                                                Log.i(TAG, child.getLineName()
                                                        + " distance: "
                                                        + distance + " ; "
                                                        + "time: " + time);
                                            }
                                            if (child != null) {
                                                // child.setRtInfo(showText);
                                                child.setRtRank(Child.ARRIVING);
                                                child.setDataChanged(true);
                                            }
                                        } else {
                                            Map<String, String> showText = new HashMap<String, String>();
                                            if (sequence == 1) {
                                                showText.put("itemsText", "起点站");
                                                child.setRtInfo(showText);
                                                child.setRtRank(Child.FIRSTSTATION);
                                                child.setDataChanged(true);
                                            } else {
                                                showText.put("itemsText",
                                                        "<font color=\"black\">"
                                                                + "等待发车"
                                                                + "</font>");
                                                if (child != null) {
                                                    child.setRtInfo(showText);
                                                    child.setRtRank(Child.NOTYET);
                                                    child.setDataChanged(true);
                                                }
                                            }
                                        }
                                        // JSONObject json = data
                                        // .getJSONObject("data");
                                        // int distance = json
                                        // .getInt("StationDistance");
                                        // int time = json
                                        // .getInt("StationArrivingTime");
                                        // Map<String, String> showText = new
                                        // HashMap<String, String>();
                                        // if (time <= 10)
                                        // showText.put("itemsText",
                                        // "<font color=\"red\">" + "即将到站"
                                        // + "</font>");
                                        // else {
                                        // int tmp = time / 60;
                                        // if (tmp <= 0)
                                        // showText.put("itemsText",
                                        // "<font color=\"red\">"
                                        // + time + " 秒钟"
                                        // + "</font>");
                                        // else
                                        // showText.put("itemsText",
                                        // "<font color=\"red\">"
                                        // + tmp + " 分钟"
                                        // + "</font>");
                                        // }
                                        // if (child != null) {
                                        // child.setRtInfo(showText);
                                        // child.setRtRank(Child.ARRIVING);
                                        // child.setDataChanged(true);
                                        //
                                        // }
                                        sortGroup();
                                        mStationAdapter.notifyDataSetChanged();

                                    } catch (JSONException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                        Log.i(TAG,
                                                "getServerInfo JSONException");
                                    }
                                }

                                @Override
                                public void onNotAccess() {
                                    // TODO Auto-generated method stub
                                    Map<String, String> showText = new HashMap<String, String>();
                                    showText.put("itemsText",
                                            "<font color=\"grey\">" + "未开通"
                                                    + "</font>");
                                    if (child != null) {
                                        child.setRtInfo(showText);
                                        child.setRtRank(Child.NOTEXIST);
                                    }
                                    sortGroup();
                                    mStationAdapter.notifyDataSetChanged();
                                    Log.i(TAG, "未开通");
                                }

                                @Override
                                public void onFormatError() {
                                    // TODO Auto-generated method stub
                                    Log.i(TAG, "数据格式不对");
                                    if (child.getOfflineID() <= 0) {
                                        Map<String, String> showText = new HashMap<String, String>();
                                        showText.put("itemsText",
                                                "<font color=\"grey\">" + "未开通"
                                                        + "</font>");
                                        if (child != null) {
                                            child.setRtInfo(showText);
                                            child.setRtRank(Child.NOTEXIST);
                                            child.setDataChanged(true);

                                        }
                                        sortGroup();
                                        mStationAdapter.notifyDataSetChanged();
                                    } else {
                                        try {
                                            getRtInfo(child);
                                        } catch (Exception e) {
                                            // TODO Auto-generated catch block
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                @Override
                                public void onDataNA(String url) {
                                    // TODO Auto-generated method stub
                                    getRtInfo(child, url);
                                    Log.i(TAG, "数据过旧");
                                }

                                @Override
                                public void onNetError() {
                                    // TODO Auto-generated method stub
                                    // Map<String, String> showText = new
                                    // HashMap<String, String>();
                                    // showText.put("itemsText",
                                    // "<font color=\"grey\">" + "网络不佳"
                                    // + "</font>");
                                    // if (child != null) {
                                    // child.setRtInfo(showText);
                                    // child.setRtRank(Child.NOTYET);
                                    // child.setDataChanged(true);
                                    //
                                    // }
                                    // sortGroup();
                                    // mStationAdapter.notifyDataSetChanged();
                                    if (child.getOfflineID() <= 0) {
                                        Map<String, String> showText = new HashMap<String, String>();
                                        showText.put("itemsText",
                                                "<font color=\"grey\">" + "未开通"
                                                        + "</font>");
                                        if (child != null) {
                                            child.setRtInfo(showText);
                                            child.setRtRank(Child.NOTEXIST);
                                            child.setDataChanged(true);

                                        }
                                        sortGroup();
                                        mStationAdapter.notifyDataSetChanged();
                                    } else {
                                        try {
                                            getRtInfo(child);
                                        } catch (Exception e) {
                                            // TODO Auto-generated catch block
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
            }
        }

    }

    private void sortGroup() {
        for (Group group : mGroups)
            Collections.sort(group.getChildren(), comparator);
    }

    private void getRtInfo(final Child child) throws JSONException,
            UnsupportedEncodingException {
        final int sequence = child.getSequence();
        int offlineID = child.getOfflineID();
        String Url = "http://bjgj.aibang.com:8899/bus.php?city="
                + URLEncoder.encode("北京", "utf-8") + "&id=" + offlineID
                + "&no=" + sequence + "&type=2&encrypt=1&versionid=2";
        Log.i("OKHTTP", "url " + Url);// 创建okHttpClient对象
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
                if (child != null) {
                    Map<String, String> showText = new HashMap<String, String>();
                    if (sequence == 1) {
                        showText.put("itemsText", "起点站");
                        child.setRtInfo(showText);
                        child.setRtRank(Child.FIRSTSTATION);
                        child.setDataChanged(true);
                    } else {
                        showText.put("itemsText", "等待发车");
                        // 到站
                        child.setRtInfo(showText);
                        child.setDataChanged(true);
                        child.setRtRank(Child.NOTYET);
                    }
                }
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        mStationAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response res)
                    throws IOException {
                // TODO Auto-generated method stub
                try {
                    String response = res.body().string();
//					Log.i("OKHTTP", "response " + response);
                    JSONObject responseJson = XML.toJSONObject(response);
                    JSONObject rootJson = responseJson.getJSONObject("root");
                    int status = rootJson.getInt("status");
                    if (status != 200) {
                        if (child != null) {
                            Map<String, String> showText = new HashMap<String, String>();
                            if (sequence == 1) {
                                showText.put("itemsText", "起点站");
                                child.setRtInfo(showText);
                                child.setRtRank(Child.FIRSTSTATION);
                                child.setDataChanged(true);
                            } else {
                                showText.put("itemsText", "等待发车");
                                // 到站
                                child.setRtInfo(showText);
                                child.setDataChanged(true);
                                child.setRtRank(Child.NOTYET);
                            }
                        }
                        sortGroup();
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                mStationAdapter.notifyDataSetChanged();
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
                    dealRtInfo(busJsonArray, child);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    if (child != null) {
                        Map<String, String> showText = new HashMap<String, String>();
                        if (sequence == 1) {
                            showText.put("itemsText", "起点站");
                            child.setRtInfo(showText);
                            child.setRtRank(Child.FIRSTSTATION);
                            child.setDataChanged(true);
                        } else {
                            showText.put("itemsText", "等待发车");
                            // 到站
                            child.setRtInfo(showText);
                            child.setDataChanged(true);
                            child.setRtRank(Child.NOTYET);
                        }
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            mStationAdapter.notifyDataSetChanged();
                        }
                    });
                    e.printStackTrace();
                }
            }
        });
    }

    private void getRtInfo(final Child child, String url) {
        final int sequence = child.getSequence();
//		Log.i("Volley", "url:" + url);
        // 创建okHttpClient对象
        OkHttpClient mOkHttpClient = new OkHttpClient();
        // 创建一个Request
        final Request request = new Request.Builder()
                .url(url).build();
        // new call
        Call call = mOkHttpClient.newCall(request);
        // 请求加入调度
        call.enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException arg1) {
                // TODO Auto-generated method stub
                // Log.i(TAG, "onFailure: " + arg0.body().toString());
                if (child != null) {
                    Map<String, String> showText = new HashMap<String, String>();
                    if (sequence == 1) {
                        showText.put("itemsText", "起点站");
                        child.setRtInfo(showText);
                        child.setRtRank(Child.FIRSTSTATION);
                        child.setDataChanged(true);
                    } // 未开通
                    else {
                        showText.put("itemsText", "等待发车");
                        // 到站
                        child.setRtInfo(showText);
                        child.setRtRank(Child.NOTYET);
                        child.setDataChanged(true);
                    }
                }
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        mStationAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response res)
                    throws IOException {
                // TODO Auto-generated method stub
                try {
                    String response = res.body().string();
//					Log.i(TAG, "onResponse: " + response);
//					Log.i("OKHTTP", "response " + response);
                    JSONObject responseJson = XML.toJSONObject(response);
                    JSONObject rootJson = responseJson.getJSONObject("root");
                    int status = rootJson.getInt("status");
                    if (status != 200) {
                        // Log.i(TAG, child.getBuslineFullName()
                        // + " 暂无实时公交信息");
                        if (child != null) {
                            Map<String, String> showText = new HashMap<String, String>();
                            if (sequence == 1) {
                                showText.put("itemsText", "起点站");
                                child.setRtInfo(showText);
                                child.setRtRank(Child.FIRSTSTATION);
                                child.setDataChanged(true);
                            } else {
                                showText.put("itemsText", "等待发车");
                                // 到站
                                child.setRtInfo(showText);
                                child.setDataChanged(true);
                                child.setRtRank(Child.NOTYET);
                            }
                        }
                        sortGroup();
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                mStationAdapter.notifyDataSetChanged();
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
                    dealRtInfo(busJsonArray, child);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    if (child != null) {
                        Map<String, String> showText = new HashMap<String, String>();
                        if (sequence == 1) {
                            showText.put("itemsText", "起点站");
                            child.setRtInfo(showText);
                            child.setRtRank(Child.FIRSTSTATION);
                            child.setDataChanged(true);
                        } else {
                            showText.put("itemsText", "等待发车");
                            // 到站
                            child.setRtInfo(showText);
                            child.setDataChanged(true);

                            child.setRtRank(Child.NOTYET);
                        }
                    }
                    sortGroup();
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            mStationAdapter.notifyDataSetChanged();
                        }
                    });
                    e.printStackTrace();
                }
            }
        });

    }

    private int TimeStampToDelTime(Long timestampString) {
        if (timestampString < 0)
            return (int) 0;
        double delTime = (timestampString * 1000 - System.currentTimeMillis()) / 1000.0 / 60.0;
        return (int) Math.ceil(delTime);
    }

    private boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("-?[0-9]+.*[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    Comparator<Child> comparator = new Comparator<Child>() {
        public int compare(Child c1, Child c2) {

            if (c1 == null && c2 == null)
                return 0;
            else if (c1 == null)
                return -1;
            else if (c2 == null)
                return 1;
            int rank1 = c1.getRtRank();
            int rank2 = c2.getRtRank();
            if (rank1 > rank2)
                return -1;
            else if (rank1 < rank2)
                return 1;
            else
                return 0;
        }
    };

    Comparator<Map<String, ?>> comparatorRt = new Comparator<Map<String, ?>>() {
        public int compare(Map<String, ?> c1, Map<String, ?> c2) {

            int n1 = Integer.parseInt(c1.get("nextStationNum").toString());
            int n2 = Integer.parseInt(c2.get("nextStationNum").toString());
            if (n1 > n2)
                return -1;
            else if (n1 < n2)
                return 1;
            return 0;

        }
    };

    private void dealRtInfo(JSONArray json, final Child child) {
        Map<String, String> showText = new HashMap<String, String>();
        showText.put("itemsText", "等待发车");
        int rank = Child.NOTYET;
        int sequence = child.getSequence();
        try {
            try {
                int count = json.length();
                Log.i(TAG, "busJsonArray_count: " + count);
                JSONObject uploadJson = new JSONObject();
                JSONArray uploadData = new JSONArray();
                uploadJson.put("c", "beijing");
                uploadJson.put("dt", uploadData);
                int max = 0;
                List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
                // Map<String, Object> map = new HashMap<String, Object>();
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
                    // Log.i(TAG,
                    // "next_station_name: " + nextStationName + "\n"
                    // + "next_station_num: " + nextStationNum
                    // + "\n" + "next_station_distance: "
                    // + nextStationDistance + "\n"
                    // + "next_station_arriving_time: "
                    // + nextStationTime + "\n"
                    // + "station_distance: " + stationDistance
                    // + "\n" + "station_arriving_time: "
                    // + stationArrivingTime + "   " + st_c + "\n"
                    // + " currentTime "
                    // + System.currentTimeMillis());
                    uplodaItem.put("LID", child.getLineID());
                    uplodaItem.put("BID",
                            child.getLineID() + String.format("%02d", j + 1));
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
                    if (nextStationNum <= sequence) {
                        // map.clear();
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("nextStationNum", nextStationNum);
                        map.put("stationArrivingTime", stationArrivingTime);
                        map.put("stationDistance", stationDistance);
                        max = nextStationNum;
                        list.add(map);
                    }
                }
                mVolleyNetwork.upLoadRtInfo(uploadJson, new VolleyNetwork.upLoadListener() {

                    @Override
                    public void onSuccess() {
                        // TODO Auto-generated method stub
                        Log.i(TAG, child.getLineName() + " 上传成功");
                    }

                    @Override
                    public void onFail() {
                        // TODO Auto-generated method stub
                        Log.i(TAG, child.getLineName() + " 上传失败");
                    }
                });
                List<Map<String, ?>> tmpList = child.getRtInfoList();
                tmpList.clear();
                if (list.size() <= 0) {

                    // 起点站
                    if (sequence == 1) {
                        showText.put("itemsText", "起点站");
                        rank = Child.FIRSTSTATION;
                    } // 未开通
                    else {
                        showText.put("itemsText", "等待发车");
                        rank = Child.NOTYET;
                    }
                    // throw new JSONException("等待发车");

                } else {
                    Collections.sort(list, comparatorRt);
                    int size = list.size() > 3 ? 3 : list.size();
                    for (int i = 0; i < size; i++) {
                        Map<String, ?> map = list.get(i);
                        int nextStationNum = (Integer) map
                                .get("nextStationNum");
                        String stationArrivingTime = map.get(
                                "stationArrivingTime").toString();
                        String stationDistance = map.get("stationDistance")
                                .toString();
                        // // 未开通
                        // if (stationArrivingTime == null || nextStationNum ==
                        // 0) {
                        // showText.put("itemsText", "等待发车");
                        // rank = Child.NOTYET;
                        // }
                        // // 起点站
                        // else if (sequence == 1) {
                        // showText.put("itemsText", "起点站");
                        // rank = Child.FIRSTSTATION;
                        // }
                        // 到站
                        Map<String, String> tmpMap = new HashMap<String, String>();
                        if (nextStationNum <= sequence) {
                            if (isNumeric(stationArrivingTime)) {
                                if (nextStationNum == sequence) {
                                    // 已到站
                                    if (Integer.parseInt(stationArrivingTime) < 10) {
                                        // showText.put("itemsText",
                                        // "<font color=\"red\">" + "已到站"
                                        // + "</font>");
                                        tmpMap.put("station", "已经");
                                        tmpMap.put("time", "到站");
                                        rank = Child.ARRIVING;
                                    }
                                    // 即将到站
                                    else if (Integer.parseInt(stationDistance) < 10) {
                                        // showText.put("itemsText",
                                        // "<font color=\"red\">" + "即将到站"
                                        // + "</font>");
                                        tmpMap.put("station", "即将");
                                        tmpMap.put("time", "到站");
                                        rank = Child.SOON;
                                    } else {
                                        int nstime = TimeStampToDelTime(Long
                                                .parseLong(stationArrivingTime));// 计算还有几分钟
                                        if (nstime <= 0) {
                                            // showText.put("itemsText",
                                            // "<font color=\"red\">"
                                            // + "即将到站"
                                            // + "</font>");
                                            tmpMap.put("station", "即将");
                                            tmpMap.put("time", "到站");
                                            rank = Child.SOON;
                                        } else {
                                            // showText.put("itemsText",
                                            // "<font color=\"red\">"
                                            // + nstime + " 分钟"
                                            // + "</font>");
                                            tmpMap.put("station", 1 + " 站");
                                            tmpMap.put("time", nstime + " 分");
                                            rank = Child.ONTHEWAY;
                                        }
                                    }
                                } else {
                                    int nstime = TimeStampToDelTime(Long
                                            .parseLong(stationArrivingTime));// 计算还有几分钟
                                    if (nstime <= 0) {
                                        // showText.put("itemsText",
                                        // "<font color=\"red\">" + "即将到站"
                                        // + "</font>");
                                        tmpMap.put("station", "即将");
                                        tmpMap.put("time", "到站");
                                        rank = Child.SOON;
                                    } else {
                                        // showText.put("itemsText",
                                        // "<font color=\"red\">" + nstime
                                        // + " 分钟" + "</font>");
                                        tmpMap.put("station",
                                                (sequence - nextStationNum)
                                                        + " 站");
                                        tmpMap.put("time", nstime + " 分");
                                        rank = Child.ONTHEWAY;
                                    }
                                }
                            }
                        }
                        tmpList.add(tmpMap);
                        // else {
                        // showText.put("itemsText", "等待发车");
                        // rank = Child.NOTYET;
                        // }
                    }
                }
            } catch (SQLException sqle) {
                throw sqle;
            }

        } catch (JSONException e) {
//			Log.e("JSON exception", e.getMessage());
            e.printStackTrace();
        } finally {
            if (child != null) {
                child.setRtInfo(showText);
                child.setDataChanged(true);
                child.setRtRank(rank);
            }
            sortGroup();
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    mStationAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
//		MobclickAgent.onPageEnd("SplashScreen");
//		MobclickAgent.onPause(this);
        // unregisterReceiver(mWifiReceiver);
        // unregisterReceiver(mActivityReceiver);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onStop() {
        super.onStop();
        dismissLoading();
        cancelRefreshTimertask();
    }

    @Override
    public void onRestart() {
        super.onRestart();
    }

    public void onResume() {
        super.onResume();
//		MobclickAgent.onPageStart("SplashScreen"); // ͳ��ҳ��
//		MobclickAgent.onResume(this); // ͳ��ʱ��
        // registerReceiver(mWifiReceiver, wifiFilter);
        // registerReceiver(mActivityReceiver, activityFilter);
        initTitleRightLayout();
        loadDataBase();
        openRefreshTimertask();
    }

    @Override
    public void onPopClick(Child child) {
        // TODO Auto-generated method stub
        Log.i("pop", "onPopClick: ");
        mChild = child;
        mCanversLayout = (RelativeLayout) findViewById(R.id.rlayout_shadow);
        menuWindow = new SelectPicPopupWindow(StationListActivity.this, mChild,
                mPopItemListener);
        menuWindow.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {// 点击消失
                mCanversLayout.setVisibility(View.GONE);
            }
        });
        menuWindow.showAtLocation(
                StationListActivity.this.findViewById(R.id.DLayout), Gravity.BOTTOM
                        | Gravity.CENTER_HORIZONTAL, 0, 0);
        menuWindow.setFocusable(true);
        menuWindow.setOutsideTouchable(false);
        menuWindow.update();
        mCanversLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoginClick() {

    }

    // 为弹出窗口实现监听类
    private OnClickListener mPopItemListener = new OnClickListener() {

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
            mStationAdapter.notifyDataSetChanged();
        }
    };

    public class MyOverlayManager extends OverlayManager {
        private List<OverlayOptions> mOverLayOptionsList = new ArrayList<OverlayOptions>();

        public MyOverlayManager(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public List<OverlayOptions> getOverlayOptions() {
            return mOverLayOptionsList;
        }

        @Override
        public boolean onMarkerClick(Marker marker) {
            return true;
        }

        public void setData(OverlayOptions optionsList) {
            this.mOverLayOptionsList.add(optionsList);
        }

        public void clearData() {
            for (OverlayOptions option : mOverLayOptionsList)
                option = null;
            this.mOverLayOptionsList.clear();
        }

        public void getInfo() {
            {

            }
        }

        @Override
        public boolean onPolylineClick(Polyline arg0) {
            // TODO Auto-generated method stub
            return false;
        }
    }


    private void initTitleRightLayout() {
        //mAbTitleBar.clearRightView();


        if (mUserDB.IsAlertStation(StationName)) {
            image_alert.setBackgroundResource(R.drawable.icon_isalert);
        } else {
            image_alert.setBackgroundResource(R.drawable.icon_notalert);
        }

    }

    class loadDataBaseTask extends AsyncTask<Integer, Integer, List<Group>> {
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
        protected List<Group> doInBackground(Integer... params) {
            // TODO Auto-generated method stub
            Log.d(TAG, "doInBackground");
            publishProgress();
            return loadBuslineData();
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
        protected void onPostExecute(List<Group> result) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onPostExecute");
            // super.onPostExecute(result);
            if (result != null && result.size() > 0) {
                mGroups = result;
                mStationAdapter.updateData(result);
                mStationAdapter.notifyDataSetChanged();
                int groupCount = result.size();
                if (groupCount > 0)
                    mStationListView.expandGroup(0);
                // getRtParam(result);
                getServerInfo(mGroups);
            } else {
                // mNearHint.setVisibility(View.VISIBLE);
            }
            searchStations();
            dismissLoading();

        }

    }

}