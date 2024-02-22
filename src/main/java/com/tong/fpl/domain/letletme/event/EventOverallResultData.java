package com.tong.fpl.domain.letletme.event;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2021/9/2
 */
@Data
@Accessors(chain = true)
public class EventOverallResultData {

    private int event;
    private int averageEntryScore;
    private boolean finished;
    private int highestScoringEntry;
    private int highestScore;
    private List<EventChipData> chipPlays;
    private int mostSelected;
    private String mostSelectedWebName;
    private int mostTransferredIn;
    private String mostTransferredInWebName;
    private EventTopElementData topElementInfo;
    private int transfersMade;
    private int mostCaptained;
    private String mostCaptainedWebName;
    private int mostViceCaptained;
    private String mostViceCaptainedWebName;

}
