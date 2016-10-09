package com.artfara.apps.kipper;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class PlacesListViewActivity extends Activity {

    public static PlacesListViewAdapter customBaseAdapter;
    private static final String TAG = " PlacesListView class";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        String placeType = getIntent().getStringExtra(Constants.PLACE_TYPE_KEY);

//        Log.d(TAG, "onCreate set entries " + placeTotals);

        //Create Custom Adapter
        customBaseAdapter = new PlacesListViewAdapter(this, placeType);

        //Grab a handle on ListView
        final ListView listview = (ListView) findViewById(R.id.ListViewTotals);

        listview.setAdapter(customBaseAdapter);

    }
}
