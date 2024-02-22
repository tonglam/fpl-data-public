package com.tong.fpl.domain.data.elementSummary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Create by tong on 2021/8/30
 */
@Data
public class ElementHistory {

    private int element;
    private int fixture;
    @JsonProperty("opponent_team")
    private int opponentTeam;
    @JsonProperty("total_points")
    private int totalPoints;
    @JsonProperty("was_home")
    private boolean wasHome;
    @JsonProperty("kickoff_time")
    private String kickoffTime;
    @JsonProperty("team_h_score")
    private int teamHScore;
    @JsonProperty("team_a_score")
    private int teamAScore;
    private int round;
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
    private int penaltiesSave;
    private int penaltiesMissed;
    private int yellowCards;
    private int redCards;
    private int saves;
    private int bonus;
    private int bps;
    private String influence;
    private String creativity;
    private String threat;
    @JsonProperty("ict_index")
    private String ictIndex;
    private int value;
    @JsonProperty("transfers_balance")
    private int transfersBalance;
    private int selected;
    @JsonProperty("transfers_in")
    private int transfersIn;
    @JsonProperty("transfers_out")
    private int transfersOut;

}
