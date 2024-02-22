package com.tong.fpl.domain.data.leaguesClassic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Create by tong on 2021/8/30
 */
@Data
public class ClassicInfo {

	private int id;
	private String name;
	private String created;
	private boolean closed;
	private int rank;
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

}
