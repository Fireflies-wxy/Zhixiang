package com.bnrc.bnrcbus.application;

import android.app.Application;

import com.bnrc.bnrcbus.ui.icon.IconFontModule;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.joanzapata.iconify.fonts.IoniconsModule;
import com.mob.MobSDK;

/**
 * Created by apple on 2018/5/23.
 *
 */

public class BnrcApplication extends Application {

    private static BnrcApplication mApplication = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;

        //初始化图标库
        Iconify
                .with(new FontAwesomeModule())
                .with(new IoniconsModule())
                .with(new IconFontModule());
        //初始化Mob社会化分享模块
        MobSDK.init(this);
    }

    public static BnrcApplication getInstance(){
        return mApplication;
    }
}
