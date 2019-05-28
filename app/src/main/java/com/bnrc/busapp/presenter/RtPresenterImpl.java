package com.bnrc.busapp.presenter;

import android.content.Context;
import android.database.SQLException;
import android.os.Handler;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.bnrc.busapp.adapter.NearAdapter;
import com.bnrc.busapp.model.Child;
import com.bnrc.busapp.model.Group;
import com.bnrc.busapp.network.VolleyNetwork;
import com.bnrc.busapp.util.MyCipher;
import com.bnrc.busapp.util.NetAndGpsUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RtPresenterImpl implements RtPresenter {

    private static final String TAG = "RtPresenterImpl";

    private Context mContext;

    private VolleyNetwork mVolleyNetwork;

    private OkHttpClient mOkHttpClient;

    private NetAndGpsUtil mNetAndGpsUtil;

    private List<Group> mNearGroups;

    private CoordinateConverter mCoordConventer;

    private Handler mHandler;


    private RtPresenterImpl(Context mContext,Handler mHandler){
        this.mContext = mContext;
        this.mHandler = mHandler;
        mVolleyNetwork = VolleyNetwork.getInstance(mContext);
        mNetAndGpsUtil = NetAndGpsUtil.getInstance(mContext
                .getApplicationContext());
        mCoordConventer =  new CoordinateConverter();
    }

    public static RtPresenterImpl newInstance(Context mContext,Handler mHandler){
        return new RtPresenterImpl(mContext,mHandler);
    }

    @Override

    public void getServerInfo(List<Group> groups,final NearAdapter mNearAdapter) {

        this.mNearGroups = groups;

        for (Group group : groups) {
            if (group.getChildrenCount() <= 0)
                continue;
            List<Child> children = group.getChildren();
            for (final Child child : children) {
                final int LineID = child.getLineID();
                int StationID = child.getStationID();

                final int sequence = child.getSequence();
                if (sequence == 1) {
                    Map<String, String> showText = new HashMap<String, String>();
                    showText.put("itemsText", "起点站");
                    child.setRtInfo(showText);
                    child.setRtRank(Child.FIRSTSTATION);
                    child.setDataChanged(true);
                } else
                    mVolleyNetwork.getNearestBusWithLineAndOneStation(LineID,
                            StationID, new VolleyNetwork.requestListener() {

                                @Override
                                public void onSuccess(JSONObject data) {
                                    try {
                                        JSONArray arr = null;
                                        if (data.toString().indexOf("[") > 0) {
                                            arr = data.getJSONArray("dt");
                                        } else {
                                            JSONObject busJsonObject = data
                                                    .getJSONObject("dt");
                                            arr = new JSONArray("["
                                                    + busJsonObject.toString()
                                                    + "]");
                                        }
                                        if (arr != null && arr.length() > 0) {
                                            int size = arr.length();
                                            List<Map<String, ?>> list = child
                                                    .getRtInfoList();
                                            list.clear();
                                            for (int i = 0; i < size; i++) {
                                                Map<String, String> map = new HashMap<String, String>();
                                                JSONObject json = arr
                                                        .getJSONObject(i);
                                                int distance = json
                                                        .getInt("Sd");
                                                int time = json.getInt("St");
                                                int station = json.getInt("bn");
                                                if (time <= 10) {
                                                    map.put("station", "已经");
                                                    map.put("time", "到站");
                                                } else {
                                                    int tmp = time / 60;
                                                    if (tmp <= 0)
                                                        map.put("station", time
                                                                + " 秒");
                                                    else
                                                        map.put("station", tmp
                                                                + " 分");
                                                    map.put("time", station
                                                            + " 站");
                                                }
                                                list.add(map);
                                            }
                                            if (child != null) {
                                                // child.setRtInfo(showText);
                                                child.setRtRank(Child.ARRIVING);
                                                child.setDataChanged(true);
                                            }
                                        } else {
                                            Map<String, String> showText = new HashMap<String, String>();
                                            if (sequence == 1) {
                                                showText.put("itemsText", "起点站");
                                                child.setRtInfo(showText);
                                                child.setRtRank(Child.FIRSTSTATION);
                                                child.setDataChanged(true);
                                            } else {
                                                showText.put("itemsText",
                                                        "<font color=\"black\">"
                                                                + "等待发车"
                                                                + "</font>");
                                                if (child != null) {
                                                    child.setRtInfo(showText);
                                                    child.setRtRank(Child.NOTYET);
                                                    child.setDataChanged(true);
                                                }
                                            }
                                        }
                                        sortGroup();
                                        mNearAdapter.notifyDataSetChanged();
                                    } catch (JSONException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onNotAccess() {
                                    // TODO Auto-generated method stub
                                    Map<String, String> showText = new HashMap<String, String>();
                                    showText.put("itemsText",
                                            "<font color=\"grey\">" + "未开通"
                                                    + "</font>");
                                    if (child != null) {
                                        child.setRtInfo(showText);
                                        child.setRtRank(Child.NOTEXIST);
                                        child.setDataChanged(true);

                                    }
                                    sortGroup();
                                    mNearAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onFormatError() {
                                    // TODO Auto-generated method stub
                                    if (child.getOfflineID() > 0) {
                                        try {
                                            getRtInfo(child,mNearAdapter);
                                        } catch (Exception e) {
                                            // TODO Auto-generated catch block
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Map<String, String> showText = new HashMap<String, String>();
                                        showText.put("itemsText",
                                                "<font color=\"grey\">" + "未开通"
                                                        + "</font>");
                                        if (child != null) {
                                            child.setRtInfo(showText);
                                            child.setDataChanged(true);
                                            child.setRtRank(Child.NOTEXIST);
                                        }
                                        sortGroup();
                                        mNearAdapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onDataNA(String url) {
                                    // TODO Auto-generated method stub
                                    getRtInfo(child, url,mNearAdapter);
                                }

                                @Override
                                public void onNetError() {
                                    // TODO Auto-generated method stub
                                    if (child.getOfflineID() > 0) {
                                        try {
                                            getRtInfo(child,mNearAdapter);
                                        } catch (Exception e) {
                                            // TODO Auto-generated catch block
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Map<String, String> showText = new HashMap<String, String>();
                                        showText.put("itemsText",
                                                "<font color=\"grey\">" + "未开通"
                                                        + "</font>");
                                        if (child != null) {
                                            child.setRtInfo(showText);
                                            child.setDataChanged(true);
                                            child.setRtRank(Child.NOTEXIST);
                                        }
                                        sortGroup();
                                        mNearAdapter.notifyDataSetChanged();
                                    }
                                }
                            });
            }
        }

    }

    public void getRtInfo(final Child child,final NearAdapter mNearAdapter) throws
            UnsupportedEncodingException {
        final int sequence = child.getSequence();
        int offlineID = child.getOfflineID();
        String Url =
                "http://223.72.210.21:8512/ssgj/bus.php?city="
                        + URLEncoder.encode("北京", "utf-8") + "&id=" + offlineID
                        + "&no=" + sequence + "&type=2&encrypt=1&versionid=2";
        Log.i("Test single getRtInfo", "url:" + Url);// 创建okHttpClient对象
        final List<Map<String, ?>> tmp = child.getRtInfoList();
        // 创建一个Request
        final Request request = new Request.Builder().url(Url).build();
        // new call
        if(mOkHttpClient==null){
            mOkHttpClient = new OkHttpClient();
        }
        Call call = mOkHttpClient.newCall(request);
        // 请求加入调度
        call.enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException arg1) {
                // TODO Auto-generated method stub

                if (child != null && tmp != null && tmp.size() == 0
                        || !mNetAndGpsUtil.isNetworkAvailable()) {
                    Map<String, String> showText = new HashMap<String, String>();
                    if (sequence == 1) {
                        showText.put("itemsText", "起点站");
                        child.setRtInfo(showText);
                        child.setRtRank(Child.FIRSTSTATION);
                        child.setDataChanged(true);
                    } else {
                        showText.put("itemsText", "等待发车");
                        // 到站
                        child.setRtInfo(showText);
                        child.setDataChanged(true);
                        child.setRtRank(Child.NOTYET);
                    }
                }
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        mNearAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response res)
                    throws IOException {
                // TODO Auto-generated method stub
                try {
                    String response = res.body().string();
//					Log.i("OKHTTP", "response " + response);
                    JSONObject responseJson = XML.toJSONObject(response);
                    JSONObject rootJson = responseJson.getJSONObject("root");
                    int status = rootJson.getInt("status");
                    if (status != 200) {
                        if (child != null) {
                            Map<String, String> showText = new HashMap<String, String>();
                            if (sequence == 1) {
                                showText.put("itemsText", "起点站");
                                child.setRtInfo(showText);
                                child.setRtRank(Child.FIRSTSTATION);
                                child.setDataChanged(true);
                            } else {
                                showText.put("itemsText", "等待发车");
                                // 到站
                                child.setRtInfo(showText);
                                child.setDataChanged(true);
                                child.setRtRank(Child.NOTYET);
                            }
                        }
                        sortGroup();
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                mNearAdapter.notifyDataSetChanged();
                            }
                        });
                        return;
                    }
                    JSONObject dataJson = rootJson.getJSONObject("data");
                    JSONArray busJsonArray = null;
                    if (dataJson.toString().indexOf("[") > 0) {
                        busJsonArray = (JSONArray) dataJson.get("bus");
                        busJsonArray = dataJson.getJSONArray("bus");
                    } else {
                        JSONObject busJsonObject = dataJson
                                .getJSONObject("bus");
                        busJsonArray = new JSONArray("["
                                + busJsonObject.toString() + "]");
                    }
                    dealRtInfo(busJsonArray, child,mNearAdapter);
                    Log.i(TAG, child.getLineName() + " 成功请求到了信息！！！！！！");
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    Log.i(TAG, "是200: exception " + child.getLineName());
                    if (child != null && tmp != null && tmp.size() == 0
                            || !mNetAndGpsUtil.isNetworkAvailable()) {
                        Map<String, String> showText = new HashMap<String, String>();
                        if (sequence == 1) {
                            showText.put("itemsText", "起点站");
                            child.setRtInfo(showText);
                            child.setRtRank(Child.FIRSTSTATION);
                            child.setDataChanged(true);
                        } else {
                            showText.put("itemsText", "等待发车");
                            // 到站
                            child.setRtInfo(showText);
                            child.setDataChanged(true);
                            child.setRtRank(Child.NOTYET);
                        }
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            mNearAdapter.notifyDataSetChanged();
                        }
                    });
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 有URL传入
     * @param child
     * @param url
     */
    public void getRtInfo(final Child child, String url,final NearAdapter mNearAdapter) {
        final int sequence = child.getSequence();
        Log.i("Test double getRtInfo", "url:" + url);
        // 创建okHttpClient对象
        // 创建一个Request
        final Request request = new Request.Builder()
                .url(url).build();
        final List<Map<String, ?>> tmp = child.getRtInfoList();
        // new call
        if(mOkHttpClient==null){
            mOkHttpClient = new OkHttpClient();
        }
        Call call = mOkHttpClient.newCall(request);
        // 请求加入调度
        call.enqueue(new Callback() {

            @Override
            public void onFailure(Call call,
                                  IOException arg1) {
                // TODO Auto-generated method stub
                if (child != null && tmp != null && tmp.size() == 0
                        || !mNetAndGpsUtil.isNetworkAvailable()) {
                    Map<String, String> showText = new HashMap<String, String>();
                    if (sequence == 1) {
                        showText.put("itemsText", "起点站");
                        child.setRtInfo(showText);
                        child.setRtRank(Child.FIRSTSTATION);
                        child.setDataChanged(true);
                    } else {
                        showText.put("itemsText", "等待发车");
                        // 到站
                        child.setRtInfo(showText);
                        child.setRtRank(Child.NOTYET);
                        child.setDataChanged(true);
                    }

                }
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        mNearAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response res)
                    throws IOException {
                // TODO Auto-generated method stub
                try {
                    String response = res.body().string();
                    JSONObject responseJson = XML.toJSONObject(response);
                    JSONObject rootJson = responseJson.getJSONObject("root");
                    int status = rootJson.getInt("status");
                    if (status != 200) {
                        if (child != null) {
                            Map<String, String> showText = new HashMap<String, String>();
                            if (sequence == 1) {
                                showText.put("itemsText", "起点站");
                                child.setRtInfo(showText);
                                child.setRtRank(Child.FIRSTSTATION);
                                child.setDataChanged(true);
                            } else {
                                showText.put("itemsText", "等待发车");
                                // 到站
                                child.setRtInfo(showText);
                                child.setDataChanged(true);

                                child.setRtRank(Child.NOTYET);
                            }
                        }
                        sortGroup();
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                mNearAdapter.notifyDataSetChanged();
                            }
                        });
                        return;
                    }
                    JSONObject dataJson = rootJson.getJSONObject("data");
                    JSONArray busJsonArray = null;
                    if (dataJson.toString().indexOf("[") > 0) {
                        busJsonArray = (JSONArray) dataJson.get("bus");
                        busJsonArray = dataJson.getJSONArray("bus");
                    } else {
                        JSONObject busJsonObject = dataJson
                                .getJSONObject("bus");
                        busJsonArray = new JSONArray("["
                                + busJsonObject.toString() + "]");
                    }
                    dealRtInfo(busJsonArray, child,mNearAdapter);
                } catch (Exception e) {
                    // TODO Auto-generated catch block

                    if (child != null && tmp != null && tmp.size() == 0
                            || !mNetAndGpsUtil.isNetworkAvailable()) {
                        Map<String, String> showText = new HashMap<String, String>();
                        if (sequence == 1) {
                            showText.put("itemsText", "起点站");
                            child.setRtInfo(showText);
                            child.setRtRank(Child.FIRSTSTATION);
                            child.setDataChanged(true);
                        } else {
                            showText.put("itemsText", "等待发车");
                            // 到站
                            child.setRtInfo(showText);
                            child.setDataChanged(true);

                            child.setRtRank(Child.NOTYET);
                        }
                    }
                    sortGroup();
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            mNearAdapter.notifyDataSetChanged();
                        }
                    });
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * 在这里将返回的json数据转换为具体的距离几站，多少时间。
     * @param json
     * @param child
     */
    private void dealRtInfo(JSONArray json, final Child child, final NearAdapter mNearAdapter) {
        Map<String, String> showText = new HashMap<String, String>();
        showText.put("itemsText", "等待发车");
        int rank = Child.NOTYET;
        int sequence = child.getSequence();
        try {
            try {
                int count = json.length();
                JSONObject uploadJson = new JSONObject();
                JSONArray uploadData = new JSONArray();
                uploadJson.put("c", "beijing");
                uploadJson.put("dt", uploadData);
                int max = 0;
                List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
                // Map<String, Object> map = new HashMap<String, Object>();
                for (int j = 0; j < count; j++) {
                    JSONObject busJson = (JSONObject) json.get(j);
                    JSONObject uplodaItem = new JSONObject();
                    MyCipher mCiper = new MyCipher("aibang"
                            + busJson.getString("gt"));

                    String nextStationName = mCiper.decrypt(busJson
                            .getString("ns"));// nextStationName
                    int nextStationNum = Integer.parseInt(mCiper
                            .decrypt(busJson.getString("nsn")));// nextStationNum
                    int id = busJson.getInt("id");
                    String nextStationDistance = busJson.getString("nsd");// nextStationDistance
                    String nextStationTime = busJson.getString("nst");// nextStationTime

                    String stationDistance = mCiper.decrypt(busJson
                            .getString("sd"));// stationDistance
                    String stationArrivingTime = mCiper.decrypt(busJson
                            .getString("st"));
                    String st_c = null;
                    if (isNumeric(stationArrivingTime))
                        st_c = String.valueOf(TimeStampToDelTime(Long
                                .parseLong(stationArrivingTime)));// station_arriving_time
                    else
                        st_c = "-1";
                    String x = mCiper.decrypt(busJson.getString("x"));
                    String y = mCiper.decrypt(busJson.getString("y"));
                    uplodaItem.put("LID", child.getLineID());
                    uplodaItem.put("BID",
                            child.getLineID() + String.format("%02d", j + 1));
                    uplodaItem.put("Nsn", nextStationNum);
                    uplodaItem.put("Nsd", nextStationDistance);
                    LatLng latLngBaidu = mCoordConventer
                            .from(CoordinateConverter.CoordType.COMMON)
                            .coord(new LatLng(Double.parseDouble(y), Double
                                    .parseDouble(x))).convert();
                    uplodaItem.put("Lat", latLngBaidu.latitude);
                    uplodaItem.put("Lon", latLngBaidu.longitude);
                    uplodaItem.put("T", System.currentTimeMillis() / 1000);
                    uploadData.put(uplodaItem);
                    if (nextStationNum <= sequence) {
                        // map.clear();
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("nextStationNum", nextStationNum);
                        map.put("stationArrivingTime", stationArrivingTime);
                        map.put("stationDistance", stationDistance);
                        max = nextStationNum;
                        list.add(map);
                    }
                }
                mVolleyNetwork.upLoadRtInfo(uploadJson, new VolleyNetwork.upLoadListener() {

                    @Override
                    public void onSuccess() {
                        // TODO Auto-generated method stub
                        Log.i(TAG, child.getLineName() + " 上传成功");
                    }

                    @Override
                    public void onFail() {
                        // TODO Auto-generated method stub
                        Log.i(TAG, child.getLineName() + " 上传失败");
                    }
                });

                List<Map<String, ?>> tmpList = child.getRtInfoList();
                tmpList.clear();
                Log.i(TAG,
                        "busJsonArray_count: " + list.size() + " "
                                + child.getLineName());

                if (list.size() <= 0) {
                    // 起点站
                    if (sequence == 1) {
                        showText.put("itemsText", "起点站");
                        rank = Child.FIRSTSTATION;
                    } // 未开通
                    else {
                        showText.put("itemsText", "等待发车");
                        rank = Child.NOTYET;
                    }
                    // throw new JSONException("等待发车");
                } else {
                    Collections.sort(list, comparatorRt);
                    int size = list.size() > 3 ? 3 : list.size();
                    for (int i = 0; i < size; i++) {
                        Map<String, ?> map = list.get(i);
                        int nextStationNum = (Integer) map
                                .get("nextStationNum");
                        String stationArrivingTime = map.get(
                                "stationArrivingTime").toString();
                        String stationDistance = map.get("stationDistance")
                                .toString();
                        Map<String, String> tmpMap = new HashMap<String, String>();
                        if (isNumeric(stationArrivingTime)) {
                            if (nextStationNum == sequence) {
                                // 已到站
                                if (Integer.parseInt(stationArrivingTime) < 10) {
                                    tmpMap.put("station", "已经");
                                    tmpMap.put("time", "到站");
                                    rank = Child.ARRIVING;
                                }
                                // 即将到站
                                else if (Integer.parseInt(stationDistance) < 10) {
                                    tmpMap.put("station", "即将");
                                    tmpMap.put("time", "到站");
                                    rank = Child.SOON;
                                } else {
                                    int nstime = TimeStampToDelTime(Long
                                            .parseLong(stationArrivingTime));// 计算还有几分钟
                                    if (nstime <= 0) {
                                        tmpMap.put("station", "即将");
                                        tmpMap.put("time", "到站");
                                        rank = Child.SOON;
                                    } else {
                                        tmpMap.put("station", 1 + " 站");
                                        tmpMap.put("time", nstime + " 分");
                                        rank = Child.ONTHEWAY;
                                    }
                                }
                            } else {
                                int nstime = TimeStampToDelTime(Long
                                        .parseLong(stationArrivingTime));// 计算还有几分钟
                                if (nstime <= 0) {
                                    tmpMap.put("station", "即将");
                                    tmpMap.put("time", "到站");
                                    rank = Child.SOON;
                                } else {
                                    tmpMap.put("station", (sequence
                                            - nextStationNum + 1)
                                            + " 站");
                                    tmpMap.put("time", nstime + " 分");
                                    rank = Child.ONTHEWAY;
                                }
                            }
                        }
                        tmpList.add(tmpMap);
                    }
                }
            } catch (SQLException sqle) {
                throw sqle;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (child != null) {
                child.setRtInfo(showText);
                child.setDataChanged(true);
                child.setRtRank(rank);
            }
            sortGroup();
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    mNearAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private int TimeStampToDelTime(Long timestampString) {
        if (timestampString < 0)
            return (int) 0;
        double delTime = (timestampString * 1000 - System.currentTimeMillis()) / 1000.0 / 60.0;
        return (int) Math.ceil(delTime);
    }

    private boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("-?[0-9]+.*[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    /**
     * 自定义比较器
     */
    private void sortGroup() {
        for (Group group : mNearGroups)
            Collections.sort(group.getChildren(), comparator);
    }

    Comparator<Child> comparator = new Comparator<Child>() {
        public int compare(Child c1, Child c2) {

            if (c1 == null && c2 == null)
                return 0;
            else if (c1 == null)
                return -1;
            else if (c2 == null)
                return 1;
            int rank1 = c1.getRtRank();
            int rank2 = c2.getRtRank();
            if (rank1 > rank2)
                return -1;
            else if (rank1 < rank2)
                return 1;
            else
                return 0;
        }
    };

    Comparator<Map<String, ?>> comparatorRt = new Comparator<Map<String, ?>>() {
        public int compare(Map<String, ?> c1, Map<String, ?> c2) {

            int n1 = Integer.parseInt(c1.get("nextStationNum").toString());
            int n2 = Integer.parseInt(c2.get("nextStationNum").toString());
            if (n1 > n2)
                return -1;
            else if (n1 < n2)
                return 1;
            return 0;

        }
    };
}
