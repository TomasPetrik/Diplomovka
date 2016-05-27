package com.pop24.androidapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.pop24.androidapp.config.Constants;
import com.pop24.androidapp.helpers.IUtilitySingleButtonHandler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.regex.Pattern;


/**
 * Created by Tomas on 3. 11. 2015.
 */
public class Utility {
    private static ProgressDialog m_progressDialog = null;

    public static String getSpeedInKmph(double value) {
        return String.valueOf(round((value * 3.6f), 2));
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    public static float getDistanceBetween(LatLng first, LatLng second){
        float[] distance = new float[1];
        Location.distanceBetween(first.latitude,
                first.longitude,
                second.latitude,
                second.longitude, distance);

        return distance[0];
    }

    public static String getTimeString(Long totalSecs){
        Long hours = totalSecs / 3600;
        Long minutes = (totalSecs % 3600) / 60;
        Long seconds = totalSecs % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static String numberFormatting(Float value) {
        return Utility.numberFormatting(value, "#,###,##0.00");
    }

    public static String numberFormatting(Float value, String format) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        DecimalFormat formatter = new DecimalFormat(format, symbols);
        return formatter.format(value).replace(",", ".");
    }

    public static boolean checkNetworkConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public static boolean checkLocationServiceEnabled(Context context){
        LocationManager lm = null;
        boolean gps_enabled = false;
        if(lm==null)
            lm = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        try{
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }catch(Exception ex){}

        if(!gps_enabled)
            return false;
        else return true;
    }

    public static void showAlertDialog(Context context, String title, String message, String okButtonText) {
        Utility.showAlertDialog(context, title, message, okButtonText, null);
    }
    public static void showAlertDialog(Context context, String title, String message, String okButtonText, final IUtilitySingleButtonHandler handler) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(okButtonText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (handler != null) {
                    handler.OnClick();
                }
            }
        });
        alertDialog.show();
    }

    public static void showProgressDialog(Context context, String title, String message) {
        try {
            if (Utility.m_progressDialog != null) {
                Utility.hideProgressDialog();
            }
            Utility.m_progressDialog = ProgressDialog.show(context, title, message, true);
        }
        catch (Exception ex) {
            Utility.m_progressDialog = null;
            Utility.log(ex);
        }
    }

    public static void hideProgressDialog() {
        try {
            if (Utility.m_progressDialog != null) {
                Utility.m_progressDialog.dismiss();
                Utility.m_progressDialog = null;
            }
        }
        catch (Exception ex) {
            Utility.m_progressDialog = null;
            Utility.log(ex);
        }
    }

    public static void showToast(Context context, int resString){
        Toast toast;
        toast = Toast.makeText(context, resString, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void setSettingsString(Context context, String value, String key){
        SharedPreferences settings = context.getSharedPreferences(Constants.USER_PREFERENCES, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }
    public static String getSettingsString(Context context, String key, String defaultValue) {
        try {
            SharedPreferences settings = context.getSharedPreferences(Constants.USER_PREFERENCES, 0);
            return settings.getString(key, defaultValue);
        }
        catch (Exception ex) {
            return defaultValue;
        }
    }

    public static void setSettingsInteger(Context context, int value, String key){
        SharedPreferences settings = context.getSharedPreferences(Constants.USER_PREFERENCES, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.commit();
    }
    public static Integer getSettingsInteger(Context context, String key, int defaultValue) {
        try {
            SharedPreferences settings = context.getSharedPreferences(Constants.USER_PREFERENCES, 0);
            return settings.getInt(key, defaultValue);
        }
        catch (Exception ex) {
            return defaultValue;
        }
    }

    public static void setSettingsBoolean(Context context, boolean value, String key){
        SharedPreferences settings = context.getSharedPreferences(Constants.USER_PREFERENCES, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
    public static boolean getSettingsBoolean(Context context, String key, boolean defaultValue) {
        try {
            SharedPreferences settings = context.getSharedPreferences(Constants.USER_PREFERENCES, 0);
            return settings.getBoolean(key, defaultValue);
        }
        catch (Exception ex) {
            return defaultValue;
        }
    }

    public static boolean isEmailAddress(String address) {
        if (Utility.stringIsNullOrEmpty(address)) {
            return false;
        } else {
            Pattern p = Pattern.compile( "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}\\@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+");
            return p.matcher(address).matches();
        }
    }

    public static boolean stringIsNullOrEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static void log(String text) {
        Log.i("Utility", ">>>>>>>>>> " + text);
    }
    public static void log(Exception exception) {
        Log.i("Utility", ">>>>>>>>>> " + exception.toString());
        Log.e("Utility", exception.getMessage(), exception);
    }

}
