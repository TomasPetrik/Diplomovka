package com.pop24.androidapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.pop24.androidapp.config.Constants;

/**
 * Created by Tomas on 24. 3. 2016.
 */
public class StartActivity extends AppCompatActivity {
    private String tag = "StartActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Utility.getSettingsInteger(getApplicationContext(), Constants.ID_FLEET, -1) < 1)
            goToLoginActivity();
        else
            goToMainActivity();

    }

    private void goToLoginActivity(){
        Intent i = new Intent(StartActivity.this, LoginRegisterActivity.class);
        startActivity(i);
    }

    private void goToMainActivity(){
        Intent i = new Intent(StartActivity.this, MainActivity.class);
        startActivity(i);
    }


}