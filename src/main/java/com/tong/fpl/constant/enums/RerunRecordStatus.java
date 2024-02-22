package com.tong.fpl.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Create by tong on 2022/8/17
 */
@Getter
@AllArgsConstructor
public enum RerunRecordStatus {

    Terminate(-1, "不需重跑"),
    Waiting(0, "待执行"),
    Running(1, "正在执行"),
    Success(2, "执行成功"),
    Fail(3, "执行失败");

    private final int status;
    private final String message;

}
