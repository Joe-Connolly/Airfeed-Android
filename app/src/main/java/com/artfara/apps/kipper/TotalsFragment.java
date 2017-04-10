package com.artfara.apps.kipper;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


/**
 * A simple {@link Fragment} subclass.
 */
public class TotalsFragment extends Fragment {
    private Handler mHandler;
    public static TotalsListViewAdapter customBaseAdapter;
    private static final String TAG = "Totals Fragment";

    public TotalsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        Log.d(TAG, "onCreate");

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_totals, container, false);

        //Create Custom Adapter
        customBaseAdapter = new TotalsListViewAdapter(getActivity(), true);

        //Grab a handle on ListView
        final ListView listview = (ListView) rootView.findViewById(R.id.ListViewTotals);

        listview.setAdapter(customBaseAdapter);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
//        Log.d(TAG, "onStart");
        //update place total numbers as soon as they become available
        mHandler = new Handler();
        mHandler.postDelayed(mPopulateListViewRunnable, 200);

    }

    Runnable mPopulateListViewRunnable = new Runnable() {
        public void run() {
            //If data has not yet been downloaded, try again later
            if (Globals.globalPlaces == null){
//                Log.d(TAG, "places still null");
                mHandler.postDelayed(this, 200);
            }
            else{
                customBaseAdapter.setEntries(Globals.globalPlaces);
            }
        }
    };

}
