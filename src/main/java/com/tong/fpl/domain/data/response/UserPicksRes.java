package com.tong.fpl.domain.data.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tong.fpl.domain.data.userpick.AutoSubs;
import com.tong.fpl.domain.data.userpick.EntryHistory;
import com.tong.fpl.domain.data.userpick.Pick;
import lombok.Data;

import java.util.List;

/**
 * Create by tong on 2021/8/30
 */
@Data
public class UserPicksRes {

    private int entry;
    @JsonProperty("active_chip")
    private String activeChip;
    @JsonProperty("automatic_subs")
    private List<AutoSubs> automaticSubs;
    @JsonProperty("entry_history")
    private EntryHistory entryHistory;
    private List<Pick> picks;

}
