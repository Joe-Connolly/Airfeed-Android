package com.artfara.apps.kipper;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Van on 8/31/16.
 * This class is structured so that it can be inserted or queried from
 * the Firebase database
 * For a particular user, this class holds their latitude and longitude
 */
@IgnoreExtraProperties
public class Latlng {

    public Double latitude;
    public Double longitude;


    public Latlng() {
        // Default constructor required for calls to DataSnapshot.getValue(Latlng.class)
    }

    public Latlng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

}