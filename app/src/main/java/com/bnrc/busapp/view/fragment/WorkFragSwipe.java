package com.bnrc.busapp.view.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
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
import com.baidu.mapapi.utils.CoordinateConverter;
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
import com.bnrc.busapp.network.VolleyNetwork;
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
import com.bnrc.busapp.util.MyCipher;
import com.bnrc.busapp.util.NetAndGpsUtil;
import com.bnrc.busapp.view.activity.BuslineListActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WorkFragSwipe extends BaseFragment {
	private static final String TAG = WorkFragSwipe.class.getSimpleName();
	private PullLoadMenuListView mWorkConcernExplistview;
	private CollectAdapter mWorkConcernAdapter;
	private RelativeLayout mWorkConcernHint;
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
			if (mWorkConcernAdapter.getChildrenCount(pos) > 0) {
				// if (lastGroupPos != pos) {
				// mWorkConcernExplistview.collapseGroup(lastGroupPos);
				// lastGroupPos = pos;
				// }
				// mWorkConcernExplistview.setSelectedGroup(pos);
			}
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

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_work_frag_swipe,
				(ViewGroup) getActivity().findViewById(R.id.content), false);
		mContext = getActivity();

		mRtPresenter = RtPresenterImpl.newInstance(mContext,mHandler);

		mWorkConcernExplistview = (PullLoadMenuListView) view
				.findViewById(R.id.explistview_work_concern);
		mWorkConcernHint = (RelativeLayout) view
				.findViewById(R.id.rLayout_work_concern);
		mUserDataDBHelper = PCUserDataDBHelper.getInstance(mContext
				.getApplicationContext());
		mGroups = new ArrayList<Group>();
		mWorkConcernExplistview.setMenuCreator(mMenuCreator);
		mWorkConcernAdapter = new CollectAdapter(mGroups, mContext,
				mWorkConcernExplistview.listView, mOnSelectBtn);
		mWorkConcernExplistview.setAdapter(mWorkConcernAdapter);
		// mWorkConcernExplistview
		// .setOnMenuItemClickListener(mMenuItemClickListener);
		mWorkConcernExplistview
				.setOnGroupExpandListener(mOnGroupExpandListener);
		// mWorkConcernExplistview.setOnChildClickListener(mOnChildExpandListener);
		mWorkConcernExplistview.setPullToRefreshEnable(true);
		mWorkConcernExplistview
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
		Log.i(TAG, "getFavStationsAndBuslines");
		// startTimer();
		if (mBDLocation != null) {
			LatLng point = new LatLng(mBDLocation.getLatitude(),
					mBDLocation.getLongitude());
			mGroups = mUserDataDBHelper.acquireFavInfoWithLocation(point,
					Constants.TYPE_WORK);
		} else
			mGroups = mUserDataDBHelper.acquireFavInfoWithLocation(null,
					Constants.TYPE_WORK);
		return mGroups;
	}

	private void pullToRefresh() {
		Log.i("testToast", "WorkFragSwip: pullToRefresh");
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
				mWorkConcernExplistview.stopRefresh();
			}
		}, 3000);
		// getRtParam(mNearGroups);
		mRtPresenter.getServerInfo(mGroups,mWorkConcernAdapter);

	}

	@Override
	public void refreshConcern() {
		mRtPresenter.getServerInfo(mGroups,mWorkConcernAdapter);
	}

	@Override
	public void refresh() {
		Log.i("refresh", "refreshed in WorkFragSwipe");
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
			Log.i("testContext", "Work : context == null? : "+(mContext==null));
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
				mWorkConcernAdapter.updateData(mGroups);
				mWorkConcernAdapter.notifyDataSetChanged();
				mWorkConcernHint.setVisibility(View.GONE);
				mWorkConcernExplistview.setVisibility(View.VISIBLE);
				int groupCount = result.size();
				for (int i = 0; i < groupCount; i++)
					mWorkConcernExplistview.expandGroup(i);
				// getRtParam(mGroups);
				mRtPresenter.getServerInfo(mGroups,mWorkConcernAdapter);
			} else {
				mWorkConcernHint.setVisibility(View.VISIBLE);
				mWorkConcernExplistview.setVisibility(View.GONE);

			}
			// swipeRefreshLayout.setRefreshing(false);
		}

	}

}
