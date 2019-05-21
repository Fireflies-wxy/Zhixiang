package com.bnrc.bnrcbus.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.bnrc.bnrcbus.R;
import com.bnrc.bnrcbus.model.Child;
import com.bnrc.bnrcbus.model.Group;
import com.bnrc.bnrcbus.util.MostSimilarString;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("SdCardPath")
public class DataBaseHelper extends SQLiteOpenHelper {
	private static final String TAG = DataBaseHelper.class.getSimpleName();
	// The Android's default system path of your application database.
	private static DataBaseHelper instance;
	public static String DB_PATH = "/data/data/com.bnrc.busapp/databases/";
	public static String DB_NAME = "businfo.db";
	public SQLiteDatabase myDataBase;
	public Context myContext;
	private int FileLength;
	private int DownedFileLength = 0;
	private InputStream inputStream;
	private URLConnection connection;
	private OutputStream outputStream;

	// private Handler handler = new Handler() {
	// public void handleMessage(Message msg) {
	// if (!Thread.currentThread().isInterrupted()) {
	// switch (msg.what) {
	// case 0:
	// // progressBar.setMax(FileLength);
	// Log.i("文件长度----------->", FileLength + "");
	// break;
	// case 1:
	// // progressBar.setProgress(DownedFileLength);
	// int x = DownedFileLength * 100 / FileLength;
	// Log.i("文件长度----------->", DownedFileLength + "");
	// // textView.setText(x+"%");
	// break;
	// case 2:
	// openDataBase();
	// MobclickAgent.updateOnlineConfig(myContext);
	// String value = MobclickAgent.getConfigParams(myContext,
	// "bus_data_version");
	// Log.i("bus_data_version", value);
	// JSONObject jsonObj = null;
	// try {
	// jsonObj = new JSONObject(value);
	// String version = jsonObj.getString("version");
	// SharedPreferences mySharedPreferences = myContext
	// .getSharedPreferences("setting",
	// SettingView.MODE_PRIVATE);
	// SharedPreferences.Editor editor = mySharedPreferences
	// .edit();
	// editor.putString("bus_data_version", version);
	// editor.commit();
	// } catch (JSONException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// Toast.makeText(myContext, "公交数据更新完成", Toast.LENGTH_LONG)
	// .show();
	// break;
	//
	// default:
	// break;
	// }
	// }
	// }
	//
	// };

