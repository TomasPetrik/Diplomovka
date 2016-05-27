package com.pop24.androidapp.internal_dbs;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.pop24.androidapp.RouteSubject;
import com.pop24.androidapp.helpers.Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class SQLiteDbsHelper extends SQLiteOpenHelper implements Observer{
    private String tag = "LocalDbs";
    public static final String DATABASE_NAME = "LocalDbs.db";
    private static final int DATABASE_VERSION = 1;
    private RouteSubject routeSubject;

    public SQLiteDbsHelper(Context context) {   //for ExternalDbsHelper
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public SQLiteDbsHelper(Context context, RouteSubject routeSubject) {    //for MainActivity during create observer pattern to be observed from routeSubject
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.routeSubject = routeSubject;
    }

    @Override
    public void update(Observable observable, Object o) {   //update dbs by add new point
        Log.d(tag, "update() called");
        if(routeSubject == observable)
            insertPoint(routeSubject.getLiveData());
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        for(String sql : DatabaseDump.getDatabaseDump())
            database.execSQL(sql);

        Log.d(tag, "dbsCreated() ");
    }

    @Override
    public void onOpen(SQLiteDatabase database){
        super.onOpen(database);
        Log.d(tag, "onOpen() called");

        if(!database.isReadOnly()){
            database.execSQL(DatabaseDump.dbsDumpSetForeignKeysSupport);
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(tag, "onUpdate() called");
        if(newVersion > oldVersion){
            switch (newVersion) {

                default: {
                    onCreate(db);
                }
            }
        }
    }

    public Boolean insertPoint(RouteLiveData routeLiveData){
        Log.d(tag, "insertPoint() called");
        SQLiteDatabase database = null;
        try {
            database = this.getWritableDatabase();
            ContentValues values = this.internalGetPointContentValues(routeLiveData.getIdRoute(), routeLiveData.getPointStruct());
            long count = database.insert(DatabaseDump.TABLE_POINTS, null, values);
            //return getHighestID(database, DatabaseDump.TABLE_POINTS);
             return count >= 0;
        }
        catch (Exception ex) {
            Utility.log(ex);
            return false;
        }
        finally {
            if (database != null) {
                database.close();
            }
        }
    }

    public Integer insertRoute(RouteLiveData routeLiveData){
        Log.d(tag, "insertRoute() called");
        SQLiteDatabase database = null;
        try {
            database = this.getWritableDatabase();
            ContentValues values = this.internalGetRouteContentValues(routeLiveData);
            long count = database.insert(DatabaseDump.TABLE_ROUTES, null, values);
            return getHighestID(database, DatabaseDump.TABLE_ROUTES);

           // return count >= 0;
        }
        catch (Exception ex) {
            Utility.log(ex);
            return -1;
        }
        finally {
            if (database != null) {
                database.close();
            }
        }
    }

    public Boolean removeRoute(Integer id) {
        SQLiteDatabase database = null;
        try {
            database = this.getWritableDatabase();
            int count = database.delete("routes", String.format("id = %s", id), null);

            Log.d(tag, "removeRoute, id : " + id + " ,count : " + count);

            return count > 0;
        }
        catch (Exception ex) {
            Utility.log(ex);
            return false;
        }
        finally {
            if (database != null) {
                database.close();
            }
        }
    }

    public Integer getHighestID(SQLiteDatabase database, String tableName) {
        final String MY_QUERY = "SELECT MAX(id) FROM " + tableName;
        Cursor cur = database.rawQuery(MY_QUERY, null);
        cur.moveToFirst();
        Integer ID = cur.getInt(0);
        cur.close();
        return ID;
    }

    public Boolean updateRoute(RouteLiveData routeLiveData) {
        Log.d(tag, "updateRoute() called");
        SQLiteDatabase database = null;
        try {
            database = this.getWritableDatabase();
            String whereClause = String.format("id = %s", routeLiveData.getIdRoute());
            ContentValues value = this.internalGetRouteContentValues(routeLiveData);
            int count = database.update(DatabaseDump.TABLE_ROUTES, value, whereClause, null);
            return count > 0;
        }
        catch (Exception ex) {
            Utility.log(ex);
            return false;
        }
        finally {
            if (database != null) {
                database.close();
            }
        }
    }

    public List<PointStruct> getPoints(long idRoute) {
        List<PointStruct> pointStructs;
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            database = this.getReadableDatabase();
            cursor = database.rawQuery(String.format("SELECT points.* FROM points JOIN routes ON routes.id = points.id_route WHERE routes.id = %s ",  idRoute), null);
            if (cursor != null) {
                pointStructs = new ArrayList<>();
                if  (cursor.moveToFirst()) {
                    do {
                        pointStructs.add(internalSelectPointStruct(cursor));
                    }
                    while (cursor.moveToNext());

                    if (pointStructs != null && pointStructs.size() > 0) {
                        return pointStructs;
                    }

                }
            }
            return null;
        }
        catch (Exception ex) {
            Utility.log(ex);
            return null;
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
            if (database != null) {
                database.close();
            }
        }
    }

    public RouteLiveData getRoute(Integer id) {
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            database = this.getReadableDatabase();
            cursor = database.rawQuery(String.format("SELECT * FROM %s WHERE id = %s", DatabaseDump.TABLE_ROUTES, id), null);
            if (cursor != null) {
                if  (cursor.moveToFirst()) {
                    do {
                        RouteLiveData routeLiveData = internalSelectRouteItem(cursor);
                        if (routeLiveData != null) {
                            return routeLiveData;
                        }
                    }
                    while (cursor.moveToNext());
                }
            }
            return null;
        }
        catch (Exception ex) {
            Utility.log(ex);
            return null;
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
            if (database != null) {
                database.close();
            }
        }
    }


    private ContentValues internalGetPointContentValues(Integer idRoute, PointStruct pointStruct) {
        ContentValues values = new ContentValues();

        if (idRoute == null) {
            //values.putNull("id_route");
        }else {
            values.put("id_route", idRoute);
        }

        if (pointStruct.getLatLng() == null) {
            values.putNull("lat");
        }else {
            values.put("lat", pointStruct.getLatLng().latitude);
        }

        if (pointStruct.getLatLng() == null) {
            values.putNull("lng");
        }else {
            values.put("lng", pointStruct.getLatLng().longitude);
        }

        if (pointStruct.getSpeed() == null) {
            values.putNull("speed");
        }else {
            values.put("speed", pointStruct.getSpeed());
        }

        if (pointStruct.getHr() == null) {
            values.putNull("hr");
        }else {
            values.put("hr", pointStruct.getHr());
        }

        if (pointStruct.getEe() == null) {
            values.putNull("ee");
        }else {
            values.put("ee", pointStruct.getEe());
        }


        if (pointStruct.get_when() == null) {
            values.putNull("_when");
        }else {
            values.put("_when", pointStruct.get_when());
        }

        return values;
    }

    private PointStruct internalSelectPointStruct(Cursor cursor) {
        PointStruct pointStruct = new PointStruct();
        pointStruct.setLatLng(new LatLng(cursor.getFloat(2), cursor.getFloat(3)));  //0 - id, 1 - id_route
        pointStruct.setSpeed(cursor.getFloat(4));
        pointStruct.setHr(cursor.getInt(5));
        pointStruct.setEe(cursor.getFloat(6));
        pointStruct.set_when(cursor.getLong(7));
        return pointStruct;
    }

    private ContentValues internalGetRouteContentValues(RouteLiveData routeLiveData) {
        ContentValues values = new ContentValues();
        if (routeLiveData.getIdRoute() == null) {
            //values.putNull("id");
        }else {
            values.put("id", routeLiveData.getIdRoute());
        }

        if (routeLiveData.getName() == null) {
            values.putNull("name");
        }else {
            values.put("name", routeLiveData.getName());
        }

        if (routeLiveData.getDistance() == null) {
            values.putNull("distance");
        }else {
            values.put("distance", routeLiveData.getDistance());
        }

        if (routeLiveData.getAvgSpeed() == null) {
            values.putNull("avg_speed");
        }else {
            values.put("avg_speed", routeLiveData.getAvgSpeed());
        }

        if (routeLiveData.getMaxSpeed() == null) {
            values.putNull("max_speed");
        }else {
            values.put("max_speed", routeLiveData.getMaxSpeed());
        }

        if (routeLiveData.getBurnedCalories() == null) {
            values.putNull("burned_kcal");
        }else {
            values.put("burned_kcal", routeLiveData.getBurnedCalories());
        }


        if (routeLiveData.getAvgHr() == null) {
            values.putNull("avg_hr");
        }else {
            values.put("avg_hr", routeLiveData.getAvgHr());
        }

        if (routeLiveData.getMaxHr() == null) {
            values.putNull("max_hr");
        }else {
            values.put("max_hr", routeLiveData.getMaxHr());
        }

        if (routeLiveData.getElevation() == null) {
            values.putNull("elevation");
        }else {
            values.put("elevation", routeLiveData.getElevation());
        }

        if (routeLiveData.getDuration() == null) {
            values.putNull("duration");
        }else {
            values.put("duration", routeLiveData.getDuration());
        }

        if (routeLiveData.getDuration() == null) {
            values.putNull("video_name");
        }else {
            values.put("video_name", routeLiveData.getVideoName());
        }

        if (routeLiveData.getWhen() == null) {
            values.putNull("_when");
        }else {
            values.put("_when", routeLiveData.getWhen());
        }

        if(routeLiveData.getMaxEe() == null){
            values.putNull("max_ee");
        }else{
            values.put("max_ee", routeLiveData.getMaxEe());
        }

        return values;
    }

    private RouteLiveData internalSelectRouteItem(Cursor cursor) {
        RouteLiveData routeStruct = new RouteLiveData();
        routeStruct.setIdRoute(cursor.getInt(0));
        routeStruct.setName(cursor.getString(1));
        routeStruct.setDistance(cursor.getFloat(2));
        routeStruct.setBurnedCalories(cursor.getFloat(3));
        routeStruct.setAvgHr(cursor.getInt(4));
        routeStruct.setMaxHr(cursor.getInt(5));
        routeStruct.setMaxEe(cursor.getFloat(6));
        routeStruct.setAvgSpeed(cursor.getFloat(7));
        routeStruct.setMaxSpeed(cursor.getFloat(8));
        routeStruct.setElevation(cursor.getInt(9));
        routeStruct.setDuration(cursor.getLong(10));
        routeStruct.setVideoName(cursor.getString(11));
        routeStruct.setWhen(cursor.getLong(12));


        return routeStruct;
    }


}
