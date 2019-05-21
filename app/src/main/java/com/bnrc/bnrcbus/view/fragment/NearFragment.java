package com.bnrc.bnrcbus.view.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.bnrc.bnrcbus.R;
import com.bnrc.bnrcbus.adapter.NearAdapter;
import com.bnrc.bnrcbus.listener.IPopWindowListener;
import com.bnrc.bnrcbus.model.Group;
import com.bnrc.bnrcbus.model.bus.BusModel;
import com.bnrc.bnrcbus.ui.pullloadmenulistview.PullLoadMenuListView;
import com.bnrc.bnrcbus.util.LocationUtil;
import com.bnrc.bnrcbus.util.NetAndGpsUtil;

import java.util.List;

import okhttp3.OkHttpClient;

/**
 * Created by apple on 2018/5/24.
 */

public class NearFragment extends BaseFragment {


    private static final String TAG = "NearFragment";

    private PullLoadMenuListView mNearExplistview;
    private NearAdapter mNearAdapter;//adapter
    private RelativeLayout mNearHint;
    private List<Group> mNearGroups;
    private Context mContext;
    private View mContentView;
    public LocationUtil mLocationUtil = null;
    private BDLocation mBDLocation = null;
    private IPopWindowListener mChooseListener;
    private DownloadTask mTask;
    private int mChildrenSize = 0;
    public static boolean isFirstLoad = true;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private LatLng mOldPoint;
    private NetAndGpsUtil mNetAndGpsUtil;
    private CoordinateConverter mCoordConventer;
    private OkHttpClient mOkHttpClient;

    private BusModel mBusData;
    private TextView text_near;
    private String ErrorBusURL;

    private ProgressDialog progressDialog;

    private View mContentView;

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


