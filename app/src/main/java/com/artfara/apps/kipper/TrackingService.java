package com.artfara.apps.kipper;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class TrackingService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    private static final String TAG = "Tracking Service ";

    //testing ignore
//    double increment =  .05;
//    Runnable mR;
    private int mCount;
    private Map<String, Object> mHashMap;
    private LatLng mLocation;
    private DatabaseReference mDatabase;
    private String mUserName;
    private GoogleApiClient mGoogleApiClient;
    private ArrayList<Place> mCurrentPlaces;
    private SharedPreferences mPrefs;
    private String mID;
    private boolean serviceAlreadyStarted = false;

    @Override
    public void onCreate(){
        Log.d("service", "onCreate");

        if (serviceAlreadyStarted){
            return;
        }
        serviceAlreadyStarted = true;

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mID = mPrefs.getString(Constants.ID_KEY, null);
        if (mID == null){
            Log.d(TAG, "ID NULL");
            mID = mDatabase.child(Constants.USERS_TABLE_NAME).push().getKey();
            mPrefs.edit().putString(Constants.ID_KEY, mID).apply();
        }
        mID = Constants.TEST_KEY;
        Log.d(TAG, "loading key = " + Constants.ID_KEY + " " + mID);

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



    public LocationListener mLocationListener = new com.google.android.gms.location.LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "location updated, location = " + location + "");
            Date today = new Date();
            final DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd hhmmss");
            dateFormatter.setLenient(false);
            String s = dateFormatter.format(today);
            Latlng loc = new Latlng(location.getLatitude(), location.getLongitude(), true, "Tracking " +s);


            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put(mID, loc.toMap());
            mDatabase.child(Constants.USERS_TABLE_NAME).updateChildren(childUpdates);
        }
    };






    @Override
    //Remove notifications if activity is shut down
    public void onDestroy() {

        Log.d(TAG, "onDestroy");
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, mLocationListener);
        mGoogleApiClient.disconnect();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(6000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (Build.VERSION.SDK_INT <= 22 || ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, mLocationListener);
        }
    }








    //*******************************************************************************************
    //ALL METHODS and FIELDS BELOW THIS LINE ARE NOT USED
    //*******************************************************************************************

    //OnStartCommand gets called multiple times, so use onCreate() instead
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
    public TrackingService() {}
    @Override
    public IBinder onBind(Intent intent) {
        return (mBinder);
    }

    public class TrackingBinder extends Binder {
        TrackingService getService() {
            // Return this instance of DownloadBinder so MapDisplayActivity can call public methods
            return TrackingService.this;
        }
    }
    private final IBinder mBinder = new TrackingBinder();

    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


}
