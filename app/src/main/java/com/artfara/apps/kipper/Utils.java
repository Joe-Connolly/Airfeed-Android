package com.artfara.apps.kipper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

/**
 * Created by Van on 10/6/16.
 */

public class Utils {
    private static final String TAG = " Utils";
    public static String getPeopleString(Place place){
        //Inflate the number of people based on how many users have their location provided
        int people = ((Double) (place.people.doubleValue())).intValue();
        String peopleString = "";
        peopleString = (people == 1) ? people + " person" : people + " people";
        return peopleString;
    }

    public static void startAlarmTrackingService(Context context){
        //Start Alarm Manager Tracking Service
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent recurringAlarm = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar updateTime = Calendar.getInstance();
        alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, updateTime.getTimeInMillis(), 4000, recurringAlarm); //you can modify the interval of course
    }

    public static String getUserID(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
//        String userID = prefs.getString(Constants.USER_ID_KEY, null);
        String userID = null;
        if (userID == null){
            userID = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            Log.d(TAG, "android_id = " + userID);
            if (userID == null) {
                userID = FirebaseDatabase.getInstance().getReference().push().getKey();
            }
            prefs.edit().putString(Constants.USER_ID_KEY, userID).apply();
        }
        return userID;
    }
}
