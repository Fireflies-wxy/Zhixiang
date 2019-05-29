package com.bnrc.busapp.model.bus;


import com.bnrc.busapp.model.BaseModel;

import java.util.ArrayList;

/**
 * Created by apple on 2018/6/18.
 */

public class BusModel extends BaseModel {
    public int linestatus;
    public int stationstatus;
    public ArrayList<BusInfo> lineinfo;

    //失败时返回数据
    public String msg;
    public int errorCode;
    public String requestURL;
}
