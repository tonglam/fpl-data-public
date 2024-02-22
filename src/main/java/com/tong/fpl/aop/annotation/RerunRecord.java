package com.tong.fpl.aop.annotation;

import com.tong.fpl.constant.enums.RerunRecordStatus;

import java.lang.annotation.*;

/**
 * Create by tong on 2022/8/17
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RerunRecord {

    RerunRecordStatus status() default RerunRecordStatus.Waiting;

}
