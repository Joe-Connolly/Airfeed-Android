package com.artfara.apps.kipper;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PostActivity extends AppCompatActivity {
    private static final String TAG = " PostActivity ";
    private String mParentPostID;
    private DatabaseReference mPostDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

//        mPostDatabase = FirebaseDatabase.getInstance().getReference().child(Constants.POSTS_TABLE_NAME);
        mParentPostID = getIntent().getStringExtra(Constants.POST_ID_KEY);



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("");
            setSupportActionBar(toolbar);
            TextView title = (TextView) toolbar.findViewById(R.id.toolbar_title);
            Typeface typeFaceBold = Typeface.createFromAsset(getAssets(), "Comfortaa-Bold.ttf");
            title.setTypeface(typeFaceBold);
        }

        EditText editText = (EditText) findViewById(R.id.postBody);
        editText.setHorizontallyScrolling(false);
        editText.setMaxLines(Integer.MAX_VALUE);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.d(TAG, "Send action");
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    Log.d(TAG, "Send action");
                    onPostClicked(v);
                    return true;
                }
                return false;
            }
        });
    }

    public void onPostClicked(View view) {

        String postBody = ((EditText) findViewById(R.id.postBody)).getText().toString();
        Log.d(TAG, postBody);
        //validate input
        if (postBody.length() < 1 || postBody.length() >= Constants.POST_MAXLENGTH){
            Toast.makeText(this, "Please write between 1-" + Constants.POST_MAXLENGTH + " characters", Toast.LENGTH_SHORT).show();
            return;
        }
        //Strip excess white space

        //We are adding a post
        if (mParentPostID == null) {
            PostDatabaseHelper.addPost(postBody, this);
        }
        //We are adding a reply
        else{
            PostDatabaseHelper.addReply(postBody, mParentPostID, this);
        }
        finish();
    }
}
