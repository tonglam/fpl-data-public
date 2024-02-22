package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/9/8
 */
@Data
@Accessors(chain = true)
@TableName(value = "event_live_explain")
public class EventLiveExplainEntity {

    @TableId
    private Integer id;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer element;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY)
    private Integer elementType;
    private Integer teamId;
    @TableField(value = "`event`", insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer event;
    private Integer totalPoints;
    private Integer bps;
    private Integer bonus;
    private Integer minutes;
    private Integer minutesPoints;
    private Integer goalsScored;
    private Integer goalsScoredPoints;
    private Integer assists;
    private Integer assistsPoints;
    private Integer cleanSheets;
    private Integer cleanSheetsPoints;
    private Integer goalsConceded;
    private Integer goalsConcededPoints;
    private Integer ownGoals;
    private Integer ownGoalsPoints;
    private Integer penaltiesSaved;
    private Integer penaltiesSavedPoints;
    private Integer penaltiesMissed;
    private Integer penaltiesMissedPoints;
    private Integer yellowCards;
    private Integer yellowCardsPoints;
    private Integer redCards;
    private Integer redCardsPoints;
    private Integer saves;
    private Integer savesPoints;

}
