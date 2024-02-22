package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/8/30
 */
@Data
@Accessors(chain = true)
@TableName(value = "tournament_group")
public class TournamentGroupEntity {

    @TableId
    private Integer id;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer tournamentId;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer groupId;
    private String groupName;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer groupIndex;
    private Integer entry;
    private Integer startGw;
    private Integer endGw;
    private Integer groupPoints;
    private Integer groupRank;
    private Integer play;
    private Integer win;
    private Integer draw;
    private Integer lose;
    private Integer totalPoints;
    private Integer totalTransfersCost;
    private Integer totalNetPoints;
    private Boolean qualified;
    private Integer overallRank;
    @TableField(fill = FieldFill.INSERT)
    private String createTime;
    @TableField(fill = FieldFill.UPDATE)
    private String updateTime;

}
