package com.bnrc.busapp.ui.pullloadmenulistview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import com.bnrc.busapp.ui.subview.DefaultHeaderView;
import com.bnrc.busapp.util.DensityUtil;


/**
 * Created by frank on 15/12/26.
 */
public abstract class PullRefreshLayout<T extends View> extends LinearLayout
		implements IPullRefresh {
	private static final String TAG = "PullRefreshLayout";
	protected final static int STATE_RELEASE_TO_REFRESH = 0;
	protected final static int STATE_PULL_TO_REFRESH = 1;
	protected final static int STATE_REFRESHING = 2;
	protected final static int STATE_IDLE = 3;
	protected T mContentView;
	protected boolean mPullToRefreshEnable = true;
	private int mState = STATE_IDLE;
	private OverScrollHeaderLayout mHeaderLayout;
	private boolean mIsDraging = false;
	private int mTouchSlop;
	private int mMaxHeight = 0;
	private int mFactor = 0;

	private PullRefreshListener mListener;
	private int mLastX;
	private int mLastY;
	private int mInitialY;

	public PullRefreshLayout(Context context) {
		super(context);
		initView(context, null);
	}

	public PullRefreshLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context, attrs);
	}

	public PullRefreshLayout(Context context, AttributeSet attrs,
                             int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView(context, attrs);
	}

	@Override
	public void addView(View child, int index, ViewGroup.LayoutParams params) {
		if (mContentView instanceof ViewGroup) {
			((ViewGroup) mContentView).addView(child, index, params);
		}
	}

	protected void initView(Context context, AttributeSet attrs) {
		mMaxHeight = DensityUtil.dip2px(context, 150);
		mFactor = DensityUtil.dip2px(context, 150);

		mTouchSlop = (int) (ViewConfiguration.get(context).getScaledTouchSlop());

		setOrientation(LinearLayout.VERTICAL);
		setGravity(Gravity.CENTER);

		mContentView = createContentView(context, attrs);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		params.weight = 1;
		super.addView(mContentView, -1, params);
		setRefreshHeaderView(new DefaultHeaderView(getContext()));
	}

	protected void initView(Context context, int type, AttributeSet attrs) {
		mMaxHeight = DensityUtil.dip2px(context, 150);
		mFactor = DensityUtil.dip2px(context, 150);

		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

		setOrientation(LinearLayout.VERTICAL);
		setGravity(Gravity.CENTER);

		mContentView = createContentView(context, attrs);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		params.weight = 1;
		super.addView(mContentView, -1, params);
		setRefreshHeaderView(new DefaultHeaderView(getContext()));
	}

	public void setRefreshHeaderView(OverScrollHeaderLayout headerView) {
		mHeaderLayout = headerView;
		int paddingTop = calcHeaderPadding();
		setPadding(0, paddingTop, 0, 0);
	}

	private int calcHeaderPadding() {
		if (mHeaderLayout != null && mPullToRefreshEnable) {
			super.addView(mHeaderLayout, 0, new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

			return -mHeaderLayout.getViewHeight();
		}
		return 0;
	}

	public void setPullToRefreshEnable(boolean enable) {
		this.mPullToRefreshEnable = enable;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (!mPullToRefreshEnable) {
			Log.i(TAG, "!mPullToRefreshEnable 1");
			return false;
		}

		final int action = event.getAction();

		if (action == MotionEvent.ACTION_CANCEL
				|| action == MotionEvent.ACTION_UP) {
			Log.i(TAG,
					"action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP 2");
			mIsDraging = false;
			return false;
		}

		if (mIsDraging && action != MotionEvent.ACTION_DOWN) {
			Log.i(TAG, "mIsDraging && action != MotionEvent.ACTION_DOWN 3");
			return true;
		}

		switch (action) {
			case MotionEvent.ACTION_MOVE: {
				Log.i(TAG, "case MotionEvent.ACTION_MOVE");

				if (isRefreshing()) {
					Log.i(TAG, "isRefreshing()");

					return true;
				}

				// if (isReadyForPullStart()) {
				// Log.i(TAG, "isReadyForPullStart()");
				//
				// final int y = (int) event.getY();
				// final int diff, absDiff;
				//
				// diff = y - mLastY;
				// absDiff = Math.abs(diff);
				// if (Math.abs(event.getX() - mLastX) > mTouchSlop / 5
				// && absDiff < mTouchSlop) {
				// Log.i(TAG,
				// "Math.abs(event.getX() - mLastX) > mTouchSlop / 5");
				// mLastX = (int) event.getX();
				// mIsDraging = false;
				// break;
				// } else if (absDiff > mTouchSlop) {
				// Log.i(TAG, "absDiff > mTouchSlop");
				// setPullDirection(y, diff);
				// } else
				// mIsDraging = false;
				//
				// }

				if (isReadyForPullStart()) {
					Log.i(TAG, "isReadyForPullStart()");

					final int y = (int) event.getY();
					final int diff, absDiff;

					diff = y - mLastY;
					absDiff = Math.abs(diff);
					if (Math.abs(event.getX() - mLastX) > mTouchSlop / 5
							&& diff < mTouchSlop) {
						Log.i(TAG,
								"Math.abs(event.getX() - mLastX) > mTouchSlop / 5");
						mLastX = (int) event.getX();
						mIsDraging = false;
						break;
					} else if (diff > mTouchSlop) {
						Log.i(TAG, "absDiff > mTouchSlop");
						setPullDirection(y, absDiff);
					} else
						mIsDraging = false;

				}
				break;
			}
			case MotionEvent.ACTION_DOWN: {
				Log.i(TAG, "case MotionEvent.ACTION_DOWN");

				if (isReadyForPullStart()) {
					Log.i(TAG, "isReadyForPullStart()");

					mLastY = mInitialY = (int) event.getY();
					mLastX = (int) event.getX();
					mIsDraging = false;
				}
				break;
			}
		}

		return mIsDraging;

	}

	private void setPullDirection(final int y, final int diff) {

		mLastY = 0;
		mIsDraging = false;
		if (diff >= 1f && mPullToRefreshEnable && isReadyForPullStart()) {
			mLastY = y;
			mIsDraging = true;
		}
		mState = STATE_IDLE;
	}

	@Override
	public final boolean onTouchEvent(MotionEvent event) {

		if (!mPullToRefreshEnable) {
			return false;
		}

		if (isRefreshing()) {
			return true;
		}
		// if (event.getAction() == MotionEvent.ACTION_DOWN
		// && event.getEdgeFlags() != 0) {
		// return false;
		// }
		switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE: {
				if (mIsDraging) {
					mLastY = (int) event.getY();
					int diffY = mLastY - mInitialY;
					handlePullEvent(diffY);
					return true;
				}

				break;
			}
			case MotionEvent.ACTION_DOWN: {
				if (isReadyForPullStart()) {
					mLastY = mInitialY = (int) event.getY();
					return true;
				}
				break;
			}
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP: {
				if (mIsDraging) {
					mIsDraging = false;
					finishPullEvent();
					return true;
				}
				break;
			}
		}

		return false;

	}

	private void finishPullEvent() {
		if (mState == STATE_REFRESHING) {
			return;
		}
		if (mState == STATE_PULL_TO_REFRESH) {
			mState = STATE_IDLE;
		}
		if (mState == STATE_RELEASE_TO_REFRESH) {
			mState = STATE_REFRESHING;
		}
		updateState(mState);
	}

	private void handlePullEvent(int diffY) {

		if (isRefreshing()) {
			return;
		}

		int diff = getPowerDiff(Math.abs(diffY), mMaxHeight, mFactor);

		if (diff < mHeaderLayout.getViewHeight()) {
			updateState(STATE_PULL_TO_REFRESH);
		}

		if (mState == STATE_PULL_TO_REFRESH
				&& diff >= mHeaderLayout.getViewHeight()) {
			updateState(STATE_RELEASE_TO_REFRESH);
		}

		scrollTo(0, -diff);
	}

	private void updateState(int state) {
		mState = state;
		switch (mState) {
			case STATE_RELEASE_TO_REFRESH:
				onStateReleaseToRefresh();
				break;
			case STATE_PULL_TO_REFRESH:
				onStatePullToRefresh();
				break;
			case STATE_REFRESHING:
				onStateRefreshing();
				break;
			case STATE_IDLE:
				onStateDone();
				break;
		}
	}

	private void onStateRefreshing() {
		if (mHeaderLayout == null) {
			return;
		}

		smoothScrollTo(this, getScrollY(),
				mHeaderLayout.getRefreshingPosition(),
				new OnSmoothScrollFinishedListener() {

					@Override
					public void onSmoothScrollFinished() {
						onScrollToRefreshing();
					}
				});
	}

	private void onScrollToRefreshing() {
		mHeaderLayout.onRefreshing();
		mListener.onRefresh();
	}

	private void onStatePullToRefresh() {
		if (mHeaderLayout == null) {
			return;
		}
		mHeaderLayout.show();
		mHeaderLayout.onPullToRefresh();
	}

	private void onStateReleaseToRefresh() {
		if (mHeaderLayout == null) {
			return;
		}
		mHeaderLayout.onReleaseToRefresh();
	}

	private void onStateDone() {
		if (mHeaderLayout == null) {
			return;
		}

		smoothScrollTo(this, getScrollY(), mHeaderLayout.getInitialPosition(),
				new OnSmoothScrollFinishedListener() {

					@Override
					public void onSmoothScrollFinished() {
						mHeaderLayout.onInit();
					}
				});
	}

	private boolean isRefreshing() {
		return mState == STATE_REFRESHING;
	}

	protected void smoothScrollTo(final LinearLayout view, final int initY,
                                  final int finalY, final OnSmoothScrollFinishedListener listener) {
		if (initY == finalY) {
			if (listener != null) {
				listener.onSmoothScrollFinished();
			}
			return;
		}

		Animation animation = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime,
											   Transformation t) {
				super.applyTransformation(interpolatedTime, t);
				int scrollY = (int) (initY + (finalY - initY)
						* interpolatedTime);
				view.scrollTo(0, scrollY);
			}

		};
		animation.setDuration(300);
		DecelerateInterpolator inter = new DecelerateInterpolator();
		animation.setInterpolator(inter);

		animation.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (listener != null) {
					listener.onSmoothScrollFinished();
				}
			}
		});
		view.startAnimation(animation);
	}

	protected int getPowerDiff(int diff, int maxHeight, int factor) {
		return diff * maxHeight / (factor + diff);
	}

	abstract protected T createContentView(Context context, AttributeSet attrs);

	abstract protected boolean isReadyForPullStart();

	@Override
	public void setPullRefreshListener(PullRefreshListener listener) {
		mListener = listener;
	}

	@Override
	public void startRefresh() {
		updateState(STATE_REFRESHING);
	}

	@Override
	public void stopRefresh() {
		updateState(STATE_IDLE);
	}

	private interface OnSmoothScrollFinishedListener {
		void onSmoothScrollFinished();
	}
}