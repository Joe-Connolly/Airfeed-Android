package com.artfara.apps.kipper;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Joe Connolly on 10/7/16.
 */

public class PlacesListViewAdapter extends BaseAdapter {

    private static ArrayList<Place> mActivePlaces; //ArrayList to hold active Entries in the Adapter
    private Context c; //Holds context adapter was created in

    private static final String TAG = " Places ListAdapter";
    private LayoutInflater mInflater;
    private static String mPlaceType;

    public PlacesListViewAdapter(Context context, String placeType) {
        mInflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        c = context;
        mPlaceType = placeType;
        mActivePlaces = new ArrayList<>();
    }

    //Sort Places in order of decreasing people count and only display places of correct type
    public void setEntries(ArrayList<Place> places) {
            ArrayList<Place> tempPlaces = new ArrayList<>();
            for (Place place : places) {
                if (place.type.equals(mPlaceType)) {
                    tempPlaces.add(place);
                }
            }
            mActivePlaces = tempPlaces;
            notifyDataSetChanged();
    }

    public int getCount() {
        Log.d(TAG, "" + mActivePlaces);
        return mActivePlaces.size();
    }

    public Object getItem(int position) {
        return mActivePlaces.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    //Displays Name of Place, and #of people for each row in listView.
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();

        Typeface typeFaceBold = Typeface.createFromAsset(c.getAssets(), "Comfortaa-Bold.ttf");
        Typeface typeFaceLight = Typeface.createFromAsset(c.getAssets(), "Comfortaa-Light.ttf");

        Place currentPlace = mActivePlaces.get(position);
        View rowView = mInflater.inflate(R.layout.places_custom_row_view, null);
        holder.txtPlaceName = (TextView) rowView.findViewById(R.id.placeName);
        holder.txtPeopleCount = (TextView) rowView.findViewById(R.id.peopleCount);

        holder.txtPlaceName.setText(currentPlace.location);
        holder.txtPlaceName.setTypeface(typeFaceLight);
        holder.txtPeopleCount.setText(Utils.getPeopleString(currentPlace));
        holder.txtPeopleCount.setTypeface(typeFaceBold);
        Log.d(TAG, "called ");

        return rowView;
    }

    //Empty class to hold both TextViews
    static class ViewHolder {
        TextView txtPlaceName;
        TextView txtPeopleCount;
    }
}
