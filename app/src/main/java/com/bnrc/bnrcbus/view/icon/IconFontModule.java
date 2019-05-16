package com.bnrc.bnrcbus.view.icon;

import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconFontDescriptor;

/**
 * Created by apple on 2018/5/24.
 */

public class IconFontModule implements IconFontDescriptor {
    @Override
    public String ttfFileName() {
        return "iconfont.ttf";//assets中的字体文件
    }

    @Override
    public Icon[] characters() {
        return IconFonts.values();
    }
}