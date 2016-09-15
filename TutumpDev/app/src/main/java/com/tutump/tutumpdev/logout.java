package com.tutump.tutumpdev;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by casertillo on 15/07/16.
 */
public class logout extends Preference{

    public logout(Context context){
        super(context);
    }
    public logout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.logout_button);
    }

    @Override
    protected void onBindView(View view) {
        View button = view.findViewById(R.id.logoutButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FacebookSdk.sdkInitialize(getContext());
                LoginManager.getInstance().logOut();
                FirebaseAuth.getInstance().signOut();
                Intent SignInActivity = new Intent(v.getContext(), com.tutump.tutumpdev.Activities.SignInActivity.class);
                getContext().startActivity(SignInActivity);
            }
        });
        super.onBindView(view);
    }
}
