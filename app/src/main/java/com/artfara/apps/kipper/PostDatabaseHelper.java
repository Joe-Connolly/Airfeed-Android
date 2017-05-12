package com.artfara.apps.kipper;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;

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
    private static DatabaseReference mPostsRef;
    private static String mPostType;
    private static ArrayBlockingQueue<Post> mAddReplyQueue;
    public static boolean mFinishedDownloading;
    private static long mTimeLastRefreshed;


    public static void initialize() {
        mPostsRef = FirebaseDatabase.getInstance().getReference().child(Constants.POSTS_TABLE_NAME);
        mAddReplyQueue = new ArrayBlockingQueue<>(100);
        mGlobalPosts = new HashMap<>();
        mFinishedDownloading = false;
    }

    public static void setPostType(String postType) {
        mPostType = postType;
    }

    public static boolean contains(String postID){
        return mGlobalPosts.get(postID) != null;
    }
    public static ArrayList<Post> getPosts() {
        if (mGlobalPosts == null) return null;
        ArrayList<Post> posts = new ArrayList<>(mGlobalPosts.values());
        if (mPostType.equals(Constants.POSTS_TYPE_NEW)) {
            sortDescendingByTime(posts);
        } else {
            sortDescendingByVoteCount(posts);
        }
        formatTime(posts);
        return posts;
    }

    public static ArrayList<Post> getReplies(String postID) {
        ArrayList<Post> replies = new ArrayList<>(mGlobalPosts.get(postID).replies.values());
        sortAscendingByTime(replies);
        //Add original post to front of entries
        replies.add(0, mGlobalPosts.get(postID));
        formatTime(replies);
        setUserLetters(replies);
        return replies;
    }

    public static void setUserLetters(ArrayList<Post> posts) {
        HashMap<String, String> lettersAssigned = new HashMap<>();
        String userLetter;
        for (Post post : posts) {
            //Iterate through all posts to be displayed in scrollview
            userLetter = lettersAssigned.get(post.userID);
            if (userLetter == null) {
                //The post has not already been assigned a letter, so assign the next available letter
                //Put it in HashMap so that we can account that that letter has been assigned to that user
                userLetter = String.valueOf(Constants.LETTERS[lettersAssigned.size()]);
                lettersAssigned.put(post.userID, userLetter);
            }
            //Assign the post a userLetter (Later we will display the userLetter as a TextView for each row in scrollview
            post.userLetter = userLetter;
        }
    }

    public static void addPost(String postBody, Context context) {

        long time = System.currentTimeMillis();
        String userID = Utils.getAndroidID(context.getApplicationContext());
        Post post = new Post(userID, postBody, time);

        post.ID = mPostsRef.push().getKey();
        mGlobalPosts.put(post.ID, post);
        mPostsRef.child(post.ID).setValue(post);

        Intent intent = new Intent(context, OnReplyNotificationService.class);
        intent.putExtra(Constants.POST_ID_KEY, post.ID);
        context.startService(intent);
    }

    public static void addReply(String postBody, String parentPostID, Context context) {

        long time = System.currentTimeMillis();
        String userID = Utils.getAndroidID(context.getApplicationContext());
        Post post = new Post(userID, postBody, time);

        DatabaseReference repliesDatabase = mPostsRef
                .child(parentPostID).child(Constants.REPLIES_TABLE_NAME);

        String replyId = repliesDatabase.push().getKey();
        post.ID = replyId;
        post.parentPostID = parentPostID;
        HashMap<String, Post> replies = mGlobalPosts.get(parentPostID).replies;
        if (replies == null) replies = new HashMap<>();
        replies.put(replyId, post);
        mAddReplyQueue.add(post);
        mPostsRef.child(parentPostID).addListenerForSingleValueEvent(mAddReplySingleEventListener);

        Intent intent = new Intent(context, OnReplyNotificationService.class);
        intent.putExtra(Constants.POST_ID_KEY, parentPostID);
        context.startService(intent);
    }


    public static void incrementPost(Post post) {
        mGlobalPosts.get(post.ID).voteCount++;
//        Log.d(TAG, " vote " + mGlobalPosts.get(post.ID).voteCount);
        mPostsRef.child(post.ID).child(Constants.VOTE_FIELD_NAME).runTransaction(mUpVoteHandler);
    }

    public static void decrementPost(Post post) {
        mGlobalPosts.get(post.ID).voteCount--;
//        Log.d(TAG, " vote " + mGlobalPosts.get(post.ID).voteCount);
        mPostsRef.child(post.ID).child(Constants.VOTE_FIELD_NAME).runTransaction(mDownVoteHandler);
    }

    public static void incrementReply(Post post, String parentPostID) {
        mGlobalPosts.get(parentPostID).replies.get(post.ID).voteCount++;
//        Log.d(TAG, " vote " + mGlobalPosts.get(parentPostID).replies.get(post.ID).voteCount);
        mPostsRef.child(parentPostID).child(Constants.REPLIES_TABLE_NAME).child(post.ID).child(Constants.VOTE_FIELD_NAME).runTransaction(mUpVoteHandler);
    }

    public static void decrementReply(Post post, String parentPostID) {
        mGlobalPosts.get(parentPostID).replies.get(post.ID).voteCount--;
//        Log.d(TAG, " vote " + mGlobalPosts.get(parentPostID).replies.get(post.ID).voteCount);
        mPostsRef.child(parentPostID).child(Constants.REPLIES_TABLE_NAME).child(post.ID).child(Constants.VOTE_FIELD_NAME).runTransaction(mDownVoteHandler);
    }


    public static void downloadPosts() {
//        Log.d(TAG, "About to download posts");
        mFinishedDownloading = false;
        mPostsRef.addListenerForSingleValueEvent(mPostsSingleEventListener);
    }

    private static ValueEventListener mPostsSingleEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            HashMap<String, Post> posts = new HashMap<>();
            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                Post post = postSnapshot.getValue(Post.class);
                if (post.replies == null) {
                    post.replies = new HashMap<>();
                }
                for (String replyID : post.replies.keySet()) {
                    post.replies.get(replyID).ID = replyID;
                }
                posts.put(postSnapshot.getKey(), post);
            }
            mGlobalPosts = posts;
            mFinishedDownloading = true;
