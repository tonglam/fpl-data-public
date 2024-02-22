package com.tong.fpl.domain.data.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tong.fpl.domain.data.leaguesH2h.H2hInfo;
import com.tong.fpl.domain.data.leaguesH2h.H2hNewEntries;
import com.tong.fpl.domain.data.leaguesH2h.H2hStandings;
import lombok.Data;

/**
 * Create by tong on 2021/8/30
 */
@Data
public class LeagueH2hRes {

    private H2hInfo league;
    @JsonProperty("new_entries")
    private H2hNewEntries newEntries;
    private H2hStandings standings;

}
