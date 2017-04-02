package com.artfara.apps.kipper;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {
    private Handler mHandler;
    private ChatListViewAdapter customBaseAdapter;
    private static final String TAG = " Chat Fragment ";
    private PostDatabaseHelper mPostDatabaseHelper;

    public ChatFragment() {
        // Required empty public constructor
        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        Button postButton = (Button) rootView.findViewById(R.id.post);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), PostActivity.class);
                startActivity(intent);
            }
        });
        Button showHotButton = (Button) rootView.findViewById(R.id.showhot);
        showHotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostDatabaseHelper.showPosts(Constants.POSTS_TYPE_HOT);
                customBaseAdapter.setEntries(PostDatabaseHelper.getPosts());
            }
        });
        Button showNewButton = (Button) rootView.findViewById(R.id.shownew);
        showNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostDatabaseHelper.showPosts(Constants.POSTS_TYPE_NEW);
                customBaseAdapter.setEntries(PostDatabaseHelper.getPosts());
            }
        });
        Button refreshButton = (Button) rootView.findViewById(R.id.refresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customBaseAdapter.setEntries(new ArrayList<Post>());
                PostDatabaseHelper.downloadPosts();
                //update posts as soon as they become available
                mHandler = new Handler();
                mHandler.postDelayed(mPopulateListViewRunnable, 100);
            }
        });


        //Create Custom Adapter
        customBaseAdapter = new ChatListViewAdapter(getActivity(), null);

        //Grab a handle on ListView
        final ListView listview = (ListView) rootView.findViewById(R.id.ListViewPosts);

        listview.setAdapter(customBaseAdapter);

        return rootView;
        // Inflate the layout for this fragment
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        //Refresh only if necessary
        if (PostDatabaseHelper.isTimeToRefresh()) {
            customBaseAdapter.setEntries(new ArrayList<Post>());

            PostDatabaseHelper.downloadPosts();
            //update posts as soon as they become available
            mHandler = new Handler();
            mHandler.postDelayed(mPopulateListViewRunnable, 100);
        }
    }


    Runnable mPopulateListViewRunnable = new Runnable() {
        public void run() {
            //If data has not yet been downloaded, try again later
            if (PostDatabaseHelper.mFinishedDownloading == false){
                Log.d(TAG, "posts still null");
                mHandler.postDelayed(this, 200);
            }
            else{
                customBaseAdapter.setEntries(PostDatabaseHelper.getPosts());
        }
        }
    };



}
