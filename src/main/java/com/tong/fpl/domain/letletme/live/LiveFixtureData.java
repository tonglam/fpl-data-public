package com.tong.fpl.domain.letletme.live;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/8/30
 */
@Data
@Accessors(chain = true)
public class LiveFixtureData {

    private int teamId;
    private String teamName;
    private String teamShortName;
    private int teamScore;
    private int againstId;
    private String againstName;
    private String againstShortName;
    private int againstTeamScore;
    private String kickoffTime;
    private String score;
    private boolean wasHome;
    private boolean started;
    private boolean finished;

}
