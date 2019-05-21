package com.bnrc.bnrcbus.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bnrc.bnrcbus.R;
import com.bnrc.bnrcbus.constant.Constants;
import com.bnrc.bnrcbus.listener.ItemDelListener;
import com.bnrc.bnrcbus.model.historyItem;

import java.util.List;

/**
 * Copyright (c) 2011 All rights reserved ���ƣ�MyListViewAdapter
 * ������ListView�Զ���Adapter����
 * 
 * @author zhaoqp
 * @date 2011-11-8
 * @version
 */
public class MyLatestListViewAdapter extends BaseAdapter {

	private Context mContext;
	// ���еĲ���
	private int mResource;
	// �б�չ�ֵ�����
	private List<historyItem> mData;
	// Map�е�key
	private String[] mFrom;
	// view��id
	private int[] mTo;

	private ItemDelListener mDelListener;

	/**
	 * ���췽��
	 * 
	 * @param context
	 * @param data
	 *            �б�չ�ֵ�����
	 * @param resource
	 *            ���еĲ���
	 * @param from
	 *            Map�е�key
	 * @param to
	 *            view��id
	 */
	public MyLatestListViewAdapter(Context context, List<historyItem> data,
                                   int resource, String[] from, int[] to) {
		mContext = context;
		mData = data;
		mResource = resource;
		mFrom = from;
		mTo = to;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView,
                        final ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(mResource,
					parent, false);
			holder = new ViewHolder();
			holder.itemsIcon = ((ImageView) convertView.findViewById(mTo[0]));
			holder.itemsTitle = ((TextView) convertView.findViewById(mTo[1]));
			holder.itemsText = ((TextView) convertView.findViewById(mTo[2]));
			holder.itemsDel = ((ImageView) convertView.findViewById(mTo[3]));
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		historyItem item = mData.get(position);
		int type = item.getType();
		switch (type) {
		case Constants.BUSLINE:
			holder.itemsIcon.setImageResource(R.drawable.icon_bus);
			holder.itemsTitle.setText(item.getLineName());
			holder.itemsText.setText(item.getStartStation() + " - "
					+ item.getEndStation());

			break;
		case Constants.STATION:
			holder.itemsIcon.setImageResource(R.drawable.icon_station);
			holder.itemsTitle.setText(item.getStationName());
			holder.itemsText.setText("途径 " + item.getLineNum() + " 条线路");
			break;
		default:
			break;
		}
		// holder.itemsIcon.setImageResource((Integer) data0);
		// holder.itemsTitle.setText(data1.toString());
		// holder.itemsText.setText(data2.toString());
		if (item.isDelete()) {
			holder.itemsDel.setVisibility(View.VISIBLE);
		} else
			holder.itemsDel.setVisibility(View.GONE);
		holder.itemsDel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {
				// TODO Auto-generated method stub
				if (mDelListener != null)
					mDelListener.onDelItem(position);
			}
		});
		return convertView;
	}

	public void setDelListener(ItemDelListener delListener) {
		this.mDelListener = delListener;
	}

	/**
	 * ViewHolder��
	 */
	static class ViewHolder {
		ImageView itemsIcon;
		TextView itemsTitle;
		TextView itemsText;
		ImageView itemsDel;
	}
}