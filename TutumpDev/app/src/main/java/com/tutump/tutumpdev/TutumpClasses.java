package com.tutump.tutumpdev;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by casertillo on 22/07/16.
 */
public class TutumpClasses {

    public static class StaticTutump{
        private static final String TAG = "TutumpClass";
        public String getAge(int year, int month, int day){
            Calendar dob = Calendar.getInstance();
            Calendar today = Calendar.getInstance();

            dob.set(year, month, day);

            int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
                age--;
            }

            Integer ageInt = new Integer(age);
            String ageS = ageInt.toString();

            return ageS;
        }
        public void setDefaultPreferences(Context context)
        {
            SharedPreferences prefs =  context.getSharedPreferences(context.getPackageName()+"_preferences", Context.MODE_PRIVATE);
            Boolean searchMen = prefs.getBoolean("search_men", false);
            Boolean searchWomen = prefs.getBoolean("search_women", false);
            Boolean searchByDistance = prefs.getBoolean("search_by_distance", false);
            Integer searchDistanceRadio = prefs.getInt("search_distance_radio", 5);
            Boolean visible = prefs.getBoolean("search_visibility", false);
            Boolean newMatch = prefs.getBoolean("notification_new_Match", false);
            Boolean newMessage = prefs.getBoolean("notifications_new_message", false);
            Set<String> ageRange = new HashSet<>();
            ageRange.add("18");
            ageRange.add("55");
            Set<String> ageRangePref = new HashSet<>();
            ageRangePref = prefs.getStringSet("age_range", ageRange);

            /*
            Log.d(TAG,"Search Men: " + searchMen.toString());
            Log.d(TAG,"Search WoMen: " + searchWomen.toString());
            Log.d(TAG,"Search By Distance: " + searchByDistance.toString());
            Log.d(TAG,"Search Distance Radio: " + searchDistanceRadio.toString());
            Log.d(TAG,"New Match: " + newMatch.toString());
            Log.d(TAG,"New Message: " + newMessage.toString());
            Log.d(TAG,"Visible : " + visible.toString());
            List<String> list = new ArrayList<String>(ageRangePref);
            Log.d(TAG,"Max : " + list.get(0) );
            Log.d(TAG,"Min : " + list.get(1));
            */
        }

    }
}
