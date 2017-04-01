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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Joe Connolly on 12/13/16.
 */
public class PostDatabaseHelper {

    private static final String TAG = " DatabaseHelper ";
    private static HashMap<String, Post> mGlobalPosts;
    private static DatabaseReference mDatabase;
    private static String mPostType;
    private static ArrayBlockingQueue<Post> mAddReplyQueue;


    public PostDatabaseHelper(){
        mAddReplyQueue = new ArrayBlockingQueue<>(100);
    }


     public static ArrayList<Post>  getPosts(){
         if (mGlobalPosts == null) return null;
        ArrayList<Post> posts = new ArrayList<>(mGlobalPosts.values());
        if (mPostType.equals(Constants.POSTS_TYPE_NEW)){
            sortDescendingByTime(posts);
        }
        else{
            sortDescendingByVoteCount(posts);
        }
        formatTime(posts);
        return posts;
    }


    public static  ArrayList<Post> getReplies(String postID){
        ArrayList<Post> replies = new ArrayList<>(mGlobalPosts.get(postID).replies.values());
        //Grab entries
//        ArrayList<Post> entries;
        Log.d(TAG, "PostKey = " + postID);
//        HashMap<String, Post> replies = Globals.globalPosts.get(mPostId).replies;
//        if (replies != null){
//            entries = (new ArrayList<>(Globals.globalPosts.get(mPostId).replies.values()));
//        }
//        else{
//            entries = new ArrayList<>();
//        }

        Log.d(TAG, "Adding entry first");
        //Add origional post to front of entries
        replies.add(0, mGlobalPosts.get(postID));
        formatTime(replies);
        setUserLetters(replies);
        sortDescendingByTime(replies);
        return replies;
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

        DatabaseReference postDatabase =  mDatabase.child(Constants.POSTS_TABLE_NAME);
        post.ID = postDatabase.push().getKey();
        mGlobalPosts.put(post.ID, post);
        postDatabase.child(post.ID).setValue(post);
    }

    public static void addReply(String postBody, String parentPostID, Context context){

        long time = System.currentTimeMillis();
        String userID = Utils.getUserID(context.getApplicationContext());
        Post post = new Post(userID, postBody, time);

        DatabaseReference repliesDatabase =  mDatabase.child(Constants.POSTS_TABLE_NAME)
                .child(parentPostID).child(Constants.REPLIES_TABLE_NAME);

        String replyId = repliesDatabase.push().getKey();
        post.ID = replyId;
        HashMap<String, Post> replies = mGlobalPosts.get(parentPostID).replies;
        if (replies == null) {
            //Does this make replies point to a new hashmap?
            replies = new HashMap<>();
        }
        replies.put(replyId, post);
//        repliesDatabase.child(replyId).updateChildren(post.toMap());
        mAddReplyQueue.add(post);
        mDatabase.child(Constants.POSTS_TABLE_NAME)
                .child(parentPostID).runTransaction(mAddReplyHandler);
    }


    public static void incrementPost(Post post){
        mGlobalPosts.get(post.ID).voteCount++;
        Log.d(TAG, " vote " + mGlobalPosts.get(post.ID).voteCount);
        mDatabase.child(Constants.POSTS_TABLE_NAME).child(post.ID).runTransaction(mUpVoteHandler);
    }

    public static void decrementPost(Post post){
        mGlobalPosts.get(post.ID).voteCount--;
        Log.d(TAG, " vote " + mGlobalPosts.get(post.ID).voteCount);
        mDatabase.child(Constants.POSTS_TABLE_NAME).child(post.ID).runTransaction(mDownVoteHandler);
    }

    public static void incrementReply(Post post, String parentPostID){
        mGlobalPosts.get(parentPostID).replies.get(post.ID).voteCount++;
        Log.d(TAG, " vote " + mGlobalPosts.get(parentPostID).replies.get(post.ID).voteCount);
        mDatabase.child(Constants.POSTS_TABLE_NAME).child(parentPostID).child("replies").child(post.ID).runTransaction(mUpVoteHandler);
    }
    public static void decrementReply(Post post, String parentPostID){
        mGlobalPosts.get(parentPostID).replies.get(post.ID).voteCount--;
        Log.d(TAG, " vote " + mGlobalPosts.get(parentPostID).replies.get(post.ID).voteCount);
        mDatabase.child(Constants.POSTS_TABLE_NAME).child(parentPostID).child("replies").child(post.ID).runTransaction(mDownVoteHandler);
    }


    public void downloadPosts(){
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(Constants.POSTS_TABLE_NAME).addListenerForSingleValueEvent(mPostsSingleEventListener);
        mPostType = Constants.POSTS_TYPE_NEW;
    }
    private ValueEventListener mPostsSingleEventListener = new ValueEventListener() {
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
            mGlobalPosts = posts;
            Log.d(TAG, " Downloading Posts");
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

//    private ValueEventListener mAddReplySingleEventListener = new ValueEventListener() {
//        @Override
//        public void onDataChange(DataSnapshot dataSnapshot) {
//            HashMap<String, Post> posts = new HashMap<>();
//            replyID =
//            if posts.get()
//        }
//
//        @Override
//        public void onCancelled(DatabaseError databaseError) {
//        }
//    };


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

    private static com.google.firebase.database.Transaction.Handler mAddReplyHandler =
            new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    Post post = mutableData.getValue(Post.class);
                    if (post == null) {
                        return Transaction.success(mutableData);
                    }
                    Post reply = mAddReplyQueue.poll();
                    if (post.replies == null) {
                        post.replies = new HashMap<>();
                    }
                    post.replies.put(reply.ID, reply);
                    mutableData.setValue((post.ID != null ? post : null));
                    return Transaction.success(mutableData);
                }
                @Override
                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {}
            };

    public static void sortDescendingByTime(ArrayList<Post> posts) {
        Collections.sort(posts, new Comparator<Post>() {
            @Override
            public int compare(Post post1, Post post2) {
                return ((Long) post2.timeInMilliseconds).compareTo(post1.timeInMilliseconds);
            }
        });
    }   public static void sortAscendingByTime(ArrayList<Post> posts) {
        Collections.sort(posts, new Comparator<Post>() {
            @Override
            public int compare(Post post1, Post post2) {
                return ((Long) post1.timeInMilliseconds).compareTo(post2.timeInMilliseconds);
            }
        });
    } public static void sortDescendingByVoteCount(ArrayList<Post> posts) {
        Collections.sort(posts, new Comparator<Post>() {
            @Override
            public int compare(Post post1, Post post2) {
                return (new Integer(post2.voteCount)).compareTo(post1.voteCount);
            }
        });
    }


    public static void showPosts(String postType){
        if (postType.equals(Constants.POSTS_TYPE_HOT) && mPostType.equals(Constants.POSTS_TYPE_NEW)){
            Log.d(TAG, "show HOT");
            mPostType = Constants.POSTS_TYPE_HOT;
        }
        else if (postType.equals(Constants.POSTS_TYPE_NEW) && mPostType.equals(Constants.POSTS_TYPE_HOT)){
            Log.d(TAG, "show NEW");
            mPostType = Constants.POSTS_TYPE_NEW;
        }
    }


    public static void formatTime(ArrayList<Post> posts) {
        for (Post post:posts){
            post.displayedTime = DateUtils.getRelativeTimeSpanString(post.timeInMilliseconds,
                    System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString();
        }
    }
}
