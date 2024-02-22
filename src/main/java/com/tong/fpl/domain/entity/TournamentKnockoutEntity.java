package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/8/30
 */
@Data
@Accessors(chain = true)
@TableName(value = "tournament_knockout")
public class TournamentKnockoutEntity {

    @TableId
    private Integer id;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer tournamentId;
    @TableField(value = "`round`")
    private Integer round;
    private Integer startGw;
    private Integer endGw;
    private Integer matchId;
    private Integer nextMatchId;
    private Integer homeEntry;
    private Integer homeEntryNetPoints;
    private Integer homeEntryGoalsScored;
    private Integer homeEntryGoalsConceded;
    private Double homeEntryWinningNum;
    private Integer awayEntry;
    private Integer awayEntryNetPoints;
    private Integer awayEntryGoalsScored;
    private Integer awayEntryGoalsConceded;
    private Double awayEntryWinningNum;
    private Integer roundWinner;
    @TableField(fill = FieldFill.INSERT)
    private String createTime;
    @TableField(fill = FieldFill.UPDATE)
    private String updateTime;

}
