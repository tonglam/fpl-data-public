package com.tong.fpl.domain.data.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tong.fpl.domain.data.entry.League;
import lombok.Data;

/**
 * Create by tong on 2021/8/30
 */
@Data
public class EntryRes {

	private int id;
	@JsonProperty("joined_time")
	private String joinedTime;
	@JsonProperty("started_event")
	private int startedEvent;
	@JsonProperty("favourite_team")
	private int favouriteTeam;
	@JsonProperty("player_first_name")
	private String playerFirstName;
	@JsonProperty("player_last_name")
	private String playerLastName;
	@JsonProperty("player_region_id")
	private int playerRegionId;
	@JsonProperty("player_region_name")
	private String playerRegionName;
	@JsonProperty("player_region_iso_code_short")
	private String playerRegionIsoCodeShort;
	@JsonProperty("player_region_iso_code_long")
	private String playerRegioIsoCodeLong;
	@JsonProperty("summary_overall_points")
	private int summaryOverallPoints;
	@JsonProperty("summary_overall_rank")
	private int summaryOverallRank;
	@JsonProperty("summary_event_points")
	private int summaryEventPoints;
	@JsonProperty("summary_event_rank")
	private int summaryEventRank;
	@JsonProperty("current_event")
	private int currentEvent;
	private League leagues;
	private String name;
	private String kit;
	@JsonProperty("last_deadline_bank")
	private int lastDeadlineBank;
	@JsonProperty("last_deadline_value")
	private int lastDeadlineValue;
	@JsonProperty("last_deadline_total_transfers")
	private int lastDeadlineTotalTransfers;

}
