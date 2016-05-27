package com.pop24.androidapp;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.pop24.androidapp.config.Constants;
import com.pop24.androidapp.config.DrivingStates;
import com.pop24.androidapp.external_dbs.ExternalDbsHelper;
import com.pop24.androidapp.heartrate.HrZoneAndKcalMonitor;
import com.pop24.androidapp.internal_dbs.PointStruct;
import com.pop24.androidapp.internal_dbs.RouteLiveData;
import com.pop24.androidapp.internal_dbs.SQLiteDbsHelper;

import java.util.Date;
import java.util.LinkedList;
import java.util.Observable;

/**
 * Created by Tomas on 19. 11. 2015.
 */
public class RouteSubject extends Observable{
    private String tag = "RouteSubject";
    private int DRIVING_STATE = DrivingStates.DRIVING_STATE_FREE;
    private int CAMS_COUNT = Camera.getNumberOfCameras();

    private SQLiteDbsHelper sqLiteDbsHelper;

    LinkedList<RouteLiveData> fifo; //stack

    Integer idRoute = null;

    Float recentlyLenght = null;
    PointStruct pointStruct = null;
    RouteLiveData routeLiveData = null;
    HrZoneAndKcalMonitor hrZoneKcalMonitor = null;
    Integer pointsCount = 0;

    Float avgSpeed = null;
    Float maxSpeed = null;
    Integer avgHr = null;
    Integer maxHr = null;
    Float maxEe = 0f;
    Float distance = null;
    Float burnedCalories = null;
    Integer hrZone = null;
    Integer elevation = null;

    Long firstPointTimestamp = 0L;
    Long duration = null;
    long when;



    public RouteSubject(){
        routeLiveData = new RouteLiveData();
        hrZoneKcalMonitor = new HrZoneAndKcalMonitor();
        sqLiteDbsHelper = new SQLiteDbsHelper(MyApp.getContext(), this);
        fifo = new LinkedList<>();
    }

    private void resetData(){
        idRoute = null;
        recentlyLenght = 0f;
        pointsCount = 0;
        avgSpeed = 0f;
        maxSpeed = 0f;
        avgHr = 0;
        maxHr = 0;
        maxEe = 0f;
        distance = 0f;
        burnedCalories = 0f;
        elevation = 0;
        when = 0;

        firstPointTimestamp = 0L;
        duration = 0L;

    }

    private void insertIntoFifo(RouteLiveData routeLiveData){
        fifo.addFirst(routeLiveData);
        if(fifo.size() > 30){
            fifo.removeLast();
        }

        Log.d(tag, "fifo size : " + fifo.size());
    }

    public int getDrivingState(){
        return this.DRIVING_STATE;
    }

    public int getCamsCount(){
        return this.CAMS_COUNT;
    }

    public Boolean isRec(){
        return DRIVING_STATE == DrivingStates.DRIVING_STATE_RECORDING ? true : false;
    }

    public String startRec(){
        //set state recording and init values
        DRIVING_STATE = DrivingStates.DRIVING_STATE_RECORDING;
        resetData();
        routeLiveData = new RouteLiveData("", null,  avgSpeed,  maxSpeed,  avgHr,  maxHr, maxEe, elevation,  distance,  duration,  burnedCalories,  hrZone,  pointStruct, null, null);

        when = new Date().getTime();
        //create new route in dbs
        idRoute = sqLiteDbsHelper.insertRoute(routeLiveData);

        return Utility.getSettingsString(MyApp.content, Constants.EMAIL, "") +"_"+ (when/1000)+".mp4";
    }

