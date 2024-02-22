package com.tong.fpl.utils;

import com.google.common.collect.Lists;
import com.tong.fpl.constant.Constant;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Create by tong on 2021/8/30
 */
@Component
public class CommonUtils {

    public static String getLocalZoneDateTime(String time) {
        ZoneId zoneId = ZonedDateTime.now().getZone();
        return LocalDateTime.ofInstant(Instant.parse(time), zoneId).atZone(zoneId).format(DateTimeFormatter.ofPattern(Constant.DATETIME));
    }

    public static List<String> getAllSeasons() {
        int current = Integer.parseInt(getCurrentSeason());
        List<String> list = Lists.newArrayList();
        for (int i = 1617; i < 9999; i = i + 101) {
            if (i > current) {
                break;
            } else {
                list.add(String.valueOf(i));
            }
        }
        return list;
    }

    public static String getCurrentSeason() {
        if (LocalDate.now().getMonth().getValue() < 6) {
            return String.valueOf(LocalDate.now().minusYears(1).getYear()).substring(2, 4) +
                    String.valueOf(LocalDate.now().getYear()).substring(2, 4);
        }
        return String.valueOf(LocalDate.now().getYear()).substring(2, 4) +
                String.valueOf(LocalDate.now().plusYears(1).getYear()).substring(2, 4);
    }

}
