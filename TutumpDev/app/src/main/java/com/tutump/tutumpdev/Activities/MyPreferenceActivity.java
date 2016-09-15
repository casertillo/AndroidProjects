package com.tutump.tutumpdev.Activities;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tutump.tutumpdev.R;

/**
 * Created by casertillo on 27/06/16.
 */
public class MyPreferenceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new MyPreferenceFragment())
                .commit();

        ActionBar toolbar = getSupportActionBar();
        if(toolbar != null) {
            toolbar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        DatabaseReference mDatabase;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

            Preference ageRange = findPreference("age_range");
            ageRange.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    return true;
                }
            });
            Preference myPref = (Preference) findPreference("logout");
            myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //open browser or intent here
                    return true;
                }
            });

            final Preference visibility = (Preference) findPreference("search_visibility");
            visibility.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    //Reference to database
                    mDatabase = FirebaseDatabase.getInstance().getReference();
                    mDatabase.child("users").child(user.getUid()).child("searchVisibility").setValue(o);
                    return true;
                }
            });
            final Preference newMatch = (Preference) findPreference("notification_new_Match");
            newMatch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    //Reference to database
                    mDatabase = FirebaseDatabase.getInstance().getReference();
                    mDatabase.child("users").child(user.getUid()).child("notificationNewMatch").setValue(o);
                    return true;
                }
            });
            final Preference newMessage = (Preference) findPreference("notifications_new_message");
            newMessage.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    //Reference to database
                    mDatabase = FirebaseDatabase.getInstance().getReference();
                    mDatabase.child("users").child(user.getUid()).child("notificationsNewMessage").setValue(o);
                    return true;
                }
            });

        }
    }


}
