package com.artfara.apps.kipper;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;

import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class TrackingService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    //testing ignore
//    double increment =  1.0;
//    Runnable mR;

    private DatabaseReference mDatabase;
    private String mUserName;
    private GoogleApiClient mGoogleApiClient;
    public LocationListener mLocationListener = new com.google.android.gms.location.LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            Log.d("service", "location updated, location = " + location + "");
            mDatabase.child(Constants.USERS_TABLE_NAME).child(mUserName).setValue(new Latlng(location.getLatitude(), location.getLongitude()));

        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("service", "onStartCommand");

        mDatabase = FirebaseDatabase.getInstance().getReference();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mUserName = prefs.getString(Constants.USERNAME_KEY, "someDefaultValue");

        startLocationUpdates();



//        //ignore just for testing
//        mR = new Runnable() {
//            public void run() {
//                mDatabase.child(Constants.USERS_TABLE_NAME).child(mUserName).setValue(new Latlng(42 + increment, 73));
//                increment++;
//                Handler h = new Handler();
//                h.postDelayed(mR, 1000);
//            }
//        };
//        Handler h = new Handler();
//        h.postDelayed(mR, 1000);



        return super.onStartCommand(intent, flags, startId);
    }

    private void startLocationUpdates() {
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
    //Remove notifications if activity is shut down
    public void onDestroy() {

        Log.d("service", "onDestroy");
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, mLocationListener);
        mGoogleApiClient.disconnect();
    }





    @Override
    public void onConnected(@Nullable Bundle bundle) {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, mLocationListener);

    }


    //*******************************************************************************************
    //ALL METHODS and FIELDS BELOW THIS LINE ARE NOT USED
    //*******************************************************************************************

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
