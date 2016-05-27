package com.pop24.androidapp.external_dbs;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;

import com.pop24.androidapp.MyApp;
import com.pop24.androidapp.R;
import com.pop24.androidapp.Utility;
import com.pop24.androidapp.config.Constants;
import com.pop24.androidapp.internal_dbs.PointStruct;
import com.pop24.androidapp.internal_dbs.RouteLiveData;
import com.pop24.androidapp.internal_dbs.SQLiteDbsHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class ExternalDbsHelper implements Callback{
	String tag = "ExternalDbsHelper";
	Callback cb;
	Connection conn;
	Activity content;

	private String DB_NAME = "tomasDB";

	private static Integer idRoute;
	private static Integer[] permissions;

	private static int CMD;
	public static final int CMD_TRY_SEND_ROUTE = 0;
	public static final int CMD_TRY_SEND_PENDING_ROUTES = 1;
	public static final int CMD_TRY_LOGIN = 2;
	public static final int CMD_UPDATE_PERMISSIONS = 3;
	public static final int CMD_TRY_REGISTER = 4;


	public static final int RESULT_STATE_OK = 0;
	public static final int RESULT_STATE_FAILED = 1;
	public static final int RESULT_STATE_INVALID_USER_ID = 2;
	public static final int RESULT_STATE_NO_POINTS = 3;

	public static final int RESULT_USER_ALREADY_EXIST = 5;
	public static final int RESULT_USER_HAS_NOT_BEEN_INVITED = 6;
	
//	public void getPopulatedRoutes(){
//		new Service1().execute();
//	}

	public ExternalDbsHelper(Builder builder){
		this.cb = builder.cb;
		this.content = builder.content;
		this.idRoute = builder.idRoute;
		this.permissions = builder.permissions;
		this.CMD = builder.CMD;

	}

	public static class Builder {
		private Callback cb;
		private Activity content;

		private Integer idRoute;
		private Integer[] permissions;
		private int CMD;



		public Builder setContent(Activity content) {
			this.content = content;
			return this;
		}

		public Builder setIdRoute(Integer idRoute) {
			this.idRoute = idRoute;
			return this;
		}

		public Builder setPermissions(Integer... permissions) {
			this.permissions = permissions;
			return this;
		}

		public Builder setCMD(int CMD) {
			this.CMD = CMD;
			return this;
		}

		public Builder setCallback(Callback cb) {
			this.cb = cb;
			return this;
		}

		public ExternalDbsHelper build() {
			return new ExternalDbsHelper(this);
		}

	}


	public void execute(){
		new DbsConnector(this).connectDbs();
	}

	private void trySendRoute(int idRoute){
		Log.d(tag, "trySendRoute, idRoute : " + idRoute);

		new Service2().execute(idRoute);
	}

	private void trySendPendingRoutes(){
		//Log.d(tag, "tempRoute size : " + tempRoute.getPoints().size());

		//new Service2().execute(idRoute);
	}

	private void tryLogin(){
		new Service1().execute();
	}

	private void tryRegister(){
		new Service0().execute();
	}

	private void tryUpdatePermissions(Integer... permissions){
		new Service3().execute(permissions);
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch(msg.what) {
			case Config.DBS_CONNECTION:
				Connection dbsConnection = (Connection) msg.obj;
				if (dbsConnection != null) {
					this.conn = dbsConnection;
					Log.d(tag, "dbsConnection : " + dbsConnection.toString());
					//routes = handler(dbsConnection).getRoutes();
					//new ServicesHandler(this, dbsConnection).getPopulatedRoutes();
					handleRequestCommand();

				} else {
					Utility.showToast(content, R.string.String0030);
				}

				break;
		}
		return false;
	}

	private void handleRequestCommand(){
		switch(CMD){
			case CMD_TRY_SEND_ROUTE :
				trySendRoute(idRoute);
				break;

			case CMD_TRY_SEND_PENDING_ROUTES :
				trySendPendingRoutes();
				break;

			case CMD_TRY_LOGIN :
				tryLogin();
				break;

			case CMD_TRY_REGISTER :
				tryRegister();
				break;

			case CMD_UPDATE_PERMISSIONS :
				tryUpdatePermissions(permissions);
				break;
		}
	}

	private class Service0 extends AsyncTask<Void, Void, ResultSet> {
		private int RESULT_INTEGER = RESULT_STATE_FAILED;

		@Override
		protected ResultSet doInBackground(Void... params) {
			try {
				String nick = Utility.getSettingsString(content, Constants.NICK_NAME, "");
				String email = Utility.getSettingsString(content, Constants.EMAIL, "");
				String password = Utility.getSettingsString(content, Constants.PASSWORD, "");

				Log.d(tag, "Start registering  " + email + " , password = " + password);

				Statement statement;

				statement = conn.createStatement();
				ResultSet resultSet = statement.executeQuery("SELECT fleet.name FROM " + DB_NAME + ".fleet" +
						" WHERE email = \"" + email + "\"");

				if(resultSet.next()){
					if(!Utility.stringIsNullOrEmpty(resultSet.getString(1))) {
						RESULT_INTEGER = RESULT_USER_ALREADY_EXIST;
						return null;
					}
				}else{
					RESULT_INTEGER = RESULT_USER_HAS_NOT_BEEN_INVITED;
					return null;
				}


				statement = conn.createStatement();
				Integer result = statement.executeUpdate("UPDATE " + DB_NAME + ".fleet " +
						" SET pass = \"" + password + "\", name = \"" + nick + "\"" +
						" WHERE email = \"" + email+ "\"");


				statement = conn.createStatement();
				ResultSet resultSet2 = statement.executeQuery("SELECT fleet.name, id_fleet, id_cyclist, fl2.`name`, fl2.email, fleet.pass FROM " + DB_NAME + ".fleet" +
						" join " + DB_NAME + ".fleet_cyclist fc on fc.id_fleet = fleet.id join " + DB_NAME + ".fleet fl2 on fl2.id = fc.id_cyclist" +
						" WHERE fl2.email = \"" + email + "\" AND " +
						" fl2.pass = \"" + password + "\"");


				resultSet2.last();

				if (resultSet2.getRow() > 0) {
					resultSet2.beforeFirst();
					RESULT_INTEGER = RESULT_STATE_OK;
					return resultSet2;
				}
				else {
					Log.d(tag, "idUser no found");
					RESULT_INTEGER = RESULT_STATE_FAILED;
					return null;
				}

			} catch (SQLException e) {
				e.printStackTrace();
				RESULT_INTEGER = RESULT_STATE_FAILED;
				return null;
			}

		}

		@Override
		protected void onPostExecute(ResultSet resultSet) {
			cb.handleMessage(new Handler().obtainMessage(RESULT_INTEGER, 0, 0, resultSet));
		}
	}


	private class Service1 extends AsyncTask<Void, Void, ResultSet> {

			@Override
			protected ResultSet doInBackground(Void... params) {
				try {
					Statement statement = conn.createStatement();
					ResultSet resultSet = statement.executeQuery("SELECT fleet.name, id_fleet, id_cyclist, fl2.`name`, fl2.email, fleet.pass FROM "+DB_NAME+".fleet"+
							" join "+DB_NAME+".fleet_cyclist fc on fc.id_fleet = fleet.id join "+DB_NAME+".fleet fl2 on fl2.id = fc.id_cyclist"+
							" WHERE fl2.email = \""+Utility.getSettingsString(content, Constants.EMAIL, "")+"\" AND " +
							" fl2.pass = \""+Utility.getSettingsString(content, Constants.PASSWORD, "") + "\"");


					resultSet.last();

					if (resultSet.getRow() > 0) {
						resultSet.beforeFirst();
						return resultSet;
					}
					else {
						Log.d(tag, "idUser no found");
						return null;
					}

				} catch (SQLException e) {
					e.printStackTrace();
					return null;
				}

			}

			@Override
			protected void onPostExecute(ResultSet resultSet) {
				if(resultSet != null)
					cb.handleMessage(new Handler().obtainMessage(RESULT_STATE_OK, 0, 0, resultSet));
				else
					cb.handleMessage(new Handler().obtainMessage(RESULT_STATE_FAILED));
			}
		}

//	private class Service1 extends AsyncTask<Void, Void, List<Route>> {
//		List<Route> list = new ArrayList<Route>();
//
//		@Override
//		protected List<Route> doInBackground(Void... params) {
//			String from, to;
//
//			Statement statement;
//			ResultSet resultSet;
//
//			//obtain list
//			try {
//				statement = conn.createStatement();
//
//				resultSet = statement.executeQuery("SELECT * FROM admin.Jurney");
//
//				 while (resultSet.next()) {
//					 Log.d(tag, "resultSet : " + resultSet.getInt(1));
//
//					 list.add(new Route(resultSet.getInt(1),
//							 new LatLng(resultSet.getDouble(2), resultSet.getDouble(3)),
//							 new LatLng(resultSet.getDouble(4), resultSet.getDouble(5)),
//							 "", "", 0, false));
//				 }
//
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//
//			//populate list
//
//			for(int i = 0; i < list.size();i++){
//				from = new Service().getAddrFromLocation(list.get(i).getStartPos());
//				to = new Service().getAddrFromLocation(list.get(i).getEndPos());
//				list.get(i).setDistance(14.5);
//				list.get(i).setFrom(from);
//				list.get(i).setTo(to);
//			}
//
//
//			return list;
//		}
//
//		@Override
//		protected void onPostExecute(List<Route> list) {
//
//			cb.handleMessage(new Handler().obtainMessage(Config.RETURN_POPULATED_ROUTES, list));
//
//	    }
//
//
//	}
	
	private class Service2 extends AsyncTask<Integer, Void, Integer> {


		@Override
		protected Integer doInBackground(Integer... params) {
			SQLiteDbsHelper dbsHelper = new SQLiteDbsHelper(content);

			RouteLiveData routeLiveData;
			List<PointStruct> points;

			Integer idLocalRoute = params[0];
			Integer idExtInsertedRoute = null;

			int gender = Utility.getSettingsBoolean(MyApp.getContext(), Constants.PREF_GENDER, true) == true ? 1 : 0;
			int weight = Utility.getSettingsInteger(MyApp.getContext(), Constants.PREF_WEIGHT, 80);
			int age = Utility.getSettingsInteger(MyApp.getContext(), Constants.PREF_AGE, 25);

			Statement statement;
			ResultSet resultSet;

			Integer idCyclist = null;

			try {
				conn.setAutoCommit(false);

				//1. get id cyclist
				//1.2 check if id_local in route exist .. if no continue...
				//2. idRoute = insert route
				//3. insert cyclist_route
				//4. insert point, and set reference on idRoute

				idCyclist = Utility.getSettingsInteger(content, Constants.ID_USER, 0);
				Log.d(tag, "idCyclist : " + idCyclist + " , idLocalRoute : " + idLocalRoute );

				if (idCyclist == null)
					return RESULT_STATE_INVALID_USER_ID;

				statement = conn.createStatement();

				//2 INSERT route (routeLiveData)

				routeLiveData = dbsHelper.getRoute(idRoute);

				Log.d(tag, "_when : " + routeLiveData.getWhen() + " video_name : " + routeLiveData.getVideoName() + " burnedKcal : " + Math.abs(routeLiveData.getBurnedCalories()));

				statement.execute(" insert into " + DB_NAME + ".route " +
								"(id_local, name, distance, burned_kcal, avg_hr, max_hr, max_ee, avg_speed, max_speed, elevation, duration, video_name, age, weight, gender, _when)" +
								" values (" + routeLiveData.getIdRoute() + ", \"" + routeLiveData.getName() + "\", " + routeLiveData.getDistance() +
								", " + Math.abs(routeLiveData.getBurnedCalories()) + ", " + routeLiveData.getAvgHr() + ", " + routeLiveData.getMaxHr() + ", " + Math.abs(routeLiveData.getMaxEe()) +
								", " + routeLiveData.getAvgSpeed() + ", " + routeLiveData.getMaxSpeed() + ", " + routeLiveData.getElevation() +
								", " + routeLiveData.getDuration() + ", \"" + routeLiveData.getVideoName() + "\", " + age + " , " + weight + " , " + gender +
								", FROM_UNIXTIME(" + routeLiveData.getWhen()/1000 +") )",
						Statement.RETURN_GENERATED_KEYS);

				resultSet = statement.getGeneratedKeys();

				if (resultSet.next()) {
					idExtInsertedRoute = resultSet.getInt(1);
				} else {
					Log.d(tag, "no obtain id of inserted route");
				}

				//3. INSERT CYCLIST_ROUTE
				statement.execute(" insert into " + DB_NAME + ".cyclist_route (id_cyclist, id_route)" +
						" values (" + idCyclist + ", " + idExtInsertedRoute + ")");

				//4. INSERT POINTS

				points = dbsHelper.getPoints(idRoute);
				if(points == null)
					return RESULT_STATE_NO_POINTS;

				Log.d(tag, "points size : " + points.size());

				PreparedStatement prepStatement = conn.prepareStatement(" INSERT INTO " + DB_NAME + ".point(id_route, lat, lng, speed, hr, ee, _when) VALUES (?, ?, ?, ?, ?, ?, FROM_UNIXTIME(?))");
				for (PointStruct point : points) {
					Log.d(tag, "inserting point : " + (point.get_when()/1000) + " " + point.getSpeed());
					prepStatement.setInt(1, idExtInsertedRoute);
					prepStatement.setDouble(2, point.getLatLng().latitude);
					prepStatement.setDouble(3, point.getLatLng().longitude);
					prepStatement.setFloat(4, point.getSpeed());
					prepStatement.setInt(5, point.getHr());
					prepStatement.setFloat(6, point.getEe());
					prepStatement.setLong(7, point.get_when()/1000);
					prepStatement.execute();
					prepStatement.clearParameters();
				}

				Log.d(tag, "Transaction commit...");
				conn.commit();
				//now can remove local route
				Log.d(tag, "Removing route from local dbs with id = " + idLocalRoute);
				dbsHelper.removeRoute(idLocalRoute);
				Log.d(tag, "Removed route from local dbs with id = " + idLocalRoute);

				return RESULT_STATE_OK;

			} catch (SQLException e) {
				if (conn != null) {
					try {
						conn.rollback();
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
					Log.d(tag, "Connection rollback...");
				}
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Log.d(tag, "result : RESULT_STATE_OK");

			return RESULT_STATE_OK;
		}

		@Override
		protected void onPostExecute(Integer resultState) {
			if (resultState == RESULT_STATE_OK) {
				Log.d(tag, "RESULT_STATE_OK");
				cb.handleMessage(new Handler().obtainMessage(RESULT_STATE_OK));
			} else {
				Log.d(tag, "RESULT_STATE_FAILED");
				cb.handleMessage(new Handler().obtainMessage(RESULT_STATE_FAILED));
			}

	    }
		

	}

	private class Service3 extends AsyncTask<Integer, Void, Integer> {

		@Override
		protected Integer doInBackground(Integer... permission) {
			try {
				int idCyclist = Utility.getSettingsInteger(content, Constants.ID_USER, 0);
				int idFleet = Utility.getSettingsInteger(content, Constants.ID_FLEET, 0);

				Log.d(tag, "Start updating permissions idCyclist = " + idCyclist + " , idFleet = " + idFleet);

				Statement statement = conn.createStatement();

				Integer result = statement.executeUpdate("UPDATE " + DB_NAME + ".fleet join " + DB_NAME + ".fleet_cyclist fc on fc.id_fleet = fleet.id join " +
						DB_NAME + ".fleet fl2 on fl2.id = fc.id_cyclist " +
						"  SET state = 2, allow_recording = " + permission[0] + ", allow_switch_cam = " + permission[1] + ", allow_send_msg = " + permission[2] +
						" WHERE id_fleet = " + idFleet + " AND id_cyclist = " + idCyclist);


				return result;



			} catch (SQLException e) {
				e.printStackTrace();
				return -13;
			}

		}

		@Override
		protected void onPostExecute(Integer result) {
			if (result > 0) {
				Log.d(tag, "Updated permissions with result " + result);
			} else {
				Log.d(tag, "Updated permissions FAILED !");
			}
		}
	}

}
