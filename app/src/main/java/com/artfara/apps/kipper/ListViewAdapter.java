package com.artfara.apps.kipper;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Van on 10/2/16.
 */

public class ListViewAdapter extends BaseAdapter {

    private static ArrayList<Place> activePlaces; //ArrayList to hold Entiries in the Adapter
    private Context c; //Holds context adapter was created in

    private static final String TAG = " ListAdapter";
    private LayoutInflater mInflater;

    public ListViewAdapter(Context context, boolean usePlaceTotalsAsEntries) {
//        activePlaces = new ArrayList<>();
//        mInflater = LayoutInflater.from(context);
        mInflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        c = context;
    }

//    public static ArrayList<ExerciseEntry> getEntries(){
//        return entries;
//    }
//    public static void setEntries(ArrayList<Place> places){
//        activePlaces = places;
//    }
    public static void setEntries(ArrayList<Place> places){
        ArrayList<Place> placeTotals = new ArrayList<>();
        Log.d(TAG, " places.Global " + places);

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

    //Creates readable summary of each exercise object using two textViews
    //to create a hearder/subheader effect.
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();

        Place currentPlace = activePlaces.get(position);
        View rowView = mInflater.inflate(R.layout.custom_row_view, null);
        holder.txtPlaceName = (TextView) rowView.findViewById(R.id.placeName);
        holder.txtPeopleCount = (TextView) rowView.findViewById(R.id.peopleCount);
        holder.placeTypeImage = (ImageView) rowView.findViewById(R.id.placeTypeImage);
//        holder.img=(ImageView) rowView.findViewById(R.id.imageView1);
//        holder.tv.setText(result[position]);

        holder.txtPlaceName.setText(currentPlace.location);
        holder.txtPeopleCount.setText(Utils.getPeopleString(currentPlace));
        holder.placeTypeImage.setImageDrawable(c.getDrawable(Constants.PLACES.get(currentPlace.type)));

        Log.d("Adapter ", "called " );



//
//        if (convertView == null) {
//            convertView = mInflater.inflate(R.layout.custom_row_view, null);
//            holder = new ViewHolder();
//            //Get handles on both textviews
//            holder.txtPlaceName = (TextView) convertView.findViewById(R.id.line1);
////            holder.txtLine2 = (TextView) convertView.findViewById(R.id.line2);
//
//            convertView.setTag(holder);
//        } else {
//            holder = (ViewHolder) convertView.getTag();
//        }
////        //Grab the entry at the current position
////        ExerciseEntry entry = entries.get(position);
////        //Get both header and subheader lines
////        String line1 = getLine1(entry);
////        String line2 = getLine2(entry);
//        //Set header to top textview
//        holder.txtPlaceName.setText("" + activePlaces.get(position));
////        //Set subheader to bottom textview
////        holder.txtLine2.setText(String.valueOf(line2));

        return rowView;
    }
//    //Helper method to generate header line for the specific ExerciseEntry
//    public String getLine1(ExerciseEntry entry){
////        String line1 = "";
////        String[] inputs = c.getResources().getStringArray(R.array.inputtype_array);
////        String[] activities = c.getResources().getStringArray(R.array.activitytype_array);
//        String dateTime = HistoryFragment.getDateTimeString(entry);
//        return HistoryFragment.getInputTypeString(entry.getmInputType(), c) + ": " +
//                HistoryFragment.getActivityTypeString(entry.getmActivityType(), c) + ", " + dateTime;
//    }
//
//    //Helper method to generate subheader line for the specific ExerciseEntry
//    public String getLine2(ExerciseEntry entry){
//        String line2 = "";
//        String distance;
//        String time;
//        distance = HistoryFragment.getDistance(entry, c);
//        if (entry.getmDuration() == 0){
//            time = "0secs";
//        }
//        else{
//            time = entry.getmDuration() + " mins 0secs";
//        }
//        return distance + ", " + time;
//    }
    //Empty class to hold both Textviews
    static class ViewHolder {
        TextView txtPlaceName;
        TextView txtPeopleCount;
        ImageView placeTypeImage;
//        TextView txtLine2;
    }
}
