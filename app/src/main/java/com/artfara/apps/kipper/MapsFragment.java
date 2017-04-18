package com.artfara.apps.kipper;


import android.*;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;

/**
 * Maps Fragment -- that is, THE Maps Fragment
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback {

    MapView mMapView;
    private GoogleMap mMap;
    private static final String TAG = "Maps Fragment";
    private Handler mHandler;
    private TileOverlay mOverlay;
    private HeatmapTileProvider mHeatMapProvider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

//        Log.d(TAG, "onCreate");
        // inflate the layout
        View v = inflater.inflate(R.layout.fragment_maps, container,
                false);
        mMapView = (MapView) v.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
//        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
//        Log.d(TAG, "onStart");
        //update markers as soon as they become available
        mHandler = new Handler();
        mHandler.postDelayed(mPopulateMapRunnable, 1000);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMapView!=null) {
            mMapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        //If we have permission, enable location
        if (Build.VERSION.SDK_INT <= 22 || ContextCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        //Center map at Hanover
        LatLng location = new LatLng(Constants.HANOVER_LATITUDE, Constants.HANOVER_LONGITUDE);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16.0f));

        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        mMap.setInfoWindowAdapter(new PopUpAdapter(layoutInflater));
    }

    Runnable mPopulateMapRunnable = new Runnable() {
        public void run() {
            //If data has not yet been downloaded, try again later
            if (Globals.globalPlaces == null || Globals.globalUsers == null || mMap == null) {
//                Log.d(TAG, "places or users or map still null");
                mHandler.postDelayed(this, 200);
            } else {
//                Log.d(TAG, "places not null");
                createHeatMap();
                createMarkers();
            }
        }
    };

    private void createHeatMap() {
        mMap.clear();
        if (Globals.globalUsers!=null && Globals.globalUsers.size() > 0) {
            mHeatMapProvider = new HeatmapTileProvider.Builder()
                    .data(Globals.globalUsers)
                    .radius(40)
                    .build();
        }
        // Add a tile overlay to the map, using the heat map tile provider.
        mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mHeatMapProvider));
    }

    private void createMarkers() {
        ArrayList<Place> places = Globals.globalPlaces;
        for (Place place : places) {
            mMap.addMarker((new MarkerOptions().position(new LatLng(place.latitude, place.longitude))
                    .icon(BitmapDescriptorFactory.fromResource(Constants.PLACES.get(place.type)))
                    .title(place.location)
                    .snippet(Utils.getPeopleString(place))));
        }
    }

}