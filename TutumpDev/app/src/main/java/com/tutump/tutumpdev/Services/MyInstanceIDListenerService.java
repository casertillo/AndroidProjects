package com.tutump.tutumpdev.Services;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Created by casertillo on 08/08/16.
 */
public class MyInstanceIDListenerService extends FirebaseInstanceIdService {
    private static final String TAG = "MyFirebaseIIDService";
    private static final String FRIENDLY_ENGAGE_TOPIC = "friendly_engage";
    /**
     * The Application's current Instance ID token is no longer valid
     * and thus a new one must be requested.
     */
    @Override
    public void onTokenRefresh() {
        // If you need to handle the generation of a token, initially or
        // after a refresh this is where you should do that.
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String token = FirebaseInstanceId.getInstance().getToken();
        if(user != null) {
            mDatabase.child("users").child(user.getUid()).child("token").setValue(token);
        }
        // Once a token is generated, we subscribe to topic.
        FirebaseMessaging.getInstance()
                .subscribeToTopic(FRIENDLY_ENGAGE_TOPIC);
    }
}
