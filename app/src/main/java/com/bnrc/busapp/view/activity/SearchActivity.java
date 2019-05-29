package com.bnrc.busapp.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.bnrc.busapp.adapter.MyLatestListViewAdapter;
import com.bnrc.busapp.constant.Constants;
import com.bnrc.busapp.database.PCDataBaseHelper;
import com.bnrc.busapp.database.PCUserDataDBHelper;
import com.bnrc.busapp.listener.ItemDelListener;

import java.util.ArrayList;
import java.util.List;

import com.bnrc.busapp.R;
import com.bnrc.busapp.model.historyItem;
import com.bnrc.busapp.util.AnimationUtil;
import com.bnrc.busapp.view.activity.base.BaseActivity;

public class SearchActivity extends BaseActivity implements ItemDelListener {
    private static final String TAG = SearchActivity.class.getSimpleName();
    private ImageView mDeleteView;
    private EditText mSearchEdt;
    private Button mSearchBtn;
    private ListView mSearchListView;
    private RelativeLayout mNoHistory;
    public PCDataBaseHelper mSearchDB = null;
    public PCUserDataDBHelper mUserDB = null;
    public List<historyItem> mListData;
    public List<historyItem> mTempData;
    private MyLatestListViewAdapter mListViewAdapter;
    private List<historyItem> mBuslines;
    private List<historyItem> mStations;
    private List<historyItem> mHistory;
    private RelativeLayout mAdContainer;

    private TextView tv_search_title, search_back_view;

    private View footerView;

    private boolean isAR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        register();
        setListener();
        mSearchEdt.setFocusable(true);
        mSearchEdt.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        // 接受软键盘输入的编辑文本或其它视图
        inputMethodManager.showSoftInput(mSearchEdt,
                InputMethodManager.SHOW_FORCED);
        mSearchDB = PCDataBaseHelper.getInstance(SearchActivity.this);
        mUserDB = PCUserDataDBHelper.getInstance(SearchActivity.this);
        mListData = new ArrayList<>();
        mTempData = new ArrayList<>();
        mHistory = new ArrayList<>();
        mListViewAdapter = new MyLatestListViewAdapter(this, mListData,
                R.layout.list_items, new String[] { "itemsIcon", "itemsTitle",
                "itemsText", "itemsDel" }, new int[] { R.id.itemsIcon,
                R.id.tv_stationName, R.id.itemsText, R.id.iv_delete });
        mSearchListView.setAdapter(mListViewAdapter);

        tv_search_title = findViewById(R.id.tv_search_title);
        search_back_view = findViewById(R.id.search_back_view);
        search_back_view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        isAR = getIntent().getBooleanExtra("isAR",false);

