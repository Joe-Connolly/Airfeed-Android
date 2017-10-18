package com.artfara.apps.kipper;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.artfara.apps.kipper.models.Account;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class RankFragment extends Fragment {
    private Account mAccount;
    private static final String TAG = "Rank Fragment ";

    public RankFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FirebaseDatabase.getInstance().getReference()
                .child(Globals.USERS_ACCOUNTS_TABLE_NAME).child(Utils.getAndroidID(getContext()))
                .addListenerForSingleValueEvent(mAccountsSingleEventListener);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rank, container, false);
    }


    private ValueEventListener mAccountsSingleEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mAccount = dataSnapshot.getValue(Account.class);
            Log.d(TAG, "account " + dataSnapshot.getKey());
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(TAG, databaseError.getMessage());
        }
    };

}
