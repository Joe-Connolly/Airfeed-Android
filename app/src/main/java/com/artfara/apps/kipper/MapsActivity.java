package com.artfara.apps.kipper;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;

    private DatabaseReference mDatabase;
    private String TAG = " maps class";
    private HeatmapTileProvider mHeatMapProvider;
    private ArrayList<Marker> mMarkers;
    private TileOverlay mOverlay;
    private boolean mPermissionGranted;
    private ValueEventListener mUserDatabaseChangedListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            ArrayList<LatLng> users = new ArrayList<>();
            Log.d(TAG, "There are " + dataSnapshot.getChildrenCount() + " users");
            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                 Latlng location = userSnapshot.getValue(Latlng.class);
                 users.add(new LatLng(location.latitude, location.longitude));
//                 mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)));

                 Log.d(TAG, location.latitude + " - " + location.longitude);
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
    private ValueEventListener mPlaceDatabaseChangedListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            removeMarkersIfNecessary();
            for (DataSnapshot placeSnapshot : dataSnapshot.getChildren()) {
                Place place = placeSnapshot.getValue(Place.class);
                String placeName = placeSnapshot.getKey();
                //set the snippet to be people or person depending on if there are 1 or more people
                String snippet = (place.people == 1) ? place.people + " person" : place.people + " people";
                Marker marker = mMap.addMarker((new MarkerOptions().position(new LatLng(place.latitude, place.longitude))
                        .icon(BitmapDescriptorFactory.fromResource(Constants.PLACES.get(place.type)))
                        .title(placeName)
                        .snippet(snippet)));
                mMarkers.add(marker);
            }
        }

        @Override //autogenerated
        public void onCancelled(DatabaseError databaseError) {
            // Getting Post failed, log a message
            Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            // ...
        }
    };


    private void removeMarkersIfNecessary() {
        if (mMarkers != null && mMarkers.size() > 0){
            for (int i = 0; i < mMarkers.size(); i++) {
                mMarkers.get(i).remove();
            }
            mMarkers = new ArrayList<>();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Fill constants hashmap that determines which markers to be used for each place on map
        Constants.prepare();

        //Initialize an arraylist of map markers, to be used later to delete the markers
        mMarkers = new ArrayList<>();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            // permission was granted, yay!
            //Start tracking users location
            initializeMap();
            mPermissionGranted = true;


        } else {

            // permission denied, boo!
            //Tell the user that they are a jackass for disabling the permission
//            NeedLocationDialogFragment dialog = new NeedLocationDialogFragment();
//            dialog.show(getSupportFragmentManager(), TAG);
            // must request the permission.
            Toast.makeText(this, "Sorry, we need your location", Toast.LENGTH_LONG).show();
//            Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    ActivityCompat.requestPermissions(getCallingActivity(),
//                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                            0);
//                }
//            }, 2000);
             ActivityCompat.requestPermissions(this,
                     new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                     0);
            mPermissionGranted = false;
            //When user presses OK in dialog, onStop() is executed and MapsActivity is restarted, calling onCreate()
        }
        return;
    }


    //Override without calling super to prevent bug from launching need location fragment in async callback
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
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
            mPermissionGranted = true;
            initializeMap();
        }





    }

    public void initializeMap(){
        //Launch location tracking service
        Intent intent = new Intent(this, TrackingService.class);
        startService(intent);

        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.setMyLocationEnabled(true);
//            LatLng location = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        //For testing
        LatLng location = new LatLng(Constants.HANOVER_LATITUDE, Constants.HANOVER_LONGITUDE);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16.0f));
        //For now just initialize heatmap with current location
        ArrayList<LatLng> data = new ArrayList<>();
        data.add(location);
        //Initialize heatmap to be populated later
        mHeatMapProvider = new HeatmapTileProvider.Builder()
                .data(data)
                .build();
        // Add a tile overlay to the map, using the heat map tile provider.
        mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mHeatMapProvider));
        //connect to firebase database and add markers for places and set data for heatmap
        populateMap();


        //For testing, save clicks
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng loc)
            {
                //For testing
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                prefs.edit().putFloat("latitude", (float) loc.latitude).commit();
                prefs.edit().putFloat("longitude", (float) loc.longitude).commit();
            }
        });

        mMap.setInfoWindowAdapter(new PopUpAdapter(getLayoutInflater()));
    }

    //Always unregister receivers and unbind service when app is closed out of
    public void onStop(){
        super.onStop();
    }

    //connect to firebase database and add markers for all connected users
    private void populateMap() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(Constants.USERS_TABLE_NAME).addValueEventListener(mUserDatabaseChangedListener);
        mDatabase.child(Constants.PLACES_TABLE_NAME).addValueEventListener(mPlaceDatabaseChangedListener);

    }


    public void onKipperIconClicked(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/jose.conrador.1"));
        startActivity(browserIntent);

    }
}
