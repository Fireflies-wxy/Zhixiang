package com.bnrc.busapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bnrc.busapp.listener.ItemDelListener;

import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2011 All rights reserved ���ƣ�MyListViewAdapter
 * ������ListView�Զ���Adapter����
 * 
 * @author zhaoqp
 * @date 2011-11-8
 * @version
 */
public class MyRoutListViewAdapter extends BaseAdapter {

	private Context mContext;
	// ���еĲ���
	private int mResource;
	// �б�չ�ֵ�����
	private List<? extends Map<String, ?>> mData;
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
	public MyRoutListViewAdapter(Context context,
                                 List<? extends Map<String, ?>> data, int resource, String[] from,
                                 int[] to) {
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
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final Map<String, ?> dataSet = mData.get(position);
		if (dataSet == null) {
			return null;
		}
		// ��ȡ��������
		final Object data0 = dataSet.get(mFrom[0]);
		final Object data1 = dataSet.get(mFrom[1]);
		final Object data2 = dataSet.get(mFrom[2]);

		// �������ݵ�View
		holder.itemsIcon.setImageResource((Integer) data0);
		holder.itemsTitle.setText(data1.toString());
		holder.itemsText.setText(data2.toString());

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