package com.artfara.apps.kipper;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class TrackingIntentService extends IntentService implements GoogleApiClient.ConnectionCallbacks,  GoogleApiClient.OnConnectionFailedListener {
    private Intent mIntent;
    private DatabaseReference mDatabase;
    private GoogleApiClient mGoogleApiClient;
    private ArrayList<Place> mCurrentPlaces;
    private SharedPreferences mPrefs;
    private String mID;

    private static final String TAG = "Intent Service ";

    public TrackingIntentService() {
        super("TrackingIntentService");
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");

        mIntent = intent;
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mID = mPrefs.getString(Constants.ID_KEY, null);
        if (mID == null){
            Log.d(TAG, "ID NULL");
            mID = mDatabase.child(Constants.USERS_TABLE_NAME).push().getKey();

            mPrefs.edit().putString(Constants.ID_KEY, mID).apply();
        }
        mID = Constants.TEST_KEY;
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //Only start app if we have the permissions we need to access location
        if (Build.VERSION.SDK_INT <= 22 || ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, mLocationListener);
        }

    }

    public LocationListener mLocationListener = new com.google.android.gms.location.LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "location updated, location = " + location + "");
            //For testing
            Date today = new Date();
            final DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd hhmmss");
            dateFormatter.setLenient(false);
            String s = dateFormatter.format(today);


            Latlng loc = new Latlng(location.getLatitude(), location.getLongitude(), true, "intent " +s);


            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put(mID, loc.toMap());
            mDatabase.child(Constants.USERS_TABLE_NAME).updateChildren(childUpdates);



            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, mLocationListener);
            mGoogleApiClient.disconnect();


            AlarmReceiver.completeWakefulIntent(mIntent);
        }
    };







    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

}