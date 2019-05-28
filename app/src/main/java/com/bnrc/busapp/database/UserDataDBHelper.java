package com.bnrc.busapp.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.bnrc.busapp.R;
import com.bnrc.busapp.constant.Constants;
import com.bnrc.busapp.model.Child;
import com.bnrc.busapp.model.Group;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressLint("SdCardPath")
public class UserDataDBHelper extends SQLiteOpenHelper {
	private static final String TAG = UserDataDBHelper.class.getSimpleName();
	// The Android's default system path of your application database.
	private static UserDataDBHelper instance;
	public static String DB_PATH = "/data/data/com.bnrc.busapp/databases/";
	public static String DB_NAME = "userdata.db";
	public SQLiteDatabase myDataBase;
	public Context myContext;
	public ArrayList<ArrayList<String>> alertStations = null;
	public ArrayList<ArrayList<String>> favStations = null;
	public ArrayList<ArrayList<String>> favBuslines = null;

	public static UserDataDBHelper getInstance(Context context) {
		if (instance == null) {
			try {
				DB_PATH = context.getFilesDir().getAbsolutePath();
				DB_PATH = DB_PATH.replace("files", "databases/");

				instance = new UserDataDBHelper(context);
				instance.myContext = context;
				instance.openDataBase();

			} catch (IOException ioe) {
				throw new Error("Unable to create database");
			}
		}
		return instance;
	}

	/**
	 * Constructor Takes and keeps a reference of the passed context in order to
	 * access to the application assets and resources.
	 * 
	 * @param context
	 * @throws IOException
	 */
	public UserDataDBHelper(Context context) throws IOException {
		super(context, DB_NAME, null, 1);

	}

	public void openDataBase() throws SQLException {
		// Open the database
		String myPath = DB_PATH + DB_NAME;
		try {
			// ����ļ��ľ���·��
			String databaseFilename = myPath;
			File dir = new File(DB_PATH);

			if (!dir.exists()) {
				dir.mkdir();
			}
			;

			if (!(new File(databaseFilename)).exists()) {
				InputStream is = myContext.getResources().openRawResource(
						R.raw.pc);
				FileOutputStream fos = new FileOutputStream(databaseFilename);
				byte[] buffer = new byte[8192];
				int count = 0;
				// ��ʼ�����ļ�
				while ((count = is.read(buffer)) > 0) {
					fos.write(buffer, 0, count);
				}
				fos.close();
				is.close();
			}

			myDataBase = SQLiteDatabase.openOrCreateDatabase(databaseFilename,
					null);

		} catch (Exception e) {
			Log.i("open error", e.getMessage());
		}
	}

