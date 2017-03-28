package com.artfara.apps.kipper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Joe Connolly on 10/7/16.
 */

public class PlacesListViewAdapter extends BaseAdapter {

    private static ArrayList<Place> allPlaces; //ArrayList to hold all current Place Entries (only a certian type is displayed at once)
    private static ArrayList<Place> activePlaces; //ArrayList to hold active Entries in the Adapter
    private Context c; //Holds context adapter was created in

    private static final String TAG = " Places ListAdapter";
    private LayoutInflater mInflater;
    private static String mPlaceType;

    public PlacesListViewAdapter(Context context, String placeType) {
        mInflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        c = context;
        mPlaceType = placeType;
    }

    //Sort Places in order of decreasing people count and only display places of correct type
    public static void setAllEntries(ArrayList<Place> places){
        allPlaces = places;
        setActiveEntries();
    }
    //Sort Places in order of decreasing people count and only display places of correct type
    public static void setActiveEntries(){

        if (mPlaceType != null){
            ArrayList<Place> tempPlaces = new ArrayList<>();
            for (Place place:allPlaces){
                if (place.type.equals(mPlaceType)){
                    tempPlaces.add(place);
                }
            }
//            // Sorting
//            Collections.sortDescending(tempPlaces, new Comparator<Place>() {
//                @Override
//                public int compare(Place place1, Place place2){
//                    return  place2.people.compareTo(place1.people);
//                }
//            });
            activePlaces = tempPlaces;
        }

    }



    public int getCount() {
        Log.d(TAG, "" + activePlaces);
        return activePlaces.size();
    }

    public Object getItem(int position) {
        return activePlaces.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    //Displays Name of Place, and #of people for each row in listView.
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();

        Typeface typeFaceBold = Typeface.createFromAsset(c.getAssets(), "Comfortaa-Bold.ttf");
        Typeface typeFaceLight = Typeface.createFromAsset(c.getAssets(), "Comfortaa-Light.ttf");

        Place currentPlace = activePlaces.get(position);
        View rowView = mInflater.inflate(R.layout.places_custom_row_view, null);
        holder.txtPlaceName = (TextView) rowView.findViewById(R.id.placeName);
        holder.txtPeopleCount = (TextView) rowView.findViewById(R.id.peopleCount);



        holder.txtPlaceName.setText(currentPlace.location);
        holder.txtPlaceName.setTypeface(typeFaceLight);
        holder.txtPeopleCount.setText(Utils.getPeopleString(currentPlace));
        holder.txtPeopleCount.setTypeface(typeFaceBold);
        Log.d(TAG, "called " );




        return rowView;
    }

    //Empty class to hold both TextViews
    static class ViewHolder {
        TextView txtPlaceName;
        TextView txtPeopleCount;
    }
}
