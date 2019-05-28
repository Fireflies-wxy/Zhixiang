package com.bnrc.busapp.ui.pullloadmenulistview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.BaseExpandableListAdapter;

import com.bnrc.busapp.ui.expandablelistview.SwipeMenuCreator;
import com.bnrc.busapp.ui.expandablelistview.SwipeMenuExpandableListView;

import java.util.ArrayList;

/**
 * Created by frank on 16/1/3.
 */
public class PullLoadMenuListView extends
		PullRefreshLayout<SwipeMenuExpandableListView> {

	public final static int MUSIC = 0;
	public final static int ALBUM = 1;
	// View mLoading, mToLoad, mLoadRoot;
	boolean mLoadMoreEnable;
	boolean mIsLoadingMore = false;
	// private bnrc.com.load.OverScrollFooterLayout mFooterView = new
	// DefaultFooterView(getContext());
	// private OverScrollFooterLayout mFooterView = new
	// DefaultFooterView(getContext());
	// private PullLoadMoreListener mListener;
	private PullRefreshListener mListener;
	private ArrayList<OnFocusIndexChangedListener> mFocusListeners = new ArrayList<OnFocusIndexChangedListener>();
	private OnItemClickListener mOnItemClickListener;
	public SwipeMenuExpandableListView listView;

	// private SwipeMenuExpandableListView.OnPullToLoadMoreReadyListener
	// onPullToLoadMoreReadyListener
	// = new SwipeMenuExpandableListView.OnPullToLoadMoreReadyListener() {
	// @Override
	// public void onReady() {
	// startLoadMore();
	// }
	// };

	public PullLoadMenuListView(Context context) {
		super(context);
		init();
	}

	public PullLoadMenuListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		// mContentView.addOnCentralPositionChangedListener(new
		// FocusedListView.OnCentralPositionChangedListener() {
		// @Override
		// public void onCentralPositionChanged(int var1) {
		// for (OnFocusIndexChangedListener l : mFocusListeners) {
		// l.onFocusIndexChanged(var1);
		// }
		// }
		// });
		// mContentView.setExtraFooterHeight(mFooterView.getViewHeight());
	}

	// public void setAdapter(RecyclerView.Adapter adapter) {
	// mContentView.setAdapter(new PullToLoadMoreAdapter(getContext(),
	// adapter));
	// }

	// public void setAdapter(SwipeMenuExpandableAdapter adapter) {
	// // mContentView.setAdapter(new PullToLoadMoreAdapter(getContext(),
	// // adapter));
	// }
	public void setAdapter(BaseExpandableListAdapter adapter) {
		mContentView.setAdapter(adapter);
	}

	// public void addOnFocusIndexChangedListener(OnFocusIndexChangedListener
	// listener) {
	// if (listener != null && !mFocusListeners.contains(listener)) {
	// mFocusListeners.add(listener);
	// }
	// }
	//
	// public void removeOnFocusIndexChangedListener(OnFocusIndexChangedListener
	// listener) {
	// if (listener != null && mFocusListeners.contains(listener)) {
	// mFocusListeners.remove(listener);
	// }
	// }

	public void setOnItemClickListener(OnItemClickListener listener) {
		mOnItemClickListener = listener;
	}

	@Override
	protected SwipeMenuExpandableListView createContentView(Context context,
															AttributeSet attrs) {
		listView = new SwipeMenuExpandableListView(context, attrs);
		/**
		 * 手动设置id，否则可能 java.lang.IllegalArgumentException: Wrong state class,
		 * expecting View State but received class
		 * android.support.v7.widget.RecyclerView$SavedState instead. This
		 * usually happens when two views of different type have the same id in
		 * the same hierarchy.
		 */
		listView.setId(android.R.id.list);
		return listView;
	}

	@Override
	protected boolean isReadyForPullStart() {
		Log.d("",
				"isReadyForPullStart " + mContentView.getFirstVisiblePosition());
		try {
			if (mContentView.getCount() == 0) {
				// 没有item的时候也可以下拉刷新
				return true;
			} else if (mContentView.getFirstVisiblePosition() == 0
					&& mContentView.getChildAt(0).getTop() >= 0) {
				// 滑到顶部了
				return true;
			} else
				return false;
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}

		// return mContentView.getFirstVisiblePosition() <= 0
		// && mContentView.getChildAt(0).getTop() >= 0;
	}

	// @Override
	// public void startLoadMore() {
	// if (!mLoadMoreEnable || mIsLoadingMore) {
	// return;
	// }
	//
	// mIsLoadingMore = true;
	//
	// if (mFooterView != null) {
	// mFooterView.onLoadingMore();
	// }
	//
	// if (mListener != null) {
	// mListener.onLoadMore();
	// }
	//
	// }
	//
	// @Override
	// public void stopLoadMore() {
	// if (mFooterView != null) {
	// mFooterView.onPullToLoadMore();
	// }
	// mIsLoadingMore = false;
	// }
	//
	// @Override
	// public void setPullLoadMoreListener(PullLoadMoreListener listener) {
	// mListener = listener;
	// }
	//
	// public void setPullLoadMoreEnable(boolean enable) {
	// mLoadMoreEnable = enable;
	// if (enable) {
	// mContentView.setOnPullToLoadMoreReadyListener(onPullToLoadMoreReadyListener);
	// } else {
	// mContentView.setOnPullToLoadMoreReadyListener(null);
	// }
	// }

	// public void focusToNext(boolean withAnimation) {
	// mContentView.focusToNext(withAnimation);
	// }
	//
	// public void focusToPrev(boolean withAnimation) {
	// mContentView.focusToPrev(withAnimation);
	// }

	@Override
	public void setPullRefreshListener(PullRefreshListener listener) {
		super.setPullRefreshListener(listener);
	}

	@Override
	public void startRefresh() {
		super.startRefresh();
	}

	@Override
	public void stopRefresh() {
		super.stopRefresh();
	}

	public void autoRefresh() {
	}

	public void setMenuCreator(SwipeMenuCreator menuCreator) {
		listView.setMenuCreator(menuCreator);
	}

	public void setOnMenuItemClickListener(
			SwipeMenuExpandableListView.OnMenuItemClickListener menuItemClickListener) {

		listView.setOnMenuItemClickListener(menuItemClickListener);
	}

	public void setOnGroupExpandListener(
			SwipeMenuExpandableListView.OnGroupExpandListener onGroupExpandListener) {
		listView.setOnGroupExpandListener(onGroupExpandListener);
	}

	public void setOnChildClickListener(
			SwipeMenuExpandableListView.OnChildClickListener onChildExpandListener) {
		listView.setOnChildClickListener(onChildExpandListener);
	}



	public void setSelectedGroup(int pos) {
		listView.setSelectedGroup(pos);
	}

	public void collapseGroup(int lastGroupPos) {
		listView.collapseGroup(lastGroupPos);
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void expandGroup(int groupPos, boolean animate) {
		listView.expandGroup(groupPos, animate);
	}

	public void expandGroup(int groupPos) {
		listView.expandGroup(groupPos);
	}

	public void scrollToPosition(int offset) {
		mContentView.setSelection(offset);
		// mContentView.scrollToPosition(offset);
	}

	public void scrollToPosition2(int offset) {
		mContentView.setSelection(offset);
		// mContentView.scroolToPostion2(0);
		// mContentView.scroolToPostion2(offset);
	}

	public void setSelection(int index) {

		scrollToPosition2(index);
	}

	public interface OnItemClickListener {
		void onItemClick(int position);
	}

	public interface OnFocusIndexChangedListener {
		void onFocusIndexChanged(int index);
	}

	// private class PullToLoadMoreAdapter extends
	// RecyclerView.Adapter<FocusedListView.ViewHolder> {
	//
	// static final int ITEM_TYPE_NORMAL = 1;
	// static final int ITEM_TYPE_FOOTER = 2;
	//
	// Context mContext;
	// RecyclerView.Adapter<FocusedListView.ViewHolder> mAdapter;
	//
	//
	// public PullToLoadMoreAdapter(Context context,
	// RecyclerView.Adapter<FocusedListView.ViewHolder> adapter) {
	// this.mContext = context;
	// this.mAdapter = adapter;
	// }
	//
	// @Override
	// public void setHasStableIds(boolean hasStableIds) {
	// mAdapter.setHasStableIds(hasStableIds);
	// }
	//
	// @Override
	// public long getItemId(int position) {
	// return mAdapter.getItemId(position);
	// }
	//
	// @Override
	// public void onViewRecycled(FocusedListView.ViewHolder holder) {
	// mAdapter.onViewRecycled(holder);
	// }
	//
	// @Override
	// public void onViewAttachedToWindow(FocusedListView.ViewHolder holder) {
	// mAdapter.onViewAttachedToWindow(holder);
	// }
	//
	// @Override
	// public void onViewDetachedFromWindow(FocusedListView.ViewHolder holder) {
	// mAdapter.onViewDetachedFromWindow(holder);
	// }
	//
	// @Override
	// public void registerAdapterDataObserver(RecyclerView.AdapterDataObserver
	// observer) {
	// mAdapter.registerAdapterDataObserver(observer);
	// }
	//
	// @Override
	// public void
	// unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver observer)
	// {
	// mAdapter.unregisterAdapterDataObserver(observer);
	// }
	//
	// @Override
	// public void onAttachedToRecyclerView(RecyclerView recyclerView) {
	// mAdapter.onAttachedToRecyclerView(recyclerView);
	// }
	//
	// @Override
	// public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
	// mAdapter.onDetachedFromRecyclerView(recyclerView);
	// }
	//
	// @Override
	// public int getItemViewType(int position) {
	//
	// if (position < mAdapter.getItemCount()) {
	// return ITEM_TYPE_NORMAL;
	// }
	//
	// return ITEM_TYPE_FOOTER;
	//
	// }
	//
	// @Override
	// public FocusedListView.ViewHolder onCreateViewHolder(ViewGroup parent,
	// int viewType) {
	// if (viewType == ITEM_TYPE_NORMAL) {
	// return mAdapter.onCreateViewHolder(parent, viewType);
	// }
	//
	// View v = mFooterView;
	//
	// return new FocusedListView.ViewHolder(v);
	// }
	//
	// @Override
	// public void onBindViewHolder(FocusedListView.ViewHolder holder, final int
	// position) {
	//
	// if (ITEM_TYPE_NORMAL == getItemViewType(position)) {
	//
	// holder.itemView.setOnClickListener(new OnClickListener() {
	// @Override
	// public void onClick(View v) {
	// if (mOnItemClickListener != null) {
	// mOnItemClickListener.onItemClick(position);
	// }
	// }
	// });
	// mAdapter.onBindViewHolder(holder, position);
	// return;
	// }
	//
	// // mLoadRoot = holder.itemView;
	// // mLoading = holder.itemView.findViewById(R.id.footer_loading);
	// // mToLoad = holder.itemView.findViewById(R.id.footer_to_load);
	//
	// if (mLoadMoreEnable) {
	// holder.itemView.setVisibility(View.VISIBLE);
	// holder.itemView.setOnClickListener(new OnClickListener() {
	// @Override
	// public void onClick(View v) {
	// startLoadMore();
	// }
	// });
	// } else {
	// holder.itemView.setVisibility(View.INVISIBLE);
	// }
	//
	// return;
	// }
	//
	// @Override
	// public int getItemCount() {
	// if (mAdapter == null) {
	// return 0;
	// }
	//
	// return mAdapter.getItemCount() + 1;
	// }
	// }
	//
	// public void scrollReset() {
	// mContentView.scroolReset();
	// }
}
