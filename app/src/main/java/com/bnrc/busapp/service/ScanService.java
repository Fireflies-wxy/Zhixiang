package com.bnrc.busapp.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.IBinder;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.bnrc.busapp.constant.Constants;
import com.bnrc.busapp.util.LocationUtil;
import com.bnrc.busapp.util.SharedPreferenceUtil;
import com.bnrc.busapp.util.collectwifi.CollectWifiDBHelper;
import com.bnrc.busapp.util.collectwifi.WifiAdmin;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ScanService extends Service {
	private static final String TAG = "ScanService";

	private CollectWifiDBHelper mCollectWifiDBHelperInstance = null;
	private List<ScanResult> wifiScanResults;
	private LocationUtil mLocationUtil;
	private WifiAdmin mWifiAdminInstance;
	private Map<String, Object> mScanWifiInfo;
	private List<String> wifiInfo;
	private List<String> delWifiList;
	private List<Map<String, Object>> delCompareList;
	private String macOnBus = "";
	private long curDate;
	private String wifiSSID = "";
	private BDLocation mBDLocation;
	private List<Map<String, Object>> compareTable;// 筛选无线网列表
	private SharedPreferenceUtil mSharePrefrenceUtil;

	@Override
	public IBinder onBind(Intent paramIntent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreate");
		// formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// formatter_compare = new SimpleDateFormat("HH:mm:ss");
		mCollectWifiDBHelperInstance = CollectWifiDBHelper
				.getInstance(getApplicationContext());
		mLocationUtil = LocationUtil.getInstance(getApplicationContext());
		mBDLocation = mLocationUtil.getmLocation();
		mWifiAdminInstance = WifiAdmin.getInstance(getApplicationContext());
		mScanWifiInfo = new HashMap<String, Object>();
		wifiInfo = new ArrayList<String>();
		delWifiList = new ArrayList<String>();
		delCompareList = new ArrayList<Map<String, Object>>();
		compareTable = new ArrayList<Map<String, Object>>();
		mSharePrefrenceUtil = SharedPreferenceUtil.getInstance(this
				.getApplicationContext());
		wifiSSID = mSharePrefrenceUtil.getValue(Constants.SETTING_AP,
				"BNRC-Air");
	}

	@SuppressLint("WrongConstant")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "scanservice_onStartCommand");
		// new PollingThread().start();
		scanProcess();
		flags = START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * Polling thread 模拟向Server轮询的异步线程
	 * 
	 * @Author Ryan
	 * @Create 2013-7-13 上午10:18:34
	 */

	private void scanProcess() {
		wifiSSID = mSharePrefrenceUtil.getValue(Constants.SETTING_AP,
				"BNRC-Air");
		wifiScanResults = mWifiAdminInstance.getScanWifiMac(wifiSSID.trim());
		wifiInfo.clear();
		macOnBus = null;
		mBDLocation = mLocationUtil.getmLocation();
		Log.i(TAG, "SSID: " + wifiScanResults.toString() + "; wifiSSid: "
				+ wifiSSID);
		// Toast.makeText(getApplicationContext(), nowTime, 1000).show();
		if (wifiScanResults.size() != 0) {
			for (ScanResult scanResult : wifiScanResults) {
				Log.i(TAG, "insert is coming!\n");
				if (mScanWifiInfo == null)
					mScanWifiInfo = new HashMap<String, Object>();
				mScanWifiInfo.clear();
				curDate = System.currentTimeMillis();
				mScanWifiInfo.put("时间", curDate);
				mScanWifiInfo.put("纬度", mBDLocation.getLatitude());
				mScanWifiInfo.put("经度", mBDLocation.getLongitude());
				mScanWifiInfo.put("SSID", wifiSSID);
				mScanWifiInfo.put("MAC", scanResult.BSSID);
				mScanWifiInfo.put("Level", scanResult.level);
				wifiInfo.add(scanResult.BSSID);
				Log.i(TAG,
						"mBDLocation.getLocType(): " + mBDLocation.getLocType());
				if (mBDLocation.getLocType() == 61
						|| mBDLocation.getLocType() == 161) {
					Log.i(TAG, "61或161 到这里了！");

					// curDate = new Date(System.currentTimeMillis());// 获取当前时间
					// nowTime = formatter.format(curDate);

					Log.i(TAG, "insertInfo: " + mScanWifiInfo.toString());
					mScanWifiInfo.put("LocType",
							getLocType(mBDLocation.getLocType()));
					mScanWifiInfo.put("LocRadius", mBDLocation.getRadius()
							+ "m");
					mScanWifiInfo.put(
							"LocSpeed",
							mBDLocation.hasSpeed() == true ? (int) mBDLocation
									.getSpeed() + "m/s" : "仅支持GPS测速");
				} else if (mBDLocation.getLocType() == 68) {
					Log.i(TAG, "68 到这里了！");

					mScanWifiInfo.put("LocType",
							getLocType(mBDLocation.getLocType()));
					mScanWifiInfo.put("LocRadius", mBDLocation.getRadius()
							+ "m");
					mScanWifiInfo.put("LocSpeed", "仅支持GPS测速");
				} else {
					Log.i(TAG, "到这里了");
					mScanWifiInfo.put("LocType",
							getLocType(mBDLocation.getLocType()));
					mScanWifiInfo.put("LocRadius", "请打开GPS或网络");
					mScanWifiInfo.put("LocSpeed", "仅支持GPS测速");
				}
				mCollectWifiDBHelperInstance.InsertScanData(mScanWifiInfo);
				try {
					macOnBus = checkBusMac(wifiInfo);
					if (macOnBus != null) {
						Log.i("CHECK",
								macOnBus
										+ " "
										+ mCollectWifiDBHelperInstance
												.HasSureData(macOnBus));
						if (!mCollectWifiDBHelperInstance.HasSureData(macOnBus)) {
							Intent intent = new Intent(Constants.UPDATE_ACTION);
							intent.setAction(Constants.UPDATE_ACTION);
							intent.putExtra("update", macOnBus + ";" + wifiSSID);
							sendBroadcast(intent);
							Log.i(TAG, "ScanService broad");
						}
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.i("RUNNABLE", e.toString() + "\n");
				}
			}

		}
	}

	// }

	private String getLocType(int type) {

		String LocType = "";
		switch (type) {
		case 61:
			LocType = "GPS定位";
			break;
		case 161:
			LocType = "网络定位";
			break;
		case 68:
			LocType = "本地离线定位结果";
			break;
		default:
			LocType = "定位失败";
			break;
		}
		return LocType;

	}

	private String checkBusMac(List<String> wifiInfo) throws ParseException {
		String macOnBus = "";// 判断为所坐公交车上的mac
		int numBusMac = 0;// 判断所坐公交车是否唯一
		// curDate = new Date(System.currentTimeMillis());// 获取当前时间
		curDate = System.currentTimeMillis();
		delWifiList.clear();
		delCompareList.clear();
		Iterator it = wifiInfo.iterator();
		while (it.hasNext()) {
			String mac = (String) it.next();
			Log.i("CHECKBUS", "scanMac " + mac + "\n");
			for (Map<String, Object> existMap : compareTable) {
				if (existMap.get("MAC").equals(mac)) {
					existMap.put("RecentTime", curDate);
					existMap.put(
							"Long",
							timeLong((Long) existMap.get("RecentTime"),
									(Long) existMap.get("StartTime")));
					// it.remove();
					delWifiList.add(mac);
					break;
				}
			}

		}
		wifiInfo.removeAll(delWifiList);
		for (String mac : wifiInfo) {
			Map<String, Object> newRecord = new HashMap<String, Object>();
			newRecord.put("MAC", mac);
			newRecord.put("StartTime", curDate);
			newRecord.put("RecentTime", curDate);
			newRecord.put("Long", timeLong((Long) curDate, (Long) curDate));
			compareTable.add(newRecord);
		}
		it = compareTable.iterator();
		while (it.hasNext()) {
			Map<String, Object> existMap = (Map) it.next();
			Log.i("CHECKBUS", existMap.toString() + "\n");
			if (compareTable.size() == 1
					&& (Long) compareTable.get(0).get("Long") > 60) {
				numBusMac++;
				macOnBus = String.valueOf(compareTable.get(0).get("MAC"));
				break;
			}
			if (timeLong(curDate, (Long) existMap.get("RecentTime")) < 30
					&& (Long) existMap.get("Long") > 300) {
				macOnBus = String.valueOf(existMap.get("MAC"));
				numBusMac++;
			}
			if (timeLong(curDate, (Long) existMap.get("RecentTime")) > 60)
				// compareTable.remove(existMap);
				delCompareList.add(existMap);
		}
		compareTable.removeAll(delCompareList);
		Log.i(TAG, "compareTable: \n");
		for (Map<String, Object> map : compareTable) {
			Log.i(TAG, "******************\n");
			Log.i(TAG,
					String.valueOf(map.get("MAC")) + "; "
							+ String.valueOf(map.get("StartTime")) + "; "
							+ String.valueOf(map.get("RecentTime")) + "; "
							+ String.valueOf(map.get("Long")));
		}
		if (numBusMac == 1)
			return macOnBus;
		else
			return null;

	}

	private long timeLong(long nowTime, long startTime) throws ParseException {

		return (nowTime - startTime) / 1000;
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "scanService_onDestroy");
		super.onDestroy();

	}

}
