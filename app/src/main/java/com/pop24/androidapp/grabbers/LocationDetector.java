package com.pop24.androidapp.grabbers;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.maps.LocationSource;
import com.pop24.androidapp.MyApp;
import com.pop24.androidapp.external_dbs.Config;

public class LocationDetector extends Activity implements LocationListener, LocationSource{
	private static LocationDetector instance;
	private String tag = "LocationDetector";

	//public MainActivity content = WsdApp.mainActivity;
	
	private OnLocationChangedListener listener;

	private Location myLastKnowLocation;
	
	private Callback cb;
	
	private Context context = MyApp.getContext();
	
	LocationManager locationManager;
	
	//final Controller aController = (Controller) getApplicationContext();
	
	public static LocationDetector getInstance(){
		if(instance == null)
			instance = new LocationDetector();
		
		return instance;		
	}
	
	public void addCallback(Callback cb){
		this.cb = cb;
	}

	public Boolean checkLolipopValidityApiPermissions(){
		if ( Build.VERSION.SDK_INT >= 23 &&
				ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
			return false;
		else
			return true;
	}

	public LocationDetector(){
		locationManager = (LocationManager)context.getSystemService(context.getApplicationContext().LOCATION_SERVICE);

		if ( Build.VERSION.SDK_INT >= 23 &&
				ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			locationManager.removeUpdates(this);
		}else {		//try get last gps

			if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null)
				myLastKnowLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			else if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
				myLastKnowLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			}else if (locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) != null) {
				myLastKnowLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
			}


			if(myLastKnowLocation != null) {
				//force set speed to zero, because user must first time speed = 0
				//myLastKnowLocation.setSpeed(0f);
				Log.d(tag, "found myLastKnowLocation : " + myLastKnowLocation.toString());
			}
			else
				Log.d(tag, "no found myLastKnowLocation !");
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		//Log.d(tag, "onLocationChanged " + location.getProvider() + " : " + location.getLatitude() + ", " + location.getLongitude() + ", speed:" + location.getSpeed());
		
		this.myLastKnowLocation = location;
		
		//content.firstLocalized = true;
		
		if(cb != null)
			cb.handleMessage(new Handler().obtainMessage(Config.UPDATE_LOCATION, location));
		
		if(listener != null)
        {
            listener.onLocationChanged(location);
        }
		//every time must call server for new events
		
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.d(tag, "onProviderDisabled() called: " + provider);
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.d(tag, "onProviderEnabled() called: " + provider);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		//Log.d(tag, "onStatusChanged() called: " + provider);
		
	}
	
	public Location getLastKnowLocation(){
		if(myLastKnowLocation != null)
			//return new LatLng(this.myLastKnowLocation.getLatitude(), this.myLastKnowLocation.getLongitude());
			return myLastKnowLocation;
		else return null;
	}
	
	public void stopRequestLocationUpdates(){
		if(locationManager != null)
			if ( Build.VERSION.SDK_INT >= 23 &&
					ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
					ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
				locationManager.removeUpdates(this);
		
	}
	
	public void tryStartRequestForLocation(){
		 boolean isNetEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
         boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

		if ( Build.VERSION.SDK_INT >= 23 &&
				ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
			return;

			locationManager.removeUpdates(this);
         
             if(isNetEnabled) {
                 locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Config.REQ_TIME_GET_GPS_POINT, 0, this);
             }

             if(isGpsEnabled) {
                 locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,  Config.REQ_TIME_GET_GPS_POINT, Config.MIN_DISTANCE_GPS_TO_CALL_LOCATION_CHANGED, this);
             }
     }
	
	public void stopRequestGpsLocationUpdates(){
		Log.d(tag, "stopRequestGpsLocationUpdates() called");
		if ( Build.VERSION.SDK_INT >= 23 &&
				ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
			return;

		locationManager.removeUpdates(this);
	}

	@Override
	public void activate(OnLocationChangedListener listener) {
		this.listener = listener;
		if(listener != null && myLastKnowLocation != null){
			listener.onLocationChanged(myLastKnowLocation);
		}

		if ( Build.VERSION.SDK_INT >= 23 &&
				ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
			return;
			
        LocationProvider gpsProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
        if(gpsProvider != null)
        {
            locationManager.requestLocationUpdates(gpsProvider.getName(), 0, 10, this);
        }

        LocationProvider networkProvider = locationManager.getProvider(LocationManager.NETWORK_PROVIDER);;
        if(networkProvider != null) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 60 * 5, 0, this);
        }
		
	}

	@Override
	public void deactivate() {
		if ( Build.VERSION.SDK_INT >= 23 &&
				ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
			return;
		locationManager.removeUpdates(this);
		
	}
	

}
