package com.artfara.apps.kipper;
import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Van on 9/5/16.
 * This class is structured so that it can be inserted or queried from
 * the Firebase database
 * For a particular place, this class holds its latitude and longitude
 * as well as name (location), etc.
 */
@IgnoreExtraProperties
public class Place {

    public Double latitude;
    public Double longitude;
    public Double radius;
    public String location;
    public Integer people;


    public Place() {
        // Default constructor required for calls to DataSnapshot.getValue(Latlng.class)
    }

    public Place(double latitude, double longitude, double radius, String location, int userCount) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.location = location;
        this.people = userCount;
    }

}