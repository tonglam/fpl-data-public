package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/8/30
 */
@Data
@Accessors(chain = true)
@TableName(value = "event")
public class EventEntity {

	@TableId(type = IdType.INPUT)
	private Integer id;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private String name;
	private String deadlineTime;
	private Integer averageEntryScore;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private Boolean finished;
	private Integer highestScore;
	private Integer highestScoringEntry;
	private Boolean isPrevious;
	private Boolean isCurrent;
	private Boolean isNext;
	private Integer mostSelected;
	private Integer mostTransferredIn;
	private Integer mostCaptained;
	private Integer mostViceCaptained;

}
