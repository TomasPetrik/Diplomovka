package com.pop24.androidapp;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.pop24.androidapp.internal_dbs.RouteLiveData;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Tomas on 19. 11. 2015.
 */
public class MainView implements Observer {
    private String tag = "MainView";
    private View globalView;
    private RouteSubject routeSubject;


    public MainView(View globalView, RouteSubject routeSubject){
        this.globalView = globalView;
        this.routeSubject = routeSubject;
    }

    @Override
    public void update(Observable observable, Object o) {
        Log.d(tag, "update() called");
        if(routeSubject == observable)
            updateView(routeSubject.getLiveData());
    }

    private void updateView(RouteLiveData routeLiveData) {
        Log.d(tag, "updateView() called");
        if(globalView.findViewById(R.id.txt1) == null)
            return;

       //((TextView) globalView.findViewById(R.id.txt1)).setText(routeLiveData.getDuration() == null ? "N/A" :  Utility.getTimeString(routeLiveData.getDuration()));
        ((TextView) globalView.findViewById(R.id.txt2)).setText(routeLiveData.getPointStruct().getSpeed() == null ? "N/A" : Utility.getSpeedInKmph(routeLiveData.getPointStruct().getSpeed()));
        ((TextView) globalView.findViewById(R.id.txt3)).setText(routeLiveData.getDistance() == null ? "N/A" : (Utility.round(routeLiveData.getDistance()/1000, 1) + ""));   // /1000 => in km
        ((TextView) globalView.findViewById(R.id.txt4)).setText(routeLiveData.getPointStruct().getHr() == null ? "N/A" : (routeLiveData.getPointStruct().getHr() + ""));
    }

    public void setHR(int hrVal){
        ((TextView) globalView.findViewById(R.id.txt4)).setText(hrVal + "");
    }

    public void setTimer(Long secondsCount){
        ((TextView) globalView.findViewById(R.id.txt1)).setText(secondsCount == null ? "N/A" : Utility.getTimeString(secondsCount));
    }


}
