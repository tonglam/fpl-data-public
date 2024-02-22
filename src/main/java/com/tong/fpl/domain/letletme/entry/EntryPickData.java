package com.tong.fpl.domain.letletme.entry;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/9/1
 */
@Data
@Accessors(chain = true)
public class EntryPickData {

    private int event;
    private int entry;
    private int element;
    private int position;
    private int elementType;
    private String elementTypeName;
    private String webName;
    private int multiplier;
    private boolean isCaptain;
    private boolean isViceCaptain;
    private int teamId;
    private String teamName;
    private String teamShortName;
    private int minutes;
    private int points;
    private int sellPrice;
    private boolean eventTransferIn;
    private boolean eventTransferOut;

}
