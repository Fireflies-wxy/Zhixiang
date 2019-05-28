package com.bnrc.busapp.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bnrc.busapp.R;
import com.bnrc.busapp.ui.viewpager.NoScrollViewPager;
import com.bnrc.busapp.ui.viewpager.ViewpagerIndicator;
import com.bnrc.busapp.ui.viewpager.VpAdapter;
import com.bnrc.busapp.view.activity.SearchActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by apple on 2018/5/23.
 */

public class HomeFragment extends BaseFragment {

    private View mContentView;

    //viewpager相关
    private ViewpagerIndicator indicator;
    private NoScrollViewPager viewPager;
    private VpAdapter mAdapter;
    private List<BaseFragment> mFragment;
    private List<String> mTitle;
    private TextView search_view;

    //滑动控件
    private NestedScrollView nestedScrollView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();
        mContentView = inflater.inflate(R.layout.fragment_home,container,false);
        initView();
        return mContentView;
    }

    public void initView(){
        viewPager = mContentView.findViewById(R.id.viewpager);

        search_view = mContentView.findViewById(R.id.search_view);
        search_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                /** 加这个判断，防止该事件被执行两次 */
                if (v.getId() == R.id.search_view) {
                    Intent intent = new Intent(getActivity(),
                            SearchActivity.class);   //搜索界面
                    startActivity(intent);
                }
            }
        });

        mTitle = new ArrayList<>();
        mTitle.add(getString(R.string.text_collect));
        mTitle.add(getString(R.string.text_near));
        mTitle.add(getString(R.string.text_concern));

        mFragment = new ArrayList<>();
        mFragment.add(new CollectFragment());
        mFragment.add(new NearFragment());
        mFragment.add(new ConcernFragment());

        //设置适配器
        viewPager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
            //选中的item
            @Override
            public Fragment getItem(int position) {
                return mFragment.get(position);
            }

            //返回item的个数
            @Override
            public int getCount() {
                return mFragment.size();
            }

            //设置标题
            @Override
            public CharSequence getPageTitle(int position) {
                return mTitle.get(position);
            }
        });

        //设置indicator
        indicator = mContentView.findViewById(R.id.indicator);
        indicator.setViewPager(viewPager);
    }


    // 刷新实时数据
    @Override
    public void refresh() {
        if (this != null && !this.isDetached() && this.isVisible()) {
            if (mFragment == null)
                return;
            for (BaseFragment frag : mFragment)
                frag.refresh();
        }
    }

}
