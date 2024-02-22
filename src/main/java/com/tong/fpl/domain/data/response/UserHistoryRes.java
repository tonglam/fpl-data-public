package com.tong.fpl.domain.data.response;

import com.tong.fpl.domain.data.userHistory.Current;
import com.tong.fpl.domain.data.userHistory.HistoryChips;
import com.tong.fpl.domain.data.userHistory.Past;
import lombok.Data;

import java.util.List;

/**
 * Create by tong on 2021/8/30
 */
@Data
public class UserHistoryRes {

    private int entry;
    private List<Current> current;
    private List<Past> past;
    private List<HistoryChips> chips;

}
