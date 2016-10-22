package com.artfara.apps.kipper;

import android.content.Context;
import android.content.Intent;
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
        setActiveEntries();
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
            // Sorting
            Collections.sort(tempPlaces, new Comparator<Place>() {
                @Override
                public int compare(Place place1, Place place2){
                    return  place2.people.compareTo(place1.people);
                }
            });
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

        Place currentPlace = activePlaces.get(position);
        View rowView = mInflater.inflate(R.layout.custom_row_view, null);
        holder.txtPlaceName = (TextView) rowView.findViewById(R.id.placeName);
        holder.txtPeopleCount = (TextView) rowView.findViewById(R.id.peopleCount);
        holder.placeTypeImage = (ImageView) rowView.findViewById(R.id.placeTypeImage);


        holder.txtPlaceName.setText(currentPlace.location);
//        holder.txtPeopleCount.setText(Utils.getPeopleString(currentPlace));
        holder.placeTypeImage.setImageDrawable(c.getDrawable(Constants.PLACES.get(currentPlace.type)));

        Log.d(TAG, "called " );


        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(c.getApplicationContext(), ChatActivity.class);
                c.startActivity(intent);
            }
        });



        return rowView;
    }

    //Empty class to hold both TextViews
    static class ViewHolder {
        TextView txtPlaceName;
        TextView txtPeopleCount;
        ImageView placeTypeImage;
    }
}
