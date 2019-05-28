package com.bnrc.busapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bnrc.busapp.R;
import com.bnrc.busapp.database.PCUserDataDBHelper;
import com.bnrc.busapp.model.Child;
import com.bnrc.busapp.model.Group;
import com.bnrc.busapp.util.AnimationUtil;
import com.bnrc.busapp.view.activity.AlertSelectListActivity;


import java.util.List;

public class ConcernAdapter extends BaseExpandableListAdapter {
	public static final int NULL = 0;
	public static final int FAV = 1;
	public static final int NORMAL = 2;
	private Context mContext;
	private static final String TAG = ConcernAdapter.class.getSimpleName();
	private List<Group> groups;
	private LayoutInflater inflater;
	public static final int ADDALERT = 11;
	public static final int DONTADDALERT = 12;
	private int type = ADDALERT;

	public ConcernAdapter(List<Group> groups, Context context) {
		this.groups = groups;
		this.mContext = context;
		inflater = LayoutInflater.from(this.mContext);
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

		ChildViewHolder childHolder = null;
		if (convertView == null) {
//			if (isLastChild)
//				convertView = inflater.inflate(R.layout.child_alert_bottom,
//						null);
//			else
				convertView = inflater.inflate(R.layout.child_alert, null);
			childHolder = new ChildViewHolder();
			childHolder.buslineName = (TextView) convertView
					.findViewById(R.id.tv_alert_line);
			childHolder.ringImg = (ImageView) convertView
					.findViewById(R.id.iv_alerticon);
			childHolder.switchImg = (ImageView) convertView
					.findViewById(R.id.iv_switch);
			childHolder.deleteBtn = (Button) convertView.findViewById(R.id.btn_delete);
			childHolder.deleteBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					Log.i(TAG, "onClick: delete alert");
				}
			});
			convertView.setTag(childHolder);
		} else {
			childHolder = (ChildViewHolder) convertView.getTag();
		}
		final Child child = groups.get(groupPosition).getChildItem(
				childPosition);
		Log.i(TAG, "getChildView " + child.getLineName());
		if (child != null) {
			childHolder.buslineName.setText(child.getLineName());
			int LineID = child.getLineID();
			String StationName = child.getStationName();
			if (child.isAlertOpen() == Child.CLOSE) {
				childHolder.switchImg
						.setImageResource(R.drawable.switchbtn_closed);
				child.setAlertOpen(Child.CLOSE);
			} else if (child.isAlertOpen() == Child.OPEN) {
				childHolder.switchImg
						.setImageResource(R.drawable.switchbtn_open);
				child.setAlertOpen(Child.OPEN);
			} else {
				if (PCUserDataDBHelper.getInstance(mContext)
						.IsAlertOpenBusline(LineID, StationName)) {
					childHolder.switchImg
							.setImageResource(R.drawable.switchbtn_open);
					child.setAlertOpen(Child.OPEN);
				} else {
					childHolder.switchImg
							.setImageResource(R.drawable.switchbtn_closed);
					child.setAlertOpen(Child.CLOSE);
				}
			}
		}
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// Log.i(TAG, "getChildrenCount ");

		Log.i(TAG, "groupPosition: " + groupPosition);
		if (groups == null || groups.size() == 0)
			return 0;
		return groups.get(groupPosition).getChildrenCount();

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
		GroupViewHolder groupHolder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.group_concern, null);
			groupHolder = new GroupViewHolder();
			groupHolder.icon = (ImageView) convertView
					.findViewById(R.id.groupIcon);
			groupHolder.text = (TextView) convertView
					.findViewById(R.id.tv_arrive);
			groupHolder.title = (TextView) convertView
					.findViewById(R.id.tv_rtstation);
			groupHolder.mStationSeq = (ImageView) convertView
					.findViewById(R.id.iv_sequence);
			groupHolder.img_waitStatus = convertView.findViewById(R.id.img_waitStatus);
			convertView.setTag(groupHolder);
		} else {
			groupHolder = (GroupViewHolder) convertView.getTag();
		}
		if (isExpanded) {
			groupHolder.icon.setImageResource(R.drawable.down_arrow);
		} else {
			groupHolder.icon.setImageResource(R.drawable.right_arrow);
		}
		final Group group = groups.get(groupPosition);
		groupHolder.title.setText(group.getStationName());
		switch (type) {
		case ADDALERT:
			groupHolder.text.setVisibility(View.VISIBLE);
			groupHolder.text.setText("添加提醒");
			groupHolder.text.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (group == null)
						return;
					Intent intent = new Intent(mContext,
							AlertSelectListActivity.class);
					intent.putExtra("StationName", group.getStationName());
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mContext.startActivity(intent);
					AnimationUtil.activityZoomAnimation(mContext);
				}
			});
			break;
		case DONTADDALERT:
			groupHolder.text.setVisibility(View.INVISIBLE);
			break;
		default:
			break;
		}
		groupHolder.mStationSeq.setVisibility(View.GONE);

		int waitStatusRate = group.getStationStatus();  //乘车拥挤度

		switch (waitStatusRate){
			case 1:
				groupHolder.img_waitStatus.setBackgroundResource(R.drawable.wait_status_low);
				break;
			case 2:
				groupHolder.img_waitStatus.setBackgroundResource(R.drawable.wait_status_mid);
				break;
			case 3:
				groupHolder.img_waitStatus.setBackgroundResource(R.drawable.wait_status_high);
				break;
		}

		if(group.getStationName().equals("明光桥北")){
			groupHolder.img_waitStatus.setBackgroundResource(R.drawable.wait_status_mid);
		}

		groupHolder.img_waitStatus.setOnClickListener(new OnClickListener() {
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

	class GroupViewHolder {
		ImageView icon;
		TextView text;
		ImageView mStationSeq;
		ImageView img_waitStatus;
		TextView title;
	}

	class ChildViewHolder {
		TextView buslineName;
		ImageView switchImg;
		ImageView ringImg;
		Button deleteBtn;
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

	public void setType(int type) {
		this.type = type;
	}

}
