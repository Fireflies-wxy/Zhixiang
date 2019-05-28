package com.bnrc.busapp.view.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.bnrc.busapp.R;
import com.bnrc.busapp.adapter.SearchPOIListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SugSearchActivity extends AppCompatActivity implements OnGetPoiSearchResultListener {

    private static final String TAG = "SugSearchActivity";
    
    private List<PoiInfo> mPOIList = new ArrayList<>();

    /**
     * 搜索结果的适配器
     */
    private SearchPOIListAdapter mSearchPOIListAdapter;

    /**
     * 搜索结果列表
     */
    private ListView mSearchListView;

    private ImageView mDeleteView;
    private TextView icon_back;
    private EditText mSearchEditText;
    private LinearLayout mSearchNoResult;

    private PoiSearch mPoiSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sug_search);

        initview();

        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);
    }

    private void initview(){
        mSearchEditText = findViewById(R.id.edt_input);
        mSearchEditText.addTextChangedListener(mTextWatcher);

        mSearchListView = findViewById(R.id.search_lv_suggest);
        mSearchListView.setOnItemClickListener(mSearchListItemClickListener);

        mSearchNoResult =  findViewById(R.id.search_no_result);
        mDeleteView = findViewById(R.id.iv_delete);

        icon_back = findViewById(R.id.search_back_view);
        icon_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hidenAndFinish();
            }
        });

        final Intent intent = getIntent();
        if (intent != null) {
            if(!intent.getStringExtra("poiname").equals("")){
                mSearchEditText.setHint("我的位置: "+intent.getStringExtra("poiname"));
            }else {
                mSearchEditText.setHint("请输入终点");
            }
        }

        mSearchEditText.setFocusable(true);
        mSearchEditText.setFocusableInTouchMode(true);
        mSearchEditText.requestFocus();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) mSearchEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(mSearchEditText,0);
            }
        },100);

    }

    public static void startSearchActivityForResult(FragmentActivity activity,String poiname,int requestCode){
        Intent intent = new Intent(activity, SugSearchActivity.class);
        intent.putExtra("poiname",poiname);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 监听搜索框的文字变化
     */
    private TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String content = s.toString();
            if (!TextUtils.isEmpty(content)) {
                Log.i(TAG, "afterTextChanged: ");
                mPoiSearch.searchInCity((new PoiCitySearchOption())
                        .city("北京").keyword(mSearchEditText.getText().toString())
                        .isReturnAddr(true)
                        .pageNum(0));
            }
        }
    };

    private AdapterView.OnItemClickListener mSearchListItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Intent intent = new Intent();
            intent.putExtra("PoiInfo", mPOIList.get(position));
            setResult(RESULT_OK, intent);
            hidenAndFinish();
        }
    };

    private void hidenAndFinish() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(SugSearchActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
        finish();
    }

    private void updatePoiList(PoiResult poiResult) {
        mPOIList.clear();
        mSearchNoResult.setVisibility((poiResult != null && poiResult.getAllPoi().size() > 0) ? View.GONE : View.VISIBLE);

        if (poiResult != null && poiResult.getAllPoi().size() > 0) {
            for (PoiInfo nPoi : poiResult.getAllPoi()) {
                mPOIList.add(nPoi);
            }
        }

        if (mSearchPOIListAdapter == null) {
            mSearchPOIListAdapter = new SearchPOIListAdapter(SugSearchActivity.this, mPOIList);
            mSearchListView.setAdapter(mSearchPOIListAdapter);
        } else {
            mSearchPOIListAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onGetPoiResult(PoiResult poiResult) {

        if (poiResult == null || poiResult.error != SearchResult.ERRORNO.NO_ERROR){
            if (mSearchPOIListAdapter == null) {
                mSearchPOIListAdapter = new SearchPOIListAdapter(SugSearchActivity.this, mPOIList);
                mSearchListView.setAdapter(mSearchPOIListAdapter);
            } else {
                mSearchPOIListAdapter.notifyDataSetChanged();
            }
            mSearchNoResult.setVisibility(View.VISIBLE);
        }else {
            updatePoiList(poiResult);
        }

    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }

    @Override
    protected void onDestroy() {
        mPoiSearch.destroy();
        super.onDestroy();
    }


}
