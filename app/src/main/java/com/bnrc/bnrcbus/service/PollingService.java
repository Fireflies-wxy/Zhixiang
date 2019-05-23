package com.bnrc.bnrcbus.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import com.bnrc.bnrcbus.R;
import com.bnrc.bnrcbus.database.UserDataDBHelper;
import com.bnrc.bnrcbus.util.LocationUtil;


public class PollingService extends Service {
	private final String TAG = PollingService.class.getSimpleName();
	public static final String ACTION = "com.bnrc.busapp.PollingService";
	public static boolean hasAlert;
	public static boolean hasKnown;
	public static String stationNameString = "";
	public static int alertR = 0;
	public static double preDistance;
	public UserDataDBHelper userdabase = null;
	public static LocationUtil locationer;

	private Notification mNotification;
	private NotificationManager mManager;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		initNotifiManager();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		new PollingThread().start();
		userdabase = UserDataDBHelper.getInstance(getApplicationContext());
		locationer = LocationUtil.getInstance(PollingService.this);

		SharedPreferences mySharedPreferences = getApplicationContext()
				.getSharedPreferences("setting", Context.MODE_PRIVATE);
		String alertRMode = mySharedPreferences.getString("alertRMode", "200米");
		alertR = Integer.parseInt(alertRMode.subSequence(0,
				alertRMode.length() - 1).toString());

	}

	private void initNotifiManager() {
		mManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		int icon = R.drawable.ic_launcher;
		mNotification = new Notification();
		mNotification.icon = icon;
		mNotification.tickerText = "下车提醒";
		mNotification.defaults |= Notification.DEFAULT_SOUND;
		mNotification.flags = Notification.FLAG_AUTO_CANCEL;
	}


	/**
	 * Polling thread ģ����Server��ѯ���첽�߳�
	 * 
	 * @Author Ryan
	 * @Create 2013-7-13 ����10:18:34
	 */
	static int count = 0;

	class PollingThread extends Thread {
		@Override
		public void run() {

		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		System.out.println("Service:onDestroy");
	}

}
