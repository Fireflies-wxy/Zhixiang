package com.bnrc.bnrcbus.view.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bnrc.bnrcbus.R;
import com.bnrc.bnrcbus.network.IHttpClient;
import com.bnrc.bnrcbus.network.IRequest;
import com.bnrc.bnrcbus.network.IResponse;
import com.bnrc.bnrcbus.network.api.API;
import com.bnrc.bnrcbus.network.impl.BaseRequest;
import com.bnrc.bnrcbus.network.impl.OkHttpClientImpl;

/**
 * Created by apple on 2018/5/24.
 */

public class NearFragment extends BaseFragment {


    private TextView mSubway;
    private TextView mHotel;
    private TextView mRestaurant;
    private TextView mBank;
    private TextView mSupermarket;
    private TextView mOil;
    private TextView mNetbar;
    private TextView mKTV;

    private View mContentView;

    private IHttpClient httpClient;
    private static final String TAG = "NearFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();
        mContentView = inflater.inflate(R.layout.fragment_near,container,false);
        initView();
        return mContentView;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void initView(){

    }
}


