package com.pop24.androidapp.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.pop24.androidapp.R;
import com.pop24.androidapp.config.Constants;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class Utility {
    private static String tag = "Utility";

    public static int PICKER_ITEM_WIDTH = 250;
    private static ProgressDialog m_progressDialog = null;

    public static void setImageImageView(Activity activity, int id, int resourceId) {
        ImageView imageView = (ImageView)activity.findViewById(id);
        if (imageView != null) {
            imageView.setImageResource(resourceId);
        }
    }
    public static Boolean checkPaidProfie() {
        return false;
    }
    public static Boolean validateLoginData(Context context) {
        String email = Utility.getSettingsString(context, Constants.SETTINGS_KEY_USER_EMAIL, null);
        String password = Utility.getSettingsString(context, Constants.SETTINGS_KEY_USER_PASSWORD, null);
        String hash = Utility.getSettingsString(context, Constants.SETTINGS_KEY_USER_HASH, null);
        if (!Utility.stringIsNullOrEmpty(email) && !Utility.stringIsNullOrEmpty(password) && !Utility.stringIsNullOrEmpty(hash)) {
            return true;
        }
        return false;
    }
    public static Float parseFloat(String value) {
        try {
            value = value.replace(" ", "");
            if (!Utility.stringIsNullOrEmpty(value)) {
                return Float.parseFloat(value);
            }
        }
        catch (Exception ex) {
            Utility.log(ex);
        }
        return null;
    }
    public static String getVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(),0);
            return  packageInfo.versionName;
        }
        catch (PackageManager.NameNotFoundException e) {
            Utility.log(e.toString());
            return null;
        }
    }
    public static Date parseDate(String date, String format) {
        return Utility.parseDate(date, format, false);
    }
    public static Date parseDate(String date, String format, boolean gtm) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            if (gtm) {
                formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
            }
            return formatter.parse(date);
        }
        catch (ParseException e) {
            Utility.log(e);
            return null;
        }
    }
    public static Long getSettingsLong(Context context, String key, Long defaultValue) {
        try {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            return settings.getLong(key, defaultValue);
        }
        catch (Exception ex) {
            return defaultValue;
        }
    }


    public static Integer getSettingsValue(Context context, int index) {
        try {
            if (index >= 1) {
                Resources resources = context.getResources();
                TypedArray array = resources.obtainTypedArray(R.array.integer_array_settings);
                return array.getInteger(index, -1);
            }
            return null;
        }
        catch (Exception ex) {
            Utility.log(ex);
            return null;
        }
    }
    public static Integer getSettingsIndex(Context context, int value) {
        try {
            Resources resources = context.getResources();
            TypedArray array = resources.obtainTypedArray(R.array.integer_array_settings);
            for (int i = 0; i < array.length(); i++) {
                if (array.getInteger(i, -1) == value) {
                    return i;
                }
            }
            return null;
        }
        catch (Exception ex) {
            Utility.log(ex);
            return null;
        }
    }

    public static void log(String text) {
        Log.i("Utility", ">>>>>>>>>> " + text);
    }
    public static void log(Exception exception) {
        Log.i("Utility", ">>>>>>>>>> " + exception.toString());
        Log.e("Utility", exception.getMessage(), exception);
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
    public static String getSettingsString(Context context, String key, String defaultValue) {
        try {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            return settings.getString(key, defaultValue);
        }
        catch (Exception ex) {
            return defaultValue;
        }
    }
    public static Integer getSpinnerSelectedIndex(Activity context, int id) {
        Spinner spinner = (Spinner)context.findViewById(id);
        if (spinner != null) {
            return spinner.getSelectedItemPosition();
        }
        return null;
    }
    public static String getEditTextValue(Activity context, int id) {
        EditText editText = (EditText)context.findViewById(id);
        if (editText != null) {
            return editText.getText().toString();
        }
        return null;
    }

    public static String getEditTextValue(View view, int id) {
        EditText editText = (EditText)view.findViewById(id);
        if (editText != null) {
            return editText.getText().toString();
        }
        return null;
    }

    public static void setSettingsString(Context context, String value, String key) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }
    public static void setSettingsInteger(Context context, Integer value, String key){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.commit();
    }
    public static Integer getSettingsInteger(Context context, String key, Integer defaultValue) {
        try {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            return settings.getInt(key, defaultValue);
        }
        catch (Exception ex) {
            return defaultValue;
        }
    }
    public static void setSettingsLong(Context context, long value, String key){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(key, value);
        editor.commit();
    }
    public static boolean stringIsNullOrEmpty(String str) {
        return str == null || str.length() == 0;
    }
    public static boolean checkNetworkConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }
    public static String stringFromDate(Date date, String format) {
        return Utility.stringFromDate(date, format, false);
    }
    public static String stringFromDate(Date date, String format, boolean gtm) {
        if (date != null) {
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            if (gtm) {
                formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
            }
            return formatter.format(date);
        }
        return null;
    }
    public static String getDeviceAndApplicationInfo(Context context, String appName) {
        return String.format("%s v%s [Android: %s, SDK: %s, SDK Version: %s]", appName, Utility.getVersionName(context), Build.VERSION.RELEASE, Build.VERSION.SDK, Build.VERSION.SDK_INT);
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
    public static String getEditTextText(Activity activity, int id) {
        EditText editText = (EditText)activity.findViewById(id);
        if (editText != null) {
            return editText.getEditableText().toString();
        }
        return null;
    }
    public static boolean isEmailAddress(String address) {
        if (Utility.stringIsNullOrEmpty(address)) {
            return false;
        } else {
            Pattern p = Pattern.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}\\@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+");
            return p.matcher(address).matches();
        }
    }
    public static String padLeft(String str, char item, int length) {
        if (str.length() >= length) {
            return str;
        }
        else {
            while (str.length() < length) {
                str = String.format("%s%s", item, str);
            }
            return str;
        }
    }

    public static String getDateString(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return padLeft(calendar.get(Calendar.DAY_OF_MONTH)+"", '0', 2) + "." + padLeft((calendar.get(Calendar.MONTH) + 1) +"", '0', 2) + "." + Utility.padLeft(calendar.get(Calendar.YEAR)+"", '0', 4);

    }

    public static String getUpperDateString(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        SimpleDateFormat format = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
        return format.format(calendar.getTime());

    }

    public static void setTextViewText(View view, int id, String text) {
        TextView textView = (TextView)view.findViewById(id);
        if (textView != null) {
            textView.setText(text);
        }
    }
    public static void setEditTextText(View view, int id, String text) {
        EditText editText = (EditText)view.findViewById(id);
        if (editText != null) {
            editText.setText(text);
        }
    }

    public static void setEditTextText(Activity activity, int id, String text) {
        EditText editText = (EditText)activity.findViewById(id);
        if (editText != null) {
            editText.setText(text);
        }
    }

    public static void setSpinnerSelection(View view, int id, int position) {
        Spinner spinner = (Spinner)view.findViewById(id);
        if (spinner != null) {
            spinner.setSelection(position);
        }
    }

    public static void setSpinnerSelection(Activity activity, int id, int position) {
        Spinner spinner = (Spinner)activity.findViewById(id);
        if (spinner != null) {
            spinner.setSelection(position);
        }
    }

    public static void hideKeyboard(Activity activity){
        InputMethodManager inputManager =
                (InputMethodManager) activity.
                        getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static String encodeTobase64(Bitmap image)
    {
        Bitmap immagex=image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        Log.e("LOOK", imageEncoded);
        return imageEncoded;
    }

    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    public static Bitmap getScaledBitmap(String path){
        /*

        if((bitmap.getHeight() > Config.MAX_IMG_SIZE) || (bitmap.getWidth() > Config.MAX_IMG_SIZE) ) {
            Bitmap newBitmap;


            double ratio;

            if (bitmap.getHeight() > bitmap.getWidth()) {
                ratio = (double) Config.MAX_IMG_SIZE / (double) bitmap.getHeight();
            } else if (bitmap.getHeight() < bitmap.getWidth()) {
                ratio = (double) Config.MAX_IMG_SIZE / (double) bitmap.getWidth();
            } else {        //if is rectangle
                ratio = (double) Config.MAX_IMG_SIZE / (double) bitmap.getWidth();
            }


            int newWidth = (int) (bitmap.getWidth() * ratio);
            int newHeight = (int) (bitmap.getHeight() * ratio);

            Log.d(tag, "must changed bitmap res from  " + bitmap.getWidth() + " x " + bitmap.getHeight() + " to " + newWidth + " " + newHeight);

            newBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);

            bitmap = newBitmap;

            */

        int originalSize = 0;
        int scaledSize = 0;

        Bitmap bitmapImage = BitmapFactory.decodeFile(path);
        int nh = (int) ( bitmapImage.getHeight() * (1920.0 / bitmapImage.getWidth()) );
        Bitmap scaled = Bitmap.createScaledBitmap(bitmapImage, 1920, nh, true);


        Log.d(tag, "getScaledImage(), original to scaled size : " + originalSize + " -> " + scaledSize);
        return scaled;

    }

    // convert from bitmap to byte array
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


}
