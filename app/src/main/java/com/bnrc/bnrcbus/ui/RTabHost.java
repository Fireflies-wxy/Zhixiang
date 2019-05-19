package com.bnrc.bnrcbus.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class RTabHost extends LinearLayout {


    public static interface RTabHostListener {
        public void onTabSelected(int index);
    }

    public RTabHost(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private RTabHostListener mRTabHostListener;

    public void setQQTabHostListener(RTabHostListener listener) {
        mRTabHostListener = listener;
    }

    public void selectTab(int index){
        selectTab(index, true);
    }

    public void selectTab(int index, boolean execute) {
        mSelectedTab = index;
        for(int i= 0 ; i < this.getChildCount(); ++i) {
            View child = this.getChildAt(i);
            if(i == index){
                child.setSelected(true);
                if(mRTabHostListener != null && execute){
                    mRTabHostListener.onTabSelected(i);
                }
            } else {
                child.setSelected(false);
            }
        }
    }

    private int mSelectedTab;

    public int getSelectedTab(){
        return mSelectedTab;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        for (int i = 0; i < this.getChildCount(); ++i) {
            final int index = i;
            View child = this.getChildAt(i);
            child.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    onChildClick(index);
                }
            });
        }
    }
    protected void onChildClick(int index) {
        selectTab(index);
    }
}