	public static DataBaseHelper getInstance(Context context) {
		if (instance == null) {
			try {
				DB_PATH = context.getFilesDir().getAbsolutePath();
				DB_PATH = DB_PATH.replace("files", "databases/");

				instance = new DataBaseHelper(context);
				// Runtime.getRuntime().exec("chmod 666" +
				// "/data/data/test.txt");
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
	public DataBaseHelper(Context context) throws IOException {
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

	// Add your public helper methods to access and get content from the
	// database.
	// You could return cursors by doing "return myDataBase.query(....)" so it'd
	// be easy
	// to you to create adapters for your views.

	// 附近站点信息（合并同名站点）
	public ArrayList<ArrayList<String>> AcquireAroundStationsWithLocation(
			final LatLng myPoint) {
		// openDataBase();
		ArrayList<ArrayList<String>> stations = new ArrayList<ArrayList<String>>();
		float latRadius = 0.0f;
		float lngRadius = 0.0f;

		SharedPreferences mySharedPreferences = myContext.getSharedPreferences(
				"setting", Context.MODE_PRIVATE);
		String searchradius = mySharedPreferences.getString("searchRMode",
				"600米");
		int radius = Integer.parseInt(searchradius.subSequence(0,
				searchradius.length() - 1).toString());
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
			break;
		}

		double lat = myPoint.latitude;
		double lng = myPoint.longitude;
		double smallLat = lat - latRadius;
		double smallLng = lng - lngRadius;
		double bigLat = lat + latRadius;
		double bigLng = lng + lngRadius;
		String sql = "select AZ,NAME,GY,GX,LNs from CSTATIONS where  GY > "
				+ smallLat + " and GY < " + bigLat + " and GX > " + smallLng
				+ " and GX < " + bigLng;
		Map<String, String> stationMap = new HashMap<String, String>();
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		while (cursor.moveToNext()) {
			ArrayList<String> arrayList = new ArrayList<String>();
			LatLng stationPoint = new LatLng(cursor.getDouble(2),
					cursor.getDouble(3));

			double distance = DistanceUtil.getDistance(myPoint, stationPoint);
			String stationName = cursor.getString(1);
			if (!stationMap.containsKey(stationName)) {// 不同名站点情况
				arrayList.add(cursor.getInt(0) + "");// AZ
				arrayList.add(cursor.getString(1));// stationNAME
				arrayList.add(cursor.getString(2) + "");// GY
				arrayList.add(cursor.getString(3) + "");// GX
				arrayList.add(cursor.getString(4));// LNs
				arrayList.add(distance + "");// distance
				stations.add(arrayList);
				stationMap.put(stationName,
						cursor.getInt(0) + ";" + cursor.getString(2) + ";"
								+ cursor.getString(3));
			} else if (Math.abs((Integer.parseInt(stationMap.get(stationName)
					.split(";")[0]) - cursor.getInt(0))) > 90) {// 上下行情况
				arrayList.add(cursor.getInt(0) + "");// AZ
				arrayList.add(cursor.getString(1));// stationNAME
				arrayList.add(cursor.getString(2) + "");// GY
				arrayList.add(cursor.getString(3) + "");// GX
				arrayList.add(cursor.getString(4));// LNs
				arrayList.add(distance + "");// distance
				stations.add(arrayList);
				stationMap.put(stationName,
						cursor.getInt(0) + ";" + cursor.getString(2) + ";"
								+ cursor.getString(3));
			} else {// 同方向但相距超过80m情况
				LatLng oldPoint = new LatLng(Double.parseDouble(stationMap.get(
						stationName).split(";")[1]),
						Double.parseDouble(stationMap.get(stationName).split(
								";")[2]));

				if (DistanceUtil.getDistance(oldPoint, stationPoint) > 100) {
					arrayList.add(cursor.getInt(0) + "");// AZ
					arrayList.add(cursor.getString(1));// stationNAME
					arrayList.add(cursor.getString(2) + "");// GY
					arrayList.add(cursor.getString(3) + "");// GX
					arrayList.add(cursor.getString(4));// LNs
					arrayList.add(distance + "");// distance
					stations.add(arrayList);
					stationMap.put(stationName,
							cursor.getInt(0) + ";" + cursor.getString(2) + ";"
									+ cursor.getString(3));
				}
			}
		}

		cursor.close();
		// close();
		return stations;
	}

	public ArrayList<ArrayList<String>> AcquireStationsWithLocation(
			final LatLng myPoint) {
		// openDataBase();
		ArrayList<ArrayList<String>> stations = new ArrayList<ArrayList<String>>();
		float latRadius = 0.0f;
		float lngRadius = 0.0f;

		SharedPreferences mySharedPreferences = myContext.getSharedPreferences(
				"setting", Context.MODE_PRIVATE);
		String searchradius = mySharedPreferences.getString("searchRMode",
				"800米");
		int radius = Integer.parseInt(searchradius.subSequence(0,
				searchradius.length() - 1).toString());
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
			break;
		}

		double lat = myPoint.latitude;
		double lng = myPoint.longitude;
		double smallLat = lat - latRadius;
		double smallLng = lng - lngRadius;
		double bigLat = lat + latRadius;
		double bigLng = lng + lngRadius;
		String sql = "select AZ,NAME,GY,GX,LNs from CSTATIONS where  GY > "
				+ smallLat + " and GY < " + bigLat + " and GX > " + smallLng
				+ " and GX < " + bigLng;
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		while (cursor.moveToNext()) {
			ArrayList<String> arrayList = new ArrayList<String>();
			arrayList.add(cursor.getInt(0) + "");// AZ
			arrayList.add(cursor.getString(1));// stationNAME
			arrayList.add(cursor.getString(2) + "");// GY
			arrayList.add(cursor.getString(3) + "");// GX
			arrayList.add(cursor.getString(4));// LNs
			stations.add(arrayList);
		}
		cursor.close();
		// close();
		return stations;
	}

	// 附近线路信息（合并同名线路）
	public ArrayList<ArrayList<String>> getNearbyBuslineWithLocation1(
			LatLng location) {
		ArrayList<ArrayList<String>> buslines = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> stations = AcquireAroundStationsWithLocation(location);
		ArrayList<ArrayList<String>> lineStop = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < stations.size(); i++) {
			String lineString = stations.get(i).get(4);// LNs
			String[] lineArr = lineString.split(";");
			int lineArrLength = lineArr.length;
			for (int k = 0; k < lineArrLength; k++) {
				int j = 0;
				for (j = 0; j < lineStop.size(); j++) {
					if (lineStop.get(j).get(0)
							.equalsIgnoreCase(lineArr[k].substring(0, 7))) {
						break;
					}
				}
				if (j == lineStop.size()) {
					ArrayList<String> item = new ArrayList<String>();
					item.add(lineArr[k].substring(0, 7));// LN
					item.add(stations.get(i).get(1));// stationName
					item.add(stations.get(i).get(2));// GY
					item.add(stations.get(i).get(3));// GX
					lineStop.add(item);
				}
			}

		}
		// 根据线路id查找线路名，需要显示线路名，线路id需要纪录，传到下一页面，查看线路详情。
		for (int i = 0; i < lineStop.size(); i++) {
			String sql = "select NAME, S_START, S_END from LINES where LN = "
					+ lineStop.get(i).get(0);// LN
			Cursor cursor = getMyDataBase().rawQuery(sql, null);
			while (cursor.moveToNext()) {
				ArrayList<String> arrayList;
				arrayList = new ArrayList<String>();
				arrayList.add(lineStop.get(i).get(0));// buslineId
				arrayList.add(cursor.getString(0));// keyName
				arrayList.add(cursor.getString(0) + "(" + cursor.getString(1)
						+ "-" + cursor.getString(2) + ")");// Name
				arrayList.add(lineStop.get(i).get(1));// stationName
				arrayList.add(lineStop.get(i).get(2));// GY
				arrayList.add(lineStop.get(i).get(3));// GX
				arrayList.add(cursor.getString(2));// S_END
				buslines.add(arrayList);
			}
			cursor.close();
		}

		return buslines;
	}

	// public ArrayList<Group> AcquireNearInfoWithLocation(LatLng location,
	// ArrayList<Group> groupList) {
	// groupList.clear();
	// float latRadius = 0.0f;
	// float lngRadius = 0.0f;
	//
	// SharedPreferences mySharedPreferences = myContext.getSharedPreferences(
	// "setting", Context.MODE_PRIVATE);
	// String searchradius = mySharedPreferences.getString("searchRMode",
	// "800米");
	// int radius = Integer.parseInt(searchradius.subSequence(0,
	// searchradius.length() - 1).toString());
	// switch (radius) {
	// case 600:
	// latRadius = 0.004f;
	// lngRadius = 0.005f;
	// break;
	// case 700:
	// latRadius = 0.005f;
	// lngRadius = 0.0065f;
	// break;
	// case 800:
	// latRadius = 0.006f;
	// lngRadius = 0.008f;
	// break;
	// case 900:
	// latRadius = 0.007f;
	// lngRadius = 0.009f;
	// break;
	// case 1000:
	// latRadius = 0.008f;
	// lngRadius = 0.010f;
	// break;
	// case 1100:
	// latRadius = 0.009f;
	// lngRadius = 0.011f;
	// break;
	// case 1200:
	// latRadius = 0.009f;
	// lngRadius = 0.012f;
	// break;
	// case 1300:
	// latRadius = 0.010f;
	// lngRadius = 0.013f;
	// break;
	// case 1400:
	// latRadius = 0.010f;
	// lngRadius = 0.014f;
	// break;
	// case 1500:
	// latRadius = 0.011f;
	// lngRadius = 0.015f;
	// break;
	// default:
	// break;
	// }
	//
	// double lat = location.latitude;
	// double lng = location.longitude;
	// double smallLat = lat - latRadius;
	// double smallLng = lng - lngRadius;
	// double bigLat = lat + latRadius;
	// double bigLng = lng + lngRadius;
	// // ���Ҹ�����վ
	// String sql =
	// "select AZ,NAME,GY,GX,GROUP_CONCAT(LNs)as LNs from CSTATIONS where  GY > "
	// + smallLat
	// + " and GY < "
	// + bigLat
	// + " and GX > "
	// + smallLng
	// + " and GX < " + bigLng + " group by NAME";
	// Cursor cursor = getMyDataBase().rawQuery(sql, null);
	// while (cursor.moveToNext()) {
	// LatLng stationPoint = new LatLng(cursor.getDouble(2),
	// cursor.getDouble(3));
	// double distance = DistanceUtil.getDistance(location, stationPoint);
	// String stationName = cursor.getString(1);
	// String GY = cursor.getString(2) + "";
	// String GX = cursor.getString(3) + "";
	// String[] LNs = cursor.getString(4).split("[,;]");
	//
	// Group group = new Group(stationName, GY, GX);
	// group.setDistance(distance);
	// group.setLNs(LNs);
	// Log.i(TAG, stationName);
	// // Set<String> lineName = new HashSet<String>();
	// // for (String LN : LNs) {
	// // sql =
	// // "select NAME, S_START, S_END,STIME,ETIME from LINES where LN = "
	// // + LN.substring(0, 7);// LN
	// // Cursor cur = getMyDataBase().rawQuery(sql, null);
	// // while (cur.moveToNext()) {
	// // String buslineId = LN.substring(0, 7);
	// // String buslineSN = LN;
	// // String buslineTitle = cur.getString(0);
	// // String buslineAllName = cur.getString(0) + "("
	// // + cur.getString(1) + "-" + cur.getString(2) + ")";
	// // String destination = cur.getString(2);
	// // String startStation = cur.getString(1) + "";
	// // String endStation = cur.getString(2) + "";
	// // String startTime = cur.getString(3) + "";
	// // String endTime = cur.getString(4) + "";
	// //
	// // if (!lineName.contains(buslineTitle)
	// // && BuslineDBHelper.getInstance(myContext)
	// // .IsRtBusline(
	// // cur.getString(0) + "("
	// // + cur.getString(1) + "-"
	// // + cur.getString(2) + ")")) {
	// // Child child = new Child(buslineId, buslineTitle,
	// // buslineAllName, stationName, GY, GX,
	// // destination);
	// //
	// // child.setStartStation(startStation);
	// // child.setEndStation(endStation);
	// // child.setStartTime(startTime);
	// // child.setEndTime(endTime);
	// // child.setBuslineSN(buslineSN);
	// // lineName.add(buslineTitle);
	// // group.addChildrenItem(child);
	// // }
	// // }
	// // cur.close();
	// // }
	//
	// groupList.add(group);
	// }
	// cursor.close();
	// sortStationByDistance(groupList);
	// Set<String> lineName = new HashSet<String>();
	// int stationNum = 0;
	// for (Group group : groupList) {
	// if (stationNum == 3)
	// break;
	// lineName.clear();
	// String[] LNs = group.getLNs();
	// String stationName = group.getStationTitle();
	// String GY = group.getLatitide();
	// String GX = group.getLongitude();
	// for (String LN : LNs) {
	// sql = "select NAME, S_START, S_END,STIME,ETIME from LINES where LN = "
	// + LN.substring(0, 7);// LN
	// Cursor cur = getMyDataBase().rawQuery(sql, null);
	// while (cur.moveToNext()) {
	// if (lineName.size() < 3) {
	// String buslineId = LN.substring(0, 7);
	// String buslineSN = LN;
	// String buslineTitle = cur.getString(0);
	// String buslineAllName = cur.getString(0) + "("
	// + cur.getString(1) + "-" + cur.getString(2)
	// + ")";
	// String destination = cur.getString(2);
	// String startStation = cur.getString(1) + "";
	// String endStation = cur.getString(2) + "";
	// String startTime = cur.getString(3) + "";
	// String endTime = cur.getString(4) + "";
	//
	// if (!lineName.contains(buslineTitle)
	// && BuslineDBHelper.getInstance(myContext)
	// .IsRtBusline(
	// cur.getString(0) + "("
	// + cur.getString(1)
	// + "-"
	// + cur.getString(2)
	// + ")")) {
	// Child child = new Child(buslineId, buslineTitle,
	// buslineAllName, stationName, GY, GX,
	// destination);
	//
	// child.setStartStation(startStation);
	// child.setEndStation(endStation);
	// child.setStartTime(startTime);
	// child.setEndTime(endTime);
	// child.setBuslineSN(buslineSN);
	// lineName.add(buslineTitle);
	// group.addChildrenItem(child);
	// }
	// }
	// }
	// cur.close();
	// }
	// if (group.getChildrenCount() > 0) {
	// stationNum++;
	// } else {
	// groupList.remove(group);
	// }
	// }
	// return groupList;
	// }
//	public List<Group> AcquireNearInfoWithLocation(LatLng location,
//			List<Group> groupList) {
//		groupList.clear();
//		float latRadius = 0.0f;
//		float lngRadius = 0.0f;
//
//		SharedPreferences mySharedPreferences = myContext.getSharedPreferences(
//				"setting", Context.MODE_PRIVATE);
//		String searchradius = mySharedPreferences.getString("searchRMode",
//				"800米");
//		int radius = Integer.parseInt(searchradius.subSequence(0,
//				searchradius.length() - 1).toString());
//		switch (radius) {
//		case 600:
//			latRadius = 0.004f;
//			lngRadius = 0.005f;
//			break;
//		case 700:
//			latRadius = 0.005f;
//			lngRadius = 0.0065f;
//			break;
//		case 800:
//			latRadius = 0.006f;
//			lngRadius = 0.008f;
//			break;
//		case 900:
//			latRadius = 0.007f;
//			lngRadius = 0.009f;
//			break;
//		case 1000:
//			latRadius = 0.008f;
//			lngRadius = 0.010f;
//			break;
//		case 1100:
//			latRadius = 0.009f;
//			lngRadius = 0.011f;
//			break;
//		case 1200:
//			latRadius = 0.009f;
//			lngRadius = 0.012f;
//			break;
//		case 1300:
//			latRadius = 0.010f;
//			lngRadius = 0.013f;
//			break;
//		case 1400:
//			latRadius = 0.010f;
//			lngRadius = 0.014f;
//			break;
//		case 1500:
//			latRadius = 0.011f;
//			lngRadius = 0.015f;
//			break;
//		default:
//			break;
//		}
//
//		double lat = location.latitude;
//		double lng = location.longitude;
//		double smallLat = lat - latRadius;
//		double smallLng = lng - lngRadius;
//		double bigLat = lat + latRadius;
//		double bigLng = lng + lngRadius;
//		// ���Ҹ�����վ
//		String sql = "select AZ,NAME,GY,GX,GROUP_CONCAT(LNs)as LNs from CSTATIONS where  GY > "
//				+ smallLat
//				+ " and GY < "
//				+ bigLat
//				+ " and GX > "
//				+ smallLng
//				+ " and GX < " + bigLng + " group by NAME";
//		Cursor cursor = getMyDataBase().rawQuery(sql, null);
//		while (cursor.moveToNext()) {
//			LatLng stationPoint = new LatLng(cursor.getDouble(2),
//					cursor.getDouble(3));
//			double distance = DistanceUtil.getDistance(location, stationPoint);
//			String stationName = cursor.getString(1);
//			double GY = cursor.getDouble(2);
//			double GX = cursor.getDouble(3);
//			int AZ = Integer.parseInt(cursor.getString(3));
//			String[] LNs = cursor.getString(4).split("[,;]");
//
//			Group group = new Group(stationName, GY, GX);
//			group.setDistance(distance);
//			group.setLNs(LNs);
//			Log.i(TAG, stationName);
//			Set<String> lineName = new HashSet<String>();
//			for (String LN : LNs) {
//				sql = "select NAME, S_START, S_END,STIME,ETIME from LINES where LN = "
//						+ LN.substring(0, 7);// LN
//				Cursor cur = getMyDataBase().rawQuery(sql, null);
//				while (cur.moveToNext()) {
//					int buslineId = Integer.parseInt(LN.substring(0, 7));
//					String buslineSN = LN;
//					String buslineTitle = cur.getString(0);
//					String buslineAllName = cur.getString(0) + "("
//							+ cur.getString(1) + "-" + cur.getString(2) + ")";
//					String destination = cur.getString(2);
//					String startStation = cur.getString(1) + "";
//					String endStation = cur.getString(2) + "";
//					String startTime = cur.getString(3) + "";
//					String endTime = cur.getString(4) + "";
//
//					if (!lineName.contains(buslineTitle)
//							&& BuslineDBHelper.getInstance(myContext)
//									.IsRtBusline(
//											cur.getString(0) + "("
//													+ cur.getString(1) + "-"
//													+ cur.getString(2) + ")")) {
//						Child child = new Child(buslineId, 0, buslineTitle,
//								buslineAllName, stationName, GY, GX);
//						int type = -1;
//						if ((type = UserDataDBHelper.getInstance(myContext)
//								.IsWhichKindFavInfo(buslineSN)) != -1)
//							child.setType(type);
//						else
//							child.setType(-1);
//						child.setStartStation(startStation);
//						child.setEndStation(endStation);
//						child.setStartTime(startTime);
//						child.setEndTime(endTime);
//						// child.setBuslineSN(buslineSN);
//						child.setAZ(AZ);
//						lineName.add(buslineTitle);
//						group.addChildrenItem(child);
//					}
//				}
//				cur.close();
//			}
//
//			groupList.add(group);
//		}
//		cursor.close();
//		sortStationByDistance(groupList);
//
//		return groupList;
//	}

	private void sortStationByDistance(List<Group> groupList) {
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

		Collections.sort(groupList, comparator);
	}

	// collectWifi用到的
	public ArrayList<Map<String, String>> getNearBusInfo(LatLng location) {
		ArrayList<Map<String, String>> buslines = new ArrayList<Map<String, String>>();
		ArrayList<ArrayList<String>> stations = AcquireAroundStationsWithLocation(location);
		ArrayList<ArrayList<String>> lineStop = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < stations.size(); i++) {
			String lineString = stations.get(i).get(4);
			String stationLat = stations.get(i).get(3);
			String[] l = lineString.split(";");
			for (int k = 0; k < l.length; k++) {
				int j = 0;
				for (j = 0; j < lineStop.size(); j++) {
					if (lineStop.get(j).get(0)
							.equalsIgnoreCase(l[k].substring(0, 7))) {
						break;
					}
				}
				if (j == lineStop.size()) {
					ArrayList<String> item = new ArrayList<String>();
					item.add(l[k].substring(0, 7));
					item.add(stations.get(i).get(1));
					item.add(stations.get(i).get(5));
					lineStop.add(item);
				}
			}

		}
		// 根据线路id查找线路名，需要显示线路名，线路id需要纪录，传到下一页面，查看线路详情。
		for (int i = 0; i < lineStop.size(); i++) {
			String sql = "select NAME, S_START, S_END from LINES where LN = "
					+ lineStop.get(i).get(0);
			Cursor cursor = getMyDataBase().rawQuery(sql, null);
			while (cursor.moveToNext()) {
				Map<String, String> summary = new HashMap<String, String>();
				summary.put("距离",
						(int) Double.parseDouble(lineStop.get(i).get(2)) + "");
				summary.put("车站", lineStop.get(i).get(1));
				summary.put("方向", cursor.getString(2));
				summary.put("线路", cursor.getString(0));
				buslines.add(summary);
			}
			cursor.close();
		}

		return buslines;
	}

