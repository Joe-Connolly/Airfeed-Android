package com.artfara.apps.kipper;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Van on 8/31/16.
 */
@IgnoreExtraProperties
public class Latlng {

    public Double latitude;
    public Double longitude;
//        public String username;

    public Latlng() {
        // Default constructor required for calls to DataSnapshot.getValue(Latlng.class)
    }

    public Latlng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
//            this.username = username;
    }

}