//            Log.d(TAG, "Finish downloading posts");
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    private static ValueEventListener mAddReplySingleEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Post post = dataSnapshot.getValue(Post.class);
            if (post == null) return;
            Post reply = mAddReplyQueue.poll();
            mPostsRef.child(post.ID).child(Constants.REPLIES_TABLE_NAME).child(reply.ID)
                    .updateChildren(reply.toMap());
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };


    private static com.google.firebase.database.Transaction.Handler mUpVoteHandler =
            new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    Long vote = mutableData.getValue(Long.class);
                    if (vote == null) {
                        return Transaction.success(mutableData);
                    }
                    vote++;
                    mutableData.setValue(vote);
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                   }
            };
    private static com.google.firebase.database.Transaction.Handler mDownVoteHandler =
            new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    Long vote = mutableData.getValue(Long.class);
                    if (vote == null) {
                        return Transaction.success(mutableData);
                    }
                    vote--;
                    mutableData.setValue(vote);
                    return Transaction.success(mutableData);
                }
                @Override
                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                }
            };


    public static void sortDescendingByTime(ArrayList<Post> posts) {
        Collections.sort(posts, new Comparator<Post>() {
            @Override
            public int compare(Post post1, Post post2) {
                return ((Long) post2.timeInMilliseconds).compareTo(post1.timeInMilliseconds);
            }
        });
    }

    public static void sortAscendingByTime(ArrayList<Post> posts) {
        Collections.sort(posts, new Comparator<Post>() {
            @Override
            public int compare(Post post1, Post post2) {
                return ((Long) post1.timeInMilliseconds).compareTo(post2.timeInMilliseconds);
            }
        });
    }

    public static void sortDescendingByVoteCount(ArrayList<Post> posts) {
        Collections.sort(posts, new Comparator<Post>() {
            @Override
            public int compare(Post post1, Post post2) {
                return (new Integer(post2.voteCount)).compareTo(post1.voteCount);
            }
        });
    }

    public static void formatTime(ArrayList<Post> posts) {
        for (Post post : posts) {
            post.displayedTime = getRelativeTimeSpanString(post.timeInMilliseconds);
        }
    }


    public static boolean isTimeToRefresh() {
        long currentTime = System.currentTimeMillis();
        if (((currentTime - mTimeLastRefreshed) > Constants.REFRESH_RATE) || (mGlobalPosts.size() < 1)) {
            mTimeLastRefreshed = currentTime;
            return true;
        }
        return false;
    }

    private static final String ABBR_YEAR = "y";
    private static final String ABBR_WEEK = "w";
    private static final String ABBR_DAY = "d";
    private static final String ABBR_HOUR = "h";
    private static final String ABBR_MINUTE = "m";

    public static String getRelativeTimeSpanString(long timeMillis) {
        long span = Math.max(System.currentTimeMillis() - timeMillis, 0);
        if (span >= DateUtils.YEAR_IN_MILLIS) {
            return (span / DateUtils.YEAR_IN_MILLIS) + ABBR_YEAR;
        }
        if (span >= DateUtils.WEEK_IN_MILLIS) {
            return (span / DateUtils.WEEK_IN_MILLIS) + ABBR_WEEK;
        }
        if (span >= DateUtils.DAY_IN_MILLIS) {
            return (span / DateUtils.DAY_IN_MILLIS) + ABBR_DAY;
        }
        if (span >= DateUtils.HOUR_IN_MILLIS) {
            return (span / DateUtils.HOUR_IN_MILLIS) + ABBR_HOUR;
        }
        return (span / DateUtils.MINUTE_IN_MILLIS) + ABBR_MINUTE;
    }


}
