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

/**
 * Created by Joe on 10/2/16.
 * Listview Adapter to Display Place Totals, i.e. total number of students
 * at frats, at gyms, etc.
 */

public class TotalsListViewAdapter extends BaseAdapter {

    private static ArrayList<Place> activePlaces; //ArrayList to hold Entries in the Adapter
    private Context c; //Holds context adapter was created in

    private static final String TAG = " ListAdapter";
    private LayoutInflater mInflater;

    public TotalsListViewAdapter(Context context, boolean usePlaceTotalsAsEntries) {
        mInflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        c = context;
        activePlaces = new ArrayList<>();
    }

    public void setEntries(ArrayList<Place> places){
        ArrayList<Place> placeTotals = new ArrayList<>();
//        Log.d(TAG, " places.Global " + places);
        for (int i = 0; i < Constants.PLACE_TOTALS_TEMPLATES.length; i++){
            Place placeTotal = new Place(Constants.PLACE_TOTALS_TEMPLATES[i].location, Constants.PLACE_TOTALS_TEMPLATES[i].type);
            for (Place place:places){
                if (place.type.equals(placeTotal.type)){
                    placeTotal.people += place.people;
                }
            }
            placeTotals.add(placeTotal);
        }


        activePlaces = placeTotals;

      notifyDataSetChanged();
    }
    public int getCount() {
//        Log.d(TAG, "" + activePlaces);
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
        final Place currentPlace = activePlaces.get(position);
        View rowView = mInflater.inflate(R.layout.custom_row_view, null);

        Typeface typeFaceBold = Typeface.createFromAsset(c.getAssets(), "Comfortaa-Bold.ttf");
        Typeface typeFaceLight = Typeface.createFromAsset(c.getAssets(), "Comfortaa-Light.ttf");

        holder.txtPlaceName = (TextView) rowView.findViewById(R.id.placeName);
        holder.txtPeopleCount = (TextView) rowView.findViewById(R.id.peopleCount);
        holder.placeTypeImage = (ImageView) rowView.findViewById(R.id.placeTypeImage);


        holder.txtPlaceName.setText(currentPlace.location);
        holder.txtPlaceName.setTypeface(typeFaceLight);
        holder.txtPeopleCount.setText(Utils.getPeopleString(currentPlace));
        holder.txtPeopleCount.setTypeface(typeFaceBold);
        holder.placeTypeImage.setImageDrawable(c.getDrawable(Constants.PLACES.get(currentPlace.type)));



        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(c.getApplicationContext(), PlacesListViewActivity.class);
                intent.putExtra(Constants.PLACE_TYPE_KEY, currentPlace.type);
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
