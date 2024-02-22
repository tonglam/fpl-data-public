package com.tong.fpl.domain.data.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Create by tong on 2021/8/30
 */
@Data
public class UserTransfersRes {

    @JsonProperty("element_in")
    private int elementIn;
    @JsonProperty("element_in_cost")
    private int elementInCost;
    @JsonProperty("element_out")
    private int elementOut;
    @JsonProperty("element_out_cost")
    private int elementOutCost;
    private int entry;
    private int event;
    private String time;

}
