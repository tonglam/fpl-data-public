package com.tong.fpl.service;

import com.tong.fpl.FplDataApplicationTests;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Create by tong on 2021/8/31
 */
public class ReportTest extends FplDataApplicationTests {

    @Autowired
    private IReportService reportService;

    @ParameterizedTest
    @CsvSource({"4, 65"})
    void insertLeagueEventPick(int event, int leagueId) {
        this.reportService.insertLeagueEventPick(event, leagueId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"4, 65"})
    void updateLeagueEventResult(int event, int leagueId) {
        this.reportService.updateLeagueEventResult(event, leagueId);
        System.out.println(1);
    }

}
