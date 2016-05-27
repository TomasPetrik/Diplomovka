package com.pop24.androidapp.external_dbs;

public class Config {
	
	public static final int ON_DISMIS = 1;
	public static final int UPDATE_LOCATION = 10;
	public static final int DBS_CONNECTION = 20;
	public static final int RETURN_POPULATED_ROUTES = 21;
	public static final int SUCCES_SENT_ROUTE = 22;
	public static final int NO_SUCCES_SENT_ROUTE = 23;
	
	public static final int SEC_PER_ROUTE_SEGMENT = 120;
	
	public static final int MODE_BASE = 0;
	public static final int MODE_FIND_START = 1;
	public static final int MODE_RECORDING = 2;
	
	public static final int MIN_DISTANCE_POINTS = 150;
	public static final int SHOW_AREA_POINTS = 250;
	public static final int PADDING_MAP = 50;

	
	public static final int MIN_DISTANCE_GPS_TO_CALL_LOCATION_CHANGED = 1;	//metres
	public static final long REQ_TIME_GET_GPS_POINT = 500; //ms
	public static final int DEFAULT_TOAST_TIME = 3000; //ms
	
	/* API KEY*/
	public static final String API_KEY = "AIzaSyAmM8A4NVDuSWdfH6dVMgdSph4cshQeTFY";
	
	/* Geolocation*/
	
	public static final String getAddUriP1  = "https://maps.googleapis.com/maps/api/geocode/json?latlng=";
	public static final String getAddUriP2  = "&location_type=ROOFTOP&result_type=street_address&key=" + API_KEY;
	
	/* DBS */
	public static final String DRIVER_CLASS = "com.mysql.jdbc.Driver";
	public static final String URL = "jdbc:mysql://147.175.145.110:3306/admin";
	public static final String USER = "admin";
	public static final String PASSWORD = "fleetadmin951";
	
	public static final String SSH_USER = "android";
	public static final String SSH_PASSWORD = "fleetadmin951";
	
	public static final String PUBLIC_IP = "147.175.145.110";

	public static final String LOCAL_IP = "127.0.0.1";
}
