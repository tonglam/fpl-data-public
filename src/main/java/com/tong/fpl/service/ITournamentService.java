package com.tong.fpl.service;

/**
 * Create by tong on 2021/9/2
 */
public interface ITournamentService {

    void upsertTournamentEventResult(int event, int tournamentId);

    void updatePointsRaceGroupResult(int event, int tournamentId);

    void updateBattleRaceGroupResult(int event, int tournamentId);

    void updateKnockoutResult(int event, int tournamentId);

}
