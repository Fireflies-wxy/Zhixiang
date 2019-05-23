package com.bnrc.bnrcbus.view.fragment;

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
import com.bnrc.bnrcbus.R;
import com.bnrc.bnrcbus.adapter.CollectAdapter;
import com.bnrc.bnrcbus.adapter.NearAdapter;
import com.bnrc.bnrcbus.constant.Constants;

import com.bnrc.bnrcbus.database.PCUserDataDBHelper;
import com.bnrc.bnrcbus.listener.GetLocationListener;
import com.bnrc.bnrcbus.listener.IPopWindowListener;
import com.bnrc.bnrcbus.model.Child;
import com.bnrc.bnrcbus.model.Group;
import com.bnrc.bnrcbus.network.MyVolley;
import com.bnrc.bnrcbus.network.VolleyNetwork;
import com.bnrc.bnrcbus.ui.LoadingDialog;
import com.bnrc.bnrcbus.ui.expandablelistview.SwipeMenu;
import com.bnrc.bnrcbus.ui.expandablelistview.SwipeMenuCreator;
import com.bnrc.bnrcbus.ui.expandablelistview.SwipeMenuExpandableListView;
import com.bnrc.bnrcbus.ui.expandablelistview.SwipeMenuItem;
import com.bnrc.bnrcbus.ui.pullloadmenulistview.IPullRefresh;
import com.bnrc.bnrcbus.ui.pullloadmenulistview.PullLoadMenuListView;
import com.bnrc.bnrcbus.util.AnimationUtil;
import com.bnrc.bnrcbus.util.LocationUtil;
import com.bnrc.bnrcbus.util.MyCipher;
import com.bnrc.bnrcbus.util.NetAndGpsUtil;
import com.bnrc.bnrcbus.view.activity.BuslineListActivity;

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

public class AllConcernFragSwipe extends BaseFragment {
	private static final String TAG = AllConcernFragSwipe.class.getSimpleName();
	private PullLoadMenuListView mAllConcernExplistview;
	private CollectAdapter mAllConcernAdapter;
	private RelativeLayout mAllConcernHint;
	private List<Group> mGroups;
	private Context mContext;
	private PCUserDataDBHelper mUserDataDBHelper;
	public LocationUtil mLocationUtil = null;
	// private SwipeRefreshLayout swipeRefreshLayout;
	public BDLocation mBDLocation = null;
	private DownloadTask mTask;
	private Handler mHandler = new Handler();
	private IPopWindowListener mOnSelectBtn;
	private VolleyNetwork mVolleyNetwork;
	private NetAndGpsUtil mNetAndGpsUtil;
	private CoordinateConverter mCoordConventer;

	private LoadingDialog mLoading;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		showLoading();

