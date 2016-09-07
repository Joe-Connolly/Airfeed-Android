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
    private ArrayList<Place> mPlaces;
    public LocationListener mLocationListener = new com.google.android.gms.location.LocationListener() {

        @Override
        public void onLocationChanged(Location location) {

//            mLocation = location;
            Log.d(TAG, "location updated, location = " + location + "");
            //add new location to database
            mDatabase.child(Constants.USERS_TABLE_NAME).child(mUserName).setValue(new Latlng(location.getLatitude(), location.getLongitude()));
            mDatabase.child(Constants.PLACES_TABLE_NAME).addListenerForSingleValueEvent(mSingleEventListner);

        }
    };


    ValueEventListener mSingleEventListner = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
//            mLocation = new LatLng(43.705446, -72.288828);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            mLocation = new LatLng(prefs.getFloat("latitude", 0), prefs.getFloat("longitude", 0));
            for (DataSnapshot placeSnapshot : dataSnapshot.getChildren()) {
                Place place = placeSnapshot.getValue(Place.class);
                //If user used to be at a place, but now left, decrement count
                if (userWasAtPlace(place) && !userIsAtPlace(place)) {
                    removePlace(place);
                    //decrement user count in database
                    mHashMap = new HashMap<String, Object>();
                    place.people--;
                    mHashMap.put("people", place.people);
                    mDatabase.child(Constants.PLACES_TABLE_NAME).child(place.location).updateChildren(mHashMap);

                }
                //If user was not at place, but now is, increment count
                if (!userWasAtPlace(place) && userIsAtPlace(place)) {
                    mPlaces.add(place);
                    //increment user count in database
                    mHashMap = new HashMap<String, Object>();
                    place.people++;
                    mHashMap.put("people", place.people);
                    mDatabase.child(Constants.PLACES_TABLE_NAME).child(place.location).updateChildren(mHashMap);


                }

            }
            for (int i = 0; i < mPlaces.size(); i++){
                Log.d(TAG, "place = " + mPlaces.get(i).location);
                Log.d(TAG, mPlaces.get(i).latitude + " - " + mPlaces.get(i).longitude);
            }

        }

            @Override
            public void onCancelled(DatabaseError databaseError){
                Log.w(TAG, "getUser:onCancelled", databaseError.toException());
            }
        };

    private void removePlace(Place place) {
        for (int i = 0; i < mPlaces.size(); i++){
            if (mPlaces.get(i).latitude == place.latitude) {
                mPlaces.remove(i);
            }
        }
    }

    private boolean userWasAtPlace(Place place) {
        for (int i = 0; i < mPlaces.size(); i++){
            if (mPlaces.get(i).latitude == place.latitude) {
                return true;
            }

        }
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("service", "onStartCommand");

        mDatabase = FirebaseDatabase.getInstance().getReference();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mUserName = prefs.getString(Constants.USERNAME_KEY, "someDefaultValue");


        mPlaces = new ArrayList<>();
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



    private boolean userIsAtPlace(Place place){
        float [] distance = new float[1];
        Location.distanceBetween(place.latitude, place.longitude, mLocation.latitude, mLocation.longitude, distance);
        return distance[0] < place.radius; //Math.sqrt(Math.pow(place.latitude - mLocation.latitude, 2) + Math.pow(place.longitude - mLocation.longitude,2)) < place.radius;
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
