package com.bnrc.bnrcbus.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.bnrc.bnrcbus.R;

import com.bnrc.bnrcbus.database.PCUserDataDBHelper;
import com.bnrc.bnrcbus.model.Child;
import com.bnrc.bnrcbus.util.LocationUtil;
import com.bnrc.bnrcbus.util.SharedPreferenceUtil;
import com.bnrc.bnrcbus.view.activity.HomeActivity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class AlertService extends Service {
	private static final String TAG = AlertService.class.getSimpleName();
	private AlertBinder mBinder = new AlertBinder();
	private TimerTask mTask;
	private Timer mTimer;
	private long TIMEINTERVAL = 20 * 1000;
	private int mAlertDistance = 500;
	private SharedPreferenceUtil mSharePrefrenceUtil;
	private LocationUtil mLocationUtil;
	private BDLocation mBdLocation;
	private PCUserDataDBHelper mUserDataDBHelper;
	private List<Child> children;
	private Notification mNotification;
	private NotificationManager mManager;
	private Set<String> keySet;
	private Handler mHandler = new Handler();
	private double minAlertScale = Double.MAX_VALUE;
	private Child mNearChild;
	private String mNearID;
	private Vibrator mVibrator;
	private String alertStationName = "";
	private onAlertListener mListener;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		initNotifiManager();
		keySet = new HashSet<String>();
		mUserDataDBHelper = PCUserDataDBHelper.getInstance(this);
		mLocationUtil = LocationUtil.getInstance(AlertService.this);
		mLocationUtil.startLocation();
		mBdLocation = mLocationUtil.getmLocation();
		mSharePrefrenceUtil = SharedPreferenceUtil
				.getInstance(getApplicationContext());
		mVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
	}

	private void openAlertTimertask() {
		mTask = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				scanScale();
			}
		};
		mTimer = new Timer(true);
		mTimer.schedule(mTask, 0, TIMEINTERVAL);
	}

	private void cancelAlertTimertask() {
		if (mTask != null)
			mTask.cancel();
		if (mTimer != null)
			mTimer.cancel();
		if (mVibrator != null)
			mVibrator.cancel();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mListener = null;
		cancelAlertTimertask();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

	private void scanScale() {
		mBdLocation = mLocationUtil.getmLocation();
		if (mBdLocation == null)
			return;
		String value = mSharePrefrenceUtil.getValue("alertRadius", "500米");
		mAlertDistance = Integer
				.parseInt(value.substring(0, value.indexOf("米")));
		LatLng myPoint = new LatLng(mBdLocation.getLatitude(),
				mBdLocation.getLongitude());
		children = mUserDataDBHelper.acquireAlertLineWithLocation(myPoint);
		for (Child child : children) {
			if (child == null)
				continue;
			double Latitude = child.getLatitude();
			double Longitude = child.getLongitude();
			LatLng stationPoint = new LatLng(Latitude, Longitude);
			double distance = DistanceUtil.getDistance(myPoint, stationPoint);
			// Log.i(TAG, "myPoint: " + myPoint + " ; stationPoint: "
			// + stationPoint);
			Log.i(TAG, "distance: " + distance);
			int LineID = child.getLineID();
			int StationID = child.getStationID();
			int AZ = child.getAZ();
			String ID = calculateID(LineID, StationID, AZ);
			if (child.isAlertOpen() == Child.OPEN && distance < mAlertDistance
					&& !keySet.contains(ID)) {
				if (distance < minAlertScale) {
					minAlertScale = distance;
					mNearChild = child;
					mNearID = ID;
				}
			}
			Log.i(TAG, "keySet: " + keySet);
		}
		if (mNearChild != null && mNearID != null && mNearID.length() > 0) {
			alert(mNearChild.getStationName(), (int) minAlertScale);
			keySet.add(mNearID);
			mNearID = null;
			mNearChild = null;
			minAlertScale = Double.MAX_VALUE;
		}
	}

	private void initNotifiManager() {
		mManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNotification = new Notification();
		int icon = R.drawable.ic_launcher;
		mNotification.icon = icon;
		mNotification.tickerText = "下车提醒";
		mNotification.defaults |= Notification.DEFAULT_SOUND;
		mNotification.defaults |= Notification.DEFAULT_VIBRATE;
		mNotification.flags = Notification.FLAG_AUTO_CANCEL;
	}

	private void alert(String stationName, int distance) {
		mListener.onAlert();
		alertStationName = stationName;
		openTimer();
		Log.i(TAG, "alert: " + stationName);
		mNotification.when = System.currentTimeMillis();
		// Navigator to the new activity when click the notification title
		Intent intent = new Intent(this, HomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				intent,PendingIntent.FLAG_CANCEL_CURRENT);
//
//		mNotification.setLatestEventInfo(this,
//				getResources().getString(R.string.app_name), "即将到达  "
//						+ stationName + "站  距离您" + distance + "米",
//				pendingIntent);

		mManager.notify(0, mNotification);
		mVibrator.vibrate(new long[] { 500, 1000, 500, 1000, 500, 1000 }, -1);// 振动提醒已到设定位置附近
	}

	private void openTimer() {
		mHandler.removeCallbacks(alertCancelRunnable);
		mHandler.postDelayed(alertCancelRunnable, 60 * 1000);
	}

	private String calculateID(int LineID, int StationID, int AZ) {
		return LineID + "" + StationID + "" + AZ;
	}

	private String getServiceAlertStationName() {
		return alertStationName;
	}

	private void stopServiceAlert() {
		mManager.cancel(0);
		if (mVibrator != null)
			mVibrator.cancel();
	}

	private Runnable alertCancelRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			mListener.onCancelAlert();
			mManager.cancel(0);
			mVibrator.cancel();
		}
	};

	public class AlertBinder extends Binder {

		public void startScanAlert() {
			Log.d(TAG, "startScanAlert() executed");
			openAlertTimertask();// 执行具体的任务
		}

		public String getAlertStationName() {
			return getServiceAlertStationName();
		}

		public void stopAlert() {
			stopServiceAlert();
		}

		public void setListener(onAlertListener listener) {
			mListener = listener;
		}
	}

	public interface onAlertListener {
		void onAlert();

		void onCancelAlert();
	}

}