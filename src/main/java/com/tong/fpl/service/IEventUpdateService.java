package com.tong.fpl.service;

/**
 * Create by tong on 2021/9/2
 */
public interface IEventUpdateService {

    /**
     * @apiNote daily
     */
    void updateEvent();

    void updatePlayerValue();

    void updatePlayerStat();

    void updateAllEntryInfo();

    /**
     * @apiNote matchDay
     */
    void updateEventLiveCache(int event);

    void updateEventLive(int event);

    void updateEventLiveSummary();

    void updateEventLiveExplain();

    void upsertEventOverallResult();

    /**
     * @apiNote tournament
     */
    void updateTournamentInfo();

    void insertAllTournamentEventPick(int event);

    void insertAllTournamentEventTransfers(int event);

    void upsertAllTournamentEventResult(int event);

    void updateAllPointsRaceGroupResult(int event);

    void updateAllBattleRaceGroupResult(int event);

    void updateAllKnockoutResult(int event);

    void updateAllTournamentEventTransfers(int event);

    void upsertAllTournamentEventCupResult(int event);

    /**
     * @apiNote report
     */
    void insertAllLeagueEventPick(int event);

    void updateAllLeagueEventResult(int event);

    /**
     * @apiNote scout
     */
    void updateAllEventSourceScoutResult(int event);

}
