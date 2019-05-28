package com.bnrc.busapp.network;

import android.app.ActivityManager;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * MyVolley.java
 *
 * @author Fireflies
 * 
 */
public class MyVolley {
	private static final String TAG = "MyVolley";

	private static MyVolley instance;
	private static RequestQueue mRequestQueue;
	private final static int RATE = 4; // Ĭ�Ϸ������ռ�ļ���֮һ

	private MyVolley(Context context) {
		mRequestQueue = Volley.newRequestQueue(context);

		// ȷ����LruCache�У����仺��ռ��С,Ĭ�ϳ���������ռ�� 1/8
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
	}

	/**
	 * ��ʼ��Volley��ض�����ʹ��VolleyǰӦ����ɳ�ʼ��
	 * 
	 * @param context
	 */
	public static MyVolley sharedVolley(Context context) {
		if (instance == null) {
			instance = new MyVolley(context);
		}
		return instance;
	}

	/**
	 * �õ�������ж���
	 * 
	 * @return
	 */
	public RequestQueue getRequestQueue() {
		throwIfNotInit();
		return mRequestQueue;
	}

	public void addRequest(Request<?> request) {
		getRequestQueue().add(request);
	}

	public void stop() {
		mRequestQueue.stop();
		mRequestQueue.getCache().clear();
	}

	public void start() {
		mRequestQueue.start();
	}

	public void reStart() {
		stop();
		start();
	}

	/**
	 * ����Ƿ���ɳ�ʼ��
	 */
	private static void throwIfNotInit() {
		if (instance == null) {// ��δ��ʼ��
			throw new IllegalStateException(
					"MyVolley��δ��ʼ������ʹ��ǰӦ��ִ��init()");
		}
	}
}