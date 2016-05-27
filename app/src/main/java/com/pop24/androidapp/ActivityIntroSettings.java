package com.pop24.androidapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.pop24.androidapp.config.Constants;

/**
 * Created by Tomas on 23. 11. 2015.
 */
public class ActivityIntroSettings extends AppCompatActivity {
    private String tag = "ActivityIntroSettings";
    String[] fleetNames;
    int[] idsFleets;
    private Spinner spinner, spinner2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_settings);

        fleetNames = getIntent().getStringArrayExtra("fleetNames");
        idsFleets = getIntent().getIntArrayExtra("idsFleets");

        setSpinners();
        setListeners();




    }

    private void setSpinners(){
        String[] items;

        spinner = (Spinner)findViewById(R.id.spinner);
        items = getResources().getStringArray(R.array.string_gender_type);
        Log.d(tag, "items size : " + items.length);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplication(), R.layout.row_spn, items);
        adapter.setDropDownViewResource(R.layout.row_spn_dropdown);
        spinner.setAdapter(adapter);

        spinner2 = (Spinner)findViewById(R.id.spinner2);
        Log.d(tag, "items fleet size : " + fleetNames.length);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(getApplication(), R.layout.row_spn, fleetNames);
        adapter2.setDropDownViewResource(R.layout.row_spn_dropdown);
        spinner2.setAdapter(adapter2);

    }

    private void setListeners(){
        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateData()){
                    Intent i = new Intent(ActivityIntroSettings.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }else{
                    Utility.showToast(getApplication(), R.string.String0020);
                }
            }
        });
    }

    private Boolean validateData(){
        EditText editAge = (EditText)findViewById(R.id.edit1);
        EditText editWeight = (EditText)findViewById(R.id.edit2);

        if(editAge.getText().toString().matches("[0-9]{1,2}")){
            editAge.requestFocus();
        }else
            return false;

        if(editWeight.getText().toString().matches("[0-9]{2,3}")){
            editWeight.requestFocus();
        }else
            return false;


        Utility.setSettingsString(getApplicationContext(), fleetNames[spinner2.getSelectedItemPosition()], Constants.FLEET_NAME);
        Utility.setSettingsInteger(getApplicationContext(), idsFleets[spinner2.getSelectedItemPosition()], Constants.ID_FLEET);

        Utility.setSettingsInteger(getApplicationContext(), Integer.valueOf(editAge.getText().toString()), Constants.PREF_AGE);
        Utility.setSettingsInteger(getApplicationContext(), Integer.valueOf(editWeight.getText().toString()), Constants.PREF_WEIGHT);
        Utility.setSettingsBoolean(getApplicationContext(), spinner.getSelectedItemPosition() == 2 ? false : true, Constants.PREF_GENDER);

        return true;
    }

}
