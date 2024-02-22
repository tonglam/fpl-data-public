package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/8/30
 */
@Data
@Accessors(chain = true)
@TableName(value = "team")
public class TeamEntity {

    @TableId(type = IdType.INPUT)
    private Integer id;
    private Integer code;
    private String name;
    private String shortName;

}
