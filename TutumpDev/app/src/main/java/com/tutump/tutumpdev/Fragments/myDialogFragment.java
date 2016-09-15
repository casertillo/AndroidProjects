package com.tutump.tutumpdev.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.tutump.tutumpdev.R;

/**
 * Created by casertillo on 29/08/16.
 */
public class myDialogFragment extends DialogFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_new_match, container, false);
        getDialog().setTitle("New Match!");
        String strtext = getArguments().getString("username");
        TextView messageText = (TextView) rootView.findViewById(R.id.textNewMatch);
        messageText.setText("You have a match with "+ strtext);

        Button listButton = (Button) rootView.findViewById(R.id.newMatchButtonContinue);
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewPager pager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                pager.setCurrentItem(2);
                dismiss();
            }
        });
        return rootView;
    }
}
