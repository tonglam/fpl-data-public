package com.tong.fpl.domain.data.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tong.fpl.domain.data.leaguesClassic.ClassicInfo;
import com.tong.fpl.domain.data.leaguesClassic.ClassicNewEntries;
import com.tong.fpl.domain.data.leaguesClassic.ClassicStandings;
import lombok.Data;

/**
 * Create by tong on 2021/8/30
 */
@Data
public class LeagueClassicRes {

    private ClassicInfo league;
    @JsonProperty("new_entries")
    private ClassicNewEntries newEntries;
    private ClassicStandings standings;

}
