package com.bnrc.bnrcbus.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bnrc.bnrcbus.R;
import com.bnrc.bnrcbus.database.PCUserDataDBHelper;
import com.bnrc.bnrcbus.listener.IPopWindowListener;
import com.bnrc.bnrcbus.model.Child;
import com.bnrc.bnrcbus.model.Group;
import com.bnrc.bnrcbus.ui.expandablelistview.FrontViewToMove;
import com.bnrc.bnrcbus.util.AnimationUtil;
import com.bnrc.bnrcbus.util.DensityUtil;
import com.bnrc.bnrcbus.util.NetAndGpsUtil;
import com.bnrc.bnrcbus.util.RandomColor;
import com.bnrc.bnrcbus.view.activity.BuslineListActivity;
import com.bnrc.bnrcbus.view.activity.StationListActivity;

import java.util.List;
import java.util.Map;

public class CollectAdapter extends BaseExpandableListAdapter {
	public static final int NULL = 0;
	public static final int FAV = 1;
	public static final int NORMAL = 2;
	private Context mContext;

	private static final String TAG = CollectAdapter.class.getSimpleName();
	private List<Group> groups;
	private LayoutInflater inflater;
	private RandomColor mColor = RandomColor.MATERIAL;
	private Animation push_left_in;
	private ListView mListView;
	private IPopWindowListener mChooseListener;
	private NetAndGpsUtil mNetAndGpsUtil;

	public CollectAdapter(List<Group> groups, Context context,
                          ListView listview, IPopWindowListener mChooseListener) {
		this.groups = groups;
		this.mContext = context;
		inflater = LayoutInflater.from(this.mContext);
		push_left_in = AnimationUtils.loadAnimation(context,
				R.anim.in_from_left);
		mListView = listview;
		this.mChooseListener = mChooseListener;
		mNetAndGpsUtil = NetAndGpsUtil.getInstance(mContext);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// Log.i(TAG, "getChild ");
		if (groups == null)
			return null;
		return groups.get(groupPosition).getChildItem(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// Log.i(TAG, "getChildId ");
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	public void updateData(List<Group> groups) {
		this.groups = groups;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
		Log.i(TAG, "getChildView ");

		ChildViewHolder holder = null;
		if (convertView == null) {
			if (isLastChild)
				convertView = inflater.inflate(R.layout.child_loc,
						null);
			else
				convertView = inflater.inflate(R.layout.child_loc, null);
			holder = new ChildViewHolder();
			holder.buslineName = (TextView) convertView
					.findViewById(R.id.tv_buslineName);
			holder.destination = (TextView) convertView
					.findViewById(R.id.tv_destination);
			holder.rtInfo = (TextView) convertView.findViewById(R.id.tv_info);
			holder.rLayout = (RelativeLayout) convertView
					.findViewById(R.id.rLayout);
			holder.concernStar = (ImageView) convertView
					.findViewById(R.id.iv_concern);
			holder.lLayoutContainer = (LinearLayout) convertView
					.findViewById(R.id.lLayout_container);
			holder.fixButton = (TextView) convertView
					.findViewById(R.id.btn_delete);
			holder.frontView = convertView.findViewById(R.id.id_front);
			holder.img_carStatus = convertView.findViewById(R.id.img_carStatus);
			convertView.setTag(holder);

		} else {
			holder = (ChildViewHolder) convertView.getTag();
		}
		final Child child = groups.get(groupPosition).getChildItem(
				childPosition);
		Log.i(TAG, "getChildView " + child.getLineName());
		if (child.isDataChanged()) {
			child.setDataChanged(false);
			// push_left_in.setDuration(duration);
			convertView.startAnimation(push_left_in);
			// convertView.setAnimation(push_left_in);
		}
		//
		// if (child != null) {
		// holder.buslineName.setText(child.getLineName());
		// holder.destination.setText(child.getEndStation());
		// if (child.getRtInfo() != null) {
		// holder.rtInfo.setText(Html.fromHtml((child.getRtInfo().get(
		// "itemsText").toString())));
		// }
		// }
		setData(holder, groupPosition, childPosition);

		int carStatusRate = child.getLineStatus();  //乘车拥挤度

		switch (carStatusRate){
			case 1:
				holder.img_carStatus.setBackgroundResource(R.drawable.wait_status_low);
				break;
			case 2:
				holder.img_carStatus.setBackgroundResource(R.drawable.wait_status_mid);
				break;
			case 3:
				holder.img_carStatus.setBackgroundResource(R.drawable.wait_status_high);
				break;
		}


		holder.img_carStatus.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (child.getLineStatus()){
					case 1:
						Toast.makeText(mContext.getApplicationContext(),"乘车拥挤度:舒适",Toast.LENGTH_SHORT).show();
						break;
					case 2:
						Toast.makeText(mContext.getApplicationContext(),"乘车拥挤度：适中",Toast.LENGTH_SHORT).show();
						break;
					case 3:
						Toast.makeText(mContext.getApplicationContext(),"乘车拥挤度；拥挤",Toast.LENGTH_SHORT).show();
						break;
				}
			}
		});

