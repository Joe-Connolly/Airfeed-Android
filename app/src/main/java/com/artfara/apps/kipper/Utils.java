package com.artfara.apps.kipper;

import android.util.Log;

/**
 * Created by Van on 10/6/16.
 */

public class Utils {

    public static String getPeopleString(Place place){
        int people = ((Double) (MapsActivity.mStudentsWithAppRatio * place.people.doubleValue())).intValue();
        //Add place to placesGlobal so it can be accessed from listView
        String peopleString = (people == 1) ? people + " person" : people + " people";
        return peopleString;
    }
}