		mLocationUtil = LocationUtil.getInstance(mContext
				.getApplicationContext());
		mLocationUtil.startLocation(new GetLocationListener() {
			@Override
			public void onGetLocation() {
				dismissLoading();
			}
		});
	}

	private SwipeMenuExpandableListView.OnGroupExpandListener mOnGroupExpandListener = new SwipeMenuExpandableListView.OnGroupExpandListener() {
		int lastGroupPos = 0;

		@Override
		public void onGroupExpand(int pos) {
			if (mAllConcernAdapter.getChildrenCount(pos) > 0) {
				// if (lastGroupPos != pos) {
				// mAllConcernExplistview.collapseGroup(lastGroupPos);
				// lastGroupPos = pos;
				// }
				// mAllConcernExplistview.setSelectedGroup(pos);
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
		View view = inflater.inflate(
				R.layout.activity_allconcern_frag_swipemenu,
				(ViewGroup) getActivity().findViewById(R.id.content), false);
		mAllConcernExplistview = (PullLoadMenuListView) view
				.findViewById(R.id.explistview_all_concern);
		mAllConcernHint = (RelativeLayout) view
				.findViewById(R.id.rLayout_all_concern);
		mVolleyNetwork = VolleyNetwork.getInstance(mContext);
		mUserDataDBHelper = PCUserDataDBHelper.getInstance(mContext
				.getApplicationContext());
		mNetAndGpsUtil = NetAndGpsUtil.getInstance(mContext
				.getApplicationContext());
		mGroups = new ArrayList<Group>();
		mAllConcernExplistview.setMenuCreator(mMenuCreator);
		mAllConcernAdapter = new CollectAdapter(mGroups, mContext,
				mAllConcernExplistview.listView, mOnSelectBtn);
		mAllConcernExplistview.setAdapter(mAllConcernAdapter);
		// mAllConcernExplistview
		// .setOnMenuItemClickListener(mMenuItemClickListener);
		mAllConcernExplistview.setOnGroupExpandListener(mOnGroupExpandListener);
		// mAllConcernExplistview.setOnChildClickListener(mOnChildExpandListener);
		mAllConcernExplistview.setPullToRefreshEnable(true);
		mAllConcernExplistview
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
		mCoordConventer = new CoordinateConverter();

		return view;
	}

	public List<Group> getFavStationsAndBuslines() {
		Log.i(TAG, "getFavStationsAndBuslines");
		if (mBDLocation != null) {
			LatLng point = new LatLng(mBDLocation.getLatitude(),
					mBDLocation.getLongitude());
			mGroups = mUserDataDBHelper.acquireFavInfoWithLocation(point,
					Constants.TYPE_ALL);
		} else
			mGroups = mUserDataDBHelper.acquireFavInfoWithLocation(null,
					Constants.TYPE_ALL);
		return mGroups;
	}

	private void pullToRefresh() {
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
				mAllConcernExplistview.stopRefresh();
			}
		}, 3000);
		// getRtParam(mNearGroups);
		getServerInfo(mGroups);

	}

	@Override
	public void refreshConcern() {
		getServerInfo(mGroups);
	}

	@Override
	public void refresh() {
        Log.i("refresh", "refreshed in AllConcernFragSwipe");
		loadDataBase();
	}

	private void loadDataBase() {
		if (mTask != null)
			mTask.cancel(true);
		mTask = new DownloadTask(getActivity());
		mTask.execute();
	}


	private void sortGroup() {
		for (Group group : mGroups)
			Collections.sort(group.getChildren(), comparator);
	}

	private void getRtInfo(final Child child) throws JSONException,
            UnsupportedEncodingException {
		final int sequence = child.getSequence();
		int offlineID = child.getOfflineID();
		String Url = "http://bjgj.aibang.com:8899/bus.php?city="
				+ URLEncoder.encode("北京", "utf-8") + "&id=" + offlineID
				+ "&no=" + sequence + "&type=2&encrypt=1&versionid=2";
//		Log.i("OKHTTP", "url " + Url);// 创建okHttpClient对象
		OkHttpClient mOkHttpClient = new OkHttpClient();
		// 创建一个Request
		final Request request = new Request.Builder()
				.url(Url).build();
		// new call
		Call call = mOkHttpClient.newCall(request);
		// 请求加入调度
		call.enqueue(new Callback() {

			@Override
			public void onFailure(Call call, IOException e) {
				// TODO Auto-generated method stub
				// Log.i(TAG, "onFailure: " + arg0.body().toString());
				if (child != null) {
					Map<String, String> showText = new HashMap<String, String>();
					if (sequence == 1) {
						showText.put("itemsText", "起点站");
						child.setRtInfo(showText);
						child.setRtRank(Child.FIRSTSTATION);
						child.setDataChanged(true);
					} else {
						showText.put("itemsText", "等待发车");
						// 到站
						child.setRtInfo(showText);
						child.setDataChanged(true);
						child.setRtRank(Child.NOTYET);
					}
				}
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						mAllConcernAdapter.notifyDataSetChanged();
					}
				});
			}

			@Override
			public void onResponse(Call call, Response arg0) throws IOException {
// TODO Auto-generated method stub
				try {
					String response = arg0.body().string();
					// Log.i("OKHTTP", "response " + response);
					JSONObject responseJson = XML.toJSONObject(response);
					JSONObject rootJson = responseJson.getJSONObject("root");
					int status = rootJson.getInt("status");
					if (status != 200) {
						if (child != null) {
							Map<String, String> showText = new HashMap<String, String>();
							if (sequence == 1) {
								showText.put("itemsText", "起点站");
								child.setRtInfo(showText);
								child.setRtRank(Child.FIRSTSTATION);
								child.setDataChanged(true);
							} else {
								showText.put("itemsText", "等待发车");
								// 到站
								child.setRtInfo(showText);
								child.setDataChanged(true);
								child.setRtRank(Child.NOTYET);
							}
						}
						sortGroup();
						mHandler.post(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								mAllConcernAdapter.notifyDataSetChanged();
							}
						});
						return;
					}
					JSONObject dataJson = rootJson.getJSONObject("data");
					JSONArray busJsonArray = null;
					if (dataJson.toString().indexOf("[") > 0) {
						busJsonArray = (JSONArray) dataJson.get("bus");
						busJsonArray = dataJson.getJSONArray("bus");
					} else {
						JSONObject busJsonObject = dataJson
								.getJSONObject("bus");
						busJsonArray = new JSONArray("["
								+ busJsonObject.toString() + "]");
					}
					dealRtInfo(busJsonArray, child);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					if (child != null) {
						Map<String, String> showText = new HashMap<String, String>();
						if (sequence == 1) {
							showText.put("itemsText", "起点站");
							child.setRtInfo(showText);
							child.setRtRank(Child.FIRSTSTATION);
							child.setDataChanged(true);
						} else {
							showText.put("itemsText", "等待发车");
							// 到站
							child.setRtInfo(showText);
							child.setDataChanged(true);
							child.setRtRank(Child.NOTYET);
						}
					}
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							mAllConcernAdapter.notifyDataSetChanged();
						}
					});
					e.printStackTrace();
				}
			}
		});
	}

	private void getRtInfo(final Child child, String url) {
		final int sequence = child.getSequence();
//		Log.i("Volley", "url:" + url);
		// 创建okHttpClient对象
		OkHttpClient mOkHttpClient = new OkHttpClient();
		// 创建一个Request
		final Request request = new Request.Builder()
				.url(url).build();
		// new call
		Call call = mOkHttpClient.newCall(request);
		// 请求加入调度
		call.enqueue(new Callback() {

			@Override
			public void onFailure(Call call, IOException e) {
				// TODO Auto-generated method stub
				// Log.i(TAG, "onFailure: " + arg0.body().toString());
				if (child != null) {
					Map<String, String> showText = new HashMap<String, String>();
					if (sequence == 1) {
						showText.put("itemsText", "起点站");
						child.setRtInfo(showText);
						child.setRtRank(Child.FIRSTSTATION);
						child.setDataChanged(true);
					} else {
						showText.put("itemsText", "等待发车");
						// 到站
						child.setRtInfo(showText);
						child.setRtRank(Child.NOTYET);
						child.setDataChanged(true);
					}

				}
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						mAllConcernAdapter.notifyDataSetChanged();
					}
				});
			}

			@Override
			public void onResponse(Call call, Response arg0) throws IOException {
				try {
					String response = arg0.body().string();
//					Log.i(TAG, "onResponse: " + response);
//					Log.i("OKHTTP", "response " + response);
					JSONObject responseJson = XML.toJSONObject(response);
					JSONObject rootJson = responseJson.getJSONObject("root");
					int status = rootJson.getInt("status");
					if (status != 200) {
						// Log.i(TAG, child.getBuslineFullName()
						// + " 暂无实时公交信息");
						if (child != null) {
							Map<String, String> showText = new HashMap<String, String>();
							if (sequence == 1) {
								showText.put("itemsText", "起点站");
								child.setRtInfo(showText);
								child.setRtRank(Child.FIRSTSTATION);
								child.setDataChanged(true);
							} else {
								showText.put("itemsText", "等待发车");
								// 到站
								child.setRtInfo(showText);
								child.setDataChanged(true);

								child.setRtRank(Child.NOTYET);
							}
						}
						sortGroup();
						mHandler.post(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								mAllConcernAdapter.notifyDataSetChanged();
							}
						});
						return;
					}
					JSONObject dataJson = rootJson.getJSONObject("data");
					JSONArray busJsonArray = null;
					if (dataJson.toString().indexOf("[") > 0) {
						busJsonArray = (JSONArray) dataJson.get("bus");
						busJsonArray = dataJson.getJSONArray("bus");
					} else {
						JSONObject busJsonObject = dataJson
								.getJSONObject("bus");
						busJsonArray = new JSONArray("["
								+ busJsonObject.toString() + "]");
					}
					dealRtInfo(busJsonArray, child);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					if (child != null) {
						Map<String, String> showText = new HashMap<String, String>();
						if (sequence == 1) {
							showText.put("itemsText", "起点站");
							child.setRtInfo(showText);
							child.setRtRank(Child.FIRSTSTATION);
							child.setDataChanged(true);
						} else {
							showText.put("itemsText", "等待发车");
							// 到站
							child.setRtInfo(showText);
							child.setDataChanged(true);

							child.setRtRank(Child.NOTYET);
						}
					}
					sortGroup();
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							mAllConcernAdapter.notifyDataSetChanged();
						}
					});
					e.printStackTrace();
				}
			}
		});

	}

	private void getServerInfo(List<Group> groups) {
		if (groups == null)
			return;
		for (Group group : groups) {
			if (group.getChildrenCount() <= 0)
				continue;
			List<Child> children = group.getChildren();
			for (final Child child : children) {
				final int LineID = child.getLineID();
				int StationID = child.getStationID();
				Log.i(TAG, "LineID: " + LineID + " ; " + "StationID: "
						+ StationID);
				final int sequence = child.getSequence();
				if (sequence == 1) {
					Map<String, String> showText = new HashMap<String, String>();
					showText.put("itemsText", "起点站");
					child.setRtInfo(showText);
					child.setRtRank(Child.FIRSTSTATION);
					child.setDataChanged(true);
			
				} else
					mVolleyNetwork.getNearestBusWithLineAndOneStation(LineID,
							StationID, new VolleyNetwork.requestListener() {

								@Override
								public void onSuccess(JSONObject data) {
									// TODO Auto-generated method stub
									try {
										Log.i(TAG, "实验室服务器有数据");
										JSONArray arr = null;
										if (data.toString().indexOf("[") > 0) {
											arr = data.getJSONArray("dt");
										} else {
											JSONObject busJsonObject = data
													.getJSONObject("dt");
											arr = new JSONArray("["
													+ busJsonObject.toString()
													+ "]");
										}
										if (arr != null && arr.length() > 0) {
											Log.i(TAG, "ARR!=NULL");
											int size = arr.length();
											List<Map<String, ?>> list = child
													.getRtInfoList();
											list.clear();
											for (int i = 0; i < size; i++) {
												Map<String, String> map = new HashMap<String, String>();
												JSONObject json = arr
														.getJSONObject(i);
												int distance = json
														.getInt("Sd");
												int time = json.getInt("St");
												int station = json.getInt("bn");
												if (time <= 10) {
													map.put("station", "已经");
													map.put("time", "到站");
												} else {
													int tmp = time / 60;
													if (tmp <= 0)
														map.put("station", time
																+ " 秒");
													else
														map.put("station", tmp
																+ " 分");
													map.put("time", station
															+ " 站");
												}
												list.add(map);
												Log.i(TAG, child.getLineName()
														+ " distance: "
														+ distance + " ; "
														+ "time: " + time);
											}
											if (child != null) {
												// child.setRtInfo(showText);
												child.setRtRank(Child.ARRIVING);
												child.setDataChanged(true);
											}
										} else {
											Map<String, String> showText = new HashMap<String, String>();
											if (sequence == 1) {
												showText.put("itemsText", "起点站");
												child.setRtInfo(showText);
												child.setRtRank(Child.FIRSTSTATION);
												child.setDataChanged(true);
											} else {
												showText.put("itemsText",
														"<font color=\"black\">"
																+ "等待发车"
																+ "</font>");
												if (child != null) {
													child.setRtInfo(showText);
													child.setRtRank(Child.NOTYET);
													child.setDataChanged(true);
												}
											}
										}
										// JSONObject json = data
										// .getJSONObject("dt");
										// int distance = json
										// .getInt("StationDistance");
										// int time = json
										// .getInt("StationArrivingTime");
										// Map<String, String> showText = new
										// HashMap<String, String>();
										// if (distance == 0 && time == 0)
										// showText.put("itemsText",
										// "<font color=\"black\">"
										// + "等待发车" + "</font>");
										// else if (time <= 10)
										// showText.put("itemsText",
										// "<font color=\"red\">" + "即将到站"
										// + "</font>");
										// else {
										// int tmp = time / 60;
										// if (tmp <= 0)
										// showText.put("itemsText",
										// "<font color=\"red\">"
										// + time + " 秒钟"
										// + "</font>");
										// else
										// showText.put("itemsText",
										// "<font color=\"red\">"
										// + tmp + " 分钟"
										// + "</font>");
										// }
										// if (child != null) {
										// child.setRtInfo(showText);
										// child.setRtRank(Child.ARRIVING);
										// child.setDataChanged(true);
										// }
										sortGroup();
										mAllConcernAdapter
												.notifyDataSetChanged();

									} catch (JSONException e) {
										// TODO Auto-generated catch block
//										Log.i(TAG,
//												"onSuccess: " + e.getMessage());
										// e.printStackTrace();
									}
								}

								@Override
								public void onNotAccess() {
									// TODO Auto-generated method stub
									// Map<String, String> showText = new
									// HashMap<String, String>();
									// showText.put("itemsText",
									// "<font color=\"grey\">" + "未开通"
									// + "</font>");
									// if (child != null) {
									// child.setRtInfo(showText);
									// child.setRtRank(Child.NOTEXIST);
									// }
									// sortGroup();
									// mAllConcernAdapter.notifyDataSetChanged();
									Log.i(TAG, "未开通");
									Map<String, String> showText = new HashMap<String, String>();
									showText.put("itemsText",
											"<font color=\"grey\">" + "未开通"
													+ "</font>");
									if (child != null) {
										child.setRtInfo(showText);
										child.setRtRank(Child.NOTEXIST);
										child.setDataChanged(true);

									}
									sortGroup();
									mAllConcernAdapter.notifyDataSetChanged();
								}

								@Override
								public void onFormatError() {
									// TODO Auto-generated method stub
									Log.i(TAG, "数据格式不对");
									if (child.getOfflineID() <= 0) {
										Map<String, String> showText = new HashMap<String, String>();
										showText.put("itemsText",
												"<font color=\"grey\">" + "未开通"
														+ "</font>");
										if (child != null) {
											child.setRtInfo(showText);
											child.setDataChanged(true);

											child.setRtRank(Child.NOTEXIST);
										}
										sortGroup();
										mAllConcernAdapter
												.notifyDataSetChanged();

									} else {
										try {
											getRtInfo(child);
										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								}

								@Override
								public void onDataNA(String url) {
									// TODO Auto-generated method stub
									getRtInfo(child, url);
									Log.i(TAG, "数据过旧");
								}

								@Override
								public void onNetError() {
									// TODO Auto-generated method stub
									if (child.getOfflineID() <= 0) {
										Map<String, String> showText = new HashMap<String, String>();
										showText.put("itemsText",
												"<font color=\"grey\">" + "未开通"
														+ "</font>");
										if (child != null) {
											child.setRtInfo(showText);
											child.setDataChanged(true);

											child.setRtRank(Child.NOTEXIST);
										}
										sortGroup();
										mAllConcernAdapter
												.notifyDataSetChanged();

									} else {
										try {
											getRtInfo(child);
										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								}
							});
			}
		}

	}

	private String TimeStampToDate(Long timestampString, String formats) {
		if (timestampString < 0)
			return "0";
		String date = new java.text.SimpleDateFormat(formats)
				.format(new java.util.Date(timestampString * 1000));
		return date;
	}

	private int TimeStampToDelTime(Long timestampString) {
		if (timestampString < 0)
			return (int) 0;
		double delTime = (timestampString / 60 - System.currentTimeMillis() / 60000);
		Log.i(TAG, "TimeStampToDelTime: " + delTime);
		return (int) Math.ceil(delTime);
	}

	private boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("-?[0-9]+.*[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}

	Comparator<Child> comparator = new Comparator<Child>() {
		public int compare(Child c1, Child c2) {

			if (c1 == null && c2 == null)
				return 0;
			else if (c1 == null)
				return -1;
			else if (c2 == null)
				return 1;
			int rank1 = c1.getRtRank();
			int rank2 = c2.getRtRank();
			if (rank1 > rank2)
				return -1;
			else if (rank1 < rank2)
				return 1;
			else
				return 0;

		}
	};
	Comparator<Map<String, ?>> comparatorRt = new Comparator<Map<String, ?>>() {
		public int compare(Map<String, ?> c1, Map<String, ?> c2) {

			int n1 = Integer.parseInt(c1.get("nextStationNum").toString());
			int n2 = Integer.parseInt(c2.get("nextStationNum").toString());
			if (n1 > n2)
				return -1;
			else if (n1 < n2)
				return 1;
			return 0;

		}
	};

	private void dealRtInfo(JSONArray json, final Child child) {
		Map<String, String> showText = new HashMap<String, String>();
		showText.put("itemsText", "等待发车");
		int rank = Child.NOTYET;
		int sequence = child.getSequence();
		try {
			try {
				int count = json.length();
				Log.i(TAG, "busJsonArray_count: " + count);
				JSONObject uploadJson = new JSONObject();
				JSONArray uploadData = new JSONArray();
				uploadJson.put("c", "beijing");
				uploadJson.put("dt", uploadData);
				int max = 0;
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				// Map<String, Object> map = new HashMap<String, Object>();
				for (int j = 0; j < count; j++) {
					JSONObject busJson = (JSONObject) json.get(j);
					JSONObject uplodaItem = new JSONObject();
					MyCipher mCiper = new MyCipher("aibang"
							+ busJson.getString("gt"));

					String nextStationName = mCiper.decrypt(busJson
							.getString("ns"));// nextStationName
					int nextStationNum = Integer.parseInt(mCiper
							.decrypt(busJson.getString("nsn")));// nextStationNum
					int id = busJson.getInt("id");
					String nextStationDistance = busJson.getString("nsd");// nextStationDistance
					String nextStationTime = busJson.getString("nst");// nextStationTime

					String stationDistance = mCiper.decrypt(busJson
							.getString("sd"));// stationDistance
					String stationArrivingTime = mCiper.decrypt(busJson
							.getString("st"));
					String st_c = null;
					if (isNumeric(stationArrivingTime))
						// st_c = TimeStampToDate(Long.parseLong(st),
						// "HH:mm");// station_arriving_time
						st_c = String.valueOf(TimeStampToDelTime(Long
								.parseLong(stationArrivingTime)));// station_arriving_time
					else
						st_c = "-1";
					String x = mCiper.decrypt(busJson.getString("x"));
					String y = mCiper.decrypt(busJson.getString("y"));
//					Log.i(TAG,
//							"next_station_name: " + nextStationName + "\n"
//									+ "next_station_num: " + nextStationNum
//									+ "\n" + "next_station_distance: "
//									+ nextStationDistance + "\n"
//									+ "next_station_arriving_time: "
//									+ nextStationTime + "\n"
//									+ "station_distance: " + stationDistance
//									+ "\n" + "station_arriving_time: "
//									+ stationArrivingTime + "   " + st_c + "\n"
//									+ " currentTime "
//									+ System.currentTimeMillis());
					uplodaItem.put("LID", child.getLineID());
					uplodaItem.put("BID",
							child.getLineID() + String.format("%02d", j + 1));
					uplodaItem.put("Nsn", nextStationNum);
					uplodaItem.put("Nsd", nextStationDistance);
					LatLng latLngBaidu = mCoordConventer
							.from(CoordinateConverter.CoordType.COMMON)
							.coord(new LatLng(Double.parseDouble(y), Double
									.parseDouble(x))).convert();
					uplodaItem.put("Lat", latLngBaidu.latitude);
					uplodaItem.put("Lon", latLngBaidu.longitude);
					uplodaItem.put("T", System.currentTimeMillis() / 1000);
					uploadData.put(uplodaItem);
					if (nextStationNum <= sequence) {
						// map.clear();
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("nextStationNum", nextStationNum);
						map.put("stationArrivingTime", st_c);
						map.put("stationDistance", stationDistance);
						max = nextStationNum;
						list.add(map);
					}
				}
				mVolleyNetwork.upLoadRtInfo(uploadJson, new VolleyNetwork.upLoadListener() {

					@Override
					public void onSuccess() {
						// TODO Auto-generated method stub
						Log.i(TAG, child.getLineName() + " 上传成功");
					}

					@Override
					public void onFail() {
						// TODO Auto-generated method stub
						Log.i(TAG, child.getLineName() + " 上传失败");
					}
				});
				List<Map<String, ?>> tmpList = child.getRtInfoList();
				tmpList.clear();
				if (list.size() <= 0) {

					// 起点站
					if (sequence == 1) {
						showText.put("itemsText", "起点站");
						rank = Child.FIRSTSTATION;
					} // 未开通
					else {
						showText.put("itemsText", "等待发车");
						rank = Child.NOTYET;
					}
					// throw new JSONException("等待发车");

				} else {
					Collections.sort(list, comparatorRt);
					int size = list.size() > 3 ? 3 : list.size();
					for (int i = 0; i < size; i++) {
						Map<String, ?> map = list.get(i);
						int nextStationNum = (Integer) map
								.get("nextStationNum");
						String stationArrivingTime = map.get(
								"stationArrivingTime").toString();
						String stationDistance = map.get("stationDistance")
								.toString();
						// // 未开通
						// if (stationArrivingTime == null || nextStationNum ==
						// 0) {
						// showText.put("itemsText", "等待发车");
						// rank = Child.NOTYET;
						// }
						// // 起点站
						// else if (sequence == 1) {
						// showText.put("itemsText", "起点站");
						// rank = Child.FIRSTSTATION;
						// }
						// 到站
						Map<String, String> tmpMap = new HashMap<String, String>();
						if (nextStationNum <= sequence) {
							if (isNumeric(stationArrivingTime)) {
								if (nextStationNum == sequence) {
									// 已到站
									if (Integer.parseInt(stationArrivingTime) < 10) {
										// showText.put("itemsText",
										// "<font color=\"red\">" + "已到站"
										// + "</font>");
										tmpMap.put("station", "已经");
										tmpMap.put("time", "到站");
										rank = Child.ARRIVING;
									}
									// 即将到站
									else if (Integer.parseInt(stationDistance) < 10) {
										// showText.put("itemsText",
										// "<font color=\"red\">" + "即将到站"
										// + "</font>");
										tmpMap.put("station", "即将");
										tmpMap.put("time", "到站");
										rank = Child.SOON;
									} else {
										int nstime = TimeStampToDelTime(Long
												.parseLong(stationArrivingTime));// 计算还有几分钟
										if (nstime <= 0) {
											// showText.put("itemsText",
											// "<font color=\"red\">"
											// + "即将到站"
											// + "</font>");
											tmpMap.put("station", "即将");
											tmpMap.put("time", "到站");
											rank = Child.SOON;
										} else {
											// showText.put("itemsText",
											// "<font color=\"red\">"
											// + nstime + " 分钟"
											// + "</font>");
											tmpMap.put("station", 1 + " 站");
											tmpMap.put("time", nstime + " 分");
											rank = Child.ONTHEWAY;
										}
									}
								} else {
									int nstime = TimeStampToDelTime(Long
											.parseLong(stationArrivingTime));// 计算还有几分钟
									if (nstime <= 0) {
										// showText.put("itemsText",
										// "<font color=\"red\">" + "即将到站"
										// + "</font>");
										tmpMap.put("station", "即将");
										tmpMap.put("time", "到站");
										rank = Child.SOON;
									} else {
										// showText.put("itemsText",
										// "<font color=\"red\">" + nstime
										// + " 分钟" + "</font>");
										tmpMap.put("station", (sequence
												- nextStationNum + 1)
												+ " 站");
										tmpMap.put("time", nstime + " 分");
										rank = Child.ONTHEWAY;
									}
								}
							}
						}
						tmpList.add(tmpMap);
						// else {
						// showText.put("itemsText", "等待发车");
						// rank = Child.NOTYET;
						// }
					}
				}
			} catch (SQLException sqle) {
				throw sqle;
			}

		} catch (JSONException e) {
//			Log.e("JSON exception", e.getMessage());
			e.printStackTrace();
		} finally {
			if (child != null) {
				child.setRtInfo(showText);
				child.setDataChanged(true);
				child.setRtRank(rank);
			}
			sortGroup();
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					mAllConcernAdapter.notifyDataSetChanged();
				}
			});
		}
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
		if(mLocationUtil==null){
			mLocationUtil = LocationUtil.getInstance(mContext);
		}
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
			Log.i("testContext", "All : context == null? : "+(context==null));
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
			showLoading();
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
			dismissLoading();
			if (result != null && result.size() > 0) {
				mGroups = result;
				mAllConcernAdapter.updateData(mGroups);
				mAllConcernAdapter.notifyDataSetChanged();
				mAllConcernHint.setVisibility(View.GONE);
				mAllConcernExplistview.setVisibility(View.VISIBLE);
				int groupCount = result.size();
				for (int i = 0; i < groupCount; i++)
					mAllConcernExplistview.expandGroup(i);
				// getRtParam(mGroups);
				getServerInfo(mGroups);
			} else {
				mAllConcernHint.setVisibility(View.VISIBLE);
				mAllConcernExplistview.setVisibility(View.GONE);
			}
		}

	}

	public synchronized LoadingDialog showLoading() {
		if (mLoading == null) {
			mLoading = new LoadingDialog(mContext, R.layout.view_tips_loading);
			mLoading.setCancelable(false);
			mLoading.setCanceledOnTouchOutside(true);
		}
		mLoading.show();
		return mLoading;
	}

	public synchronized void dismissLoading() {
		if (!getActivity().isFinishing() && this.mLoading != null
				&& this.mLoading.isShowing()) {
			mLoading.dismiss();
		}
	}

}
