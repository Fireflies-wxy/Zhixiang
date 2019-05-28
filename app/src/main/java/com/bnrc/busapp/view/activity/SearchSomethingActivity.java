package com.bnrc.busapp.view.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.bnrc.busapp.R;
import com.bnrc.busapp.ui.mapoverlay.PoiOverlay;
import com.bnrc.busapp.util.LocationUtil;
import com.bnrc.busapp.view.activity.base.BaseActivity;


public class SearchSomethingActivity extends BaseActivity {

	public MapView mMapView;
	public BaiduMap mBaiduMap = null;
	public LatLng mPoint = null;
	public LocationUtil mLocationUtil = null;
	private PoiSearch mPoiSearch = null;
	private String Keyword = null;
	private BDLocation mBDLocation = null;
	RelativeLayout mAdContainer;
	private TextView tv_near,icon_back;

	@SuppressLint("ResourceAsColor")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_search_something);

		mLocationUtil = LocationUtil.getInstance(this.getApplicationContext());

		icon_back = findViewById(R.id.icon_back);
		icon_back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});

		// 加载地图和定位
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();

		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@SuppressLint("ResourceAsColor")
			@Override
			public boolean onMarkerClick(final Marker arg0) {
				// TODO Auto-generated method stub
				// 创建InfoWindow展示的view
				if (arg0 == null)
					return false;
				final LatLng pt = arg0.getPosition();
				double distance = mLocationUtil.getDistanceWithLocations(
						mPoint, pt);
				// button.setBackgroundResource(R.drawable.popup);
				if ("我的位置".equalsIgnoreCase(arg0.getTitle())) {
					Button button = new Button(getApplicationContext());
					button.setTextSize(17);
					button.setEnabled(true);
					button.setText(arg0.getTitle() + "\n");
					// 创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
					InfoWindow mInfoWindow = new InfoWindow(button, pt, -47);
					// 显示InfoWindow
					mBaiduMap.showInfoWindow(mInfoWindow);
				}

				return false;
			}
		});
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
		mBaiduMap.setTrafficEnabled(false);
		tv_near = findViewById(R.id.tv_home_title);
		getAroundSomething();

		findViewById(R.id.menu_view).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	public void getAroundSomething() {

		mBaiduMap.clear();
		mBDLocation = mLocationUtil.getmLocation();

		if (mBDLocation != null) {
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(mBDLocation.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(100).latitude(mBDLocation.getLatitude())
					.longitude(mBDLocation.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			mPoint = new LatLng(mBDLocation.getLatitude(),
					mBDLocation.getLongitude());

			// 开启定位图层
			mBaiduMap.setMyLocationEnabled(true);

			MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(mPoint,
					17.0f);
			mBaiduMap.animateMapStatus(u);
		}
		mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
				LocationMode.NORMAL, true, null));

		mPoiSearch = PoiSearch.newInstance();
		OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener() {
			@Override
			public void onGetPoiResult(PoiResult result) {
				// 获取POI检索结果
				if (result == null
						|| result.error != PoiResult.ERRORNO.NO_ERROR) {
					Toast.makeText(SearchSomethingActivity.this.getApplicationContext(), "抱歉，未找到结果",
							Toast.LENGTH_SHORT).show();
				}
				if (result.error == PoiResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
					// 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
					// result.getSuggestAddrInfo()
					return;
				}
				if (result.error == PoiResult.ERRORNO.NO_ERROR) {
					PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);
					mBaiduMap.setOnMarkerClickListener(overlay);
					overlay.setData(result);
					overlay.addToMap();
				}
			}

			@Override
			public void onGetPoiDetailResult(PoiDetailResult result) {
				// TODO Auto-generated method stub
				if (result.error != PoiDetailResult.ERRORNO.NO_ERROR) {
					Toast.makeText(SearchSomethingActivity.this.getApplicationContext(), "抱歉，未找到结果",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(SearchSomethingActivity.this.getApplicationContext(),
							result.getName() + ": " + result.getAddress(),
							Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

			}
		};

		Intent intent = getIntent();
		Keyword = intent.getStringExtra("Keyword");
		if (Keyword.length() == 0) {
			Keyword = "学校";
		}

		tv_near.setText("附近的"+Keyword);

		mPoiSearch.setOnGetPoiSearchResultListener(poiListener);
		if (mPoint != null) {
			PoiNearbySearchOption option = (new PoiNearbySearchOption())
					.keyword(Keyword).location(mPoint).radius(1000)
					.pageCapacity(20).pageNum(1);
			mPoiSearch.searchNearby(option);
			MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(mPoint);
			mBaiduMap.animateMapStatus(u);
		}

	}

	private class MyPoiOverlay extends PoiOverlay {

		public MyPoiOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public boolean onPoiClick(int index) {
			super.onPoiClick(index);
			PoiInfo poi = getPoiResult().getAllPoi().get(index);
			// if (poi.hasCaterDetails) {
			mPoiSearch.searchPoiDetail((new PoiDetailSearchOption())
					.poiUid(poi.uid));
			// }
			return true;
		}
	}

	public void onResume() {
		super.onResume();

		mMapView.onResume();

	}

	public void onPause() {
		super.onPause();
		mMapView.onPause();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}

}
