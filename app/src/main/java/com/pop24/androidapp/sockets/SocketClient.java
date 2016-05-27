package com.pop24.androidapp.sockets;

import android.util.Log;

import com.pop24.androidapp.Connectable;
import com.pop24.androidapp.MainActivity;
import com.pop24.androidapp.MyApp;
import com.pop24.androidapp.RouteSubject;
import com.pop24.androidapp.TvPermissionsSubject;
import com.pop24.androidapp.Utility;
import com.pop24.androidapp.config.Constants;
import com.pop24.androidapp.config.DrivingStates;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Tomas on 19. 11. 2015.
 */


public class SocketClient extends WebSocketClient implements Observer {
    private String tag = "SocketClient";
    private MainActivity activity;
    private RouteSubject routeSubject;

    private String FROM_TV = "";

    private String ACT_CMD = "";
    private String STREAM_CMD = "";
    private final String REQUEST_USER_STREAM = "REQUEST_USER_STREAM";
    private final String CLIENT_PUSH_STREAM = "CLIENT_PUSH_STREAM";
    private final String CLIENT_CHANGE_PERMISSIONS = "CLIENT_CHANGE_PERMISSIONS";
    public static final String STREAM_WAS_STARTED = "STREAM_WAS_STARTED";

    private final String STRING_FREE_DRIVING = "Voľná";
    private final String STRING_RECORD_ROUTE = "Záznam";

    final String host = "147.175.145.110";
    final int portNumber = 9000;
    WebSocketClient webSocketClient;


    public SocketClient(URI serverURI, MainActivity activity, RouteSubject routeSubject) {
        super(serverURI);
        this.activity = activity;
        this.routeSubject = routeSubject;
    }

    @Override
    public void update(Observable observable, Object o) {       //depends on command must send value in realtime via socket

        if(observable instanceof RouteSubject) {
            Log.d(tag, "update() called for RouteSubject");
            if(observable == routeSubject) {
                if(STREAM_CMD.equals(REQUEST_USER_STREAM) && getConnection().isOpen() )
                    sendMessage(routeSubject);
            }

        }else if(observable instanceof TvPermissionsSubject) {
            Log.d(tag, "update() called for TvPermissionsSubject");

            sendMessage((TvPermissionsSubject)observable);

        }

    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.d(tag, "Opened socket");
        HashMap jsonPairs = new HashMap();
        jsonPairs.put("type", "SLAVE");
        jsonPairs.put("command", "SET_MY_CREDENTIALS");
        jsonPairs.put("id_cmp", "1");
        jsonPairs.put("name", Utility.getSettingsString(MyApp.content, Constants.NICK_NAME, ""));
        jsonPairs.put("email", Utility.getSettingsString(MyApp.content, Constants.EMAIL, ""));
        this.send(getJSONString(jsonPairs));

        activity.setIconIsConnected(true);
    }


