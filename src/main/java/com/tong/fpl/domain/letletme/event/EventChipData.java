package com.tong.fpl.domain.letletme.event;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/9/2
 */
@Data
@Accessors(chain = true)
public class EventChipData {

    private String chipName;
    private int numberPlayed;

}
