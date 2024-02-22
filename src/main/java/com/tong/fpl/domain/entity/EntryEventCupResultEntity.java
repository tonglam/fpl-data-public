package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/8/30
 */
@Data
@Accessors(chain = true)
@TableName(value = "entry_event_cup_result")
public class EntryEventCupResultEntity {

    @TableId
    private Integer id;
    @TableField(value = "`event`", insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer event;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer entry;
    private String entryName;
    private String playerName;
    private Integer eventPoints;
    private Integer againstEntry;
    private String againstEntryName;
    private String againstPlayerName;
    private Integer againstEventPoints;
    private String result;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateTime;

}
