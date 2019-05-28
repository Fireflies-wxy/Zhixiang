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
    private String distance;
    private View poiTag;

    public ARPoint(String name, String distance, double lat, double lon, double altitude) {
        this.name = name;
        this.distance = distance;
        location = new BDLocation();
        location.setLatitude(lat);
        location.setLongitude(lon);
        location.setAltitude(altitude);
        latitude = lat;
        longitude = lon;
        this.altitude = altitude;
    }

    public void setLocation(BDLocation location) {
        this.location = location;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setDistance(String distance) { this.distance = distance; }

    public void setPoiTag(View poiTag) { this.poiTag = poiTag; }

    public BDLocation getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public String getDistance() { return distance; }

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
}
