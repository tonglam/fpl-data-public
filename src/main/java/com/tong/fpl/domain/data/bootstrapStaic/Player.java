package com.tong.fpl.domain.data.bootstrapStaic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Create by tong on 2021/8/30
 */
@Data
public class Player {

    @JsonProperty("chance_of_playing_next_round")
    private int chanceOfPlayingNextRound;
    @JsonProperty("chance_of_playing_this_round")
    private int chanceOfPlayingThisRound;
    private int code;
    @JsonProperty("cost_change_event")
    private int costChangeEvent;
    @JsonProperty("cost_change_event_fall")
    private int costChangeEventFall;
    @JsonProperty("cost_change_start")
    private int costChangeStart;
    @JsonProperty("cost_change_start_fall")
    private int costChangeStartFall;
    @JsonProperty("dreamteam_count")
    private int dreamteamCount;
    @JsonProperty("element_type")
    private int elementType;
    @JsonProperty("ep_next")
    private String epNext;
    @JsonProperty("ep_this")
    private String epThis;
    @JsonProperty("event_points")
    private int eventPoints;
    @JsonProperty("first_name")
    private String firstName;
    private String form;
    private int id;
    @JsonProperty("in_dreamteam")
    private boolean inDreamteam;
    private String news;
    @JsonProperty("news_added")
    private String newsAdded;
    @JsonProperty("now_cost")
    private int nowCost;
    private String photo;
    @JsonProperty("points_per_game")
    private String pointsPerGame;
    @JsonProperty("second_name")
    private String secondName;
    @JsonProperty("selected_by_percent")
    private String selectedByPercent;
    private boolean special;
    @JsonProperty("squad_number")
    private int squadNumber;
    private String status;
    private int team;
    @JsonProperty("team_code")
    private int teamCode;
    @JsonProperty("total_points")
    private int totalPoints;
    @JsonProperty("transfers_in")
    private int transfersIn;
    @JsonProperty("transfers_in_event")
    private int transfersInEvent;
    @JsonProperty("transfers_out")
    private int transfersOut;
    @JsonProperty("transfers_out_event")
    private int transfersOutEvent;
    @JsonProperty("value_form")
    private String valueForm;
    @JsonProperty("value_season")
    private String valueSeason;
    @JsonProperty("web_name")
    private String webName;
    private int minutes;
    @JsonProperty("goals_scored")
    private int goalsScored;
    private int assists;
    @JsonProperty("clean_sheets")
    private int cleanSheets;
    @JsonProperty("goals_conceded")
    private int goalsConceded;
    @JsonProperty("own_goals")
    private int ownGoals;
    @JsonProperty("penalties_saved")
    private int penaltiesSaved;
    @JsonProperty("penalties_missed")
    private int penaltiesMissed;
    @JsonProperty("yellow_cards")
    private int yellowCards;
    @JsonProperty("red_cards")
    private int redCards;
    private int saves;
    private int bonus;
    private int bps;
    private String influence;
    private String creativity;
    private String threat;
    @JsonProperty("ict_index")
    private String ictIndex;
    @JsonProperty("influence_rank")
    private String influenceRank;
    @JsonProperty("influence_rank_type")
    private String influenceRankType;
    @JsonProperty("creativity_rank")
    private String creativityRank;
    @JsonProperty("creativity_rank_type")
    private String creativityRankType;
    @JsonProperty("threat_rank")
    private String threatRank;
    @JsonProperty("threat_rank_type")
    private String threatRankType;
    @JsonProperty("ict_index_rank")
    private String ictIndexRank;
    @JsonProperty("ict_index_rank_type")
    private String ictIndexRankType;
    @JsonProperty("corners_and_indirect_freekicks_order")
    private int cornersAndIndirectFreekicksOrder;
    @JsonProperty("corners_and_indirect_freekicks_text")
    private String cornersAndIndirectFreekicksText;
    @JsonProperty("direct_freekicks_order")
    private int directFreekicksOrder;
    @JsonProperty("direct_freekicks_text")
    private String directFreekicksText;
    @JsonProperty("penalties_order")
    private int penaltiesOrder;
    @JsonProperty("penalties_text")
    private String penaltiesText;

}
