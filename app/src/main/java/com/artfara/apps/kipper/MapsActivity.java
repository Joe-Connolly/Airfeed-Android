package com.artfara.apps.kipper;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
//change fragment to appcompact

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;
    private DatabaseReference mDatabase;
    private String TAG = " maps class";
    private HeatmapTileProvider mHeatMapProvider;
    private ArrayList<Marker> mMarkers;
    private TileOverlay mOverlay;
    private int mUpdatable;
    private ArrayList<Place> placesGlobal;
    private ScheduledFuture<?> mTask;
    public static double mStudentsWithAppRatio;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Sometimes when activity is stopped, the ratio listener isn't called in time, so it is
        //helpful to load it here from last time
        if (savedInstanceState != null) {
            mStudentsWithAppRatio = savedInstanceState.getDouble(Constants.RATIO_KEY);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Initialize constants hashmap that determines which markers to be used for each place on map
        Constants.prepare();

        //Initialize an arraylist of map markers, to be used later to delete the markers
        mMarkers = new ArrayList<>();

        final ImageView listButtion = (ImageView) findViewById(R.id.listButton);
        listButtion.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ((ImageView) view).setImageDrawable(getDrawable(R.drawable.list_button_darker));
                    Log.d(TAG, "pressed down");
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    ((ImageView) view).setImageDrawable(getDrawable(R.drawable.list_button));
                    Log.d(TAG, "pressed up");
                    Handler handler = new Handler();
                    Runnable r = new Runnable() {
                        public void run() {
                            Intent intent = new Intent(MapsActivity.this, TotalsListViewActivity.class);
                            startActivity(intent);
                        }
                    };
                    handler.postDelayed(r, 1000);

                }
                Log.d(TAG, "neither");
                return true;
            }
        });

    }


    private ValueEventListener mUsersSingleEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            ArrayList<LatLng> users = new ArrayList<>();
            Log.d(TAG, "There are " + dataSnapshot.getChildrenCount() + " users");
            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                //GMAPS heatmap API can only take 1000 locations
                if (users.size() > 997) break;
                 Latlng location = userSnapshot.getValue(Latlng.class);
                 users.add(new LatLng(location.latitude, location.longitude));
//                 Log.d(TAG, "user = " + userSnapshot.getKey() + " " + location.latitude + " - " + location.longitude);
            }
            if (users.size() > 0) {mHeatMapProvider.setData(users);}
            mOverlay.clearTileCache();
        }

        @Override //autogenerated
        public void onCancelled(DatabaseError databaseError) {
            // Getting Post failed, log a message
            Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            // ...
        }
    };

    private ValueEventListener mPlacesSingleEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            removeMarkersIfNecessary();
//            mMap.clear();
//            Log.d(TAG, "clearing map");
            ArrayList<Place> places = new ArrayList<>();
            for (DataSnapshot placeSnapshot : dataSnapshot.getChildren()) {
                Place place = placeSnapshot.getValue(Place.class);
                String placeName = placeSnapshot.getKey();
//                Log.d(TAG, "Before ratio " + placeName + " " + place.people + " ratio" + mStudentsWithAppRatio);
                //Add place to places so it can be accessed from listView
                places.add(place);
//                Log.d(TAG, placeName + " " + place.people + " ratio" + mStudentsWithAppRatio);
                Marker marker = mMap.addMarker((new MarkerOptions().position(new LatLng(place.latitude, place.longitude))
                        .icon(BitmapDescriptorFactory.fromResource(Constants.PLACES.get(place.type)))
                        .title(placeName)
                        .snippet(Utils.getPeopleString(place))));
                mMarkers.add(marker);
            }
            TotalsListViewAdapter.setEntries(places);
            if (TotalsListViewActivity.customBaseAdapter != null){
                TotalsListViewActivity.customBaseAdapter.notifyDataSetChanged();
            }
            PlacesListViewAdapter.setAllEntries(places);
            if (PlacesListViewActivity.customBaseAdapter != null){
                PlacesListViewActivity.customBaseAdapter.notifyDataSetChanged();
            }
        }

        @Override //autogenerated
        public void onCancelled(DatabaseError databaseError) {
            // Getting Post failed, log a message
            Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            // ...
        }
    };

    private ValueEventListener mRatioSingleEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            DBConstants ratioResult = dataSnapshot.getValue(DBConstants.class);
            mStudentsWithAppRatio = ratioResult.ratioValue;
        }

        @Override //autogenerated
        public void onCancelled(DatabaseError databaseError) {
            // Getting Post failed, log a message
            Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            // ...
        }
    };





    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            //permission was granted, yay!
            //Start tracking users location
            initializeMap();


        } else {

            // permission denied, boo!
            // Tell the user that they are a jackass for disabling the permission
            // must request the permission.
            Toast.makeText(this, "Sorry, we need your location", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this,
                     new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                     0);
            //When user presses OK in dialog, onStop() is executed and MapsActivity is restarted, calling onCreate()
        }
        return;
    }


    //Override without calling super to prevent bug from launching need location fragment in async callback
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
        outState.putDouble(Constants.RATIO_KEY, mStudentsWithAppRatio);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Check permissions for Android API 22 and up to use location
        if (Build.VERSION.SDK_INT >= 22 && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // must request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    0);

            // onRequestPermissionsResult() gets the
            // result of the request.

        }

        else {
            //No need to request permissions, start using location
            initializeMap();
        }





    }

    public void initializeMap(){
        //Launch location tracking service
        Intent intent = new Intent(this, TrackingService.class);
        startService(intent);

        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setMyLocationEnabled(true);
        //Center map at Hanover
        LatLng location = new LatLng(Constants.HANOVER_LATITUDE, Constants.HANOVER_LONGITUDE);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16.0f));
        //For now just initialize heatmap with hanover as a latlng
        ArrayList<LatLng> data = new ArrayList<>();
        data.add(location);
        mHeatMapProvider = new HeatmapTileProvider.Builder()
                .data(data)
                .build();
        // Add a tile overlay to the map, using the heat map tile provider.
        mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mHeatMapProvider));
        //connect to firebase database and add markers for places and set data for heatmap
