package com.tong.fpl.service;

import com.tong.fpl.FplDataApplicationTests;
import com.tong.fpl.domain.entity.EventFixtureEntity;
import com.tong.fpl.domain.entity.TournamentInfoEntity;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.league.LeagueInfoData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Create by tong on 2021/8/31
 */
public class QueryTest extends FplDataApplicationTests {

    @Autowired
    private IQueryService queryService;

    // time

    @ParameterizedTest
    @CsvSource({"6"})
    void isMatchDay(int event) {
        boolean a = this.queryService.isMatchDay(event);
        System.out.println(a);
    }

    @ParameterizedTest
    @CsvSource({"19"})
    void isAfterMatchDay(int event) {
        boolean a = this.queryService.isAfterMatchDay(event);
        System.out.println(a);
    }

    @ParameterizedTest
    @CsvSource({"3"})
    void isMatchDayTime(int event) {
        boolean a = this.queryService.isMatchDayTime(event);
        System.out.println(a);
    }

    @ParameterizedTest
    @CsvSource({"19"})
    void isSelectTime(int event) {
        boolean a = this.queryService.isSelectTime(event);
        System.out.println(a);
    }

    // event

    @ParameterizedTest
    @CsvSource({"2122, 3"})
    void getDeadlineByEvent(String season, int event) {
        String deadline = this.queryService.getDeadlineByEvent(season, event);
        System.out.println(1);
    }

    @Test
    void getCurrentEvent() {
        int event = this.queryService.getCurrentEvent();
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"6"})
    void getMatchDayByEvent(int event) {
        List<LocalDate> list = this.queryService.getMatchDayByEvent(event);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"6"})
    void getAfterMatchDayByEvent(int event) {
        List<LocalDate> list = this.queryService.getAfterMatchDayByEvent(event);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"3"})
    void getMatchDayTimeByEvent(int event) {
        List<LocalDateTime> list = this.queryService.getMatchDayTimeByEvent(event);
        System.out.println(1);
    }

    // fixture

    @ParameterizedTest
    @CsvSource({"2122, 6"})
    void getEventFixtureByEvent(String season, int event) {
        List<EventFixtureEntity> list = this.queryService.getEventFixtureByEvent(season, event);
        System.out.println(1);
    }

    // entry

    @Test
    void qryAllEntryList() {
        List<Integer> list = this.queryService.qryAllEntryList();
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2122, 1713"})
    void qryEntryInfo(String season, int entry) {
        EntryInfoData data = this.queryService.qryEntryInfo(season, entry);
        System.out.println(1);
    }

    // tournament

    @ParameterizedTest
    @CsvSource({"1715"})
    void qryTournamentInfoById(int entry) {
        TournamentInfoEntity data = this.queryService.qryTournamentInfoById(entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1"})
    void qryEntryListByTournament(int tournamentId) {
        List<Integer> list = this.queryService.qryEntryListByTournament(tournamentId);
        System.out.println(1);
    }

    @Test
    void qryAllTournamentInfoList() {
        List<TournamentInfoEntity> list = this.queryService.qryAllTournamentInfoList();
        System.out.println(1);
    }

    @Test
    void qryAllTournamentList() {
        List<Integer> list = this.queryService.qryAllTournamentList();
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"3"})
    void qryPointsRaceGroupTournamentList(int event) {
        List<Integer> list = this.queryService.qryPointsRaceGroupTournamentList(event);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"3"})
    void qryBattleRaceGroupTournamentList(int event) {
        List<Integer> list = this.queryService.qryBattleRaceGroupTournamentList(event);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"3"})
    void qryKnockoutTournamentList(int event) {
        List<Integer> list = this.queryService.qryKnockoutTournamentList(event);
        System.out.println(1);
    }

    // report

    @ParameterizedTest
    @CsvSource({"1353"})
    void qryReportLeagueDataByTournamentId(int tournamentId) {
        LeagueInfoData data = this.queryService.qryLeagueEventReportDataByLeagueId(tournamentId);
        System.out.println(1);
    }

    @Test
    void qryAllReportLeagueDataList() {
        List<LeagueInfoData> list = this.queryService.qryAllReportLeagueDataList();
        System.out.println(1);
    }

}
