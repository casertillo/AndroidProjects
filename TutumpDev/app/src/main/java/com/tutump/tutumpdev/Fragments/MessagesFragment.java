package com.tutump.tutumpdev.Fragments;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.tutump.tutumpdev.Activities.ChatActivity;
import com.tutump.tutumpdev.Adapter.MatchAdapter;
import com.tutump.tutumpdev.ChatListDecoration;
import com.tutump.tutumpdev.Models.Match;
import com.tutump.tutumpdev.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by casertillo on 19/06/16.
 */
public class MessagesFragment extends Fragment {

    View view;

    private List<Match> matchList = new ArrayList<>();
    private RecyclerView recyclerView;
    private MatchAdapter mAdapter;
    private DatabaseReference mDatabase;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    ChildEventListener listener;
    Query queryRef;

    public MessagesFragment(){

    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();


    }

    @Override
    public void onStart() {
        super.onStart();
        queryRef = mDatabase.child("users").child(user.getUid()).child("matches");
        listener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Match match = dataSnapshot.getValue(Match.class);
                matchList.add(match);
                mAdapter = new MatchAdapter(getContext(), matchList, new MatchAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Match item) {
                        Intent myIntent = new Intent(getActivity(), ChatActivity.class);
                        myIntent.putExtra("userName", item.getName());
                        myIntent.putExtra("userPhoto", item.getPhotoUrl());
                        myIntent.putExtra("userId", item.getId());
                        myIntent.putExtra("chatId", item.getChatId());
                        myIntent.putExtra("token", item.getToken());
                        getActivity().startActivity(myIntent);
                    }
                });
                recyclerView.setAdapter(mAdapter);
                recyclerView.addItemDecoration(new ChatListDecoration(1));
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        queryRef.addChildEventListener(listener);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Remove listener
        if(listener !=null) {
            // Remove listener
            queryRef.removeEventListener(listener);
        }
        // Clean chat message
        if(mAdapter != null) {
            mAdapter.cleanUp();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        view = inflater.inflate(R.layout.fragment_messages, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.matchesRecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        return view;
    }
}


