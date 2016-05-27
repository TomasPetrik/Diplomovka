package com.pop24.androidapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.pop24.androidapp.config.Constants;
import com.pop24.androidapp.external_dbs.ExternalDbsHelper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Tomas on 30. 11. 2015.
 */

interface State{
    void startAction();
    void execute (StateContext state);
}

abstract class StateStartup{
    public String tag = "StateStartup";
    public Activity activity;
    public Button btn1, btn2;
    public TextView txt1;
    public EditText edit1, edit2, edit3, edit4;

    public StateStartup(Activity activity){
        this.activity = activity;

        btn1 = (Button)activity.findViewById(R.id.btn1);
        btn2 = (Button)activity.findViewById(R.id.btn2);
        txt1 = (TextView)activity.findViewById(R.id.txt1);
        edit1 = (EditText)activity.findViewById(R.id.edit1);
        edit2 = (EditText)activity.findViewById(R.id.edit2);
        edit3 = (EditText)activity.findViewById(R.id.edit3);
        edit4 = (EditText)activity.findViewById(R.id.edit4);


    }

    public abstract void adjustView (View view);

    public void showFailedMsg(int resString){
        Utility.showToast(activity, resString);
    }

    public Boolean validateData(){
        if(!Utility.isEmailAddress(edit1.getText().toString())){
            edit1.requestFocus();
            return false;
        }else if(Utility.stringIsNullOrEmpty(edit2.getText().toString())){
            edit2.requestFocus();
            return false;
        }

        Utility.setSettingsString(activity, edit1.getText().toString(), Constants.EMAIL);
        Utility.setSettingsString(activity, edit2.getText().toString(), Constants.PASSWORD);

        return true;
    }

    public void goToNextActivity(Intent intent){
        activity.startActivity(intent);
    }

}

class StateLogin extends StateStartup implements State{

    public StateLogin(Activity activity) {
        super(activity);
    }

    @Override
    public void startAction() {
        if (validateData()) {
            //Log.d(tag, "on login clicked()");
            ExternalDbsHelper externalDbsHelper = new ExternalDbsHelper.Builder().setContent(activity)
                            .setCMD(ExternalDbsHelper.CMD_TRY_LOGIN)
                            .setCallback(new Handler.Callback() {
                                @Override
                                public boolean handleMessage(Message message) {
                                    switch (message.what) {
                                        case ExternalDbsHelper.RESULT_STATE_OK:
                                            try {
                                                ResultSet resultSet = (ResultSet) message.obj;
                                                resultSet.last();
                                                int rsSize = resultSet.getRow();
                                                resultSet.beforeFirst();

                                                String[] fleetNames = new String[rsSize];
                                                int[] idsFleets = new int[rsSize];

                                                int i = 0;
                                                while (resultSet.next()) {

                                                    Log.d(tag, "found idUser = " + resultSet.getInt(2) + " , " + resultSet.getString(3));

                                                    Utility.setSettingsInteger(activity, resultSet.getInt(3), Constants.ID_USER);
                                                    Utility.setSettingsString(activity, resultSet.getString(4), Constants.NICK_NAME);
                                                    Utility.setSettingsString(activity, resultSet.getString(5), Constants.EMAIL);
                                                    Utility.setSettingsString(activity, resultSet.getString(6), Constants.PASSWORD);


                                                    Log.d(tag, "fleetName = " + resultSet.getString(1) +
                                                            "idFleet = " + resultSet.getInt(2) +
                                                            ",idUser = " + Utility.getSettingsInteger(activity, Constants.ID_USER, 0) +
                                                            " ,nick = " + Utility.getSettingsString(activity, Constants.NICK_NAME, "") +
                                                            " ,email = " + Utility.getSettingsString(activity, Constants.EMAIL, "") +
                                                            " ,password = " + Utility.getSettingsString(activity, Constants.PASSWORD, ""));


                                                    fleetNames[i] = resultSet.getString(1);
                                                    idsFleets[i] = resultSet.getInt(2);

                                                    i += 1;

                                                }

                                                Intent intent = new Intent(activity, ActivityIntroSettings.class);
                                                intent.putExtra("fleetNames", fleetNames);
                                                intent.putExtra("idsFleets", idsFleets);
                                                goToNextActivity(intent);


                                            } catch (SQLException e) {
                                                e.printStackTrace();
                                                break;
                                            }
                                            break;

                                        case ExternalDbsHelper.RESULT_STATE_FAILED:
                                            showFailedMsg(R.string.String0021);
                                            break;
                                    }
                                    return false;
                                }
                            }).build();
            externalDbsHelper.execute();
        } else {
            showFailedMsg(R.string.String0020);
        }
    }


    @Override
    public void adjustView(View view) {
        txt1.setText(R.string.String0003);
        btn2.setText(R.string.String0007);
        btn1.setText(R.string.String0005);
        edit3.setVisibility(View.GONE);
        edit4.setVisibility(View.GONE);
    }

    @Override
    public void execute(StateContext state) {
        state.setState(new StateRegister(activity));
        state.setInvokerState(this);
        adjustView(activity.findViewById(android.R.id.content));
    }

}

