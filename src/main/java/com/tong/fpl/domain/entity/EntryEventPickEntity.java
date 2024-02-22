package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/8/30
 */
@Data
@Accessors(chain = true)
@TableName(value = "entry_event_pick")
public class EntryEventPickEntity {

    @TableId
    private Integer id;
    @TableField(value = "`event`", insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer event;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer entry;
    private Integer transfers;
    private Integer transfersCost;
    private String chip;
    private String picks;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateTime;

}
