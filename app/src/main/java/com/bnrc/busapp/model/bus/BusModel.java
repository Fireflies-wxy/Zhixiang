package com.bnrc.busapp.model.bus;

import com.baidu.mapapi.search.core.BusInfo;

/**
 * Created by apple on 2018/6/18.
 */

public class BusModel {
    public int linestatus;
    public int stationstatus;
    public BusInfo busInfo;

    //失败时返回数据
    public String msg;
    public int errorCode;
    public String requestURL;
}
