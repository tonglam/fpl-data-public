package com.tong.fpl.service;

import com.tong.fpl.FplDataApplicationTests;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.IntStream;

/**
 * Create by tong on 2021/9/2
 */
public class TournamentTest extends FplDataApplicationTests {

    @Autowired
    private ITournamentService tournamentService;

    @ParameterizedTest
    @CsvSource({"4, 4"})
    void upsertTournamentEventResult(int event1, int tournamentId1) {
        IntStream.rangeClosed(22, 21).forEach(event->{
            IntStream.rangeClosed(15, 16).forEach(tournamentId->{
                this.tournamentService.upsertTournamentEventResult(event, tournamentId);
            });
        });
    }

    @ParameterizedTest
    @CsvSource({"22"})
    void updatePointsRaceGroupResult(int event1) {
        IntStream.rangeClosed(17, 18).forEach(event->{
            IntStream.rangeClosed(1, 13).forEach(tournamentId->{
            this.tournamentService.updatePointsRaceGroupResult(event, tournamentId);
            });
        });
    }

    @ParameterizedTest
    @CsvSource({"8"})
    void updateBattleRaceGroupResult(int event) {
        IntStream.rangeClosed(1, 5).forEach(tournamentId -> {
            System.out.println("event: " + event + ", tournament: " + tournamentId);
            this.tournamentService.updateBattleRaceGroupResult(event, tournamentId);
        });
    }

    @ParameterizedTest
    @CsvSource({"28, 15"})
    void updateKnockoutResult(int event1, int tournament) {
        IntStream.rangeClosed(20, 22).forEach(event -> {
            System.out.println("event: " + event + ", tournament: " + tournament);
            this.tournamentService.updateKnockoutResult(event, tournament);
        });
    }

}
