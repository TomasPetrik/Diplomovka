package com.pop24.androidapp.heartrate;

import com.pop24.androidapp.MyApp;
import com.pop24.androidapp.Utility;
import com.pop24.androidapp.config.Constants;
import com.pop24.androidapp.internal_dbs.PointStruct;

/**
 * Created by Tomas on 23. 11. 2015.
 */
public class HrZoneAndKcalMonitor {
    private String tag = "HrZoneMonitor";


    private boolean IS_MAN = Utility.getSettingsBoolean(MyApp.getContext(), Constants.PREF_GENDER, true);
    private int WEIGHT = Utility.getSettingsInteger(MyApp.getContext(), Constants.PREF_WEIGHT, 80);
    private int RHR = Utility.getSettingsInteger(MyApp.getContext(), Constants.PREF_HR_REST, 60);
    private int AGE = Utility.getSettingsInteger(MyApp.getContext(), Constants.PREF_AGE, 25);
    private float MHR = 208f - (0.7f * AGE);

    private final float HR_MINIMUM = 40f;
    private final float[] HR_ZONES = new float[]{0.5f*(MHR-RHR)+RHR, 0.6f*(MHR-RHR)+RHR, 0.7f*(MHR-RHR)+RHR, 0.8f*(MHR-RHR)+RHR, 0.9f*(MHR-RHR)+RHR, 1.0f*(MHR-RHR)+RHR};


    public Integer getHrZone(PointStruct pointStruct){
        if(pointStruct.getHr() == null) return 0;

        //Log.d(tag, "AGE: " + AGE + " , MHR : " + MHR + " , RHR : " + RHR + " , actHR : " + pointStruct.getHr() + " , HR_ZONE_1 : " + HR_ZONES[0]);

        if(HR_MINIMUM <= pointStruct.getHr() && pointStruct.getHr() <= HR_ZONES[0]){
            return 0;
        }else if(HR_ZONES[0] <= pointStruct.getHr() && pointStruct.getHr() <= HR_ZONES[1]){
            return 1;
        }else if(HR_ZONES[1] <= pointStruct.getHr() && pointStruct.getHr() <= HR_ZONES[2]){
            return 2;
        }else if(HR_ZONES[2] <= pointStruct.getHr() && pointStruct.getHr() <= HR_ZONES[3]){
            return 3;
        }else if(HR_ZONES[3] <= pointStruct.getHr() && pointStruct.getHr() <= HR_ZONES[4]){
            return 4;
        }else if(HR_ZONES[4] <= pointStruct.getHr() && pointStruct.getHr() <= HR_ZONES[5]){
            return 5;
        }
        return 0;
    }


    public Integer getHrZone(int hr){
        if(hr == 0) return 0;

        //Log.d(tag, "AGE: " + AGE + " , MHR : " + MHR + " , RHR : " + RHR + " , actHR : " + hr + " , HR_ZONE_1 : " + HR_ZONES[0]);

        if(HR_MINIMUM <= hr && hr <= HR_ZONES[0]){
            return 0;
        }else if(HR_ZONES[0] <= hr && hr <= HR_ZONES[1]){
            return 1;
        }else if(HR_ZONES[1] <= hr && hr <= HR_ZONES[2]){
            return 2;
        }else if(HR_ZONES[2] <= hr && hr <= HR_ZONES[3]){
            return 3;
        }else if(HR_ZONES[3] <= hr && hr <= HR_ZONES[4]){
            return 4;
        }else if(HR_ZONES[4] <= hr && hr <= HR_ZONES[5]){
            return 5;
        }
        return 0;
    }


    public Float getEnergyExperditure(Integer HR){
        if(HR == null)
            return null;

        if(IS_MAN)
            return (((-55.0969f + (0.6309f * HR) + (0.1988f * WEIGHT) + (0.2017f * AGE)) / 4.184f) * 60);
        else
            return (((-20.4022f + (0.4472f * HR) + (0.1263f * WEIGHT) + (0.074f * AGE)) / 4.184f) * 60);
    }

    public Float getKcalPer500ms(Integer HR){
        if(HR == null)
            return null;

        if(IS_MAN)
            return (((-55.0969f + (0.6309f * HR) + (0.1988f * WEIGHT) + (0.2017f * AGE)) / 4.184f) / 60 / 2);   // /60 = per sec AND /2 = per half sec
        else
            return (((-20.4022f + (0.4472f * HR) + (0.1263f * WEIGHT) + (0.074f * AGE)) / 4.184f) / 60 / 2);    // /60 = per sec AND /2 = per half sec
    }
}
