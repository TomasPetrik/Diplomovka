package com.pop24.androidapp;

import android.app.Application;
import android.content.Context;

import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc;

/**
 * Created by Tomas on 23. 11. 2015.
 */
public class MyApp extends Application {
    private static MyApp instance;

    public static MainActivity content;

    public static MyApp getInstance() {
        return instance;
    }

    public static AntPlusHeartRatePcc hrPcc = null;

    public static Context getContext(){
        return instance;
        // or return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();


    }
}