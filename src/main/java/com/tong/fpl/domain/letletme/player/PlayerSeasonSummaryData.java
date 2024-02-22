package com.tong.fpl.domain.letletme.player;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by tong on 2022/07/15
 */
@Data
@Accessors(chain = true)
public class PlayerSeasonSummaryData {

    private String season;
    private int element;
    private String webName;
    private int code;
    private int startPrice;
    private int endPrice;
    private int elementType;
    private int teamId;
    private String teamName;
    private String teamShortName;
    private int minutes;
    private int goalsScored;
    private int assists;
    private int cleanSheets;
    private int goalsConceded;
    private int ownGoals;
    private int penaltiesSaved;
    private int penaltiesMissed;
    private int yellowCards;
    private int redCards;
    private int saves;
    private int bonus;
    private int bps;
    private int totalPoints;
    
}
