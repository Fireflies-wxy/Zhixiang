package com.bnrc.busapp.view.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.SQLException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.bnrc.busapp.R;

import com.bnrc.busapp.adapter.NearAdapter;

import com.bnrc.busapp.database.PCDataBaseHelper;
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
import com.bnrc.busapp.ui.expandablelistview.SwipeMenuItem;
import com.bnrc.busapp.ui.pullloadmenulistview.IPullRefresh;
import com.bnrc.busapp.ui.pullloadmenulistview.PullLoadMenuListView;
import com.bnrc.busapp.util.LocationUtil;
import com.bnrc.busapp.util.MyCipher;
import com.bnrc.busapp.util.NetAndGpsUtil;


import com.bnrc.busapp.network.VolleyNetwork.*;

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


/**
 * Created by apple on 2018/6/4.
 */

public class NearFragment extends BaseFragment{

	private static final String TAG = "NearFragment";

	private PullLoadMenuListView mNearExplistview;
	private NearAdapter mNearAdapter;//adapter
	private RelativeLayout mNearHint;
	private List<Group> mNearGroups;
	private Context mContext;
	private View mContentView;
	public LocationUtil mLocationUtil = null;
	private BDLocation mBDLocation = null;
	private IPopWindowListener mChooseListener;
	private DownloadTask mTask;
	private int mChildrenSize = 0;
	public static boolean isFirstLoad = true;
	private Handler mHandler = new Handler(Looper.getMainLooper());
	private VolleyNetwork mVolleyNetwork;
	private LatLng mOldPoint;
	private NetAndGpsUtil mNetAndGpsUtil;
	private CoordinateConverter mCoordConventer;
	private OkHttpClient mOkHttpClient;

	private RtPresenter mRtPresenter;

	private ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	private SwipeMenuCreator mMenuCreator = new SwipeMenuCreator() {
		@Override
		public void create(SwipeMenu menu) {
			switch (menu.getViewType()) {
				case NearAdapter.FAV:
					SwipeMenuItem item1 = new SwipeMenuItem(getActivity());
					item1.setBackground(R.drawable.bg_circle_drawable_notstar);
					item1.setWidth(220);
					item1.setTitleColor(getResources().getColor(R.color.white));
					item1.setTitleSize(50);
					item1.setTitle("修改");
					menu.addMenuItem(item1);
					break;
				case NearAdapter.NORMAL:
					SwipeMenuItem item2 = new SwipeMenuItem(getActivity());
					item2.setBackground(R.drawable.bg_circle_drawable);
					item2.setWidth(220);
					item2.setTitleColor(getResources().getColor(R.color.white));
					item2.setTitleSize(50);
					item2.setTitle("收藏");
					menu.addMenuItem(item2);
					break;
			}
		}
	};

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		mContentView = inflater.inflate(R.layout.fragment_near,container,false);

		mContext = getActivity();

		mRtPresenter = RtPresenterImpl.newInstance(mContext,mHandler);

		mChooseListener.showLoading();

		mLocationUtil = LocationUtil.getInstance(mContext
				.getApplicationContext());
        if(mLocationUtil==null)
            mLocationUtil.startLocation(new GetLocationListener() {
				@Override
				public void onGetLocation() {
					mChooseListener.dismissLoading();
				}
			});
		mBDLocation = mLocationUtil.getmLocation();

		if (mBDLocation != null)
			mOldPoint = new LatLng(mBDLocation.getLatitude(),
					mBDLocation.getLongitude());

		mVolleyNetwork = VolleyNetwork.getInstance(mContext);

		mNetAndGpsUtil = NetAndGpsUtil.getInstance(mContext
				.getApplicationContext());
		mNearExplistview = (PullLoadMenuListView) mContentView
				.findViewById(R.id.explistview_near);
		mNearHint = (RelativeLayout) mContentView.findViewById(R.id.rLayout_near);
		mNearGroups = new ArrayList<Group>();
		mNearGroups = Collections.synchronizedList(mNearGroups);
		mNearAdapter = new NearAdapter(mNearGroups, mContext,
				mNearExplistview.listView, mChooseListener);
		mNearExplistview.setAdapter(mNearAdapter);
		mNearExplistview.setMenuCreator(mMenuCreator);

		mNearExplistview.setPullToRefreshEnable(true);
		mNearExplistview
				.setPullRefreshListener(new IPullRefresh.PullRefreshListener() {

					@Override
					public void onRefresh() {
						// TODO Auto-generated method stub
						MyVolley.sharedVolley(mContext.getApplicationContext())
								.reStart();
						pullToRefresh();
					}
				});

		Log.i(TAG, TAG + " onCreateView");
		mCoordConventer = new CoordinateConverter();

		loadDataBase();

