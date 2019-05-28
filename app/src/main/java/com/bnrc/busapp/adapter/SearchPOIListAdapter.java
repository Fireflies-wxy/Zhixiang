package com.bnrc.busapp.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.baidu.mapapi.search.core.PoiInfo;
import com.bnrc.busapp.R;

import java.util.List;

/**
 * Created by heyouhua on 2017/7/7.
 */

public class SearchPOIListAdapter extends BaseAdapter {
    private Context mContext;
    /**
     * poi列表
     */
    private List<PoiInfo> mPOIList;

    public SearchPOIListAdapter(Context mContext, List<PoiInfo> mPOIList) {
        this.mContext = mContext;
        this.mPOIList = mPOIList;
    }

    @Override
    public int getCount() {
        return mPOIList != null ? mPOIList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mPOIList != null ? mPOIList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return mPOIList != null ? position : 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final PoiInfo poi = mPOIList.get(position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.sug_search_poi_item_view, null);
            holder = new ViewHolder();
            holder.mNameTextView = (TextView) convertView.findViewById(R.id.search_poi_txt_name);
            holder.mAddressTextView = (TextView) convertView.findViewById(R.id.search_poi_txt_address);
            holder.mIconImageView = (ImageView) convertView.findViewById(R.id.search_poi_iv_icon);
            holder.mHintTextView = (TextView) convertView.findViewById(R.id.search_poi_txt_hint);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.mNameTextView.setText(poi.name);
        if (!TextUtils.isEmpty(poi.address)) {
            holder.mAddressTextView.setText(poi.address);
        } else {
            holder.mAddressTextView.setText("");
        }

        holder.mIconImageView.setVisibility(View.GONE);
        holder.mHintTextView.setVisibility(View.GONE);

        return convertView;
    }

    static class ViewHolder {
        TextView mNameTextView;
        TextView mAddressTextView;
        ImageView mIconImageView;
        TextView mHintTextView;
    }
}
