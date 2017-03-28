package com.artfara.apps.kipper;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by Joe Connolly on 12/13/16.
 */
public class PostDatabaseHelper {

    private static final String TAG = " DatabaseHelper ";
    private static HashMap<String, Post> mGlobalPosts;
    private static HashMap<String, Post> hotPosts;
    private static HashMap<String, Post> newPosts;
    private static DatabaseReference mDatabase;
    private static String mPostType;

    public static HashMap<String, Post>  getPosts(){
        return mGlobalPosts;
    }

    public static  HashMap<String, Post> getReplies(String postID){
        return mGlobalPosts.get(postID).replies;
    }

    public static void setUserLetters(ArrayList<Post> posts) {
        HashMap<String, String> lettersAssigned = new HashMap<>();
        String userLetter;
        for (Post post:posts) {
            //Iterate through all posts to be displayed in scrollview
            userLetter = lettersAssigned.get(post.userID);
            if (userLetter == null) {
                //The post has not already been assigned a letter, so assign the next available letter
                //Put it in HashMap so that we can account that that letter has been assigned to that user
                userLetter = String.valueOf(Constants.LETTERS.charAt(lettersAssigned.size()));
                lettersAssigned.put(post.userID, userLetter);
            }
            //Assign the post a userLetter (Later we will display the userLetter as a TextView for each row in scrollview
            post.userLetter = userLetter;
        }
    }

    public static void addPost(String postBody, Context context){

        long time = System.currentTimeMillis();
        String userID = Utils.getUserID(context.getApplicationContext());
        Post post = new Post(userID, postBody, time);

        DatabaseReference postDatabase =  FirebaseDatabase.getInstance().getReference().child(Constants.POSTS_NEW_TABLE_NAME);
        post.ID = postDatabase.push().getKey();
        newPosts.put(post.ID, post);
        postDatabase.child(post.ID).setValue(post);
    }

    public static void addReply(String postBody, String parentPostID, Context context){

        long time = System.currentTimeMillis();
        String userID = Utils.getUserID(context.getApplicationContext());
        Post post = new Post(userID, postBody, time);
        String replyId = mDatabase.push().getKey();
        post.ID = replyId;


        if (newPosts.get(parentPostID) != null){
            DatabaseReference repliesDatabase =  FirebaseDatabase.getInstance().getReference().child(Constants.POSTS_NEW_TABLE_NAME)
                    .child(parentPostID).child(Constants.REPLIES_TABLE_NAME);
            HashMap<String, Post> replies = newPosts.get(parentPostID).replies;
            if (replies == null) {
                replies = new HashMap<>();
            }
            replies.put(replyId, post);
            repliesDatabase.child(replyId).setValue(post);
        }
        if (hotPosts.get(parentPostID) != null){
            DatabaseReference repliesDatabase =  FirebaseDatabase.getInstance().getReference().child(Constants.POSTS_HOT_TABLE_NAME)
                    .child(parentPostID).child(Constants.REPLIES_TABLE_NAME);
            HashMap<String, Post> replies = hotPosts.get(parentPostID).replies;
            if (replies == null) {
                replies = new HashMap<>();
            }
            replies.put(replyId, post);
            repliesDatabase.child(replyId).setValue(post);
        }
    }

    public static void incrementPost(Post post){
        if (newPosts.get(post.ID) != null){
            newPosts.get(post.ID).voteCount++;
            Log.d(TAG, " vote " + newPosts.get(post.ID).voteCount);
            mDatabase.child(Constants.POSTS_NEW_TABLE_NAME).child(post.ID).runTransaction(mUpVoteHandler);
        }
        if (hotPosts.get(post.ID) != null){
            hotPosts.get(post.ID).voteCount++;
            Log.d(TAG, " vote " + newPosts.get(post.ID).voteCount);
            mDatabase.child(Constants.POSTS_HOT_TABLE_NAME).child(post.ID).runTransaction(mUpVoteHandler);
        }

    }

    public static void decrementPost(Post post){
        if (newPosts.get(post.ID) != null){
            newPosts.get(post.ID).voteCount--;
            Log.d(TAG, " vote " + newPosts.get(post.ID).voteCount);
            mDatabase.child(Constants.POSTS_NEW_TABLE_NAME).child(post.ID).runTransaction(mDownVoteHandler);

        }
        if (hotPosts.get(post.ID) != null){
            hotPosts.get(post.ID).voteCount--;
            Log.d(TAG, " vote " + hotPosts.get(post.ID).voteCount);
            mDatabase.child(Constants.POSTS_HOT_TABLE_NAME).child(post.ID).runTransaction(mDownVoteHandler);
        }
      }

