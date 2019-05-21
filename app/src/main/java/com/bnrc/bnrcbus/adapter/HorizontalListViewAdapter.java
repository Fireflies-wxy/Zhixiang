package com.bnrc.bnrcbus.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bnrc.bnrcbus.R;
import com.bnrc.bnrcbus.model.Bean;
import com.bnrc.bnrcbus.model.Child;
import com.bnrc.bnrcbus.util.RandomColor;
import com.bnrc.bnrcbus.util.TextDrawable;

import java.util.List;
import java.util.Map;

public class HorizontalListViewAdapter extends BaseAdapter {
	private int selectIndex = -1;
	private RandomColor mColor = RandomColor.DEFAULT;
	private Context mContext;
	private int mResource;
	private List<Child> mChildren;
	private Map<Integer, Integer> mHasRtStationList;
	private String[] mFrom;
	private int[] mTo;

	// private ArrayList<Integer> mRtList;
	// private int mCurStationPos = 0;

	public HorizontalListViewAdapter(Context context, List<Child> data,
                                     int resource, String[] from, int[] to) {
		mInflater = LayoutInflater.from(context);
		mContext = context;
		mChildren = data;
		mResource = resource;
		mFrom = from;
		mTo = to;
	}

	public void updateData(List<Child> data) {
		this.mChildren = data;
	}

	public void updateBusAmount(Map<Integer, Integer> hasRtStationList) {
		this.mHasRtStationList = hasRtStationList;
	}

	@Override
	public int getCount() {
		if (mChildren == null || mChildren.size() == 0)
			return 0;
		return mChildren.size();
	}

	private LayoutInflater mInflater;

	@Override
	public Object getItem(int position) {
		return mChildren.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.horizontallistview_item_parallel,
					null);
			holder.mStationSeq = (ImageView) convertView
					.findViewById(R.id.iv_stationSeq);
			holder.mStationName = (TextView) convertView
					.findViewById(R.id.tv_stationName);
			holder.mRTBus = (ImageView) convertView.findViewById(R.id.iv_bus);
			holder.mLineNum = (TextView) convertView
					.findViewById(R.id.tv_linenum);
			holder.mRelativeLayout = (RelativeLayout) convertView
					.findViewById(R.id.rLayout_container);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Child child = mChildren.get(position);
		Bean bean = new Bean(getSequence(position).toString());

		holder.mStationSeq.setImageDrawable(TextDrawable.builder().buildRound(
				bean.name, mColor.getColor(bean.name, position)));
		String stationName = child.getStationName();// stationName


		holder.mStationName.setText(stationName);
		if (position == selectIndex) {
			holder.mStationName.setBackgroundColor(Color.parseColor("#2E9DCC"));// ff9234
		} else {
			holder.mStationName.setBackgroundColor(Color.parseColor("#f7f7f7"));// 00BB9C

		}
		holder.mStationName.setAlpha(0.8f);
		if (mHasRtStationList != null
				&& mHasRtStationList.containsKey(position)) {
			if (position == selectIndex)
				holder.mRTBus
						.setImageResource(R.drawable.icon_bus_arrive);
			else if (position > selectIndex)
				holder.mRTBus
						.setImageResource(R.drawable.icon_bus_pass);
			else
				holder.mRTBus
						.setImageResource(R.drawable.icon_bus_wait);
			int amount = mHasRtStationList.get(position);
			if (amount <= 1)
				holder.mLineNum.setVisibility(View.INVISIBLE);
			else {
				holder.mLineNum.setText(amount + "");
				holder.mLineNum.setVisibility(View.VISIBLE);
			}
			holder.mRelativeLayout.setVisibility(View.VISIBLE);
		} else {
			holder.mRelativeLayout.setVisibility(View.INVISIBLE);
		}

		return convertView;

	}

	private static class ViewHolder {
		private TextView mStationName;
		private ImageView mStationSeq;
		private ImageView mRTBus;
		private RelativeLayout mRelativeLayout;
		private TextView mLineNum;
	}

	public void setSelectIndex(int i) {
		selectIndex = i;
	}

	private String getSequence(int position) {
		return position + 1 + "";
	}


}