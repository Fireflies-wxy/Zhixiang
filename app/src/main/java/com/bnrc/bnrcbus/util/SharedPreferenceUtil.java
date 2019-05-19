package com.bnrc.bnrcbus.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.bnrc.bnrcbus.view.activity.SettingActivity;

/**
 * Created by apple on 2018/6/16.
 */

public class SharedPreferenceUtil {
    private static SharedPreferenceUtil mInstance;
    private Context mContext;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;
    private final String category = "SETTING";

    private SharedPreferenceUtil(Context ctx) {
        mContext = ctx;
    }

    public static SharedPreferenceUtil getInstance(Context ctx) {
        if (mInstance == null)
            mInstance = new SharedPreferenceUtil(ctx);
        return mInstance;
    }

    public void setKey(String key, String value) {
        mPreferences = mContext.getSharedPreferences(category,
                SettingActivity.MODE_PRIVATE);
        mEditor = mPreferences.edit();
        mEditor.putString(key, value);
        mEditor.commit();
    }

    public String getValue(String key, String defaultString) {
        mPreferences = mContext.getSharedPreferences(category,
                SettingActivity.MODE_PRIVATE);
        String value = mPreferences.getString(key, defaultString);
        return value;
    }
}
