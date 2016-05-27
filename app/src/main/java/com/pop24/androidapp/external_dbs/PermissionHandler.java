package com.pop24.androidapp.external_dbs;

import android.util.Log;

import com.pop24.androidapp.MyApp;
import com.pop24.androidapp.TvPermissionsSubject;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Tomas on 9. 4. 2016.
 */
public class PermissionHandler implements Observer {
    private String tag = "PermissionHandler";

    @Override
    public void update(Observable observable, Object o) {
        if(observable instanceof TvPermissionsSubject) {
            Integer[] permissions = ((TvPermissionsSubject)observable).getPermissions();
            Log.d(tag, "update called from PermissionHandler " + permissions[0]+ " , " + permissions[1] + " , " + permissions[2]);

            ExternalDbsHelper externalDbsHelper = new ExternalDbsHelper.Builder().setCMD(ExternalDbsHelper.CMD_UPDATE_PERMISSIONS)
                                                                    .setPermissions(permissions)
                                                                    .setContent(MyApp.content).build();

            externalDbsHelper.execute();
        }

    }
}
