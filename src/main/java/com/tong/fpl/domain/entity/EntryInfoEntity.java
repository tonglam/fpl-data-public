package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/8/30
 */
@Data
@Accessors(chain = true)
@TableName("entry_info")
public class EntryInfoEntity {

    @TableId(type = IdType.INPUT)
    private Integer entry;
    private String entryName;
    private String playerName;
    private String region;
    private Integer startedEvent;
    private Integer overallPoints;
    private Integer overallRank;
    private Integer bank;
    private Integer teamValue;
    private Integer totalTransfers;
    private String lastEntryName;
    private Integer lastOverallPoints;
    private Integer lastOverallRank;
    private Integer lastTeamValue;
    private String usedEntryName;
    @TableField(fill = FieldFill.INSERT)
    private String createTime;
    @TableField(fill = FieldFill.UPDATE)
    private String updateTime;

}
