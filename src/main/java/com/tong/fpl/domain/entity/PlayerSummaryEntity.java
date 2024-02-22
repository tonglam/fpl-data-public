package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2022/7/14
 */
@Data
@Accessors(chain = true)
@TableName(value = "player_summary")
public class PlayerSummaryEntity {

    @TableId(type = IdType.INPUT)
    private Integer id;
    private String season;
    private Integer element;
    private String webName;
    private Integer code;
    private Integer startPrice;
    private Integer endPrice;
    private Integer elementType;
    private Integer teamId;
    private String teamName;
    private String teamShortName;
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
    private Integer totalPoints;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateTime;

}