    @Override
    public void onMessage(String s) {
        final String message = s;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(tag, "onMessage : " + message);

                try {
                    JSONObject jsonObj = new JSONObject(message);
                    ACT_CMD = jsonObj.getString("command");
                    if (ACT_CMD.equals("REQUEST_USER_STREAM")) {
                        STREAM_CMD = ACT_CMD;
                        activity.setTvWatching(true);
                        FROM_TV = jsonObj.getString("from");
                        //start stream user data
                        //must notify MainActivity (Controller), about RouteSubject must observe this SocketClient about every change
                        //after will start getting update(Observable observable, Object o) and sending it via created socket


                    } else if (ACT_CMD.equals("STOP_USER_STREAM")) {
                        STREAM_CMD = ACT_CMD;
                        activity.setTvWatching(false);
                        FROM_TV = jsonObj.getString("from");

                    } else if (ACT_CMD.equals("START_VIDEO_STREAM")) {
                        activity.setTvStreamWatching(true);
                        FROM_TV = jsonObj.getString("from");

                    } else if (ACT_CMD.equals("STOP_VIDEO_STREAM")) {    //physical data will be still sending
                        activity.setTvStreamWatching(false);
                        FROM_TV = jsonObj.getString("from");

                    } else if (ACT_CMD.equals("STOP_STREAM")) {
                        ACT_CMD = null;

                        //stop stream user data
                    } else if (ACT_CMD.equals("SWITCH_CAMERA")) {
                        activity.mSession.switchCamera();

                    } else if (ACT_CMD.equals("START_RECORDING")) {
                        activity.fab.performClick();

                    } else if (ACT_CMD.equals("STOP_RECORDING")) {
                        activity.fab.performClick();

                    } else if (ACT_CMD.equals("SEND_MESSAGE")) {
                        Utility.showAlertDialog(MyApp.content, jsonObj.getString("title"), jsonObj.getString("message"), "OK");

                    } else if (ACT_CMD.equals("PUSH_MASTER")) {
                        Log.d(tag, "PUSH_MASTER");
                        if (jsonObj.getJSONObject("master") != null)
                            ((Connectable) activity).setTvConnected(true, jsonObj.getJSONObject("master").getString("name"));
                        else
                            ((Connectable) activity).setTvConnected(false, "Nepripojený");


                    }

                } catch (JSONException e) {

                }

            }
        });
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.d(tag, "Closed socket " + reason);
        activity.setIconIsConnected(false);
    }

    @Override
    public void onError(Exception ex) {
        Log.d(tag, "Socket error " + ex.getMessage());
        activity.setIconIsConnected(false);
    }


    private void sendMessage(String msg){
        if(this != null){
            this.send(msg);
        }
    }

    private void sendMessage(RouteSubject routeSubject){
        HashMap jsonPairs = new HashMap();
        jsonPairs.put("type", "SLAVE");
        jsonPairs.put("id_cmp", Utility.getSettingsInteger(MyApp.getContext(), Constants.ID_FLEET, 0));
        jsonPairs.put("command", CLIENT_PUSH_STREAM);
        jsonPairs.put("to_tv", FROM_TV);
        jsonPairs.put("from_client", Utility.getSettingsString(MyApp.getContext(), Constants.EMAIL, null));
        jsonPairs.put("DRIVING_STATE", routeSubject.getDrivingState() == 0 ? STRING_FREE_DRIVING : STRING_RECORD_ROUTE);
        jsonPairs.put("CAMS_COUNT", routeSubject.getCamsCount());
        jsonPairs.put("REMOTE_ACCESS", Utility.getSettingsBoolean(MyApp.getContext(), Constants.REMOTE_ACCESS, false));
        jsonPairs.put("LAT", routeSubject.getLiveData().getPointStruct().getLatLng() == null ? null : routeSubject.getLiveData().getPointStruct().getLatLng().latitude);
        jsonPairs.put("LON", routeSubject.getLiveData().getPointStruct().getLatLng() == null ? null : routeSubject.getLiveData().getPointStruct().getLatLng().longitude);
        jsonPairs.put("SPEED", routeSubject.getLiveData().getPointStruct().getSpeed() == null ? null : routeSubject.getLiveData().getPointStruct().getSpeed());
        jsonPairs.put("HR", routeSubject.getLiveData().getPointStruct().getHr() == null ? null : routeSubject.getLiveData().getPointStruct().getHr());
        jsonPairs.put("HR_ZONE", routeSubject.getLiveData().getHrZone());
        jsonPairs.put("WHEN", routeSubject.getLiveData().getPointStruct().get_when());
        if(routeSubject.getDrivingState() == DrivingStates.DRIVING_STATE_RECORDING) {
            jsonPairs.put("DURATION", routeSubject.getLiveData().getDuration()/2);
            jsonPairs.put("DISTANCE", routeSubject.getLiveData().getDistance());
            jsonPairs.put("AVG_SPEED", routeSubject.getLiveData().getAvgSpeed());
            jsonPairs.put("MAX_SPEED", routeSubject.getLiveData().getMaxSpeed());
            jsonPairs.put("BURNED_CALORIES", routeSubject.getLiveData().getBurnedCalories());
            jsonPairs.put("ENERGY_EXPERDITURE", routeSubject.getLiveData().getPointStruct().getEe());
        }
        sendFormattedJsonMsg(jsonPairs);
    }

    private void sendMessage(TvPermissionsSubject permissionsSubject) {
        HashMap jsonPairs = new HashMap();
        jsonPairs.put("type", "SLAVE");
        jsonPairs.put("id_cmp",  Utility.getSettingsInteger(MyApp.getContext(), Constants.ID_FLEET, 0));
        jsonPairs.put("command", CLIENT_CHANGE_PERMISSIONS);
        jsonPairs.put("allow_recording", ""+permissionsSubject.getPermissions()[0]);
        jsonPairs.put("allow_switch_cam", ""+permissionsSubject.getPermissions()[1]);
        jsonPairs.put("allow_send_msg", ""+permissionsSubject.getPermissions()[2]);
        jsonPairs.put("to_tv", FROM_TV);
        jsonPairs.put("from_client", Utility.getSettingsString(MyApp.getContext(), Constants.EMAIL, null));

        sendFormattedJsonMsg(jsonPairs);
    }

    public void sendSignal(String command) {
        HashMap jsonPairs = new HashMap();
        jsonPairs.put("type", "SLAVE");
        jsonPairs.put("id_cmp",  Utility.getSettingsInteger(MyApp.getContext(), Constants.ID_FLEET, 0));
        jsonPairs.put("command", command);
        jsonPairs.put("to_tv", FROM_TV);
        jsonPairs.put("from_client", Utility.getSettingsString(MyApp.getContext(), Constants.EMAIL, null));

        sendFormattedJsonMsg(jsonPairs);
    }

    private void sendFormattedJsonMsg(HashMap jsonPairs){
        Log.d(tag, "sendingFormattedJsonMsg() : " + getJSONString(jsonPairs));
        if(this.getConnection() != null && this.getConnection().isOpen()) {
            this.send(getJSONString(jsonPairs));
        }else{
            Log.d(tag, "sendFormattedJsonMsg FAILED ");
        }
    }


    private void closeSocket(){
        if(this.getConnection() != null){
            this.getConnection().close();
        }
    }

    private String getJSONString(HashMap values){
        JSONObject jsonObj = new JSONObject(values);
        return jsonObj.toString();
    }


}
