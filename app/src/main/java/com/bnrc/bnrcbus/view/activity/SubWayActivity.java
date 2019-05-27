package com.bnrc.bnrcbus.view.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.bnrc.bnrcbus.R;
import com.bnrc.bnrcbus.util.NetAndGpsUtil;
import com.bnrc.bnrcbus.view.activity.base.BaseActivity;


public class SubWayActivity extends BaseActivity {
	private static final String TAG = SubWayActivity.class.getSimpleName();
	private WebView mWebview;
	private WebSettings settings;
	private NetAndGpsUtil mNetAndGpsUtil;
	private Handler mHandler = new Handler();
	private static final String subwayUrl = "http://www.bjsubway.com/subway/images/subway_map.jpg";
	private static final String subwayAssets = "file:///android_asset/subway_map.png";
	private static final String baidu = "http://www.baidu.com";
	private static final String map = "http://ii.911cha.com/ditie/beijing.png";
	private static final String error = "file:///android_asset/error.html";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_subway);
		mNetAndGpsUtil = NetAndGpsUtil.getInstance(getApplicationContext());
		findViewById(R.id.menu_view).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		openWeb();
	}

	private void openWeb() {
		mWebview = (WebView) findViewById(R.id.webView);
		mWebview.setWebViewClient(new MyWebViewClient());
		if (mNetAndGpsUtil.isNetworkAvailable())
			openUrl(map);
		else
			mWebview.loadUrl(error);

	}

	public void openUrl(String url) {
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		int width = wm.getDefaultDisplay().getWidth();
		if (width > 650) {
			this.mWebview.setInitialScale(190);
		} else if (width > 520) {
			this.mWebview.setInitialScale(160);
		} else if (width > 450) {
			this.mWebview.setInitialScale(140);
		} else if (width > 300) {
			this.mWebview.setInitialScale(120);
		} else {
			this.mWebview.setInitialScale(100);
		}
		settings = mWebview.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		settings.setBuiltInZoomControls(true);
		settings.setDisplayZoomControls(false);
		settings.setSupportZoom(true);
		settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		settings.setLoadWithOverviewMode(true);
		// settings.setDomStorageEnabled(true);// 扩大比例的缩放
		settings.setUseWideViewPort(true);
		mWebview.loadUrl(url);
	}

	private class MyWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			// view.loadUrl(url);
			return true;
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
			// TODO Auto-generated method stub
			super.onReceivedError(view, errorCode, description, failingUrl);
			dismissLoading();
			mWebview.loadUrl(error);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			// TODO Auto-generated method stub
			super.onPageStarted(view, url, favicon);
			Log.i(TAG, "onPageStarted");
			showLoading();

		}

		@Override
		public void onPageFinished(WebView view, String url) {
			// TODO Auto-generated method stub
			super.onPageFinished(view, url);
			dismissLoading();
			Log.i(TAG, "onPageFinished");

		}

	}

	@Override
	protected void onStop() {
		super.onStop();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mHandler.removeCallbacksAndMessages(null);
		if (mWebview != null) {
			// settings.setBuiltInZoomControls(true);
			// mWebview.setVisibility(View.GONE);
			mWebview.stopLoading();
			mWebview.removeAllViews();
			mWebview.destroy();
			mWebview = null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onPause() {
		super.onPause();

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}
}
