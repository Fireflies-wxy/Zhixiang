package com.bnrc.bnrcbus.model.AR;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bnrc.bnrcbus.R;

public class POITag extends FrameLayout {

    private TextView poi_name,poi_distance;

    Context mContext;

    public POITag(Context context) {
        this(context, null);
    }

    public POITag(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public POITag(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        poi_name = findViewById(R.id.poi_name);
        poi_distance = findViewById(R.id.poi_distance);
        poi_name.setWidth(10+(poi_name.length()>poi_distance.length()?poi_name.length():poi_distance.length()));
        poi_distance.setWidth(10+(poi_name.length()>poi_distance.length()?poi_name.length():poi_distance.length()));
    }
}
