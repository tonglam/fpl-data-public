package com.tong.fpl.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by tong on 2023/04/23
 */
@Getter
@AllArgsConstructor
public enum NotifyEnums {

    Telegram(1), Email(2);

    private final int type;

}
