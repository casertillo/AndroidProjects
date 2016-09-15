package com.tutump.tutumpdev.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tutump.tutumpdev.Models.Album;
import com.tutump.tutumpdev.R;

import java.util.ArrayList;

/**
 * Created by casertillo on 25/07/16.
 * Album adapter is used to assign the album values from the facebook graph.
 */
public class AlbumAdapter extends BaseAdapter{
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Album> mDataSource;

    public AlbumAdapter(Context context, ArrayList<Album> items) {
        mContext = context;
        mDataSource = items;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    //1
    @Override
    public int getCount() {
        return mDataSource.size();
    }

    //2
    @Override
    public Object getItem(int position) {
        return mDataSource.get(position);
    }

    //3
    @Override
    public long getItemId(int position) {
        return position;
    }

    //4
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

// 1
        if(convertView == null) {

            // 2
            convertView = mInflater.inflate(R.layout.list_item_recipe, parent, false);

            // 3
            holder = new ViewHolder();
            holder.thumbnailImageView = (ImageView) convertView.findViewById(R.id.recipe_list_thumbnail);
            holder.titleTextView = (TextView) convertView.findViewById(R.id.recipe_list_title);
            holder.subtitleTextView = (TextView) convertView.findViewById(R.id.recipe_list_subtitle);

            // 4
            convertView.setTag(holder);
        }
        else{
            // 5
            holder = (ViewHolder) convertView.getTag();
        }

// 6
        TextView titleTextView = holder.titleTextView;
        TextView subtitleTextView = holder.subtitleTextView;
        ImageView thumbnailImageView = holder.thumbnailImageView;
        Album album = (Album) getItem(position);

// 2
        titleTextView.setText(album.getName() );
        subtitleTextView.setText(album.getImageCount());

// 3
        Glide.with(mContext)
                .load(album.getImageUrl())
                .into(thumbnailImageView);

        return convertView;
    }
    private static class ViewHolder {
        public TextView titleTextView;
        public TextView subtitleTextView;
        public ImageView thumbnailImageView;
    }
}
