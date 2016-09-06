package com.artfara.apps.kipper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "Login Activity ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void onLoginClicked(View view) {

        String username = ((EditText) findViewById(R.id.username)).getText().toString();
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
        //save username to internal storage
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs.edit().putString(Constants.USERNAME_KEY, username).commit();
    }
}
