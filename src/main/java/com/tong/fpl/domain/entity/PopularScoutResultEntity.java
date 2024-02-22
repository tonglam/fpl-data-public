package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2022/08/08
 */
@Data
@Accessors(chain = true)
@TableName(value = "popular_scout_result")
public class PopularScoutResultEntity {

    @TableId
    private Integer id;
    @TableField(value = "`event`", insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer event;
    @TableField(value = "`source`", insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private String source;
    @TableField(value = "`position_1`")
    private Integer position1;
    @TableField(value = "`position_1_points`")
    private Integer position1Points;
    @TableField(value = "`position_2`")
    private Integer position2;
    @TableField(value = "`position_2_points`")
    private Integer position2Points;
    @TableField(value = "`position_3`")
    private Integer position3;
    @TableField(value = "`position_3_points`")
    private Integer position3Points;
    @TableField(value = "`position_4`")
    private Integer position4;
    @TableField(value = "`position_4_points`")
    private Integer position4Points;
    @TableField(value = "`position_5`")
    private Integer position5;
    @TableField(value = "`position_5_points`")
    private Integer position5Points;
    @TableField(value = "`position_6`")
    private Integer position6;
    @TableField(value = "`position_6_points`")
    private Integer position6Points;
    @TableField(value = "`position_7`")
    private Integer position7;
    @TableField(value = "`position_7_points`")
    private Integer position7Points;
    @TableField(value = "`position_8`")
    private Integer position8;
    @TableField(value = "`position_8_points`")
    private Integer position8Points;
    @TableField(value = "`position_9`")
    private Integer position9;
    @TableField(value = "`position_9_points`")
    private Integer position9Points;
    @TableField(value = "`position_10`")
    private Integer position10;
    @TableField(value = "`position_10_points`")
    private Integer position10Points;
    @TableField(value = "`position_11`")
    private Integer position11;
    @TableField(value = "`position_11_points`")
    private Integer position11Points;
    @TableField(value = "`position_12`")
    private Integer position12;
    @TableField(value = "`position_12_points`")
    private Integer position12Points;
    @TableField(value = "`position_13`")
    private Integer position13;
    @TableField(value = "`position_13_points`")
    private Integer position13Points;
    @TableField(value = "`position_14`")
    private Integer position14;
    @TableField(value = "`position_14_points`")
    private Integer position14Points;
    @TableField(value = "`position_15`")
    private Integer position15;
    @TableField(value = "`position_15_points`")
    private Integer position15Points;
    private Integer captain;
    private Integer captainPoints;
    private Integer viceCaptain;
    private Integer viceCaptainPoints;
    private Integer playedCaptain;
    private Integer playedCaptainPoints;
    private Integer rawTotalPoints;
    private Integer totalPoints;
    private Integer averagePoints;
    private String chip;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateTime;

}