class StateRegister extends StateStartup implements State{
    public StateRegister(Activity activity) {
        super(activity);
    }

    @Override
    public void startAction() {
        if (validateData()) {
            //Log.d(tag, "on login clicked()");
            ExternalDbsHelper externalDbsHelper = new ExternalDbsHelper.Builder().setContent(activity)
                    .setCMD(ExternalDbsHelper.CMD_TRY_REGISTER)
                    .setCallback(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message message) {
                            switch (message.what) {
                                case ExternalDbsHelper.RESULT_USER_ALREADY_EXIST:
                                    showFailedMsg(R.string.String0024);
                                    break;
                                case ExternalDbsHelper.RESULT_USER_HAS_NOT_BEEN_INVITED:
                                    showFailedMsg(R.string.String0025);
                                    break;
                                case ExternalDbsHelper.RESULT_STATE_OK:
                                    try {
                                        ResultSet resultSet = (ResultSet) message.obj;
                                        resultSet.last();
                                        int rsSize = resultSet.getRow();
                                        resultSet.beforeFirst();

                                        String[] fleetNames = new String[rsSize];
                                        int[] idsFleets = new int[rsSize];

                                        int i = 0;
                                        while (resultSet.next()) {

                                            //Log.d(tag, "found idUser = " + resultSet.getInt(2) + " , " + resultSet.getString(3));

                                            Utility.setSettingsInteger(activity, resultSet.getInt(3), Constants.ID_USER);
                                            Utility.setSettingsString(activity, resultSet.getString(4), Constants.NICK_NAME);
                                            Utility.setSettingsString(activity, resultSet.getString(5), Constants.EMAIL);
                                            Utility.setSettingsString(activity, resultSet.getString(6), Constants.PASSWORD);


                                            Log.d(tag, "fleetName = " + resultSet.getString(1) +
                                                    "idFleet = " + resultSet.getInt(2) +
                                                    ",idUser = " + Utility.getSettingsInteger(activity, Constants.ID_USER, 0) +
                                                    " ,nick = " + Utility.getSettingsString(activity, Constants.NICK_NAME, "") +
                                                    " ,email = " + Utility.getSettingsString(activity, Constants.EMAIL, "") +
                                                    " ,password = " + Utility.getSettingsString(activity, Constants.PASSWORD, ""));


                                            fleetNames[i] = resultSet.getString(1);
                                            idsFleets[i] = resultSet.getInt(2);

                                            i += 1;

                                        }

                                        Intent intent = new Intent(activity, ActivityIntroSettings.class);
                                        intent.putExtra("fleetNames", fleetNames);
                                        intent.putExtra("idsFleets", idsFleets);
                                        goToNextActivity(intent);


                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                        break;
                                    }
                                    break;

                                case ExternalDbsHelper.RESULT_STATE_FAILED:
                                    showFailedMsg(R.string.String0023);
                                    break;
                            }
                            return false;
                        }
                    }).build();

                    externalDbsHelper.execute();
        } else {
            showFailedMsg(R.string.String0020);
        }
    }

    @Override
    public Boolean validateData(){
        if(super.validateData() == true){       //continue validation
            if(Utility.stringIsNullOrEmpty(edit4.getText().toString()) ){   //nickname
                edit4.requestFocus();
                return false;
            }else if(Utility.stringIsNullOrEmpty(edit3.getText().toString()) || !edit3.getText().toString().equals(edit2.getText().toString())){    //password again
                edit3.requestFocus();
                return false;
            }

            Utility.setSettingsString(activity, edit3.getText().toString(), Constants.PASSWORD);
            Utility.setSettingsString(activity, edit4.getText().toString(), Constants.NICK_NAME);
        }
        return true;
    }

    @Override
    public void adjustView(View view) {
        txt1.setText(R.string.String0004);
        btn2.setText(R.string.String0008);
        btn1.setText(R.string.String0006);
        edit3.setVisibility(View.VISIBLE);
        edit4.setVisibility(View.VISIBLE);
    }

    @Override
    public void execute(StateContext state) {
        state.setState(new StateLogin(activity));
        state.setInvokerState(this);
        adjustView(activity.findViewById(android.R.id.content));
    }

}


class StateContext{
    private State myState;
    private State invokerState;

    StateContext(Activity activity){
        if(Utility.getSettingsInteger(activity, Constants.ID_USER, 0) > 0)
            setState(new StateLogin(activity));
        else
            setState(new StateRegister(activity));
    }

    void setState(State state){
        this.myState = state;
    }

    void setInvokerState(State invokerState){
        this.invokerState = invokerState;
    }

    public void execute(){
        myState.execute(this);
    }

    public void startAction(){
        this.invokerState.startAction();
    }
}

public class LoginRegisterActivity extends AppCompatActivity {
    StateContext stateContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_registration);
        setListeners();

        stateContext = new StateContext(this);
        stateContext.execute(); //init state

    }

    private void setListeners(){
        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stateContext.startAction();
            }
        });

        findViewById(R.id.btn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stateContext.execute();
            }
        });


    }


}
