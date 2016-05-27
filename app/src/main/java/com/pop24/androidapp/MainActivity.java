package com.pop24.androidapp;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.StrictMode;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc;
import com.dsi.ant.plugins.antplus.pcc.defines.EventFlag;
import com.google.android.gms.maps.model.LatLng;
import com.pop24.androidapp.config.Constants;
import com.pop24.androidapp.config.SocketConfig;
import com.pop24.androidapp.external_dbs.Config;
import com.pop24.androidapp.external_dbs.PermissionHandler;
import com.pop24.androidapp.grabbers.LocationDetector;
import com.pop24.androidapp.heartrate.Activity_AsyncScanHeartRateSampler;
import com.pop24.androidapp.heartrate.HrZoneAndKcalMonitor;
import com.pop24.androidapp.helpers.IUtilitySingleButtonHandler;
import com.pop24.androidapp.internal_dbs.PointStruct;
import com.pop24.androidapp.internal_dbs.SQLiteDbsHelper;
import com.pop24.androidapp.otg.UsbVideoConstructs;
import com.pop24.androidapp.server_comunication.IUtilityAsyncResponse;
import com.pop24.androidapp.server_comunication.SecureComm;
import com.pop24.androidapp.server_comunication.UtilityAsyncRequest;
import com.pop24.androidapp.sockets.SocketClient;
import com.rey.material.widget.Switch;

import net.majorkernelpanic.streaming.MediaStream;
import net.majorkernelpanic.streaming.gl.MySurfaceView;
import net.majorkernelpanic.streaming.video.VideoQuality;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import infinitegra.usb.video.IUsbVideoCallback;
import infinitegra.usb.video.IUsbVideoService;

public class MainActivity extends Activity_AsyncScanHeartRateSampler implements Connectable, Handler.Callback, net.majorkernelpanic.streaming.Session.Callback, net.majorkernelpanic.streaming.rtsp.RtspClient.Callback, SurfaceHolder.Callback{
    private String tag = "MainActivity";

    private ServiceConnection serviceConn;    //service for OTG Camera
    private static final String TOAST_STRING_PARAMETER_1 = "TOAST_PARAMETER_1";
    private static final int MESSAGE_SHOW_TOAST = 1;
    private boolean serviceStarted = false;
    private boolean isStarted = false;
    private boolean bindService = false;
    private IUsbVideoService serviceIf;
    private IUsbVideoCallback callback;
    private int width = 640;
    private int height = 480;
    private int txW = -1;
    private int txH = -1;

    private boolean running = false;
    private Handler handler = null;
    private boolean isRecording = false;
    private boolean setRecDir = false;

    private Toolbar mToolbar;
    private FragmentDrawer drawerFragment;

    private LocationDetector locationDetector;
    private Location lastLocation;

    private net.majorkernelpanic.streaming.rtsp.RtspClient mClient;
    public net.majorkernelpanic.streaming.Session mSession;
    private net.majorkernelpanic.streaming.gl.MySurfaceView mMySurfaceView;

    public FloatingActionButton fab;
    private MainView mainView;
    HrZoneAndKcalMonitor hrZoneAndKcalMonitor;
    private RouteSubject routeSubject;
    private PermissionHandler permissionHandler;
    private TvPermissionsSubject permissionsSubject;
    private SQLiteDbsHelper dbsHelper;
    private SocketClient socketClient;

    DisplayMetrics metrics;

    private LinearLayout hrZone1, hrZone2, hrZone3, hrZone4, hrZone5, hrZone6;
    private LinearLayout[] hrZoneList = new LinearLayout[6];

    private Connection dbsConnection;   //external dbs connetion

    private static final String[] INITIAL_PERMS={
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_CONTACTS
    };
    private static final int INITIAL_REQUEST=1337;
    private final int HR_PCC_REQUEST=1002;

    private Integer heartRate = null;

    private Timer timer;
    //private Chronometer stopWatch;

    static {
        System.loadLibrary("ImageProc");
    }

    public native int pixeltobmp(byte[] jp, int l, Bitmap bmp);
    public native void freeCameraMemory();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApp.content = this;
        setContentView(R.layout.activity_main);

        //new SecureComm(this).executeQuery();

        metrics = getResources().getDisplayMetrics();

        init();

        Utility.setSettingsBoolean(this, true, Constants.REMOTE_ACCESS);

