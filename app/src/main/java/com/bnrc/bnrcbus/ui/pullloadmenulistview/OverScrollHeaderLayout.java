package com.bnrc.bnrcbus.ui.pullloadmenulistview;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public abstract class OverScrollHeaderLayout extends FrameLayout implements IPullRefreshLayout {
	protected Context mContext;
	protected View mContentView;
	protected int mViewHeight;

	public OverScrollHeaderLayout(Context context) {
		super(context);
		initView(context);
	}

	public OverScrollHeaderLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	protected final void initView(Context context) {
		mContext = context;
		mContentView = getContentView(context);
		addView(mContentView);
		mContentView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		mViewHeight = mContentView.getMeasuredHeight();
		hide();
	}

	protected View getContentView(Context context) {
		LinearLayout content = new LinearLayout(context);
		content.setOrientation(LinearLayout.VERTICAL);
		content.setBackgroundColor(Color.TRANSPARENT);
		return content;
	}

	public int getViewHeight() {
		Log.d("", "RJZ header getviewheight " + mViewHeight);

		return mViewHeight;
	}

	public void hide() {
		setVisibility(View.INVISIBLE);
	}

	public void show() {
		setVisibility(View.VISIBLE);
	}

}
