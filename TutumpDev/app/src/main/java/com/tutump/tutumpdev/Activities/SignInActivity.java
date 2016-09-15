package com.tutump.tutumpdev.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.tutump.tutumpdev.Models.User;
import com.tutump.tutumpdev.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "FacebookLogin";
    ProgressDialog progress;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    // [START declare_auth_listener]
    private FirebaseAuth.AuthStateListener mAuthListener;
    // [END declare_auth_listener]

    private DatabaseReference mDatabase;

    private LoginButton loginButton;
    private CallbackManager callbackManager;

    private Bundle bFacebookData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialize facebook sdk
        facebookSDKInitialize();
        //LAUNCH LAYOUT
        setContentView(R.layout.activity_signin);

        //Views
        loginButton = (LoginButton)findViewById(R.id.login_button);


        // [START initialize_auth]
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        // [START auth_state_listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                    mDatabase = FirebaseDatabase.getInstance().getReference();
                    final String userId = user.getUid();
                    mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists())
                                    {
                                        Log.d(TAG, "USER EXIST");
                                        // [START_EXCLUDE]
                                        updateUI(user);
                                        // [END_EXCLUDE]
                                    }
                                    else
                                    {
                                        //Add new user
                                        writeNewUser(user.getUid(), user.getPhotoUrl().toString());
                                        // [START_EXCLUDE]
                                        updateUI(user);
                                        // [END_EXCLUDE]
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                                }
                            });
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }

            }
        };
        // [END auth_state_listener]

        callbackManager = CallbackManager.Factory.create();
        //require permissions for facebook
        loginButton.setReadPermissions(Arrays.asList("public_profile","email", "user_birthday", "user_education_history", "user_photos", "user_work_history"));
        getLoginDetails(loginButton);

    }

    //Write a new user
    private void writeNewUser(String userId, String profileImage) {

        bFacebookData.putBoolean("searchVisibility", true);
        bFacebookData.putBoolean("notificationNewMatch", true);
        bFacebookData.putBoolean("notificationsNewMessage", true);
        bFacebookData.putString("token", FirebaseInstanceId.getInstance().getToken());
        User user = new User(bFacebookData, profileImage);
        mDatabase.child("users").child(userId).setValue(user);
        Log.d(TAG, "USER ADDED:");
    }
    /*
    Initialize the facebook sdk.
    And then callback manager will handle the login responses.
    */
    protected void facebookSDKInitialize() {

        FacebookSdk.sdkInitialize(getApplicationContext());
    }

    /*
     Register a callback function with LoginButton to respond to the login result.
    */

    protected void getLoginDetails(LoginButton login_button){

        // Callback registration
        login_button.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),new GraphRequest.GraphJSONObjectCallback() {

                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.i("LoginActivity", response.toString());
                        // Get facebook data from login
                        bFacebookData = getFacebookData(object);
                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, first_name, last_name,gender, birthday, education, work"); // Parámetros que pedimos a facebook
                request.setParameters(parameters);
                request.executeAsync();
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                // code for cancellation
                Log.e(TAG, "cancel");
                finish();
            }

            @Override
            public void onError(FacebookException error) {
                //  code to handle error
                Log.e(TAG, "Error", error);
                finish();
            }
        });
    }

    private Bundle getFacebookData(JSONObject object) {

        JSONArray educationArray = null;
        String schoolName = "";
        JSONArray workArray = null;
        String workName = "";
        //Log.d(TAG, "COMPLETE OBJECT:" + object.toString());
        try {
            Bundle bundle = new Bundle();

            if (object.has("first_name"))
                bundle.putString("first_name", object.getString("first_name"));
            if (object.has("gender"))
                bundle.putString("gender", object.getString("gender"));
            if (object.has("birthday"))
                bundle.putString("birthday", object.getString("birthday"));
            if (object.has("education"))
                if(object.getJSONArray("education").length() > 0)
                {
                    educationArray = object.getJSONArray("education");
                    schoolName = educationArray.getJSONObject(0).getJSONObject("school").getString("name");
                    bundle.putString("education", schoolName);
                }
            if (object.has("work"))
                if(object.getJSONArray("work").length() > 0) {
                    workArray = object.getJSONArray("work");
                    workName = workArray.getJSONObject(0).getJSONObject("position").getString("name");
                    bundle.putString("work", workName);
                }

            return bundle;
        }
        catch(JSONException e){
            Log.d(TAG, "JSONException: " + e.toString());
            return null;
        }
    }
    // [START auth_with_facebook]
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        progress = ProgressDialog.show(this, "Logging in",
                "Loading...", true);
        loginButton.setVisibility(View.GONE);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        progress.dismiss();
                    }
                });


    }
    // [END auth_with_facebook]

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    protected void onStop() {
        super.onStop();  // Always call the superclass method first
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    private void updateUI(FirebaseUser user)
    {
        if(user != null)
        {
            mDatabase.child("users").child(user.getUid()).child("token").setValue(FirebaseInstanceId.getInstance().getToken());
            Intent TabsActivity = new Intent(SignInActivity.this, com.tutump.tutumpdev.Activities.TabsActivity.class);
            startActivity(TabsActivity);
            finish();
        }

    }

}