	// collectWifi用到的
	public Cursor FindBusByKeyname(String keyWord) {
		Cursor cursor = getMyDataBase()
				.rawQuery(
						"select * from LINES where NAME like ? group by NAME order by NAME",
						new String[] { keyWord + "%" });
		return cursor;
	}

	public ArrayList<ArrayList<String>> getSpecificStationsWithBuslineName(
			String busline) {
		busline = busline.replace("路(", "(");
		ArrayList<ArrayList<String>> buslines = getBusLinesWithKeyword(busline
				.substring(0, busline.indexOf("-")));
		if (buslines.size() > 0) {
			return AcquireStationsWithBuslineID(Integer.parseInt(buslines
					.get(0).get(0).toString()));
		}
		return null;
	}

	public String getBuslineIdWithBuslineName(String busline) {
		busline = busline.replace("路(", "(");
		ArrayList<ArrayList<String>> buslines = getBusLinesWithKeyword(busline
				.substring(0, busline.indexOf("-")));
		if (buslines.size() > 0) {
			return buslines.get(0).get(0).toString();
		}
		return null;
	}

	public ArrayList<ArrayList<String>> getStationsWithStationKeyword(
			String keyword) {
		ArrayList<ArrayList<String>> stations = new ArrayList<ArrayList<String>>();
		String sql = "select AZ,NAME,GY,GX,LNs from CSTATIONS where  NAME  like \'"
				+ keyword + "%\'";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);

