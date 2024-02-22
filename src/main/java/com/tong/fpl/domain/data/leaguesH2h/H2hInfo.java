package com.tong.fpl.domain.data.leaguesH2h;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Create by tong on 2021/8/30
 */
@Data
public class H2hInfo {

	private int id;
	private String name;
	private String created;
	private boolean closed;
	@JsonProperty("max_entries")
	private int maxEntries;
	@JsonProperty("league_type")
	private String leagueType;
	private String scoring;
	@JsonProperty("admin_entry")
	private int adminEntry;
	@JsonProperty("start_event")
	private int startEvent;
	@JsonProperty("code_privacy")
	private String codePrivacy;
	@JsonProperty("ko_rounds")
	private int koRounds;

}
