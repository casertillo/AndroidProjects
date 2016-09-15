package com.tutump.tutumpdev.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Created by casertillo on 25/07/16.
 */
public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<String> mDataSource;

    public ImageAdapter(Context context, ArrayList<String> items){
        this.mContext = context;
        this.mDataSource = items;
    }
    public int getCount() {
        return this.mDataSource.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, 150));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }

        Glide.with(mContext)
                .load(mDataSource.get(position))
                .into(imageView);

        return imageView;
    }
}