		while (cursor.moveToNext()) {
			ArrayList<String> arrayList = new ArrayList<String>();
			arrayList.add(cursor.getInt(0) + "");// AZ
			arrayList.add(cursor.getString(1));// NAME
			arrayList.add(cursor.getString(2) + "");// GY
			arrayList.add(cursor.getString(3) + "");// GX
			arrayList.add(cursor.getString(4));// LNs
			String[] linesNum = cursor.getString(4).split(";");
			arrayList.add(linesNum.length + "");// 线路数量

			stations.add(arrayList);
		}
		cursor.close();
		// close();
		return stations;
	}

	public ArrayList<ArrayList<String>> searchBusLinesWithKeyword(String keyword) {
		ArrayList<ArrayList<String>> buslines = new ArrayList<ArrayList<String>>();
		// openDataBase();

		String sql = "select LN, NAME, S_START, S_END,STIME,ETIME from LINES  where NAME like \'"
				+ keyword + "%\' order by NAME asc ";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);

		while (cursor.moveToNext()) {
			ArrayList<String> arrayList = new ArrayList<String>();
			arrayList.add(cursor.getString(0));// ID
			arrayList.add(cursor.getString(1));// KeyName
			arrayList.add(cursor.getString(1) + "(" + cursor.getString(2) + "-"
					+ cursor.getString(3) + ")");// FullName
			arrayList.add(cursor.getString(2));// s_start
			arrayList.add(cursor.getString(3));// s_end
			arrayList.add(cursor.getString(4));// s_start
			arrayList.add(cursor.getString(5));// s_end
			int i = 0;
			for (i = 0; i < buslines.size(); i++) {
				ArrayList<String> array = buslines.get(i);
				if (getNumWithStr(cursor.getString(1)) < getNumWithStr(array
						.get(1))) {
					buslines.add(i, arrayList);
					break;
				}
			}
			if (i == buslines.size()) {
				buslines.add(arrayList);
			}
		}
		cursor.close();

		// close();
		return buslines;
	}

	public ArrayList<ArrayList<String>> getBusLinesWithKeyword(String keyword) {
		ArrayList<ArrayList<String>> buslines = new ArrayList<ArrayList<String>>();
		// openDataBase();

		Log.i("keyword", keyword);
		String sql = "select LN, NAME, S_START, S_END from LINES  where NAME like \'%"
				+ keyword.substring(0, keyword.indexOf("("))
				+ "%\' and S_START like  \'%"
				+ keyword.substring(keyword.indexOf("(") + 1,
						keyword.indexOf("(") + 3) + "%\' order by NAME asc ";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);

		while (cursor.moveToNext()) {
			ArrayList<String> arrayList = new ArrayList<String>();
			arrayList.add(cursor.getString(0));
			arrayList.add(cursor.getString(1));
			arrayList.add(cursor.getString(1) + "(" + cursor.getString(2) + "-"
					+ cursor.getString(3) + ")");

			int i = 0;
			for (i = 0; i < buslines.size(); i++) {
				ArrayList<String> array = buslines.get(i);
				if (getNumWithStr(cursor.getString(1)) < getNumWithStr(array
						.get(1))) {
					buslines.add(i, arrayList);
					break;
				}
			}
			if (i == buslines.size()) {
				buslines.add(arrayList);
			}
		}
		cursor.close();
		// close();
		return buslines;
	}

	public ArrayList<ArrayList<String>> AcquireOtherBranchBusLinesWithBuslineName(
            String keyword, int BuslineID) {
		Log.i("keyword", keyword);
		ArrayList<ArrayList<String>> buslines = new ArrayList<ArrayList<String>>();
		String sql = "";
		if (keyword.lastIndexOf("内") == keyword.length() - 1) {
			sql = "select LN, NAME, S_START, S_END ,STIME,ETIME from LINES  where substr(NAME,1,length(Name)-1) = \'"
					+ keyword.substring(0, keyword.length() - 1)
					+ "\' "
					+ "order by NAME asc ";
			Log.i("sql", sql);

		} else if (keyword.lastIndexOf("外") == keyword.length() - 1) {
			sql = "select LN, NAME, S_START, S_END ,STIME,ETIME from LINES   where substr(NAME,1,length(Name)-1) = \'"
					+ keyword.substring(0, keyword.length() - 1)
					+ "\' "
					+ "order by NAME asc ";
			Log.i("sql", sql);

		} else {
			Log.i("keyword", keyword);
			sql = "select LN, NAME, S_START, S_END,STIME,ETIME  from LINES  where NAME = \'"
					+ keyword + "\' order by NAME asc ";
			Log.i("sql", sql);

		}
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		while (cursor.moveToNext()) {
			if (!cursor.getString(0).trim().equalsIgnoreCase(BuslineID + "")) {
				ArrayList<String> arrayList = new ArrayList<String>();
				arrayList.add(cursor.getString(0));// LN
				arrayList.add(cursor.getString(1));// KeyName
				arrayList.add(cursor.getString(1) + "(" + cursor.getString(2)
						+ "-" + cursor.getString(3) + ")");// FullName
				arrayList.add(cursor.getString(2));// startStation
				arrayList.add(cursor.getString(3));// endStation
				arrayList.add(cursor.getString(4));// startTime
				arrayList.add(cursor.getString(5));// endTime
				buslines.add(arrayList);
			}
		}
		cursor.close();
		return buslines;
	}

	public int getNumWithStr(String str) {
		str = str.trim();
		String str2 = "-1";
		if (str != null && !"".equals(str)) {
			for (int i = 0; i < str.length(); i++) {
				if (str.charAt(i) >= 48 && str.charAt(i) <= 57) {
					str2 += str.charAt(i);
				}
			}
		}
		return Integer.parseInt(str2);
	}

	public ArrayList<ArrayList<String>> AcquireStationsWithBuslineID(
			int buslineId) {
		ArrayList<ArrayList<String>> stations = new ArrayList<ArrayList<String>>();
		// openDataBase();

		String sql = "select NAME,GY,GX ,SN ,AZ from STATIONS where substr(SN,0,8) = \'"
				+ buslineId + "\' order by substr(SN,8,10) asc";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		while (cursor.moveToNext()) {
			ArrayList<String> arrayList = new ArrayList<String>();
			arrayList.add(cursor.getString(0));// stationName
			arrayList.add(cursor.getString(1) + "");// GY
			arrayList.add(cursor.getString(2) + "");// GX
			arrayList.add(cursor.getString(3));// SN
			arrayList.add(cursor.getString(4));// AZ
			stations.add(arrayList);
		}
		cursor.close();
		return stations;
	}

	public ArrayList<ArrayList<String>> getBusLinesWithStation(
			ArrayList<String> station) {
		ArrayList<ArrayList<String>> buslines = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> stations = new ArrayList<ArrayList<String>>();

		String sql = "select LNs,GY,GX from CSTATIONS  where NAME = \'"
				+ station.get(0) + "\'";
		Log.i("buslineIds", "sql = " + sql);
		Cursor cursor = getMyDataBase().rawQuery(sql, null);

		MostSimilarString mostSimilarString = new MostSimilarString();
		String buslineIds = null;
		String[] buslineidArr = null;
		float maxSimilar = 0;
		int maxsimilarIndex = 0;
		float curSimilar;
		while (cursor.moveToNext()) {
			ArrayList<String> arrayList = new ArrayList<String>();
			arrayList.add(cursor.getString(0));
			arrayList.add(cursor.getString(1));
			arrayList.add(cursor.getString(2));
			stations.add(arrayList);
			curSimilar = mostSimilarString.getSimilarityRatio(
					cursor.getString(1), station.get(1))
					+ mostSimilarString.getSimilarityRatio(cursor.getString(2),
							station.get(2));
			if (curSimilar > maxSimilar) {
				maxSimilar = curSimilar;
				maxsimilarIndex = stations.size() - 1;
			}
		}
		if (stations.size() == 0) {
			buslineIds = null;
		} else {
			buslineIds = stations.get(maxsimilarIndex).get(0);
		}
		Log.i("buslineIds", "buslineIds = " + buslineIds);
		cursor.close();
		if (buslineIds == null) {
			return buslines;
		} else {
			buslineidArr = buslineIds.split(";");
			for (String stemp : buslineidArr) {
				if (stemp.length() > 7) {
					String sql2 = "select LN, NAME, S_START, S_END from LINES where LN ="
							+ stemp.substring(0, 7);
					Cursor cursor2 = getMyDataBase().rawQuery(sql2, null);
					while (cursor2.moveToNext()) {
						ArrayList<String> arrayList = new ArrayList<String>();
						arrayList.add(cursor2.getString(0));
						arrayList.add(cursor2.getString(1));
						arrayList.add(cursor2.getString(1) + "("
								+ cursor2.getString(2) + "-"
								+ cursor2.getString(3) + ")");
						buslines.add(arrayList);
						break;
					}
					cursor2.close();
				}

			}
		}
		// close();
		return buslines;
	}

	public ArrayList<ArrayList<ArrayList<String>>> getBothsideBusLinesWithStation(
			ArrayList<String> station) {
		ArrayList<ArrayList<ArrayList<String>>> bothsideBusLines = new ArrayList<ArrayList<ArrayList<String>>>();
		ArrayList<ArrayList<String>> stations = new ArrayList<ArrayList<String>>();
		// openDataBase();
		String sql = "select LNs,GY,GX from CSTATIONS  where NAME = \'"
				+ station.get(0) + "\'";
		Log.i("buslineIds", "sql = " + sql);
		Cursor cursor = getMyDataBase().rawQuery(sql, null);

		String buslineIds = null;
		String[] buslineidArr = null;
		while (cursor.moveToNext()) {
			ArrayList<String> arrayList = new ArrayList<String>();
			arrayList.add(cursor.getString(0));
			Log.i("buslineIds", "buslineIds = " + cursor.getString(0));
			arrayList.add(cursor.getString(1));
			arrayList.add(cursor.getString(2));
			stations.add(arrayList);
		}
		cursor.close();
		int j = stations.size();
		if (j == 0) {
			return null;
		}
		ArrayList<ArrayList<String>> buslines;
		for (int i = 0; i < j; i++) {
			buslines = new ArrayList<ArrayList<String>>();
			buslineIds = stations.get(i).get(0);
			buslineidArr = buslineIds.split(";");
			for (String stemp : buslineidArr) {
				if (stemp.length() > 7) {
					String sql2 = "select LN, NAME, S_START, S_END from LINES where LN ="
							+ stemp.substring(0, 7);
					Cursor cursor2 = getMyDataBase().rawQuery(sql2, null);
					while (cursor2.moveToNext()) {
						ArrayList<String> arrayList = new ArrayList<String>();
						arrayList.add(cursor2.getString(0));
						arrayList.add(cursor2.getString(1));
						arrayList.add(cursor2.getString(1) + "("
								+ cursor2.getString(2) + "-"
								+ cursor2.getString(3) + ")");
						buslines.add(arrayList);
						break;
					}
					cursor2.close();
				}

			}
			stations.get(i).add(station.get(0));
			buslines.add(stations.get(i));
			bothsideBusLines.add(buslines);
		}
		return bothsideBusLines;
	}

	// 获得查询站点的线路信息
	public ArrayList<Child> AcquireBusLinesWithStation(ArrayList<String> station) {
		ArrayList<Child> lineList = new ArrayList<Child>();
		// openDataBase();
		String sql = "select LNs,GY,GX from CSTATIONS  where NAME = \'"
				+ station.get(0) + "\' and GX=\'" + station.get(1)
				+ "\' and GY=\'" + station.get(2) + "\'";
		Log.i("buslineIds", "sql = " + sql);
		Cursor cursor = getMyDataBase().rawQuery(sql, null);

		String[] buslineIDArr = null;
		if (cursor.moveToNext()) {
			buslineIDArr = cursor.getString(0).split("[,:]");
			for (String Id : buslineIDArr) {
				String sql1 = "select LN, NAME, S_START, S_END,STIME,ETIME from LINES where LN ="
						+ Id.substring(0, 7);
				Cursor cursor1 = getMyDataBase().rawQuery(sql1, null);
				if (cursor1.moveToNext()) {

					Child child = new Child(
							Integer.parseInt(Id.substring(0, 7)), 0,
							cursor1.getString(1), cursor1.getString(1) + "("
									+ cursor1.getString(2) + "-"
									+ cursor1.getString(3) + ")",
							station.get(0), Double.parseDouble(station.get(1)),
							Double.parseDouble(station.get(2)));

					lineList.add(child);
				}
				cursor1.close();
			}

		}
		cursor.close();
		return lineList;
	}

