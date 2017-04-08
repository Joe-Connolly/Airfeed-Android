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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PostActivity extends AppCompatActivity {
    private static final String TAG = " PostActivity ";
    private String mParentPostID;
    private DatabaseReference mDatabase;
    private String mPostBody;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mDatabase = FirebaseDatabase.getInstance().getReference();
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

        mPostBody = ((EditText) findViewById(R.id.postBody)).getText().toString();
        Log.d(TAG, mPostBody);
        //validate input
        if (mPostBody.length() < 1 || mPostBody.length() >= Constants.POST_MAXLENGTH){
            Toast.makeText(this, "Please write between 1-" + Constants.POST_MAXLENGTH + " characters", Toast.LENGTH_SHORT).show();
            return;
        }

        //Make sure user is valid before posting
        mDatabase.child(Constants.BANNED_USERS_TABLE_NAME).child(Utils.getUserID(this))
                .addListenerForSingleValueEvent(mIsUserBannedListener);
    }

    private ValueEventListener mIsUserBannedListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            Log.d(TAG, " add reply ");
            String bannedString = dataSnapshot.getValue(String.class);
            //User is valid
            if (bannedString == null){
                //We are adding a post
                if (mParentPostID == null) {
                    PostDatabaseHelper.addPost(mPostBody, PostActivity.this);
                }
                //We are adding a reply
                else{
                    PostDatabaseHelper.addReply(mPostBody, mParentPostID, PostActivity.this);
                }
                finish();
            }
            //User is banned
            else{
                Toast.makeText(PostActivity.this, " Your account is banned", Toast.LENGTH_SHORT).show();
            }
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };
}