    public static void incrementReply(Post post, String parentPostID){
        if (newPosts.get(parentPostID) != null){
            newPosts.get(parentPostID).replies.get(post.ID).voteCount++;
            Log.d(TAG, " vote " + newPosts.get(parentPostID).replies.get(post.ID).voteCount);
            mDatabase.child(Constants.POSTS_NEW_TABLE_NAME).child(parentPostID).child(Constants.REPLIES_TABLE_NAME).child(post.ID).runTransaction(mUpVoteHandler);

        }
        if (hotPosts.get(parentPostID) != null){
            hotPosts.get(parentPostID).replies.get(post.ID).voteCount++;
            Log.d(TAG, " vote " + hotPosts.get(parentPostID).replies.get(post.ID).voteCount);
            mDatabase.child(Constants.POSTS_HOT_TABLE_NAME).child(parentPostID).child(Constants.REPLIES_TABLE_NAME).child(post.ID).runTransaction(mUpVoteHandler);

        }
    }
    public static void decrementReply(Post post, String parentPostID){
        if (newPosts.get(parentPostID) != null){
            newPosts.get(parentPostID).replies.get(post.ID).voteCount--;
            Log.d(TAG, " vote " + newPosts.get(parentPostID).replies.get(post.ID).voteCount);
            mDatabase.child(Constants.POSTS_NEW_TABLE_NAME).child(parentPostID).child(Constants.REPLIES_TABLE_NAME).child(post.ID).runTransaction(mDownVoteHandler);
        }
        if (hotPosts.get(parentPostID) != null){
            hotPosts.get(parentPostID).replies.get(post.ID).voteCount--;
            Log.d(TAG, " vote " + hotPosts.get(parentPostID).replies.get(post.ID).voteCount);
            mDatabase.child(Constants.POSTS_HOT_TABLE_NAME).child(parentPostID).child(Constants.REPLIES_TABLE_NAME).child(post.ID).runTransaction(mDownVoteHandler);
        }
    }


    public void downloadPosts(){
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(Constants.POSTS_NEW_TABLE_NAME).addListenerForSingleValueEvent(mNewPostsSingleEventListener);
        mDatabase.child(Constants.POSTS_HOT_TABLE_NAME).addListenerForSingleValueEvent(mHotPostsSingleEventListener);
    }

    private ValueEventListener mNewPostsSingleEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            HashMap<String, Post> posts = new HashMap<>();
            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                Post post = postSnapshot.getValue(Post.class);
                if (post.replies == null){
                    post.replies = new HashMap<>();
                }

                for (String replyID : post.replies.keySet()){
                    post.replies.get(replyID).ID = replyID;
                }
                posts.put(postSnapshot.getKey(), post);
            }
            newPosts = posts;
            mGlobalPosts = posts;
            mPostType = Constants.POSTS_TYPE_NEW;
            Log.d(TAG, " Downloading Posts");
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };
    private ValueEventListener mHotPostsSingleEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            HashMap<String, Post> posts = new HashMap<>();
            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                Post post = postSnapshot.getValue(Post.class);
                if (post.replies == null){
                    post.replies = new HashMap<>();
                }

                for (String replyID : post.replies.keySet()){
                    post.replies.get(replyID).ID = replyID;
                }

                posts.put(postSnapshot.getKey(), post);
            }
            hotPosts = posts;
            Log.d(TAG, " Downloading Posts");
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };


    private static com.google.firebase.database.Transaction.Handler mUpVoteHandler =
            new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    Post post = mutableData.getValue(Post.class);
                    if (post == null){
                        return Transaction.success(mutableData);
                    }
                    post.voteCount++;
                    Log.d(TAG, " voteCount " + post.voteCount);
                    mutableData.setValue(post);
                    return Transaction.success(mutableData);
                }
                @Override
                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {}
            };
    private static com.google.firebase.database.Transaction.Handler mDownVoteHandler =
            new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    Post post = mutableData.getValue(Post.class);
                    if (post == null){
                        return Transaction.success(mutableData);
                    }
                    post.voteCount--;
                    Log.d(TAG, " voteCount " + post.voteCount);
                    mutableData.setValue(post);
                    return Transaction.success(mutableData);
                }
                @Override
                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {}
            };

    public static void sortDescending(ArrayList<Post> posts) {
        Collections.sort(posts, new Comparator<Post>() {
            @Override
            public int compare(Post post1, Post post2) {
                return ((Long) post2.timeInMilliseconds).compareTo(post1.timeInMilliseconds);
            }
        });
    }   public static void sortAscending(ArrayList<Post> posts) {
        Collections.sort(posts, new Comparator<Post>() {
            @Override
            public int compare(Post post1, Post post2) {
                return ((Long) post1.timeInMilliseconds).compareTo(post2.timeInMilliseconds);
            }
        });
    }

    public static void showPosts(String postType){
        if (postType.equals(Constants.POSTS_TYPE_HOT) && mPostType.equals(Constants.POSTS_TYPE_NEW)){
            Log.d(TAG, "show HOT");
            mGlobalPosts = hotPosts;
            mPostType = Constants.POSTS_TYPE_HOT;
        }
        else if (postType.equals(Constants.POSTS_TYPE_NEW) && mPostType.equals(Constants.POSTS_TYPE_HOT)){
            mGlobalPosts = newPosts;
            mPostType = Constants.POSTS_TYPE_NEW;
        }
    }


    public static void formatTime(ArrayList<Post> posts) {
        Calendar calendar;
        for (Post post:posts){
            post.displayedTime = DateUtils.getRelativeTimeSpanString(post.timeInMilliseconds,
                    System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString();
        }
    }
}