//	// 获得所有同名站点的分支，将点击查询的站点放在第一个
//	public ArrayList<Group> AcquireAllBranchBusLinesWithStation(
//			ArrayList<Group> groupList, ArrayList<String> station) {
//		groupList.clear();
//		String sql = "select LNs,GY,GX from CSTATIONS  where NAME = \'"
//				+ station.get(0) + "\' ";
//		Log.i("buslineIds", "sql = " + sql);
//		Cursor cursor = getMyDataBase().rawQuery(sql, null);
//		String stationName = station.get(0);
//		final double GY = Double.parseDouble(station.get(1));
//		final double GX = Double.parseDouble(station.get(2));
//
//		String[] buslineIDArr = null;
//		while (cursor.moveToNext()) {
//
//			Group group = new Group(station.get(0), cursor.getDouble(1),
//					cursor.getDouble(2));
//			groupList.add(group);
//			buslineIDArr = cursor.getString(0).split(";");
//			Log.i(TAG, cursor.getString(0));
//
//			for (String Id : buslineIDArr) {
//				Log.i(TAG, "id " + Id);
//
//				String sql1 = "select LN, NAME, S_START, S_END,STIME,ETIME from LINES where LN ="
//						+ Id.substring(0, 7);
//				Cursor cursor1 = getMyDataBase().rawQuery(sql1, null);
//				if (cursor1.moveToFirst()) {
//					if (BuslineDBHelper.getInstance(myContext).IsRtBusline(
//							cursor1.getString(1) + "(" + cursor1.getString(2)
//									+ "-" + cursor1.getString(3) + ")")) {
//						String startStation = cursor1.getString(2) + "";
//						String endStation = cursor1.getString(3) + "";
//						String startTime = cursor1.getString(4) + "";
//						String endTime = cursor1.getString(5) + "";
//						Child child = new Child(Integer.parseInt(Id.substring(
//								0, 7)), 0, cursor1.getString(1),
//								cursor1.getString(1) + "("
//										+ cursor1.getString(2) + "-"
//										+ cursor1.getString(3) + ")",
//								stationName, cursor.getDouble(1),
//								cursor.getDouble(2));
//						child.setStartStation(startStation);
//						child.setEndStation(endStation);
//						child.setStartTime(startTime);
//						child.setEndTime(endTime);
//						ArrayList<String> arr = acquireSNAndAZWithStaionNameAndBuslineID(
//								Id.substring(0, 7), station.get(0));
//						// child.setBuslineSN(arr.get(0));
//						child.setAZ(Integer.parseInt(arr.get(1)));
//						int type = -1;
//						// type = UserDataDBHelper.getInstance(myContext)
//						// .IsWhichKindFavInfo(child.getBuslineSN());
//						// if (type != -1)
//						// child.setType(type + "");
//						// else
//						// child.setType(null);
//						group.addChildrenItem(child);
//					}
//					cursor1.close();
//
//				}
//			}
//
//		}
//
//		cursor.close();
//
//		Comparator<Group> comparator = new Comparator<Group>() {
//			public int compare(Group s1, Group s2) {
//				if (s1.getLatitide() == GY && s1.getLongitude() == GX)
//					return 1;
//				else
//					return -1;
//			}
//		};
//
//		Collections.sort(groupList, comparator);
//
//		return groupList;
//	}

	// 获得所有同名站点的分支，将点击查询的站点放在第一个
	public ArrayList<Child> AcquireAllBusLinesWithStation(String station) {
		ArrayList<Child> children = new ArrayList<Child>();
		String sql = "select LNs,GY,GX from CSTATIONS  where NAME = \'"
				+ station + "\' ";
		Log.i("buslineIds", "sql = " + sql);
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		String[] buslineIDArr = null;
		while (cursor.moveToNext()) {
			buslineIDArr = cursor.getString(0).split(";");
			Log.i(TAG, cursor.getString(0));

			for (String Id : buslineIDArr) {
				Log.i(TAG, "id " + Id);

				String sql1 = "select LN, NAME, S_START, S_END,STIME,ETIME from LINES where LN ="
						+ Id.substring(0, 7);
				Cursor cursor1 = getMyDataBase().rawQuery(sql1, null);
				if (cursor1.moveToFirst()) {
					String startStation = cursor1.getString(2) + "";
					String endStation = cursor1.getString(3) + "";
					String startTime = cursor1.getString(4) + "";
					String endTime = cursor1.getString(5) + "";
					Child child = new Child(
							Integer.parseInt(Id.substring(0, 7)), 0,
							cursor1.getString(1), cursor1.getString(1) + "("
									+ cursor1.getString(2) + "-"
									+ cursor1.getString(3) + ")", station,
							cursor.getDouble(1), cursor.getDouble(2));
					child.setStartStation(startStation);
					child.setEndStation(endStation);
					child.setStartTime(startTime);
					child.setEndTime(endTime);
					ArrayList<String> arr = acquireSNAndAZWithStaionNameAndBuslineID(
							Id.substring(0, 7), station);
//					child.setBuslineSN(arr.get(0));
					child.setAZ( Integer.parseInt(arr.get(1)));
					int type = -1;
					// type = UserDataDBHelper.getInstance(myContext)
					// .IsWhichKindFavInfo(child.getBuslineSN());
					// if (type != -1)
					// child.setType(type + "");
					// else
					// child.setType(null);
					children.add(child);
				}
				cursor1.close();

			}
		}
		cursor.close();

		return children;
	}

	private ArrayList<String> acquireSNAndAZWithStaionNameAndBuslineID(
            String buslineID, String stationName) {
		String sql = "select * from STATIONS where SN like \'" + buslineID
				+ "%\'";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		while (cursor.moveToNext()) {
			if (cursor.getString(1).equals(stationName)) {
				break;
			}
		}
		ArrayList<String> arr = new ArrayList<String>();
		arr.add(cursor.getString(0));
		arr.add(cursor.getString(4));
		cursor.close();
		return arr;
	}

	public int AcquireStationSeqWithBusline(String buslineID, String stationName) {
		int seq = -1;
		String sql = "select SN from STATIONS where SUBSTR(LN,1,8) =\'"
				+ buslineID + "\' and NAME =\'" + stationName + "\'";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);
		while (cursor.moveToNext()) {
			seq = Integer.parseInt((cursor.getString(0).substring(7, 9)));
		}
		cursor.close();
		return seq;
	}

	public ArrayList<ArrayList<String>> getBusLinesWithStationName(
			String station) {
		ArrayList<ArrayList<String>> buslines = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> stations = new ArrayList<ArrayList<String>>();
		// openDataBase();
		String sql = "select LNs,GY,GX from CSTATIONS  where NAME = \'"
				+ station + "\'";
		Cursor cursor = getMyDataBase().rawQuery(sql, null);

		MostSimilarString mostSimilarString = new MostSimilarString();
		String buslineIds = null;
		String[] buslineidArr = null;
		float maxSimilar = 0;
		int maxsimilarIndex = 0;
		float curSimilar;
		while (cursor.moveToNext()) {
			ArrayList<String> arrayList = new ArrayList<String>();
			arrayList.add(cursor.getString(0));// LNs
			arrayList.add(cursor.getString(1));// GY
			arrayList.add(cursor.getString(2));// GX
			stations.add(arrayList);
			curSimilar = mostSimilarString.getSimilarityRatio(
					cursor.getString(1), station)
					+ mostSimilarString.getSimilarityRatio(cursor.getString(2),
							station);
			if (curSimilar > maxSimilar) {
				maxSimilar = curSimilar;
				maxsimilarIndex = stations.size() - 1;
			}
		}
		if (stations.size() == 0) {
			buslineIds = null;
		} else {
			buslineIds = stations.get(maxsimilarIndex).get(0);
		}
		cursor.close();
		if (buslineIds == null) {
			return buslines;
		} else {
			buslineidArr = buslineIds.split(";");
			for (String stemp : buslineidArr) {
				String sql2 = "select LN, NAME, S_START, S_END from LINES where LN ="
						+ stemp.substring(0, 7);
				Cursor cursor2 = getMyDataBase().rawQuery(sql2, null);
				while (cursor2.moveToNext()) {
					ArrayList<String> arrayList = new ArrayList<String>();
					arrayList.add(cursor2.getString(0));// LN
					arrayList.add(cursor2.getString(1));// KeyName
					arrayList.add(cursor2.getString(1) + "("
							+ cursor2.getString(2) + "-" + cursor2.getString(3)
							+ ")");
					arrayList.add(station);// stationName
					arrayList.add(stations.get(maxsimilarIndex).get(1));// GY
					arrayList.add(stations.get(maxsimilarIndex).get(2));// Gx
					arrayList.add(cursor2.getString(3));// S_END

					buslines.add(arrayList);
					break;
				}
				cursor2.close();
			}
		}
		// close();
		return buslines;
	}

