package com.tong.fpl.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Create by tong on 2021/8/30
 */
@Getter
@AllArgsConstructor
public enum GroupMode {

    No_group("0", "无小组赛"),
    Points_race("1", "积分赛"),
    Battle_race("2", "对阵赛"),
    Custom("3", "自定义");

    private final String value;
    private final String modeName;

}
