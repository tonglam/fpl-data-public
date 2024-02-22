package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/9/4
 */
@Data
@Accessors(chain = true)
@TableName(value = "player_history")
public class PlayerHistoryEntity {

    @TableId
    private Integer id;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer code;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private String season;
    private Integer price;
    private Integer startPrice;
    private Integer totalPoints;
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

}
