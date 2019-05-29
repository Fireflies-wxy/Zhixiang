package com.bnrc.busapp.model;

import com.bnrc.busapp.util.RandomUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Group implements Serializable {
    // private String id;
    private String stationName = "";
    private double latitide;
    private double longitude;
    private List<Child> children = new ArrayList<Child>();
    private double distance;
    private String[] LNs = {};
    private String relations = "";
    private String sameNameID = "";
    private int stationStatus = 0; //候车拥挤度

    public String getSameNameID() {
        return sameNameID;
    }

    public void setSameNameID(String sameNameID) {
        this.sameNameID = sameNameID;
    }

    public String[] getLNs() {
        return LNs;
    }

    public void setLNs(String[] lNs) {
        LNs = lNs;
    }

    public void setRelations(String relations) {
        this.relations = relations;
    }

    public String getRelations() {
        return relations;
    }

    public Group(String stationName, double latitide, double longitude) {
        // this.id = id;
        this.stationName = stationName;
        this.latitide = latitide;
        this.longitude = longitude;

    }

    public Group() {
    }

    public String getStationName() {
        return stationName;
    }

    public void addChildrenItem(Child child) {
        children.add(child);
    }

    public void addChildrenItemFront(Child child) {
        children.add(0, child);
    }

    public int getChildrenCount() {
        if (children != null)
            return children.size();
        else
            return 0;
    }

    public Child getChildItem(int index) {
        return children.get(index);
    }

    public void sortChild() {
        if (children != null && children.size() > 0)
            Collections.sort(children, new Comparator<Child>() {
                public int compare(Child arg0, Child arg1) {

                    if (arg0 != null && arg1 == null)
                        return -1;
                    else if (arg0 == null && arg1 != null)
                        return 1;
                    else if (arg0 == null && arg1 == null)
                        return 0;
                    if (arg0.getRtInfo() != null && arg1.getRtInfo() == null)
                        return -1;
                    else if (arg0.getRtInfo() == null
                            && arg1.getRtInfo() != null)
                        return 1;
                    else if (arg0.getRtInfo() == null
                            && arg1.getRtInfo() == null)
                        return 0;
                    else if (Long.parseLong(arg0.getRtInfo().get("timeStamp")
                            .toString()) == -1
                            && Long.parseLong(arg1.getRtInfo().get("timeStamp")
                            .toString()) != -1)
                        return 1;
                    else if (Long.parseLong(arg1.getRtInfo().get("timeStamp")
                            .toString()) == -1
                            && Long.parseLong(arg0.getRtInfo().get("timeStamp")
                            .toString()) != -1)
                        return -1;
                    else if (Long.parseLong(arg0.getRtInfo().get("timeStamp")
                            .toString()) == -1
                            && Long.parseLong(arg1.getRtInfo().get("timeStamp")
                            .toString()) == -1)
                        return 0;
                    else if (Long.parseLong(arg0.getRtInfo().get("timeStamp")
                            .toString()) > Long.parseLong(arg1.getRtInfo()
                            .get("timeStamp").toString()))
                        return 1;
                    else if (Long.parseLong(arg0.getRtInfo().get("timeStamp")
                            .toString()) < Long.parseLong(arg1.getRtInfo()
                            .get("timeStamp").toString()))
                        return -1;
                    else
                        return 0;
                }
            });

    }

    public double getLatitide() {
        return latitide;
    }

    public void setLatitide(double latitide) {
        this.latitide = latitide;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public List<Child> getChildren() {
        return children;
    }

    public void setChildren(List<Child> children) {
        this.children = children;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public int getStationStatus() {
        return stationStatus;
    }

    public void setStationStatus(int stationStatus) {
        this.stationStatus = stationStatus;
    }


    public void openAllChildAlert() {
        for (Child child : children)
            child.setAlertOpen(Child.OPEN);
    }

    public void closeAllChildAlert() {
        for (Child child : children)
            child.setAlertOpen(Child.CLOSE);
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
