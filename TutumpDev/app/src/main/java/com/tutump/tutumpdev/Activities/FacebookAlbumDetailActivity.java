package com.tutump.tutumpdev.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.tutump.tutumpdev.Adapter.ImageAdapter;
import com.tutump.tutumpdev.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class FacebookAlbumDetailActivity extends AppCompatActivity {

    private String AlbumId;
    ArrayList<String> pictures = new ArrayList<String>();
    private ImageAdapter imageAdapter;
    private GridView gridView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.activity_facebook_album_detail);

        ActionBar toolbar = getSupportActionBar();
        if(toolbar != null) {
            toolbar.setDisplayHomeAsUpEnabled(true);
        }


        gridView = (GridView) findViewById(R.id.gridview);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            AlbumId = extras.getString("AlbumId");
            //The key argument here must match that used in the other activity
            GetFacebookImages(this);
        }
        else
        {
            Toast.makeText(FacebookAlbumDetailActivity.this, "Error getting album information",
                    Toast.LENGTH_SHORT).show();
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                //Toast.makeText(FacebookAlbumDetailActivity.this, "" + position,
                //        Toast.LENGTH_SHORT).show();

                Intent returnIntent = new Intent();
                returnIntent.putExtra("ImageURL", pictures.get(position).toString());
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });



    }
    public void GetFacebookImages(final Context context) {
//        String url = "https://graph.facebook.com/" + "me" + "/"+albumId+"/photos?access_token=" + AccessToken.getCurrentAccessToken() + "&fields=images";
        Bundle parameters = new Bundle();
        parameters.putString("fields", "images");
        /* make the API call */
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/" + AlbumId + "/photos",
                parameters,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
            /* handle the result */
                        try {
                            if (response.getError() == null) {

                                JSONObject joMain = response.getJSONObject();
                                if (joMain.has("data")) {
                                    try {
                                        JSONArray jArray = joMain.optJSONArray("data");
                                        for (int i = 0; i < jArray.length(); i++){
                                            JSONObject dataObj = jArray.getJSONObject(i);
                                            JSONArray jaImages = dataObj.getJSONArray("images");
                                            pictures.add(jaImages.getJSONObject(0).getString("source"));
                                        }

                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }

                                }
                                //set your adapter here
                                imageAdapter = new ImageAdapter(context, pictures);
                                gridView.setAdapter(imageAdapter);
                            }
                        else {
                            Log.v("TAG", response.getError().toString());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
    }
    ).executeAsync();
}
}
