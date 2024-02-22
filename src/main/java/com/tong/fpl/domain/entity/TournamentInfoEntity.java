package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/8/30
 */
@Data
@Accessors(chain = true)
@TableName(value = "tournament_info")
public class TournamentInfoEntity {

    @TableId
    private Integer id;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private String name;
    private String creator;
    private Integer adminerEntry;
    private Integer leagueId;
    private String leagueType;
    private Integer totalTeam;
    private String tournamentMode;
    private String groupMode;
    private Integer groupPlayAgainstNum;
    private Integer teamPerGroup;
    private Integer groupStartGw;
    private Integer groupEndGw;
    private Integer groupRounds;
    private Integer groupQualifiers;
    private Boolean groupFillAverage;
    private Integer groupNum;
    private Integer knockoutTeam;
    private String knockoutMode;
    private Integer knockoutPlayAgainstNum;
    private Integer knockoutStartGw;
    private Integer knockoutEndGw;
    private Integer knockoutRounds;
    private Integer knockoutEvents;
    private Integer state;
    @TableField(fill = FieldFill.INSERT)
    private String createTime;
    @TableField(fill = FieldFill.UPDATE)
    private String updateTime;

}
