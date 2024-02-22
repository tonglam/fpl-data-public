package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
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
@TableName(value = "event_live")
public class EventLiveEntity {

	@TableId
	private Integer id;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private Integer element;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY)
	private Integer elementType;
	private Integer teamId;
	@TableField(value = "`event`", insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private Integer event;
	private String fixture;
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

}
