package com.tutump.tutumpdev;

import android.graphics.Color;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by casertillo on 06/08/16.
 */
public class ChatListDecoration extends RecyclerView.ItemDecoration{
    private int space;

    public ChatListDecoration(int space) {
        this.space = space;
    }


    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        outRect.left = space;
        outRect.right = space;
        outRect.bottom = space;
        //view.setBackgroundColor(Color.parseColor("#FFFFFF"));
        //parent.setBackgroundColor();
        // Add top margin only for the first item to avoid double space between items
        if (parent.getChildLayoutPosition(view) == 0) {
            outRect.top = space;
        } else {
            outRect.top = 0;
        }
    }

}
