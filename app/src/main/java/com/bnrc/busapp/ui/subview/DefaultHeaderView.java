package com.bnrc.busapp.ui.subview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.bnrc.busapp.R;
import com.bnrc.busapp.ui.pullloadmenulistview.OverScrollHeaderLayout;


/**
 * Created by frank on 16/1/1.
 */
public class DefaultHeaderView extends OverScrollHeaderLayout {

	private RotateLoading mRotateLoading;

	public DefaultHeaderView(Context context) {
		super(context);
	}

	public DefaultHeaderView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected View getContentView(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.default_pull_refresh_header, null);
		mRotateLoading = (RotateLoading) view.findViewById(R.id.default_pull_refresh_anim);
		return view;
	}

	@Override
	final public int getRefreshingPosition() {
		return -mViewHeight;
	}

	@Override
	final public int getInitialPosition() {
		return 0;
	}

	@Override
	public void onRefreshing() {
		Log.d("", "RJZ onRefreshing");
		if (!mRotateLoading.isStart()) {
			mRotateLoading.start();
		}
	}

	@Override
	public void onPullToRefresh() {
		Log.d("", "RJZ onPullToRefresh");
		if (mRotateLoading.isStart()) {
			mRotateLoading.stop();
		}
		mRotateLoading.invalidate();
	}

	@Override
	public void onReleaseToRefresh() {
		Log.d("", "RJZ onReleaseToRefresh");

		if (!mRotateLoading.isStart()) {
			mRotateLoading.start();
		}
	}

	@Override
	public void onInit() {
		Log.d("", "RJZ onInit");
		mRotateLoading.stop();
	}
}
