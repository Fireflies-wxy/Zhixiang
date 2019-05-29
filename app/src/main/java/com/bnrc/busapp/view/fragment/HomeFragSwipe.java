package com.bnrc.busapp.view.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;
import com.bnrc.busapp.R;
import com.bnrc.busapp.adapter.CollectAdapter;
import com.bnrc.busapp.adapter.NearAdapter;
import com.bnrc.busapp.constant.Constants;
import com.bnrc.busapp.database.PCUserDataDBHelper;
import com.bnrc.busapp.listener.GetLocationListener;
import com.bnrc.busapp.listener.IPopWindowListener;
import com.bnrc.busapp.model.Child;
import com.bnrc.busapp.model.Group;
import com.bnrc.busapp.network.MyVolley;
import com.bnrc.busapp.presenter.RtPresenter;
import com.bnrc.busapp.presenter.RtPresenterImpl;
import com.bnrc.busapp.ui.expandablelistview.SwipeMenu;
import com.bnrc.busapp.ui.expandablelistview.SwipeMenuCreator;
import com.bnrc.busapp.ui.expandablelistview.SwipeMenuExpandableListView;
import com.bnrc.busapp.ui.expandablelistview.SwipeMenuItem;
import com.bnrc.busapp.ui.pullloadmenulistview.IPullRefresh;
import com.bnrc.busapp.ui.pullloadmenulistview.PullLoadMenuListView;
import com.bnrc.busapp.util.AnimationUtil;
import com.bnrc.busapp.util.LocationUtil;
import com.bnrc.busapp.view.activity.BuslineListActivity;

import java.util.ArrayList;
import java.util.List;

