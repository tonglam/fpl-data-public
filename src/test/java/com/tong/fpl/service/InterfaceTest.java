package com.tong.fpl.service;

import com.tong.fpl.FplDataApplicationTests;
import com.tong.fpl.domain.data.response.*;
import com.tong.fpl.domain.data.userpick.Pick;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

/**
 * Create by tong on 2021/8/30
 */
public class InterfaceTest extends FplDataApplicationTests {

    @Autowired
    private IInterfaceService interfaceService;

    @Test
    void getBootstrapStatic() {
        Optional<StaticRes> res = this.interfaceService.getBootstrapStatic();
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"3"})
    void getEventFixture(int event) {
        Optional<List<EventFixturesRes>> res = this.interfaceService.getEventFixture(event);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1713"})
    void getEntry(int entry) {
        Optional<EntryRes> res = this.interfaceService.getEntry(entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"3"})
    void getEntryCup(int event) {
        Optional<EntryCupRes> res = this.interfaceService.getEntryCup(event);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"3, 1713"})
    void getUserPicks(int event, int entry) {
        Optional<UserPicksRes> res = this.interfaceService.getUserPicks(event, entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1713"})
    void getUserHistory(int entry) {
        Optional<UserHistoryRes> res = this.interfaceService.getUserHistory(entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1713"})
    void getUserTransfers(int entry) {
        Optional<List<UserTransfersRes>> res = this.interfaceService.getUserTransfers(entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"3"})
    void getEventLive(int event) {
        Optional<EventLiveRes> res = this.interfaceService.getEventLive(event);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"233"})
    void getElementSummary(int element) {
        Optional<ElementSummaryRes> res = this.interfaceService.getElementSummary(element);
        System.out.println(1);
    }

    @Test
    void adjdha() {
        int element = 360;
        int entry = 0;
        for (int i = 0; i < 8000000; i++) {
            Optional<UserPicksRes> result = this.interfaceService.getUserPicks(19, i);
            if (result.isPresent()) {
                UserPicksRes o = result.get();
                List<Pick> picks = o.getPicks();
                if (CollectionUtils.isEmpty(picks)) {
                    return;
                }
                for (Pick pick : picks) {
                    if (pick.getPosition() < 12 && pick.getElement() == element) {
                        entry = o.getEntry();
                        System.out.println(entry);
                        break;
                    }
                }
            }
        }
        System.out.println(entry);
    }

}
