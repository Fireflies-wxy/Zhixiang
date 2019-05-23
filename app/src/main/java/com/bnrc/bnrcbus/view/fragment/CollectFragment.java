package com.bnrc.bnrcbus.view.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioGroup;


import com.bnrc.bnrcbus.R;
import com.bnrc.bnrcbus.constant.Constants;
import com.bnrc.bnrcbus.database.UserDataDBHelper;
import com.bnrc.bnrcbus.listener.IPopWindowListener;
import com.bnrc.bnrcbus.service.PollingService;
import com.bnrc.bnrcbus.service.ScanService;
import com.bnrc.bnrcbus.ui.viewpager.MyViewPager;
import com.bnrc.bnrcbus.util.PollingUtils;
import com.bnrc.bnrcbus.util.ServiceUtils;
import com.bnrc.bnrcbus.view.SegmentedGroup;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by apple on 2018/6/4.
 */

public class CollectFragment extends BaseFragment {

    private static final String TAG = CollectFragment.class.getSimpleName();
    private Context mContext;
    private SegmentedGroup segmented;
    private MyViewPager mPager;
    private AllConcernFragSwipe mAllFrag;
    private WorkFragSwipe mWorkFrag;
    private HomeFragSwipe mHomeFrag;
    private ArrayList<BaseFragment> mFragmentList;
    private ImageButton menuSettingBtn;// 菜单呼出按钮
    private IPopWindowListener mChooseListener;
    private MyViewPagerAdapter mPagerAdapter;
    private UserDataDBHelper mUserDB;
    private int mLastIndex = 0;
    private List<Integer> TABLE;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        mContext = (Context) getActivity();
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.nav_collect, null);
        mUserDB = UserDataDBHelper.getInstance(mContext);

        segmented = view.findViewById(R.id.segmentedGroup);
        segmented.setTintColor(getResources().getColor(
                R.color.radio_button_selected_color));

        mAllFrag = new AllConcernFragSwipe();
        mWorkFrag = new WorkFragSwipe();
        mHomeFrag = new HomeFragSwipe();
        mFragmentList = new ArrayList<BaseFragment>();
        mFragmentList.add(mAllFrag);
        mFragmentList.add(mWorkFrag);
        mFragmentList.add(mHomeFrag);

        mPager = (MyViewPager) view.findViewById(R.id.content);

        segmented.setOnCheckedChangeListener(new CheckedChangeListener());

        mPagerAdapter = new MyViewPagerAdapter(getFragmentManager());

        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(new PageChangeListener());
        mPager.setOffscreenPageLimit(3);

        segmented.check(R.id.radBtn_all);
        TABLE = new ArrayList<Integer>();
        TABLE.add(0);
        TABLE.add(1);
        TABLE.add(2);
        return view;
    }

    public class MyViewPagerAdapter extends FragmentPagerAdapter {
        private FragmentManager mFragmentManager;
        private FragmentTransaction mCurTransaction;

        public MyViewPagerAdapter(FragmentManager fm) {
            super(fm);
            // TODO Auto-generated constructor stub
            this.mFragmentManager = fm;
        }

        @Override
        public Fragment getItem(int position) {
            Log.i(TAG, "mFragmentList.get(position) " + position);
            return mFragmentList.get(position);

        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public int getItemPosition(Object object) {
            // TODO Auto-generated method stub
            return FragmentStatePagerAdapter.POSITION_NONE;
        }

        public void destroyAllItem() {
            int mPosition = mPager.getCurrentItem();
            int mPositionMax = mPager.getCurrentItem() + 1;
            if (TABLE.size() > 0 && mPosition < TABLE.size()) {
                if (mPosition > 0) {
                    mPosition--;
                }
                mPosition = 0;
                mPositionMax = 3;
                for (int i = mPosition; i < mPositionMax; i++) {
                    try {
                        Object objectobject = this.instantiateItem(mPager,
                                TABLE.get(i).intValue());
                        if (objectobject != null)
                            destroyItem(mPager, TABLE.get(i).intValue(),
                                    objectobject);
                    } catch (Exception e) {
                        Log.i(TAG, "no more Fragment in FragmentPagerAdapter");
                    }
                }
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);

            if (position <= getCount()) {
                if (mCurTransaction == null)
                    mCurTransaction = mFragmentManager.beginTransaction();
                mCurTransaction.remove((Fragment) object);
                mCurTransaction.commit();
            }
        }

    }

    private class CheckedChangeListener implements RadioGroup.OnCheckedChangeListener {
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radBtn_all:
                    mPager.setCurrentItem(0);
                    Log.i(TAG, "onCheckedChanged: "+0);
                    mLastIndex = 0;
                    break;
                case R.id.radBtn_work:
                    mPager.setCurrentItem(1);
                    Log.i(TAG, "onCheckedChanged: "+1);
                    mLastIndex = 1;
                    break;
                case R.id.radBtn_home:
                    mPager.setCurrentItem(2);
                    Log.i(TAG, "onCheckedChanged: "+2);
                    mLastIndex = 2;
                    break;

            }
        }
    }

    // 刷新实时数据
    @Override
    public void refreshConcern() {
        if (this != null && !this.isDetached() && this.isVisible()) {
            if (mFragmentList == null)
                return;
            for (BaseFragment frag : mFragmentList)
                frag.refreshConcern();
        }
    }

    // 刷新实时数据
    @Override
    public void refresh() {
        if (this != null && !this.isDetached() && this.isVisible()) {
            if (mFragmentList == null)
                return;
            for (BaseFragment frag : mFragmentList)
                frag.refresh();
        }
    }

    private class PageChangeListener implements ViewPager.OnPageChangeListener {
        public void onPageSelected(int position) {
            switch (position) {
                case 0:
                    segmented.check(R.id.radBtn_all);
                    break;
                case 1:
                    segmented.check(R.id.radBtn_work);
                    break;
                case 2:
                    segmented.check(R.id.radBtn_home);
                    break;
            }
        }

        public void onPageScrollStateChanged(int arg0) {
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }
    }


    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
        mContext = (Context) activity;
        mChooseListener = (IPopWindowListener) activity;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop polling service
        System.out.println("Stop polling service...");
        PollingUtils.stopPollingService(mContext, PollingService.class,
                PollingService.ACTION);
        ServiceUtils.stopPollingService(mContext.getApplicationContext(),
                ScanService.class, Constants.SERVICE_ACTION);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();

    }

    @Override
    public void onDetach() {
        // TODO Auto-generated method stub
        super.onDetach();
        mPagerAdapter.destroyAllItem();

    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

    }

    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();

    }

}
