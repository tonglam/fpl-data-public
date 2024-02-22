package com.tong.fpl.service;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/8/30
 */
public interface IDataService {

    void upsertEntryInfoByList(List<Integer> entryList);

    void insertEventPickByEntryList(int event, List<Integer> entryList);

    void insertEventTransfersByEntryList(int event, List<Integer> entryList);

    void updateEventTransfersByEntryList(int event, List<Integer> entryList);

    void upsertEventCupResultByEntryList(int event, List<Integer> entryList);

    void upsertEventResultByEntryList(int event, List<Integer> entryList);

    void updateAllEventSourceScoutResult(int event);

    void updateEventSourceScoutResult(int event, String source);

    void insertPlayerValueInfo() throws Exception;

}
