package com.tong.fpl.domain.data.leaguesClassic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Create by tong on 2021/8/30
 */
@Data
public class ClassicResult {

	private int id;
	@JsonProperty("event_total")
	private int eventTotal;
	@JsonProperty("player_name")
	private String playerName;
	private int rank;
	@JsonProperty("last_rank")
	private int lastRank;
	@JsonProperty("rank_sort")
	private int rankSort;
	private int total;
	private int entry;
	@JsonProperty("entry_name")
	private String entryName;

}
