package com.tong.fpl.service;

import com.tong.fpl.domain.entity.EventFixtureEntity;
import com.tong.fpl.domain.entity.TournamentInfoEntity;
import com.tong.fpl.domain.entity.TournamentKnockoutResultEntity;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.league.LeagueInfoData;
import com.tong.fpl.utils.CommonUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Create by tong on 2021/8/31
 */
public interface IQueryService {

    /**
     * @apiNote time
     */
    boolean isMatchDay(int event);

    boolean isAfterMatchDay(int event);

    boolean isMatchDayTime(int event);

    boolean isSelectTime(int event);

    /**
     * @apiNote event
     */
    default String getDeadlineByEvent(int event) {
        return this.getDeadlineByEvent(CommonUtils.getCurrentSeason(), event);
    }

    String getDeadlineByEvent(String season, int event);

    int getCurrentEvent();

    int getLastEvent();

    List<LocalDate> getMatchDayByEvent(int event);

    List<LocalDate> getAfterMatchDayByEvent(int event);

    List<LocalDateTime> getMatchDayTimeByEvent(int event);

    /**
     * @apiNote fixture
     */
    default List<EventFixtureEntity> getEventFixtureByEvent(int event) {
        return this.getEventFixtureByEvent(CommonUtils.getCurrentSeason(), event);
    }

    List<EventFixtureEntity> getEventFixtureByEvent(String season, int event);

    /**
     * @apiNote entry
     */
    List<Integer> qryAllEntryList();

    default EntryInfoData qryEntryInfo(int entry) {
        return this.qryEntryInfo(CommonUtils.getCurrentSeason(), entry);
    }

    EntryInfoData qryEntryInfo(String season, int entry);

    /**
     * @apiNote tournament
     */
    TournamentInfoEntity qryTournamentInfoById(int tournamentId);

    List<Integer> qryEntryListByTournament(int tournamentId);

    List<TournamentInfoEntity> qryAllTournamentInfoList();

    List<Integer> qryAllTournamentList();

    List<Integer> qryPointsRaceGroupTournamentList(int event);

    List<Integer> qryBattleRaceGroupTournamentList(int event);

    List<Integer> qryKnockoutTournamentList(int event);

    List<TournamentKnockoutResultEntity> qryKnockoutResultTournamentList(int tournamentId, int matchId);

    /**
     * @apiNote report
     */
    LeagueInfoData qryLeagueEventReportDataByLeagueId(int leagueId);

    List<LeagueInfoData> qryAllReportLeagueDataList();

}
