package com.bnrc.bnrcbus.network;

/**
 * Created by apple on 2018/5/31.
 * @function: 所有请求相关地址
 */

public class HttpConstants {

    private static final String ROOT_URL = "http://39.96.3.214:8080";

    //返回错误码类型
    protected static final int PARAMETER_ERROR = 10000;
    protected static final int EMPTY_BUS_ERROR = 20000;
    protected static final int RELATION_QUERY_ERROR = 20001;
    protected static final int LINE_QUERY_ERROR = 20002;
    protected static final int DATABASE_NO_ERROR = 30000;
    protected static final int APP_NO_ERROR = 30001;
    protected static final int BOTH_UPDATE = 30002;
    protected static final int APP_UPDATE = 30003;
    protected static final int DATABASE_UPDATE = 30004;

    /**
     * 新版服务器地址
     */
    public static final String UPLOAD_URL = ROOT_URL + "/api/v1/update";// 上传实时数据
    public static final String BUS_URL = ROOT_URL + "/api/v1/bus";// 在某站点（如明光桥北）申请某条线路（如694）上目前正在跑着的公交
    public static final String LINE_URL =ROOT_URL + "/apo/v1/line";// 一条线路上所有站点的信息
    public static final String VERSION_URL = ROOT_URL + "/api/v1/version";// 请求数据库版本号
    public static final String BEIJINGDB_URL = ROOT_URL + "/api/v1/download";// 更新数据库
    public static final String POSTCOLLECTMESSAGE_URL = ROOT_URL + "/api/v1/collect";

    public static final String REGISTER_URL = ROOT_URL + "/api/v1/registerpwd";//注册
    public static final String LOGIN_URL = ROOT_URL + "/api/v1/login";//登录
    public static final String TOKEN_URL = ROOT_URL + "/api/v1/loginstate";//登录token

    public static final String COMMENT_URL = ROOT_URL + "/api/v1/\n" +
            "http://39.96.3.214:8080/api/v1/addcom";//评论


}
