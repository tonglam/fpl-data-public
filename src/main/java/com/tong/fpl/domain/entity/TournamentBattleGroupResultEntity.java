package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/8/30
 */
@Data
@Accessors(chain = true)
@TableName(value = "tournament_battle_group_result")
public class TournamentBattleGroupResultEntity {

    @TableId
    private Integer id;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer tournamentId;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer groupId;
    @TableField(value = "`event`", insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer event;
    private Integer homeIndex;
    private Integer homeEntry;
    private Integer homeEntryNetPoints;
    private Integer homeEntryRank;
    private Integer homeEntryMatchPoints;
    private Integer awayIndex;
    private Integer awayEntry;
    private Integer awayEntryNetPoints;
    private Integer awayEntryRank;
    private Integer awayEntryMatchPoints;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateTime;

}
