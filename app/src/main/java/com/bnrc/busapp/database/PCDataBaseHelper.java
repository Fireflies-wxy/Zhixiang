package com.bnrc.busapp.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressLint("SdCardPath")
public class PCDataBaseHelper extends SQLiteOpenHelper {
	private static final String TAG = PCDataBaseHelper.class.getSimpleName();
	// The Android's default system path of your application database.
	private static PCDataBaseHelper instance;
	private static final String PACKAGE_NAME = "com.bnrc.busapp";
	private static final String DATABASE_PATH = "/data"
			+ Environment.getDataDirectory().getAbsolutePath() + "/"
			+ PACKAGE_NAME; // 在手机里存放数据库的位置
	public static String DB_PATH = "/data/data/com.bnrc.busapp/databases/";
	public static String DB_NAME = "pc.db";
	public SQLiteDatabase myDataBase;
	public Context myContext;
	private SharedPreferenceUtil mSharePrefrenceUtil;

	public static PCDataBaseHelper getInstance(Context context) {
		if (instance == null) {
			try {
				DB_PATH = context.getFilesDir().getAbsolutePath();
				DB_PATH = DB_PATH.replace("files", "databases/");
				instance = new PCDataBaseHelper(context);
				// Runtime.getRuntime().exec("chmod 666" +
				// "/data/data/test.txt");
				instance.myContext = context;
				instance.mSharePrefrenceUtil = SharedPreferenceUtil
						.getInstance(context.getApplicationContext());
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
	public PCDataBaseHelper(Context context) throws IOException {
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



	// 获取附近站点和公交线路
	public List<Group> acquireStationAndBusline(LatLng location) {
		List<Group> groups = new ArrayList<Group>();
		String sql = "";
		if (location != null) {
			double lat = location.latitude;
			double lng = location.longitude;
			List<Double> locationRank = dealLocation(lat, lng);
			double smallLat = locationRank.get(0);
			double smallLng = locationRank.get(1);
			double bigLat = locationRank.get(2);
			double bigLng = locationRank.get(3);
			sql = "select * ,GROUP_CONCAT(Relations)as Relations from Stations where  Latitude > "
					+ smallLat
					+ " and Latitude < "
					+ bigLat
					+ " and Longitude > "
					+ smallLng
					+ " and Longitude < "
					+ bigLng + " group by StationName";
		} else
			return groups;
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		while (cursor.moveToNext()) {
			double stationLat = cursor.getDouble(cursor
					.getColumnIndex("Latitude"));
			double stationLng = cursor.getDouble(cursor
					.getColumnIndex("Longitude"));
			LatLng stationPoint = new LatLng(stationLat, stationLng);
			double distance = DistanceUtil.getDistance(location, stationPoint);
			String stationName = cursor.getString(cursor
					.getColumnIndex("StationName"));
			String AZ = cursor.getString(cursor.getColumnIndex("Azimuth"));
			String relations = cursor.getString(cursor
					.getColumnIndex("Relations"));
			Group group = new Group(stationName, stationLat, stationLng);
			group.setDistance(distance);
			group.setRelations(relations);
			Log.i(TAG, stationName);
			groups.add(group);
		}
		groups = sortStationByDistance(groups);
		int size = groups.size() > 3 ? 3 : groups.size();
		// int size = groups.size();
		for (int i = 0; i < size; i++) {
			Set<String> lineSet = new HashSet<String>();
			Group group = groups.get(i);
			String[] relations = group.getRelations().split("[,;]");
			for (String relation : relations) {
				if(relation.length()>0) {
					sql = "select * from Relations where RelationID = " + relation;
					Cursor cur = getMyDataBase().rawQuery(sql, null);
					while (cur.moveToNext()) {
						int StationID = cur.getInt(cur.getColumnIndex("StationID"));
						String StationName = cur.getString(cur
								.getColumnIndex("StationName"));
						int LineID = cur.getInt(cur.getColumnIndex("LineID"));
						String LineName = cur.getString(cur
								.getColumnIndex("LineName"));
						int Sequence = cur.getInt(cur.getColumnIndex("Sequence"));
						double Latitude = cur.getDouble(cur
								.getColumnIndex("Latitude"));
						double Longitude = cur.getDouble(cur
								.getColumnIndex("Longitude"));

						if (!lineSet.contains(LineName)) {
							sql = "select * from Lines where LineID = " + LineID;
							Cursor cur1 = getMyDataBase().rawQuery(sql, null);
							if (cur1.moveToNext()) {
								String EndStation = cur1.getString(cur1
										.getColumnIndex("EndStation"));
								String startStation = cur1.getString(cur1
										.getColumnIndex("StartStation"));
								String startTime = cur1.getString(cur1
										.getColumnIndex("StartTime"));
								String endTime = cur1.getString(cur1
										.getColumnIndex("EndTime"));
								String fullName = LineName + "(" + startStation
										+ "-" + EndStation + ")";
								int MatchId = cur1.getInt(cur1
										.getColumnIndex("MatchId"));
								// int offlineID = checkOfflineID(LineName,
								// startStation, EndStation);
								if (MatchId > 0 && dealStartTime(startTime)
										&& dealEndTime(endTime)) {
									Child child = new Child(LineID, StationID,
											LineName, fullName, StationName,
											Latitude, Longitude);
									int Type = PCUserDataDBHelper.getInstance(
											myContext).IsWhichKindFavInfo(LineID,
											StationID);
									child.setOfflineID(MatchId);
									child.setType(Type);
									child.setStartStation(startStation);
									child.setEndStation(EndStation);
									child.setStartTime(startTime);
									child.setEndTime(endTime);
									child.setLineFullName(fullName);
									child.setSequence(Sequence);
									lineSet.add(LineName);
									group.addChildrenItem(child);
								}
							}
							cur1.close();
						}
					}
					cur.close();
					if (lineSet.size() >= 2)
						break;
				}
			}

		}
		for (int i = 0; i < groups.size(); i++)
			Log.i(TAG, groups.get(i).getStationName());
		cursor.close();
		return groups;
	}

	private boolean dealStartTime(String startTime) {
		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");// 可以方便地修改日期格式
		String nowTime = dateFormat.format(now);
		try {
			Date date2 = dateFormat.parse(startTime);// 23:20
			Date date3 = dateFormat.parse(nowTime);// now
			int number = date2.compareTo(date3);
			if (number == 1)
				return false;

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	private boolean dealEndTime(String endTime) {
		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");// 可以方便地修改日期格式
		String nowTime = dateFormat.format(now);
		try {
			Date date2 = dateFormat.parse(endTime);// 23:20
			Date date3 = dateFormat.parse(nowTime);// now
			int number = date2.compareTo(date3);
			if (number == 1)
				return true;

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
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

	private List<Double> dealLocation(double lat, double lng) {
		List<Double> locationRank = new ArrayList<Double>();
		double latRadius = 0.0f;
		double lngRadius = 0.0f;
		String searchRadius = mSharePrefrenceUtil.getValue("searchRadius",
				"800米");
		int radius = Integer.parseInt(searchRadius.substring(0,
				searchRadius.indexOf("米")));
		Log.i(TAG, "dealLocation： radius " + radius);
		switch (radius) {
		case 600:
			latRadius = 0.004f;
			lngRadius = 0.005f;
			break;
		case 700:
			latRadius = 0.005f;
			lngRadius = 0.0065f;
			break;
		case 800:
			latRadius = 0.006f;
			lngRadius = 0.008f;
			break;
		case 900:
			latRadius = 0.007f;
			lngRadius = 0.009f;
			break;
		case 1000:
			latRadius = 0.008f;
			lngRadius = 0.010f;
			break;
		case 1100:
			latRadius = 0.009f;
			lngRadius = 0.011f;
			break;
		case 1200:
			latRadius = 0.009f;
			lngRadius = 0.012f;
			break;
		case 1300:
			latRadius = 0.010f;
			lngRadius = 0.013f;
			break;
		case 1400:
			latRadius = 0.010f;
			lngRadius = 0.014f;
			break;
		case 1500:
			latRadius = 0.011f;
			lngRadius = 0.015f;
			break;
		default:
			latRadius = 0.004f;
			lngRadius = 0.005f;
			break;
		}
		double smallLat = lat - latRadius;
		double smallLng = lng - lngRadius;
		double bigLat = lat + latRadius;
		double bigLng = lng + lngRadius;
		Log.i(TAG, "dealLocation： loc " + smallLat + " " + smallLng + " "
				+ bigLat + " " + bigLng);

		locationRank.add(smallLat);
		locationRank.add(smallLng);
		locationRank.add(bigLat);
		locationRank.add(bigLng);
		return locationRank;
	}

	private List<Group> sortStationByDistance(List<Group> groups) {
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
		Collections.sort(groups, comparator);
		return groups;
	}

	// 获取某条线路上的所有站点信息
	public List<Child> acquireStationsWithBuslineID(int buslineID) {
		List<Child> stationList = new ArrayList<Child>();
		String sql = "select * from Relations where LineID = \'" + buslineID
				+ "\' order by sequence asc";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		while (cursor.moveToNext()) {
			Child item = new Child();
			int lineID = cursor.getInt(cursor.getColumnIndex("LineID"));
			int RelationID = cursor.getInt(cursor.getColumnIndex("RelationID"));
			int StationID = cursor.getInt(cursor.getColumnIndex("StationID"));
			int Sequence = cursor.getInt(cursor.getColumnIndex("Sequence"));
			double Longitude = cursor.getDouble(cursor
					.getColumnIndex("Longitude"));
			double Latitude = cursor.getDouble(cursor
					.getColumnIndex("Latitude"));
			String StationName = cursor.getString(cursor
					.getColumnIndex("StationName"));
			String LineName = cursor.getString(cursor
					.getColumnIndex("LineName"));
			item.setLineName(LineName);
			item.setAZ(0);
			item.setLatitude(Latitude);
			item.setLineID(lineID);
			item.setLongitude(Longitude);
			item.setSequence(Sequence);

			item.setStationID(StationID);
			item.setStationName(StationName);
			stationList.add(item);
		}
		cursor.close();
		return stationList;
	}

	// 获取stationID
	public String aquireStationId(String stationId) {
		String sql = "select * from Relations  where RelationID = \'"
				+ stationId + "\'";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			cursor.close();
			return cursor.getString(1);
		} else {
			cursor.close();
			return "";
		}
	}

	// 获取同名线路LineID
	public List<Integer> aquireLineIdWithLineName(String LineName) {
		List<Integer> LineIDList = new ArrayList<Integer>();
		String sql = "select * from Lines  where LineName = \'" + LineName
				+ "\'";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		while (cursor.moveToFirst()) {
			int LineID = cursor.getInt(cursor.getColumnIndex("LineID"));
			LineIDList.add(LineID);
		}
		cursor.close();
		return LineIDList;
	}

	// 获取首末班发车时间、首站终点站、线路名
	public Map<String, Object> acquireLineInfoWithLineID(int LineID) {
		Map<String, Object> map = new HashMap<String, Object>();
		String sql = "select * from Lines  where LineID = \'" + LineID + "\'";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			String StartTime = cursor.getString(cursor
					.getColumnIndex("StartTime"));
			String EndTime = cursor.getString(cursor.getColumnIndex("EndTime"));
			String StartStation = cursor.getString(cursor
					.getColumnIndex("StartStation"));
			String EndStation = cursor.getString(cursor
					.getColumnIndex("EndStation"));
			String LineName = cursor.getString(cursor
					.getColumnIndex("LineName"));
			int OfflineID = cursor.getInt(cursor.getColumnIndex("MatchId"));
			map.put("StartTime", StartTime);
			map.put("EndTime", EndTime);
			map.put("StartStation", StartStation);
			map.put("EndStation", EndStation);
			map.put("LineName", LineName);
			map.put("OfflineID", OfflineID);

		}
		cursor.close();
		return map;
	}

	// 模糊匹配站点名
	public List<historyItem> acquireStationsWithStationKeyword(String keyword) {
		List<historyItem> stations = new ArrayList<historyItem>();
		String sql = "select *,sum(LinesNum) as LinesNum from Stations where  StationName like \'"
				+ keyword + "%\' group by StationName";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);

		while (cursor.moveToNext()) {
			historyItem item = new historyItem();
			item.setType(Constants.STATION);
			item.setStationID(cursor.getInt(cursor.getColumnIndex("StationID")));
			item.setStationName(cursor.getString(cursor
					.getColumnIndex("StationName")));
			item.setLineNum(cursor.getInt(cursor.getColumnIndex("LinesNum")));
			item.setLongitude(cursor.getDouble(cursor
					.getColumnIndex("Longitude")));
			item.setLatitude(cursor.getDouble(cursor.getColumnIndex("Latitude")));
			item.setAzimuth(cursor.getInt(cursor.getColumnIndex("Azimuth")));
			item.setDelete(false);

			// Map<String, Object> map = new HashMap<String, Object>();
			// map.put("StationID",
			// cursor.getInt(cursor.getColumnIndex("StationID")));
			// map.put("StationName",
			// cursor.getString(cursor.getColumnIndex("StationName")));
			// map.put("LineNum",
			// cursor.getInt(cursor.getColumnIndex("LineNum")));
			// map.put("Longitude",
			// cursor.getDouble(cursor.getColumnIndex("Longitude")));
			// map.put("Latitude",
			// cursor.getDouble(cursor.getColumnIndex("Latitude")));
			// map.put("Azimuth",
			// cursor.getInt(cursor.getColumnIndex("Azimuth")));

			// ArrayList<String> arrayList = new ArrayList<String>();
			// arrayList.add(cursor.getInt(0) + "");// AZ
			// arrayList.add(cursor.getString(1));// NAME
			// arrayList.add(cursor.getString(2) + "");// GY
			// arrayList.add(cursor.getString(3) + "");// GX
			// arrayList.add(cursor.getString(4));// LNs
			// String[] linesNum = cursor.getString(4).split(";");
			// arrayList.add(linesNum.length + "");// 线路数量
			stations.add(item);
		}
		cursor.close();
		// close();
		return stations;
	}

	// 模糊匹配线路名
	public List<historyItem> acquireBusLinesWithKeyword(String keyword) {
		List<historyItem> buslines = new ArrayList<historyItem>();
		// openDataBase();

		String sql = "select * from Lines  where LineName like \'" + keyword
				+ "%\' order by LineID asc ";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);

		while (cursor.moveToNext()) {
			historyItem item = new historyItem();
			item.setType(Constants.BUSLINE);
			item.setLineID(cursor.getInt(cursor.getColumnIndex("LineID")));
			item.setLineName(cursor.getString(cursor.getColumnIndex("LineName")));
			item.setStationsNum(cursor.getInt(cursor
					.getColumnIndex("StationsNum")));
			item.setStartStation(cursor.getString(cursor
					.getColumnIndex("StartStation")));
			item.setEndStation(cursor.getString(cursor
					.getColumnIndex("EndStation")));
			item.setStartTime(cursor.getString(cursor
					.getColumnIndex("StartTime")));
			item.setEndTime(cursor.getString(cursor.getColumnIndex("EndTime")));
			item.setDelete(false);
			buslines.add(item);
		}
		cursor.close();
		// close();
		return buslines;
	}

	// 获得所有同名站点的分支
	public List<Group> acquireAllBranchBusLinesWithStation(String StationName) {
		List<Group> groups = new ArrayList<Group>();
		String sql = "select * from Stations where StationName = \'"
				+ StationName + "\' ";
		Log.i("buslineIds", "sql = " + sql);
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		String[] relationList = null;
		while (cursor.moveToNext()) {
			Log.i("PCDatabaseHelperNum","Entered");
			double Latitude = cursor.getDouble(cursor
					.getColumnIndex("Latitude"));
			double Longitude = cursor.getDouble(cursor
					.getColumnIndex("Longitude"));
			String SameNameID = cursor.getString(cursor
					.getColumnIndex("SameNameID"));
			int StationID = cursor.getInt(cursor.getColumnIndex("StationID"));
			int Azimuth = cursor.getInt(cursor.getColumnIndex("Azimuth"));
			Log.i("PCDatabaseHelper",Latitude+" "+Longitude+" "+SameNameID+" "+StationID+" "+Azimuth);
			Group group = new Group(StationName, Latitude, Longitude);
			group.setSameNameID(SameNameID);
			groups.add(group);
			String relations = cursor.getString(cursor
					.getColumnIndex("Relations"));
			Log.i(TAG, "relarions:" + relations);

			if(relations.length()>0) {
				relationList = relations.split(";");
				for (String relationID : relationList) {
					Log.i(TAG, "id " + relationID.length());
					String sql1 = "select * from Relations where RelationID ="
							+ relationID;
					Log.i(TAG, "sql:" + sql1);
					Cursor cursor1 = getMyDataBase().rawQuery(sql1, null);
					if (cursor1.moveToFirst()) {
						int LineID = cursor1.getInt(cursor1
								.getColumnIndex("LineID"));
						String LineName = cursor1.getString(cursor1
								.getColumnIndex("LineName"));
						double Latitude1 = cursor1.getDouble(cursor1
								.getColumnIndex("Latitude"));
						double Longitude1 = cursor1.getDouble(cursor1
								.getColumnIndex("Longitude"));
						int Sequence = cursor1.getInt(cursor1
								.getColumnIndex("Sequence"));
						String sql2 = "select * from Lines where LineID =" + LineID;
						Cursor cursor2 = getMyDataBase().rawQuery(sql2, null);
						if (cursor2.moveToNext()) {
							String StartStation = cursor2.getString(cursor2
									.getColumnIndex("StartStation"));
							String EndStation = cursor2.getString(cursor2
									.getColumnIndex("EndStation"));
							String StartTime = cursor2.getString(cursor2
									.getColumnIndex("StartTime"));
							String EndTime = cursor2.getString(cursor2
									.getColumnIndex("EndTime"));
							int OfflineID = cursor2.getInt(cursor2
									.getColumnIndex("MatchId"));
							cursor2.close();
							Child child = new Child();
							child.setLineID(LineID);
							child.setStationID(StationID);
							child.setStationName(StationName);
							child.setSequence(Sequence);
							child.setLineName(LineName);
							child.setLatitude(Latitude1);
							child.setLongitude(Longitude1);
							child.setStartStation(StartStation);
							child.setEndStation(EndStation);
							child.setStartTime(StartTime);
							child.setEndTime(EndTime);
							child.setAZ(Azimuth);
							child.setOfflineID(OfflineID);
							group.addChildrenItem(child);
						}
						cursor1.close();
					}
				}
			}
		}
		cursor.close();
		Comparator<Group> comparator = new Comparator<Group>() {
			public int compare(Group s1, Group s2) {
				String sameNameID1 = s1.getSameNameID();
				String sameNameID2 = s2.getSameNameID();
				return sameNameID1.compareTo(sameNameID2);
			}
		};
		Collections.sort(groups, comparator);
		return groups;
	}

	// 获得所有同名站点的所有线路
	public List<Group> acquireAllBusLinesWithStation(String StationName) {
		List<Group> groups = new ArrayList<Group>();
		Group group = new Group();
		group.setStationName(StationName);
		groups.add(group);
		String sql = "select * from Stations where StationName = \'"
				+ StationName + "\' ";
		Log.i("buslineIds", "sql = " + sql);
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		String[] relationList = null;
		while (cursor.moveToNext()) {
			double Latitude = cursor.getDouble(cursor
					.getColumnIndex("Latitude"));
			double Longitude = cursor.getDouble(cursor
					.getColumnIndex("Longitude"));
			int StationID = cursor.getInt(cursor.getColumnIndex("StationID"));
			int Azimuth = cursor.getInt(cursor.getColumnIndex("Azimuth"));

			String relations = cursor.getString(cursor
					.getColumnIndex("Relations"));
			relationList = relations.split(";");
			Log.i(TAG, relations);
			for (String relationID : relationList) {
				Log.i(TAG, "id " + relationID);
				String sql1 = "";
				if(!(relationID==null||relationID.equals("")))
				{
					sql1 = "select * from Relations where RelationID ="
							+ relationID;
					Cursor cursor1 = getMyDataBase().rawQuery(sql1, null);
					if (cursor1.moveToNext()) {
						int LineID = cursor1.getInt(cursor1
								.getColumnIndex("LineID"));
						String LineName = cursor1.getString(cursor1
								.getColumnIndex("LineName"));
						double Latitude1 = cursor1.getDouble(cursor1
								.getColumnIndex("Latitude"));
						double Longitude1 = cursor1.getDouble(cursor1
								.getColumnIndex("Longitude"));
						Child child = new Child();
						child.setLineID(LineID);
						child.setStationID(StationID);
						child.setStationName(StationName);
						child.setLineName(LineName);
						child.setLatitude(Latitude1);
						child.setLongitude(Longitude1);
						child.setAZ(Azimuth);
						if (PCUserDataDBHelper.getInstance(myContext)
								.IsAlertOpenBusline(LineID, StationName))
							child.setAlertOpen(Child.OPEN);
						else
							child.setAlertOpen(Child.CLOSE);
						group.addChildrenItem(child);
					}
					cursor1.close();
				}

			}
		}
		cursor.close();
		return groups;
	}

	// 获取周围站点（合并同名站点）
	public List<Group> acquireAroundStationsWithLocation(final LatLng myPoint) {
		List<Group> groups = new ArrayList<Group>();
		if (myPoint == null)
			return groups;
		double lat = myPoint.latitude;
		double lng = myPoint.longitude;
		List<Double> locationRank = dealLocation(lat, lng);
		double smallLat = locationRank.get(0);
		double smallLng = locationRank.get(1);
		double bigLat = locationRank.get(2);
		double bigLng = locationRank.get(3);
		String sql = "select * ,GROUP_CONCAT(Relations)as Relations from Stations where  Latitude > "
				+ smallLat
				+ " and Latitude < "
				+ bigLat
				+ " and Longitude > "
				+ smallLng
				+ " and Longitude < "
				+ bigLng
				+ " group by StationName";
		Set<String> set = new HashSet();
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		while (cursor.moveToNext()) {
			Group group = new Group();
			double Latitude = cursor.getDouble(cursor
					.getColumnIndex("Latitude"));
			double Longitude = cursor.getDouble(cursor
					.getColumnIndex("Longitude"));
			String StationName = cursor.getString(cursor
					.getColumnIndex("StationName"));
			if (!set.contains(StationName)) {// 不同名站点情况
				group.setLatitide(Latitude);
				group.setLongitude(Longitude);
				group.setStationName(StationName);
				groups.add(group);
				set.add(StationName);
			}
		}
		cursor.close();
		// close();
		return groups;
	}

	public List<Integer> getStationID(int LineID, int sequence,
                                      String StationName) {
		String sql = "select * from Relations where LineID= \'" + LineID + "\'"
				+ " and StationName=\'" + StationName + "\'";
		List<Integer> list = new ArrayList<Integer>();
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		if (cursor != null && cursor.getCount() == 0) {
			cursor.close();
			return null;
		}
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		while (cursor.moveToNext()) {
			int seq = cursor.getInt(cursor.getColumnIndex("Sequence"));
			int stationID = cursor.getInt(cursor.getColumnIndex("StationID"));
			map.put(seq, stationID);
		}
		int diff = Integer.MAX_VALUE;
		int stationId = -1;
		int sequence1 = -1;
		Set<Integer> set = map.keySet();
		for (Integer seq1 : set) {
			if (Math.abs(seq1 - sequence) < diff) {
				stationId = map.get(seq1);
				sequence1 = seq1;
			}
		}
		cursor.close();
		list.add(stationId);
		list.add(sequence1);
		return list;
	}

	public int getStationID(int LineID, String StationName) {
		String sql = "select * from Relations where LineID= \'" + LineID + "\'"
				+ " and StationName=\'" + StationName + "\'";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		int stationID = -1;
		if (cursor != null && cursor.moveToNext())
			stationID = cursor.getInt(cursor.getColumnIndex("StationID"));
		cursor.close();
		return stationID;
	}

	public int getOfflineID(int LineID) {
		String sql = "select * from Lines where LineID= \'" + LineID + "\'";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		if (cursor.getCount() == 0)
			return -1;
		int offlineID = -1;
		if (cursor != null && cursor.moveToNext())
			offlineID = cursor.getInt(cursor.getColumnIndex("MatchId"));
		cursor.close();
		return offlineID;
	}

	public void copyFile(String oldPath, String newPath) {
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);
			String newFilename = DATABASE_PATH + "/databases/" + newPath;
			if (oldfile.exists()) { // 文件存在时
				InputStream inStream = new FileInputStream(oldPath); // 读入原文件
				FileOutputStream fs = new FileOutputStream(newFilename);
				byte[] buffer = new byte[1024];
				int length;
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; // 字节数 文件大小
					System.out.println(bytesum);
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
				oldfile.delete();
			}
		} catch (Exception e) {
			System.out.println("复制单个文件操作出错");
			e.printStackTrace();
		}
	}

}
