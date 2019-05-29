package com.bnrc.busapp.model;

import com.bnrc.busapp.util.RandomUtil;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class Child implements Cloneable, Serializable {
    public static int ARRIVING = 5;// 已到站
    public static int SOON = 4;// 即将到站
    public static int ONTHEWAY = 3;// 在路上
    public static int NOTYET = 2;// 未发车（暂无信息）
    public static int FIRSTSTATION = 1;// 起始站
    public static int NOTEXIST = 0;// 未开通
    public static int OPEN = 11;
    public static int CLOSE = 12;
    public static int NONE = 10;

    private int LineID;
    private int stationID;

    public int getStationID() {
        return stationID;
    }

    public void setStationID(int stationID) {
        this.stationID = stationID;
    }

    private String lineName = "";
    private Map<String, ?> rtInfo = null;
    private List<Map<String, ?>> rtInfoList = new Vector<Map<String, ?>>();

    public List<Map<String, ?>> getRtInfoList() {
        return rtInfoList;
    }

    public void setRtInfoList(List<Map<String, ?>> rtInfoList) {
        this.rtInfoList = rtInfoList;
    }

    private String stationName = "";
    private double latitide;
    private double longitude;
    private String fullName = "";
    private String startStation = "";
    private String endStation = "";
    private String startTime = "";
    private String endTime = "";
    private int AZ;
    private int Type;
    private int sequence;
    private int rtRank = NOTEXIST;
    private int offlineID;
    private boolean isDataChanged = false;
    private int isAlertOpen = NONE;

    //添加评论分数
    private int lineStatus = 0; //乘车拥挤度

    public int isAlertOpen() {
        return isAlertOpen;
    }

    public void setAlertOpen(int isAlertOpen) {
        this.isAlertOpen = isAlertOpen;
    }

    public boolean isDataChanged() {
        return isDataChanged;
    }

    public void setDataChanged(boolean isDataChanged) {
        this.isDataChanged = isDataChanged;
    }

    public int getOfflineID() {
        return offlineID;
    }

    public void setOfflineID(int offlineID) {
        this.offlineID = offlineID;
    }

    public int getRtRank() {
        return rtRank;
    }

    public void setRtRank(int hasRtRank) {
        this.rtRank = hasRtRank;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public Child(int LineID, int stationID, String LineName, String FullName,
                 String stationName, double latitide, double longitude) {
        this.LineID = LineID;
        this.stationID = stationID;
        this.lineName = LineName;
        this.stationName = stationName;
        this.latitide = latitide;
        this.longitude = longitude;
        this.fullName = FullName;

    }

    public Child() {
    }

    public int getLineID() {
        return LineID;
    }

    public void setLineID(int buslineId) {
        this.LineID = buslineId;
    }

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String buslineKeyName) {
        this.lineName = buslineKeyName;
    }

    public Map<String, ?> getRtInfo() {
        return rtInfo;
    }

    public void setRtInfo(Map<String, ?> rtInfo) {
        this.rtInfo = rtInfo;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public double getLatitude() {
        return latitide;
    }

    public void setLatitude(double latitide) {
        this.latitide = latitide;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getLineFullName() {
        return fullName;
    }

    public void setLineFullName(String buslineFullName) {
        this.fullName = buslineFullName;
    }

    public String getStartStation() {
        return startStation;
    }

    public void setStartStation(String startStation) {
        this.startStation = startStation;
    }

    public String getEndStation() {
        return endStation;
    }

    public void setEndStation(String endStation) {
        this.endStation = endStation;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getAZ() {
        return AZ;
    }

    public void setAZ(int aZ) {
        AZ = aZ;
    }

    public int getType() {
        return Type;
    }

    public void setType(int type) {
        Type = type;
    }

    public Child clone() {
        try {

            return (Child) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }


    public int getLineStatus() {
        return lineStatus;
    }

    public void setLineStatus(int lineStatus) {
        this.lineStatus = lineStatus;
    }
}