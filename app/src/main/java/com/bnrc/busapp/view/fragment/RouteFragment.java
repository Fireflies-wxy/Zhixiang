package com.bnrc.busapp.view.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.bnrc.busapp.R;
import com.bnrc.busapp.database.PCDataBaseHelper;
import com.bnrc.busapp.model.Group;
import com.bnrc.busapp.util.AnimationUtil;
import com.bnrc.busapp.util.LocationUtil;
import com.bnrc.busapp.view.activity.StationListActivity;
import com.bnrc.busapp.view.activity.StationRouteActivity;
import com.bnrc.busapp.view.activity.SugSearchActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by apple on 2018/5/24.
 */

public class RouteFragment extends BaseFragment implements View.OnClickListener{

    private View mContentView;
    private boolean flag = true;

    private TextView tv_start,tv_end;
    private ImageView icon_switch,icon_search;

    // 地图相关
    private ImageView mReLocate;
    public MapView mMapView;
    public PCDataBaseHelper mPcDataBaseHelper = null;
    public BaiduMap mBaiduMap = null;
    public LatLng myPoint = null;
    public LocationUtil mLocationUtil = null;
    private BDLocation mBDLocation = null;
    private LatLng startLng;
    private LatLng endLng;
    List<Overlay> overList = new ArrayList<Overlay>();
    private List<Group> stations;