		return convertView;
	}

	private void setData(ChildViewHolder holder, final int groupPosition,
			final int childPosition) {
		final Child child = groups.get(groupPosition).getChildItem(
				childPosition);
		if (child != null) {
			holder.buslineName.setText(child.getLineName());
			holder.destination.setText(child.getEndStation());
			if (!mNetAndGpsUtil.isNetworkAvailable()) {
				holder.rtInfo.setVisibility(View.VISIBLE);
				holder.lLayoutContainer.setVisibility(View.GONE);
				holder.rtInfo.setText("暂无网络");
				return;
			}
			if (child.getRtRank() >= 3) {
				holder.rtInfo.setVisibility(View.GONE);
				holder.lLayoutContainer.setVisibility(View.VISIBLE);
				holder.lLayoutContainer.removeAllViews();
				List<Map<String, ?>> list = child.getRtInfoList();
				synchronized (list) {
					for (Map<String, ?> map : list) {
						String station = map.get("station").toString();
						String time = map.get("time").toString();
						View item = View.inflate(mContext,
								R.layout.rtinfo_item, null);
						int w = View.MeasureSpec.makeMeasureSpec(0,
								View.MeasureSpec.UNSPECIFIED);
						int h = View.MeasureSpec.makeMeasureSpec(0,
								View.MeasureSpec.UNSPECIFIED);
						item.measure(w, h);
						int width = item.getMeasuredWidth();
						LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
								width, LinearLayout.LayoutParams.WRAP_CONTENT);
						lp.setMargins(0, 0, 10, 0);
						item.setLayoutParams(lp);
						TextView tvStation = (TextView) item
								.findViewById(R.id.tv_info1);
						TextView tvTime = (TextView) item
								.findViewById(R.id.tv_info2);
						tvStation.setText(station);
						tvTime.setText(time);
						holder.lLayoutContainer.addView(item);
					}
				}
			} else {
				holder.rtInfo.setVisibility(View.VISIBLE);
				holder.lLayoutContainer.setVisibility(View.GONE);
				if (child.getRtInfo() != null) {
					Log.i(TAG, child.getLineName() + " "
							+ child.getRtInfo().get("itemsText"));
					if (child.getRtInfo().get("itemsText") != null)
						holder.rtInfo.setText(Html.fromHtml((child.getRtInfo()
								.get("itemsText").toString())));
					else
						holder.rtInfo.setText("null");
				}
			}
			holder.frontView.setOnClickListener(new OnClickListener() {
				// ��Ϊ��дontouch�¼�ʹonChildClickListenerʧЧ����Ҫ���ôμ���������
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Group group = groups.get(groupPosition);
					Child child = group.getChildItem(childPosition);
					Intent intent = new Intent(mContext, BuslineListActivity.class);
					intent.putExtra("LineID", child.getLineID());
					intent.putExtra("StationID", child.getStationID());
					intent.putExtra("FullName", child.getLineFullName());
					intent.putExtra("Sequence", child.getSequence());
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mContext.startActivity(intent);
					AnimationUtil.activityZoomAnimation((Activity) mContext);
				}
			});
			int xToMove = DensityUtil.dip2px(mContext, 60);

			final FrontViewToMove frontViewToMove = new FrontViewToMove(
					holder.frontView, mListView, xToMove);
			// �ؼ���䣬ʹ���Լ�д��������frontView��ontouch�¼���д��ʵ����ͼ����Ч��

			int childType = getChildType(groupPosition, childPosition);
			switch (childType) {
			case FAV:
				holder.fixButton.setText("修改");
				holder.fixButton
						.setBackgroundResource(R.drawable.bg_circle_drawable_notstar);
				// holder.fixButton.setTextColor(R.color.white);
				break;
			case NORMAL:
				holder.fixButton.setText("收藏");
				holder.fixButton
						.setBackgroundResource(R.drawable.bg_circle_drawable);
				// holder.fixButton.setTextColor(R.color.white);
				break;
			default:
				holder.fixButton.setText("收藏");
				holder.fixButton
						.setBackgroundResource(R.drawable.bg_circle_drawable);
				// holder.fixButton.setTextColor(R.color.white);
				break;
			}

			holder.fixButton.setOnClickListener(new OnClickListener() {
				// Ϊbutton���¼��������ô˰�ť��ʵ��ɾ���¼�

				@Override
				public void onClick(View v) {
					if (mChooseListener == null)
						Log.i(TAG, "mChooseListener==null");
					if (child == null)
						Log.i(TAG, "child==null");
					mChooseListener.onPopClick(child);
					frontViewToMove.swipeBack();
				}
			});

		}
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// Log.i(TAG, "getChildrenCount ");

		Log.i(TAG, "groupPosition: " + groupPosition);
		if (groups == null)
			return 0;
		if (groupPosition >= 0)
			return groups.get(groupPosition).getChildrenCount();
		else
			return -1;

	}

	@Override
	public Object getGroup(int groupPosition) {
		// Log.i(TAG, "getGroup ");
		if (groups == null)
			return null;
		return groups.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		// Log.i(TAG, "getGroupCount " + groups.size());
		if (groups == null)
			return 0;
		return groups.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		// Log.i(TAG, "getGroupId ");
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
		// Log.i(TAG, "getGroupView ");
		GroupViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.group_concern, null);
			holder = new GroupViewHolder();
			holder.icon = (ImageView) convertView.findViewById(R.id.groupIcon);
			holder.mStationSeq = (ImageView) convertView
					.findViewById(R.id.iv_sequence);
			holder.title = (TextView) convertView
					.findViewById(R.id.tv_rtstation);
			holder.text = (TextView) convertView.findViewById(R.id.tv_arrive);
			final Group group = groups.get(groupPosition);
			holder.img_waitStatus = convertView.findViewById(R.id.img_waitStatus);
			if(group.getStationName().equals("明光桥北")){
				holder.img_waitStatus.setBackgroundResource(R.drawable.wait_status_mid);
			}
			convertView.setTag(holder);
		} else {
			holder = (GroupViewHolder) convertView.getTag();
		}
		if (isExpanded) {
			holder.icon.setImageResource(R.drawable.down_arrow);
		} else {
			holder.icon.setImageResource(R.drawable.right_arrow);
		}
		final Group group = groups.get(groupPosition);
		holder.title.setText(group.getStationName());
		holder.text.setVisibility(View.VISIBLE);
		holder.text.setText("查看详细");
		holder.text.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent stationIntent = new Intent(mContext,
						StationListActivity.class);

				stationIntent.putExtra("StationName", group.getStationName());
				stationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(stationIntent);
				AnimationUtil.activityZoomAnimation(mContext);

			}
		});
		holder.mStationSeq.setVisibility(View.GONE);

		holder.img_waitStatus = convertView.findViewById(R.id.img_waitStatus);

		int waitStatusRate = group.getStationStatus();  //乘车拥挤度

		switch (waitStatusRate){
			case 1:
				holder.img_waitStatus.setBackgroundResource(R.drawable.wait_status_low);
				break;
			case 2:
				holder.img_waitStatus.setBackgroundResource(R.drawable.wait_status_mid);
				break;
			case 3:
				holder.img_waitStatus.setBackgroundResource(R.drawable.wait_status_high);
				break;
		}

		if(group.getStationName().equals("明光桥北")){
			holder.img_waitStatus.setBackgroundResource(R.drawable.wait_status_mid);
		}

		holder.img_waitStatus.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (group.getStationStatus()){
					case 1:
						Toast.makeText(mContext.getApplicationContext(),"候车拥挤度:舒适",Toast.LENGTH_SHORT).show();
						break;
					case 2:
						Toast.makeText(mContext.getApplicationContext(),"候车拥挤度：适中",Toast.LENGTH_SHORT).show();
						break;
					case 3:
						Toast.makeText(mContext.getApplicationContext(),"候车拥挤度；拥挤",Toast.LENGTH_SHORT).show();
						break;
				}
			}
		});

		return convertView;
	}

	public boolean isNetworkConnected(Context ctx) {
		if (ctx != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) ctx
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	private View createChildrenView() {
		// Log.i(TAG, "createChildrenView ");
		return inflater.inflate(R.layout.child_concern, null);
	}

	private View createChildrenViewBottom() {
		Log.i(TAG, "createChildrenView ");
		return inflater.inflate(R.layout.child_concern_bottom, null);
	}

	private View createGroupView() {
		Log.i(TAG, "createGroupView ");
		return inflater.inflate(R.layout.group_concern, null);
	}

	class GroupViewHolder {
		ImageView icon;
		TextView text;
		ImageView mStationSeq;
		ImageView img_waitStatus;
		TextView title;
	}

	class ChildViewHolder {
		TextView buslineName;
		TextView destination;
		TextView rtInfo;
		RelativeLayout rLayout;
		ImageView concernStar;
		LinearLayout lLayoutContainer;
		TextView fixButton; //
		View frontView;
		ImageView img_carStatus;
	}

	private String getSequence(int position) {
		return String.valueOf((char) (position + 65));
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	@Override
	public int getChildType(int groupPosition, int childPosition) {
		Group group = groups.get(groupPosition);
		Child child = group.getChildItem(childPosition);
		int LineID = child.getLineID();
		int StationID = child.getStationID();
		if (PCUserDataDBHelper.getInstance(mContext).IsFavStation(LineID,
				StationID))
			return FAV;
		else
			return NORMAL;
	}

	@Override
	public int getChildTypeCount() {
		return 3;
	}

	@Override
	public int getGroupType(int groupPosition) {
		return NULL;
	}

	@Override
	public int getGroupTypeCount() {
		return 0;
	}

	public void refresh() {
		this.notifyDataSetChanged();
	}
}
