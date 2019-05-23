package com.bnrc.bnrcbus.ui.viewpager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by apple on 2018/6/5.
 */

public class VpAdapter extends FragmentPagerAdapter {

    private ArrayList<View> viewLists;

    String[] pageTitles = {"附近站点", "收藏线路", "提醒线路"};

    public VpAdapter(FragmentManager fm) {
        super(fm);
    }
//
//    public VpAdapter() {
//    }
//
//    public VpAdapter(ArrayList<View> viewLists) {
//        super();
//        this.viewLists = viewLists;
//    }

    @Override
    public int getCount() {
        return viewLists.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return pageTitles[position % 3];
    }


    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Fragment getItem(int position) {
        return null;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(viewLists.get(position));
        return viewLists.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(viewLists.get(position));
    }
}
