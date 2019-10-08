package com.artfara.apps.kipper;

import android.Manifest;
import android.app.AlertDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.*;

import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.artfara.apps.kipper.models.College;
import com.artfara.apps.kipper.models.CustomPlace;
import com.artfara.apps.kipper.models.Latlng;
import com.artfara.apps.kipper.models.Place;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends AppCompatActivity {

    private static final String TAG = "Maps Activity ";
    private DatabaseReference mDatabase;
    private TabLayout mTabLayout;
    private SharedPreferences mPrefs;
    private boolean mLocationDialogVisible;
    private Bundle mSavedInstanceState;
    private FusedLocationProviderClient mFusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSavedInstanceState = savedInstanceState;
        Log.d(TAG, "onCreate");

        Constants.prepare();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Log.d(TAG, "database " + Globals.DATABASE_ROOT_NAME + " " + Globals.ACCOUNTS_TABLE_NAME);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        boolean databaseSet = Utils.setDatabaseIfPossible(this);
//        Log.d(TAG, "databaseSet " + databaseSet);
        if (!databaseSet) {
            Log.d(TAG, "launching select College ");
            Intent intent = new Intent(this, SelectCollegeActivity.class);
            startActivity(intent);
            finish();
            return;
        }
//        if (Build.VERSION.SDK_INT < 22) requestLocationAndStartApp();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        PostDatabaseHelper.initialize(Utils.getAndroidID(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            //permission was granted, yay!
            //Start tracking users location
            initializeApplication();

        } else {
            // permission denied, boo!
            // Tell the user that they are a jackass for disabling the permission
            // must request the permission.
            showDialog();
//            Log.d(TAG, "permission denied");
            //When user presses OK in dialog, onStop() is executed and MapsActivity is restarted, calling onCreate()
        }
        return;
    }

    private void showDialog() {
        mLocationDialogVisible = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.location_explanation_message)
                .setCancelable(false)
                .setPositiveButton(
                        "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mLocationDialogVisible = false;
                                dialog.dismiss();
                                requestLocationAndStartApp();
                            }
                        })
                .create()
                .show();
        initializeApplication();
    }

    public void requestLocationAndStartApp() {
        //Only start app if we have the permissions we need to access location
        if (Build.VERSION.SDK_INT >= 22 && ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //We need to request permission
            if (!mLocationDialogVisible) {
//                Log.d(TAG, "requesting permission");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        0);
            }
        } else {
            //No need to ask for permission
            initializeApplication();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 22) requestLocationAndStartApp();
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (prefs.getString(Constants.ACCEPT_RULES_KEY, null) == null) {
            Intent intent = new Intent(this, RulesActivity.class);
            startActivity(intent);
        }
    }

    private void initializeApplication() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
           requestLocationAndStartApp();
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            mLocation = location;
                            // Logic to handle location object
                            FirebaseDatabase.getInstance().getReference()
                                    .child(Globals.COLLEGES_TABLE_NAME)
                                    .addListenerForSingleValueEvent(
                                            new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    College closestCollege = new College();
                                                    float minDistance = -1;
                                                    for (DataSnapshot collegeSnapshot : dataSnapshot.getChildren()) {
                                                        College college = collegeSnapshot.getValue(College.class);
                                                        float[] distance = new float[3];
                                                        Location.distanceBetween(mLocation.getLatitude(),
                                                                mLocation.getLongitude(),
                                                                college.latitude,
                                                                college.longitude, distance);
                                                        if(minDistance == -1 || distance[0] < minDistance){
                                                            closestCollege = college;
                                                            minDistance = distance[0];
                                                        }
                                                    }

                                                    closestCollege.databaseRoot = closestCollege.databaseRoot + "/";
                                                    Gson gson = new Gson();
                                                    mPrefs.edit().putString(Constants.COLLEGE_KEY,
                                                            gson.toJson(closestCollege)).apply();
                                                    Log.d(TAG, "college closest " + closestCollege.name);
                                                    finish();
                                                    startActivity(getIntent());
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                    Log.e(TAG, databaseError.getMessage());
                                                }
                                            }
                                    );
                        }
                    }
                });

        Intent intent = getIntent();
//        && mSavedInstanceState == null
        String postIDIntentExtra = intent.getStringExtra(Constants.POST_ID_KEY);
        if (postIDIntentExtra != null) {
            PostDatabaseHelper.downloadPosts();
            Intent chatReplyIntent = new Intent(this, ChatReplyListViewActivity.class);
            chatReplyIntent.putExtra(Constants.POST_ID_KEY, postIDIntentExtra);
            intent.removeExtra(Constants.POST_ID_KEY);
            startActivity(chatReplyIntent);
        }

//        scheduleLocationTracking();
        Utils.sendFCMTokenToServer(getApplicationContext()); //Did these two commands cause the
        // not loading problem?
    }

    public void onPause() {
        super.onPause();
//        Log.d(TAG, "onPause");
    }

    public void onStop() {
        super.onStop();
//        Log.d(TAG, "onStop");
    }

    public void hideTabs() {
        if (mTabLayout != null) mTabLayout.setVisibility(TabLayout.GONE);
    }

    public void showTabs() {
        if (mTabLayout != null) mTabLayout.setVisibility(TabLayout.VISIBLE);
    }

    private class CustomFragmentPageAdapter extends FragmentPagerAdapter {
        public CustomFragmentPageAdapter(FragmentManager supportFragmentManager) {
            super(supportFragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            return new ChatFragment();
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "CHAT";
                case 1:
                    return "RANK";
                case 2:
                    return "MAP";
                default:
                    return "FEED";
            }
        }
    }

}
