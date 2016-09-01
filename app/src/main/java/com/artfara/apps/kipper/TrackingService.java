package com.artfara.apps.kipper;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;

//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Date;


public class TrackingService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    // Binder given to clients
    private final IBinder mBinder = new TrackingBinder();
    private DatabaseReference mDatabase;

    private String mUserName;
    public String test = "test";
    private Double increment;
    private GoogleApiClient mGoogleApiClient;
    public com.google.android.gms.location.LocationListener mLocationListener = new com.google.android.gms.location.LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            Log.d("service", "location updated, location = " + location + "");

            mDatabase.child("users").child(mUserName).setValue(new Latlng(location.getLatitude() + increment, location.getLongitude()));
            increment++;
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("service", "onStartCommand");
        mUserName = intent.getStringExtra(LoginActivity.EXTRA_USERNAME);
        Log.d("service", "mUserName = " + mUserName);

        Latlng testlatlng = new Latlng(42.0, 73.0);
        // Write a latlng to the database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        increment = 1.0;



        startLocationUpdates();



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


    public TrackingService() {
    }


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


    @Override
    public void onConnected(@Nullable Bundle bundle) {

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            Log.d("service", "required permissions not set");
//        }
//        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
//                mGoogleApiClient);
//        Log.d("service", lastLocation + "");

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, mLocationListener);

    }


    //required but unused methods
    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


}
