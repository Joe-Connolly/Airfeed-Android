package com.artfara.apps.kipper;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {
    public final static String EXTRA_USERNAME = "com.artfara.kipper.USERNAME";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void onLoginClicked(View view) {
        String username = ((EditText) findViewById(R.id.username)).getText().toString();
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(EXTRA_USERNAME, username);
        startActivity(intent);
    }
}
