package com.bnrc.busapp.ui.icon;

import com.joanzapata.iconify.Icon;

/**
 * Created by apple on 2018/5/24.
 */

public enum IconFonts implements Icon {
    icon_collect('\ue64b'),
    icon_collect_fill('\ue64c'),
    icon_back('\ue679'),
    icon_right('\ue6a3'),
    icon_message('\ue6bc'),
    icon_share('\ue6f3'),
    icon_alert('\ue708'),
    icon_alert_fill('\ue707'),
    icon_menu('\ue647'),
    icon_final('\ue607'),
    icon_sina('\ue6e5'),
    icon_qq('\ue614'),
    icon_wechat('\ue65b'),
    icon_find('\ue67e'),
    icon_condition('\ue6b4'),
    icon_geton('\ue6ed'),
    icon_home('\ue681'),
    icon_bus('\ue72b'),
    icon_railway('\ue662'),
    icon_reversal('\ue62a'),
    icon_food('\ue606'),
    icon_car('\ue622'),
    icon_hotel('\ue6b2'),
    icon_service('\ue608'),
    icon_search('\ue615'),
    icon_tip('\ue60c'),
    icon_quit('\ue7cb'),
    icon_walk('\ue689'),
    icon_location('\ue655'),
    icon_rate('\ue60f'),
    icon_switch('\ue636'),
    icon_map('\ue884'),
    icon_shop('\ue613'),
    icon_atm('\ue605'),
    icon_route('\ue610'),
    icon_index('\ue557'),
    icon_work('\ue611'),
    icon_ktv('\ue62c'),
    icon_filling('\ue63c'),
    icon_cafe('\ue98f'),
    icon_setting('\ue676'),
    icon_edit('\ue61d'),
    icon_about('\ue629');


    private char character;

    IconFonts(char character) {
        this.character = character;
    }

    @Override
    public String key() {
        return name().replace('_', '-');
    }

    @Override
    public char character() {
        return character;
    }
}