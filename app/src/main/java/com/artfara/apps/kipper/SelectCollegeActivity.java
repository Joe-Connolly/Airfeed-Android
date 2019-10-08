package com.artfara.apps.kipper;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
    private boolean mLocationDialogVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_select_college);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        findCollege();
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
                                findCollege();
                            }
                        })
                .create()
                .show();
        findCollege();
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
            findCollege();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            //permission was granted, yay!
            //Start tracking users location
            findCollege();

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


    public void findCollege() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationAndStartApp();
            return;
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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
                                                        if (minDistance == -1 || distance[0] < minDistance) {
                                                            closestCollege = college;
                                                            minDistance = distance[0];
                                                        }
                                                    }

                                                    closestCollege.databaseRoot = closestCollege.databaseRoot + "/";
                                                    Gson gson = new Gson();
                                                    mPrefs.edit().putString(Constants.COLLEGE_KEY,
                                                            gson.toJson(closestCollege)).apply();
                                                    Log.d(TAG, "college closest " + closestCollege.name);
                                                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                                                    startActivity(intent);
                                                    finish();
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
    }

    public void onBackPressed() {
        // Override to disable
    }
}


