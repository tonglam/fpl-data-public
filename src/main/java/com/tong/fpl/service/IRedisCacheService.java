package com.tong.fpl.service;

import com.tong.fpl.domain.data.response.ElementSummaryRes;
import com.tong.fpl.domain.data.response.EventFixturesRes;
import com.tong.fpl.domain.data.response.EventLiveRes;
import com.tong.fpl.domain.data.response.StaticRes;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.event.EventOverallResultData;
import com.tong.fpl.domain.letletme.live.LiveFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerSeasonSummaryData;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/8/30
 */
public interface IRedisCacheService {

    void insertTeam(StaticRes staticRes);

    void insertHisTeam(String season);

    void insertEvent(StaticRes staticRes);

    void insertHisEvent(String season);

    void insertEventFixture();

    void insertHisEventFixture(String season);

    void insertSingleEventFixture(int event, List<EventFixturesRes> eventFixturesResList);

    void insertSingleEventFixtureCache(int event, List<EventFixturesRes> eventFixturesResList);

    void insertLiveFixtureCache();

    void insertPlayer(StaticRes staticRes);

    void insertHisPlayer(String season);

    void insertPlayerStat(StaticRes staticRes);

    void insertHisPlayerStat(String season);

    void insertPlayerValue(StaticRes staticRes);

    void insertPlayerHistory();

    void insertSinglePlayerHistory(ElementSummaryRes elementSummaryRes);

    void insertEventLive(int event, EventLiveRes eventLiveRes);

    void insertEventLiveCache(int event, EventLiveRes eventLiveRes);

    void insertEventLiveExplain(int event, EventLiveRes eventLiveRes);

    void insertEventLiveSummary();

    void insertHisEventLiveSummary(String season);

    void insertLiveBonusCache();

    void insertEventOverallResult(StaticRes staticRes);

    void insertPlayerSummary();

    void insertSeasonPlayerSummaryCache(String season);

    int getCurrentEvent();

    Map<String, String> getTeamNameMap(String season);

    Map<String, String> getTeamShortNameMap(String season);

    String getDeadlineByEvent(String season, int event);

    List<EventFixtureEntity> getEventFixtureByEvent(String season, int event);

    Map<String, Map<String, List<LiveFixtureData>>> getEventLiveFixtureMap();

    Map<String, PlayerEntity> getPlayerMap(String season);

    Map<String, EventLiveEntity> getEventLiveByEvent(int event);

    Map<String, EventLiveSummaryEntity> getEventLiveSummaryMap(String season);

    Map<String, List<PlayerSummaryEntity>> getPlayerSummaryMap();

    Map<String, PlayerSeasonSummaryData> getPlayerSeasonSummaryMap(String season);
    EventOverallResultData getEventOverallResultByEvent(String season, int event);

}
