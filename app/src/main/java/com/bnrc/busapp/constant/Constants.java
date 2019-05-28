package com.bnrc.busapp.constant;

import android.Manifest;
import android.os.Environment;

/**
 * @author: vision
 * @function:
 * @date: 18/6/16
 */
public class Constants {

    /**
     * 权限常量相关
     */
    public static final int WRITE_READ_EXTERNAL_CODE = 0x01;
    public static final String[] WRITE_READ_EXTERNAL_PERMISSION = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    public static final int HARDWEAR_CAMERA_CODE = 0x02;
    public static final String[] HARDWEAR_CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};

    public static final int ACCESS_LOCATION_CODE = 0x03;
    public static final String[] ACCESS_LOCATION_PERMISSION = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

    public static final int READ_PHONE_STATE_CODE = 0x04;
    public static final String[] READ_PHONE_STATE_PERMISSION = new String[]{Manifest.permission.READ_PHONE_STATE};


    //整个应用文件下载保存路径
    public static String APP_PHOTO_DIR = Environment.
            getExternalStorageDirectory().getAbsolutePath().
            concat("/bnrcbus/photo/");

    /* 多盟账号id */
    public static final String PUBLISHER_ID = "56OJyM1ouMGoaSnvCK";
    public static final String InlinePPID = "16TLwebvAchksY6iO_8oSb-i";
    /* 扫描wifi的一些常量 */
    public static final String category_str1[] = new String[] { "   北京市   ",
            "  上海市  ", "  广州市 ", "  深圳市  ", "  天津市  ", "  南京市  ", "  杭州市  ",
            "  大连市  ", "  青岛市  " };
    public static final String category_str2[] = new String[] { "  16wifi   ",
            " BNRC-Air  ", " BUPT-1  " };
    public static final String UPDATE_ACTION = "com.bnrc.bnrcbus.action.UPDATE_ACTION";
    public static final String SERVICE_ACTION = "com.bnrc.bnrcbus.scanservice";
    public static final String SHAREPRF_ACTION = "com.bnrc.bnrcbus.setting_broad";
    public static final String SSIDSELECT_ACTION = "com.bnrc.bnrcbus.ui_listener_mySelectTouchListener";
    public static final String SETTING = "SETTING";
    public static final String SETTING_BAT = "SETTING_Battery";
    public static final String SETTING_RAD = "SETTING_Radius";
    public static final String SETTING_ALE = "SETTING_AlertDistance";

    public static final String SETTING_MET = "SETTING_Method";
    public static final String SETTING_AP = "SETTING_Ap";
    public static final String SETTING_FRE = "SETTING_Frequency";
    public static final String SETTING_PRECISION = "SETTING_Precision";
    public static final int TYPE_NONE = 0;// 全部标签
    public static final int TYPE_ALL = 1;// 全部标签
    public static final int TYPE_WORK = 2;// 工作标签
    public static final int TYPE_HOME = 3;// 回家标签
    public static final int TYPE_OTHER = 4;// 其他标签
    //	public static final int TYPE_DEL = 5;// 取消收藏标签
    public static final int BUSLINE = 1;// 站点
    public static final int STATION = 2;// 线路
}