    public String stopRec(String name){
        //set state free
        DRIVING_STATE = DrivingStates.DRIVING_STATE_FREE;

        Log.d(tag, "when = " + when +  ", maxEe : " + maxEe);

        String videoName = Utility.getSettingsString(MyApp.content, Constants.EMAIL, "") +"_"+ (when/1000)+".mp4";
        routeLiveData = new RouteLiveData(name, idRoute, avgSpeed, maxSpeed, avgHr, maxHr, maxEe, elevation, distance, duration, burnedCalories, hrZone, pointStruct,
                videoName, when);
        //update route recorded data in dbs
        sqLiteDbsHelper.updateRoute(routeLiveData);

        //send automatically route in external dbs
        ExternalDbsHelper externalDbsHelper = new ExternalDbsHelper.Builder().setContent(MyApp.content)
                .setCMD(ExternalDbsHelper.CMD_TRY_SEND_ROUTE)
                .setIdRoute(idRoute)
                .setCallback(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message message) {
                        switch (message.what) {
                            case ExternalDbsHelper.RESULT_STATE_OK :
                                Log.d(tag, "RESULT_STATE_OK");
                                break;

                            case ExternalDbsHelper.RESULT_STATE_FAILED :
                                Log.d(tag, "RESULT_STATE_FAILED");
                                Utility.showToast(MyApp.getContext(), R.string.String0022);
                                break;
                        }
                        resetData();            //null recorded data
                        updateLiveData(pointStruct);   //updateLiveData
                        return false;
                    }
                }).build();
        externalDbsHelper.execute();


        return videoName;

    }


    public void addPointStruct(PointStruct pointStruct){

        switch(DRIVING_STATE){
            case DrivingStates.DRIVING_STATE_FREE :
                updateLiveData(pointStruct);
                break;

            case DrivingStates.DRIVING_STATE_RECORDING :
                if(pointsCount == 1)
                    firstPointTimestamp = pointStruct.get_when();
                else {
                    if(this.pointStruct != null && this.pointStruct.getLatLng() != null && pointStruct.getLatLng() != null)
                    recentlyLenght = Utility.getDistanceBetween(this.pointStruct.getLatLng(), pointStruct.getLatLng());
                }

                pointsCount+=1;
                recalculateRecordingData(pointStruct);

                Log.d(tag, "before insert point into route with id = " + idRoute);
                sqLiteDbsHelper.insertPoint(this.routeLiveData);    //after recalculate insert routeLiveData

                break;
        }

        this.pointStruct = pointStruct;

        setChanged();
        notifyObservers();

    }


    private void recalculateRecordingData(PointStruct pointStruct){
        if(pointStruct.getSpeed() > maxSpeed)
            maxSpeed = pointStruct.getSpeed();

        duration = Long.valueOf(pointsCount/2);

        if(avgSpeed == 0.0)
            avgSpeed = pointStruct.getSpeed();
        else
            avgSpeed = avgSpeed + ((pointStruct.getSpeed()-avgSpeed)/pointsCount);

        distance += recentlyLenght;


        if(pointStruct.getHr() != null){
            Float kcalAmount = hrZoneKcalMonitor.getKcalPer500ms(pointStruct.getHr());
            if(kcalAmount > 0)
                burnedCalories += kcalAmount;

            if(pointStruct.getHr() > maxHr)
                maxHr = pointStruct.getHr();

            if(avgHr == 0)
                avgHr = pointStruct.getHr();
            else
                avgHr = avgHr + ((pointStruct.getHr()-avgHr)/pointsCount);
        }

        updateLiveData(pointStruct);
    }

    private void updateLiveData(PointStruct pointStruct) {
        this.routeLiveData.setIdRoute(this.idRoute);
        this.routeLiveData.setAvgSpeed(avgSpeed);
        this.routeLiveData.setMaxSpeed(maxSpeed);
        this.routeLiveData.setAvgHr(avgHr);
        this.routeLiveData.setMaxHr(maxHr);
        this.routeLiveData.setMaxEe(maxEe);
        this.routeLiveData.setDistance(distance);
        this.routeLiveData.setDuration(duration);
        hrZone = hrZoneKcalMonitor.getHrZone(pointStruct);
        this.routeLiveData.setHrZone(hrZone);
        this.routeLiveData.setBurnedCalories(burnedCalories);
        if(pointStruct.getHr() != null) {
            Float actEe = hrZoneKcalMonitor.getEnergyExperditure(pointStruct.getHr());
            if(actEe >= 0)
                pointStruct.setEe(actEe);
            else
                pointStruct.setEe(0f);

            if(pointStruct.getEe() > maxEe) {
                maxEe = pointStruct.getEe();
            }
        }
        this.routeLiveData.setPointStruct(pointStruct);

        insertIntoFifo(this.routeLiveData);
    }

    public RouteLiveData getLiveData(){
        return this.routeLiveData;
    }


}
