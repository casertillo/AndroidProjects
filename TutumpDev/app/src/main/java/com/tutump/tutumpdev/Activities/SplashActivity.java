package com.tutump.tutumpdev.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by casertillo on 17/06/16.
 */
public class SplashActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent TabsActivity = new Intent(this, com.tutump.tutumpdev.Activities.TabsActivity.class);
        startActivity(TabsActivity);
        finish();
    }
}
