package com.artfara.apps.kipper;

import java.util.HashMap;

/**
 * Created by Joe Connolly on 12/13/16.
 */
public class Post {
    public String ID;
    public String text;
    public HashMap<String, Post> replies;
    public int voteCount;
    public long timeInMilliseconds;
    public String userID;
    public String userLetter;
    public String displayedTime;

    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue()
    }

    public Post(String userID, String messsageBody, long time) {
        text = messsageBody;
        replies = new HashMap<>();
        this.userID = userID;
        this.timeInMilliseconds = time;
    }

}
