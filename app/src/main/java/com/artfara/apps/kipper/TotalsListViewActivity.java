package com.artfara.apps.kipper;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class TotalsListViewActivity extends Activity {



    public static TotalsListViewAdapter customBaseAdapter;
    private static final String TAG = " TotalsListView class";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);






//        Log.d(TAG, "onCreate set entries " + placeTotals);

        //Create Custom Adapter
        customBaseAdapter = new TotalsListViewAdapter(this, true);

        //Grab a handle on ListView
        final ListView listview = (ListView) findViewById(R.id.ListViewTotals);

        listview.setAdapter(customBaseAdapter);



    }
}
