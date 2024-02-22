package com.tong.fpl.domain.data.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tong.fpl.domain.data.elementSummary.ElementFixture;
import com.tong.fpl.domain.data.elementSummary.ElementHistory;
import com.tong.fpl.domain.data.elementSummary.ElementHistoryPast;
import lombok.Data;

import java.util.List;

/**
 * Create by tong on 2021/9/4
 */
@Data
public class ElementSummaryRes {

    private List<ElementFixture> fixtures;
    private List<ElementHistory> history;
    @JsonProperty("history_past")
    private List<ElementHistoryPast> historyPast;

}