		return mContentView;
	}

	private void loadDataBase() {
		if (mTask != null)
			mTask.cancel(true);
		mTask = new DownloadTask(getActivity());
		mTask.execute();
	}

	private void pullToRefresh() {   //下拉刷新
		Log.i("testToast", "NearFragSwip: pullToRefresh");
		if (checkPositionChange())   //位置改变之后，重载数据库
			loadDataBase();
		else {
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
					Toast.makeText(mContext.getApplicationContext(), position, Toast.LENGTH_SHORT)
							.show();
					mNearExplistview.stopRefresh();
				}
			}, 3000);
			mRtPresenter.getServerInfo(mNearGroups,mNearAdapter);
		}

	}

	private boolean checkPositionChange() {
		// LatLng newPoint = new LatLng(mBDLocation.getLatitude(),
		// mBDLocation.getLongitude());
		double distance = mLocationUtil.getDistanceWithLocation(mOldPoint);
		if (mBDLocation != null)
			mOldPoint = new LatLng(mBDLocation.getLatitude(),
					mBDLocation.getLongitude());
		Log.i(TAG, "getNearbyStationsAndBuslines " + "mOldPoint " + mOldPoint);
		if (distance > 200)
			return true;
		return false;
	}

	@Override
	public void refresh() {

		if(mNearAdapter==null){
			return;
		}

		mNearAdapter.notifyDataSetChanged();

	} //此行仅对适配器起作用

	// 刷新实时数据
	@Override
	public void refreshConcern() {
		if (this != null && !this.isDetached() && this.isVisible())
			pullToRefresh();
	}

	public List<Group> getNearbyStationsAndBuslines() {
		mBDLocation = mLocationUtil.getmLocation();
		if (mBDLocation != null) {
			LatLng newPoint = new LatLng(mBDLocation.getLatitude(),
					mBDLocation.getLongitude());

			Log.i("mBDLocation: ",mBDLocation.getLatitude()+" "+mBDLocation.getLongitude());
			mNearGroups = PCDataBaseHelper.getInstance(
					mContext.getApplicationContext()).acquireStationAndBusline(  //关键点！
					newPoint);
			mOldPoint = newPoint;
		}
		Log.i(TAG, "getNearbyStationsAndBuslines " + "mChildrenSize: "
				+ mChildrenSize);
		return mNearGroups;
	}


	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mChooseListener = (IPopWindowListener) activity;
		Log.i(TAG, TAG + " onAttach");

	}


	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		// getNearbyStationsAndBuslines();
		Log.i(TAG, TAG + " onStart");

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

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
//		loadDataBase();
		Log.i(TAG, TAG + " onResume");
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.i(TAG, TAG + " onStop");
		if (mTask != null) {
			mTask.cancel(true); // 如果Task还在运行，则先取消它
		}
		mChooseListener.dismissLoading();
		mHandler.removeCallbacksAndMessages(null);
	}

	class DownloadTask extends AsyncTask<Integer, Integer, List<Group>> {
		// 后面尖括号内分别是参数（线程休息时间），进度(publishProgress用到)，返回值 类型

		private Context mContext = null;

		public DownloadTask(Context context) {
			this.mContext = context;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			Log.i(TAG, "onPreExecute");
			super.onPreExecute();

		}

		@Override
		protected List<Group> doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			Log.i(TAG, "doInBackground");
			List<Group> llist = getNearbyStationsAndBuslines();
			return llist;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			Log.i(TAG, "onProgressUpdate");
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(List<Group> result) {
			// TODO Auto-generated method stub
			Log.i(TAG, "onPostExecute");
			mNearExplistview.stopRefresh();
//			mChooseListener.dismissLoading();
			if (result != null && result.size() > 0) {
				mNearGroups = result;
				mNearHint.setVisibility(View.GONE);
				mNearExplistview.setVisibility(View.VISIBLE);
				mNearAdapter.updateData(mNearGroups);
				mNearAdapter.notifyDataSetChanged();
				int groupCount = result.size() >= 3 ? 3 : result.size();
				for (int i = 0; i < groupCount; i++) {
					Log.d(TAG, "mNearExplistview.setSelectedGroup(0);");
					mNearExplistview.expandGroup(i, false);
				}
				// getRtParam(mNearGroups);
				mRtPresenter.getServerInfo(mNearGroups,mNearAdapter);
			} else {
				mNearHint.setVisibility(View.VISIBLE);
				mNearExplistview.setVisibility(View.GONE);
			}
			String position = "暂时没有定位信息";
			Log.i("testToast",String.valueOf(mBDLocation==null));
			if (mBDLocation != null) {
				String addr = mBDLocation.getAddrStr();
				Log.i("Test addr","changed: "+addr);
				if (addr != null && addr.length() > 0)
					position = addr;
			}
			Log.i("Test position",position);
			Toast.makeText(mContext.getApplicationContext(), position, Toast.LENGTH_SHORT).show();
		}

	}

}
