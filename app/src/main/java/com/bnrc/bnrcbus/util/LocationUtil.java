package com.bnrc.bnrcbus.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.bnrc.bnrcbus.application.BnrcApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationUtil extends BnrcApplication {
	private static final String TAG = LocationUtil.class.getSimpleName();
	private static LocationUtil mInstance = null;
	private static LocationClient mLocationClient;
	private static LocationClientOption mLocOption = new LocationClientOption();
	private MyLocationListener mMyLocationListener = new MyLocationListener();
	private static SharedPreferenceUtil mSharePrefrenceUtil;
	private String mLocationResult;
	private BDLocation mLocation = null;
	private Context mContext;
	private String mProvider;
	private String mDirection = "0.0";
	private NetAndGpsUtil mNetAndGpsUtil;
	private static final int DEFAULT_TIME_INTERVAL = 3 * 1000;
	private static TelephonyManager telephonyManager;
	private String deviceID = "";
	private List<Map<String, Object>> postMessage = new ArrayList<Map<String, Object>>();
	private static boolean isIntelligentSwitchGPS = false;

	public List<Map<String, Object>> getPostMessage() {
		return postMessage;
	}

	public void setPostMessage(List<Map<String, Object>> postMessage) {
		this.postMessage = postMessage;
	}

	private double oldLatitude = 0.0;
	private double oldLongitude = 0.0;
	private long timeStamp;

	@SuppressLint("MissingPermission")
	public String getDeviceID() {
		if (deviceID.length() == 0)
			deviceID = telephonyManager.getDeviceId();
		Log.i(TAG, "deviceID： " + deviceID);
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public static LocationUtil getInstance(Context ctx) {
		if (mInstance == null) {
			SDKInitializer.initialize(ctx.getApplicationContext());
			mInstance = new LocationUtil(ctx.getApplicationContext());
		}
		if (!mInstance.mLocationClient.isStarted()) {
			mInstance.mLocationClient.start();
		}
		if (mInstance.mLocationClient != null
				&& mInstance.mLocationClient.isStarted()) {
			mInstance.mLocationClient.requestLocation();
		}
		return mInstance;
	}

	public LocationUtil() {
	}

	@RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
//		CrashReport
//				.initCrashReport(getApplicationContext(), "900023807", false);
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
			@Override
			public void onActivityCreated(Activity activity,
					Bundle savedInstanceState) {
				Log.i(TAG, "onActivityCreated");
			}

			@Override
			public void onActivityStarted(Activity activity) {
				activityCount++;
				Log.i(TAG, "onActivityStarted");
				if (isIntelligentSwitchGPS && mLocOption != null
						&& mLocationClient != null) {
					mLocOption.setLocationMode(LocationMode.Hight_Accuracy);
					mLocOption.setOpenGps(true);
					mLocationClient.setLocOption(mLocOption);

				}
			}

			@Override
			public void onActivityResumed(Activity activity) {
				Log.i(TAG, "onActivityResumed");

			}

			@Override
			public void onActivityPaused(Activity activity) {
				Log.i(TAG, "onActivityPaused");

			}

			@Override
			public void onActivityStopped(Activity activity) {
				Log.i(TAG, "onActivityStopped");

				activityCount--;
				if (0 == activityCount) {
					isForeground = false;
					Log.i(TAG, "onActivityStopped: isForeground: "
							+ isForeground + " isIntelligentSwitchGPS "
							+ isIntelligentSwitchGPS);
					if (isIntelligentSwitchGPS && mLocOption != null
							&& mLocationClient != null) {
						Log.i(TAG, "onActivityStopped: isForeground: "
								+ "here");
						mLocOption.setLocationMode(LocationMode.Battery_Saving);
						mLocOption.setOpenGps(false);
						mLocationClient.setLocOption(mLocOption);
					}
				}
			}

			@Override
			public void onActivityDestroyed(Activity activity) {
				Log.i(TAG, "onActivityDestroyed");
				// TODO Auto-generated method stub

			}

			@Override
			public void onActivitySaveInstanceState(Activity activity,
					Bundle outState) {
				// TODO Auto-generated method stub

			}

		});
	}

	public LocationUtil(Context context) {
		mContext = context;
		mLocationClient = new LocationClient(context.getApplicationContext());
		mSharePrefrenceUtil = SharedPreferenceUtil.getInstance(context
				.getApplicationContext());
		mNetAndGpsUtil = NetAndGpsUtil.getInstance(context);
		initLocation();
		startLocation();
	}

	private void changeBatteryOption() {
		Log.i(TAG, "changeBatteryOption()");
		isIntelligentSwitchGPS = false;
		String battery = mSharePrefrenceUtil.getValue("battery", "智能切换");
		if (mNetAndGpsUtil.isGpsEnable()) {
			if (battery.equalsIgnoreCase("1级(比较损耗)")) {
				Log.i(TAG, battery);
				mLocOption.setLocationMode(LocationMode.Hight_Accuracy);
				mLocOption.setOpenGps(true);
			} else if (battery.equalsIgnoreCase("2级(推荐)")) {
				Log.i(TAG, battery);
				mLocOption.setLocationMode(LocationMode.Device_Sensors);
				mLocOption.setOpenGps(true);
			} else if (battery.equalsIgnoreCase("3级(损耗很少)")) {
				Log.i(TAG, battery);
				mLocOption.setLocationMode(LocationMode.Battery_Saving);
				mLocOption.setOpenGps(false);
			} else {
				Log.i(TAG, "isIntelligentSwitchGPS: " + isIntelligentSwitchGPS);
				isIntelligentSwitchGPS = true;
				mLocOption.setLocationMode(LocationMode.Hight_Accuracy);
				mLocOption.setOpenGps(true);
			}
		} else {
			Log.i(TAG, "2级(推荐)");
			Log.i(TAG, "isIntelligentSwitchGPS: " + isIntelligentSwitchGPS);
			mLocOption.setLocationMode(LocationMode.Hight_Accuracy);
			mLocOption.setOpenGps(true);
			isIntelligentSwitchGPS = true;
		}
		mLocationClient.setLocOption(mLocOption);
	}

	/**
	 * ʵ��ʵλ�ص�����
	 */
	public class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {

			if (location == null)
				return;
			mLocation = location;
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("Latitude", mLocation.getLatitude());
			map.put("Longitude", mLocation.getLongitude());
			map.put("TimeInterval", System.currentTimeMillis() / 1000);
			if (location.getLocType() == BDLocation.TypeGpsLocation) {
				map.put("Speed", location.getSpeed());

			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {

				LatLng oldOne = new LatLng(oldLatitude, oldLongitude);
				LatLng newOne = new LatLng(location.getLatitude(),
						location.getLongitude());
				map.put("Speed", getDistanceWithLocations(oldOne, newOne)
						* 1000 / (System.currentTimeMillis() - timeStamp));
			}
			mDirection = gps2d(location.getLatitude(), location.getLongitude(),
					oldLatitude, oldLongitude);
			if (mDirection.equals("NaN"))
				mDirection = "0.0";
			map.put("Amizuth", mDirection);
			oldLatitude = location.getLatitude();
			oldLongitude = location.getLongitude();
			timeStamp = System.currentTimeMillis();
			Log.i(TAG, map.toString()+" map");
			postMessage.add(map);
		}
	}

	public BDLocation getmLocation() {
		// Log.i(TAG, "mLocation==null " + (mLocation == null));
		return mLocation;
	}

	public void setmLocation(BDLocation mLocation) {
		this.mLocation = mLocation;
	}

	public LocationClientOption getmLocOption() {
		return mLocOption;
	}

	public void setmLocOption(LocationClientOption mLocOption) {
		this.mLocOption = mLocOption;
	}

	public void initLocation() {
		changeBatteryOption();
		mLocOption.setCoorType("bd09ll");
		mLocOption.setScanSpan(DEFAULT_TIME_INTERVAL);
		mLocOption.setIsNeedAddress(true);
		mLocOption.setNeedDeviceDirect(true);
		mLocOption.setLocationNotify(true);
		mLocationClient.setLocOption(mLocOption);
		mLocationClient.registerLocationListener(mMyLocationListener);

	}

	public void startLocation() {
		mLocationClient.start();
		mLocationClient.requestLocation();
	}

	public double getDistanceWithLocation(LatLng location) {
		Log.i(TAG, "getDistanceWithLocation: mLocation==null"
				+ (mLocation == null));
		Log.i(TAG, "getDistanceWithLocation: location==null"
				+ (location == null));
		if (mLocation != null)
			Log.i(TAG, "getDistanceWithLocation: mLocation.loc==null"
					+ (mLocation.getLatitude()));
		if (mLocation == null || location == null)
			return Double.MAX_VALUE;
		double distance = DistanceUtil.getDistance(
				new LatLng(mLocation.getLatitude(), mLocation.getLongitude()),
				location);
		Log.i(TAG, "getDistanceWithLocation: distance" + distance);
		return distance;
	}

	public double getDistanceWithLocations(LatLng locationa, LatLng locationb) {
		if (locationa == null || locationb == null)
			return Double.MAX_VALUE;
		return DistanceUtil.getDistance(locationa, locationb);
	}

	private String gps2d(double lat_a, double lng_a, double lat_b, double lng_b) {
		// Log.i(TAG, lat_a+" "+lng_a+" "+lat_b+" "+lng_b);
		double d = 0;
		lat_a = lat_a * Math.PI / 180;
		lng_a = lng_a * Math.PI / 180;
		lat_b = lat_b * Math.PI / 180;
		lng_b = lng_b * Math.PI / 180;
		try {
			d = Math.sin(lat_a) * Math.sin(lat_b) + Math.cos(lat_a)
					* Math.cos(lat_b) * Math.cos(lng_b - lng_a);
			d = Math.sqrt(1 - d * d);
			d = Math.cos(lat_b) * Math.sin(lng_b - lng_a) / d;
			d = Math.asin(d) * 180 / Math.PI;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			d = 0;
		} catch (ArithmeticException e) {
			e.printStackTrace();
			d = 0;
		}

		Log.i(TAG, d + " d");

		return d + "";
	}

	public void stopLocation() {
		// InitLocation();
		mLocationClient.stop();
		mLocationClient.unRegisterLocationListener(mMyLocationListener);
	}

	private int activityCount;// activity的count数
	private boolean isForeground;// 是否在前台

}
