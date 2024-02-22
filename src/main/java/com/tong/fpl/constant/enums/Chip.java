package com.tong.fpl.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Create by tong on 2021/8/30
 */
@Getter
@AllArgsConstructor
public enum Chip {

    NONE("n/a"), BB("bboost"), FH("freehit"), WC("wildcard"), TC("3xc");

    private final String value;

}
