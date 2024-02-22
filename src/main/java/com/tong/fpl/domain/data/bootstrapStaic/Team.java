package com.tong.fpl.domain.data.bootstrapStaic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Create by tong on 2021/8/30
 */
@Data
public class Team {

	private int code;
	private int draw;
	private int form;
	private int id;
	private int loss;
	private String name;
	private int played;
	private int points;
	private int position;
	@JsonProperty("short_name")
	private String shortName;
	private int strength;
	@JsonProperty("team_division")
	private String teamDivision;
	private boolean unavailable;
	private int win;
	@JsonProperty("strength_overall_home")
	private int strengthOverallHome;
	@JsonProperty("strength_overall_away")
	private int strengthOverallAway;
	@JsonProperty("strength_attack_home")
	private int strengthAttackHome;
	@JsonProperty("strength_attack_away")
	private int strengthAttackAway;
	@JsonProperty("strength_defence_home")
	private int strengthDefenceHome;
	@JsonProperty("strength_defence_away")
	private int strengthDefenceAway;
	@JsonProperty("pulse_id")
	private int pulseId;

}
