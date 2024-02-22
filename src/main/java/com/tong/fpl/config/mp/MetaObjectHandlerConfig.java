package com.tong.fpl.config.mp;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.tong.fpl.constant.Constant;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Create by tong on 2021/8/30
 */
@Configuration
public class MetaObjectHandlerConfig implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.setFieldValByName("createTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern(Constant.DATETIME)), metaObject); // redis not support LocalDateTime
        this.setFieldValByName("updateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern(Constant.DATETIME)), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updateTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern(Constant.DATETIME)), metaObject);
    }

}
