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
import com.google.android.gms.maps.model.LatLng;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSavedInstanceState = savedInstanceState;
        Log.d(TAG, "onCreate");

        Constants.prepare();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        boolean databaseSet = Utils.setDatabaseIfPossible(this);
//        Log.d(TAG, "databaseSet " + databaseSet);
        if (!databaseSet) {
//            Log.d(TAG, "launching select College ");
            Intent intent = new Intent(this, SelectCollegeActivity.class);
            startActivity(intent);
            finish();
            return;
        }
//        Log.d(TAG, "database " + Globals.DATABASE_ROOT_NAME + " " + Globals.ACCOUNTS_TABLE_NAME);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        PostDatabaseHelper.initialize(Utils.getAndroidID(this));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("");
            setSupportActionBar(toolbar);
            TextView title = (TextView) toolbar.findViewById(R.id.toolbar_title);
            Typeface typeFaceBold = Typeface.createFromAsset(getAssets(), "Comfortaa-Bold.ttf");
            title.setTypeface(typeFaceBold);
        }
        initializeApplication();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    private void initializeApplication() {
//        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
//        viewPager.setAdapter(new CustomFragmentPageAdapter(getSupportFragmentManager()));
//        mTabLayout.setupWithViewPager(viewPager);
//        mTabLayout.setOnTabSelectedListener(
//                new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
//                    @Override
//                    public void onTabSelected(TabLayout.Tab tab) {
//                        super.onTabSelected(tab);
//                        int currentTabIndex = tab.getPosition();
//                        mPrefs.edit().putInt(Constants.LAST_TAB_SELECTED_KEY, currentTabIndex).apply();
//                    }
//                });
//        int lastTabSelectedIndex = mPrefs.getInt(Constants.LAST_TAB_SELECTED_KEY, -1);
//        if (lastTabSelectedIndex != -1){
//            TabLayout.Tab selectedTab = mTabLayout.getTabAt(lastTabSelectedIndex);
//            selectedTab.select();
//        }
//        else {
//            TabLayout.Tab feedTab = mTabLayout.getTabAt(1);
//            feedTab.select();
//        }

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
        initializeAccountIfNessicary();
    }

    private void initializeAccountIfNessicary() {
        //If account has not been initialized
        if (!mPrefs.getBoolean(Constants.ACCOUNT_INIATILIZED_KEY, false)) {
            mPrefs.edit().putBoolean(Constants.ACCOUNT_INIATILIZED_KEY, true).apply();
            Log.d(TAG, "initializing account " + Globals.ACCOUNTS_INITIALIZED_TABLE_NAME);
            String androidID =  Utils.getAndroidID(this);
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put(androidID, androidID);
            mDatabase.child(Globals.ACCOUNTS_INITIALIZED_TABLE_NAME).updateChildren(childUpdates);
        }
    }

    public void onPause() {
        super.onPause();
//        Log.d(TAG, "onPause");
    }
    public void onStop(){
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
            switch (position) {
                case 0:
                    return new ChatFragment();
                case 1:
                    return new RankFragment();
                case 2:
                    return new MapsFragment();
                default:
                    return new RankFragment();
            }
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
