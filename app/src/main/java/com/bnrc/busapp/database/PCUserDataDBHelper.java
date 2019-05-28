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
import com.bnrc.busapp.model.historyItem;
import com.bnrc.busapp.util.SharedPreferenceUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressLint("SdCardPath")
public class PCUserDataDBHelper extends SQLiteOpenHelper {
	private static final String TAG = PCUserDataDBHelper.class.getSimpleName();
	// The Android's default system path of your application database.
	private static PCUserDataDBHelper instance;
	public static String DB_PATH = "/data/data/com.bnrc.busapp/databases/";
	public static String DB_NAME = "userdata.db";
	public SQLiteDatabase myDataBase;
	public Context myContext;
	public ArrayList<ArrayList<String>> alertStations = null;
	public ArrayList<ArrayList<String>> favStations = null;
	public ArrayList<ArrayList<String>> favBuslines = null;
	private static PCDataBaseHelper mPcDataBaseHelper;
	private static SharedPreferenceUtil mSharePrefrenceUtil;

	public static PCUserDataDBHelper getInstance(Context context) {
		if (instance == null) {
			try {
				DB_PATH = context.getFilesDir().getAbsolutePath();
				DB_PATH = DB_PATH.replace("files", "databases/");

				instance = new PCUserDataDBHelper(context);
				mPcDataBaseHelper = PCDataBaseHelper.getInstance(context);
				mSharePrefrenceUtil = SharedPreferenceUtil.getInstance(context);
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
	public PCUserDataDBHelper(Context context) throws IOException {
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
						R.raw.pcuserdata);
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

	public void addSearchRecord(historyItem newRecord) {
		String sql = "";
		switch (newRecord.getType()) {
		case Constants.STATION:// station 0:stationName,1:lat,2:lon,3:AZ
			sql = "delete from LatestSearch where StationID = \'"
					+ newRecord.getStationID() + "\'";
			myDataBase.execSQL(sql);
			sql = "insert into LatestSearch (StationID,StationName,Latitude,Longitude,LineNum,AZ,Type) values (\'"
					+ newRecord.getStationID()
					+ "\',\'"
					+ newRecord.getStationName()
					+ "\',\'"
					+ newRecord.getLatitude()
					+ "\',\'"
					+ newRecord.getLongitude()
					+ "\',\'"
					+ newRecord.getLineNum()
					+ "\',\'"
					+ newRecord.getAzimuth()
					+ "\',\'" + newRecord.getType() + "\')";
			myDataBase.execSQL(sql);
			break;
		case Constants.BUSLINE:// busline 0:ID,1:FullName,2:KeyName
			sql = "delete from LatestSearch where LineID = \'"
					+ newRecord.getLineID() + "\'";
			myDataBase.execSQL(sql);
			sql = "insert into LatestSearch (LineID,LineName,StationNum,StartStation,EndStation,StartTime,EndTime,Type) values (\'"
					+ newRecord.getLineID()
					+ "\',\'"
					+ newRecord.getLineName()
					+ "\',\'"
					+ newRecord.getStationsNum()
					+ "\',\'"
					+ newRecord.getStartStation()
					+ "\',\'"
					+ newRecord.getEndStation()
					+ "\',\'"
					+ newRecord.getStartTime()
					+ "\',\'"
					+ newRecord.getEndTime()
					+ "\',\'"
					+ newRecord.getType()
					+ "\')";
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

	public void delOneSearchRecordByType(historyItem item) {
		String sql = "";
		int type = item.getType();
		switch (type) {
		case Constants.STATION:// station 0:stationName,1:lat,2:lon,3:AZ
			sql = "delete from LatestSearch where StationID = \'"
					+ item.getStationID() + "\'";
			myDataBase.execSQL(sql);

			break;
		case Constants.BUSLINE:// busline 0:ID,1:FullName,2:KeyName
			sql = "delete from LatestSearch where LineID = \'"
					+ item.getLineID() + "\'";
			myDataBase.execSQL(sql);
			break;
		default:
			break;
		}
	}

	public List<historyItem> acquireLatestSearchHistory() {
		List<historyItem> history = new ArrayList<historyItem>();
		// openDataBase();
		String sql = "select * from LatestSearch  order by _id desc";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		while (cursor.moveToNext() && history.size() <= 20) {
			historyItem item = new historyItem();
			item.setType(cursor.getInt(cursor.getColumnIndex("Type")));
			item.setStationID(cursor.getInt(cursor.getColumnIndex("StationID")));
			item.setStationName(cursor.getString(cursor
					.getColumnIndex("StationName")));
			item.setLineNum(cursor.getInt(cursor.getColumnIndex("LineNum")));
			item.setLongitude(cursor.getDouble(cursor
					.getColumnIndex("Longitude")));
			item.setLatitude(cursor.getDouble(cursor
					.getColumnIndex("Longitude")));
			item.setAzimuth(cursor.getInt(cursor.getColumnIndex("AZ")));
			item.setLineID(cursor.getInt(cursor.getColumnIndex("LineID")));
			item.setLineName(cursor.getString(cursor.getColumnIndex("LineName")));
			item.setStationsNum(cursor.getInt(cursor
					.getColumnIndex("StationNum")));
			item.setStartStation(cursor.getString(cursor
					.getColumnIndex("StartStation")));
			item.setEndStation(cursor.getString(cursor
					.getColumnIndex("EndStation")));
			item.setStartTime(cursor.getString(cursor
					.getColumnIndex("StartTime")));
			item.setEndTime(cursor.getString(cursor.getColumnIndex("EndTime")));
			item.setDelete(true);
			history.add(item);
		}
		cursor.close();
		// close();
		return history;
	}

	public List<Group> acquireFavInfoWithLocation(LatLng location, int type) {
		String updateFav = mSharePrefrenceUtil.getValue("UPDATEFAV", "no");
		if (updateFav.equalsIgnoreCase("no")) {
			Log.i(TAG, "acquireFavInfoWithLocation update");
			updateFavorite();
			mSharePrefrenceUtil.setKey("UPDATEFAV", "yes");
		}
		List<Group> mGroups = new ArrayList();
		Set<String> keySet = new HashSet<String>();
		String sql = "";
		if (type == Constants.TYPE_ALL)
			sql = "select * from Favorite order by _id desc";
		else
			sql = "select * from Favorite where Type=\'" + type
					+ "\' order by _id desc";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		while (cursor.moveToNext()) {
			double stationLat = cursor.getDouble(cursor
					.getColumnIndex("Latitude"));
			double stationLng = cursor.getDouble(cursor
					.getColumnIndex("Longitude"));
			LatLng stationPoint = new LatLng(stationLat, stationLng);
			double distance = 0.0;
			if (location != null)
				distance = DistanceUtil.getDistance(location, stationPoint);
			String StationName = cursor.getString(cursor
					.getColumnIndex("StationName"));
			String LineName = cursor.getString(cursor
					.getColumnIndex("LineName"));
			String StartStation = cursor.getString(cursor
					.getColumnIndex("StartStation"));
			String EndStation = cursor.getString(cursor
					.getColumnIndex("EndStation"));
			int LineID = cursor.getInt(cursor.getColumnIndex("LineID"));
			int StationID = cursor.getInt(cursor.getColumnIndex("StationID"));
			int Azimuth = cursor.getInt(cursor.getColumnIndex("Azimuth"));
			int Type = cursor.getInt(cursor.getColumnIndex("Type"));
			int Sequence = cursor.getInt(cursor.getColumnIndex("Sequence"));
			int OfflineID = cursor.getInt(cursor.getColumnIndex("OfflineID"));
			String FullName = LineName + "(" + StartStation + "-" + EndStation
					+ ")";

			Child child = new Child(LineID, StationID, LineName, FullName,
					StationName, stationLat, stationLng);
			child.setLineID(LineID);
			child.setStartStation(StartStation);
			child.setEndStation(EndStation);
			child.setAZ(Azimuth);
			child.setType(Type);
			child.setSequence(Sequence);
			// int offlineID = checkOfflineID(LineName, StartStation,
			// EndStation);
			if (OfflineID > 0)
				child.setOfflineID(OfflineID);
			if (!keySet.contains(StationName)) {
				Group group = new Group(StationName, stationLat, stationLng);
				group.setDistance(distance);
				Log.i(TAG, StationName);
				keySet.add(StationName);
				group.addChildrenItem(child);
				mGroups.add(group);
			} else {
				for (Group group : mGroups)
					if (group.getStationName().trim()
							.equalsIgnoreCase(StationName)) {
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
		if (location != null)
			Collections.sort(mGroups, comparator);
		return mGroups;
	}

	public List<Child> acquireFavInfoWithLocation(List<Child> old) {
		String updateFav = mSharePrefrenceUtil.getValue("UPDATEFAV", "no");
		if (updateFav.equalsIgnoreCase("no")) {
			Log.i(TAG, "acquireFavInfoWithLocation update1");
			updateFavorite();
			mSharePrefrenceUtil.setKey("UPDATEFAV", "yes");
		}
		List<Child> mChildren = new ArrayList();
		String sql = "select * from Favorite order by _id desc";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		while (cursor.moveToNext()) {
			double stationLat = cursor.getDouble(cursor
					.getColumnIndex("Latitude"));
			double stationLng = cursor.getDouble(cursor
					.getColumnIndex("Longitude"));
			String StationName = cursor.getString(cursor
					.getColumnIndex("StationName"));
			String LineName = cursor.getString(cursor
					.getColumnIndex("LineName"));
			String StartStation = cursor.getString(cursor
					.getColumnIndex("StartStation"));
			String EndStation = cursor.getString(cursor
					.getColumnIndex("EndStation"));
			int LineID = cursor.getInt(cursor.getColumnIndex("LineID"));
			int StationID = cursor.getInt(cursor.getColumnIndex("StationID"));
			int Azimuth = cursor.getInt(cursor.getColumnIndex("Azimuth"));
			int Type = cursor.getInt(cursor.getColumnIndex("Type"));
			int Sequence = cursor.getInt(cursor.getColumnIndex("Sequence"));
			int OfflineID = cursor.getInt(cursor.getColumnIndex("OfflineID"));
			String FullName = LineName + "(" + StartStation + "-" + EndStation
					+ ")";
			Child child = new Child(LineID, StationID, LineName, FullName,
					StationName, stationLat, stationLng);
			child.setLineID(LineID);
			child.setStartStation(StartStation);
			child.setEndStation(EndStation);
			child.setAZ(Azimuth);
			child.setType(Type);
			child.setSequence(Sequence);
			if (OfflineID > 0)
				child.setOfflineID(OfflineID);
			mChildren.add(child);
		}
		cursor.close();
		return mChildren;
	}

	// private int checkOfflineID(String LineName, String StartStation,
	// String EndStation) {
	// Map<String, Object> map = BuslineDBHelper.getInstance(myContext)
	// .AcquireOffLineInfoWithBuslineInfo(LineName, StartStation,
	// EndStation);
	// if (map.size() > 0) {
	// return Integer.parseInt(map.get("line_id").toString());
	// } else
	// return -1;
	// }

	public void addFavRecord(Child child) {
		int LineID = child.getLineID();
		int StationID = child.getStationID();
		int Sequence = child.getSequence();
		int OfflineID = child.getOfflineID();
		String LineName = child.getLineName();
		String StationName = child.getStationName();
		String StartStation = child.getStartStation();
		String EndStation = child.getEndStation();
		double Latitude = child.getLatitude();
		double Longitude = child.getLongitude();
		int Amizuth = child.getAZ();
		int Type = child.getType();

		if (IsFavStation(LineID, StationID)) {
			changeFavKind(LineID, StationID, Type);
		} else {
			String sql = "insert into Favorite (LineID,StationID,Sequence,LineName,StationName,StartStation,EndStation,Latitude,Longitude,Azimuth,Type,OfflineID) values (\'"
					+ LineID
					+ "\',\'"
					+ StationID
					+ "\',\'"
					+ Sequence
					+ "\',\'"
					+ LineName
					+ "\',\'"
					+ StationName
					+ "\',\'"
					+ StartStation
					+ "\',\'"
					+ EndStation
					+ "\',\'"
					+ Latitude
					+ "\',\'"
					+ Longitude
					+ "\',\'"
					+ Amizuth
					+ "\',\'"
					+ Type
					+ "\',\'" + OfflineID + "\')";
			myDataBase.execSQL(sql);
		}
	}

	public boolean  IsFavStation(int LineID, int StationID) {
		String sql = "select * from Favorite where LineID = \'" + LineID
				+ "\' and StationID = \'" + StationID + "\'";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		if (cursor.moveToNext()) {
			cursor.close();
			return true;
		} else {
			cursor.close();
			return false;
		}
	}

	public int IsWhichKindFavInfo(int LineID, int StationID) {
		String sql = "select * from Favorite where LineID = \'" + LineID
				+ "\' and StationID=\'" + StationID + "\'";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		int favKindType = 0;
		if (cursor.moveToFirst()) {
			favKindType = cursor.getInt(cursor.getColumnIndex("Type"));
		}
		cursor.close();
		return favKindType;// 未收藏0，全部1，上班2，回家3,其他4，没有收藏 5
	}

	public void changeFavKind(int LineID, int StationID, int type) {
		String sql = "update Favorite set Type=\'" + type
				+ "\' where LineID = \'" + LineID + "\' and StationID =\'"
				+ StationID + "\'";
		myDataBase.execSQL(sql);

	}

	public void cancelFav(int LineID, int StationID) {
		String sql = "delete from Favorite where LineID = \'" + LineID
				+ "\' and StationID =\'" + StationID + "\'";
		myDataBase.execSQL(sql);
	}

	public List<Group> acquireAlertInfoWithLocation(LatLng location) {
		String updateAlert = mSharePrefrenceUtil.getValue("UPDATEALERT", "no");
		if (updateAlert.equalsIgnoreCase("no")) {
			Log.i(TAG, "acquireAlertInfoWithLocation update");
			updateAlertData();
			mSharePrefrenceUtil.setKey("UPDATEALERT", "yes");
		}
		List<Group> mGroups = new ArrayList();
		Set<String> keySet = new HashSet<String>();
		String sql = "select * from Alert order by _id desc";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		while (cursor.moveToNext()) {
			double stationLat = cursor.getDouble(cursor
					.getColumnIndex("Latitude"));
			double stationLng = cursor.getDouble(cursor
					.getColumnIndex("Longitude"));
			LatLng stationPoint = new LatLng(stationLat, stationLng);
			double distance = 0.0;
			if (location != null)
				distance = DistanceUtil.getDistance(location, stationPoint);
			String StationName = cursor.getString(cursor
					.getColumnIndex("StationName"));
			String LineName = cursor.getString(cursor
					.getColumnIndex("LineName"));
			int LineID = cursor.getInt(cursor.getColumnIndex("LineID"));
			int StationID = cursor.getInt(cursor.getColumnIndex("StationID"));
			int Azimuth = cursor.getInt(cursor.getColumnIndex("AZ"));
			int Open = cursor.getInt(cursor.getColumnIndex("Open"));
			Child child = new Child();
			child.setLineID(LineID);
			child.setStationID(StationID);
			child.setLineName(LineName);
			child.setStationName(StationName);
			child.setAZ(Azimuth);
			child.setAlertOpen(Open);
			child.setAZ(Azimuth);
			if (!keySet.contains(StationName)) {
				Group group = new Group(StationName, stationLat, stationLng);
				group.setDistance(distance);
				Log.i(TAG, StationName);
				keySet.add(StationName);
				group.addChildrenItem(child);
				mGroups.add(group);
			} else {
				for (Group group : mGroups)
					if (group.getStationName().trim()
							.equalsIgnoreCase(StationName)) {
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
		if (location != null)
			Collections.sort(mGroups, comparator);
		return mGroups;
	}

	public List<Child> acquireAlertLineWithLocation(LatLng location) {
		String updateAlert = mSharePrefrenceUtil.getValue("UPDATEALERT", "no");
		if (updateAlert.equalsIgnoreCase("no")) {
			Log.i(TAG, "acquireAlertInfoWithLocation update1");
			updateAlertData();
			mSharePrefrenceUtil.setKey("UPDATEALERT", "yes");
		}
		List<Child> children = new ArrayList();
		String sql = "select * from Alert order by _id desc";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		while (cursor.moveToNext()) {
			double stationLat = cursor.getDouble(cursor
					.getColumnIndex("Latitude"));
			double stationLng = cursor.getDouble(cursor
					.getColumnIndex("Longitude"));
			LatLng stationPoint = new LatLng(stationLat, stationLng);
			double distance = 0.0;
			if (location != null)
				distance = DistanceUtil.getDistance(location, stationPoint);
			String StationName = cursor.getString(cursor
					.getColumnIndex("StationName"));
			String LineName = cursor.getString(cursor
					.getColumnIndex("LineName"));
			int LineID = cursor.getInt(cursor.getColumnIndex("LineID"));
			int StationID = cursor.getInt(cursor.getColumnIndex("StationID"));
			int Azimuth = cursor.getInt(cursor.getColumnIndex("AZ"));
			int Open = cursor.getInt(cursor.getColumnIndex("Open"));
			Child child = new Child();
			child.setLineID(LineID);
			child.setStationID(StationID);
			child.setLineName(LineName);
			child.setStationName(StationName);
			child.setAZ(Azimuth);
			child.setAlertOpen(Open);
			child.setAZ(Azimuth);
			child.setLatitude(stationLat);
			child.setLongitude(stationLng);
			child.setAlertOpen(Open);
			children.add(child);
		}
		cursor.close();
		return children;
	}

	public void addAlertRecord(Child child) {
		// openDataBase();
		String sql = "insert into Alert (LineID,LineName,StationID,StationName,Open,Latitude,Longitude,AZ) values (\'"
				+ child.getLineID()
				+ "\',\'"
				+ child.getLineName()
				+ "\',\'"
				+ child.getStationID()
				+ "\',\'"
				+ child.getStationName()
				+ "\',\'"
				+ Child.OPEN
				+ "\',\'"
				+ child.getLatitude()
				+ "\',\'"
				+ child.getLongitude()
				+ "\',\'"
				+ child.getAZ()
				+ "\')";// isOpen
		// 0:close,1:open
		myDataBase.execSQL(sql);
		// close();
	}

	public boolean IsAlertBusline(int LineID, String StationName) {
		// openDataBase();
		String sql = "select * from Alert where StationName= \'" + StationName
				+ "\' and LineID =\'" + LineID + "\'";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		if (cursor.moveToNext()) {
			cursor.close();
			Log.i(TAG, "IsAlertBusline true");
			return true;
		} else {
			cursor.close();
			Log.i(TAG, "IsAlertBusline false");
			return false;
		}
	}

	public boolean IsAlertStation(String StationName) {
		// openDataBase();
		String sql = "select * from Alert where StationName= \'" + StationName
				+ "\'";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		while (cursor.moveToNext()) {
			int open = cursor.getInt(cursor.getColumnIndex("Open"));
			if (open == Child.OPEN) {
				cursor.close();
				return true;
			}
		}
		cursor.close();
		return false;
	}

	public boolean IsAlertOpenBusline(int LineID, String StationName) {
		// openDataBase();
		String sql = "select * from Alert where StationName= \'" + StationName
				+ "\' and LineID =\'" + LineID + "\'";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		if (cursor.moveToNext()) {
			if (cursor.getInt(cursor.getColumnIndex("Open")) == Child.OPEN) {
				cursor.close();
				return true;
			}
		}
		cursor.close();
		return false;
	}

	public boolean IsAlertOpenBusline(int LineID, int StationID) {
		// openDataBase();
		String sql = "select * from Alert where StationID= \'" + StationID
				+ "\' and LineID =\'" + LineID + "\'";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		if (cursor.moveToNext()) {
			if (cursor.getInt(cursor.getColumnIndex("Open")) == Child.OPEN) {
				cursor.close();
				return true;
			}
		}
		cursor.close();
		return false;
	}

	public boolean IsAllAlertOpen() {
		// openDataBase();
		String sql = "select * from Alert ";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		if (cursor.getCount() == 0)
			return false;
		if (cursor.moveToNext()) {
			if (cursor.getInt(cursor.getColumnIndex("Open")) == Child.CLOSE) {
				cursor.close();
				return false;
			}
		}
		cursor.close();
		return true;
	}

	public void deleteAlertStation(String StationName) {
		// openDataBase();
		String sql = "delete from  Alert where StationName = \'" + StationName
				+ "\'";
		myDataBase.execSQL(sql);
		// close();
	}

	public void deleteAllAlertStation() {
		// openDataBase();
		String sql = "delete from  Alert";
		myDataBase.execSQL(sql);
		// close();
	}

	public void closeAlertBusline(int LineID, String StationName) {
		// openDataBase();
		String sql = "update Alert set Open = " + Child.CLOSE
				+ " where LineID = \'" + LineID + "\' and StationName=\'"
				+ StationName + "\'";
		myDataBase.execSQL(sql);
		// close();
	}

	public void closeAllAlertBusline() {
		// openDataBase();
		String sql = "update Alert set Open = " + Child.CLOSE;
		myDataBase.execSQL(sql);
		// close();
	}

	public void openAlertBusline(int LineID, String StationName) {
		// openDataBase();
		String sql = "update Alert set Open = " + Child.OPEN
				+ " where LineID = \'" + LineID + "\' and StationName=\'"
				+ StationName + "\'";
		myDataBase.execSQL(sql);
		// close();
	}

	public void closeAlertBusline(Child child) {
		// openDataBase();
		int LineID = child.getLineID();
		int AZ = child.getAZ();
		String StationName = child.getStationName();
		if (!IsAlertBusline(LineID, StationName))
			addAlertRecord(child);
		else {
			String sql = "update Alert set Open = " + Child.CLOSE
					+ " where LineID = \'" + LineID + "\' and StationName=\'"
					+ StationName + "\' and AZ=\'" + AZ + "\'";
			myDataBase.execSQL(sql);
		}
		// close();
	}

	public void openAlertBusline(Child child) {
		int LineID = child.getLineID();
		String StationName = child.getStationName();
		int AZ = child.getAZ();
		if (!IsAlertBusline(LineID, StationName))
			addAlertRecord(child);
		else {
			String sql = "update Alert set Open = " + Child.OPEN
					+ " where LineID = \'" + LineID + "\' and StationName=\'"
					+ StationName + "\' and AZ=\'" + AZ + "\'";
			myDataBase.execSQL(sql);
		}
		// close();
	}

	public void openAllAlertBusline() {

		String sql = "update Alert set Open = " + Child.OPEN;
		myDataBase.execSQL(sql);

		// close();
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

	public void updateAlertData() {
		String sql = "select * from Alert";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		while (cursor.moveToNext()) {
			int LineID = cursor.getInt(cursor.getColumnIndex("LineID"));
			String StationName = cursor.getString(cursor
					.getColumnIndex("StationName"));
			double latitude = cursor.getDouble(cursor
					.getColumnIndex("Latitude"));
			double longitude = cursor.getDouble(cursor
					.getColumnIndex("Longitude"));
			int stationID = mPcDataBaseHelper.getStationID(LineID, StationName);
			if (stationID != -1) {
				sql = "update Alert set StationID=\'" + stationID
						+ "\' where LineID=\'" + LineID
						+ "\' and Latitude = \'" + latitude + "\'"
						+ " and Longitude = \'" + longitude + "\'";
				myDataBase.execSQL(sql);
			}
		}
		cursor.close();
	}

	public void updateFavorite() {
		String sql = "select * from Favorite";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		while (cursor.moveToNext()) {
			int LineID = cursor.getInt(cursor.getColumnIndex("LineID"));
			int sequence = cursor.getInt(cursor.getColumnIndex("Sequence"));
			double latitude = cursor.getDouble(cursor
					.getColumnIndex("Latitude"));
			double longitude = cursor.getDouble(cursor
					.getColumnIndex("Longitude"));

			String StationName = cursor.getString(cursor
					.getColumnIndex("StationName"));
			List<Integer> list = mPcDataBaseHelper.getStationID(LineID,
					sequence, StationName);
			if (list != null && list.size() > 0) {
				String stationId = list.get(0) + "";
				int seq = list.get(1);
				Log.i(TAG, StationName + " " + stationId + " " + seq);
				int offlineID = mPcDataBaseHelper.getOfflineID(LineID);
				sql = "update Favorite set StationID=\'" + stationId
						+ "\', Sequence=\'" + seq + "\', OfflineID=\'"
						+ offlineID + "\' where LineID = \'" + LineID + "\'"
						+ " and Latitude = \'" + latitude + "\'"
						+ " and Longitude = \'" + longitude + "\'";
				myDataBase.execSQL(sql);
				// sql = "update Favorite set StationID=\'" + stationId
				// + "\' where LineID = \'" + LineID + "\'"
				// + " and Latitude = \'" + latitude + "\'"
				// + " and Longitude = \'" + longitude + "\'";
				// myDataBase.execSQL(sql);
			}
		}
		cursor.close();

	}

}
