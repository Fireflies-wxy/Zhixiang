package com.bnrc.bnrcbus.view.activity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.bnrc.bnrcbus.R;
import com.bnrc.bnrcbus.adapter.ConcernAdapter;
import com.bnrc.bnrcbus.database.PCDataBaseHelper;
import com.bnrc.bnrcbus.database.PCUserDataDBHelper;
import com.bnrc.bnrcbus.model.Child;
import com.bnrc.bnrcbus.model.Group;
import com.bnrc.bnrcbus.ui.expandablelistview.SwipeMenuExpandableListView;
import com.bnrc.bnrcbus.ui.pullloadmenulistview.PullLoadMenuListView;
import com.bnrc.bnrcbus.view.activity.base.BaseActivity;

import java.util.List;

public class AlertSelectListActivity extends BaseActivity {
    private static final String TAG = AlertSelectListActivity.class.getSimpleName();
    public PCDataBaseHelper mDataManager = null;
    private String StationName;
    private List<Group> mGroups = null;
    private PCUserDataDBHelper mUserDB = null;
    private PCDataBaseHelper mDataBaseHelper = null;
    private loadDataBaseTask mTask;
    private PullLoadMenuListView mAlertListView;
    private ConcernAdapter mAlertAdapter;
    // 定义Handler对象
    private Handler mHandler = new Handler();
    private TextView alert_menu_view,tv_alert_title;

    private SwipeMenuExpandableListView.OnGroupExpandListener mOnGroupExpandListener = new SwipeMenuExpandableListView.OnGroupExpandListener() {

        @Override
        public void onGroupExpand(int pos) {

        }
    };

    private SwipeMenuExpandableListView.OnChildClickListener mOnChildExpandListener = new SwipeMenuExpandableListView.OnChildClickListener() {

        @Override
        public boolean onChildClick(ExpandableListView paramExpandableListView,
                                    View paramView, int paramInt1, int paramInt2, long paramLong) {
            // TODO Auto-generated method stub
            Child child = mGroups.get(paramInt1).getChildItem(paramInt2);
            if (child.isAlertOpen() == Child.OPEN) {
                child.setAlertOpen(Child.CLOSE);
                // mUserDB.closeAlertBusline(LineID, StationName);
                mUserDB.closeAlertBusline(child);
            } else {
                child.setAlertOpen(Child.OPEN);
                mUserDB.openAlertBusline(child);
            }
            mAlertAdapter.notifyDataSetChanged();
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_select_list);
        tv_alert_title = findViewById(R.id.tv_alert_title);
        alert_menu_view = findViewById(R.id.alert_menu_view);
        alert_menu_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.alert_menu_view:
                        finish();
                        break;
                }
            }
        });
        tv_alert_title.setText("选择提醒线路");

        mUserDB = PCUserDataDBHelper.getInstance(this);
        mDataBaseHelper = PCDataBaseHelper.getInstance(this);
        StationName = getIntent().getStringExtra("StationName");
        mAlertListView = (PullLoadMenuListView) findViewById(R.id.explistview_alert_station);
        mAlertAdapter = new ConcernAdapter(mGroups, this);
        mAlertListView.setAdapter(mAlertAdapter);
        mAlertAdapter.setType(ConcernAdapter.DONTADDALERT);
        mAlertListView.setOnGroupExpandListener(mOnGroupExpandListener);
        mAlertListView.setOnChildClickListener(mOnChildExpandListener);
        mAlertListView.setPullToRefreshEnable(false);
    }

    private List<Group> loadBusline() {
        mGroups = mDataBaseHelper.acquireAllBusLinesWithStation(StationName);
        return mGroups;
    }

    private void loadDataBase() {
        if (mTask != null)
            mTask.cancel(true);
        mTask = new loadDataBaseTask(this);
        mTask.execute();
    }

    public void onResume() {
        super.onResume();
//		MobclickAgent.onPageStart("SplashScreen"); // ͳ��ҳ��
//		MobclickAgent.onResume(this); // ͳ��ʱ��
        // registerReceiver(mWifiReceiver, wifiFilter);
        // registerReceiver(mActivityReceiver, activityFilter);
        loadDataBase();
    }

    @Override
    public void onPause() {
        super.onPause();
//		MobclickAgent.onPageEnd("SplashScreen");
//		MobclickAgent.onPause(this);
        // unregisterReceiver(mWifiReceiver);
        // unregisterReceiver(mActivityReceiver);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onRestart() {
        super.onRestart();
    }

    class loadDataBaseTask extends AsyncTask<Integer, Integer, List<Group>> {
        // 后面尖括号内分别是参数（线程休息时间），进度(publishProgress用到)，返回值 类型

        private Context mContext = null;

        public loadDataBaseTask(Context context) {
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
            publishProgress();
            return loadBusline();
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
            showLoading();
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
            // super.onPostExecute(result);
            if (result != null && result.size() > 0) {
                mAlertAdapter.updateData(result);
                mAlertAdapter.notifyDataSetChanged();
                mAlertListView.expandGroup(0);
            } else {
                // mNearHint.setVisibility(View.VISIBLE);
            }
            dismissLoading();
        }

    }

}