//	public ArrayList<ArrayList<String>> getRtBusLinesWithStationName(
//			String station) {
//		ArrayList<ArrayList<String>> buslines = new ArrayList<ArrayList<String>>();
//		ArrayList<ArrayList<String>> stations = new ArrayList<ArrayList<String>>();
//		// openDataBase();
//		String sql = "select LNs,GY,GX from CSTATIONS  where NAME = \'"
//				+ station + "\'";
//		Cursor cursor = getMyDataBase().rawQuery(sql, null);
//
//		MostSimilarString mostSimilarString = new MostSimilarString();
//		String buslineIds = null;
//		String[] buslineidArr = null;
//		float maxSimilar = 0;
//		int maxsimilarIndex = 0;
//		float curSimilar;
//		while (cursor.moveToNext()) {
//			ArrayList<String> arrayList = new ArrayList<String>();
//			arrayList.add(cursor.getString(0));// LNs
//			arrayList.add(cursor.getString(1));// GY
//			arrayList.add(cursor.getString(2));// GX
//			stations.add(arrayList);
//			curSimilar = mostSimilarString.getSimilarityRatio(
//					cursor.getString(1), station)
//					+ mostSimilarString.getSimilarityRatio(cursor.getString(2),
//							station);
//			if (curSimilar > maxSimilar) {
//				maxSimilar = curSimilar;
//				maxsimilarIndex = stations.size() - 1;
//			}
//		}
//		if (stations.size() == 0) {
//			buslineIds = null;
//		} else {
//			buslineIds = stations.get(maxsimilarIndex).get(0);
//		}
//		cursor.close();
//		if (buslineIds == null) {
//			return buslines;
//		} else {
//			buslineidArr = buslineIds.split(";");
//			for (String stemp : buslineidArr) {
//				String sql2 = "select LN, NAME, S_START, S_END from LINES where LN ="
//						+ stemp.substring(0, 7);
//				Cursor cursor2 = getMyDataBase().rawQuery(sql2, null);
//				while (cursor2.moveToNext()) {
//					if (BuslineDBHelper.getInstance(myContext).IsRtBusline(
//							cursor2.getString(1) + "(" + cursor2.getString(2)
//									+ "-" + cursor2.getString(3) + ")")) {
//						ArrayList<String> arrayList = new ArrayList<String>();
//						arrayList.add(cursor2.getString(0));// LN
//						arrayList.add(cursor2.getString(1));// KeyName
//						arrayList.add(cursor2.getString(1) + "("
//								+ cursor2.getString(2) + "-"
//								+ cursor2.getString(3) + ")");
//						arrayList.add(station);// stationName
//						arrayList.add(stations.get(maxsimilarIndex).get(1));// GY
//						arrayList.add(stations.get(maxsimilarIndex).get(2));// Gx
//						arrayList.add(cursor2.getString(3));// S_END
//
//						buslines.add(arrayList);
//					}
//					break;
//				}
//				cursor2.close();
//			}
//		}
//		// close();
//		return buslines;
//	}

	// public void DownFileWithUrl(final String urlString) {
	// DownedFileLength = 0;
	// // TODO Auto-generated method stub
	// Thread thread = new Thread() {
	// public void run() {
	// try {
	// DownFile(urlString);
	// } catch (Exception e) {
	// // TODO: handle exception
	// }
	// }
	// };
	// thread.start();
	// }

	// private void DownFile(String urlString) {
	//
	// /*
	// * 连接到服务器
	// */
	//
	// try {
	// URL url = new URL(urlString);
	// Log.i("urlString", urlString);
	// connection = url.openConnection();
	// if (connection.getReadTimeout() == 5) {
	// Log.i("---------->", "当前网络有问题");
	// // return;
	// }
	// inputStream = connection.getInputStream();
	//
	// } catch (MalformedURLException e1) {
	// // TODO Auto-generated catch block
	// e1.printStackTrace();
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// /*
	// * 文件的保存路径和和文件名其中Nobody.mp3是在手机SD卡上要保存的路径，如果不存在则新建
	// */
	//
	// File file1 = new File(DB_PATH + "update" + DB_NAME);
	// if (!file1.exists()) {
	// close();
	// file1.delete();
	// }
	// File file = new File(DB_PATH + "update" + DB_NAME);
	// if (!file.exists()) {
	// try {
	// file.createNewFile();
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// /*
	// * 向SD卡中写入文件,用Handle传递线程
	// */
	// Message message = new Message();
	// try {
	// outputStream = new FileOutputStream(file);
	// byte[] buffer = new byte[1024 * 4];
	// FileLength = connection.getContentLength();
	// message.what = 0;
	// handler.sendMessage(message);
	// while (DownedFileLength < FileLength) {
	// outputStream.write(buffer);
	// DownedFileLength += inputStream.read(buffer);
	// Log.i("-------->", DownedFileLength + "");
	// Message message1 = new Message();
	// message1.what = 1;
	// handler.sendMessage(message1);
	// }
	// Message message2 = new Message();
	// message2.what = 2;
	// handler.sendMessage(message2);
	// } catch (FileNotFoundException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

}
