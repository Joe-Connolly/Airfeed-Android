package com.artfara.apps.kipper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Van on 9/1/16.
 */
public class Constants {
    //Constants to be used throughout the class
    public final static String TEST_KEY = "testKey";
    public final static String USERS_TABLE_NAME = "users";
    public final static String PLACES_TABLE_NAME = "testplaces";
    public final static String POSTS_TABLE_NAME = "Posts";
    public final static String POSTS_HOT_TABLE_NAME = "Posts_Hot";
    public final static String REPLIES_TABLE_NAME = "replies";
    public static final String ID_KEY = "id";
    public static final double HANOVER_LATITUDE = 43.703272;
    public static final double HANOVER_LONGITUDE = -72.288633;
    public static final String CONSTANTS_TABLE_NAME = "Constants";
    public static final Place[] PLACE_TOTALS_TEMPLATES = new Place[] {new Place("Greek Life", "frat"), new Place("Food Courts", "food"),
            new Place("Libraries", "library"), new Place("Gyms", "gym"), new Place("Events", "event")};
    public static final String PLACE_TYPE_KEY = "place type";
    public static final String POST_ID_KEY = "post id";
    public static final String USER_ID_KEY = "username";
    public static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    public static final String POSTS_TYPE_HOT = "HOT";
    public static final String POSTS_TYPE_NEW = "NEW";

    public static Map<String, Integer> PLACES = new HashMap<>();
//    public static final class STATE {
//        public static final int NORMAL = 0;
//        public static final int LIT = 1;
//        public static final int OVER_CAPACITY = 2;
//    }

    public static void prepare() {
        PLACES.put("frat", R.drawable.frat_marker);
        PLACES.put("gym", R.drawable.gym_marker);
        PLACES.put("library", R.drawable.library_marker);
        PLACES.put("food", R.drawable.food_marker);
        PLACES.put("building", R.drawable.building_marker);
        PLACES.put("event", R.drawable.event_marker);
    }

}

