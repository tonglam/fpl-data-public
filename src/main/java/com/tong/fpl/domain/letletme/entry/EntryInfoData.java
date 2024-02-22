package com.tong.fpl.domain.letletme.entry;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/9/2
 */
@Data
@Accessors(chain = true)
public class EntryInfoData {

    private int entry;
    private String entryName;
    private String playerName;
    private String region;
    private int startedEvent;
    private int overallPoints;
    private int overallRank;
    private double value;
    private double bank;
    private double teamValue;
    private int totalTransfers;

}
