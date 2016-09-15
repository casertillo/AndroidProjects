package com.tutump.tutumpdev.Activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.server.converter.StringToIntConverter;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.vision.text.Text;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.iid.FirebaseInstanceId;
import com.tutump.tutumpdev.Activities.SignInActivity;
import com.tutump.tutumpdev.Fragments.MatchFragment;
import com.tutump.tutumpdev.Fragments.MessagesFragment;
import com.tutump.tutumpdev.Fragments.SettingsFragment;
import com.tutump.tutumpdev.R;
import com.tutump.tutumpdev.TutumpClasses;

public class TabsActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    //General class variables
    private static final String TAG = "MainActivity";

    //Firebase authentication
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    //Tabs adapter and layout
    private TabLayout tabLayout;
    private ViewPager viewPager;



    int settings = R.drawable.ic_face_black_48dp;
    int match = R.drawable.ic_favorite_black_48dp;
    int messages = R.drawable.ic_chat_black_48dp;

    Location mLastLocation;

    private int[] tabIcons = {
            settings,
            match,
            messages
    };

    private GoogleApiClient mGoogleApiClient;
    final private int REQUEST_CODE_LOCATION_PERMISSION = 124;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        TutumpClasses.StaticTutump tutumpClass = new TutumpClasses.StaticTutump();

        tutumpClass.setDefaultPreferences(this);

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_tabs);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(2);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        setupViewPager(viewPager);
        setupTabIcons();

        //ADD COORDINATES
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        Log.d(TAG, "FCM token: " + FirebaseInstanceId.getInstance().getToken());
    }

    //END CURRENT USER
    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
    }
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new SettingsFragment(), "Settings");
        adapter.addFragment(new MatchFragment(), "Match");
        adapter.addFragment(new MessagesFragment(), "Messages");
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1);
    }


    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // An unresolvable error has occurred and a connection to Google APIs
        // could not be established. Display an error message, or handle
        // the failure silently

        // ...
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE_LOCATION_PERMISSION);

            }
        }
        else
        {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {
                DatabaseReference mDatabase;
                //Reference to database
                mDatabase = FirebaseDatabase.getInstance().getReference();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                mDatabase.child("users").child(user.getUid()).child("Lat").setValue(mLastLocation.getLatitude());
                mDatabase.child("users").child(user.getUid()).child("Lng").setValue(mLastLocation.getLongitude());
            }

        }
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.d("Connection suspened", "Connection suspended");
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

}
