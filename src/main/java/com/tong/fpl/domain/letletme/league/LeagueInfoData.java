package com.tong.fpl.domain.letletme.league;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/8/30
 */
@Data
@Accessors(chain = true)
public class LeagueInfoData {

    private int id;
    private int leagueId;
    private String leagueType;
    private String leagueName;
    private int limit;
    private int totalTeam;

}
