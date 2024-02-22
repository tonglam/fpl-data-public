package com.tong.fpl.domain.letletme.player;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/9/4
 */
@Data
@Accessors(chain = true)
public class PlayerHistoryData {

    private int code;
    private String season;
    private int price;
    private int startPrice;
    private int totalPoints;
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
    private String influence;
    private String creativity;
    private String threat;
    private String ictIndex;

}
