package com.artfara.apps.kipper;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ListView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ListViewActivity extends Activity {



    public static ListViewAdapter customBaseAdapter;
    private static final String TAG = " ListView class";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);






//        Log.d(TAG, "onCreate set entries " + placeTotals);
//        ListViewAdapter.setEntries(placeTotals);

        //Create Custom Adapter

        customBaseAdapter = new ListViewAdapter(getApplicationContext(), true);
        //Grab a handle on ListView
        final ListView listview = (ListView) findViewById(R.id.ListViewTotals);

//        customBaseAdapter.notifyDataSetChanged();
        listview.setAdapter(customBaseAdapter);



    }
}
