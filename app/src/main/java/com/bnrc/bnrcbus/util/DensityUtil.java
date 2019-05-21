package com.bnrc.bnrcbus.util;

import android.content.Context;

/**
 * Created by frank on 15/12/23.
 */
public class DensityUtil {
    public static int dip2px(Context ctx, float dip){
        final float scale = ctx.getResources().getDisplayMetrics().density;
        float pix = scale * dip;
        return (int)pix;
    }
    public static float pix2Dip(Context ctx, float pix){
        final float scale = ctx.getResources().getDisplayMetrics().density;
        float dip =pix / scale;
        return dip;
    }
}