        loadHistoryData();
    }

    private void register() {
        mDeleteView = (ImageView) findViewById(R.id.iv_delete);
        mSearchEdt = (EditText) findViewById(R.id.edt_input);
        mSearchBtn = (Button) findViewById(R.id.btn_search);
        mSearchListView = (ListView) findViewById(R.id.mBuslineListView);
        mNoHistory = (RelativeLayout) findViewById(R.id.rLayout_search_history);
    }

    private void setListener() {
        mDeleteView.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                mSearchEdt.setText("");
                loadHistoryData();
            }
        });

        mSearchEdt.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // TODO Auto-generated method stub
                if (s.length() >= 1) {
                    // getBuslineWithKeyword(mSearchEdt.getText().toString());
                }

            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
                // loadHistoryData();
            }

            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    mDeleteView.setVisibility(View.GONE);
                    loadHistoryData();
                } else {
                    mDeleteView.setVisibility(View.VISIBLE);
                }
            }
        });

        mSearchBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                InputMethodManager imm = (InputMethodManager) getSystemService(SearchActivity.this.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mSearchEdt.getWindowToken(), 0);
                if (mSearchEdt.getText().length() < 1) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "线路名称不能为空哦~", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    getBuslineWithKeyword(mSearchEdt.getText().toString());
                }
            }
        });
        mSearchListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                historyItem item = mListData.get(position);
                switch (item.getType()) {
                    case Constants.STATION:
                        if(!isAR){
                            Intent stationIntent = new Intent(SearchActivity.this,
                                    StationListActivity.class);
                            stationIntent.putExtra("StationName", item.getStationName());
                            startActivity(stationIntent);
                            AnimationUtil.activityZoomAnimation(SearchActivity.this);
                            item.setType(Constants.STATION);
                            mUserDB.addSearchRecord(item);
                        }else {
                            Intent stationIntent = new Intent();
                            stationIntent.putExtra("searchType",1); //1代表站点
                            stationIntent.putExtra("stationName",item.getStationName());
                            stationIntent.putExtra("stationLoc", new LatLng(item.getLatitude(),item.getLongitude()));
                            setResult(RESULT_OK, stationIntent);
                            hidenAndFinish();
                        }

                        break;
                    case Constants.BUSLINE:
                        if(!isAR){
                            Intent buslineIntent = new Intent(SearchActivity.this,
                                    BuslineListActivity.class);
                            int LineID = item.getLineID();
                            int StationID = 0;
                            int Sequence = 1;
                            buslineIntent.putExtra("LineID", LineID);
                            buslineIntent.putExtra("StationID", StationID);
                            buslineIntent.putExtra("Sequence", Sequence);
                            startActivity(buslineIntent);
                            AnimationUtil.activityZoomAnimation(SearchActivity.this);
                            item.setType(Constants.BUSLINE);
                            mUserDB.addSearchRecord(item);
                        }else {
                            Intent buslineIntent = new Intent();
                            int LineID = item.getLineID();
                            int StationID = 0;
                            String StationName = "";
                            if(mSearchDB.acquireBusLinesWithKeyword(item.getLineName()).size()!=0){
                                StationID = mSearchDB.acquireStationsWithBuslineID(LineID).get(1).getStationID();
                                StationName = mSearchDB.acquireStationsWithBuslineID(LineID).get(0).getStationName();

                            }

                            Log.i(TAG, "onItemClick: LName: "+item.getLineName()+" StationName: "+StationName);
                            Log.i(TAG, "onItemClick: LineID: "+LineID+" StationID: "+StationID);
                            buslineIntent.putExtra("searchType",2);//2代表线路
                            buslineIntent.putExtra("LineID", 1064500);
                            buslineIntent.putExtra("StationID", StationID);
                            setResult(RESULT_OK, buslineIntent);
                            hidenAndFinish();
                        }

                        break;
                    default:
                        break;

                }
            };
        });
    }

    public void loadHistoryData() {
        mListData.clear();
        mHistory = mUserDB.acquireLatestSearchHistory();
        if (footerView != null && mSearchListView.getFooterViewsCount() != 0)
            try {
                // 每次总是先remove掉FooterView
                mSearchListView.removeFooterView(footerView);
            } catch (Exception e) {

            }
        mListData.addAll(mHistory);
        if (mListData.size() > 0) {

            if (footerView != null
                    && mSearchListView.getFooterViewsCount() != 0)
                try {
                    // 每次总是先remove掉FooterView
                    mSearchListView.removeFooterView(footerView);

                } catch (Exception e) {

                }
            footerView = LayoutInflater.from(this).inflate(
                    R.layout.footer_clear, null);
            mSearchListView.addFooterView(footerView);
            mNoHistory.setVisibility(View.GONE);
            footerView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View paramView) {
                    // TODO Auto-generated method stub
                    mUserDB.delAllSearchRecord();
                    mListData.clear();
                    mListViewAdapter.notifyDataSetChanged();
                    mSearchListView.removeFooterView(footerView);
                    mNoHistory.setVisibility(View.VISIBLE);
                }
            });
        } else
            mNoHistory.setVisibility(View.VISIBLE);
        mListViewAdapter.setDelListener(this);
        mListViewAdapter.notifyDataSetChanged();
    }

    public void getBuslineWithKeyword(String keyword) {
        mListData.clear();
        mTempData.clear();
        mBuslines = mSearchDB.acquireBusLinesWithKeyword(keyword);
        mStations = mSearchDB.acquireStationsWithStationKeyword(keyword);
        // mTitleTextView.setText("共搜索到"+j+"条关于\""+etSearch.getText().toString()+"\"的公交线路·");
        if (getNumWithStr(keyword) > 0) {
            mTempData.addAll(mBuslines);
            mTempData.addAll(mStations);
        } else {
            mTempData.addAll(mStations);
            mTempData.addAll(mBuslines);
        }

        if (footerView != null && mSearchListView.getFooterViewsCount() != 0)
            try {
                // 每次总是先remove掉FooterView
                mSearchListView.removeFooterView(footerView);

            } catch (Exception e) {

            }
        if (mTempData.size() > 10) {
            mListData.addAll(mTempData.subList(0, 10));
            footerView = LayoutInflater.from(this).inflate(
                    R.layout.footer_more, null);
            mSearchListView.addFooterView(footerView);
            footerView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View paramView) {
                    // TODO Auto-generated method stub
                    mListData.clear();
                    mListData.addAll(mTempData);
                    mSearchListView.removeFooterView(footerView);
                    mListViewAdapter.notifyDataSetChanged();
                }
            });
        } else {
            mListData.clear();
            mListData.addAll(mTempData);
            mListViewAdapter.notifyDataSetChanged();
        }
        mNoHistory.setVisibility(View.GONE);
        mListViewAdapter.notifyDataSetChanged();
        if (mListData == null || mListData.size() <= 0)
            Toast.makeText(getApplicationContext(), "没有相关站点或线路，请重新输入关键字！", Toast.LENGTH_SHORT)
                    .show();
    }

    public int getNumWithStr(String str) {
        str = str.trim();
        String str2 = "-1";
        if (str != null && !"".equals(str)) {
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) >= 48 && str.charAt(i) <= 57) {
                    str2 += str.charAt(i);
                }
            }
        }
        return Integer.parseInt(str2);
    }


    @Override
    public void onResume() {
        super.onResume();
        // getSearchList();
        //MobclickAgent.onPageStart("SplashScreen"); // ͳ��ҳ��
        //MobclickAgent.onResume(this); // ͳ��ʱ��
        // registerReceiver(mWifiReceiver, wifiFilter);
        // registerReceiver(mActivityReceiver, activityFilter);
        Log.i(TAG, "onResume");

    }

    @Override
    public void onRestart() {
        super.onRestart();
        // loadHistoryData();
        Log.i(TAG, "onRestart");
        // 获取编辑框焦点
        mSearchEdt.setFocusable(true);

        // 打开软键盘
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onPause() {
        super.onPause();
        //MobclickAgent.onPageEnd("SplashScreen");
        //MobclickAgent.onPause(this);
        // unregisterReceiver(mWifiReceiver);
        // unregisterReceiver(mActivityReceiver);

        // 关闭软键盘
        mSearchEdt.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchEdt.getWindowToken(), 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onDelItem(int position) {
        // TODO Auto-generated method stub
        // ArrayList<String> delRecord = new ArrayList<String>();
        historyItem item = mListData.get(position);
        mUserDB.delOneSearchRecordByType(item);
        mListData.remove(position);
        if (mListData.size() == 0 && footerView != null
                && mSearchListView.getFooterViewsCount() != 0) {
            mNoHistory.setVisibility(View.VISIBLE);
            mSearchListView.removeFooterView(footerView);
        }
        mListViewAdapter.notifyDataSetChanged();
    }

    private void hidenAndFinish() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(SearchActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
        finish();
    }

}
