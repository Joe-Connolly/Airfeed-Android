package com.artfara.apps.kipper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Joe on 10/2/16.
 * Listview Adapter to Display Posts for chat
 */

public class ChatListViewAdapter extends BaseAdapter {
    private String mParentPostId;
    private ArrayList<Post> mPosts; //ArrayList to hold Entries in the Adapter
    private boolean mIsPost; //boolean to store if listview holds posts (rather than replies to a post)
    private Context c; //Holds context adapter was created in

    private static final String TAG = "  ChatListAdapter";
    private LayoutInflater mInflater;
    private SharedPreferences mPrefs;

    public ChatListViewAdapter(Context context, String parentPostId) {
        mInflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        c = context;
        mPosts = new ArrayList<>();
        mIsPost = (parentPostId == null);
        mParentPostId = parentPostId;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    public void setEntries(ArrayList<Post> posts){
        mPosts = posts;
//        PostDatabaseHelper.formatTime(posts);
//        if (mIsPost) {
//            PostDatabaseHelper.sortDescending(posts);
//        }
//        else{
//            PostDatabaseHelper.sortAscending(posts);
//            PostDatabaseHelper.setUserLetters(posts);
//        }
      notifyDataSetChanged();
    }
    public int getCount() {
        return mPosts.size();
    }

    public Object getItem(int position) {
        return mPosts.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    //Displays Name of Place, and #of people for each row in listView.
    public View getView(int position, View convertView, ViewGroup parent) {
        final boolean isPost = (position == 0 ? true : mIsPost);


        ViewHolder holder = new ViewHolder();

        final Post currentPost = mPosts.get(position);
        View rowView = mInflater.inflate(R.layout.post_custom_row_view, null);

        holder.postBody = (TextView) rowView.findViewById(R.id.postBody);
        holder.txtUserLetter = (TextView) rowView.findViewById(R.id.userLetter);
        holder.txtVoteCount = (TextView) rowView.findViewById(R.id.voteCount);
        holder.txtTime = (TextView) rowView.findViewById(R.id.time);
        holder.txtNumReplies = (TextView) rowView.findViewById(R.id.numReplies);
        holder.upVoteButton = (Button) rowView.findViewById(R.id.upVoteButton);
        holder.downVoteButton = (Button) rowView.findViewById(R.id.downVoteButton);


        holder.postBody.setText(currentPost.text);
        holder.txtUserLetter.setText((mIsPost ? "" : currentPost.userLetter));
        holder.txtVoteCount.setText("" + currentPost.voteCount);
        holder.txtTime.setText(currentPost.displayedTime + " ");


        holder.upVoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onUpClick called");
                if (alreadyVoted(currentPost.ID, true)){
                    return;
                }

                if (isPost){
                    PostDatabaseHelper.incrementPost(currentPost);
                }
                else{
                    PostDatabaseHelper.incrementReply(currentPost, mParentPostId);
                }
                notifyDataSetChanged();
            }
        });

        holder.downVoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onDownClick called");
                if (alreadyVoted(currentPost.ID, false)){
                    return;
                }
                if (isPost){
                    PostDatabaseHelper.decrementPost(currentPost);
                }
                else{
                    PostDatabaseHelper.decrementReply(currentPost, mParentPostId);
                }
                notifyDataSetChanged();
            }
        });

        if (mIsPost) {
            holder.txtNumReplies.setText(currentPost.replies.size() + " replies");
            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(c.getApplicationContext(), ChatReplyListViewActivity.class);
                    intent.putExtra(Constants.POST_ID_KEY, currentPost.ID);
                    c.startActivity(intent);
                }
            });
        }
        return rowView;
    }

    private boolean alreadyVoted(String id, boolean isUpVoting) {
        int amountVoted =  mPrefs.getInt(id, 0);
        amountVoted = (isUpVoting ? amountVoted + 1 : amountVoted - 1);
        if (amountVoted > 1 || amountVoted < -1){
            return true;
        }
        else{
            mPrefs.edit().putInt(id, amountVoted).apply();
            return false;
        }
    }


    //Empty class to hold both TextViews
    static class ViewHolder {
        TextView postBody;
        TextView txtTime;
        TextView txtVoteCount;
        TextView txtUserLetter;
        TextView txtNumReplies;
        Button upVoteButton;
        Button downVoteButton;
    }
}
