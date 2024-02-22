package com.tong.fpl.service;

/**
 * Create by tong on 2021/8/31
 */
public interface IReportService {

    void insertLeagueEventPick(int event, int leagueId);

    void updateLeagueEventResult(int event, int leagueId);

}
