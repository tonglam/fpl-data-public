package com.tong.fpl.service;

import com.tong.fpl.FplDataApplicationTests;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.IntStream;

/**
 * Create by tong on 2021/9/2
 */
public class EventUpdateTest extends FplDataApplicationTests {

    @Autowired
    private IEventUpdateService eventUpdateService;

    // daily

    @Test
    void updateEventData() {
        this.eventUpdateService.updateEvent();
    }

    @Test
    void updatePlayerValue() {
        this.eventUpdateService.updatePlayerValue();
    }

    @Test
    void updatePlayerStat() {
        this.eventUpdateService.updatePlayerStat();
    }

    @Test
    void updateAllEntryInfo() {
        this.eventUpdateService.updateAllEntryInfo();
    }

    // matchDay

    @ParameterizedTest
    @CsvSource({"11"})
    void updateEventLiveCache(int event) {
        this.eventUpdateService.updateEventLiveCache(event);
    }

    @ParameterizedTest
    @CsvSource({"11"})
    void updateEventLiveData(int event) {
        this.eventUpdateService.updateEventLive(event);
    }

    @Test
    void updateEventLiveSummary() {
        this.eventUpdateService.updateEventLiveSummary();
    }

    // tournament

    @ParameterizedTest
    @CsvSource({"5"})
    void insertAllEntryEventPicks(int event) {
        this.eventUpdateService.insertAllTournamentEventPick(event);
    }

    @ParameterizedTest
    @CsvSource({"19"})
    void insertAllEntryEventTransfers(int event) {
        this.eventUpdateService.insertAllTournamentEventTransfers(event);
    }

    @ParameterizedTest
    @CsvSource({"7"})
    void updateAllTournamentEventTransfers(int event) {
        this.eventUpdateService.updateAllTournamentEventTransfers(event);
    }

    @ParameterizedTest
    @CsvSource({"17"})
    void updateAllLeagueEventResult(int event){
        this.eventUpdateService.updateAllLeagueEventResult(event);
    }

}
