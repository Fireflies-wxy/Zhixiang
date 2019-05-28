package com.bnrc.busapp.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

import com.bnrc.busapp.constant.Constants;

public class ServiceUtils {

	// 开启轮询服务
	private static boolean isPollingStarted = false;

	public static void startPollingService(Context context, Class<?> cls,
                                           String action) {
		// 获取AlarmManager系统服务
		AlarmManager manager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		// 包装需要执行Service的Intent
		Intent intent = new Intent(context, cls);
		intent.setAction(action);
		PendingIntent pendingIntent = PendingIntent.getService(context, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// 触发服务的起始时间
		long triggerAtTime = SystemClock.elapsedRealtime();
		SharedPreferences preferences = context.getSharedPreferences(
				Constants.SETTING, context.MODE_PRIVATE);
		String seconds = preferences.getString(Constants.SETTING_FRE, "5秒");
		seconds = seconds.substring(0, seconds.indexOf("秒"));
		// 使用AlarmManger的setRepeating方法设置定期执行的时间间隔（seconds秒）和需要执行的Service
		manager.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtTime,
				Integer.parseInt(seconds) * 1000, pendingIntent);
		isPollingStarted = true;
	}

	// 停止轮询服务
	public static void stopPollingService(Context context, Class<?> cls,
                                          String action) {
		AlarmManager manager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, cls);
		intent.setAction(action);
		PendingIntent pendingIntent = PendingIntent.getService(context, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		// 取消正在执行的服务
		manager.cancel(pendingIntent);
		isPollingStarted = false;
	}

	public static boolean isPollingStart() {
		return isPollingStarted;
	}

}
