package com.tong.fpl.domain.data.entry;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Create by tong on 2021/8/30
 */
@Data
public class Classic {

	private int id;
	private String name;
	@JsonProperty("short_name")
	private String shortName;
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
	@JsonProperty("entry_rank")
	private int entryRank;
	@JsonProperty("entry_last_rank")
	private int entryLastRank;
	@JsonProperty("entry_can_leave")
	private boolean entryCanLeave;
	@JsonProperty("entry_can_admin")
	private boolean entryCanAdmin;
	@JsonProperty("entry_can_invite")
	private boolean entryCanInvite;

}
