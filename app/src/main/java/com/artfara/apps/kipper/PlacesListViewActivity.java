package com.artfara.apps.kipper;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class PlacesListViewActivity extends AppCompatActivity {

    public static PlacesListViewAdapter customBaseAdapter;
    private static final String TAG = " PlacesListView ";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        String placeType = getIntent().getStringExtra(Constants.PLACE_TYPE_KEY);

        //Create Custom Adapter
        customBaseAdapter = new PlacesListViewAdapter(this, placeType);
        customBaseAdapter.setEntries(Globals.globalPlaces);

        //Grab a handle on ListView
        final ListView listview = (ListView) findViewById(R.id.ListViewTotals);

        listview.setAdapter(customBaseAdapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("");
            setSupportActionBar(toolbar);
            TextView title = (TextView) toolbar.findViewById(R.id.toolbar_title);
            Typeface typeFaceBold = Typeface.createFromAsset(getAssets(), "Comfortaa-Bold.ttf");
            title.setTypeface(typeFaceBold);
        }
    }
}
