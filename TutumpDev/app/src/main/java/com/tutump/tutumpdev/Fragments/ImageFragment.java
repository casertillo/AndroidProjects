package com.tutump.tutumpdev.Fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.tutump.tutumpdev.R;

/**
 * Created by casertillo on 26/06/16.
 */
public class ImageFragment extends Fragment{
    int fragVal;
    static ImageFragment init(int val, String[] images) {
        ImageFragment truitonFrag = new ImageFragment();
        // Supply val input as an argument.
        Bundle args = new Bundle();
        args.putInt("val", val);
        args.putStringArray("images", images);
        truitonFrag.setArguments(args);
        return truitonFrag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragVal = getArguments() != null ? getArguments().getInt("val") : 1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layoutView = inflater.inflate(R.layout.fragment_image, container,
                false);
        ImageView imageView = (ImageView) layoutView.findViewById(R.id.imageView);
        Bundle bundle = getArguments();
        int position = bundle.getInt("val");
        String[] images = bundle.getStringArray("images");
        String imageFileName = images[position];

        //Check if image is default
        if(imageFileName.length() <= 20) {
            int imgResId = getResources().getIdentifier(imageFileName, "drawable", "com.tutump.tutumpdev");
            imageView.setImageResource(imgResId);
        }
        else {
            //imageView.setImageBitmap(decodeBase64(imageFileName));
            Glide.with(this)
                    .load(imageFileName)
                    .into(imageView);
        }
        return layoutView;
    }
    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}
