package com.bnrc.bnrcbus.util;
import android.util.Log;

/**
 * Created by apple on 2018/6/16.
 */

public class BnrcLog  {

    //开关
    public static final  boolean DEBUG = true;

    //五个等级  DIWE

    public static void d(String TAG, String text){
        if(DEBUG){
            Log.d(TAG,text);
        }
    }

    public static void i(String TAG, String text){
        if(DEBUG){
            Log.i(TAG,text);
        }
    }

    public static void w(String TAG, String text){
        if(DEBUG){
            Log.w(TAG,text);
        }
    }

    public static void e(String TAG, String text){
        if(DEBUG){
            Log.e(TAG,text);
        }
    }

}
