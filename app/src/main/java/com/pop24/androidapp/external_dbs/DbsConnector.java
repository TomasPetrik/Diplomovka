package com.pop24.androidapp.external_dbs;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Handler.Callback;

import java.sql.Connection;

public class DbsConnector{
	Callback cb;
	
	public DbsConnector(Callback cb){
		this.cb = cb;
		
	}
	
	public void connectDbs(){
		new Connector().execute();	
	}

	private class Connector extends AsyncTask<Void, Void, Connection> {

		Connection dbsConnection = null;
	
		@Override
		protected Connection doInBackground(Void... params) {
			
			
			dbsConnection = JDBCMySQLConnection.getConnection();
			
			return dbsConnection;
		}
		
		@Override
		protected void onPostExecute(Connection conn) {
	
			cb.handleMessage(new Handler().obtainMessage(Config.DBS_CONNECTION, conn));
			
	    }

	
	
	}

}
