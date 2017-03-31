package com.artfara.apps.kipper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

/**
 * Created by Van on 10/6/16.
 */

public class Utils {

    public static String getPeopleString(Place place){
        //Inflate the number of people based on how many users have their location provided
        int people = ((Double) (place.people.doubleValue())).intValue();
        String peopleString = "";
//        Log.d("utils",  place.location + " " + place.state);
//        switch (place.state){
//            case (Constants.STATE.NORMAL):
//                 //set the snippet to be people or person depending on if there are 1 or more people
//                    peopleString = (people == 1) ? people + " person" : people + " people";
//                break;
//
//            case (Constants.STATE.LIT):
//                        peopleString = people + " Lit!";
//                break;
//
//            case (Constants.STATE.OVER_CAPACITY):
//                peopleString = "OVERCAPACITY";
//                break;
//        }

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

    public static void updatePost(Post currentPost, String mParentPostId) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference postRef = database.getReference().child(Constants.POSTS_TABLE_NAME).child(currentPost.ID);
        postRef.child("voteCount").setValue(currentPost.voteCount);
    }


    public static String getUserID(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String userID = prefs.getString(Constants.USER_ID_KEY, null);
        if (userID == null){
            userID = FirebaseDatabase.getInstance().getReference().push().getKey();
            prefs.edit().putString(Constants.USER_ID_KEY, userID).apply();
        }
        return userID;
    }
}
