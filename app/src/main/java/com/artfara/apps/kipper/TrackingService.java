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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class TrackingService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    private static final String TAG = "Tracking Service ";

    //testing ignore
    double increment =  .05;
//    Runnable mR;
    private int mCount;
    private Map<String, Object> mHashMap;
    private LatLng mLocation;
    private DatabaseReference mDatabase;
    private String mUserName;
    private GoogleApiClient mGoogleApiClient;
    private ArrayList<Place> mCurrentPlaces;
    private SharedPreferences mPrefs;

    private boolean mLocationUpdatesApplied = false;

    public LocationListener mLocationListener = new com.google.android.gms.location.LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
//            Log.d(TAG, "location updated, location = " + location + "");
//            String ID = mPrefs.getString(Constants.ID_KEY, null);
            //If user is not already in database, add them

//            if (ID == null){

            increment++;
            Latlng loc = new Latlng(location.getLatitude() + increment, location.getLongitude(), true);
            mLocation = new LatLng(location.getLatitude(), location.getLongitude());
            //for testing
//            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            //Get location from user click in MapsActivity.java for testing purposes
//            loc = new Latlng(prefs.getFloat("latitude", 0), prefs.getFloat("longitude", 0));

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put(mID, loc.toMap());
            mDatabase.child(Constants.USERS_TABLE_NAME).updateChildren(childUpdates);



//            mDatabase.child(Constants.USERS_TABLE_NAME).child(mUserName).setValue(new Latlng(location.getLatitude(), location.getLongitude()));
            //add a listener to perform a onetime check to determine whether to increment or decrement userCount of nearby places
            mDatabase.child(Constants.PLACES_TABLE_NAME).addListenerForSingleValueEvent(mSingleEventListner);

        }
    };


    ValueEventListener mSingleEventListner = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
//            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            //Get location from user click in MapsActivity.java for testing purposes
            // mLocation = new LatLng(prefs.getFloat("latitude", 0), prefs.getFloat("longitude", 0));
            for (DataSnapshot placeSnapshot : dataSnapshot.getChildren()) {
                Place place = placeSnapshot.getValue(Place.class);
                String placeName = placeSnapshot.getKey();
//                Log.d(TAG, "iterating through " + placeName);
//                Log.d(TAG, "places = " + mCurrentPlaces.toString());
                //If user used to be at a place, but now left, decrement count
                if (userWasAtPlace(place) && !userIsAtPlace(place)) {
                    Log.d(TAG, "Decrementing place = " + placeName + " places = " + mCurrentPlaces.toString());
                    //user is no longer at place so remove them from mCurrentPlaces ArrayList
                    removePlace(place);
                    //decrement user count in database
                    mDatabase.child(Constants.PLACES_TABLE_NAME).child(placeName).runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            Place place = mutableData.getValue(Place.class);
                            if (place == null) {
//                                Log.d(TAG, "data is null");
                                return Transaction.success(mutableData);
                            }
                            place.people--;
                            mutableData.setValue(place);
                            return Transaction.success(mutableData);
                        }
                        @Override
                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {}
                    });

                }
                //If user was not at place, but now is, increment count
                if (!userWasAtPlace(place) && userIsAtPlace(place)) {
//                    Log.d(TAG, "Incrementing place = " + placeName + "places = " + mCurrentPlaces.toString());
                    //user is now at place, so add it to mCurrentPlaces ArrayList
                    mCurrentPlaces.add(place);
                    //increment user count in database
                    mDatabase.child(Constants.PLACES_TABLE_NAME).child(placeName).runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            Place place = mutableData.getValue(Place.class);
                            if (place == null) {
                                Log.d(TAG, "data is null");
                                return Transaction.success(mutableData);
                            }
                            place.people++;
                            mutableData.setValue(place);
                            return Transaction.success(mutableData);
                        }
                        @Override
                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {}
                    });
                }

            }
            //for testing, ignore
            for (int i = 0; i < mCurrentPlaces.size(); i++){
//                Log.d(TAG, mCurrentPlaces.get(i).latitude + " - " + mCurrentPlaces.get(i).longitude);
            }

        }

            @Override
            public void onCancelled(DatabaseError databaseError){
                Log.w(TAG, "getUser:onCancelled", databaseError.toException());
            }
        };
    private String mID;


    private boolean userIsAtPlace(Place place) {
        float[] distance = new float[1]; //stores distance
        Location.distanceBetween(place.latitude, place.longitude, mLocation.latitude, mLocation.longitude, distance);
//        Log.d(TAG, "User is at " + place.location + " within" + place.radius + " ? " + (distance[0] < place.radius));
        return distance[0] < place.radius;
    }

    private boolean userWasAtPlace(Place place) {
        //If user was at place it would be stored in the mCurrentPlaces array
        for (int i = 0; i < mCurrentPlaces.size(); i++){
            if (mCurrentPlaces.get(i).latitude.equals(place.latitude)) {
                return true;
            }

        }
        return false;
    }
    private void removePlace(Place place) {
        for (int i = 0; i < mCurrentPlaces.size(); i++){
            if (mCurrentPlaces.get(i).latitude.equals(place.latitude)) {
                mCurrentPlaces.remove(i);
            }
        }
    }

    @Override
    public void onCreate(){
        Log.d("service", "onCreate");


        if (mLocationUpdatesApplied){
            return;
        }
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mDatabase = FirebaseDatabase.getInstance().getReference();





        mCurrentPlaces = new ArrayList<>();
        startLocationUpdates();
    }


    //OnStartCommand gets called multiple times, so use onCreate() instead
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }





    private void startLocationUpdates() {

        mLocationUpdatesApplied = true;
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mCount = mPrefs.getInt("count", 0);
        mCount++;
        mPrefs.edit().putInt("count", mCount).commit();
        Log.d(TAG, "count = " + mCount);


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

        mID = mDatabase.child(Constants.USERS_TABLE_NAME).push().getKey();
        Log.d(TAG, "loading key = " + Constants.ID_KEY + " " + mID);


        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(6000);
        mLocationRequest.setFastestInterval(4000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, mLocationListener);


        Random random = new Random();
//        for testing add 1000 users to database
        for (int i = 0; i < 1000; i++) {
            String id = "" + i;
            double lat = 0.010 * random.nextDouble() + 43.7010;
            double lng = -0.010 * random.nextDouble() + -72.2890;
            mDatabase.child(Constants.USERS_TABLE_NAME).child(id).setValue(new Latlng(lat, lng, false));
        }

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
