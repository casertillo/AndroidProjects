package com.tutump.tutumpdev.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tutump.tutumpdev.Models.Match;
import com.tutump.tutumpdev.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by casertillo on 05/08/16.
 */
public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MyViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Match item);
    }

    private List<Match> matchList;
    private final OnItemClickListener listener;
    private Context mContext;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView messengerTextView;
        public CircleImageView messengerImageView;


        public MyViewHolder(View view) {
            super(view);
            messengerTextView = (TextView) view.findViewById(R.id.name_list);
            messengerImageView = (CircleImageView) view.findViewById(R.id.recipe_list_thumbnail);
        }

        public void bind(final Match item, final OnItemClickListener listener) {
            messengerTextView.setText(item.getName());
            Glide.with(mContext)
                    .load(item.getPhotoUrl())
                    .into(messengerImageView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
    }

    public MatchAdapter(Context context, List<Match> chatList, OnItemClickListener listener){
         this.matchList = chatList;
         this.mContext = context;
         this.listener = listener;
    }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_chats, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.bind(matchList.get(position), listener);
            /*
            Match match = matchList.get(position);
            holder.messengerTextView.setText(match.getName());

            Glide.with(mContext)
                    .load(matchList.get(position).getPhotoUrl())
                    .into(holder.messengerImageView);
            */
        }

    public void cleanUp() {
        matchList.clear();
    }

    @Override
        public int getItemCount() {
            return matchList.size();
        }

}