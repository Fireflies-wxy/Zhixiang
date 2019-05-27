package com.bnrc.bnrcbus.view.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;
import com.bnrc.bnrcbus.R;
import com.bnrc.bnrcbus.adapter.ConcernAdapter;
import com.bnrc.bnrcbus.constant.Constants;
import com.bnrc.bnrcbus.database.PCUserDataDBHelper;
import com.bnrc.bnrcbus.listener.IPopWindowListener;
import com.bnrc.bnrcbus.model.Child;
import com.bnrc.bnrcbus.model.Group;
import com.bnrc.bnrcbus.service.PollingService;
import com.bnrc.bnrcbus.service.ScanService;
import com.bnrc.bnrcbus.ui.expandablelistview.SwipeMenuExpandableListView;
import com.bnrc.bnrcbus.ui.pullloadmenulistview.PullLoadMenuListView;
import com.bnrc.bnrcbus.util.LocationUtil;
import com.bnrc.bnrcbus.util.PollingUtils;
import com.bnrc.bnrcbus.util.ServiceUtils;

import java.util.List;

/**
 * Created by apple on 2018/6/4.
 */

public class ConcernFragment extends BaseFragment {

    private View mContentView;
    private static final String TAG = ConcernFragment.class.getSimpleName();
    private Context mContext;
    private PullLoadMenuListView mListView;
    private IPopWindowListener mChooseListener;
    private ImageButton menuSettingBtn;// 菜单呼出按钮
    private ImageView mMainSwitch;
    private ConcernAdapter mAdapter;
    private List<Group> mListData;
    private PCUserDataDBHelper mUserDB;
    public LocationUtil mLocationUtil = null;
    public BDLocation mBDLocation = null;
    private RelativeLayout mHint;
    private DownloadTask mTask;
    private boolean isAllOpen = false;

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
        mContext = (Context) getActivity();
        mChooseListener = (IPopWindowListener) activity;
        Log.i("BaseFragment", TAG + " onAttach");

    }

    private SwipeMenuExpandableListView.OnGroupExpandListener mOnGroupExpandListener = new SwipeMenuExpandableListView.OnGroupExpandListener() {
        int lastGroupPos = 0;

        @Override
        public void onGroupExpand(int pos) {
            if (lastGroupPos != pos) {
                mListView.collapseGroup(lastGroupPos);
                lastGroupPos = pos;
            }

            mListView.setSelectedGroup(pos);
        }
    };

    private SwipeMenuExpandableListView.OnChildClickListener mOnChildExpandListener = new SwipeMenuExpandableListView.OnChildClickListener() {

        @Override
        public boolean onChildClick(ExpandableListView paramExpandableListView,
                                    View paramView, int paramInt1, int paramInt2, long paramLong) {
            // TODO Auto-generated method stub
            Group group = mListData.get(paramInt1);
            Child child = group.getChildItem(paramInt2);
            if (child.isAlertOpen() == Child.OPEN) {
                child.setAlertOpen(Child.CLOSE);
                mUserDB.closeAlertBusline(child);
            } else {
                child.setAlertOpen(Child.OPEN);
                mUserDB.openAlertBusline(child);
            }
            mAdapter.notifyDataSetChanged();
            return false;
        }

    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();
        mContentView = inflater.inflate(R.layout.fragment_concern,container,false);
        mLocationUtil = LocationUtil.getInstance(mContext);
        mLocationUtil.startLocation(null);
        mBDLocation = mLocationUtil.getmLocation();
        mListView =  mContentView.findViewById(R.id.list);
        mHint = mContentView.findViewById(R.id.rLayout_alert);
//        mMainSwitch = mContentView.findViewById(R.id.open_alert_btn);
//        mMainSwitch.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                if (isAllOpen) {
//                    mUserDB.closeAllAlertBusline();
//                    ((ImageView) v).setImageResource(R.drawable.alertoffimg);
//                    isAllOpen = false;
//                    if (mListData != null)
//                        for (Group gro : mListData)
//                            gro.closeAllChildAlert();
//                } else {
//                    mUserDB.openAllAlertBusline();
//                    ((ImageView) v).setImageResource(R.drawable.alertonimg);
//                    isAllOpen = true;
//                    if (mListData != null)
//                        for (Group gro : mListData)
//                            gro.openAllChildAlert();
//                }
//                if (mAdapter != null)
//                    mAdapter.notifyDataSetChanged();
//
//            }
//        });

        mUserDB = PCUserDataDBHelper.getInstance(mContext);
        mAdapter = new ConcernAdapter(mListData, mContext);
        mListView.setAdapter(mAdapter);
        mListView.setOnGroupExpandListener(mOnGroupExpandListener);
        mListView.setOnChildClickListener(mOnChildExpandListener);
        mListView.setPullToRefreshEnable(false);
        return mContentView;
    }

    private List<Group> loadAlertInfo() {
        LatLng myPoint = null;
        if (mBDLocation != null)
            myPoint = new LatLng(mBDLocation.getLatitude(),
                    mBDLocation.getLongitude());
        mListData = mUserDB.acquireAlertInfoWithLocation(myPoint);
        return mListData;
    }

    private void loadDataBase() {
        if (mTask != null)
            mTask.cancel(true);
        mTask = new DownloadTask(getActivity());
        mTask.execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i("BaseFragment", TAG + " onStart");

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
        Log.i(TAG, TAG + " onDestroy");

        Log.i("BaseFragment", TAG + " onDestroy");

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        Log.i("BaseFragment", TAG + " onActivityCreated");

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Log.i("BaseFragment", TAG + " onCreate");

    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
        Log.i("BaseFragment", TAG + " onDestroyView");

    }

    @Override
    public void onDetach() {
        // TODO Auto-generated method stub
        super.onDetach();
        Log.i("BaseFragment", TAG + " onDetach");

    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.i("BaseFragment", TAG + " onResume");
//        isAllOpen = mUserDB.IsAllAlertOpen();
//        if (isAllOpen)
//            mMainSwitch.setImageResource(R.drawable.alertonimg);
//        else
//            mMainSwitch.setImageResource(R.drawable.alertoffimg);
        loadDataBase();
        InputMethodManager imm = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        Log.i("BaseFragment", TAG + " onStop");
    }

    class DownloadTask extends AsyncTask<Integer, Integer, List<Group>> {
        // 后面尖括号内分别是参数（线程休息时间），进度(publishProgress用到)，返回值 类型

        private Context mContext = null;

        public DownloadTask(Context context) {
            this.mContext = context;
        }

        /*
         * 第一个执行的方法 执行时机：在执行实际的后台操作前，被UI 线程调用
         * 作用：可以在该方法中做一些准备工作，如在界面上显示一个进度条，或者一些控件的实例化，这个方法可以不用实现。
         *
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            Log.d(TAG, "onPreExecute");
            super.onPreExecute();
        }

        /*
         * 执行时机：在onPreExecute 方法执行后马上执行，该方法运行在后台线程中 作用：主要负责执行那些很耗时的后台处理工作。可以调用
         * publishProgress方法来更新实时的任务进度。该方法是抽象方法，子类必须实现。
         *
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override
        protected List<Group> doInBackground(Integer... params) {
            // TODO Auto-generated method stub
            Log.d(TAG, "doInBackground");
            // for (int i = 0; i <= 100; i++) {
            // mProgressBar.setProgress(i);
            publishProgress();

            // try {
            // Thread.sleep(params[0]);
            // } catch (InterruptedException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
            // }
            return loadAlertInfo();
        }

        /*
         * 执行时机：这个函数在doInBackground调用publishProgress时被调用后，UI
         * 线程将调用这个方法.虽然此方法只有一个参数,但此参数是一个数组，可以用values[i]来调用
         * 作用：在界面上展示任务的进展情况，例如通过一个进度条进行展示。此实例中，该方法会被执行100次
         *
         * @see android.os.AsyncTask#onProgressUpdate(Progress[])
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onProgressUpdate");
            // mTextView.setText(values[0] + "%");
            mChooseListener.showLoading();
            super.onProgressUpdate(values);
        }

        /*
         * 执行时机：在doInBackground 执行完成后，将被UI 线程调用 作用：后台的计算结果将通过该方法传递到UI
         * 线程，并且在界面上展示给用户 result:上面doInBackground执行后的返回值，所以这里是"执行完毕"
         *
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(List<Group> result) {
            // TODO Auto-generated method stub
            Log.d(TAG, "onPostExecute");
            if (result != null && result.size() > 0) {
                mListData = result;
                mAdapter.updateData(mListData);
                mAdapter.notifyDataSetChanged();
                mHint.setVisibility(View.GONE);
                mListView.expandGroup(0);
            } else {
                mHint.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.GONE);
            }
            mChooseListener.dismissLoading();
        }

    }

}
