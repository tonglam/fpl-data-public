package com.tong.fpl.domain.data.eventLive;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Create by tong on 2021/8/30
 */
@Data
public class ElementStat {

	private int minutes;
	@JsonProperty("goals_scored")
	private int goalsScored;
	private int assists;
	@JsonProperty("clean_sheets")
	private int cleanSheets;
	@JsonProperty("goals_conceded")
	private int goalsConceded;
	@JsonProperty("own_goals")
	private int ownGoals;
	@JsonProperty("penalties_saved")
	private int penaltiesSaved;
	@JsonProperty("penalties_missed")
	private int penaltiesMissed;
	@JsonProperty("yellow_cards")
	private int yellowCards;
	@JsonProperty("red_cards")
	private int redCards;
	private int saves;
	private int bonus;
	private int bps;
	private String influence;
	private String creativity;
	private String threat;
	@JsonProperty("ict_index")
	private String ictIndex;
	@JsonProperty("total_points")
	private int totalPoints;
	@JsonProperty("in_dreamteam")
	private boolean inDreamteam;

}