public class HomeFragSwipe extends BaseFragment {
	private static final String TAG = HomeFragSwipe.class.getSimpleName();
	private PullLoadMenuListView mHomeConcernExplistview;
	private CollectAdapter mHomeConcernAdapter;
	private RelativeLayout mHomeConcernHint;
	private List<Group> mGroups;
	private Context mContext;
	private PCUserDataDBHelper mUserDataDBHelper;
	public LocationUtil mLocationUtil = null;
	public BDLocation mBDLocation = null;
	private DownloadTask mTask;
	private Handler mHandler = new Handler();
	private IPopWindowListener mOnSelectBtn;
	private RtPresenter mRtPresenter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mOnSelectBtn.showLoading();
		mLocationUtil = LocationUtil.getInstance(mContext
				.getApplicationContext());
		mLocationUtil.startLocation(new GetLocationListener() {
			@Override
			public void onGetLocation() {
				mOnSelectBtn.dismissLoading();
			}
		});
	}

	private SwipeMenuExpandableListView.OnGroupExpandListener mOnGroupExpandListener = new SwipeMenuExpandableListView.OnGroupExpandListener() {
		int lastGroupPos = 0;

		@Override
		public void onGroupExpand(int pos) {
			if (mHomeConcernAdapter.getChildrenCount(pos) > 0) {
				// if (lastGroupPos != pos) {
				// mHomeConcernExplistview.collapseGroup(lastGroupPos);
				// lastGroupPos = pos;
				// }
				// mHomeConcernExplistview.setSelectedGroup(pos);
			}
		}
	};

	private SwipeMenuExpandableListView.OnChildClickListener mOnChildExpandListener = new SwipeMenuExpandableListView.OnChildClickListener() {

		@Override
		public boolean onChildClick(ExpandableListView paramExpandableListView,
                                    View paramView, int paramInt1, int paramInt2, long paramLong) {
			// TODO Auto-generated method stub
			Group group = mGroups.get(paramInt1);
			Child child = group.getChildItem(paramInt2);
			Intent intent = new Intent(mContext, BuslineListActivity.class);
			intent.putExtra("LineID", child.getLineID());
			intent.putExtra("StationID", child.getStationID());
			intent.putExtra("FullName", child.getLineFullName());
			intent.putExtra("Sequence", child.getSequence());
			intent.putExtra("lineStatus", child.getLineStatus());
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(intent);
			AnimationUtil.activityZoomAnimation((Activity) mContext);
			return false;
		}

	};

	private SwipeMenuCreator mMenuCreator = new SwipeMenuCreator() {
		@Override
		public void create(SwipeMenu menu) {
			switch (menu.getViewType()) {
			case NearAdapter.FAV:
				SwipeMenuItem item1 = new SwipeMenuItem(getActivity());
				// item1.setBackground(getResources().getColor(R.color.blue));
				item1.setBackground(R.drawable.bg_circle_drawable_notstar);
				// item1.setIcon(R.drawable.select_star);
				item1.setWidth(220);
				item1.setTitleColor(getResources().getColor(R.color.white));
				item1.setTitleSize(50);
				item1.setTitle("修改");
				menu.addMenuItem(item1);
				break;
			case NearAdapter.NORMAL:
				SwipeMenuItem item2 = new SwipeMenuItem(getActivity());
				// item1.setBackground(getResources().getColor(R.color.colorPrimaryDark));
				item2.setBackground(R.drawable.bg_circle_drawable);
				// item2.setIcon(R.drawable.not_select_star);
				item2.setWidth(220);
				item2.setTitleColor(getResources().getColor(R.color.white));
				item2.setTitleSize(50);
				item2.setTitle("收藏");
				menu.addMenuItem(item2);
				break;
			}
		}
	};

	private SwipeMenuExpandableListView.OnMenuItemClickListener mMenuItemClickListener = new SwipeMenuExpandableListView.OnMenuItemClickListener() {
		@Override
		public boolean onMenuItemClick(int groupPosition, int childPosition,
									   SwipeMenu menu, int index) {

			if (groupPosition < mGroups.size() && groupPosition >= 0) {
				Group group = mGroups.get(groupPosition);
				List<Child> children = group.getChildren();
				if (childPosition < children.size() && childPosition >= 0) {
					Child child = children.get(childPosition);
					mOnSelectBtn.onPopClick(child);
				}
			}
			return false;
		}
	};

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_home_frag_swipe,
				(ViewGroup) getActivity().findViewById(R.id.content), false);

        mContext = getActivity();

		mRtPresenter = RtPresenterImpl.newInstance(mContext,mHandler);

		mHomeConcernExplistview = (PullLoadMenuListView) view
				.findViewById(R.id.explistview_home_concern);
		mHomeConcernHint = (RelativeLayout) view
				.findViewById(R.id.rLayout_home_concern);
		mUserDataDBHelper = PCUserDataDBHelper.getInstance(mContext
				.getApplicationContext());
		mGroups = new ArrayList<Group>();
		mHomeConcernExplistview.setMenuCreator(mMenuCreator);
		mHomeConcernAdapter = new CollectAdapter(mGroups, mContext,
				mHomeConcernExplistview.listView, mOnSelectBtn);
		mHomeConcernExplistview.setAdapter(mHomeConcernAdapter);
		// mHomeConcernExplistview
		// .setOnMenuItemClickListener(mMenuItemClickListener);
		mHomeConcernExplistview
				.setOnGroupExpandListener(mOnGroupExpandListener);
		// mHomeConcernExplistview.setOnChildClickListener(mOnChildExpandListener);
		mHomeConcernExplistview.setPullToRefreshEnable(true);
		mHomeConcernExplistview
				.setPullRefreshListener(new IPullRefresh.PullRefreshListener() {

					@Override
					public void onRefresh() {
						// TODO Auto-generated method stub
						MyVolley.sharedVolley(mContext.getApplicationContext())
								.reStart();
						pullToRefresh();
					}
				});
		mBDLocation = mLocationUtil.getmLocation();
		return view;
	}

	public List<Group> getFavStationsAndBuslines() {
		// startTimer();
		if (mBDLocation != null) {
			LatLng point = new LatLng(mBDLocation.getLatitude(),
					mBDLocation.getLongitude());
			mGroups = mUserDataDBHelper.acquireFavInfoWithLocation(point,
					Constants.TYPE_HOME);
		} else
			mGroups = mUserDataDBHelper.acquireFavInfoWithLocation(null,
					Constants.TYPE_HOME);
		Log.i(TAG, "getFavStationsAndBuslines： " + mGroups.size());

		return mGroups;
	}

	private void pullToRefresh() {
		Log.i("testToast", "HomeFragSwip: pullToRefresh");
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				String position = "暂时没有定位信息";
				if (mBDLocation != null) {
					String addr = mBDLocation.getAddrStr();
					if (addr != null && addr.length() > 0)
						position = addr;
				}
				Toast.makeText(mContext.getApplicationContext(), position, Toast.LENGTH_SHORT).show();
				mHomeConcernExplistview.stopRefresh();
			}
		}, 3000);
		// getRtParam(mNearGroups);
		mRtPresenter.getServerInfo(mGroups,mHomeConcernAdapter);

	}

	@Override
	public void refreshConcern() {
		mRtPresenter.getServerInfo(mGroups,mHomeConcernAdapter);
	}

	@Override
	public void refresh() {
		Log.i("refresh", "refreshed in HomeFragSwipe");
		if(mContext==null||mUserDataDBHelper==null){
			return;
		}
		loadDataBase();
	}

	private void loadDataBase() {
		if (mTask != null)
			mTask.cancel(true);
		mTask = new DownloadTask(getActivity());
		mTask.execute();

	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mContext = (Context) getActivity();
		mOnSelectBtn = (IPopWindowListener) activity;
		Log.i(TAG, TAG + " onAttach");

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i(TAG, TAG + " onDestroy");

	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		Log.i(TAG, TAG + " onDestroyView");

	}

	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		Log.i(TAG, TAG + " onDetach");
		mHandler.removeCallbacksAndMessages(null);
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.i(TAG, TAG + " onPause");

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		loadDataBase();
		Log.i(TAG, TAG + " onResume");
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.i(TAG, TAG + " onStart");
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.i(TAG, TAG + " onStop");
		mHandler.removeCallbacksAndMessages(null);

	}

	class DownloadTask extends AsyncTask<Integer, Integer, List<Group>> {
		// 后面尖括号内分别是参数（线程休息时间），进度(publishProgress用到)，返回值 类型

		private Context mContext = null;

		public DownloadTask(Context context) {
			Log.i("testContext", "Home : context == null? : "+(context==null));
			this.mContext = context;
		}

		/*
		 * 第一个执行的方法 执行时机：在执行实际的后台操作前，被UI 线程调用
		 * 作用：可以在该方法中做一些准备工作，如在界面上显示一个进度条，或者一些控件的实例化，这个方法可以不用实现。
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			Log.d(TAG, "onPreExecute");
			super.onPreExecute();
		}

		/*
		 * 执行时机：在onPreExecute 方法执行后马上执行，该方法运行在后台线程中 作用：主要负责执行那些很耗时的后台处理工作。可以调用
		 * publishProgress方法来更新实时的任务进度。该方法是抽象方法，子类必须实现。
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected List<Group> doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			Log.d(TAG, "doInBackground");
			// for (int i = 0; i <= 100; i++) {
			// mProgressBar.setProgress(i);
			publishProgress();

			// try {
			// Thread.sleep(params[0]);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// }
			return getFavStationsAndBuslines();
		}

		/*
		 * 执行时机：这个函数在doInBackground调用publishProgress时被调用后，UI
		 * 线程将调用这个方法.虽然此方法只有一个参数,但此参数是一个数组，可以用values[i]来调用
		 * 作用：在界面上展示任务的进展情况，例如通过一个进度条进行展示。此实例中，该方法会被执行100次
		 * 
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onProgressUpdate");
			// mTextView.setText(values[0] + "%");
			mOnSelectBtn.showLoading();
			super.onProgressUpdate(values);
		}

		/*
		 * 执行时机：在doInBackground 执行完成后，将被UI 线程调用 作用：后台的计算结果将通过该方法传递到UI
		 * 线程，并且在界面上展示给用户 result:上面doInBackground执行后的返回值，所以这里是"执行完毕"
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(List<Group> result) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onPostExecute");
			mOnSelectBtn.dismissLoading();
			if (result != null && result.size() > 0) {
				mGroups = result;
				mHomeConcernAdapter.updateData(mGroups);
				mHomeConcernAdapter.notifyDataSetChanged();
				mHomeConcernHint.setVisibility(View.GONE);
				mHomeConcernExplistview.setVisibility(View.VISIBLE);
				int groupCount = result.size();
				for (int i = 0; i < groupCount; i++)
					mHomeConcernExplistview.expandGroup(i);
				// getRtParam(mGroups);
				mRtPresenter.getServerInfo(mGroups,mHomeConcernAdapter);
			} else {
				mHomeConcernHint.setVisibility(View.VISIBLE);
				mHomeConcernExplistview.setVisibility(View.GONE);

			}
		}

	}

}
