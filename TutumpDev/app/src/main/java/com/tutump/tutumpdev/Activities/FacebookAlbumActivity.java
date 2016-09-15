package com.tutump.tutumpdev.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.tutump.tutumpdev.Adapter.AlbumAdapter;
import com.tutump.tutumpdev.Models.Album;
import com.tutump.tutumpdev.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FacebookAlbumActivity extends AppCompatActivity {

    private ListView mListView;
    private ArrayList<Album> albums = new ArrayList<Album>();
    private AlbumAdapter adapter;
    private static int RESULT_LOAD_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.activity_facebook_albums);

        ActionBar toolbar = getSupportActionBar();
        if(toolbar != null) {
            toolbar.setDisplayHomeAsUpEnabled(true);
        }

        mListView = (ListView) findViewById(R.id.recipe_list_view);

        getAlbums(this);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 1
                Album selectedAlbum = albums.get(position);

                Intent myIntent = new Intent(FacebookAlbumActivity.this, FacebookAlbumDetailActivity.class);
                myIntent.putExtra("AlbumId", selectedAlbum.getId());
                startActivityForResult(myIntent, RESULT_LOAD_IMAGE);
            }

        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("ImageURL", data.getStringExtra("ImageURL"));
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }

    }
    /*
    PRIVATE CLASS TO CALL THE FACEBOOK API AND BRING ALBUMS LIST
    */
    public void getAlbums(final Context context){

        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        if (object != null) {
                            try {
                                JSONObject obj = object.getJSONObject("albums");
                                JSONArray jArray = obj.getJSONArray("data");
                                for (int i = 0; i < jArray.length(); i++) {
                                    Album album = new Album();
                                    JSONObject dataObj = jArray.getJSONObject(i);
                                    album.setId(dataObj.getString("id"));
                                    album.setName(dataObj.getString("name"));
                                    album.setImageCount(dataObj.getString("count"));
                                    album.setImageUrl("https://graph.facebook.com/" + dataObj.getString("id") + "/picture?type=small"
                                            + "&access_token=" + AccessToken.getCurrentAccessToken().getToken());
                                    albums.add(album);
                                }
                                adapter = new AlbumAdapter(context, albums);
                                mListView.setAdapter(adapter);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else
                            Toast.makeText(FacebookAlbumActivity.this, "You do not have albums!", Toast.LENGTH_SHORT).show();
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "albums{id,cover_photo,name, count}");
        request.setParameters(parameters);
        request.executeAsync();

    }
    /*
    FINISH PRIVATE CLASS ASYNC
     */

}
