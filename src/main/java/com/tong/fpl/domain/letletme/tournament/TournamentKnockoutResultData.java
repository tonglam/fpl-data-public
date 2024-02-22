package com.tong.fpl.domain.letletme.tournament;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/8/30
 */
@Data
@Accessors(chain = true)
public class TournamentKnockoutResultData {

    private int tournamentId;
    private int round;
    private int event;
    private int playAgainstId;
    private int matchId;
    private int homeEntry;
    private int awayEntry;
    private int homeEntryNetPoints;
    private int awayEntryNetPoints;
    private int homeEntryGroupId;
    private int awayEntryGroupId;
    private String homeEntryGroupName;
    private String awayEntryGroupName;
    private String homeEntryName;
    private String awayEntryName;
    private int homeEntryRank;
    private int awayEntryRank;
    private int homeEntryGoalsScored;
    private int awayEntryGoalsScored;
    private int homeEntryGoalsConceded;
    private int awayEntryGoalsConceded;
    private double homeEntryWinningNum;
    private double awayEntryWinningNum;
    private String matchInfo;
    private int matchWinner;
    private int winnerRank;

}
