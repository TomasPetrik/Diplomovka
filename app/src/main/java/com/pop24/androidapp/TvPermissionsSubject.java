package com.pop24.androidapp;

import android.content.Context;

import com.pop24.androidapp.config.Constants;

import java.util.Observable;

/**
 * Created by Tomas on 8. 4. 2016.
 */
public class TvPermissionsSubject extends Observable {
    Integer permissions[] = new Integer[]{0, 0, 0};
    private Context context;

    public TvPermissionsSubject(Context context){
        this.context = context;
        permissions[0] = Utility.getSettingsInteger(context, Constants.SETTINGS_KEY_USER_PERMISSION+"0", 0);
        permissions[1] = Utility.getSettingsInteger(context, Constants.SETTINGS_KEY_USER_PERMISSION + "1", 0);
        permissions[2] = Utility.getSettingsInteger(context, Constants.SETTINGS_KEY_USER_PERMISSION+"2", 0);
    }

    public void updatePermission(int  permissionIndex, int allow){
        this.permissions[permissionIndex] = allow;
        Utility.setSettingsInteger(context , allow, Constants.SETTINGS_KEY_USER_PERMISSION+permissionIndex);

        setChanged();
        notifyObservers();
    }

    public void updateUserState(){
        setChanged();
        notifyObservers();
    }

    public Integer[] getPermissions(){
        return permissions;
    }

}
