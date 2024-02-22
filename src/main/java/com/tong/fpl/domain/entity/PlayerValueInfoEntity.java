package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * Create by tong on 2022/8/17
 */
@Data
@Accessors(chain = true)
@TableName(value = "player_value_info")
public class PlayerValueInfoEntity {

    @TableId
    private Integer id;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private String hourIndex;
    @TableField(value = "`date`", insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private String date;
    private Integer event;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer element;
    private Integer code;
    private Integer elementType;
    private String webName;
    private Integer teamId;
    private String teamShortName;
    private Integer chanceOfPlayingNextRound;
    private Integer chanceOfPlayingThisRound;
    private Integer transfersIn;
    private Integer transfersInEvent;
    private Integer transfersOut;
    private Integer transfersOutEvent;
    private String selectedByPercent;
    private Integer nowCost;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateTime;

}
