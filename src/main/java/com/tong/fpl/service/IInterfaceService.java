package com.tong.fpl.service;

import com.tong.fpl.domain.data.response.*;

import java.util.List;
import java.util.Optional;

/**
 * Create by tong on 2021/8/30
 */
public interface IInterfaceService {

    void telegramText(String text, List<String> userList);

    void telegramImage(String url, String caption, List<String> userList);

    Optional<StaticRes> getBootstrapStatic();

    Optional<List<EventFixturesRes>> getEventFixture(int event);

    Optional<EntryRes> getEntry(int entry);

    Optional<EntryCupRes> getEntryCup(int entry);

    Optional<UserHistoryRes> getUserHistory(int entry);

    Optional<UserPicksRes> getUserPicks(int event, int entry);

    Optional<List<UserTransfersRes>> getUserTransfers(int entry);

    Optional<EventLiveRes> getEventLive(int event);

    Optional<ElementSummaryRes> getElementSummary(int element);

    Optional<LeagueClassicRes> getLeaguesClassic(int classicId, int page);

    Optional<LeagueH2hRes> getLeagueH2H(int h2hId, int page);

    List<Integer> getEntryListFromClassicByLimit(int classicId, int limit);

    List<Integer> getEntryListFromH2hByLimit(int h2hId, int limit);

}
