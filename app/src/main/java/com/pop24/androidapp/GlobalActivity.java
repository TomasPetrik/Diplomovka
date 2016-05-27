package com.pop24.androidapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pcc.defines.EventFlag;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc;
import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle;

import java.math.BigDecimal;
import java.util.EnumSet;

/**
 * Created by Tomas on 28. 11. 2015.
 */
public abstract class GlobalActivity extends AppCompatActivity {
    private String tag = "GlobalActivity";

    protected AntPlusHeartRatePcc hrPcc = null;
    protected PccReleaseHandle<AntPlusHeartRatePcc> releaseHandle = null;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        handleReset();
    }

    protected abstract void requestAccessToPcc();
    protected abstract void initScanDisplay();

    /**
     * Resets the PCC connection to request access again and clears any existing display data.
     */

    protected void handleReset()
    {
        //Release the old access if it exists
        if(releaseHandle != null)
        {
            releaseHandle.close();
        }

        requestAccessToPcc();
    }

    //Receives state changes and shows it on the status display line
    protected AntPluginPcc.IDeviceStateChangeReceiver base_IDeviceStateChangeReceiver =
            new AntPluginPcc.IDeviceStateChangeReceiver()
            {
                @Override
                public void onDeviceStateChange(final DeviceState newDeviceState)
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(tag, "HR Device  " + hrPcc.getDeviceName() + ", changes tate to : " + newDeviceState);
                        }
                    });


                }
            };


    protected AntPluginPcc.IPluginAccessResultReceiver<AntPlusHeartRatePcc> base_IPluginAccessResultReceiver =
            new AntPluginPcc.IPluginAccessResultReceiver<AntPlusHeartRatePcc>() {
                //Handle the result, connecting to events on success or reporting failure to user.
                @Override
                public void onResultReceived(AntPlusHeartRatePcc result, RequestAccessResult resultCode,
                                             DeviceState initialDeviceState) {
                    Log.d(tag, "HR connecting to belt..");

                    setContentView(R.layout.activity_main);

                    switch(resultCode) {
                        case SUCCESS:
                            hrPcc = result;

                            //subscribeToHrEvents();

                            break;
                    }
                }

            };

    public void subscribeToHrEvents() {
        hrPcc.subscribeHeartRateDataEvent(new AntPlusHeartRatePcc.IHeartRateDataReceiver() {
            @Override
            public void onNewHeartRateData(final long estTimestamp, EnumSet<EventFlag> eventFlags,
                                           final int computedHeartRate, final long heartBeatCount,
                                           final BigDecimal heartBeatEventTime, final AntPlusHeartRatePcc.DataState dataState) {
                // Mark heart rate with asterisk if zero detected
                final String textHeartRate = String.valueOf(computedHeartRate)
                        + ((AntPlusHeartRatePcc.DataState.ZERO_DETECTED.equals(dataState)) ? "*" : "");

                // Mark heart beat count and heart beat event time with asterisk if initial value
                final String textHeartBeatCount = String.valueOf(heartBeatCount)
                        + ((AntPlusHeartRatePcc.DataState.INITIAL_VALUE.equals(dataState)) ? "*" : "");
                final String textHeartBeatEventTime = String.valueOf(heartBeatEventTime)
                        + ((AntPlusHeartRatePcc.DataState.INITIAL_VALUE.equals(dataState)) ? "*" : "");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(tag, "HR onNewHeartRateData() called, [HR, HR_beat_count, HR_beat_event_time] : " + textHeartRate + " , " + textHeartBeatCount + " , " + textHeartBeatEventTime);

                    }
                });
            }
        });

    }

    @Override
    protected void onDestroy()
    {
        if(releaseHandle != null)
        {
            releaseHandle.close();
        }
        super.onDestroy();
    }




}
