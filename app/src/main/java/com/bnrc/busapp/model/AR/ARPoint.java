package com.bnrc.busapp.model.AR;

import android.view.View;

import com.baidu.location.BDLocation;

/**
 * Created by ntdat on 1/16/17.
 */

public class ARPoint {
    private BDLocation location;
    private double latitude;
    private double longitude;
    private double altitude;
    private String name;
    private String description;
    private View poiTag;
    private int type;  //1为普通POI，2为站点，3为线路

    public ARPoint(String name, String description, double lat, double lon, double altitude, int type) {
        this.name = name;
        this.description = description;
        location = new BDLocation();
        location.setLatitude(lat);
        location.setLongitude(lon);
        location.setAltitude(altitude);
        latitude = lat;
        longitude = lon;
        this.altitude = altitude;
        this.type = type;
    }

    public void setLocation(BDLocation location) {
        this.location = location;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) { this.description = description; }

    public void setPoiTag(View poiTag) { this.poiTag = poiTag; }

    public BDLocation getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public String getDescription() { return description; }

    public View getPoiTag() {
        return poiTag;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        location.setLatitude(latitude);
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        location.setLongitude(longitude);
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        location.setAltitude(altitude);
        this.altitude = altitude;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
