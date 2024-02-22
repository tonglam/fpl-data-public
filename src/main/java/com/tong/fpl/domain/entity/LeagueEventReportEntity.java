package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/8/30
 */
@Data
@Accessors(chain = true)
@TableName(value = "league_event_report")
public class LeagueEventReportEntity {

    @TableId
    private Integer id;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY)
    private Integer leagueId;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY)
    private String leagueType;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY)
    private String leagueName;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer entry;
    private String entryName;
    private String playerName;
    private Integer overallPoints;
    private Integer overallRank;
    private Integer teamValue;
    private Integer bank;
    @TableField(value = "`event`", insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer event;
    private Integer eventPoints;
    private Integer eventTransfers;
    private Integer eventTransfersCost;
    private Integer eventNetPoints;
    private Integer eventBenchPoints;
    private Integer eventAutoSubPoints;
    private Integer eventRank;
    private String eventChip;
    private Integer position1;
    private Integer position2;
    private Integer position3;
    private Integer position4;
    private Integer position5;
    private Integer position6;
    private Integer position7;
    private Integer position8;
    private Integer position9;
    private Integer position10;
    private Integer position11;
    private Integer position12;
    private Integer position13;
    private Integer position14;
    private Integer position15;
    private Integer captain;
    private Integer captainPoints;
    private Boolean captainBlank;
    private String captainSelected;
    private Integer viceCaptain;
    private Integer viceCaptainPoints;
    private Boolean viceCaptainBlank;
    private String viceCaptainSelected;
    private int playedCaptain;
    private Integer highestScore;
    private Integer highestScorePoints;
    private Boolean highestScoreBlank;
    private String highestScoreSelected;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateTime;

}
