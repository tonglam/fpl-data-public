package com.tong.fpl.domain.data.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tong.fpl.domain.data.bootstrapStaic.*;
import lombok.Data;

import java.util.List;

/**
 * Create by tong on 2021/8/30
 */
@Data
public class StaticRes {

	private List<Event> events;
	@JsonProperty("game_settings")
	private GameSettings gameSettings;
	private List<Phase> phases;
	private List<Team> teams;
	@JsonProperty("total_players")
	private int totalPlayers;
	@JsonProperty("elements")
	private List<Player> elements;
	@JsonProperty("element_stats")
	private List<ElementStat> elementStats;
	@JsonProperty("element_types")
	private List<ElementType> elementTypes;

}
