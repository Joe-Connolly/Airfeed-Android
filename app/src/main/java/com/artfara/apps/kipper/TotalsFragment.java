package com.artfara.apps.kipper;


import android.app.ProgressDialog;
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
    private ProgressDialog mProgressDialog;

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
        mHandler.postDelayed(mPopulateListViewRunnable, Constants.MILLISECONDS_BEFORE_POLLING);

    }

    Runnable mPopulateListViewRunnable = new Runnable() {
        public void run() {
            //If data has not yet been downloaded, try again later
            if (Globals.globalPlaces == null){
                if (mProgressDialog == null && getContext() != null) {
                    //Display loading spinner
                    mProgressDialog = new ProgressDialog(getContext());
                    mProgressDialog.setMessage(getString(R.string.loading_message));
                    mProgressDialog.show();
                }
//                Log.d(TAG, "places still null");
                mHandler.postDelayed(this, Constants.MILLISECONDS_BETWEEN_POLLING);
            }
            else{
                customBaseAdapter.setEntries(Globals.globalPlaces);
                if (getActivity() != null && mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }
            }
        }
    };

}
