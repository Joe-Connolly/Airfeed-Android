package com.artfara.apps.kipper;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.*;

import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends AppCompatActivity {

    private static final String TAG = "Maps Activity ";
    private ScheduledFuture<?> mQueryUsersTask;
    private ScheduledFuture<?> mQueryPlacesTask;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG,"onCreate");

//        MapsFragment mapsFragment = new MapsFragment();
//        // Update the layout
//        getSupportFragmentManager().beginTransaction().add(R.id.view_group, mapsFragment).commit();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);


        viewPager.setAdapter(new CustomFragmentPageAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);

        mDatabase = FirebaseDatabase.getInstance().getReference();






        ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor();
//        mQueryUsersTask = scheduler.scheduleAtFixedRate
//                (new Runnable() {
//                    public void run() {
//
//                    }
//                }, 3, 1, TimeUnit.SECONDS);
        mQueryPlacesTask = scheduler.scheduleAtFixedRate
                (new Runnable() {
                    public void run() {

                        mDatabase.child(Constants.PLACES_TABLE_NAME).addListenerForSingleValueEvent(mPlacesSingleEventListener);
                    }
                }, 3, 1, TimeUnit.SECONDS);

    }




    private ValueEventListener mPlacesSingleEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            ArrayList<Place> places = new ArrayList<>();
            for (DataSnapshot placeSnapshot : dataSnapshot.getChildren()) {
                Place place = placeSnapshot.getValue(Place.class);
                String placeName = placeSnapshot.getKey();
//                Log.d(TAG, "place = " + placeName + " " + place.people);
                //Add place to places so it can be accessed from listView
                places.add(place);
            }
            Globals.globalPlaces = places;
//            TotalsListViewAdapter.setEntries(places);
//            if (TotalsListViewActivity.customBaseAdapter != null){
//                TotalsListViewActivity.customBaseAdapter.notifyDataSetChanged();
//            }
//            PlacesListViewAdapter.setAllEntries(places);
//            if (PlacesListViewActivity.customBaseAdapter != null){
//                PlacesListViewActivity.customBaseAdapter.notifyDataSetChanged();
//            }
        }

        @Override //autogenerated
        public void onCancelled(DatabaseError databaseError) {
            // Getting Post failed, log a message
            Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            // ...
        }
    };



    public void onStop(){
        super.onStop();
        Log.d(TAG, "onStop");
    }


    private class CustomFragmentPageAdapter extends FragmentPagerAdapter {
        public CustomFragmentPageAdapter(FragmentManager supportFragmentManager) {
            super(supportFragmentManager);
        }
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new MapsFragment();
                case 1:
                default:
                    return new TotalsFragment();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "HEATMAP";
                case 1:
                default:
                    return "PLACES";
            }
        }
    }

}
