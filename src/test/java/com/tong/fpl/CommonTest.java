package com.tong.fpl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.tong.fpl.config.mp.MybatisPlusConfig;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.IRedisCacheService;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.CommonUtils;
import com.tong.fpl.utils.JsonUtils;
import com.tong.fpl.utils.RedisUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Create by tong on 2021/8/31
 */
public class CommonTest extends FplDataApplicationTests {

    @Autowired
    private IQueryService queryService;
    @Autowired
    private IRedisCacheService redisCacheService;
    @Autowired
    private TournamentInfoService tournamentInfoService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private EventFixtureService eventFixtureService;
    @Autowired
    private EntryInfoService entryInfoService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private LeagueEventReportService leagueEventReportService;
    @Autowired
    private EntryEventResultService entryEventResultService;


    @ParameterizedTest
    @CsvSource({"1"})
    void db(int id) {
        TournamentInfoEntity tournamentInfoEntity = this.tournamentInfoService.getOne(new QueryWrapper<TournamentInfoEntity>().lambda()
                .eq(TournamentInfoEntity::getId, id));
        System.out.println(1);
    }

    @Test
    void redis() {
        String key = "insertLeagueEventReport";
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        List<Integer> list = Lists.newArrayList(1353, 1169, 1594, 484438, 1289837, 65, 314);
        Map<String, Object> valueMap = Maps.newHashMap();
        valueMap.put("3", list);
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    @Test
    void readRedis() {
        String key = "PlayerValueEntity::20211020";
        Boolean a = this.redisTemplate.hasKey(key);
        System.out.println(Objects.equals(a, Boolean.TRUE));
    }

    @ParameterizedTest
    @CsvSource("1617")
    void test(String season) {
        List<EventFixtureEntity> updateList = Lists.newArrayList();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(Constant.DATETIME);
        MybatisPlusConfig.season.set(season);
        this.eventFixtureService.list().forEach(o -> {
            LocalDateTime kickoffTime = LocalDateTime.parse(o.getKickoffTime().replaceAll(" ", "T"));
            int diffHour = this.getDiffHour(season, kickoffTime);
            o.setKickoffTime(kickoffTime.plusHours(diffHour).format(dateTimeFormatter));
            updateList.add(o);
        });
        this.eventFixtureService.updateBatchById(updateList);
        MybatisPlusConfig.season.remove();
    }

    private int getDiffHour(String season, LocalDateTime kickoffTime) {
        LocalDateTime start = LocalDateTime.of(Integer.parseInt("20" + season.substring(0, 2)), 11, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(Integer.parseInt("20" + season.substring(2, 4)), 3, 27, 23, 59, 59);
        if (kickoffTime.isAfter(start) && kickoffTime.isBefore(end)) {
            return 8;
        }
        return 7;
    }

    @Test
    void fix() {
        Map<Integer, EntryInfoEntity> map = Maps.newHashMap();
        Multimap<Integer, String> usedNameMultiMap = HashMultimap.create();
        this.entryInfoService.list().forEach(o -> {
            // used entry name list
            List<String> list = Lists.newArrayList();
            List<String> usedList = JsonUtils.json2Collection(o.getUsedEntryName(), List.class, String.class);
            if (CollectionUtils.isEmpty(usedList)) {
                return;
            }
            usedList.forEach(i -> {
                if (!list.contains(i) && !StringUtils.equals(i, o.getEntryName())) {
                    list.add(i);
                }
            });
            if (CollectionUtils.isEmpty(list)) {
                return;
            }
            String lastEntryName = list.get(list.size() - 1);
            o.setLastEntryName(lastEntryName);
            map.put(o.getEntry(), o);
        });
        this.entryInfoService.updateBatchById(new ArrayList<>(map.values()));
    }

    @ParameterizedTest
    @CsvSource("1920, 13861")
    void firstGw(String season, int entry) {
        Map<String, PlayerEntity> playerEntityMap = this.redisCacheService.getPlayerMap(season);
        MybatisPlusConfig.season.set(season);
        LeagueEventReportEntity leagueEventReportEntity = this.leagueEventReportService.getOne(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getEvent, 1)
                .eq(LeagueEventReportEntity::getLeagueId, 65)
                .eq(LeagueEventReportEntity::getEntry, entry));
        if (leagueEventReportEntity == null) {
            return;
        }
        MybatisPlusConfig.season.remove();
        System.out.println(
                "1-" + playerEntityMap.get(String.valueOf(leagueEventReportEntity.getPosition1())).getWebName() + "[" + playerEntityMap.get(String.valueOf(leagueEventReportEntity.getPosition1())).getStartPrice() / 10 + "m]" + "\n"
                        + "2-" + playerEntityMap.get(String.valueOf(leagueEventReportEntity.getPosition2())).getWebName() + "[" + playerEntityMap.get(String.valueOf(leagueEventReportEntity.getPosition2())).getStartPrice() / 10 + "m]" + "\n"
                        + "3-" + playerEntityMap.get(String.valueOf(leagueEventReportEntity.getPosition3())).getWebName() + "[" + playerEntityMap.get(String.valueOf(leagueEventReportEntity.getPosition3())).getStartPrice() / 10 + "m]" + "\n"
                        + "4-" + playerEntityMap.get(String.valueOf(leagueEventReportEntity.getPosition4())).getWebName() + "[" + playerEntityMap.get(String.valueOf(leagueEventReportEntity.getPosition4())).getStartPrice() / 10 + "m]" + "\n"
                        + "5-" + playerEntityMap.get(String.valueOf(leagueEventReportEntity.getPosition5())).getWebName() + "[" + playerEntityMap.get(String.valueOf(leagueEventReportEntity.getPosition5())).getStartPrice() / 10 + "m]" + "\n"
                        + "6-" + playerEntityMap.get(String.valueOf(leagueEventReportEntity.getPosition6())).getWebName() + "[" + playerEntityMap.get(String.valueOf(leagueEventReportEntity.getPosition6())).getStartPrice() / 10 + "m]" + "\n"
                        + "7-" + playerEntityMap.get(String.valueOf(leagueEventReportEntity.getPosition7())).getWebName() + "[" + playerEntityMap.get(String.valueOf(leagueEventReportEntity.getPosition7())).getStartPrice() / 10 + "m]" + "\n"
                        + "8-" + playerEntityMap.get(String.valueOf(leagueEventReportEntity.getPosition8())).getWebName() + "[" + playerEntityMap.get(String.valueOf(leagueEventReportEntity.getPosition8())).getStartPrice() / 10 + "m]" + "\n"
                        + "9-" + playerEntityMap.get(String.valueOf(leagueEventReportEntity.getPosition9())).getWebName() + "[" + playerEntityMap.get(String.valueOf(leagueEventReportEntity.getPosition9())).getStartPrice() / 10 + "m]" + "\n"
                        + "10-" + playerEntityMap.get(String.valueOf(leagueEventReportEntity.getPosition10())).getWebName() + "[" + playerEntityMap.get(String.valueOf(leagueEventReportEntity.getPosition10())).getStartPrice() / 10 + "m]" + "\n"
                        + "11-" + playerEntityMap.get(String.valueOf(leagueEventReportEntity.getPosition11())).getWebName() + "[" + playerEntityMap.get(String.valueOf(leagueEventReportEntity.getPosition11())).getStartPrice() / 10 + "m]" + "\n"
                        + "12-" + playerEntityMap.get(String.valueOf(leagueEventReportEntity.getPosition12())).getWebName() + "[" + playerEntityMap.get(String.valueOf(leagueEventReportEntity.getPosition12())).getStartPrice() / 10 + "m]" + "\n"
                        + "13-" + playerEntityMap.get(String.valueOf(leagueEventReportEntity.getPosition13())).getWebName() + "[" + playerEntityMap.get(String.valueOf(leagueEventReportEntity.getPosition13())).getStartPrice() / 10 + "m]" + "\n"
                        + "14-" + playerEntityMap.get(String.valueOf(leagueEventReportEntity.getPosition14())).getWebName() + "[" + playerEntityMap.get(String.valueOf(leagueEventReportEntity.getPosition14())).getStartPrice() / 10 + "m]" + "\n"
                        + "15-" + playerEntityMap.get(String.valueOf(leagueEventReportEntity.getPosition15())).getWebName() + "[" + playerEntityMap.get(String.valueOf(leagueEventReportEntity.getPosition15())).getStartPrice() / 10 + "m]"
        );
    }

    @ParameterizedTest
    @CsvSource("1920, 13861")
    void firstGw2(String season, int entry) {
        Map<String, PlayerEntity> playerEntityMap = this.redisCacheService.getPlayerMap(season);
        MybatisPlusConfig.season.set(season);
        EntryEventResultEntity entryEventResultEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEvent, 1)
                .eq(EntryEventResultEntity::getEntry, entry));
        if (entryEventResultEntity == null) {
            return;
        }
        List<EntryEventPickEntity> list = JsonUtils.json2Collection(entryEventResultEntity.getEventPicks(), List.class, String.class);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(o -> {
            System.out.println();
        });
        MybatisPlusConfig.season.remove();

    }

    @ParameterizedTest
    @CsvSource("微信截图_20221122151827.png")
    void tesssst(String a) throws UnsupportedEncodingException {
        System.out.println(CommonUtils.getCurrentSeason());


        int x = 13;
        int y = 1;
        if (x > 5) {
            y = 2;
        }
        if (x > 12) {
            y = 3;
        } else if (x > 5) {
            y = 4;
        } else {
            y = 5;
        }
        System.out.println(y);
    }

}
