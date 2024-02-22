package com.tong.fpl.domain.data.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tong.fpl.domain.data.eventFixtures.FixtureStats;
import lombok.Data;

import java.util.List;

/**
 * Create by tong on 2021/8/30
 */
@Data
public class EventFixturesRes {

	private int code;
	private int event;
	private boolean finished;
	@JsonProperty("finished_provisional")
	private boolean finishedProvisional;
	private int id;
	@JsonProperty("kickoff_time")
	private String kickoffTime;
	private int minutes;
	@JsonProperty("provisional_start_time")
	private boolean provisionalStartTime;
	private boolean started;
	@JsonProperty("team_a")
	private int teamA;
	@JsonProperty("team_a_score")
	private int teamAScore;
	@JsonProperty("team_h")
	private int teamH;
	@JsonProperty("team_h_score")
	private int teamHScore;
	private List<FixtureStats> stats;
	@JsonProperty("team_h_difficulty")
	private int teamHDifficulty;
	@JsonProperty("team_a_difficulty")
	private int teamADifficulty;

}
