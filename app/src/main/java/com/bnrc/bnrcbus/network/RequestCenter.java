package com.bnrc.bnrcbus.network;



import com.bnrc.bnrcbus.model.bus.BusModel;
import com.bnrc.bnrcbus.model.user.LoginInfo;
import com.bnrc.bnrcbus.model.user.RegisterInfo;
import com.bnrc.bnrcbus.model.user.TokenInfo;
import com.bnrc.bnrcbus.model.version.VersionModel;
import com.bnrc.bnrcbus.network.listener.DisposeDataHandler;
import com.bnrc.bnrcbus.network.listener.DisposeDataListener;
import com.bnrc.bnrcbus.network.request.CommonRequest;
import com.bnrc.bnrcbus.network.request.RequestParams;

import java.util.Date;

/**
 * Created by apple on 2018/5/31.
 */

public class RequestCenter {

    private static final String TAG = "RequestCenter";

    //根据参数发送所有post请求
    public static void dealPostRequest(String url, RequestParams params, DisposeDataListener listener, Class<?> clazz) {
        CommonOkHttpClient.post(CommonRequest.
                createPostRequest(url, params), new DisposeDataHandler(listener, clazz));
    }

    //处理get请求
    public static void dealGetRequest(String url, RequestParams params, DisposeDataListener listener, Class<?> clazz) {
        CommonOkHttpClient.get(CommonRequest.
                createGetRequest(url, params), new DisposeDataHandler(listener, clazz));
    }


    public static void requestVersionData(DisposeDataListener listener) {

        RequestParams params = new RequestParams();
        params.put("V", "2.3");
        params.put("appV", "1.4");
        params.put("Tm","1");

        RequestCenter.dealPostRequest(HttpConstants.VERSION_URL, params, listener, VersionModel.class);
    }

    public static void requestBusData(DisposeDataListener listener) {

        RequestParams params = new RequestParams();
        params.put("SID", "1000020761");
        params.put("LID", "1038700");
        params.put("T", Long.toString(new Date().getTime()));

        RequestCenter.dealPostRequest(HttpConstants.BUS_URL, params, listener, BusModel.class);
    }

    public static void register(String uid, String pwd, DisposeDataListener listener) {
        RequestParams params = new RequestParams();

        params.put("username",uid);
        params.put("password",pwd);
        params.put("usertype","1"); //暂定为0，以后添加QQ、微博

        RequestCenter.dealPostRequest(HttpConstants.REGISTER_URL,params,listener, RegisterInfo.class);
    }

    public static void login(String uid, String pwd, DisposeDataListener listener) {
        RequestParams params = new RequestParams();

        params.put("username",uid);
        params.put("password",pwd);

        RequestCenter.dealPostRequest(HttpConstants.LOGIN_URL,params,listener, LoginInfo.class);
    }

    public static void checkToken(String uid, String token, DisposeDataListener listener) {
        RequestParams params = new RequestParams();

        params.put("username",uid);
        params.put("token",token);

        RequestCenter.dealPostRequest(HttpConstants.TOKEN_URL,params,listener, TokenInfo.class);
    }

    public static void submitRate(String uid, int lineID, int stationID, int lineRate, int stationRate, DisposeDataListener listener) {

//        RequestParams params = new RequestParams();
//        params.put("SID", "1000020761");
//        params.put("LID", "1038700");
//        params.put("T", Long.toString(new Date().getTime()));
//
//        RequestCenter.dealPostRequest(HttpConstants.BUS_URL, params, listener, BusModel.class);
    }

}
