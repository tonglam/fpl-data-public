package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 18/8/2023
 */
@Data
@Accessors(chain = true)
@TableName("tournament_royale")
public class TournamentRoyaleEntity {

    @TableId
    private Integer id;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer tournamentId;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer event;
    private Integer eventEliminatedNum;
    private Integer nextEventEliminatedNum;
    private String eventEliminatedEntries;
    private String waitingEliminatedEntries;
    private String allEliminatedEntries;
    @TableField(fill = FieldFill.INSERT)
    private String createTime;
    @TableField(fill = FieldFill.UPDATE)
    private String updateTime;

}
