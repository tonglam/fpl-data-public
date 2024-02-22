package com.tong.fpl.domain.data.leaguesH2h;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Create by tong on 2021/8/30
 */
@Data
public class H2hResult {

	private int id;
	private int division;
	private int entry;
	@JsonProperty("player_name")
	private String playerName;
	private int rank;
	@JsonProperty("last_rank")
	private int lastRank;
	@JsonProperty("rank_sort")
	private int rankSort;
	private int total;
	@JsonProperty("entry_name")
	private String entryName;
	@JsonProperty("matches_played")
	private int matchesPlayed;
	@JsonProperty("matches_won")
	private int matchesWon;
	@JsonProperty("matches_drawn")
	private int matchesDrawn;
	@JsonProperty("matchesLost")
	private int matches_lost;
	@JsonProperty("points_for")
	private int pointsFor;

}
