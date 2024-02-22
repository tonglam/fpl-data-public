package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/8/30
 */
@Data
@Accessors(chain = true)
@TableName(value = "player_stat")
public class PlayerStatEntity {

    @TableId
    private Integer id;
    private Integer event;
    private Integer element;
    private Integer code;
    private Integer matchPlayed;
    private Integer chanceOfPlayingNextRound;
    private Integer chanceOfPlayingThisRound;
    private Integer dreamteamCount;
    private Integer eventPoints;
    private String form;
    private Boolean inDreamteam;
    private String news;
    private String newsAdded;
    private String pointsPerGame;
    private String selectedByPercent;
    private Integer minutes;
    private Integer goalsScored;
    private Integer assists;
    private Integer cleanSheets;
    private Integer goalsConceded;
    private Integer ownGoals;
    private Integer penaltiesSaved;
    private Integer penaltiesMissed;
    private Integer yellowCards;
    private Integer redCards;
    private Integer saves;
    private Integer bonus;
    private Integer bps;
    private String influence;
    private String creativity;
    private String threat;
    private String ictIndex;
    private Integer transfersInEvent;
    private Integer transfersOutEvent;
    private Integer transfersIn;
    private Integer transfersOut;
    private Integer cornersAndIndirectFreekicksOrder;
    private Integer directFreekicksOrder;
    private Integer penaltiesOrder;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateTime;

}
