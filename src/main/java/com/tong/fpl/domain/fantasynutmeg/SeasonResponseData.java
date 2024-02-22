package com.tong.fpl.domain.fantasynutmeg;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/8/30
 */
@Data
public class SeasonResponseData {

    private int assists;
    private int bonus;
    private int bps;
    @JsonProperty("chance_of_playing_next_round")
    private String chanceOfPlayingNextRound;
    @JsonProperty("chance_of_playing_this_round")
    private String chanceOfPlayingThisRound;
    @JsonProperty("clean_sheets")
    private int cleanSheets;
    private int code;
    @JsonProperty("cost_change_event")
    private int costChangeEvent;
    @JsonProperty("cost_change_event_fall")
    private int costChangeEventFall;
    @JsonProperty("cost_change_start")
    private int costChangeStart;
    @JsonProperty("cost_change_start_fall")
    private int costChangeStartFall;
    private int creativity;
    @JsonProperty("dreamteam_count")
    private int dreamteamCount;
    @JsonProperty("ea_index")
    private int eaIndex;
    @JsonProperty("element_type")
    private int elementType;
    @JsonProperty("ep_next")
    private String epNext;
    @JsonProperty("ep_this")
    private double epThis;
    @JsonProperty("event_points")
    private int eventPoints;
    @JsonProperty("first_name")
    private String firstName;
    private double form;
    @JsonProperty("goals_conceded")
    private int goalsConceded;
    @JsonProperty("goals_scored")
    private int goalsScored;
    @JsonProperty("ict_index")
    private double ictIndex;
    private int id;
    @JsonProperty("in_dreamteam")
    private boolean inDreamteam;
    private double influence;
    @JsonProperty("loaned_in")
    private int loanedIn;
    @JsonProperty("loaned_out")
    private int loanedOut;
    @JsonProperty("loans_in")
    private int loansIn;
    @JsonProperty("loans_out")
    private int loansOut;
    private int minutes;
    private String news;
    @JsonProperty("now_cost")
    private double nowCost;
    @JsonProperty("own_goals")
    private int ownGoals;
    @JsonProperty("penalties_missed")
    private int penaltiesMissed;
    @JsonProperty("penalties_saved")
    private int penaltiesSaved;
    private String photo;
    @JsonProperty("points_per_game")
    private double pointsPerGame;
    private String position;
    @JsonProperty("red_cards")
    private int redCards;
    private int saves;
    @JsonProperty("second_name")
    private String secondName;
    @JsonProperty("selected_by_percent")
    private double selectedByPercent;
    private boolean special;
    @JsonProperty("squad_number")
    private String squadNumber;
    private String status;
    private int team;
    @JsonProperty("team_code")
    private int teamCode;
    @JsonProperty("team_name")
    private String teamName;
    private double threat;
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
    private double valueForm;
    @JsonProperty("value_season")
    private double valueSeason;
    @JsonProperty("web_name")
    private String webName;
    @JsonProperty("yellow_cards")
    private int yellowCards;

}
