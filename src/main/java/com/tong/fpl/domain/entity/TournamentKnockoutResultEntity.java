package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/8/30
 */
@Data
@Accessors(chain = true)
@TableName(value = "tournament_knockout_result")
public class TournamentKnockoutResultEntity {

    @TableId
    private Integer id;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer tournamentId;
    @TableField(value = "`event`", insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer event;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer matchId;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer playAgainstId;
    private Integer homeEntry;
    private Integer homeEntryNetPoints;
    private Integer homeEntryRank;
    private Integer homeEntryGoalsScored;
    private Integer homeEntryGoalsConceded;
    private Integer awayEntry;
    private Integer awayEntryNetPoints;
    private Integer awayEntryRank;
    private Integer awayEntryGoalsScored;
    private Integer awayEntryGoalsConceded;
    private Integer matchWinner;
    @TableField(fill = FieldFill.INSERT)
    private String createTime;
    @TableField(fill = FieldFill.UPDATE)
    private String updateTime;

}
