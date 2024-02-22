package com.tong.fpl.domain.data.bootstrapStaic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Create by tong on 2021/8/30
 */
@Data
public class GameSettings {

	@JsonProperty("league_join_private_max")
	private int leagueJoinPrivateMax;
	@JsonProperty("league_join_public_max")
	private int leagueJoinPublicMax;
	@JsonProperty("league_max_size_public_classic")
	private int leagueMaxSizePublicClassic;
	@JsonProperty("league_max_size_public_h2h")
	private int leagueMaxSizePublicH2h;
	@JsonProperty("league_max_size_private_h2h")
	private int leagueMaxSizePrivateH2h;
	@JsonProperty("league_max_ko_rounds_private_h2h")
	private int leagueMaxKoRoundsPrivateH2h;
	@JsonProperty("league_prefix_public")
	private String leaguePrefixPublic;
	@JsonProperty("league_points_h2h_win")
	private int leaguePointsH2hWin;
	@JsonProperty("league_points_h2h_lose")
	private int leaguePointsH2hLose;
	@JsonProperty("league_points_h2h_draw")
	private int leaguePointsH2hDraw;
	@JsonProperty("league_ko_first_instead_of_random")
	private boolean leagueKoFirstInsteadOfRandom;
	@JsonProperty("cup_start_event_id")
	private int cupStartEventId;
	@JsonProperty("cup_stop_event_id")
	private int cupStopEventId;
	@JsonProperty("cup_qualifying_method")
	private String cupQualifyingMethod;
	@JsonProperty("cup_type")
	private String cupType;
	@JsonProperty("squad_squadplay")
	private int squadSquadplay;
	@JsonProperty("squad_squadsize")
	private int squadSquadsize;
	@JsonProperty("squad_team_limit")
	private int squadTeamLimit;
	@JsonProperty("squad_total_spend")
	private int squadTotalSpend;
	@JsonProperty("ui_currency_multiplier")
	private int uiCurrencyMultiplier;
	@JsonProperty("ui_use_special_shirts")
	private boolean uiUseSpecialShirts;
	@JsonProperty("ui_special_shirt_exclusions")
	private List<String> uiSpecialShirtExclusions;
	@JsonProperty("stats_form_days")
	private int statsFormDays;
	@JsonProperty("sys_vice_captain_enabled")
	private boolean sysViceCaptainEnabled;
	@JsonProperty("transfers_sell_on_fee")
	private double transfersSellOnFee;
	@JsonProperty("league_h2h_tiebreak_stats")
	private List<String> leagueH2hTiebreakStats;
	private String timezone;

}