	@Override
	public synchronized void close() {

		if (getMyDataBase() != null)
			getMyDataBase().close();

		super.close();

	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public SQLiteDatabase getMyDataBase() {
		return myDataBase;
	}

	public void setMyDataBase(SQLiteDatabase myDataBase) {
		this.myDataBase = myDataBase;
	}

	public void addSearchRecord(ArrayList<String> newRecord, int type) {
		String sql = "";
		switch (type) {
		case 0:// station 0:stationName,1:lat,2:lon,3:AZ
			sql = "delete from LatestSearch where KeyName = \'"
					+ newRecord.get(0) + "\'" + "and Latitude = \'"
					+ newRecord.get(1) + "\'" + "and Longitude = \'"
					+ newRecord.get(2) + "\'";
			myDataBase.execSQL(sql);
			sql = "insert into LatestSearch (FullName,KeyName,Latitude,Longitude,LNs,AZ,Type) values (\'"
					+ newRecord.get(0)
					+ "\',\'"
					+ newRecord.get(0)
					+ "\',\'"
					+ newRecord.get(1)
					+ "\',\'"
					+ newRecord.get(2)
					+ "\',\'"
					+ newRecord.get(3)
					+ "\',\'"
					+ newRecord.get(4)
					+ "\',\'"
					+ type + "\')";
			myDataBase.execSQL(sql);
			break;
		case 1:// busline 0:ID,1:FullName,2:KeyName
			sql = "delete from LatestSearch where ID = \'" + newRecord.get(0)
					+ "\'";
			myDataBase.execSQL(sql);
			sql = "insert into LatestSearch (ID,FullName,KeyName,StartStation,EndStation,StartTime,EndTime,Type) values (\'"
					+ newRecord.get(0)
					+ "\',\'"
					+ newRecord.get(1)
					+ "\',\'"
					+ newRecord.get(2)
					+ "\',\'"
					+ newRecord.get(3)
					+ "\',\'"
					+ newRecord.get(4)
					+ "\',\'"
					+ newRecord.get(5)
					+ "\',\'"
					+ newRecord.get(6) + "\',\'" + type + "\')";
			myDataBase.execSQL(sql);
			break;
		default:
			break;
		}
	}

	public void delAllSearchRecord() {
		String sql = "delete from LatestSearch ";
		myDataBase.execSQL(sql);
	}

	public void delOneSearchRecordByType(ArrayList<String> info, int type) {
		String sql = "";
		switch (type) {
		case 0:// station 0:stationName,1:lat,2:lon,3:AZ
			sql = "delete from LatestSearch where KeyName = \'" + info.get(0)
					+ "\'" + " and Latitude = \'" + info.get(1) + "\'"
					+ " and Longitude = \'" + info.get(2) + "\'";
			myDataBase.execSQL(sql);

			break;
		case 1:// busline 0:ID,1:FullName,2:KeyName
			sql = "delete from LatestSearch where ID = \'" + info.get(0) + "\'";
			myDataBase.execSQL(sql);
			break;
		default:
			break;
		}
	}

	public ArrayList<Map<String, String>> AcquireLatestSearchHistory() {
		ArrayList<Map<String, String>> history = new ArrayList<Map<String, String>>();
		// openDataBase();
		String sql = "select * from LatestSearch  order by _id desc";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);

		while (cursor.moveToNext() && history.size() <= 20) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("ID", cursor.getString(0));
			map.put("FullName", cursor.getString(1));
			map.put("KeyName", cursor.getString(2));
			map.put("Latitude", cursor.getString(3));
			map.put("Longitude", cursor.getString(4));
			map.put("LNs", cursor.getString(5));
			map.put("StartStation", cursor.getString(6));
			map.put("EndStation", cursor.getString(7));
			map.put("StartTime", cursor.getString(8));
			map.put("EndTime", cursor.getString(9));
			map.put("AZ", cursor.getString(10));
			map.put("Type", cursor.getString(11));
			history.add(map);
		}
		cursor.close();
		// close();
		return history;
	}

	/*
	 * private ArrayList<Group> mAllFavGroups; private ArrayList<Group>
	 * mWorkFavGroups; private ArrayList<Group> mHomeFavGroups;
	 * 
	 * public ArrayList<Group> getmAllFavGroups() { Log.i(TAG,
	 * "getmAllFavGroups"); return mAllFavGroups; }
	 * 
	 * public void setmAllFavGroups(ArrayList<Group> mAllFavGroups) {
	 * this.mAllFavGroups = mAllFavGroups; }
	 * 
	 * public ArrayList<Group> getmWorkFavGroups() { Log.i(TAG,
	 * "getmWorkFavGroups");
	 * 
	 * return mWorkFavGroups; }
	 * 
	 * public void setmWorkFavGroups(ArrayList<Group> mWorkFavGroups) {
	 * this.mWorkFavGroups = mWorkFavGroups; }
	 * 
	 * public ArrayList<Group> getmHomeFavGroups() { Log.i(TAG,
	 * "getmHomeFavGroups"); return mHomeFavGroups; }
	 * 
	 * public void setmHomeFavGroups(ArrayList<Group> mHomeFavGroups) {
	 * this.mHomeFavGroups = mHomeFavGroups; }
	 */

	public ArrayList<Group> AcquireFavInfoWithLocation(int type,
                                                       ArrayList<Group> mGroups, LatLng location) {
		if (mGroups == null)
			mGroups = new ArrayList();
		else
			mGroups.clear();
		Set<String> keySet = new HashSet<String>();
		String sql = "";
		if (type == Constants.TYPE_ALL)
			sql = "select ID,BuslineKeyName,StationName,StartStation,EndStation,StartTime,EndTime, Latitude,Longitude,AZ,Type from Favorite order by _id desc";
		else
			sql = "select ID,BuslineKeyName,StationName,StartStation,EndStation,StartTime,EndTime, Latitude,Longitude,AZ,Type from Favorite where Type=\'"
					+ type + "\' order by _id desc";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		while (cursor.moveToNext()) {
			LatLng stationPoint = new LatLng(cursor.getDouble(7),
					cursor.getDouble(8));
			double distance = DistanceUtil.getDistance(location, stationPoint);
			String stationName = cursor.getString(2);
			double GY = cursor.getDouble(7);
			double GX = cursor.getDouble(8);
			String startStation = cursor.getString(3) + "";
			String endStation = cursor.getString(4) + "";
			String startTime = cursor.getString(5) + "";
			String endTime = cursor.getString(6) + "";
			String buslineSN = cursor.getString(0) + "";
			int AZ = Integer.parseInt(cursor.getString(9));
			int Type = Integer.parseInt(cursor.getString(10));

			int buslineId = Integer.parseInt(cursor.getString(0)
					.substring(0, 7));// buslineId
			String buslineTitle = cursor.getString(1);
			String buslineAllName = cursor.getString(1) + "("
					+ cursor.getString(3) + "-" + cursor.getString(4) + ")";

			Child child = new Child(buslineId, 0, buslineTitle, buslineAllName,
					stationName, GY, GX);
			// child.setBuslineSN(buslineSN);
			child.setStartStation(startStation);
			child.setEndStation(endStation);
			child.setStartTime(startTime);
			child.setEndTime(endTime);
			child.setAZ(AZ);
			child.setType(Type);
			if (!keySet.contains(stationName)) {
				Group group = new Group(stationName, GY, GX);
				group.setDistance(distance);
				Log.i(TAG, stationName);
				keySet.add(stationName);
				group.addChildrenItem(child);
				mGroups.add(group);
			} else {
				for (Group group : mGroups)
					if (group.getStationName().trim()
							.equalsIgnoreCase(stationName)) {
						group.addChildrenItem(child);
						break;
					}
			}
		}
		cursor.close();
		Comparator<Group> comparator = new Comparator<Group>() {
			public int compare(Group s1, Group s2) {
				double distance1 = s1.getDistance();
				double distance2 = s2.getDistance();
				if (distance1 > distance2) {
					return 1;
				} else if (distance1 < distance2) {
					return -1;
				}
				return 0;
			}
		};

		Collections.sort(mGroups, comparator);
		return mGroups;
	}

	public ArrayList<Group> AcquireFavInfo(int type, ArrayList<Group> mGroups) {
		if (mGroups == null)
			mGroups = new ArrayList<Group>();
		else
			mGroups.clear();
		Set<String> keySet = new HashSet<String>();
		String sql = "";
		if (type == Constants.TYPE_ALL)
			sql = "select ID,BuslineKeyName,StationName,StartStation,EndStation,StartTime,EndTime, Latitude,Longitude,AZ,Type from Favorite where Type='/"
					+ type + "'/ order by _id desc";
		else
			sql = "select ID,BuslineKeyName,StationName,StartStation,EndStation,StartTime,EndTime, Latitude,Longitude,AZ,Type from Favorite where Type='/"
					+ type + "'/ order by _id desc";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		while (cursor.moveToNext()) {
			LatLng stationPoint = new LatLng(cursor.getDouble(7),
					cursor.getDouble(8));
			String stationName = cursor.getString(2);
			double GY = cursor.getDouble(7);
			double GX = cursor.getDouble(8);
			String startStation = cursor.getString(3) + "";
			String endStation = cursor.getString(4) + "";
			String startTime = cursor.getString(5) + "";
			String endTime = cursor.getString(6) + "";
			String buslineSN = cursor.getString(0) + "";
			int AZ = Integer.parseInt(cursor.getString(9));
			int Type = Integer.parseInt(cursor.getString(10));

			int buslineId = Integer.parseInt(cursor.getString(0)
					.substring(0, 7));// buslineId
			String buslineTitle = cursor.getString(1);
			String buslineAllName = cursor.getString(1) + "("
					+ cursor.getString(3) + "-" + cursor.getString(4) + ")";

			Child child = new Child(buslineId, 0, buslineTitle, buslineAllName,
					stationName, GY, GX);
			// child.setBuslineSN(buslineSN);
			child.setStartStation(startStation);
			child.setEndStation(endStation);
			child.setStartTime(startTime);
			child.setEndTime(endTime);
			child.setAZ(AZ);
			child.setType(Type);
			if (!keySet.contains(stationName)) {
				Group group = new Group(stationName, GY, GX);
				Log.i(TAG, stationName);
				keySet.add(stationName);
				group.addChildrenItem(child);
				mGroups.add(group);
			} else {
				for (Group group : mGroups)
					if (group.getStationName().trim()
							.equalsIgnoreCase(stationName)) {
						group.addChildrenItem(child);
						break;
					}
			}
		}
		cursor.close();
		return mGroups;
	}

	/*
	 * public void AcquireFavInfoWithLocation(LatLng location) { Log.i(TAG,
	 * "AcquireFavInfoWithLocation"); if (mAllFavGroups == null) mAllFavGroups =
	 * new ArrayList<Group>(); if (mWorkFavGroups == null) mWorkFavGroups = new
	 * ArrayList<Group>(); if (mHomeFavGroups == null) mHomeFavGroups = new
	 * ArrayList<Group>(); mAllFavGroups.clear(); mWorkFavGroups.clear();
	 * mHomeFavGroups.clear(); Set<String> allStation = new HashSet<String>();
	 * Set<String> workStation = new HashSet<String>(); Set<String> homeStation
	 * = new HashSet<String>();
	 * 
	 * String sql =
	 * "select ID,BuslineKeyName,StationName,StartStation,EndStation,StartTime,EndTime, Latitude,Longitude,AZ,Type from Favorite order by _id desc"
	 * ; Cursor cursor = getMyDataBase().rawQuery(sql, null); while
	 * (cursor.moveToNext()) { LatLng stationPoint = new
	 * LatLng(cursor.getDouble(7), cursor.getDouble(8)); double distance =
	 * DistanceUtil.getDistance(location, stationPoint); String stationName =
	 * cursor.getString(2); String GY = cursor.getString(7) + ""; String GX =
	 * cursor.getString(8) + ""; String startStation = cursor.getString(3) + "";
	 * String endStation = cursor.getString(4) + ""; String startTime =
	 * cursor.getString(5) + ""; String endTime = cursor.getString(6) + "";
	 * String buslineSN = cursor.getString(0) + ""; String AZ =
	 * cursor.getString(9); String Type = cursor.getString(10);
	 * 
	 * String buslineId = cursor.getString(0).substring(0, 7);// buslineId
	 * String buslineTitle = cursor.getString(1); String buslineAllName =
	 * cursor.getString(1) + "(" + cursor.getString(3) + "-" +
	 * cursor.getString(4) + ")";
	 * 
	 * Child child = new Child(buslineId, buslineTitle, buslineAllName,
	 * stationName, GY, GX, endStation); child.setBuslineSN(buslineSN);
	 * child.setStartStation(startStation); child.setEndStation(endStation);
	 * child.setStartTime(startTime); child.setEndTime(endTime);
	 * child.setAZ(AZ); child.setType(Type); switch (Integer.parseInt(Type)) {
	 * case Constants.TYPE_WORK: if (!workStation.contains(stationName)) { Group
	 * group = new Group(stationName, GY, GX); group.setDistance(distance);
	 * Log.i(TAG, stationName); workStation.add(stationName);
	 * group.addChildrenItem(child); mWorkFavGroups.add(group); } else { for
	 * (Group group : mWorkFavGroups) if (group.getStationTitle().trim()
	 * .equalsIgnoreCase(stationName)) { group.addChildrenItem(child); break; }
	 * }
	 * 
	 * break; case Constants.TYPE_HOME: if (!homeStation.contains(stationName))
	 * { Group group = new Group(stationName, GY, GX);
	 * group.setDistance(distance); Log.i(TAG, stationName);
	 * homeStation.add(stationName); group.addChildrenItem(child);
	 * mHomeFavGroups.add(group); } else { for (Group group : mHomeFavGroups) if
	 * (group.getStationTitle().trim() .equalsIgnoreCase(stationName)) {
	 * group.addChildrenItem(child); break; } } break; default: break; } if
	 * (!allStation.contains(stationName)) { Group group = new
	 * Group(stationName, GY, GX); group.setDistance(distance); Log.i(TAG,
	 * stationName); allStation.add(stationName); group.addChildrenItem(child);
	 * mAllFavGroups.add(group); } else { for (Group group : mAllFavGroups) if
	 * (group.getStationTitle().trim() .equalsIgnoreCase(stationName)) {
	 * group.addChildrenItem(child); break; } }
	 * 
	 * } cursor.close(); Comparator<Group> comparator = new Comparator<Group>()
	 * { public int compare(Group s1, Group s2) { double distance1 =
	 * s1.getDistance(); double distance2 = s2.getDistance(); if (distance1 >
	 * distance2) { return 1; } else if (distance1 < distance2) { return -1; }
	 * return 0; } };
	 * 
	 * Collections.sort(mAllFavGroups, comparator);
	 * Collections.sort(mWorkFavGroups, comparator);
	 * Collections.sort(mHomeFavGroups, comparator);
	 * 
	 * cursor.close(); Log.i(TAG, "exe"); setmAllFavGroups(mAllFavGroups);
	 * setmHomeFavGroups(mHomeFavGroups); setmWorkFavGroups(mWorkFavGroups); }
	 */

	/*
	 * public void AcquireFavInfoWithLocation() { Log.i(TAG,
	 * "AcquireFavInfoWithLocation"); if (mAllFavGroups == null) mAllFavGroups =
	 * new ArrayList<Group>(); if (mWorkFavGroups == null) mWorkFavGroups = new
	 * ArrayList<Group>(); if (mHomeFavGroups == null) mHomeFavGroups = new
	 * ArrayList<Group>(); mAllFavGroups.clear(); mWorkFavGroups.clear();
	 * mHomeFavGroups.clear(); Set<String> allStation = new HashSet<String>();
	 * Set<String> workStation = new HashSet<String>(); Set<String> homeStation
	 * = new HashSet<String>();
	 * 
	 * String sql =
	 * "select ID,BuslineKeyName,StationName,StartStation,EndStation,StartTime,EndTime, Latitude,Longitude,AZ,Type from Favorite order by _id desc"
	 * ; Cursor cursor = getMyDataBase().rawQuery(sql, null); while
	 * (cursor.moveToNext()) { LatLng stationPoint = new
	 * LatLng(cursor.getDouble(7), cursor.getDouble(8)); String stationName =
	 * cursor.getString(2); String GY = cursor.getString(7) + ""; String GX =
	 * cursor.getString(8) + ""; String startStation = cursor.getString(3) + "";
	 * String endStation = cursor.getString(4) + ""; String startTime =
	 * cursor.getString(5) + ""; String endTime = cursor.getString(6) + "";
	 * String buslineSN = cursor.getString(0) + ""; String AZ =
	 * cursor.getString(9); String Type = cursor.getString(10);
	 * 
	 * String buslineId = cursor.getString(0).substring(0, 7);// buslineId
	 * String buslineTitle = cursor.getString(1); String buslineAllName =
	 * cursor.getString(1) + "(" + cursor.getString(3) + "-" +
	 * cursor.getString(4) + ")";
	 * 
	 * Child child = new Child(buslineId, buslineTitle, buslineAllName,
	 * stationName, GY, GX, endStation); child.setBuslineSN(buslineSN);
	 * child.setStartStation(startStation); child.setEndStation(endStation);
	 * child.setStartTime(startTime); child.setEndTime(endTime);
	 * child.setAZ(AZ); child.setType(Type); switch (Integer.parseInt(Type)) {
	 * case Constants.TYPE_WORK: if (!workStation.contains(stationName)) { Group
	 * group = new Group(stationName, GY, GX); Log.i(TAG, stationName);
	 * workStation.add(stationName); group.addChildrenItem(child);
	 * mWorkFavGroups.add(group); } else { for (Group group : mWorkFavGroups) if
	 * (group.getStationTitle().trim() .equalsIgnoreCase(stationName)) {
	 * group.addChildrenItem(child); break; } }
	 * 
	 * break; case Constants.TYPE_HOME: if (!homeStation.contains(stationName))
	 * { Group group = new Group(stationName, GY, GX); Log.i(TAG, stationName);
	 * homeStation.add(stationName); group.addChildrenItem(child);
	 * mHomeFavGroups.add(group); } else { for (Group group : mHomeFavGroups) if
	 * (group.getStationTitle().trim() .equalsIgnoreCase(stationName)) {
	 * group.addChildrenItem(child); break; } } break; default: break; } if
	 * (!allStation.contains(stationName)) { Group group = new
	 * Group(stationName, GY, GX); Log.i(TAG, stationName);
	 * allStation.add(stationName); group.addChildrenItem(child);
	 * mAllFavGroups.add(group); } else { for (Group group : mAllFavGroups) if
	 * (group.getStationTitle().trim() .equalsIgnoreCase(stationName)) {
	 * group.addChildrenItem(child); break; } }
	 * 
	 * } cursor.close(); Log.i(TAG, "exe"); setmAllFavGroups(mAllFavGroups);
	 * setmHomeFavGroups(mHomeFavGroups); setmWorkFavGroups(mWorkFavGroups); }
	 */

	// public ArrayList<Group> AcquireFavInfoWithLocation(LatLng location,
	// ArrayList<Group> groupList, int type) {
	// Set<String> station = new HashSet<String>();
	// String sql = "";
	// if (type != -1)
	// sql =
	// "select ID,BuslineKeyName,StationName,StartStation,EndStation,StartTime,EndTime, Latitude,Longitude,AZ,Type from Favorite where Type = \'"
	// + type + "\' order by _id desc";
	// else
	// sql =
	// "select ID,BuslineKeyName,StationName,StartStation,EndStation,StartTime,EndTime, Latitude,Longitude,AZ,Type from Favorite order by _id desc";
	// Cursor cursor = getMyDataBase().rawQuery(sql, null);
	// while (cursor.moveToNext()) {
	// LatLng stationPoint = new LatLng(cursor.getDouble(7),
	// cursor.getDouble(8));
	// double distance = DistanceUtil.getDistance(location, stationPoint);
	// String stationName = cursor.getString(2);
	// String GY = cursor.getString(7) + "";
	// String GX = cursor.getString(8) + "";
	// String startStation = cursor.getString(3) + "";
	// String endStation = cursor.getString(4) + "";
	// String startTime = cursor.getString(5) + "";
	// String endTime = cursor.getString(6) + "";
	// String buslineSN = cursor.getString(0) + "";
	// String AZ = cursor.getString(9);
	// String Type = cursor.getString(10);
	// if (!station.contains(stationName)) {
	// Group group = new Group(stationName, GY, GX);
	// group.setDistance(distance);
	// Log.i(TAG, stationName);
	//
	// String buslineId = cursor.getString(0).substring(0, 7);// buslineId
	// String buslineTitle = cursor.getString(1);
	// String buslineAllName = cursor.getString(1) + "("
	// + cursor.getString(3) + "-" + cursor.getString(4) + ")";
	//
	// Child child = new Child(buslineId, buslineTitle,
	// buslineAllName, stationName, GY, GX, endStation);
	// child.setBuslineSN(buslineSN);
	// child.setStartStation(startStation);
	// child.setEndStation(endStation);
	// child.setStartTime(startTime);
	// child.setEndTime(endTime);
	// child.setAZ(AZ);
	// child.setType(Type);
	// child.setBuslineSN(buslineSN);
	// station.add(stationName);
	// group.addChildrenItem(child);
	// groupList.add(group);
	// } else {
	// String buslineId = cursor.getString(0).substring(0, 7);// buslineId
	// String buslineTitle = cursor.getString(1);
	// String buslineAllName = cursor.getString(1) + "("
	// + cursor.getString(3) + "-" + cursor.getString(4) + ")";
	//
	// Child child = new Child(buslineId, buslineTitle,
	// buslineAllName, stationName, GY, GX, endStation);
	// child.setBuslineSN(buslineSN);
	// child.setStartStation(startStation);
	// child.setEndStation(endStation);
	// child.setStartTime(startTime);
	// child.setEndTime(endTime);
	// child.setAZ(AZ);
	// child.setType(Type);
	// for (Group group : groupList)
	// if (group.getStationTitle().trim()
	// .equalsIgnoreCase(stationName)) {
	// group.addChildrenItem(child);
	// break;
	// }
	// }
	//
	// }
	// cursor.close();
	// Comparator<Group> comparator = new Comparator<Group>() {
	// public int compare(Group s1, Group s2) {
	//
	// double distance1 = s1.getDistance();
	//
	// double distance2 = s2.getDistance();
	// if (distance1 > distance2) {
	// return 1;
	// } else if (distance1 < distance2) {
	// return -1;
	// }
	// return 0;
	// }
	// };
	//
	// Collections.sort(groupList, comparator);
	// cursor.close();
	// Log.i(TAG, "exe");
	//
	// return groupList;
	// }

	public boolean IsFavInfo(String SN) {
		String sql = "select * from Favorite where ID = \'" + SN + "\'";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		if (cursor.moveToNext()) {
			cursor.close();
			return true;
		} else {
			cursor.close();
			return false;
		}
	}

	public int IsWhichKindFavInfo(String SN) {
		String sql = "select * from Favorite where ID = \'" + SN + "\'";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		int favKindType = -1;
		if (cursor.moveToFirst()) {
			favKindType = cursor.getInt(10);
		}
		cursor.close();
		return favKindType;// 全部0，上班1，回家2,没有收藏 -1
	}

	public void addFavRecord(ArrayList<String> newRecord) {

		if (IsFavInfo(newRecord.get(0))) {
			changeFavKind(newRecord.get(0), Integer.parseInt(newRecord.get(10)));
		} else {
			String sql = "insert into Favorite (ID,BuslineKeyName,StationName,StartStation,EndStation,StartTime,EndTime, Latitude,Longitude,AZ,Type) values (\'"
					+ newRecord.get(0)
					+ "\',\'"
					+ newRecord.get(1)
					+ "\',\'"
					+ newRecord.get(2)
					+ "\',\'"
					+ newRecord.get(3)
					+ "\',\'"
					+ newRecord.get(4)
					+ "\',\'"
					+ newRecord.get(5)
					+ "\',\'"
					+ newRecord.get(6)
					+ "\',\'"
					+ newRecord.get(7)
					+ "\',\'"
					+ newRecord.get(8)
					+ "\',\'"
					+ newRecord.get(9)
					+ "\',\'"
					+ newRecord.get(10) + "\')";
			myDataBase.execSQL(sql);
		}
	}

	public void changeFavKind(String SN, int type) {
		String sql = "update Favorite set Type=\'" + type + "\' where ID = \'"
				+ SN + "\'";
		myDataBase.execSQL(sql);

	}

	public void cancelFav(String SN) {
		String sql = "delete from Favorite where ID = \'" + SN + "\'";
		myDataBase.execSQL(sql);
	}

	public void addAlertRecord(ArrayList<String> newRecord) {
		// openDataBase();
		String sql = "insert into Alert (StationName,SN,Open,Latitude,Longitude,AZ) values (\'"
				+ newRecord.get(0)
				+ "\',\'"
				+ newRecord.get(1)
				+ "\',\'"
				+ newRecord.get(2)
				+ "\',\'"
				+ newRecord.get(3)
				+ "\',\'"
				+ newRecord.get(4) + "\',\'" + newRecord.get(5) + "\')";// isOpen
																		// 0:close,1:open
		myDataBase.execSQL(sql);
		// close();
	}

	public boolean IsAlertStation(String StationName) {
		// openDataBase();
		String sql = "select * from Alert where StationName= \'" + StationName
				+ "\'";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		if (cursor.moveToNext()) {
			cursor.close();
			return true;
		} else {
			cursor.close();
			return false;
		}
	}

	public boolean IsAlertBusline(String SN) {
		// openDataBase();
		String sql = "select * from Alert where SN= \'" + SN + "\'";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		if (cursor.moveToNext()) {
			cursor.close();
			return true;
		} else {
			cursor.close();
			return false;
		}
	}

	public boolean IsAlertOpenBusline(String SN) {
		// openDataBase();
		String sql = "select * from Alert where SN= \'" + SN + "\'";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		if (cursor.moveToNext()) {
			if (cursor.getString(2).equalsIgnoreCase(1 + "")) {
				cursor.close();
				return true;
			}
		}
		cursor.close();
		return false;
	}

	public boolean IsAlertOpenStation(String stationname) {
		// openDataBase();
		String sql = "select * from Alert where StationName= \'" + stationname
				+ "\'";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		while (cursor.moveToNext()) {
			if (cursor.getString(2).equalsIgnoreCase(1 + "")) {
				cursor.close();
				return true;
			}
		}
		cursor.close();
		return false;
	}

	public void deleteAlertStation(String SN) {
		// openDataBase();
		String sql = "delete from  Alert where SN = \'" + SN + "\'";
		myDataBase.execSQL(sql);
		// close();
	}

	public void closeAlertBusline(String SN) {
		// openDataBase();
		String sql = "update Alert set Open = '0' where SN = \'" + SN + "\'";
		myDataBase.execSQL(sql);
		// close();
	}

	public void closeAlertStation(String stationName) {
		// openDataBase();
		String sql = "update Alert set Open = '0' where StationName = \'"
				+ stationName + "\'";
		myDataBase.execSQL(sql);
		// close();
	}

	public void openAlertBusline(String SN) {
		// openDataBase();
		String sql = "update Alert set Open = '1' where SN = \'" + SN + "\'";
		myDataBase.execSQL(sql);
		// close();
	}

	public void openAlertStation(String stationName) {
		// openDataBase();
		String sql = "update Alert set Open = '1' where StationName = \'"
				+ stationName + "\'";
		myDataBase.execSQL(sql);
		// close();
	}

	public ArrayList<ArrayList<String>> AcquireAlertInfoByStation() {
		ArrayList<ArrayList<String>> alertList = new ArrayList<ArrayList<String>>();
		String sql = "select * from Alert group by StationName";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		while (cursor.moveToNext()) {
			ArrayList<String> alertStation = new ArrayList<String>();
			alertStation.add(cursor.getString(0));// stationName
			alertStation.add(cursor.getString(3));// Lat
			alertStation.add(cursor.getString(4));// Lon
			alertStation.add(cursor.getString(5));// AZ
			alertStation.add(cursor.getString(2));// IsOpen
			alertList.add(alertStation);
		}
		cursor.close();
		return alertList;
	}

	public ArrayList<ArrayList<String>> AcquireAlertBuslineInfo(
			String staionName) {
		ArrayList<ArrayList<String>> alertList = new ArrayList<ArrayList<String>>();
		// String sql = "select * from Alert where StationName=\'" + staionName
		// + "\' order by _id desc";
		// Cursor cursor = getMyDataBase().rawQuery(sql, null);
		// while (cursor.moveToNext()) {
		// ArrayList<String> alertStation = new ArrayList<String>();
		// alertStation.add(cursor.getString(0));// stationName
		// alertStation.add(cursor.getString(3));// Lat
		// alertStation.add(cursor.getString(4));// Lon
		// alertStation.add(cursor.getString(5));// AZ
		// alertStation.add(cursor.getString(2));// IsOpen
		// alertList.add(alertStation);
		// }
		// cursor.close();

		ArrayList<String> alertStation = new ArrayList<String>();
		alertStation.add("387路");// buslineName
		alertStation.add("");// Lat
		alertStation.add("");// Lon
		alertStation.add("");// AZ
		alertStation.add(0 + "");// IsOpen

		ArrayList<String> alertStation1 = new ArrayList<String>();
		alertStation1.add("606路");// buslineName
		alertStation1.add("");// Lat
		alertStation1.add("");// Lon
		alertStation1.add("");// AZ
		alertStation1.add(1 + "");// IsOpen

		ArrayList<String> alertStation2 = new ArrayList<String>();
		alertStation2.add("490路");// buslineName
		alertStation2.add("");// Lat
		alertStation2.add("");// Lon
		alertStation2.add("");// AZ
		alertStation2.add(1 + "");// IsOpen

		ArrayList<String> alertStation3 = new ArrayList<String>();
		alertStation3.add("21路");// buslineName
		alertStation3.add("");// Lat
		alertStation3.add("");// Lon
		alertStation3.add("");// AZ
		alertStation3.add(1 + "");// IsOpen

		ArrayList<String> alertStation4 = new ArrayList<String>();
		alertStation4.add("夜14路");// buslineName
		alertStation4.add("");// Lat
		alertStation4.add("");// Lon
		alertStation4.add("");// AZ
		alertStation4.add(0 + "");// IsOpen

		ArrayList<String> alertStation5 = new ArrayList<String>();
		alertStation5.add("375路");// buslineName
		alertStation5.add("");// Lat
		alertStation5.add("");// Lon
		alertStation5.add("");// AZ
		alertStation5.add(0 + "");// IsOpen

		alertList.add(alertStation);
		alertList.add(alertStation1);
		alertList.add(alertStation2);
		alertList.add(alertStation3);
		alertList.add(alertStation4);
		alertList.add(alertStation5);

		return alertList;
	}

	public boolean checkAlertStationWithStation(ArrayList<String> station) {
		// openDataBase();
		int j = alertStations.size();
		for (int i = 0; i < j; i++) {
			if (alertStations.get(i).get(0).equalsIgnoreCase(station.get(0))) {
				// 距离小于50m
				return true;
			}
		}
		return false;
		// close();
	}

	public void deleteAlertStationWithStation(ArrayList<String> station) {
		// openDataBase();
		String sql = "delete from  station_alert where latitude = \'"
				+ station.get(1) + "\' and longtitude = \'" + station.get(2)
				+ "\'";
		myDataBase.execSQL(sql);
		// close();
		getAlertStations();

	}

	public void getAlertStations() {
		alertStations = new ArrayList<ArrayList<String>>();
		// openDataBase();

		String sql = "select name,latitude,longtitude from station_alert";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);

		while (cursor.moveToNext()) {
			ArrayList<String> arrayList = new ArrayList<String>();
			arrayList.add(cursor.getString(0));
			arrayList.add(cursor.getString(1));
			arrayList.add(cursor.getString(2));
			alertStations.add(arrayList);
		}
		cursor.close();
		// close();
	}

	public void addAlertStationWithStation(ArrayList<String> newRecord) {
		// openDataBase();
		String sql = "delete from  Alert where latitude = \'"
				+ newRecord.get(1) + "\' and longtitude = \'"
				+ newRecord.get(2) + "\'";
		myDataBase.execSQL(sql);
		sql = "insert into station_alert (name,latitude,longtitude,o) values (\'"
				+ newRecord.get(0)
				+ "\',\'"
				+ newRecord.get(1)
				+ "\',\'"
				+ newRecord.get(2) + "\')";
		myDataBase.execSQL(sql);
		// close();
		getAlertStations();
	}

}
