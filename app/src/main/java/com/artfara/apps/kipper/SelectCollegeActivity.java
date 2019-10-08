package com.artfara.apps.kipper;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.artfara.apps.kipper.models.College;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;

public class SelectCollegeActivity extends AppCompatActivity {
    private static final String TAG = "College Activity ";
    private DatabaseReference mDatabase;
    private ArrayList<College> mColleges;
    private ListView mListView;
    private SelectCollegeListViewAdapter mCustomBaseAdapter;
    private Handler mHandler;
    private ProgressDialog mProgressDialog;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLocation;
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setContentView(R.layout.activity_select_college);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationAndStartApp();
        }

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

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







//    private ValueEventListener mCollegesSingleEventListener = new ValueEventListener() {
//        @Override
//        public void onDataChange(DataSnapshot dataSnapshot) {
//            mColleges = new ArrayList<>();
//            for (DataSnapshot collegeSnapshot : dataSnapshot.getChildren()) {
//                College college = collegeSnapshot.getValue(College.class);
//                mColleges.add(college);
////                Log.d(TAG, "college " + college.name);
//            }
//        }
//
//        @Override
//        public void onCancelled(DatabaseError databaseError) {
//            Log.e(TAG, databaseError.getMessage());
//        }
//    };
//
////    @Override
//    public void onResume() {
//        super.onResume();
////        Log.d(TAG, "onResume");
//        //Update colleges as soon as they become available
//        mHandler = new Handler();
//        mHandler.postDelayed(mPopulateListViewRunnable, Constants.MILLISECONDS_BEFORE_POLLING);
//    }
//
//    Runnable mPopulateListViewRunnable = new Runnable() {
//        public void run() {
//            //If data has not yet been downloaded, try again later
//            if (mColleges == null || mColleges.size() == 0) {
////                Log.d(TAG, "colleges still null");
//                mHandler.postDelayed(this, Constants.MILLISECONDS_BETWEEN_POLLING);
//                if (mProgressDialog == null) {
//                    //Display loading spinner
//                    mProgressDialog = new ProgressDialog(SelectCollegeActivity.this);
//                    mProgressDialog.setMessage(getString(R.string.loading_message));
//                    mProgressDialog.show();
//                }
//            }
//            else {
//                if (SelectCollegeActivity.this != null && mProgressDialog != null
//                        && mProgressDialog.isShowing()) {
//                    mProgressDialog.dismiss();
//                    mProgressDialog = null;
//                }
//                mCustomBaseAdapter.setEntries(mColleges);
//            }
//        }
//    };


    public void onBackPressed() {
        // Override to disable
    }
}


