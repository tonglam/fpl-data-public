package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/8/30
 */
@Data
@Accessors(chain = true)
@TableName(value = "player")
public class PlayerEntity {

    @TableId(type = IdType.INPUT)
    private Integer element;
    private Integer code;
    private Integer price;
    private Integer startPrice;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer elementType;
    private String firstName;
    private String secondName;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private String webName;
    private Integer teamId;

}
