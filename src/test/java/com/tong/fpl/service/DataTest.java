package com.tong.fpl.service;

import com.tong.fpl.FplDataApplicationTests;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by tong on 2022/08/08
 */
public class DataTest extends FplDataApplicationTests {

    @Autowired
    private IDataService dataService;

    @ParameterizedTest
    @CsvSource("1, 官推")
    void updateEventSourceScoutResult(int event, String source) {
        this.dataService.updateEventSourceScoutResult(event, source);
    }

    @Test
    void insertPlayerValueInfo() throws Exception {
        this.dataService.insertPlayerValueInfo();
    }

}
