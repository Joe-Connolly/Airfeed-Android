package com.artfara.apps.kipper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Van on 9/16/16.
 */
public class DBConstants {
    public Double ratioValue;


    public DBConstants() {
        // Default constructor required for calls to DataSnapshot.getValue(Latlng.class)
    }

    public DBConstants(double ratio) {
        this.ratioValue = ratio;
    }
}
