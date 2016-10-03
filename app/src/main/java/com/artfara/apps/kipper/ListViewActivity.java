package com.artfara.apps.kipper;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class ListViewActivity extends Activity {


    private static ListViewAdapter customBaseAdapter;
    private static final String TAG = " ListView class";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);


        Log.d(TAG, "onCreate");
        //Create Custom Adapter
        customBaseAdapter = new ListViewAdapter(getApplicationContext(), true);
        //Grab a handle on ListView
        final ListView listview = (ListView) findViewById(R.id.ListViewTotals);
        //Update adapter
//        customBaseAdapter.notifyDataSetChanged();
        listview.setAdapter(customBaseAdapter);
        //Set onClick Listener for logs to let user go to a more detailed view of each exercise
//        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
//                ExerciseEntry clickedEntry = MyCustomBaseAdapter.getEntries().get(position);
//
//                //If the inputType is not manual, display a map instead of a bunch of textboxes
//                if (clickedEntry.getmInputType() != 1 ){
//                    //Create an intent to go to MapDisplayActivityClass
//                    Intent intent = new Intent(getActivity(), MapDisplayActivity.class);
//                    //Put the position in the ArrayList of entries, so that the correct entry
//                    //is opened
//                    intent.putExtra(SELECTED_ENTRY_KEY, position);
//                    intent.putExtra(MapDisplayActivity.EDITABLE, false);
//                    startActivity(intent);
//                    return;
//                }
//
//                //Otherwise, entry is manual, so create an intent to go to DisplayEntryActivityClass
//                Intent intent = new Intent(getActivity(), DisplayEntryActivity.class);
//                //Put the position in the ArrayList of entries, so that the correct entry
//                //is opened
//                intent.putExtra(SELECTED_ENTRY_KEY, position);
//                startActivity(intent);
//            }
//        });


    }
}