//        populateMap();


        //For testing, save clicks
//        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
//        {
//            @Override
//            public void onMapClick(LatLng loc)
//            {
//                //For testing
//                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                prefs.edit().putFloat("latitude", (float) loc.latitude).commit();
//                prefs.edit().putFloat("longitude", (float) loc.longitude).commit();
//            }
//        });


        mMap.setInfoWindowAdapter(new PopUpAdapter(getLayoutInflater()));
        mMap.setOnInfoWindowClickListener(this);

        //To keep app from freezing, make sure it only updates itself every 10 seconds
//        ScheduledExecutorService scheduler =
//                Executors.newSingleThreadScheduledExecutor();
//        mTask = scheduler.scheduleAtFixedRate
//                (new Runnable() {
//                    public void run() {
//                      mUpdatable++;
////                    Log.d(TAG, mUpdatable + "");
//                    }
//                }, 6, 1, TimeUnit.SECONDS);
        populateMap();
    }




    public void onResume(){
        Log.d(TAG, "onResume");
        if (mDatabase != null){
            updateMarkers();
            updateHeatMap();
        }
        super.onResume();
    }

    public void onPause(){
        Log.d(TAG, "onPause");

        super.onPause();
    }
    //Always unregister receivers and unbind service when app is closed out of
    public void onStop(){
        Log.d(TAG, "onStop");
        super.onStop();

    }

    public void onDestroy(){
        Log.d(TAG, "onDestroy");
        mTask.cancel(true);
        super.onDestroy();
    }




    //connect to firebase database and add markers for all connected users
    private void populateMap() {

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(Constants.CONSTANTS_TABLE_NAME).addValueEventListener(mRatioSingleEventListener);

        ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor();
        mTask = scheduler.scheduleAtFixedRate
                (new Runnable() {
                    public void run() {
                        Log.d(TAG, "Thread Pool Executer executed");
                        updateMarkers();

                    }
                }, 1, 5, TimeUnit.SECONDS);
        updateHeatMap();

    }


    public void onKipperIconClicked(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/Kipper-262599437466908/?fref=ts&__mref=message_bubble"));
        startActivity(browserIntent);

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(Constants.PLACE_TITLE_KEY, marker.getTitle());
        startActivity(intent);
    }

    private void removeMarkersIfNecessary() {
        if (mMarkers != null && mMarkers.size() > 0){
            for (int i = 0; i < mMarkers.size(); i++) {
                mMarkers.get(i).remove();
            }
            mMarkers = new ArrayList<>();
        }
        else{
            Log.d(TAG, "there are no markers");
        }
    }

    public void updateHeatMap(){
        mDatabase.child(Constants.USERS_TABLE_NAME).addListenerForSingleValueEvent(mUsersSingleEventListener);

    }
    public void updateMarkers(){
        mDatabase.child(Constants.PLACES_TABLE_NAME).addListenerForSingleValueEvent(mPlacesSingleEventListener);
    }



}
