package com.tong.fpl.domain.fantasynutmeg;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/8/30
 */
@Data
public class EventResponseData {

    private int assists;
    @JsonProperty("attempted_passes")
    private int attemptedPasses;
    @JsonProperty("big_chances_created")
    private int bigChancesCreated;
    @JsonProperty("big_chances_missed")
    private int bigChancesMissed;
    private int bonus;
    private int bps;
    @JsonProperty("clean_sheets")
    private int cleanSheets;
    @JsonProperty("clearances_blocks_interceptions")
    private int clearancesBlocksInterceptions;
    @JsonProperty("completed_passes")
    private int completedPasses;
    private double creativity;
    private int dribbles;
    @JsonProperty("ea_index")
    private int eaIndex;
    private int element;
    @JsonProperty("errors_leading_to_goal")
    private int errorsLeadingToGoal;
    @JsonProperty("errors_leading_to_goal_attempt")
    private int errorsLeadingToGoalAttempt;
    private int fixture;
    private int fouls;
    @JsonProperty("goals_conceded")
    private int goalsConceded;
    @JsonProperty("goals_scored")
    private int goalsScored;
    @JsonProperty("ict_index")
    private int ictIndex;
    private int id;
    private double influence;
    @JsonProperty("key_passes")
    private int keyPasses;
    @JsonProperty("kickoff_time")
    private String kickoffTime;
    @JsonProperty("kickoff_time_formatted")
    private String kickoffTimeFormatted;
    @JsonProperty("loaned_in")
    private int loanedIn;
    @JsonProperty("loaned_out")
    private int loanedOut;
    private int minutes;
    private int offside;
    @JsonProperty("open_play_crosses")
    private int openPlayCrosses;
    @JsonProperty("opponent_team")
    private int opponentTeam;
    @JsonProperty("opponent_team_name")
    private String opponentTeamName;
    @JsonProperty("own_goals")
    private int ownGoals;
    @JsonProperty("penalties_conceded")
    private int penaltiesConceded;
    @JsonProperty("penalties_missed")
    private int penaltiesMissed;
    @JsonProperty("penalties_saved")
    private int penaltiesSaved;
    private int recoveries;
    @JsonProperty("red_cards")
    private int redCards;
    private int round;
    private int saves;
    private int selected;
    private int tackled;
    private int tackles;
    @JsonProperty("target_missed")
    private int targetMissed;
    @JsonProperty("team_a_score")
    private int teamAScore;
    @JsonProperty("team_h_score")
    private int teamHScore;
    private double threat;
    @JsonProperty("total_points")
    private int totalPoints;
    @JsonProperty("transfers_balance")
    private int transfersBalance;
    @JsonProperty("transfers_in")
    private int transfersIn;
    @JsonProperty("transfers_out")
    private int transfersOut;
    private int value;
    @JsonProperty("was_home")
    private boolean wasHome;
    @JsonProperty("winning_goals")
    private int winningGoals;
    @JsonProperty("yellow_cards")
    private int yellowCards;

}
