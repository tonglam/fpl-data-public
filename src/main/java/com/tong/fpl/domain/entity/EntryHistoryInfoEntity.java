package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/8/30
 */
@Data
@Accessors(chain = true)
@TableName("entry_history_info")
public class EntryHistoryInfoEntity {

    @TableId
    private Integer id;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer entry;
    private String season;
    private Integer totalPoints;
    private Integer overallRank;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateTime;

}
