package com.bnrc.busapp.view.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.bnrc.busapp.R;
import com.bnrc.busapp.adapter.MyRoutListViewAdapter;
import com.bnrc.busapp.ui.mapoverlay.DrivingRouteOverlay;
import com.bnrc.busapp.ui.mapoverlay.TransitRouteOverlay;
import com.bnrc.busapp.ui.mapoverlay.WalkingRouteOverlay;
import com.bnrc.busapp.util.LocationUtil;
import com.bnrc.busapp.view.activity.base.BaseActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StationRouteActivity extends BaseActivity {

    private LatLng endPoint = null;
    private MapView mMapView;
    private TextView mTitleTextView;
    private BaiduMap mBaiduMap = null;
    private LatLng startPoint = null;

    // 步行
    private Button walkButton = null;
    private ListView walkListView = null;
    private MyRoutListViewAdapter walkListViewAdapter;
    private List<Map<String, Object>> walkListData;

    // 公交
    private Button busButton = null;
    private ListView busListView = null;
    private MyRoutListViewAdapter busListViewAdapter;
    private List<Map<String, Object>> busListData;

    // 驾车
    private Button driveButton = null;
    private ListView driveListView = null;
    private MyRoutListViewAdapter driveListViewAdapter;
    private List<Map<String, Object>> driveListData;

    private RoutePlanSearch search;
    private LocationUtil mLocationUtil;
    private BDLocation mBdLocation;

    private TextView station_menu_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_station_route);

        mLocationUtil = LocationUtil.getInstance(this.getApplicationContext());
        mBdLocation = mLocationUtil.getmLocation();
        Intent intent = getIntent();


        station_menu_view = findViewById(R.id.station_menu_view);
        station_menu_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mMapView.removeViewAt(2);
        mBaiduMap.setTrafficEnabled(false);


        endPoint = intent.getParcelableExtra("endPoint");
        startPoint = intent.getParcelableExtra("startPoint");

        mTitleTextView = (TextView) findViewById(R.id.mTitleTextView);
        mTitleTextView.setText("以下是到达该站点的步行方案...");
        busButton = (Button) findViewById(R.id.busbtn);
        walkButton = (Button) findViewById(R.id.walkbtn);
        driveButton = (Button) findViewById(R.id.drivebtn);

        walkListView = (ListView) this.findViewById(R.id.mBuslineListView);
        walkListData = new ArrayList<Map<String, Object>>();
        walkListViewAdapter = new MyRoutListViewAdapter(this, walkListData,
                R.layout.list_items, new String[] { "itemsIcon", "itemsTitle",
                "itemsText" }, new int[] { R.id.itemsIcon,
                R.id.tv_stationName, R.id.itemsText });
        walkListView.setAdapter(walkListViewAdapter);

        busListView = (ListView) findViewById(R.id.mRoutListView);
        busListData = new ArrayList<Map<String, Object>>();
        busListViewAdapter = new MyRoutListViewAdapter(this, busListData,
                R.layout.list_items, new String[] { "itemsIcon", "itemsTitle",
                "itemsText" }, new int[] { R.id.itemsIcon,
                R.id.tv_stationName, R.id.itemsText });
        busListView.setAdapter(busListViewAdapter);

        driveListView = (ListView) findViewById(R.id.mRTBusListView);
        driveListData = new ArrayList<Map<String, Object>>();
        driveListViewAdapter = new MyRoutListViewAdapter(this, driveListData,
                R.layout.list_items, new String[] { "itemsIcon", "itemsTitle",
                "itemsText" }, new int[] { R.id.itemsIcon,
                R.id.tv_stationName, R.id.itemsText });
        driveListView.setAdapter(driveListViewAdapter);
        search = RoutePlanSearch.newInstance();
        search.setOnGetRoutePlanResultListener(new OnGetRoutePlanResultListener() {

            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult result) {
                // TODO Auto-generated method stub
                if (result == null
                        || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(StationRouteActivity.this.getApplicationContext(), "抱歉，未找到结果",
                            Toast.LENGTH_SHORT).show();
                }
                if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                    // result.getSuggestAddrInfo()
                    return;
                }
                if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                    // route = result.getRouteLines().get(0);
                    DrivingRouteOverlay overlay = new DrivingRouteOverlay(
                            mBaiduMap);
                    mBaiduMap.setOnMarkerClickListener(overlay);
                    mBaiduMap.clear();
                    if (result.getRouteLines() != null) {
                        if (result.getRouteLines().size() > 0) {
                            overlay.setData(result.getRouteLines().get(0));

                            DrivingRouteLine liendata = result.getRouteLines()
                                    .get(0);
                            List<DrivingRouteLine.DrivingStep> allstepList = liendata
                                    .getAllStep();
                            int j = allstepList.size();
                            Map<String, Object> map = new HashMap<String, Object>();
                            String duration = null;

                            driveListData.clear();
                            for (int i = 0; i < j; i++) {
                                DrivingRouteLine.DrivingStep step = allstepList
                                        .get(i);
                                int d = step.getDuration();

                                if (d > 3600) {
                                    duration = "预计消耗时间 " + d / 3600 + " 小时 "
                                            + d % 3600 / 60
                                            + (d % 60 > 30 ? 1 : 0) + " 分钟";
                                } else if (d > 60) {
                                    duration = "预计消耗时间 "
                                            + (d / 60 + (d % 60 > 30 ? 1 : 0))
                                            + " 分钟";
                                } else {
                                    duration = "预计消耗时间 " + (d+5) + " 秒";
                                }

                                map = new HashMap<String, Object>();
                                if (step.getInstructions().indexOf("步行") >= 0)
                                    map.put("itemsIcon", R.drawable.icon_walk);
                                else {
                                    map.put("itemsIcon", R.drawable.icon_bus);
                                }
                                map.put("itemsTitle", step.getInstructions());
                                map.put("itemsText", duration);
                                driveListData.add(map);
                            }
                            driveListViewAdapter.notifyDataSetChanged();

                            overlay.addToMap();
                            overlay.zoomToSpan();
                            MapStatusUpdate u = MapStatusUpdateFactory
                                    .zoomTo(14.0f);
                            mBaiduMap.animateMapStatus(u);
                            LatLng center = new LatLng(
                                    (startPoint.latitude + endPoint.latitude) / 2,
                                    (startPoint.longitude + endPoint.longitude) / 2);
                            u = MapStatusUpdateFactory.newLatLng(center);
                            mBaiduMap.animateMapStatus(u);
                            mTitleTextView.setText("以下是到达该站点的驾车方案");

                            BitmapDescriptor bitmap = BitmapDescriptorFactory
                                    .fromResource(R.drawable.icon_location);
                            // ����MarkerOption�������ڵ�ͼ�����Marker
                            OverlayOptions option = new MarkerOptions()
                                    .position(new LatLng(mBdLocation.getLatitude(),mBdLocation.getLongitude())).icon(bitmap)
                                    .zIndex(1) // ����marker���ڲ㼶
                                    .draggable(true).title("我的位置"); // ����������ק;
                            // �ڵ�ͼ�����Marker������ʾ
                            mBaiduMap.addOverlay(option);
                        }
                    }
                }
            }

            @Override
            public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

            }

            @Override
            public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

            }

            @Override
            public void onGetTransitRouteResult(TransitRouteResult result) {
                // TODO Auto-generated method stub
                if (result == null
                        || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(StationRouteActivity.this.getApplicationContext(), "抱歉，未找到结果",
                            Toast.LENGTH_SHORT).show();
                }
                if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                    // ���յ��;�����ַ����壬ͨ�����½ӿڻ�ȡ�����ѯ��Ϣ
                    // result.getSuggestAddrInfo()
                    return;
                }
                if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                    // route = result.getRouteLines().get(0);
                    TransitRouteOverlay overlay = new TransitRouteOverlay(
                            mBaiduMap);
                    mBaiduMap.setOnMarkerClickListener(overlay);
                    mBaiduMap.clear();
                    if (result.getRouteLines() != null) {
                        if (result.getRouteLines().size() > 0) {
                            overlay.setData(result.getRouteLines().get(0));

                            TransitRouteLine liendata = result.getRouteLines()
                                    .get(0);
                            List<TransitRouteLine.TransitStep> allstepList = liendata
                                    .getAllStep();
                            int j = allstepList.size();
                            Map<String, Object> map = new HashMap<String, Object>();
                            String duration = null;

                            busListData.clear();
                            for (int i = 0; i < j; i++) {
                                TransitRouteLine.TransitStep step = allstepList
                                        .get(i);
                                int d = step.getDuration();

                                if (d > 3600) {
                                    duration = "预计消耗时间 " + d / 3600 + " 小时 "
                                            + d % 3600 / 60
                                            + (d % 60 > 30 ? 1 : 0) + " 分钟";
                                } else if (d > 60) {
                                    duration = "预计消耗时间 "
                                            + (d / 60 + (d % 60 > 30 ? 1 : 0))
                                            + " 分钟";
                                } else {
                                    duration = "预计消耗时间 " + (d+5) + " 秒";
                                }

                                map = new HashMap<String, Object>();
                                if (step.getInstructions().indexOf("步行") >= 0)
                                    map.put("itemsIcon", R.drawable.icon_walk);
                                else {
                                    map.put("itemsIcon", R.drawable.icon_bus);
                                }
                                map.put("itemsTitle", step.getInstructions());
                                map.put("itemsText", duration);
                                busListData.add(map);
                            }
                            busListViewAdapter.notifyDataSetChanged();

                            overlay.addToMap();
                            overlay.zoomToSpan();
                            MapStatusUpdate u = MapStatusUpdateFactory
                                    .zoomTo(14.0f);
                            mBaiduMap.animateMapStatus(u);
                            LatLng center = new LatLng(
                                    (startPoint.latitude + endPoint.latitude) / 2,
                                    (startPoint.longitude + endPoint.longitude) / 2);
                            u = MapStatusUpdateFactory.newLatLng(center);
                            mBaiduMap.animateMapStatus(u);
                            mTitleTextView.setText("以下是到达该站点的公交方案");

                            BitmapDescriptor bitmap = BitmapDescriptorFactory
                                    .fromResource(R.drawable.icon_location);
                            // ����MarkerOption�������ڵ�ͼ�����Marker
                            OverlayOptions option = new MarkerOptions()
                                    .position(new LatLng(mBdLocation.getLatitude(),mBdLocation.getLongitude())).icon(bitmap)
                                    .zIndex(1) // ����marker���ڲ㼶
                                    .draggable(true).title("我的位置"); // ����������ק;
                            // �ڵ�ͼ�����Marker������ʾ
                            mBaiduMap.addOverlay(option);

                        }
                    }
                }
            }

            @Override
            public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

            }

            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult result) {
                // TODO Auto-generated method stub
                if (result == null
                        || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(StationRouteActivity.this.getApplicationContext(), "抱歉，未找到结果",
                            Toast.LENGTH_SHORT).show();
                }
                if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                    // ���յ��;�����ַ����壬ͨ�����½ӿڻ�ȡ�����ѯ��Ϣ
                    // result.getSuggestAddrInfo()
                    return;
                }
                if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                    // route = result.getRouteLines().get(0);
                    WalkingRouteOverlay overlay = new WalkingRouteOverlay(
                            mBaiduMap);
                    mBaiduMap.setOnMarkerClickListener(overlay);
                    mBaiduMap.clear();
                    if (result.getRouteLines() != null) {
                        if (result.getRouteLines().size() > 0) {
                            overlay.setData(result.getRouteLines().get(0));
                            walkListData.clear();
                            WalkingRouteLine liendata = result.getRouteLines()
                                    .get(0);
                            List<WalkingRouteLine.WalkingStep> allstepList = liendata
                                    .getAllStep();
                            int j = allstepList.size();
                            Map<String, Object> map = new HashMap<String, Object>();
                            String duration = null;
                            for (int i = 0; i < j; i++) {
                                WalkingRouteLine.WalkingStep step = allstepList
                                        .get(i);

                                int d = step.getDuration();

                                if (d > 3600) {
                                    duration = "预计消耗时间 " + d / 3600 + " 小时 "
                                            + d % 3600 / 60
                                            + (d % 60 > 30 ? 1 : 0) + " 分钟";
                                } else if (d > 60) {
                                    duration = "预计消耗时间 "
                                            + (d / 60 + (d % 60 > 30 ? 1 : 0))
                                            + " 分钟";
                                } else {
                                    duration = "预计消耗时间 " + (d + 5) + " 秒";
                                }

                                map = new HashMap<String, Object>();
                                map.put("itemsIcon", R.drawable.icon_walk);
                                map.put("itemsTitle", step.getInstructions());
                                map.put("itemsText", duration);
                                walkListData.add(map);
                            }
                            walkListViewAdapter.notifyDataSetChanged();

                            overlay.addToMap();
                            overlay.zoomToSpan();
                            MapStatusUpdate u = MapStatusUpdateFactory
                                    .zoomTo(17.0f);
                            mBaiduMap.animateMapStatus(u);

                            LatLng center = new LatLng(
                                    (startPoint.latitude + endPoint.latitude) / 2,
                                    (startPoint.longitude + endPoint.longitude) / 2);
                            u = MapStatusUpdateFactory.newLatLng(center);
                            mBaiduMap.animateMapStatus(u);
                            mTitleTextView.setText("以下是到达该站点的步行方案...");
                        }
                    }

                }
            }
        });
        if (startPoint == null) {
            Toast.makeText(this.getApplicationContext(), "无法定位，请检查网络或打开GPS！", Toast.LENGTH_SHORT);
        }
        PlanNode st = PlanNode.withLocation(startPoint);
        PlanNode ed = PlanNode.withLocation(endPoint);
        double distance = mLocationUtil.getDistanceWithLocations(startPoint,
                endPoint);
        if (distance < 1000) {
            search.walkingSearch(new WalkingRoutePlanOption().from(st).to(ed));
            walkListView.setVisibility(View.VISIBLE);
            busListView.setVisibility(View.INVISIBLE);
            driveListView.setVisibility(View.INVISIBLE);
            busButton.setTextColor(Color.rgb(255, 255, 255));
            walkButton.setTextColor(Color.rgb(255, 255, 255));
            driveButton.setTextColor(Color.rgb(255, 255, 255));
            busButton.setBackgroundColor(Color.rgb(200, 200, 200));
            driveButton.setBackgroundColor(Color.rgb(200, 200, 200));
            walkButton.setBackgroundColor(Color.rgb(44, 167, 204));
        } else {
            search.transitSearch(new TransitRoutePlanOption().from(st).to(ed)
                    .city("北京"));
            driveListView.setVisibility(View.INVISIBLE);
            busListView.setVisibility(View.VISIBLE);
            walkListView.setVisibility(View.INVISIBLE);
            busButton.setTextColor(Color.rgb(255, 255, 255));
            walkButton.setTextColor(Color.rgb(255, 255, 255));
            driveButton.setTextColor(Color.rgb(255, 255, 255));
            busButton.setBackgroundColor(Color.rgb(44, 167, 204));
            driveButton.setBackgroundColor(Color.rgb(200, 200, 200));
            walkButton.setBackgroundColor(Color.rgb(200, 200, 200));
        }

        busButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Intent intent = null;
                PlanNode st = PlanNode.withLocation(startPoint);
                PlanNode ed = PlanNode.withLocation(endPoint);
                search.transitSearch(new TransitRoutePlanOption().from(st)
                        .to(ed).city("北京"));
                walkListView.setVisibility(View.INVISIBLE);
                driveListView.setVisibility(View.INVISIBLE);
                busListView.setVisibility(View.VISIBLE);
                busButton.setTextColor(Color.rgb(255, 255, 255));
                walkButton.setTextColor(Color.rgb(255, 255, 255));
                driveButton.setTextColor(Color.rgb(255, 255, 255));
                busButton.setBackgroundColor(Color.rgb(44, 167, 204));
                walkButton.setBackgroundColor(Color.rgb(200, 200, 200));
                driveButton.setBackgroundColor(Color.rgb(200, 200, 200));
            }

        });

        walkButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Intent intent = null;
                PlanNode st = PlanNode.withLocation(startPoint);
                PlanNode ed = PlanNode.withLocation(endPoint);
                search.walkingSearch(new WalkingRoutePlanOption().from(st).to(
                        ed));
                walkListView.setVisibility(View.VISIBLE);
                busListView.setVisibility(View.INVISIBLE);
                driveListView.setVisibility(View.INVISIBLE);
                busButton.setTextColor(Color.rgb(255, 255, 255));
                walkButton.setTextColor(Color.rgb(255, 255, 255));
                driveButton.setTextColor(Color.rgb(255, 255, 255));
                walkButton.setBackgroundColor(Color.rgb(44, 167, 204));
                busButton.setBackgroundColor(Color.rgb(200, 200, 200));
                driveButton.setBackgroundColor(Color.rgb(200, 200, 200));
            }

        });

        driveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Intent intent = null;
                PlanNode st = PlanNode.withLocation(startPoint);
                PlanNode ed = PlanNode.withLocation(endPoint);
                search.drivingSearch(new DrivingRoutePlanOption().from(st).to(
                        ed));
                driveListView.setVisibility(View.VISIBLE);
                busListView.setVisibility(View.INVISIBLE);
                walkListView.setVisibility(View.INVISIBLE);
                busButton.setTextColor(Color.rgb(255, 255, 255));
                walkButton.setTextColor(Color.rgb(255, 255, 255));
                driveButton.setTextColor(Color.rgb(255, 255, 255));
                driveButton.setBackgroundColor(Color.rgb(44, 167, 204));
                busButton.setBackgroundColor(Color.rgb(200, 200, 200));
                walkButton.setBackgroundColor(Color.rgb(200, 200, 200));
            }

        });

    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }
}