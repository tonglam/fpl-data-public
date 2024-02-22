package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/8/30
 */
@Data
@Accessors(chain = true)
@TableName(value = "event_fixture")
public class EventFixtureEntity {

    @TableId(type = IdType.INPUT)
    private Integer id;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer code;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer event;
    private String kickoffTime;
    private Boolean started;
    private Boolean finished;
    private Boolean provisionalStartTime;
    private Boolean finishedProvisional;
    private Integer minutes;
    private Integer teamH;
    private Integer teamHDifficulty;
    private Integer teamHScore;
    private Integer teamA;
    private Integer teamADifficulty;
    private Integer teamAScore;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateTime;

}
