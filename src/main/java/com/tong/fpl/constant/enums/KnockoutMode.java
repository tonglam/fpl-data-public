package com.tong.fpl.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Create by tong on 2021/8/30
 */
@Getter
@AllArgsConstructor
public enum KnockoutMode {

    No_knockout("0", "无淘汰赛"),
    Single_round("1", "单循环"),
    Home_away("2", "主客场"),
    PK("3", "PK赛");

    private final String value;
    private final String modeName;

}
