package com.bnrc.busapp.ui.mapoverlay;

import android.content.Context;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.bnrc.busapp.view.activity.BuslineMapActivity;

import org.json.JSONException;

public class StationOverlay extends BusLineOverlay {

	public MapView mMapView;
	public BaiduMap mBaiduMap = null;
	public Context mCtx;

	public StationOverlay(BaiduMap arg0, Context context) {
		super(arg0);

		// TODO Auto-generated constructor stub
		mBaiduMap = arg0;
		mCtx = context;
	}

	// @Override
	public boolean onBusStationClick(int index) {
		try {
			((BuslineMapActivity) mCtx).showStationView(index);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}
