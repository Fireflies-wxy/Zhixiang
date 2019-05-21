package com.bnrc.bnrcbus.model;

public class historyItem {
	private int type;
	private int LineID;
	private String LineName;

	private int StationsNum;

	private String StartStation;

	private String EndStation;

	private String StartTime;

	private String EndTime;

	private int StationID;

	private String StationName;
	private int LineNum;
	private double Longitude;
	private double Latitude;
	private int Azimuth;
	private boolean isDelete;

	public boolean isDelete() {
		return isDelete;
	}

	public void setDelete(boolean isDelete) {
		this.isDelete = isDelete;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getLineID() {
		return LineID;
	}

	public void setLineID(int lineID) {
		LineID = lineID;
	}

	public String getLineName() {
		return LineName;
	}

	public void setLineName(String lineName) {
		LineName = lineName;
	}

	public int getStationsNum() {
		return StationsNum;
	}

	public void setStationsNum(int stationsNum) {
		StationsNum = stationsNum;
	}

	public String getStartStation() {
		return StartStation;
	}

	public void setStartStation(String startStation) {
		StartStation = startStation;
	}

	public String getEndStation() {
		return EndStation;
	}

	public void setEndStation(String endStation) {
		EndStation = endStation;
	}

	public String getStartTime() {
		return StartTime;
	}

	public void setStartTime(String startTime) {
		StartTime = startTime;
	}

	public String getEndTime() {
		return EndTime;
	}

	public void setEndTime(String endTime) {
		EndTime = endTime;
	}

	public int getStationID() {
		return StationID;
	}

	public void setStationID(int stationID) {
		StationID = stationID;
	}

	public String getStationName() {
		return StationName;
	}

	public void setStationName(String stationName) {
		StationName = stationName;
	}

	public int getLineNum() {
		return LineNum;
	}

	public void setLineNum(int lineNum) {
		LineNum = lineNum;
	}

	public double getLongitude() {
		return Longitude;
	}

	public void setLongitude(double longitude) {
		Longitude = longitude;
	}

	public double getLatitude() {
		return Latitude;
	}

	public void setLatitude(double latitude) {
		Latitude = latitude;
	}

	public int getAzimuth() {
		return Azimuth;
	}

	public void setAzimuth(int azimuth) {
		Azimuth = azimuth;
	}

}
