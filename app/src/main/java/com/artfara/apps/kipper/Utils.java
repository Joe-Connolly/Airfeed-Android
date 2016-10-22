package com.artfara.apps.kipper;

/**
 * Created by Van on 10/6/16.
 */

public class Utils {

    public static String getPeopleString(Place place){
        //Inflate the number of people based on how many users have their location provided
        int people = ((Double) (Globals.studentsWithAppRatio * place.people.doubleValue())).intValue();
        String peopleString = "";
//        Log.d("utils",  place.location + " " + place.state);
        switch (place.state){
            case (Constants.STATE.NORMAL):
                 //set the snippet to be people or person depending on if there are 1 or more people
                    peopleString = (people == 1) ? people + " person" : people + " people";
                break;

            case (Constants.STATE.LIT):
                        peopleString = people + " Lit!";
                break;

            case (Constants.STATE.OVER_CAPACITY):
                peopleString = "OVERCAPACITY";
                break;
        }

        return peopleString;
    }
}
