package com.bnrc.busapp.view.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.ListPopupWindow;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.poi.PoiSortType;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.DistanceUtil;
import com.bnrc.busapp.R;
import com.bnrc.busapp.adapter.SortAdapter;
import com.bnrc.busapp.database.PCDataBaseHelper;
import com.bnrc.busapp.listener.GetLocationListener;
import com.bnrc.busapp.model.AR.ARCamera;
import com.bnrc.busapp.model.AR.AROverlayView;
import com.bnrc.busapp.model.AR.ARPoint;
import com.bnrc.busapp.model.Child;
import com.bnrc.busapp.model.Group;
import com.bnrc.busapp.model.bus.BusInfo;
import com.bnrc.busapp.model.bus.BusModel;
import com.bnrc.busapp.network.RequestCenter;
import com.bnrc.busapp.network.listener.DisposeDataListener;
import com.bnrc.busapp.util.LocationUtil;
import com.bnrc.busapp.util.MyCipher;
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
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ARActivity extends BaseActivity implements SensorEventListener,OnGetPoiSearchResultListener {

    final static String TAG = "ARActivity";
    private SurfaceView surfaceView;
    private FrameLayout cameraContainerLayout;
    private AROverlayView arOverlayView;
    private Camera camera;
    private ARCamera arCamera;
    public LocationClient mLocationClient;
    private PoiSearch mSearch = null;
    private String keyword = "酒店";
    private ImageView menu_view_ar;
    private OkHttpClient mOkHttpClient;
    private CoordinateConverter mCoordConventer;
    public PCDataBaseHelper mDataBaseManager = null;


    //AR相关
    private FrameLayout mARContainer;
    private float[] rotatedProjectionMatrix = new float[16];
    private BDLocation currentLocation;
    private ArrayList<ARPoint> arPoints = new ArrayList<>();
    private View poiTag;

    //下拉菜单控件
    private ListPopupWindow listPopupWindow = null;
    private ImageView arrowImageView;
    private RelativeLayout relativeLayout;
    private SortAdapter adapter = null;

    private ImageView img_close;
    private TextView tv_ar_title,icon_search;


    private SensorManager sensorManager;
    private final static int REQUEST_CAMERA_PERMISSIONS_CODE = 11;
    public static final int REQUEST_LOCATION_PERMISSIONS_CODE = 0;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 0;//1000 * 60 * 1; // 1 minute

    private LocationManager locationManager;
    public Location location;
    public BDLocation bdLocation;
    boolean isGPSEnabled;
    boolean isNetworkEnabled;
    boolean locationServiceAvailable;

    public LocationUtil mLocationUtil = null;
    private BDLocation mBDLocation = null;

    private SharedPreferenceUtil mSharePrefrenceUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);

        mSharePrefrenceUtil = SharedPreferenceUtil.getInstance(this.getApplicationContext());

        mLocationUtil = LocationUtil.getInstance(this
                .getApplicationContext());


        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        cameraContainerLayout = (FrameLayout) findViewById(R.id.camera_container_layout);
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        mARContainer = findViewById(R.id.poiGroup_layout);
        tv_ar_title = findViewById(R.id.tv_ar_title);
        initDownSpinner();


        arOverlayView = new AROverlayView(this,mARContainer);

        mSearch = PoiSearch.newInstance();
        mSearch.setOnGetPoiSearchResultListener(this);


        requestLocationPermission();
        requestCameraPermission();
        registerSensors();

        findViewById(R.id.close_view_ar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        icon_search = findViewById(R.id.icon_search);
        icon_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ARActivity.this,SearchActivity.class);
                intent.putExtra("isAR",true);
                startActivityForResult(intent,1);
            }
        });

        mCoordConventer = new CoordinateConverter();
        mDataBaseManager = PCDataBaseHelper
                .getInstance(ARActivity.this);

    }

    /**
     * 以下两个方法用于初始化下拉菜单
     */

    public void initDownSpinner(){
        relativeLayout = findViewById(R.id.rl_ar);

        arrowImageView = findViewById(R.id.poi_pick_arrow);

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showListPopupWindow(relativeLayout);
            }
        });
    }

    public void showListPopupWindow(View view) {
        if (listPopupWindow == null)
            listPopupWindow = new ListPopupWindow(this);

        if (adapter == null) {
            adapter = new SortAdapter(this, android.R.layout.simple_list_item_1);

            // ListView适配器
            listPopupWindow.setAdapter(adapter);

            // 选择item的监听事件
            listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                    Toast.makeText(getApplicationContext(), adapter.getItem(pos), Toast.LENGTH_SHORT).show();
                    tv_ar_title.setText(adapter.getItem(pos));
                    mARContainer.removeAllViews();
                    Log.i("poiTag", "removed");
                    keyword = adapter.getItem(pos);
                    if(bdLocation==null){
                        initLocationService();
                        Toast.makeText(ARActivity.this,"定位错误，请重新选择关键字",Toast.LENGTH_SHORT).show();
                    }else {
                        if(keyword.equals("公交")){
                            getNearStations();
                        }else {
                            SearchPoi(keyword,bdLocation);
                        }

                    }

                    listPopupWindow.dismiss();
                }
            });

            listPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    //旋转0度是复位ImageView
                    arrowImageView.animate().setDuration(500).rotation(0).start();
                }
            });
        }


        // ListPopupWindow的锚,弹出框的位置是相对当前View的位置
        listPopupWindow.setAnchorView(view);

        listPopupWindow.setVerticalOffset(dip2px(this,12));

        // 对话框的宽高
        listPopupWindow.setWidth(view.getWidth());

        listPopupWindow.setModal(true);

        listPopupWindow.show();
        arrowImageView.animate().setDuration(500).rotation(180).start();
    }

    public void getNearStations(){
         LatLng currentLoc = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());

         List<Group> mNearGroups  = PCDataBaseHelper.getInstance(
                this.getApplicationContext()).acquireAroundStationsWithLocation(currentLoc);
         if(mNearGroups.size()!=0){
             for(Group group:mNearGroups){
                 arPoints.add(new ARPoint(group.getStationName(),
                         (int)DistanceUtil.getDistance(currentLoc,new LatLng(group.getLatitide(),group.getLongitude()))+"m",
                         group.getLatitide(),
                         group.getLongitude(),
                         0,
                         2));
             }
         }
        if(arOverlayView!=null)
            arOverlayView.updatePoiResult(arPoints);
    }


    public static int dip2px(Context context, float dipValue) {
        float sDensity = context.getResources().getDisplayMetrics().density;
        final float scale = sDensity;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * 以下两个方法用于获取相关权限
     */

    public void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSIONS_CODE);
        } else {
            initARCameraView();
        }
    }

    public void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSIONS_CODE);
        } else {
            initLocationService();
        }
    }

    /**
     * 初始化AROverlayView
     */

    public void initAROverlayView() {
        if (arOverlayView.getParent() != null) {
            ((ViewGroup) arOverlayView.getParent()).removeView(arOverlayView);
        }
        cameraContainerLayout.addView(arOverlayView);

    }

    /**
     * 初始化ARCameraView
     */

    public void initARCameraView() {
        reloadSurfaceView();

        if (arCamera == null) {
            arCamera = new ARCamera(this, surfaceView);
        }
        if (arCamera.getParent() != null) {
            ((ViewGroup) arCamera.getParent()).removeView(arCamera);
        }
        cameraContainerLayout.addView(arCamera);
        arCamera.setKeepScreenOn(true);
        initCamera();
    }

    private void reloadSurfaceView() {
        if (surfaceView.getParent() != null) {
            ((ViewGroup) surfaceView.getParent()).removeView(surfaceView);
        }
        cameraContainerLayout.addView(surfaceView);
    }

    /**
     * 初始化相机
     */

    private void initCamera() {
        int numCams = Camera.getNumberOfCameras();
        if(numCams > 0){
            try{
                camera = Camera.open();
                camera.startPreview();
                arCamera.setCamera(camera);
            } catch (RuntimeException ex){
                Toast.makeText(getApplicationContext(), "Camera not found", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 释放相机资源
     */

    private void releaseCamera() {
        if(camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            arCamera.setCamera(null);
            camera.release();
            camera = null;
        }
    }

    private void registerSensors() {
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] rotationMatrixFromVector = new float[16];
            float[] projectionMatrix = new float[16];
            float[] rotatedProjectionMatrix = new float[16];

            SensorManager.getRotationMatrixFromVector(rotationMatrixFromVector, sensorEvent.values);

            if (arCamera != null) {
                projectionMatrix = arCamera.getProjectionMatrix();
            }

            Matrix.multiplyMM(rotatedProjectionMatrix, 0, projectionMatrix, 0, rotationMatrixFromVector, 0);
            this.arOverlayView.updateRotatedProjectionMatrix(rotatedProjectionMatrix);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //do nothing
    }

    private void initLocationService() {

        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }

        try   {
            this.locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
            // Get GPS and network status
            this.isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            this.isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            this.locationServiceAvailable = true;

            if (!isNetworkEnabled && !isGPSEnabled)    {
                // cannot get location
                this.locationServiceAvailable = false;
                if(!isNetworkEnabled)
                    Toast.makeText(getApplicationContext(),"无法获取定位信息，请检查网络连接是否打开。",Toast.LENGTH_LONG).show();
                else if(!isGPSEnabled)
                    Toast.makeText(getApplicationContext(),"无法获取定位信息，请检查GPS是否打开。",Toast.LENGTH_LONG).show();
            }

            mLocationClient = new LocationClient(getApplicationContext());
            LocationClientOption option = new LocationClientOption();
            option.setOpenAutoNotifyMode();
            option.setCoorType("bd09ll");
            option.setIsNeedLocationPoiList(true);
            mLocationClient.setLocOption(option);

            if(mLocationClient!=null){
                if(locationServiceAvailable){
                    mLocationClient.registerLocationListener(new BDLocationListener() {
                        @Override
                        public void onReceiveLocation(BDLocation bdLocation) {
                            ARActivity.this.bdLocation = bdLocation;
                            updateLatestLocation(bdLocation);
                       }

                    });
                    mLocationClient.start();
                }
            }
        } catch (Exception ex)  {
            Log.e(TAG, ex.getMessage());

        }
    }

    private void updateLatestLocation(BDLocation bdlocation) {
        if (arOverlayView !=null) {
            arOverlayView.updateCurrentLocation(bdlocation);

            Log.i("poiresultinfo", "update invoked ");
            SearchPoi(keyword,bdlocation);

        }

    }

    private void SearchPoi(String keyword,BDLocation bdlocation){
        Log.i("poiresultinfo", "search invoked ");
        mSearch.searchNearby(new PoiNearbySearchOption()
                .keyword(keyword)
                .sortType(PoiSortType.comprehensive)
                .location(new LatLng(bdlocation.getLatitude(),bdlocation.getLongitude()))
                .radius(2000)
                .sortType(PoiSortType.distance_from_near_to_far)
                .pageNum(10));
    }


    @Override
    public void onGetPoiResult(PoiResult poiResult) {
        if (poiResult == null || poiResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(getApplicationContext(), "抱歉，未找到结果", Toast.LENGTH_LONG)
                    .show();
            return;
        }else {
            for(PoiInfo poi:poiResult.getAllPoi()){
                Log.i(TAG, "onGetPoiResult: ");
            }
            if(arOverlayView!=null)
                arOverlayView.updatePoiResult(poiResult);
        }

    }

    //以下三个方法没用到

    @Override
    public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }

    @Override
    public void onResume() {
        super.onResume();
        initARCameraView();
        initAROverlayView();
    }

    @Override
    public void onPause() {
        releaseCamera();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        arOverlayView.cancleTask();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode != Activity.RESULT_OK)
            return;
        int searchType = intent.getIntExtra("searchType",0);
        final LatLng currentLoc = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());

        switch (searchType) {
            case 1:
                mARContainer.removeAllViews();

                String stationName = intent.getStringExtra("stationName");
                LatLng stationLoc = intent.getParcelableExtra("stationLoc");

                final ARPoint arPoint = new ARPoint(stationName,(int)DistanceUtil.getDistance(currentLoc,stationLoc)+"m",stationLoc.latitude,stationLoc.longitude,0,2);
                if(arOverlayView!=null)
                    arOverlayView.updatePoiResult(arPoint);
                break;
            case 2:
                mARContainer.removeAllViews();
                String LID = String.valueOf(intent.getIntExtra("LineID",0));
                String LineName = intent.getStringExtra("LineName");
                try {
                    getRtInfo(LID,LineName);
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            default:
                Toast.makeText(ARActivity.this.getApplicationContext(),"搜索发生错误，请重试",Toast.LENGTH_SHORT).show();
        }
    }

    private void getRtInfo(String LineID, final String LineName) throws JSONException,
            UnsupportedEncodingException {
        Log.i(TAG, "getRtInfo: ");
        Map<String, Object> LineInfo = mDataBaseManager
                .acquireLineInfoWithLineID(Integer.parseInt(LineID));
        int OfflineID = 0;
        if (LineInfo != null && LineInfo.size() > 0) {
            OfflineID = Integer.parseInt(LineInfo.get("OfflineID")
                    .toString());
        }
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

                    String status = rootJson.getString("status");

                    if(status.equals("502")){
                        Looper.prepare();
                        Toast.makeText(ARActivity.this.getApplicationContext(),"该线路暂无实时信息",Toast.LENGTH_SHORT).show();
                        arPoints.clear();
                        Looper.loop();
                        if(arOverlayView!=null)
                            arOverlayView.updatePoiResult(arPoints);

                        return;
                    }

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

                    int busJsonArray_count = busJsonArray.length();
                    Log.i(TAG, "busJsonArray_count: "
                            + busJsonArray_count);

                    for (int j = 0; j < busJsonArray_count; j++) {
                        Log.i(TAG, "onResponse: Added!");

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

                        LatLng rtStationPoint = new LatLng(ylat, xLon);
                        LatLng rtSLatLngBaidu = mCoordConventer
                                .from(CoordinateConverter.CoordType.COMMON)
                                .coord(rtStationPoint).convert();
                        final LatLng currentLoc = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
                        arPoints.add(new ARPoint(LineName+"公交",(int)DistanceUtil.getDistance(currentLoc,rtSLatLngBaidu)+"m",ylat,xLon,0,3));
                    }
                    if(arOverlayView!=null)
                        arOverlayView.updatePoiResult(arPoints);

                }

                catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });
    }

}

