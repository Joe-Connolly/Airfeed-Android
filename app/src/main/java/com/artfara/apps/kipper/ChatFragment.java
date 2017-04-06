package com.artfara.apps.kipper;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

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

        final View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        final LinearLayout postLayout = (LinearLayout) rootView.findViewById(R.id.wrapper_post);
        postLayout.setOnClickListener(new View.OnClickListener() {
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
        ImageButton refreshButton = (ImageButton) rootView.findViewById(R.id.refresh);
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
        RadioGroup hotNewRadioGroup = (RadioGroup) rootView.findViewById(R.id.hotNewRadioGroup);
        hotNewRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {

                Log.d(TAG,"checkedId = " + checkedId + " " + R.id.showhot + " " + R.id.shownew);
            }
        });


        //Create Custom Adapter
        customBaseAdapter = new ChatListViewAdapter(getActivity(), null);

        //Grab a handle on ListView
        final ListView listview = (ListView) rootView.findViewById(R.id.ListViewPosts);
        listview.setAdapter(customBaseAdapter);
        listview.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int mLastFirstVisibleItem;
            private long mLastTimeUpdated;
            private static final long REFRESH_RATE = 50;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                Log.d(TAG, "lastitem = " + mLastFirstVisibleItem + " curritem = " + firstVisibleItem);
                if(mLastFirstVisibleItem < firstVisibleItem)
                {
                    Log.i("SCROLLING DOWN","TRUE");
                    if (isAcceptableToChangeState()) {
                        RelativeLayout wrapperLayout = (RelativeLayout) rootView.findViewById(R.id.wrapper);
                        wrapperLayout.setVisibility(RelativeLayout.GONE);
                        postLayout.setVisibility(LinearLayout.GONE);
                    }
                }
                if(mLastFirstVisibleItem > firstVisibleItem)
                {
                    Log.i("SCROLLING UP","TRUE");
                    if (isAcceptableToChangeState()) {
                        RelativeLayout wrapperLayout = (RelativeLayout) rootView.findViewById(R.id.wrapper);
                        wrapperLayout.setVisibility(RelativeLayout.VISIBLE);
                        postLayout.setVisibility(LinearLayout.VISIBLE);
                    }
                }
               mLastFirstVisibleItem = firstVisibleItem;
            }
            public boolean isAcceptableToChangeState(){
                long currTime = System.currentTimeMillis();
                if ((currTime - mLastTimeUpdated) > REFRESH_RATE){
                    mLastTimeUpdated = currTime;
                    return true;
                }
                return false;
            }
        });

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
        else{
            //Could slow stuff down
            customBaseAdapter.setEntries(PostDatabaseHelper.getPosts());
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
