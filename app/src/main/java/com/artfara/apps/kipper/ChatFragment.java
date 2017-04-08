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


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {
    private Handler mHandler;
    private ChatListViewAdapter customBaseAdapter;
    private static final String TAG = " Chat Fragment ";
    private PostDatabaseHelper mPostDatabaseHelper;
    private View mRootView;
    private LinearLayout mPostButtonLayout;
    private RadioGroup mHotNewRadioGroup;

    public ChatFragment() {
        // Required empty public constructor
        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_chat, container, false);
        mPostButtonLayout = (LinearLayout) mRootView.findViewById(R.id.wrapper_post);
        mPostButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), PostActivity.class);
                startActivity(intent);
            }
        });
        Button showHotButton = (Button) mRootView.findViewById(R.id.showhot);
        showHotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostDatabaseHelper.showPosts(Constants.POSTS_TYPE_HOT);
                customBaseAdapter.setEntries(PostDatabaseHelper.getPosts());
            }
        });
        Button showNewButton = (Button) mRootView.findViewById(R.id.shownew);
        showNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostDatabaseHelper.showPosts(Constants.POSTS_TYPE_NEW);
                customBaseAdapter.setEntries(PostDatabaseHelper.getPosts());
            }
        });
        ImageButton refreshButton = (ImageButton) mRootView.findViewById(R.id.refresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customBaseAdapter.setEntries(new ArrayList<Post>());
                PostDatabaseHelper.downloadPosts();
                //update posts as soon as they become available
                mHandler.postDelayed(mPopulateListViewRunnable, 100);
            }
        });
        mHotNewRadioGroup = (RadioGroup) mRootView.findViewById(R.id.hotNewRadioGroup);
        mHotNewRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {

                Log.d(TAG,"checkedId = " + checkedId + " " + R.id.showhot + " " + R.id.shownew);
            }
        });
        
        //Create Custom Adapter
        customBaseAdapter = new ChatListViewAdapter(getActivity(), null);

        //Grab a handle on ListView
        final ListView listview = (ListView) mRootView.findViewById(R.id.ListViewPosts);
        listview.setAdapter(customBaseAdapter);
        listview.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int mLastFirstVisibleItem;
            private long mLastTimeUpdated;
            private static final long REFRESH_RATE = 400;
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
                        hideTabs();
                    }
                }
                if(mLastFirstVisibleItem > firstVisibleItem)
                {
                    Log.i("SCROLLING UP","TRUE");
                    if (isAcceptableToChangeState()) {
                        showTabs();
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

        return mRootView;
        // Inflate the layout for this fragment
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        //Set what type of posts to display based on radiobutton value
        if (mHotNewRadioGroup.getCheckedRadioButtonId() == R.id.showhot){
            PostDatabaseHelper.setPostType(Constants.POSTS_TYPE_HOT);
        }
        else {
            PostDatabaseHelper.setPostType(Constants.POSTS_TYPE_NEW);
        }
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

    public void hideTabs(){
        RelativeLayout wrapperLayout = (RelativeLayout) mRootView.findViewById(R.id.wrapper);
        wrapperLayout.setVisibility(RelativeLayout.GONE);
        mPostButtonLayout.setVisibility(LinearLayout.GONE);
        ((MapsActivity) getActivity()).hideTabs();
    }

    public void showTabs(){
        RelativeLayout wrapperLayout = (RelativeLayout) mRootView.findViewById(R.id.wrapper);
        wrapperLayout.setVisibility(RelativeLayout.VISIBLE);
        mPostButtonLayout.setVisibility(LinearLayout.VISIBLE);
        ((MapsActivity) getActivity()).showTabs();
    }

    @Override
    public void onPause(){
        super.onPause();
        showTabs();
    }
}
