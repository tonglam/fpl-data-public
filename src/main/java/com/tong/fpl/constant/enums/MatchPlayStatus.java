package com.tong.fpl.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Create by tong on 2021/8/30
 */
@Getter
@AllArgsConstructor
public enum MatchPlayStatus {

    Next_Event(-1), Playing(0), Finished(1), Not_Start(2), Event_Not_Finished(3), Blank(4);

    private final int status;

}
