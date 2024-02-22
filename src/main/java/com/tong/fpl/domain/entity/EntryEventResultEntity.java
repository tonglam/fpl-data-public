package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/8/30
 */
@Data
@Accessors(chain = true)
@TableName(value = "entry_event_result")
public class EntryEventResultEntity {

    @TableId
    private Integer id;
    @TableField(value = "`event`", insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer event;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer entry;
    private Integer eventPoints;
    private Integer eventTransfers;
    private Integer eventTransfersCost;
    private Integer eventNetPoints;
    private Integer eventBenchPoints;
    private Integer eventAutoSubPoints;
    private Integer eventRank;
    private String eventChip;
    private Integer playedCaptain;
    private Integer captainPoints;
    private String eventPicks;
    private String eventAutoSubs;
    private Integer overallPoints;
    private Integer overallRank;
    private Integer teamValue;
    private Integer bank;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateTime;

}
