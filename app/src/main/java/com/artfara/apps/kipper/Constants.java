package com.artfara.apps.kipper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Van on 9/1/16.
 */
public class Constants {
    //Constants to be used throughout the class
    public final static String USERNAME_KEY = "com.artfara.kipper.USERNAME";
    public final static String USERS_TABLE_NAME = "users";
    public final static String PLACES_TABLE_NAME = "testplaces";
    public static final String ID_KEY = "id";
    public static final double HANOVER_LATITUDE = 43.703272;
            public static final double HANOVER_LONGITUDE = -72.288633;
    public static Map<String, Integer> PLACES = new HashMap<>();

    public static void prepare() {
        PLACES.put("frat", R.drawable.frat_marker);
        PLACES.put("gym", R.drawable.gym_marker);
        PLACES.put("library", R.drawable.library_marker);
        PLACES.put("food", R.drawable.food_marker);
        PLACES.put("building", R.drawable.building_marker);
        PLACES.put("event", R.drawable.event_marker);
    }

}

