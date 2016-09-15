package com.tutump.tutumpdev;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.appyvet.rangebar.RangeBar;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by casertillo on 29/06/16.
 */
public class RangeBarPreference extends Preference {
    private Context context;
    // Initializes the RangeBar in the application
    private RangeBar rangebar;

    public RangeBarPreference(Context context) {
        super(context);
    }

    public RangeBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_age);
    }

    public RangeBarPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        return super.onCreateView(parent);
    }

    @Override protected void onBindView(View view) {
        super.onBindView(view);
        rangebar = (RangeBar) view.findViewById(R.id.rangebar);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        Set<String> AgeRange = settings.getStringSet(getKey(), null);

        if(AgeRange == null)
        {
            updatePreference("18", "60");
            rangebar.setRangePinsByValue(18, 59);
        }
        else
        {
            String[] AgeRangeArray = AgeRange.toArray(new String[AgeRange.size()]);
            if( Float.parseFloat(AgeRangeArray[1]) >= 60)
            {
                AgeRangeArray[1]= "59";
            }
            rangebar.setRangePinsByValue(Float.parseFloat(AgeRangeArray[0]), Float.parseFloat(AgeRangeArray[1]));
        }

        // Sets the display values of the indices
        rangebar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex,
                                              int rightPinIndex,
                                              String leftPinValue, String rightPinValue) {
                updatePreference(leftPinValue, rightPinValue);
            }

        });
    }

    private void updatePreference(String leftValue, String rightValue){
        HashSet<String> ageRange = new HashSet<>();
        ageRange.add(leftValue);
        ageRange.add(rightValue);

        SharedPreferences.Editor editor =  getEditor();
        editor.putStringSet(getKey(), ageRange);
        editor.commit();
    }

}
