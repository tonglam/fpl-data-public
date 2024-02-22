package com.tong.fpl.domain.letletme.player;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/8/30
 */
@Data
@Accessors(chain = true)
public class PlayerFixtureData {

    private int event;
    private int teamId;
    private String teamName;
    private String teamShortName;
    private int againstTeamId;
    private String againstTeamName;
    private String againstTeamShortName;
    private int difficulty;
    private String kickoffTime;
    private boolean started;
    private boolean finished;
    private boolean wasHome;
    private int teamScore;
    private int againstTeamScore;
    private String score;
    private String result;
    private boolean bgw;
    private boolean dgw;

}
