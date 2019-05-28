package com.bnrc.busapp.util.collectwifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class WifiAdmin {

	public static WifiAdmin mWifiadminInstance = null;
	// 定义�?个WifiManager对象
	private WifiManager mWifiManager;
	// 定义�?个WifiInfo对象
	private WifiInfo mWifiInfo;
	// 扫描出的网络连接列表
	private List<ScanResult> mWifiList;
	// 网络连接列表
	private List<WifiConfiguration> mWifiConfigurations;
	WifiLock mWifiLock;
	// 扫描到的公交Wifi列表
	private List<ScanResult> mWifiMac;

	public WifiAdmin(Context context) {
		// 取得WifiManager对象
		mWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		// 取得WifiInfo对象
		mWifiInfo = mWifiManager.getConnectionInfo();
		mWifiMac = new ArrayList<ScanResult>();
	}

	public static WifiAdmin getInstance(Context context) {
		if (mWifiadminInstance == null) {
			mWifiadminInstance = new WifiAdmin(context);

		}
		return mWifiadminInstance;
	}

	// 扫描网络
	public List<ScanResult> getScanWifiMac(String ssid) {
		// �?始扫描网�?
		mWifiMac.clear();
		startScan();
		ScanResult mScanResult = null;
		if (mWifiList != null) {
			for (int i = 0; i < mWifiList.size(); i++) {
				// 得到扫描结果
				mScanResult = mWifiList.get(i);
				if (mScanResult.SSID.equalsIgnoreCase(ssid)) {
					Log.i("SSID", ssid);
					mWifiMac.add(mScanResult);
				}

			}
			// scanWifi.setText("扫描到的wifi网络：\n" + sb.toString());
		}
		return mWifiMac;
	}

	// 打开wifi
	public void openWifi() {
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
	}

	// 关闭wifi
	public void closeWifi() {
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		}
	}

	// �?查当前wifi状�??
	public int checkState() {
		return mWifiManager.getWifiState();
	}

	// 锁定wifiLock
	public void acquireWifiLock() {
		mWifiLock.acquire();
	}

	// 解锁wifiLock
	public void releaseWifiLock() {
		// 判断是否锁定
		if (mWifiLock.isHeld()) {
			mWifiLock.acquire();
		}
	}

	// 创建�?个wifiLock
	public void createWifiLock() {
		mWifiLock = mWifiManager.createWifiLock("test");
	}

	// 得到配置好的网络
	public List<WifiConfiguration> getConfiguration() {
		return mWifiConfigurations;
	}

	// 指定配置好的网络进行连接
	public void connetionConfiguration(int index) {
		if (index > mWifiConfigurations.size()) {
			return;
		}
		// 连接配置好指定ID的网�?
		mWifiManager.enableNetwork(mWifiConfigurations.get(index).networkId,
				true);
	}

	public void startScan() {
		mWifiManager.startScan();
		// 得到扫描结果
		mWifiList = mWifiManager.getScanResults();
		// 得到配置好的网络连接
		mWifiConfigurations = mWifiManager.getConfiguredNetworks();
	}

	// 得到网络列表
	public List<ScanResult> getWifiList() {
		return mWifiList;
	}

	// 查看扫描结果
	public StringBuffer lookUpScan() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < mWifiList.size(); i++) {
			sb.append("Index_" + new Integer(i + 1).toString() + ":");
			// 将ScanResult信息转换成一个字符串�?
			// 其中把包括：BSSID、SSID、capabilities、frequency、level
			sb.append((mWifiList.get(i)).toString()).append("\n");
		}
		return sb;
	}

	public String getMacAddress() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
	}

	public String getBSSID() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
	}

	public int getIpAddress() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
	}

	// 得到连接的ID
	public int getNetWordId() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
	}

	// 得到wifiInfo的所有信�?
	public String getWifiInfo() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
	}

	// 添加�?个网络并连接
	public void addNetWork(WifiConfiguration configuration) {
		int wcgId = mWifiManager.addNetwork(configuration);
		mWifiManager.enableNetwork(wcgId, true);
	}

	// 断开指定ID的网�?
	public void disConnectionWifi(int netId) {
		mWifiManager.disableNetwork(netId);
		mWifiManager.disconnect();
	}
}
