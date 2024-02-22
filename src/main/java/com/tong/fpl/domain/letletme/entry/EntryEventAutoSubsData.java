package com.tong.fpl.domain.letletme.entry;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/9/1
 */
@Data
@Accessors(chain = true)
public class EntryEventAutoSubsData {

    private int event;
    private int entry;
    private int elementIn;
    private String elementInWebName;
    private int elementInType;
    private String elementInTypeName;
    private int elementInTeamId;
    private String elementInTeamName;
    private String elementInTeamShortName;
    private int elementInPoints;
    private int elementOut;
    private String elementOutWebName;
    private int elementOutType;
    private String elementOutTypeName;
    private int elementOutTeamId;
    private String elementOutTeamName;
    private String elementOutTeamShortName;
    private int elementOutPoints;

}
