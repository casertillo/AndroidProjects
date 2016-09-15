package com.tutump.tutumpdev.Adapter;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tutump.tutumpdev.Models.User;
import com.tutump.tutumpdev.R;
import com.tutump.tutumpdev.Fragments.SettingsFragment;
import com.viewpagerindicator.UnderlinePageIndicator;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by casertillo on 30/07/16.
 */
public class SwipeDeskAdapter  extends BaseAdapter{
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<User> mDataSource;
    private FragmentManager mfragment;
    NumberFormat format = new DecimalFormat("#");

    public SwipeDeskAdapter(Context context, ArrayList<User> items, FragmentManager fragment) {
        mContext = context;
        mDataSource = items;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mfragment = fragment;
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
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        // 1
        if(convertView == null) {

            // 2
            convertView = mInflater.inflate(R.layout.cardview_matches, parent, false);

            // 3
            holder = new ViewHolder();
            holder.personName = (TextView) convertView.findViewById(R.id.person_name);
            holder.personTitle = (TextView) convertView.findViewById(R.id.person_title);
            holder.personAbout = (TextView) convertView.findViewById(R.id.person_about);

            holder.mPager = (ViewPager) convertView.findViewById(R.id.pager);
            holder.mIndicator = (UnderlinePageIndicator) convertView.findViewById(R.id.indicator);
            holder.mIndicator.setFades(false);
            // 4
            convertView.setTag(holder);
        }

        else{
        // 5
            holder = (ViewHolder) convertView.getTag();
        }
        User user = (User) getItem(position);
        ArrayList<String> stringArray = new ArrayList<String>();
        SettingsFragment.MyAdapter mAdapter;
        Map<String, Object> treeMap = new TreeMap<String, Object>(user.getPictures());
        for( String key: treeMap.keySet())
        {
             stringArray.add(user.getPictures().get(key).toString());
        }
        String[] stockArr = new String[user.getPictures().size()];
        stockArr = stringArray.toArray(stockArr);
        mAdapter = new SettingsFragment.MyAdapter(mfragment, stockArr);
// 6
        TextView personName = holder.personName;
        TextView personTitle = holder.personTitle;
        TextView personAbout = holder.personAbout;
        ViewPager mPager = holder.mPager;
        mPager.setOffscreenPageLimit(user.getPictures().size());
        UnderlinePageIndicator mIndicator = holder.mIndicator;

        personName.setText(user.getName()+", "+ format.format(user.getAge()));
        if(user.getWork() != null && user.getEducation() != null) {
            personTitle.setText(user.getWork()+", "+user.getEducation());
        }
        else if (user.getWork() != null && user.getEducation() == null) {
            personTitle.setText(user.getWork());
        }
        else if(user.getWork() == null && user.getEducation() != null){
            personTitle.setText(user.getEducation());
        }
        personAbout.setText(user.getAbout());

        mPager.setAdapter(mAdapter);
        mIndicator.setViewPager(mPager);

        return convertView;
    }
    private static class ViewHolder {
        public TextView personName;
        public TextView personTitle;
        public TextView personAbout;
        public ViewPager mPager;
        public UnderlinePageIndicator mIndicator;
    }
}
