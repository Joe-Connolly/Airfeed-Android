package com.artfara.apps.kipper;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;

import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class TrackingService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    private static final String TAG = "Tracking Service ";

    //testing ignore
//    double increment =  1.0;
//    Runnable mR;
    private Map<String, Object> mHashMap;
    private LatLng mLocation;
    private DatabaseReference mDatabase;
    private String mUserName;
    private GoogleApiClient mGoogleApiClient;
    private ArrayList<Place> mCurrentPlaces;
    private SharedPreferences mPrefs;

    public LocationListener mLocationListener = new com.google.android.gms.location.LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "location updated, location = " + location + "");
            String ID = mPrefs.getString(Constants.ID_KEY, null);
            //If user is not already in database, add them
            Log.d(TAG, "loading key = " + Constants.ID_KEY + " " + ID);
            if (ID == null){
                ID = mDatabase.child(Constants.USERS_TABLE_NAME).push().getKey();
                mPrefs.edit().putString(Constants.ID_KEY, ID).commit();
                Log.d(TAG, "saving key = " + Constants.ID_KEY + " " + ID);
            }

            Latlng loc = new Latlng(location.getLatitude(), location.getLongitude());

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put(ID, loc.toMap());
            mDatabase.child(Constants.USERS_TABLE_NAME).updateChildren(childUpdates);



//            mDatabase.child(Constants.USERS_TABLE_NAME).child(mUserName).setValue(new Latlng(location.getLatitude(), location.getLongitude()));
            //add a listener to perform a onetime check to determine whether to increment or decrement userCount of nearby places
            mDatabase.child(Constants.PLACES_TABLE_NAME).addListenerForSingleValueEvent(mSingleEventListner);

        }
    };


    ValueEventListener mSingleEventListner = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            //Get location from user click in MapsActivity.java for testing purposes
            mLocation = new LatLng(prefs.getFloat("latitude", 0), prefs.getFloat("longitude", 0));
            for (DataSnapshot placeSnapshot : dataSnapshot.getChildren()) {
                Place place = placeSnapshot.getValue(Place.class);

                //If user used to be at a place, but now left, decrement count
                if (userWasAtPlace(place) && !userIsAtPlace(place)) {
                    //user is no longer at place so remove them from mCurrentPlaces ArrayList
                    removePlace(place);
                    //decrement user count in database
                    mHashMap = new HashMap<String, Object>();
                    place.people--;
                    mHashMap.put("people", place.people);
                    mDatabase.child(Constants.PLACES_TABLE_NAME).child(place.location).updateChildren(mHashMap);

                }
                //If user was not at place, but now is, increment count
                if (!userWasAtPlace(place) && userIsAtPlace(place)) {
                    //user is now at place, so add it to mCurrentPlaces ArrayList
                    mCurrentPlaces.add(place);
                    //increment user count in database
                    mHashMap = new HashMap<String, Object>();
                    place.people++;
                    mHashMap.put("people", place.people);
                    mDatabase.child(Constants.PLACES_TABLE_NAME).child(place.location).updateChildren(mHashMap);
                }

            }
            //for testing, ignore
            for (int i = 0; i < mCurrentPlaces.size(); i++){
                Log.d(TAG, "place = " + mCurrentPlaces.get(i).location);
                Log.d(TAG, mCurrentPlaces.get(i).latitude + " - " + mCurrentPlaces.get(i).longitude);
            }

        }

            @Override
            public void onCancelled(DatabaseError databaseError){
                Log.w(TAG, "getUser:onCancelled", databaseError.toException());
            }
        };



    private boolean userIsAtPlace(Place place) {
        float[] distance = new float[1]; //stores distance
        Location.distanceBetween(place.latitude, place.longitude, mLocation.latitude, mLocation.longitude, distance);
        return distance[0] < place.radius;
    }

    private boolean userWasAtPlace(Place place) {
        //If user was at place it would be stored in the mCurrentPlaces array
        for (int i = 0; i < mCurrentPlaces.size(); i++){
            if (mCurrentPlaces.get(i).latitude == place.latitude) {
                return true;
            }

        }
        return false;
    }
    private void removePlace(Place place) {
        for (int i = 0; i < mCurrentPlaces.size(); i++){
            if (mCurrentPlaces.get(i).latitude == place.latitude) {
                mCurrentPlaces.remove(i);
            }
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("service", "onStartCommand");

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mDatabase = FirebaseDatabase.getInstance().getReference();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mUserName = prefs.getString(Constants.USERNAME_KEY, "someDefaultValue");


        mCurrentPlaces = new ArrayList<>();
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

        Log.d(TAG, "onDestroy");
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, mLocationListener);
        mGoogleApiClient.disconnect();
    }





    @Override
    public void onConnected(@Nullable Bundle bundle) {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(2000);
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
