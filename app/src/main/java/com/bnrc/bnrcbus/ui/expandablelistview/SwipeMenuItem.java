package com.bnrc.bnrcbus.ui.expandablelistview;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.animation.Animation;

import com.bnrc.bnrcbus.util.AppResourcesUtils;


public class SwipeMenuItem {

    private int id;
    private Context mContext;
    private String title;
    private int iconResId;
    private int backgroundResId;
    private int titleColor;
    private int titleSize;
    private int width;
    private int leftMargin;
    private int rightMargin;
    private int topMargin;
    private int bottomMargin;
    private Animation appearAnimation;
    private Animation disappearAnimation;

    public SwipeMenuItem(Context context) {
        mContext = context;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTitleColor() {
        return titleColor;
    }

    public int getTitleSize() {
        return titleSize;
    }

    public void setTitleSize(int titleSize) {
        this.titleSize = titleSize;
    }

    public void setTitleColor(int titleColor) {
        this.titleColor = titleColor;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTitle(int resId) {
      setTitle(AppResourcesUtils.getString(resId));
    }

    public Drawable getIcon() {
        return AppResourcesUtils.getDrawable(iconResId);
    }

    public void setIcon(int iconResId) {
        this.iconResId = iconResId;
    }

    public Drawable getBackground() {
        return AppResourcesUtils.getDrawable(backgroundResId);
    }

    public void setBackground(int backgroundResId) {
        this.backgroundResId = backgroundResId;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setMargins(int leftMargin, int topMargin, int rightMargin, int bottomMargin) {
        this.leftMargin = leftMargin;
        this.rightMargin = rightMargin;
        this.topMargin = topMargin;
        this.bottomMargin = bottomMargin;
    }

    public int getLeftMargin() {
        return leftMargin;
    }

    public int getRightMargin() {
        return rightMargin;
    }

    public int getTopMargin() {
        return topMargin;
    }

    public int getBottomMargin() {
        return bottomMargin;
    }

    public void setAppearAnimation(Animation appearAnimation) {
        this.appearAnimation = appearAnimation;
    }

    public void setDisappearAnimation(Animation disappearAnimation) {
        this.disappearAnimation = disappearAnimation;
    }

    public Animation getAppearAnimation() {

        return appearAnimation;
    }

    public Animation getDisappearAnimation() {
        return disappearAnimation;
    }
}
