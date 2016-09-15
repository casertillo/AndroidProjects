package com.tutump.tutumpdev.Fragments;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tutump.tutumpdev.Activities.EditProfileActivity;
import com.tutump.tutumpdev.Activities.MyPreferenceActivity;
import com.tutump.tutumpdev.Fragments.ImageFragment;
import com.tutump.tutumpdev.Models.CurrentUser;
import com.tutump.tutumpdev.R;
import com.tutump.tutumpdev.TutumpClasses;
import com.viewpagerindicator.UnderlinePageIndicator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by casertillo on 19/06/16.
 */
public class SettingsFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "SettingsFragment";
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    MyAdapter mAdapter;
    ViewPager mPager;
    UnderlinePageIndicator mIndicator;
    ImageButton settingsButton;
    ImageButton editButton;
    TextView nameText;
    TextView jobText;
    TextView schoolText;
    View view;
    CircleImageView profileImage;

    private DatabaseReference mDatabase;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Query queryRef = mDatabase.child("users").child(user.getUid()).child("pictures");
        mDatabase.child("users").child(user.getUid()).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        Map<String, Object> userValues = (Map<String, Object>) dataSnapshot.getValue();

                        if(userValues.containsKey("dateOfBirth"))
                        {
                            String dtStart = userValues.get("dateOfBirth").toString();
                            DateFormat format = new SimpleDateFormat("MM/dd/yyyy");

                            try {
                                Calendar cal = Calendar.getInstance();
                                Date date = format.parse(dtStart);
                                cal.setTime(date);
                                CurrentUser.getCurrentUser().setAge(new TutumpClasses.StaticTutump().getAge(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)));
                                mDatabase.child("users").child(user.getUid()).child("Age").setValue(Double.parseDouble(CurrentUser.getCurrentUser().getAge()));
                            } catch (ParseException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                        //Asign values to the singleton user class
                        CurrentUser.getCurrentUser().setName(userValues.get("name").toString());

                        CurrentUser.getCurrentUser().setProfilePicture(user.getPhotoUrl());

                        if(userValues.containsKey("work"))
                        {
                            CurrentUser.getCurrentUser().setJob(userValues.get("work").toString());
                        }
                        if(userValues.containsKey("education"))
                        {
                            CurrentUser.getCurrentUser().setEducation(userValues.get("education").toString());

                        }
                        if(userValues.containsKey("about"))
                        {
                            CurrentUser.getCurrentUser().setAbout(userValues.get("about").toString());
                        }
                        ArrayList<String> stringArray = new ArrayList<String>();
                        if(userValues.containsKey("pictures"))
                        {
                            //Retreive all pictures URL's
                            Map<String, Object> pictures = (Map<String, Object>) userValues.get("pictures");
                            Map<String, Object> treeMap = new TreeMap<String, Object>(pictures);
                            for( String key: treeMap.keySet())
                            {
                                stringArray.add(pictures.get(key).toString());
                            }
                        }
                        else
                        {
                            stringArray.add("defaultprofile");
                        }
                        if(userValues.containsKey("viewed"))
                        {
                            Map<String, String> pictures = (Map<String, String>) userValues.get("viewed");
                            CurrentUser.getCurrentUser().setViewedProfiles(pictures.values());
                        }
                        CurrentUser.getCurrentUser().setPictures(stringArray);

                        String[] stockArr = new String[CurrentUser.getCurrentUser().getPictures().size()];
                        stockArr = CurrentUser.getCurrentUser().getPictures().toArray(stockArr);

                        Glide.with(getActivity())
                                .load(CurrentUser.getCurrentUser().getProfilePicture())
                                .into(profileImage);
                        nameText.setText(CurrentUser.getCurrentUser().getName() +", "+ CurrentUser.getCurrentUser().getAge());
                        jobText.setText(CurrentUser.getCurrentUser().getJob());
                        schoolText.setText(CurrentUser.getCurrentUser().getEducation());

                        mAdapter = new MyAdapter(getChildFragmentManager(), stockArr);
                        mPager = (ViewPager) view.findViewById(R.id.pager);
                        mPager.setAdapter(mAdapter);
                        mIndicator = (UnderlinePageIndicator) view.findViewById(R.id.indicator);
                        mIndicator.setFades(false);
                        mIndicator.setViewPager(mPager);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });

        ValueEventListener listener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Map<String, Object> userValues = (Map<String, Object>) dataSnapshot.getValue();
                ArrayList<String> stringArray = new ArrayList<String>();

                if(userValues != null)
                {
                    Log.d(TAG, "USER CHANGED PHOTOS");
                    //Retreive all pictures URL's
                    Map<String, Object> pictures = (Map<String, Object>) userValues;
                    CurrentUser.getCurrentUser().setdashboardPictures(pictures);
                    Map<String, Object> orderPicture = new TreeMap<String, Object>(pictures);
                    for( String key: orderPicture.keySet())
                    {
                        stringArray.add(pictures.get(key).toString());
                    }
                }
                else
                {
                    stringArray.add("defaultprofile");
                }
                CurrentUser.getCurrentUser().setPictures(stringArray);
                String[] stockArr = new String[CurrentUser.getCurrentUser().getPictures().size()];
                stockArr = CurrentUser.getCurrentUser().getPictures().toArray(stockArr);
                mAdapter = new MyAdapter(getChildFragmentManager(), stockArr);
                mPager = (ViewPager) view.findViewById(R.id.pager);
                mPager.setAdapter(mAdapter);
                mIndicator = (UnderlinePageIndicator) view.findViewById(R.id.indicator);
                mIndicator.setFades(false);
                mIndicator.setViewPager(mPager);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        queryRef.addValueEventListener(listener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        view = inflater.inflate(R.layout.fragment_settings, container, false);

        settingsButton = (ImageButton) view.findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(this);

        editButton = (ImageButton) view.findViewById(R.id.logoutButton);
        editButton.setOnClickListener(this);

        //Variables
        nameText = (TextView) view.findViewById(R.id.nameText);
        jobText = (TextView) view.findViewById(R.id.jobText );
        schoolText = (TextView) view.findViewById(R.id.schoolText);
        profileImage = (CircleImageView) view.findViewById(R.id.profile_image);

        return view;
    }
    @Override
    public void onClick(View v) {
        //do what you want to do when button is clicked
        Intent myIntent = new Intent(getActivity(), MyPreferenceActivity.class);
        switch (v.getId()) {

            case R.id.settingsButton:
                myIntent = new Intent(getActivity(), MyPreferenceActivity.class);
                getActivity().startActivity(myIntent);
                break;
            case R.id.logoutButton:
                myIntent = new Intent(getActivity(), EditProfileActivity.class);
                getActivity().startActivity(myIntent);
                break;
        }
    }

    public static class MyAdapter extends FragmentStatePagerAdapter{

        String[] AllImages;

        public MyAdapter(FragmentManager fragmentManager, String[] images){
            super(fragmentManager);
            AllImages = images;
        }

        @Override
        public int getCount(){
            return AllImages.length;
        }

        @Override
        public Fragment getItem(int position)
        {
            return ImageFragment.init(position, AllImages);
        }
    }
}
