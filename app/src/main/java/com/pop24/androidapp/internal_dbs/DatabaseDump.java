package com.pop24.androidapp.internal_dbs;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public final class DatabaseDump {
	private static String tag = "DatabaseDump";

	public static final String TABLE_ROUTES = "routes";
	public static final String TABLE_POINTS = "points";
	
	public static final String dbsDumpSetForeignKeysSupport = "PRAGMA foreign_keys=ON; ";
	
	public static final String dbsDumpDropRoutes ="DROP TABLE IF EXISTS "+TABLE_ROUTES+"; ";
	public static final String dbsDumpRoutes = 
			"CREATE TABLE "+TABLE_ROUTES+"(id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "name TEXT, distance DOUBLE, burned_kcal DOUBLE, avg_hr DOUBLE, max_hr DOUBLE, max_ee DOUBLE, avg_speed DOUBLE, max_speed DOUBLE, elevation INTEGER, "
			+ " duration INTEGER, video_name TEXT, _when INTEGER);";


	public static final String dbsDumpDropPoints = "DROP TABLE IF EXISTS "+TABLE_POINTS+"; ";
	public static final String dbsDumpPoints =
			"CREATE TABLE "+TABLE_POINTS+"( "
					+ "id INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ "id_route INTEGER, "
					+ "lat DOUBLE, "
					+ "lng DOUBLE, "
					+ "speed DOUBLE, "
					+ "hr INTEGER, "
					+ "ee DOUBLE, "	//energy experditure
					+ "_when INTEGER, "
					+ "FOREIGN KEY (id_route) REFERENCES "+TABLE_ROUTES+"(id) ON DELETE CASCADE ON UPDATE CASCADE); ";

	public static final String TRIGER_1 = "";
	public static final String TRIGER_2 = "";

	//redundant dbs copy from server for user comfort

	
	public static List<String> getDatabaseDump(){
		List<String> list = new ArrayList<String>();

		list.add(dbsDumpDropRoutes);
		list.add(dbsDumpRoutes);
		list.add(dbsDumpDropPoints);
		list.add(dbsDumpPoints);
		
		Log.d(tag, "returned DatabaseDump");
		return list;
	}

}