    private LatLng startPoint,endPoint;
    private String poiname;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();
        mContentView = inflater.inflate(R.layout.fragment_route,container,false);
        initview();
        initmap();
        return mContentView;
    }


    private void initview(){

        mReLocate =  mContentView.findViewById(R.id.iv_reLocate);
        mMapView = mContentView.findViewById(R.id.bmapView);

        tv_start = mContentView.findViewById(R.id.tv_start);
        tv_start.setOnClickListener(this);

        tv_end = mContentView.findViewById(R.id.tv_end);
        tv_end.setOnClickListener(this);

        icon_switch = mContentView.findViewById(R.id.icon_switch);
        icon_switch.setOnClickListener(this);

        icon_search = mContentView.findViewById(R.id.icon_search);
        icon_search.setOnClickListener(this);
    }

    private void initmap(){
        mLocationUtil = LocationUtil.getInstance(mContext);
        // 加载地图和定位
        mBaiduMap = mMapView.getMap();
        // 开启交通图
        mBaiduMap.setTrafficEnabled(true);
        mPcDataBaseHelper = PCDataBaseHelper.getInstance(mContext
                .getApplicationContext());

        getAroundStation();
        mReLocate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                getAroundStation();
            }
        });
        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {

            @Override
            public void onMapStatusChangeStart(MapStatus arg0) {
                // TODO Auto-generated method stub
                startLng = arg0.target;
            }

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) {
                startLng = mapStatus.target;
            }

            @Override
            public void onMapStatusChangeFinish(MapStatus arg0) {
                // TODO Auto-generated method stub
                try {
                    endLng = arg0.target;
                    if (startLng.latitude != endLng.latitude
                            || startLng.longitude != endLng.longitude) {
                        searchStations(arg0.target);
                        MapStatusUpdate u = MapStatusUpdateFactory
                                .zoomTo(18.0f);
                        mBaiduMap.animateMapStatus(u);
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                }

            }

            @Override
            public void onMapStatusChange(MapStatus arg0) {
                // TODO Auto-generated method stub

            }
        });
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {

            @SuppressLint("ResourceAsColor")
            @Override
            public boolean onMarkerClick(final Marker arg0) {
                // TODO Auto-generated method stub

                if (stations == null || stations.size() <= 0)
                    return false;
                Intent intent = new Intent(mContext,
                        StationListActivity.class);
                intent.putExtra("StationName", arg0.getTitle());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                AnimationUtil.activityZoomAnimation(mContext);

                return false;
            }
        });
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {

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

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()){
            case R.id.tv_start:
                //SugSearchActivity.startSearchActivityForResult((FragmentActivity) mContext,tv_start.getText().toString(),1);
                intent = new Intent(mContext,SugSearchActivity.class);
                intent.putExtra("poiname",poiname);
                startActivityForResult(intent,1);
                break;
            case R.id.tv_end:
                //SugSearchActivity.startSearchActivityForResult((FragmentActivity) mContext,null,2);
                intent = new Intent(mContext,SugSearchActivity.class);
                intent.putExtra("poiname","");
                startActivityForResult(intent,2);
                break;
            case R.id.icon_switch:
                String temp = tv_start.getText().toString();
                if(flag){
                    if(tv_start.getText().toString().equals("   我的位置")){
                        if(tv_end.getText().toString().equals("   终点")){
                            tv_start.setText("   起点");
                            tv_start.setTextColor(getResources().getColor(R.color.color_b4b4b4));
                        }else {
                            tv_start.setText(tv_end.getText().toString());
                            tv_start.setTextColor(getResources().getColor(R.color.black));
                        }

                    }else {
                        if(tv_end.getText().toString().equals("   终点")){
                            tv_start.setText("   起点");
                            tv_start.setTextColor(getResources().getColor(R.color.color_b4b4b4));
                        }else {
                            tv_start.setText(tv_end.getText().toString());
                            tv_start.setTextColor(getResources().getColor(R.color.black));
                        }
                    }

                    if(tv_end.getText().toString().equals("   终点")){
                        if(temp.equals("   我的位置")){
                            tv_end.setText("   我的位置");
                            tv_end.setTextColor(getResources().getColor(R.color.black));
                        }else {
                            tv_end.setText(temp);
                            tv_end.setTextColor(getResources().getColor(R.color.black));
                        }
                    }else {
                        if(temp.equals("   起点")){
                            tv_end.setText("   终点");
                            tv_end.setTextColor(getResources().getColor(R.color.color_b4b4b4));
                        }else {
                            tv_end.setText(temp);
                            tv_end.setTextColor(getResources().getColor(R.color.black));
                        }
                    }

                    flag = false;
                }else {

                    if(tv_start.getText().toString().equals("   起点")){
                        if(tv_end.getText().toString().equals("   我的位置")){
                            tv_start.setText("   我的位置");
                            tv_start.setTextColor(getResources().getColor(R.color.black));
                        }else {
                            tv_start.setText(tv_end.getText().toString());
                            tv_start.setTextColor(getResources().getColor(R.color.black));
                        }

                    }else {
                        tv_start.setText(tv_end.getText().toString());
                        tv_start.setTextColor(getResources().getColor(R.color.black));
                    }

                    if(tv_end.getText().toString().equals("   我的位置")){
                        if(temp.equals("   起点")){
                            tv_end.setText("   终点");
                            tv_end.setTextColor(getResources().getColor(R.color.color_b4b4b4));
                        }else {
                            tv_end.setText(temp);
                            tv_end.setTextColor(getResources().getColor(R.color.black));
                        }

                    }else {
                        if(temp.equals("   起点")){
                            tv_end.setText("   终点");
                            tv_end.setTextColor(getResources().getColor(R.color.color_b4b4b4));
                        }else {
                            tv_end.setText(temp);
                            tv_end.setTextColor(getResources().getColor(R.color.black));
                        }
                    }

                    flag = true;
                }

                LatLng templat = startPoint;
                startPoint = endPoint;
                endPoint = templat;

                break;
            case R.id.icon_search:
                intent = new Intent(mContext,StationRouteActivity.class);
                intent.putExtra("startPoint", startPoint);
                intent.putExtra("endPoint",endPoint);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                AnimationUtil.activityZoomAnimation(mContext);
                break;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void searchStations(LatLng newPos) {
        mBaiduMap.clear();
        overList.clear();

//        try {
//            stations = mPcDataBaseHelper
//                    .acquireAroundStationsWithLocation(newPos);
//            Toast.makeText(mContext.getApplicationContext(),
//                    "在附近查找到" + stations.size() + "个站点", Toast.LENGTH_SHORT)
//                    .show();
//            for (Group station : stations) {
//                // 定义Maker坐标点
//                double Latitude = station.getLatitide();
//                double Longitude = station.getLongitude();
//                LatLng stationPoint = new LatLng(Latitude, Longitude);
//                View view = LayoutInflater.from(mContext)
//                        .inflate(R.layout.map_station_popview, null);
//                TextView tv_stationName = (TextView) view
//                        .findViewById(R.id.tv_stationName);
//                tv_stationName.setText(station.getStationName());
//                BitmapDescriptor bitmap = BitmapDescriptorFactory
//                        .fromView(view);
//                MarkerOptions option2 = new MarkerOptions()
//                        .position(stationPoint).icon(bitmap).zIndex(9) // 设置marker所在层级
//                        .draggable(true).title(station.getStationName()); // 设置手势拖拽;
//                option2.animateType(MarkerOptions.MarkerAnimateType.grow);
//
//                overList.add(mBaiduMap.addOverlay(option2));
//
//            }
//        } catch (SQLException sqle) {
//            throw sqle;
//        }

    }

    private void getAroundStation() {
        mBaiduMap.clear();
        overList.clear();
        mBDLocation = mLocationUtil.getmLocation();
        if (mBDLocation != null) {
            poiname = mBDLocation.getAddrStr();
            Toast.makeText(mContext.getApplicationContext(),mBDLocation.getAddrStr(),Toast.LENGTH_SHORT).show();
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(mBDLocation.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(mBDLocation.getLatitude())
                    .longitude(mBDLocation.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            myPoint = new LatLng(mBDLocation.getLatitude(),
                    mBDLocation.getLongitude());
            startPoint = myPoint;

            // 开启定位图层
            mBaiduMap.setMyLocationEnabled(true);

            MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(myPoint,
                    18.0f);
            mBaiduMap.animateMapStatus(u);
        }
        mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL, true, null));
//
//        try {
//            stations = mPcDataBaseHelper
//                    .acquireAroundStationsWithLocation(myPoint);
//
//            Toast.makeText(mContext.getApplicationContext(),
//                    "在附近查找到" + stations.size() + "个站点", Toast.LENGTH_SHORT)
//                    .show();
//            for (Group station : stations) {
//                // 定义Maker坐标点
//                double Latitude = station.getLatitide();
//                double Longitude = station.getLongitude();
//                LatLng stationPoint = new LatLng(Latitude, Longitude);
//                View view = LayoutInflater.from(mContext)
//                        .inflate(R.layout.map_station_popview, null);
//                TextView tv_stationName = (TextView) view
//                        .findViewById(R.id.tv_stationName);
//                tv_stationName.setText(station.getStationName());
//                BitmapDescriptor bitmap = BitmapDescriptorFactory
//                        .fromView(view);
//                MarkerOptions option2 = new MarkerOptions()
//                        .position(stationPoint).icon(bitmap).zIndex(9) // 设置marker所在层级
//                        .draggable(true).title(station.getStationName()); // 设置手势拖拽;
//                overList.add(mBaiduMap.addOverlay(option2));
//
//            }
//        } catch (SQLException sqle) {
//            throw sqle;
//        }

    }

    public void onResume() {
        super.onResume();
        mMapView.onResume();

    }

    public void onPause() {
        super.onPause();
        mMapView.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.i("testResult", "not ok "+requestCode);
        if (resultCode != Activity.RESULT_OK)
            return;
        PoiInfo poi = intent.getParcelableExtra("PoiInfo");
        Log.i("testResult", "onActivityResult: "+requestCode);
        switch (requestCode) {
            case 1:
                tv_start.setText("   "+poi.name);
                startPoint = poi.location;
                break;
            case 2:
                tv_end.setText("   "+poi.name);
                tv_end.setTextColor(getResources().getColor(R.color.black));
                endPoint = poi.location;
                break;
        }
    }
}


