package com.artfara.apps.kipper;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class ChatReplyListViewActivity extends AppCompatActivity {

    private String mPostId;
    private ChatListViewAdapter customBaseAdapter;
    private static final String TAG = " ChatReply Activity";
    private ListView listview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_replies_list_view);

        mPostId = getIntent().getStringExtra(Constants.POST_ID_KEY);


        //Create Custom Adapter
        customBaseAdapter = new ChatListViewAdapter(this, mPostId);

        //Grab a handle on ListView
        listview = (ListView) findViewById(R.id.ListViewReplies);
        listview.setAdapter(customBaseAdapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("");
            setSupportActionBar(toolbar);
            TextView title = (TextView) toolbar.findViewById(R.id.toolbar_title);
            Typeface typeFaceBold = Typeface.createFromAsset(getAssets(), "Comfortaa-Bold.ttf");
            title.setTypeface(typeFaceBold);
        }
        LinearLayout postLayout = (LinearLayout) findViewById(R.id.wrapper_post);
        postLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatReplyListViewActivity.this, PostActivity.class);
                intent.putExtra(Constants.POST_ID_KEY, mPostId);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        customBaseAdapter.setEntries(PostDatabaseHelper.getReplies(mPostId));
        if (Globals.replyJustMade) {
            listview.post(new Runnable(){
                public void run() {
                    listview.setSelection(listview.getCount() - 1);
                }});
            Globals.replyJustMade = false;
        }
    }
}
