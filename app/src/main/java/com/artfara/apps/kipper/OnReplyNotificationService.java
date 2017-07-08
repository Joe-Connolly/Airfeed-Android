package com.artfara.apps.kipper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class OnReplyNotificationService extends Service {
    private static final String TAG = "NotificationService ";
    private HashMap<String, Long> alreadyFollowedPosts;

    public OnReplyNotificationService() {
    }

    @Override
    public void onCreate() {
//        Log.d(TAG, "onCreate");
        alreadyFollowedPosts = new HashMap<>();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.d(TAG, "onStart");
        //check whether intent is null
        if (intent != null) {
            String postID = intent.getStringExtra(Constants.POST_ID_KEY);
            if (!alreadyFollowedPosts.values().contains(postID)) {
                Long currentTime = System.currentTimeMillis();
                DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                database.child(Constants.POSTS_TABLE_NAME).child(postID)
                        .child(Constants.REPLIES_TABLE_NAME)
                        .limitToLast(1)
                        .addChildEventListener(mOnReplyAddedSingleEventListener);
                alreadyFollowedPosts.put(postID, currentTime);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private ChildEventListener mOnReplyAddedSingleEventListener = new ChildEventListener() {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Post newReply = dataSnapshot.getValue(Post.class);
//            Log.d(TAG, "text = " + newReply.text);
            if (!newReply.userID.equals(Utils.getAndroidID(getApplicationContext())) &&
                    newReply.timeInMilliseconds > alreadyFollowedPosts.get(newReply.parentPostID) ) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class); // set notification activity
                intent.putExtra(Constants.POST_ID_KEY, newReply.parentPostID);
                intent.putExtra(Constants.ACTION_LAUNCH_REPLIES, true);
                PendingIntent pIntent = PendingIntent.getActivity(
                        getApplicationContext(),
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                Notification notif = new Notification.Builder(getApplicationContext())
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText("Someone replied to your post")
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentIntent(pIntent)
                        .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                        .setAutoCancel(true)
                        .build();
                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                //firstParam allows you to update the notification later on.
                notificationManager.notify(1, notif);
            }

        }

        //Required Methods
        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override //autogenerated
        public void onCancelled(DatabaseError databaseError) {
            // Getting Post failed, log a message
            Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            // ...
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


}
