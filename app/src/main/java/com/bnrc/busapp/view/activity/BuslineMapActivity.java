package com.bnrc.busapp.view.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapLoadedCallback;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.busline.BusLineSearch;
import com.baidu.mapapi.search.busline.BusLineSearchOption;
import com.baidu.mapapi.search.busline.OnGetBusLineSearchResultListener;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.utils.CoordinateConverter;

import android.content.Intent;


import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapView;
import com.bnrc.busapp.R;
import com.bnrc.busapp.database.PCDataBaseHelper;
import com.bnrc.busapp.database.PCUserDataDBHelper;
import com.bnrc.busapp.model.Child;
import com.bnrc.busapp.network.MyVolley;
import com.bnrc.busapp.ui.mapoverlay.StationOverlay;
import com.bnrc.busapp.util.LocationUtil;
import com.bnrc.busapp.util.MyCipher;
import com.bnrc.busapp.util.NetAndGpsUtil;
import com.bnrc.busapp.util.SharedPreferenceUtil;
import com.bnrc.busapp.view.activity.base.BaseActivity;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;


public class BuslineMapActivity extends BaseActivity implements
        OnGetPoiSearchResultListener, OnGetBusLineSearchResultListener,
        BaiduMap.OnMapClickListener {
    private static final String TAG = BuslineMapActivity.class.getSimpleName();
    private String LineName = "";
    private String StartStation = "";
    private String EndStation = "";
    private int OfflineID;
    private int LineID = 0;
    private BusLineResult route = null;
    private List<String> poiIDList = null;
    public List<Object> stationItems = null;
    private int poiIndex = 0;
    public LatLng mPoint = null;
    public PCDataBaseHelper dabase = null;
    public PCUserDataDBHelper userdabase = null;
    public LocationUtil mLocationUtil = null;
    public HorizontalScrollView mScrollView = null;
    // 搜索相关
    private PoiSearch mSearch = null; // 搜索模块，也可去掉地图模块独立使用
    private BusLineSearch mBusLineSearch = null;
    private BaiduMap mBaiduMap = null;
    public MapView mMapView;
    private List<Child> stationList = null;
    private ArrayList<View> busArrayList = null;
    private LinearLayout stationContainer;
    private FrameLayout busContainer;
    private int stationItemWidth = 0;
    private TimerTask mTask;
    private SharedPreferenceUtil mSharePrefrenceUtil;
    private Timer mTimer;
    private TextView mTitleText;
    private OkHttpClient mOkHttpClient;
    private NetAndGpsUtil mNetAndGpsUtil;
    private BDLocation mBDLocation = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_busline_map);

        Log.i(TAG, "onCreate: ");

        Intent intent = getIntent();

        LineName = intent.getStringExtra("LineName");
        StartStation = intent.getStringExtra("StartStation");
        EndStation = intent.getStringExtra("EndStation");
        LineID = intent.getIntExtra("LineID", 0);
        OfflineID = intent.getIntExtra("OfflineID", 0);

        String FullName = LineName + " (" + StartStation + " - " + EndStation
                + ")";
        mTitleText = findViewById(R.id.tv_map_title);
        mTitleText.setText(FullName);

        findViewById(R.id.map_menu_view).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mLocationUtil = LocationUtil.getInstance(BuslineMapActivity.this);
        mNetAndGpsUtil = NetAndGpsUtil.getInstance(this
                .getApplicationContext());
        dabase = PCDataBaseHelper.getInstance(BuslineMapActivity.this);
        userdabase = PCUserDataDBHelper.getInstance(BuslineMapActivity.this);
        // 加载地图和定位
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        // 开启交通图
        mBaiduMap.setTrafficEnabled(false);
        mBaiduMap.setOnMapClickListener(this);

        mSearch = PoiSearch.newInstance();
        mSearch.setOnGetPoiSearchResultListener(this);

        mScrollView = (HorizontalScrollView) findViewById(R.id.mScrollView);

        mBusLineSearch = BusLineSearch.newInstance();
        mBusLineSearch.setOnGetBusLineSearchResultListener(this);

        poiIDList = new ArrayList<String>();
        poiIndex = 0;
        stationContainer = (LinearLayout) findViewById(R.id.stationList);
        busContainer = (FrameLayout) findViewById(R.id.busList);
        stationList = dabase.acquireStationsWithBuslineID(LineID);

        busArrayList = new ArrayList<View>();
        optionList = new ArrayList<Marker>();
        mCoordConventer = new CoordinateConverter();

        mScrollView.setVisibility(View.GONE);


        mBDLocation = mLocationUtil.getmLocation();
        if (mBDLocation != null) {
            Toast.makeText(this.getApplicationContext(),mBDLocation.getAddrStr(),Toast.LENGTH_SHORT).show();
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(mBDLocation.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(mBDLocation.getLatitude())
                    .longitude(mBDLocation.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);

            // 开启定位图层
            mBaiduMap.setMyLocationEnabled(true);

            MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(new LatLng(mBDLocation.getLatitude(),
                    mBDLocation.getLongitude()),
                    18.0f);

        }

        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL, true, null));

        mBaiduMap.setOnMapLoadedCallback(new OnMapLoadedCallback() {

            @Override
            public void onMapLoaded() {
                // TODO Auto-generated method stub
                try {
                    loadBuslineMap();
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        mTask = new TimerTask() {
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            getRtInfo();
                        } catch (UnsupportedEncodingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        mSharePrefrenceUtil = SharedPreferenceUtil.getInstance(this
                .getApplicationContext());
        String value = mSharePrefrenceUtil.getValue("refreshFrequency", "30秒");
        int timeInternal = Integer.parseInt(value.substring(0,
                value.indexOf("秒")));
        mTimer = new Timer(true);
        mTimer.schedule(mTask, timeInternal * 1000, timeInternal * 1000);
        mBaiduMap.setOnMapClickListener(new OnMapClickListener() {

            @Override
            public boolean onMapPoiClick(MapPoi arg0) {
                // TODO Auto-generated method stub
                mBaiduMap.hideInfoWindow();
                return false;
            }

            @Override
            public void onMapClick(LatLng arg0) {
                // TODO Auto-generated method stub
                mBaiduMap.hideInfoWindow();
            }
        });
        mMapView.removeViewAt(2);
    }

    private void SearchNextBusline(View v) {
        if (poiIndex >= poiIDList.size()) {
            poiIndex = 0;
        }
        if (poiIndex >= 0 && poiIndex < poiIDList.size()
                && poiIDList.size() > 0) {
            mBusLineSearch.searchBusLine((new BusLineSearchOption().city("北京")
                    .uid(poiIDList.get(poiIndex))));
            poiIndex++;
        }
    }

    private void loadBuslineMap() throws JSONException {
        // 发起poi检索，从得到所有poi中找到公交线路类型的poi，再使用该poi的uid进行公交详情搜索
        mSearch.searchInCity((new PoiCitySearchOption()).city("北京").keyword(
                LineName));
        // 如下代码为发起检索代码，定义监听者和设置监听器的方法与POI中的类似
        mBusLineSearch.searchBusLine((new BusLineSearchOption().city("北京")
                .uid(LineName)));
    }


    @Override
    public void onPause() {
        super.onPause();
        if (mTask != null)
            mTask.cancel();
        mTimer.cancel();
        mMapView.onPause();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onDestroy() {
        mSearch.destroy();
        mBusLineSearch.destroy();
        mMapView.onDestroy();

        super.onDestroy();
    }

    @Override
    public void onGetBusLineResult(BusLineResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {

            return;
        }
        mBaiduMap.clear();

        route = result;

        StationOverlay overlay = new StationOverlay(mBaiduMap,
                BuslineMapActivity.this);
        mBaiduMap.setOnMarkerClickListener(overlay);
        overlay.setData(result);
        overlay.addToMap();
        overlay.zoomToSpan();
        Log.i(TAG, "overlay ");
        // getRtInfo();
        try {
            getRtInfo();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGetPoiResult(PoiResult result) {

        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(BuslineMapActivity.this.getApplicationContext(), "抱歉，未找到结果", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        // 遍历所有poi，找到类型为公交线路的poi
        poiIDList.clear();
        for (PoiInfo poi : result.getAllPoi()) {
            if (poi.type == PoiInfo.POITYPE.BUS_LINE
                    || poi.type == PoiInfo.POITYPE.SUBWAY_LINE) {
                poiIDList.add(poi.uid);
            }
        }
        SearchNextBusline(null);
        route = null;
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult result) {

    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }

    @Override
    public void onMapClick(LatLng point) {
        mBaiduMap.hideInfoWindow();
    }

    @Override
    public boolean onMapPoiClick(MapPoi poi) {
        return false;
    }

    public void showStationView(final int index) throws JSONException {
        // TODO Auto-generated method stub
        if (route != null) {
            if (index > 0 && index < route.getStations().size()) {
                Toast toast = Toast.makeText(BuslineMapActivity.this.getApplicationContext(), route
                                .getStations().get(index).getTitle(),
                        Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    private ArrayList<Marker> optionList;
    private CoordinateConverter mCoordConventer;


    private void getRtInfo() throws JSONException,
            UnsupportedEncodingException {
        Log.i(TAG, "getRtInfo: ");
//        String Url =
//                "http://223.72.210.21:8512/bus.php?city="
//                        + URLEncoder.encode("北京", "utf-8") + "&id=" + OfflineID
//                        + "&no=1&type=1&encrypt=0&versionid=2";
        String Url =
                "http://223.72.210.21:8512/ssgj/bus.php?city="
                        + URLEncoder.encode("北京", "utf-8") + "&id=" + OfflineID
                        + "&no=" + 1 + "&type=2&encrypt=1&versionid=2";
        // 创建一个Request
        final Request request = new Request.Builder().url(Url).build();
        // new call
        if(mOkHttpClient==null){
            mOkHttpClient = new OkHttpClient();
        }
        Call call = mOkHttpClient.newCall(request);
        // 请求加入调度
        call.enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException arg1) {
                Log.d(TAG, "!!!!!!!faliure: ");
            }

            @Override
            public void onResponse(Call call, okhttp3.Response res)
                    throws IOException {
                // TODO Auto-generated method stub
                Log.d(TAG, "!!!!!!!response: " + res);

                String response = res.body().string();
                try {

                    JSONObject responseJson = XML.toJSONObject(response);
                    JSONObject rootJson = responseJson.getJSONObject("root");

                    Log.i(TAG, "rootJson: " + rootJson.toString());

                    JSONObject dataJson = rootJson
                            .getJSONObject("data");
                    JSONArray busJsonArray;
                    if (dataJson.toString().indexOf("[") > 0) {
                        // busJsonArray = (JSONArray) dataJson
                        // .get("bus");
                        busJsonArray = dataJson.getJSONArray("bus");
                        Log.i(TAG,
                                "busJsonArray "
                                        + busJsonArray.toString());

                    } else {

                        JSONObject busJsonObject = dataJson
                                .getJSONObject("bus");
                        Log.i(TAG,
                                "busJsonObject "
                                        + busJsonObject.toString());
                        busJsonArray = new JSONArray("["
                                + busJsonObject.toString() + "]");
                        Log.i(TAG, "busJsonObject to array: "
                                + busJsonArray.toString());
                    }
                    for (Marker marker : optionList)
                        marker.remove();
                    optionList.clear();
                    int busJsonArray_count = busJsonArray.length();
                    Log.i(TAG, "busJsonArray_count: "
                            + busJsonArray_count);
                    for (int j = 0; j < busJsonArray_count; j++) {

                        JSONObject busJson = (JSONObject) busJsonArray
                                .get(j);

                        MyCipher mCiper = new MyCipher("aibang"
                                + busJson.getString("gt"));

                        String ns = mCiper.decrypt(busJson
                                .getString("ns"));
                        String nsn = mCiper.decrypt(busJson
                                .getString("nsn"));
                        String sd = mCiper.decrypt(busJson
                                .getString("sd"));
                        double xLon = Double.parseDouble(mCiper
                                .decrypt(busJson.getString("x")));
                        double ylat = Double.parseDouble(mCiper
                                .decrypt(busJson.getString("y")));
                        Log.i(TAG, "next_station_name: " + ns + "\n"
                                + "next_station_num: " + nsn + "\n"
                                + "station_distance: " + sd + "\n"
                                + "latitude: " + xLon + "\n"
                                + "longitude: " + ylat
                                + "\n********************\n");
                        BitmapDescriptor bitmap = BitmapDescriptorFactory
                                .fromResource(R.drawable.br_clr_ptrs);
                        LatLng rtStationPoint = new LatLng(ylat, xLon);
                        LatLng rtSLatLngBaidu = mCoordConventer
                                .from(CoordinateConverter.CoordType.COMMON)
                                .coord(rtStationPoint).convert();
                        // 构建MarkerOption，用于在地图上添加Marker
                        OverlayOptions myOption = new MarkerOptions()
                                .position(rtSLatLngBaidu).icon(bitmap)
                                .title("我的位置");
                        // 在地图上添加Marker，并显示
                        Marker marker = (Marker) mBaiduMap
                                .addOverlay(myOption);
                        optionList.add(marker);

                        mMapView.getMap().addOverlay(myOption);
                    }

                }

                catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // TODO Auto-generated method stub
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            try {
                loadBuslineMap();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // 延时1000ms后执行，1000 ms执行一次
            // 退出计时器
        }
    }

}
