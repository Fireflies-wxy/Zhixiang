package com.bnrc.bnrcbus.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bnrc.bnrcbus.R;
import com.bnrc.bnrcbus.ui.RTabHost;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by apple on 2018/5/23.
 */

public class HomeFragment extends BaseFragment {

    private static final String TAG = "HomeFragment";

    private View mContentView;
    private FragmentManager fm;
    private List<BaseFragment> fragmentList = new ArrayList<>();
    private List<Class<? extends BaseFragment>> classList = null;
    Class<? extends BaseFragment> fragClass = null;
    private int mLastIndex = 0;  //初始化时默认加载第一项"首页"
    private RTabHost mTabHost;

    private BaseFragment mFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();
        mContentView = inflater.inflate(R.layout.fragment_home,container,false);

        initFragments();
        initTabHost();

        return mContentView;
    }


    // 刷新实时数据
    @Override
    public void refresh() {
        if (this != null && !this.isDetached() && this.isVisible()) {
            if (mFragment == null)
                return;
            for (BaseFragment frag : fragmentList)
                frag.refresh();
        }
    }

    private void initFragments(){
        fragmentList.clear();
        classList = new ArrayList<>();
        classList.add(CollectFragment.class);
        classList.add(NearFragment.class);
        classList.add(ConcernFragment.class);

        fm = getFragmentManager();
        FragmentTransaction transcation = fm.beginTransaction();

        fragClass = classList.get(mLastIndex);
        mFragment = createFragmentByClass(fragClass);
        transcation.replace(R.id.page_conatainer, mFragment).commit();
    }

    private BaseFragment createFragmentByClass(
            Class<? extends BaseFragment> fragClass) {
        BaseFragment frag = null;
        try {
            try {
                Constructor<? extends BaseFragment> cons = null;
                cons = fragClass.getConstructor();
                frag = cons.newInstance();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        } catch (Throwable e) {
            throw new RuntimeException("Can not create instance for class "
                    + fragClass.getName(), e);
        }
        return frag;
    }

    private void initTabHost() {
        mTabHost = mContentView.findViewById(R.id.rtabhost);

        mTabHost.setQQTabHostListener(new RTabHost.RTabHostListener() {

            @Override
            public void onTabSelected(int index) {
                selectTab(index);
                Log.i(TAG, "onTabSelected: "+index);
            }
        });

        mTabHost.selectTab(mLastIndex);
    }

    private void selectTab(int index) {
        if (mLastIndex == index) {
            return;
        }
        Log.i(TAG, "selectTab: "+index);
        mLastIndex = index;
        selectFragment(index);
    }

    private void selectFragment(int index) {

        Log.i(TAG, "selectFragment: "+index);

        FragmentTransaction transcation = getFragmentManager().beginTransaction();
        fragClass = classList.get(index);
        mFragment = createFragmentByClass(fragClass);
        transcation.replace(R.id.page_conatainer, mFragment).commit();
    }

}
