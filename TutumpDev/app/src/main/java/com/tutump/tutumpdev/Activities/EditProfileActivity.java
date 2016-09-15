package com.tutump.tutumpdev.Activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tutump.tutumpdev.Models.CurrentUser;
import com.tutump.tutumpdev.R;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    private static int RESULT_LOAD_IMAGE = 1;
    private static int RESULT_LOAD_IMAGE_FACEBOOK = 2;
    private static String clicked;

    private EditText aboutText;
    private DatabaseReference mDatabase;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    Map<String, Object> updates = new HashMap<String, Object>();
    private ProgressDialog mProgressDialog;
    private Uri mDownloadUrl = null;

    private ImageButton addButton1;
    private ImageButton addButton2;
    private ImageButton addButton3;
    private ImageButton addButton4;
    private ImageButton addButton5;
    private ImageButton addButton6;

    private ImageButton deleteButton1;
    private ImageButton deleteButton2;
    private ImageButton deleteButton3;
    private ImageButton deleteButton4;
    private ImageButton deleteButton5;
    private ImageButton deleteButton6;

    // [START declare_ref]
    private StorageReference mStorageRef;
    // [END declare_ref]

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Reference to database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // [START get_storage_ref]
        mStorageRef = FirebaseStorage.getInstance().getReference();
        // [END get_storage_ref]

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        ActionBar toolbar = getSupportActionBar();
        if(toolbar != null) {
            toolbar.setDisplayHomeAsUpEnabled(true);
        }
        aboutText = (EditText)findViewById(R.id.aboutText);

        aboutText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mDatabase.child("users").child(user.getUid()).child("about").setValue(aboutText.getText().toString());
                CurrentUser.getCurrentUser().setAbout(aboutText.getText().toString());
            }
        });
        if(CurrentUser.getCurrentUser().getAbout() != "")
        {
            aboutText.setText(CurrentUser.getCurrentUser().getAbout());
        }

        addButton1 = (ImageButton) findViewById(R.id.imageAddB_1);
        addButton2 = (ImageButton) findViewById(R.id.imageAddB_2);
        addButton3 = (ImageButton) findViewById(R.id.imageAddB_3);
        addButton4 = (ImageButton) findViewById(R.id.imageAddB_4);
        addButton5 = (ImageButton) findViewById(R.id.imageAddB_5);
        addButton6 = (ImageButton) findViewById(R.id.imageAddB_6);

        addButton1.setOnClickListener(dialogListener);
        addButton2.setOnClickListener(dialogListener);
        addButton3.setOnClickListener(dialogListener);
        addButton4.setOnClickListener(dialogListener);
        addButton5.setOnClickListener(dialogListener);
        addButton6.setOnClickListener(dialogListener);

        deleteButton1 = (ImageButton) findViewById(R.id.imageDeleteB_1);
        deleteButton2 = (ImageButton) findViewById(R.id.imageDeleteB_2);
        deleteButton3 = (ImageButton) findViewById(R.id.imageDeleteB_3);
        deleteButton4 = (ImageButton) findViewById(R.id.imageDeleteB_4);
        deleteButton5 = (ImageButton) findViewById(R.id.imageDeleteB_5);
        deleteButton6 = (ImageButton) findViewById(R.id.imageDeleteB_6);

        deleteButton1.setOnClickListener(deleteListener);
        deleteButton2.setOnClickListener(deleteListener);
        deleteButton3.setOnClickListener(deleteListener);
        deleteButton4.setOnClickListener(deleteListener);
        deleteButton5.setOnClickListener(deleteListener);
        deleteButton6.setOnClickListener(deleteListener);

        //Check if there are images
        if(CurrentUser.getCurrentUser().getdashboardPicturess() != null)
        {
            for (Map.Entry<String, Object> entry : CurrentUser.getCurrentUser().getdashboardPicturess().entrySet() ) {
                int resID = getResources().getIdentifier(entry.getKey(), "id", "com.tutump.tutumpdev");
                ImageView imageView = (ImageView) findViewById(resID);
                Glide.with(EditProfileActivity.this)
                        .load(entry.getValue().toString())
                        .into(imageView);
            }
        }

    }

    private View.OnClickListener dialogListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            //get the name of the clicked button to specify the one clicked
            //I have multiple Imageviews inside
            clicked = view.getResources().getResourceName(view.getId());
            clicked = clicked.substring(clicked.indexOf("_")+1, clicked.length());
            final Dialog dialog = new Dialog(EditProfileActivity.this);
            dialog.setContentView(R.layout.dialog_gallery_options);
            dialog.setTitle("Gallery Options");

            Button gallery = (Button) dialog.findViewById(R.id.buttonGallery);
            gallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, RESULT_LOAD_IMAGE);
                }
            });

            Button facebook = (Button) dialog.findViewById(R.id.buttonFacebook);
            facebook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent myIntent = new Intent(EditProfileActivity.this, FacebookAlbumActivity.class);
                    startActivityForResult(myIntent, RESULT_LOAD_IMAGE_FACEBOOK);
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    };

    private View.OnClickListener deleteListener = new View.OnClickListener(){
        @Override
        public void onClick(View view){
            clicked = view.getResources().getResourceName(view.getId());
            clicked = "iv_" + clicked.substring(clicked.indexOf("_")+1, clicked.length());
            CurrentUser.getCurrentUser().removeDashboardPictures(clicked);
            mDatabase.child("users").child(user.getUid()).child("pictures").child(clicked).removeValue();
            int resID = getResources().getIdentifier(clicked, "id", "com.tutump.tutumpdev");
            ImageView imageView = (ImageView) findViewById(resID);
            int imgResId = getResources().getIdentifier("defaultprofile", "drawable", "com.tutump.tutumpdev");
            imageView.setImageResource(imgResId);
        }
    };
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE_FACEBOOK && resultCode == RESULT_OK && null != data) {
            try{
                String selectedImage = data.getStringExtra("ImageURL");
                clicked = "iv_"+clicked;
                int resID = getResources().getIdentifier(clicked, "id", "com.tutump.tutumpdev");
                ImageView imageView = (ImageView) findViewById(resID);
                mDatabase.child("users").child(user.getUid()).child("pictures").child(clicked).setValue(selectedImage);
                Glide.with(EditProfileActivity.this)
                        .load(selectedImage)
                        .into(imageView);
            }catch(Exception e){
                Log.d(TAG, e.toString());
            }
/*            try
            {
            String faceUrl = data.getStringExtra("ImageURL");
            Uri imageFace = Uri.parse(faceUrl);
            clicked = "iv_"+clicked;
            int resID = getResources().getIdentifier(clicked, "id", "com.tutump.tutumpdev");
            ImageView imageView = (ImageView) findViewById(resID);
            Glide.with(this)
                    .load(faceUrl)
                    .into(imageView);

                new AsyncTask<String, Void, Void>() {
                    @Override
                    protected Void doInBackground(String... params) {
                        if (Looper.myLooper()==null)
                            Looper.prepare();
                        try {

                            URL img_value = null;
                            img_value = new URL(params[0]);
                            Bitmap mIcon1 = BitmapFactory.decodeStream(img_value.openConnection().getInputStream());
                            String image = encodeToBase64(mIcon1,  Bitmap.CompressFormat.JPEG, 50);
                            mDatabase.child("users").child(user.getUid()).child("pictures").child(clicked).setValue(image);
                            CurrentUser.getCurrentUser().addPicture(image);

                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                        return null;
                    }
                    @Override
                    protected void onPostExecute(Void dummy) {

                    }
                }.execute(faceUrl);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
*/
        }

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            try{
                Uri selectedImage = data.getData();
                clicked = "iv_"+clicked;
                int resID = getResources().getIdentifier(clicked, "id", "com.tutump.tutumpdev");
                ImageView imageView = (ImageView) findViewById(resID);
                uploadFromUri(selectedImage, clicked, imageView);
            }catch(Exception e){
                Log.d(TAG, e.toString());
            }
        }

    }
    public String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality)
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, bos);
        return Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    // [START upload_from_uri]
    private void uploadFromUri(Uri fileUri, final String clicked, final ImageView imageView) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        // [START get_child_ref]
        // Get a reference to store file at photos/<FILENAME>.jpg
        final StorageReference photoRef = mStorageRef.child("photos")
                .child(user.getUid())
                .child(clicked);
        // [END get_child_ref]

        // Upload file to Firebase Storage
        // [START_EXCLUDE]
        showProgressDialog();
        // [END_EXCLUDE]
        Log.d(TAG, "uploadFromUri:dst:" + photoRef.getPath());
        photoRef.putFile(fileUri)
                .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Upload succeeded
                        Log.d(TAG, "uploadFromUri:onSuccess");

                        // Get the public download URL
                        mDownloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
                        mDatabase.child("users").child(user.getUid()).child("pictures").child(clicked).setValue(mDownloadUrl.toString());

                        Glide.with(EditProfileActivity.this)
                                .load(mDownloadUrl.toString())
                                .into(imageView);

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Upload failed
                        Log.w(TAG, "uploadFromUri:onFailure", exception);

                        mDownloadUrl = null;

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        Toast.makeText(EditProfileActivity.this, "Error: upload failed",
                                Toast.LENGTH_SHORT).show();
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END upload_from_uri]
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }
    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
