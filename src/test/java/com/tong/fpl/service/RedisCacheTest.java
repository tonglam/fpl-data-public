package com.tong.fpl.service;

import com.tong.fpl.FplDataApplicationTests;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.live.LiveFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerSeasonSummaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/9/1
 */
public class RedisCacheTest extends FplDataApplicationTests {

    @Autowired
    private IRedisCacheService redisCacheService;
    @Autowired
    private IInterfaceService interfaceService;

    // insert

    @Test
    void insertTeam() {
        this.interfaceService.getBootstrapStatic().ifPresent(o -> this.redisCacheService.insertTeam(o));
    }

    @ParameterizedTest
    @CsvSource("2021")
    void insertHisTeam(String season) {
        this.redisCacheService.insertHisTeam(season);
    }

    @Test
    void insertEvent() {
        this.interfaceService.getBootstrapStatic().ifPresent(o -> this.redisCacheService.insertEvent(o));
    }

    @ParameterizedTest
    @CsvSource("1617")
    void insertHisEvent(String season) {
        this.redisCacheService.insertHisEvent(season);
    }

    @Test
    void insertEventFixture() {
        this.redisCacheService.insertEventFixture();
    }

    @ParameterizedTest
    @CsvSource("2021")
    void insertHisEventFixture(String season) {
        this.redisCacheService.insertHisEventFixture(season);
    }

    @ParameterizedTest
    @CsvSource("3")
    void insertSingleEventFixture(int event) {
        this.interfaceService.getEventFixture(event).ifPresent(o -> this.redisCacheService.insertSingleEventFixture(event, o));
    }

    @ParameterizedTest
    @CsvSource("3")
    void insertSingleEventFixtureCache(int event) {
        this.interfaceService.getEventFixture(event).ifPresent(o -> this.redisCacheService.insertSingleEventFixtureCache(event, o));
    }

    @Test
    void insertLiveFixtureCache() {
        this.redisCacheService.insertLiveFixtureCache();
    }

    @Test
    void insertPlayer() {
        this.interfaceService.getBootstrapStatic().ifPresent(o -> this.redisCacheService.insertPlayer(o));
    }

    @ParameterizedTest
    @CsvSource("1617")
    void insertHisPlayer(String season) {
        this.redisCacheService.insertHisPlayer(season);
    }

    @Test
    void insertPlayerStat() {
        this.interfaceService.getBootstrapStatic().ifPresent(o -> this.redisCacheService.insertPlayerStat(o));
    }

    @ParameterizedTest
    @CsvSource("2021")
    void insertHisPlayerStat(String season) {
        this.redisCacheService.insertHisPlayerStat(season);
    }

    @Test
    void insertPlayerValue() {
        this.interfaceService.getBootstrapStatic().ifPresent(o -> this.redisCacheService.insertPlayerValue(o));
    }

    @Test
    void insertPlayerHistory() {
        this.redisCacheService.insertPlayerHistory();
    }

    @ParameterizedTest
    @CsvSource({"1"})
    void insertSinglePlayerHistory(int element) {
        this.interfaceService.getElementSummary(element).ifPresent(this.redisCacheService::insertSinglePlayerHistory);
    }

    @ParameterizedTest
    @CsvSource("2")
    void insertEventLive(int event) {
        this.interfaceService.getEventLive(event).ifPresent(o -> this.redisCacheService.insertEventLive(event, o));
    }

    @ParameterizedTest
    @CsvSource("17")
    void insertEventLiveCache(int event) {
        this.interfaceService.getEventLive(event).ifPresent(o -> this.redisCacheService.insertEventLiveCache(event, o));
    }

    @ParameterizedTest
    @CsvSource("2")
    void insertEventLiveExplain(int event) {
        this.interfaceService.getEventLive(event).ifPresent(o -> this.redisCacheService.insertEventLiveExplain(event, o));
    }

    @Test
    void insertEventLiveSummary() {
        this.redisCacheService.insertEventLiveSummary();
    }

    @ParameterizedTest
    @CsvSource("2021")
    void insertHisEventLiveSummary(String season) {
        this.redisCacheService.insertHisEventLiveSummary(season);
    }

    @Test
    void insertLiveBonusCache() {
        this.redisCacheService.insertLiveBonusCache();
    }

    @Test
    void insertEventOverallResult() {
        this.interfaceService.getBootstrapStatic().ifPresent(o -> this.redisCacheService.insertEventOverallResult(o));
    }

    @Test
    void insertPlayerSummary() {
        this.redisCacheService.insertPlayerSummary();
    }

    @ParameterizedTest
    @CsvSource("1617")
    void insertSeasonPlayerSummaryCache(String season) {
        this.redisCacheService.insertSeasonPlayerSummaryCache(season);
    }

    // get

    @ParameterizedTest
    @CsvSource("1617")
    void getTeamNameMap(String season) {
        Map<String, String> map = this.redisCacheService.getTeamNameMap(season);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource("1617")
    void getTeamShortNameMap(String season) {
        Map<String, String> map = this.redisCacheService.getTeamShortNameMap(season);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource("2021, 3")
    void getDeadlineByEvent(String season, int event) {
        String deadline = this.redisCacheService.getDeadlineByEvent(season, event);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource("2122, 3")
    void getEventFixtureByEvent(String season, int event) {
        List<EventFixtureEntity> list = this.redisCacheService.getEventFixtureByEvent(season, event);
        System.out.println(1);
    }

    @Test
    void getEventLiveFixtureMap() {
        Map<String, Map<String, List<LiveFixtureData>>> map = this.redisCacheService.getEventLiveFixtureMap();
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource("1617")
    void getPlayerMap(String season) {
        Map<String, PlayerEntity> map = this.redisCacheService.getPlayerMap(season);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource("3")
    void getEventLiveByEvent(int event) {
        Map<String, EventLiveEntity> map = this.redisCacheService.getEventLiveByEvent(event);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource("1617")
    void getEventLiveSummaryMap(String season) {
        Map<String, EventLiveSummaryEntity> map = this.redisCacheService.getEventLiveSummaryMap(season);
        System.out.println(1);
    }

    @Test
    void getPlayerSummaryMap() {
        Map<String, List<PlayerSummaryEntity>> map = this.redisCacheService.getPlayerSummaryMap();
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource("2223")
    void getPlayerSeasonSummaryMap(String season) {
        Map<String, PlayerSeasonSummaryData> map = this.redisCacheService.getPlayerSeasonSummaryMap(season);
        System.out.println(1);
    }

}