        ((TextView)findViewById(R.id.fnd_txt_tv_name)).setText("[" + Utility.getSettingsString(this, Constants.FLEET_NAME, "TV") + "]");


        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerFragment = (FragmentDrawer)
                getFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);

        ((TextView)findViewById(R.id.fnd_txt_tv_name)).setText("[" + Utility.getSettingsString(this, Constants.FLEET_NAME, "TV") + "]");
        ((TextView)findViewById(R.id.tb_txt1)).setText(Utility.getSettingsString(this, Constants.NICK_NAME, "Guest"));

    }

    public void setTvConnected(boolean isConnected, String tvName){
        Log.d(tag, "setTvConnected() " + isConnected + " , " + tvName);
        findViewById(R.id.fnd_tv_connected).setSelected(isConnected);
    }

    public void setTvWatching(boolean isWatching){
        if(isWatching) {
            if(!mClient.isStreaming()) {
                toggleStream(Utility.getSettingsString(MainActivity.this, Constants.EMAIL, null));
                restartGrabbingData(false);
            }
        }else {
            if(!isRecording) {
                stopVideoStream();
                nullTimer();
            }
        }

        findViewById(R.id.tb_tv_watching).setSelected(isWatching);
    }

    public void setTvStreamWatching(boolean isWatching){
        if(isWatching) {
            if(!mClient.isStreaming()) {
                toggleStream(Utility.getSettingsString(MainActivity.this, Constants.EMAIL, null));
            }
        }else {
            if(!isRecording) {
                stopVideoStream();
            }
        }
    }

    public void setIconIsConnected(boolean isConnected){
        findViewById(R.id.tb_connected).setSelected(isConnected);
    }



    private void setMainView(){

        EditText editAge = (EditText)findViewById(R.id.fnd_edit1);
        EditText editWeight = (EditText)findViewById(R.id.fnd_edit2);

        editAge.setText(Utility.getSettingsInteger(this, Constants.PREF_AGE, 0)+"");
        editWeight.setText(Utility.getSettingsInteger(this, Constants.PREF_WEIGHT, 0)+"");

        editAge.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().matches("[0-9]{1,2}"))
                    Utility.setSettingsInteger(getApplicationContext(), Integer.valueOf(s.toString()), Constants.PREF_AGE);


            }
        });

        editWeight.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().matches("[0-9]{2,3}"))
                    Utility.setSettingsInteger(getApplicationContext(), Integer.valueOf(s.toString()), Constants.PREF_WEIGHT);
            }
        });

        Spinner spinner = (Spinner)findViewById(R.id.fnd_spinner1);
        String[] items = getResources().getStringArray(R.array.string_gender_type);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplication(), R.layout.row_spn, items);
        adapter.setDropDownViewResource(R.layout.row_spn_dropdown);
        spinner.setAdapter(adapter);
        spinner.setSelection(Utility.getSettingsBoolean(this, Constants.PREF_GENDER, true) == true ? 1 : 2);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Utility.setSettingsBoolean(MainActivity.this, i == 2 ? false : true, Constants.PREF_GENDER);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setSelected(!fab.isSelected());

                if (view.isSelected()) {
                    restartGrabbingData(true);
                    String outputVideoName = routeSubject.startRec();
                    toggleStream(Utility.getSettingsString(MainActivity.this, Constants.EMAIL, null));

                    startVideoRecording(outputVideoName, Utility.getSettingsString(MainActivity.this, Constants.EMAIL, null));
                } else {
                    nullTimer();
                    stopVideoRecording(Utility.getSettingsString(MainActivity.this, Constants.EMAIL, null));
                    stopVideoStream();

                }

                Snackbar.make(view, getString(R.string.String0014), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //findViewById(R.id.btn1).setOnClickListener(new OnAbstractOnClicksLInstener());
        //findViewById(R.id.btn2).setOnClickListener(new OnAbstractOnClicksLInstener());
        findViewById(R.id.btn3).setOnClickListener(new OnAbstractOnClicksLInstener());
        findViewById(R.id.txt4).setOnClickListener(new OnAbstractOnClicksLInstener());
        findViewById(R.id.tb_logout).setOnClickListener(new OnAbstractOnClicksLInstener());


        ((Switch)findViewById(R.id.fnd_sw1)).setChecked(permissionsSubject.getPermissions()[0] == 1 ? true : false);
        ((Switch)findViewById(R.id.fnd_sw2)).setChecked(permissionsSubject.getPermissions()[1] == 1 ? true : false);
        ((Switch)findViewById(R.id.fnd_sw3)).setChecked(permissionsSubject.getPermissions()[2] == 1 ? true : false);

        ((Switch)findViewById(R.id.fnd_sw1)).setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(Switch view, boolean checked) {
                permissionsSubject.updatePermission(0, checked == true ? 1 : 0);
            }
        });

        ((Switch)findViewById(R.id.fnd_sw2)).setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(Switch view, boolean checked) {
                permissionsSubject.updatePermission(1, checked == true ? 1 : 0);
            }
        });

        ((Switch)findViewById(R.id.fnd_sw3)).setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(Switch view, boolean checked) {
                permissionsSubject.updatePermission(2, checked == true ? 1 : 0);
            }
        });

        mMySurfaceView = (MySurfaceView) findViewById(R.id.surface);


        hrZoneList[0] = (LinearLayout) findViewById(R.id.hrZone0);
        hrZoneList[1] = (LinearLayout) findViewById(R.id.hrZone1);
        hrZoneList[2] = (LinearLayout) findViewById(R.id.hrZone2);
        hrZoneList[3] = (LinearLayout) findViewById(R.id.hrZone3);
        hrZoneList[4] = (LinearLayout) findViewById(R.id.hrZone4);
        hrZoneList[5] = (LinearLayout) findViewById(R.id.hrZone5);
    }

    public void restartGrabbingData(final boolean withShowingTime){

        //stopWatch = (Chronometer) findViewById(R.id.chronometer);

        /*
        stopWatch.setBase(SystemClock.elapsedRealtime());

        stopWatch.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer arg0) {
                secondsCount = (SystemClock.elapsedRealtime() - arg0.getBase()) / 1000;
                mainView.setTimer(secondsCount);
                //Log.d(tag, "nChronometerTick() called, secondsCount : " + secondsCount);

                routeSubject.addPointStruct(new PointStruct(
                        new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()),
                        Float.valueOf(lastLocation.getSpeed() * 3.6f),   // * 3.6 => in km/h
                        heartRate,      //detected from IHeartRateDataReceiver
                        null,
                        lastLocation.getTime()
                ));
            }
        });
        stopWatch.start();
        */


        long delayTime = 0, intervalTime = 500;

        timer = new Timer();
        final long startTime = SystemClock.elapsedRealtime();
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(withShowingTime) {
                            long secondsCount2 = (SystemClock.elapsedRealtime() - startTime) / 1000;
                            Log.d(tag, "secondsCount2 : " + secondsCount2);
                            mainView.setTimer(secondsCount2);
                        }

                        routeSubject.addPointStruct(new PointStruct(
                                new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()),
                                Float.valueOf(lastLocation.getSpeed() * 3.6f),   // * 3.6 => in km/h
                                heartRate,      //detected from IHeartRateDataReceiver
                                null,
                                lastLocation.getTime()
                        ));
                    }
                });

            }
        }, delayTime, intervalTime);




    }


    public void stopCountSeconds(){
        //stopWatch.stop();
        timer.cancel();
    }

    public void nullTimer(){
        mainView.setTimer(null);
        //stopWatch.stop();
        if(timer != null)
            timer.cancel();
    }





    public void verifyConnections() {
        if (!Utility.checkNetworkConnection(this)) {
            Utility.showAlertDialog(this, this.getString(R.string.app_name), this.getString(R.string.String0040), this.getString(R.string.String0042), new IUtilitySingleButtonHandler() {
                @Override
                public void OnClick() {
                    startActivity(new Intent(Settings.ACTION_SETTINGS));
                }
            });
        } else {
            if (!Utility.checkLocationServiceEnabled(this)) {
                Utility.showAlertDialog(this, this.getString(R.string.app_name), this.getString(R.string.String0041), this.getString(R.string.String0042), new IUtilitySingleButtonHandler() {
                    @Override
                    public void OnClick() {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
            }else
                //after success connection, register callback

                if (locationDetector == null) {
                    locationDetector = LocationDetector.getInstance();
                    lastLocation = locationDetector.getLastKnowLocation();
                    locationDetector.addCallback(this);
                    locationDetector.tryStartRequestForLocation();
                }

                //1. try create and connect to via socket

                //if recording refresh views

                //2. Connect to dbs and try sync routes
                //new DbsConnector(this).connectDbs();

                //syncRoutes();


        }
    }

    private void init(){
        createInitObserverPattern();
        setMainView();
        initMultimediaContent();
        initOTGCamService();
        tryConnectOTGCam();
    }

    private void createInitObserverPattern(){
        routeSubject = new RouteSubject();
        permissionsSubject = new TvPermissionsSubject(this);

        permissionHandler = new PermissionHandler();
        permissionsSubject.addObserver(permissionHandler);

        URI uri;
        try {
            mainView = new MainView(findViewById(android.R.id.content), routeSubject);
            uri = new URI("ws://"+GlobalConfig.HOST_IP+":"+ SocketConfig.SOCKET_PORT_NUMBER);
            socketClient = new SocketClient(uri, this, routeSubject);
            dbsHelper = new SQLiteDbsHelper(getApplicationContext(), routeSubject);

            routeSubject.addObserver(mainView);

            addSocketToObserver();      //!!! call just if tv client send request for stream


        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        connectToSocket();        //also try connect to socket

        permissionsSubject.updateUserState();   //set user state to 2 in external dbs

        //setPointStruct will be called after obtain data from ANT+ and GPS
        //routeSubject.addPointStruct(new PointStruct(null, null, null, null, 1234567890l));    //ee will calculate in routeSubject
    }


    /* RTSP STREAMING PROTOCOL INIT */

    private void initMultimediaContent(){

        mSession = net.majorkernelpanic.streaming.SessionBuilder.getInstance()
                .setContext(getApplicationContext())
                .setAudioEncoder(net.majorkernelpanic.streaming.SessionBuilder.AUDIO_AAC)
                .setAudioQuality(new net.majorkernelpanic.streaming.audio.AudioQuality(8000, 16000))
                .setVideoEncoder(net.majorkernelpanic.streaming.SessionBuilder.VIDEO_H264)
                .setPreviewOrientation(90)
                .setSurfaceView(mMySurfaceView)
                .setPreviewOrientation(0)
                .setCallback(this)
                .build();


        // Configures the RTSP client
        mClient = new net.majorkernelpanic.streaming.rtsp.RtspClient();
        mClient.setSession(mSession);
        mClient.setCallback(this);

        // Use this to force streaming with the MediaRecorder API
        mSession.getVideoTrack().setStreamingMethod(MediaStream.MODE_MEDIACODEC_API_2);

        // Use this to stream over TCP, EXPERIMENTAL!
        //mClient.setTransportMode(RtspClient.TRANSPORT_TCP);

        // Use this if you want the aspect ratio of the surface view to
        // respect the aspect ratio of the camera preview
        mMySurfaceView.setAspectRatioMode(MySurfaceView.ASPECT_RATIO_PREVIEW);

        mMySurfaceView.getHolder().addCallback(this);


        selectQuality(1);
    }

    /* RTSP STREAMING PROTOCOL SET QUALITY */

    private void selectQuality(int index) {
        String RES_FPS_BITRATE[] = new String[]{"320x240, 30 fps, 250 Kbps", "640x480, 30 fps, 600 Kbps"};

        Pattern pattern = Pattern.compile("(\\d+)x(\\d+)\\D+(\\d+)\\D+(\\d+)");
        Matcher matcher = pattern.matcher(RES_FPS_BITRATE[index]);

        matcher.find();
        int width = Integer.parseInt(matcher.group(1));
        int height = Integer.parseInt(matcher.group(2));
        int framerate = Integer.parseInt(matcher.group(3));
        int bitrate = Integer.parseInt(matcher.group(4))*1000;

        mSession.setVideoQuality(new VideoQuality(width, height, framerate, bitrate));
        Toast.makeText(this, RES_FPS_BITRATE[index], Toast.LENGTH_SHORT).show();

        Log.d(tag, "Selected resolution: " + width + "x" + height);
    }

    /* RTSP STREAMING PROTOCOL START */

    public void stopVideoStream(){
        if (mClient.isStreaming())
            mClient.stopStream();
    }

    public void toggleStream(String streamName) {
        Log.d(tag, "tryRunToogleStream(), name : " + streamName);
        String uriStream = Constants.WOWZA_URI_BASE_LIVE + streamName;
        if (!mClient.isStreaming() && streamName != null) {
            Log.d(tag, "toggleStream(), name : " + streamName);
            String ip,port,path;

            // We save the content user inputs in Shared Preferences
            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putString("uri", uriStream);
            editor.putString("password", "");
            editor.putString("username", "");
            editor.commit();

            // We parse the URI written in the Editext
            Pattern uri = Pattern.compile("rtsp://(.+):(\\d*)/(.+)");
            Matcher m = uri.matcher(uriStream); m.find();
            ip = m.group(1);
            port = m.group(2);
            path = m.group(3);

            mClient.setCredentials("", "");
            mClient.setServerAddress(ip, Integer.parseInt(port));
            mClient.setStreamPath("/" + path);
            mClient.startStream();
        }
    }

    private String getJSONString(HashMap values){
        JSONObject jsonObj = new JSONObject(values);
        return jsonObj.toString();
    }

    /* START RECORDING USING WOWZA API */

    private void startVideoRecording(String outputVideoName, String userName){  //stream name is same like user name
        String stringUrl = Constants.URL_RECORD+userName+
                Constants.URL_RECORD_OUTPUT_FILE+outputVideoName+
                Constants.URL_RECORD_FILE_PATH+userName+
                Constants.URL_RECORD_ACTION_START;
            Log.d(tag, "startVideoRecording() called : " + stringUrl);

            UtilityAsyncRequest request = new UtilityAsyncRequest(new IUtilityAsyncResponse() {
                @Override
                public void setResponse(String response, Exception ex, Object state) {
                    Log.d(tag, "startVideoRecording RESPONSE : " + response);   //after response from WOWZA API, stop rec user data, and sent it to external DBS
                    isRecording = true;
                }
            });
            request.execute(stringUrl);

    }

    private void stopVideoRecording(String userName){   //stream name is same like user name
        String stringUrl = Constants.URL_RECORD+userName+Constants.URL_RECORD_ACTION_STOP;
        Log.d(tag, "stopVideoRecording() called : " + stringUrl);

        //http://147.175.145.110:8086/livestreamrecord?app=live&streamname=tp@gmail.com&action=stopRecording

        UtilityAsyncRequest request = new UtilityAsyncRequest(new IUtilityAsyncResponse() {
            @Override
            public void setResponse(String response, Exception ex, Object state) {
                Log.d(tag, "stopVideoRecording RESPONSE : " + response);
                routeSubject.stopRec("");   //put name of route
                isRecording = false;
            }
        });
        request.execute(stringUrl);

    }

    private void connectToSocket(){
        socketClient.connect();
    }


    private void addSocketToObserver(){
        routeSubject.addObserver(socketClient);
        permissionsSubject.addObserver(socketClient);
    }
    private void removeSocketFromObserver(){
        routeSubject.deleteObserver(socketClient);
        permissionsSubject.deleteObserver(socketClient);
    }

    @Override
    protected void onResume(){
        super.onResume();

        verifyConnections();
    }

    @Override
    protected void onPause(){
        super.onPause();

    }

    @Override
    public void onBackPressed(){

        Log.d(tag, "onBackPressed() called");
        moveTaskToBack(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startNEwRoute(){
        /*
            create new routeLiveData
            add mainView to routeLiveData
            remove mainView observer from routeSubject

            add new routeLiveData observer to routeSubject
        */

    }

    @Override
    public boolean handleMessage(Message msg) {
        switch(msg.what) {

            case MESSAGE_SHOW_TOAST:
                    Bundle args = null;
                    String text = null;
                    if ((args = msg.getData()) != null) {
                        text = args.getString(TOAST_STRING_PARAMETER_1);
                    }
                    if (text == null) {
                        return true;
                    }
                    showToast(text, msg.arg1);
                    break;

            case Config.DBS_CONNECTION:
                Connection dbsConnection = (Connection) msg.obj;
                if (dbsConnection != null) {
                    this.dbsConnection = dbsConnection;
                    Log.d(tag, "dbsConnection : " + dbsConnection.toString());
                    //routes = handler(dbsConnection).getRoutes();
                    //new ServicesHandler(this, dbsConnection).getPopulatedRoutes();


                } else {
                    Utility.showToast(this, R.string.String0030);
                }

                break;

            case Config.UPDATE_LOCATION :
                this.lastLocation = (Location) msg.obj;

                //Log.d(tag, "UPDATE_LOCATION unformmated speed : " + lastLocation.getSpeed() + " , formatted speed" + (Float.valueOf(lastLocation.getSpeed())*3.6f) + " , lat: " + lastLocation.getLatitude() + " , lon: " +  lastLocation.getLongitude());
                //now must try observer all observers:

                //..but first try obtain also HR

                if(!routeSubject.isRec()){      //if is free riding, update states just after detect location (mostly for socket), hr is always independent in mainView
                    routeSubject.addPointStruct(new PointStruct(
                            new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()),
                            Float.valueOf(lastLocation.getSpeed() * 3.6f),   // * 3.6 => in km/h
                            heartRate,      //detected from IHeartRateDataReceiver
                            null,
                            lastLocation.getTime()) );
                }

                //if(findViewById(android.R.id.content).getId() == getResources().getLayout(R.layout.activity_main))

                //now just create new point and update routeSubject


        }
        return false;
    }

    @Override
    public void onBitrateUpdate(long l) {

    }

    @Override
    public void onSessionError(int i, int i1, Exception e) {

    }

    @Override
    public void onPreviewStarted() {

    }

    @Override
    public void onSessionConfigured() {

    }

    @Override
    public void onSessionStarted() {
        Log.d(tag, "onSessionStarted()");
        new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                Log.d(tag, "onSessionStarted()2");
                socketClient.sendSignal(socketClient.STREAM_WAS_STARTED);
                return false;
            }
        }).sendEmptyMessageDelayed(0, 5000);
    }

    @Override
    public void onSessionStopped() {

    }

    @Override
    public void onRtspUpdate(int message, Exception e) {
        Log.d(tag, "onRtspUpdate : " + message);
        if(e != null)
            Log.d(tag, "onRtspUpdate : exception " + e.toString());

        switch (message) {
            case net.majorkernelpanic.streaming.rtsp.RtspClient.ERROR_CONNECTION_FAILED:
            case net.majorkernelpanic.streaming.rtsp.RtspClient.ERROR_WRONG_CREDENTIALS:
                //mProgressBar.setVisibility(View.GONE);
                //enableUI();
                //logError(e.getMessage());
                e.printStackTrace();
                break;
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d(tag, "onSurfaceCreated");
        mSession.startPreview();        //here is main starter to preview camera content on surfaceview !!!
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.d(tag, "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d(tag, "surfaceDestroyed");
        mClient.stopStream();
    }


    /*

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        Log.d(tag, "GL10 onSurfaceCreated");
        //mSession.startPreview();        //here is main starter to preview camera content on surfaceview !!!
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        Log.d(tag, "GL10 onSurfaceChanged");
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        Log.d(tag, "GL10 onDrawFrame");
    }

    */


    private class OnAbstractOnClicksLInstener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.txt4 :
                    Intent i = new Intent(MainActivity.this, Activity_AsyncScanHeartRateSampler.class);
                    getIntent().setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    startActivityForResult(i, HR_PCC_REQUEST);


                    break;

                case R.id.btn1 :
                    /*
                    new ExternalDbsHelper()
                            .setCMD(ExternalDbsHelper.CMD_TRY_SEND_ROUTE)
                            .setIdRoute(1)
                            .execute();

                            */

                    break;

                case R.id.btn2 :

                    break;

                case R.id.btn3 :
                    //mSession.switchCamera();
                    Log.d(tag, "switchCamera btn clicked");

                    if(isStarted) {
                        try {
                            isStarted = false;
                            mSession.switchCamera();
                            tryStopOTGStream();
                            //mMySurfaceView.startGLThread();
                        } catch (RemoteException e) {
                        }finally {
                            //mMySurfaceView.startGLThread();
                        }
                    }else {

                        try {
                            if(isPackageInstalled(Constants.ULIB_EXPRESS_PKG_NAME, getPackageManager())) {
                                mSession.switchCamera();
                                tryStartOTGStream();
                                //mMySurfaceView.startGLThread();
                                isStarted = true;
                            }else{
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.ULIB_EXPRESS_URL)));
                            }
                        } catch (RemoteException e) {
                            //mMySurfaceView.startGLThread();
                            e.printStackTrace();
                            isStarted = false;
                        }
                    }

                    break;

                case R.id.tb_logout :
                    logout();
                    break;
            }

        }
    }


    private boolean isPackageInstalled(String packagename, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void logout(){
        Utility.setSettingsInteger(this, -1, Constants.ID_FLEET);
        Intent i = new Intent(this, StartActivity.class);
        startActivity(i);
        finish();
    }

    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if(requestCode == HR_PCC_REQUEST) {
            switch(resultCode){
                case RESULT_OK :
                    Log.d(tag, "RESULT_OK");
                    subscribeToHrEvents();
                    break;
                case RESULT_CANCELED :
                    Log.d(tag, "RESULT_CANCELED");
                    break;
            }
        }

    }

    public void subscribeToHrEvents() {
        MyApp.hrPcc.subscribeHeartRateDataEvent(new AntPlusHeartRatePcc.IHeartRateDataReceiver() {
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
                        heartRate = computedHeartRate;
                        displayHr(heartRate);
                        //Log.d(tag, "HR heartrate : " + textHeartRate);
                    }
                });
            }
        });

    }

    private void displayHr(int heartRate){
        mainView.setHR(heartRate);

        if(hrZoneAndKcalMonitor == null)
            hrZoneAndKcalMonitor = new HrZoneAndKcalMonitor();

        int zone = hrZoneAndKcalMonitor.getHrZone(heartRate);

        //Log.d(tag, "subscribeHeartRateDataEvent() called, hr ; " + heartRate + " , zone : " + zone);

        int padding = (int) (10*metrics.density);
        for(int i = 0; i < hrZoneList.length; i++){
            if(i == zone)
                hrZoneList[i].setPadding(0, 0, 0, 0);
            else
                hrZoneList[i].setPadding(padding, padding, padding, padding);
        }

    }

    @Override
    protected void onStart()
    {
        Log.d(tag, "onStart");
        handler = new Handler(this);
        super.onStart();
    }

    @Override
    protected void onStop()
    {
        Log.d(tag, "onStop");
        unbindService();
        super.onStop();
    }



    @Override
    protected void onDestroy() {
        Log.d(tag, "onDestroy() called");

        unbindService();
        super.onDestroy();
    }


    /* CODE FOR OTG CAMERA */



    public void tryStartOTGStream() throws RemoteException{
        //mMySurfaceView.initMjpegView(this);
        //mMySurfaceView.startPlayback();
        mMySurfaceView.setOtgActive(true);

        try {
            if (serviceIf != null) {
                Log.d(tag, "sending serviceIf request : CALL_VIDEO_STREAM_START");
                serviceIf.send(UsbVideoConstructs.CALL_VIDEO_STREAM_START,
                        0, 0, null);
            }
        } catch (RemoteException e) {
            Log.e(tag, e.getMessage(), e);
            throw e;
        }
    }

    public void tryStopOTGStream() throws RemoteException{
        mMySurfaceView.setOtgActive(false);
        try {
            if (serviceIf != null) {
                Log.d(tag, "sending serviceIf request : CALL_VIDEO_STREAM_STOP");
                serviceIf.send(UsbVideoConstructs.CALL_VIDEO_STREAM_STOP,
                        0, 0, null);

            }
        } catch (RemoteException e) {
            Log.e(tag, e.getMessage(), e);
            throw e;
        }
    }

    public void tryConnectOTGCam(){
        if (!bindService) {
            Intent intent = new Intent();
            intent.setClassName("infinitegra.usb.video.app",
                    "infinitegra.usb.video.app.MainService");
            this.bindService(intent, serviceConn,
                    Context.BIND_AUTO_CREATE);

            Log.d(tag, "OTG bind service");
        }
    }

    public void initOTGCamService(){

        serviceConn = new ServiceConnection() {

            @Override
            public void onServiceDisconnected(ComponentName name)
            {
                Log.d(tag, "OTG service disconnected");
                serviceStarted = false;
                bindService = false;
                serviceIf = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service)
            {
                bindService = true;
                Log.d(tag, "OTG service connected");
                callback = new IUsbVideoCallback.Stub() {

                    @Override
                    public void onNotifyMessage(int event, int arg1, int arg2,
                                                String arg3)
                    {
                        Log.d(tag, "onNotifyMessage : " + event + " arg1 : " + arg1 + " arg2 : " + arg2 + " arg3 : " + arg3);

                        switch (event) {
                            case UsbVideoConstructs.RECV_NOTIFY_ERROR:
                                Log.d(tag, "OTG notify error. Error id = " + arg1);
                                onNotifyError(arg3);
                                break;
                            case UsbVideoConstructs.RECV_IMAGE_SIZE:
                                setImageSize(arg1, arg2);
                                break;
                            case UsbVideoConstructs.RECV_IMAGE_TEXTURE_SIZE:
                                setImageTextureSize(arg1, arg2);
                                break;
                            case UsbVideoConstructs.RECV_POST_START_STREAM:
                                postStartStream();
                                break;
                            case UsbVideoConstructs.RECV_RUNNING:
                                running = arg1 == 1 ? true : false;
                                break;
                            default:
                                break;
                        }
                    }

                    @Override
                    public ParcelFileDescriptor encodeVideo(int size) throws RemoteException {
                        return null;
                    }

                    @Override
                    public ParcelFileDescriptor renderStream(int size)
                            throws RemoteException
                    {
                        Log.d(tag, "renderStream : getReady " + mMySurfaceView.getReady());
                        if (mMySurfaceView == null || !mMySurfaceView.getReady()) {
                            return null;
                        }
                        ParcelFileDescriptor pfd = null;
                        try {
                            pfd = pipeTo(size);
                        } catch (IOException e) {
                            pfd = null;
                            e.printStackTrace();
                        }
                        return pfd;
                    }

                    public void postStartStream()
                    {
                        isStarted = true;
                    }

                };

                serviceIf = IUsbVideoService.Stub.asInterface(service);
                try {
                    serviceIf.registerCallback(callback);
                } catch (RemoteException re) {
                    // nop.
                }
                serviceStarted = true;
                isStarted = false;
            }
        };
    }


    private ParcelFileDescriptor pipeTo(int size) throws IOException
    {
        //Log.d(tag, "pipeTo, size : " + size);

        ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
        ParcelFileDescriptor readSide = pipe[0];
        ParcelFileDescriptor writeSide = pipe[1];

        // create thread every time.
        // This is simple code for sample application.
        new TransferThread(
                new ParcelFileDescriptor.AutoCloseInputStream(readSide),
                size).start();
        return writeSide;
    }

    class TransferThread extends Thread
    {
        InputStream is;
        int bufSize;

        TransferThread(InputStream in, int size)
        {
            super("ParcelFileDescriptor Transfer Thread");
            //Log.d(tag, "TransferThread constructor called");
            is = in;
            bufSize = size;
        }

        @Override
        public void run()
        {
            byte[] buf = new byte[1024];
            int len;
            ByteBuffer bb = ByteBuffer.allocate(bufSize);
            try {
                while ((len = is.read(buf)) > 0) {
                    bb.put(buf, 0, len);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            } catch (Exception xe) {
                xe.printStackTrace();
                return;
            }

            byte[] image = bb.array();
            Log.d(tag, "mSurfaceView.render image lenght " + image.length);
            //mMySurfaceView.render(image);
            mMySurfaceView.updateOTGImage(image);
        }
    }

    private void onNotifyError(String errorMessage)
    {
        Log.d(tag, errorMessage);
        showToast(Toast.LENGTH_SHORT, errorMessage);
    }

    private void setImageSize()
    {
        if (mMySurfaceView == null)
            return;
        if (width > 0 && height > 0 && txW > 0 && txH > 0) {
            mMySurfaceView.setImageSize(width, height, txW, txH);
        }
    }

    private void setImageSize(int width, int height)
    {
        this.width = width;
        this.height = height;
        setImageSize();
    }

    private void setImageTextureSize(int txw, int txh)
    {
        this.txW = txw;
        this.txH = txh;
        setImageSize();
    }

    public void showToast(String s, int length)
    {
        Toast.makeText(this, s, length).show();
    }

    private void showToast(int length, String value)
    {
        if (handler == null) {
            return;
        }

        Message msg = Message.obtain();
        msg.what = MESSAGE_SHOW_TOAST;
        msg.arg1 = length;
        if (value != null) {
            Bundle args = new Bundle();
            args.putString(TOAST_STRING_PARAMETER_1, value);
            msg.setData(args);
        }
        handler.sendMessage(msg);
    }

    private void unbindService()
    {
        if (bindService) {
            bindService = false;
            serviceStarted = false;
            if (callback != null) {
                try {
                    serviceIf.unregisterCallback(callback);
                } catch (RemoteException re) {
                    // nop.
                }
                callback = null;
            }
            serviceIf = null;
            isStarted = false;
            unbindService(serviceConn);
            running = false;
            Log.d(tag, "OTG unbindService");
        }
    }


}
