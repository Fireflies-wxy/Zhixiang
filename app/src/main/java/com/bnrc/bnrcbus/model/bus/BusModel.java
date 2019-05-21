package com.bnrc.bnrcbus.model.bus;

/**
 * Created by apple on 2018/6/18.
 */

public class BusModel {
    public String Distance;
    public String Time;
    public String Beforethis;
    public String NextStationNum;
    public String NextStationDistance;
    public float Lat;
    public float Lon;

    //失败时返回数据
    public String msg;
    public String errorCode;
    public String requestURL;
}
