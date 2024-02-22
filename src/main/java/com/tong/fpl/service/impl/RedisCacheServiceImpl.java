package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.*;
import com.tong.fpl.config.collector.LiveFixtureCollector;
import com.tong.fpl.config.collector.PlayerValueCollector;
import com.tong.fpl.config.mp.MybatisPlusConfig;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.constant.enums.MatchPlayStatus;
import com.tong.fpl.constant.enums.ValueChangeType;
import com.tong.fpl.domain.data.bootstrapStaic.Player;
import com.tong.fpl.domain.data.eventLive.ElementExplain;
import com.tong.fpl.domain.data.eventLive.ElementStat;
import com.tong.fpl.domain.data.response.ElementSummaryRes;
import com.tong.fpl.domain.data.response.EventFixturesRes;
import com.tong.fpl.domain.data.response.EventLiveRes;
import com.tong.fpl.domain.data.response.StaticRes;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.event.EventChipData;
import com.tong.fpl.domain.letletme.event.EventOverallResultData;
import com.tong.fpl.domain.letletme.event.EventTopElementData;
import com.tong.fpl.domain.letletme.live.LiveFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerSeasonSummaryData;
import com.tong.fpl.service.IRedisCacheService;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.CommonUtils;
import com.tong.fpl.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Create by tong on 2021/8/30
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RedisCacheServiceImpl implements IRedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private final InterfaceServiceImpl interfaceService;

    private final TeamService teamService;
    private final EventService eventService;
    private final EventFixtureService eventFixtureService;
    private final EventLiveService eventLiveService;
    private final EventLiveExplainService eventLiveExplainService;
    private final EventLiveSummaryService eventLiveSummaryService;
    private final PlayerService playerService;
    private final PlayerStatService playerStatService;
    private final PlayerValueService playerValueService;
    private final PlayerHistoryService playerHistoryService;
    private final PlayerSummaryService playerSummaryService;

    private final ForkJoinPool forkJoinPool = new ForkJoinPool(4);

    @Override
    public void insertTeam(StaticRes staticRes) {
        if (staticRes == null) {
            return;
        }
        // insert table
        this.teamService.getBaseMapper().truncate();
        List<TeamEntity> teamList = Lists.newArrayList();
        staticRes.getTeams().forEach(bootstrapTeam ->
                teamList.add(
                        new TeamEntity()
                                .setId(bootstrapTeam.getId())
                                .setCode(bootstrapTeam.getCode())
                                .setName(bootstrapTeam.getName())
                                .setShortName(bootstrapTeam.getShortName())
                ));
        this.teamService.saveBatch(teamList);
        log.info("insert team size:{}!", teamList.size());
        // set cache
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        String season = CommonUtils.getCurrentSeason();
        // set team_name cache
        this.setTeamNameCache(cacheMap, teamList, season);
        // set team_short_name cache
        this.setTeamShortNameCache(cacheMap, teamList, season);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    @Override
    public void insertHisTeam(String season) {
        MybatisPlusConfig.season.set(season);
        List<TeamEntity> teamList = this.teamService.list();
        MybatisPlusConfig.season.remove();
        if (CollectionUtils.isEmpty(teamList)) {
            return;
        }
        // set cache
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        // set team_name cache
        this.setTeamNameCache(cacheMap, teamList, season);
        // set team_short_name cache
        this.setTeamShortNameCache(cacheMap, teamList, season);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    private void setTeamNameCache(Map<String, Map<String, Object>> cacheMap, List<TeamEntity> teamList, String season) {
        Map<String, Object> valueMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", TeamEntity.class.getSimpleName(), season, "name");
        RedisUtils.removeCacheByKey(key);
        teamList.forEach(o -> valueMap.put(String.valueOf(o.getId()), o.getName()));
        cacheMap.put(key, valueMap);
    }

    private void setTeamShortNameCache(Map<String, Map<String, Object>> cacheMap, List<TeamEntity> teamList, String season) {
        Map<String, Object> valueMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", TeamEntity.class.getSimpleName(), season, "shortName");
        RedisUtils.removeCacheByKey(key);
        teamList.forEach(o -> valueMap.put(String.valueOf(o.getId()), o.getShortName()));
        cacheMap.put(key, valueMap);
    }

    @Override
    public void insertEvent(StaticRes staticRes) {
        // insert table
        this.eventService.getBaseMapper().truncate();
        List<EventEntity> eventList = Lists.newArrayList();
        staticRes.getEvents().forEach(bootstrapEvent -> {
            EventEntity eventEntity = new EventEntity();
            BeanUtil.copyProperties(bootstrapEvent, eventEntity, CopyOptions.create().ignoreNullValue());
            eventEntity
                    .setId(bootstrapEvent.getId())
                    .setDeadlineTime(bootstrapEvent.getDeadlineTime());
            eventList.add(eventEntity);
        });
        this.eventService.saveBatch(eventList);
        log.info("insert event size:{}!", eventList.size());
        // set cache
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", EventEntity.class.getSimpleName(), CommonUtils.getCurrentSeason());
        RedisUtils.removeCacheByKey(key);
        eventList.forEach(o -> valueMap.put(String.valueOf(o.getId()), o.getDeadlineTime()));
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    @Override
    public void insertHisEvent(String season) {
        MybatisPlusConfig.season.set(season);
        List<EventEntity> eventList = this.eventService.list();
        MybatisPlusConfig.season.remove();
        if (CollectionUtils.isEmpty(eventList)) {
            return;
        }
        // set cache
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", EventEntity.class.getSimpleName(), season);
        RedisUtils.removeCacheByKey(key);
        eventList.forEach(o -> valueMap.put(String.valueOf(o.getId()), o.getDeadlineTime()));
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    @Override
    public void insertEventFixture() {
        List<EventFixtureEntity> fixtureList = Lists.newArrayList();
        Map<String, Set<Object>> cacheMap = Maps.newHashMap();
        // set cache by event
        IntStream.rangeClosed(1, 38).forEach(event -> {
            List<EventFixturesRes> eventFixturesResList = this.interfaceService.getEventFixture(event).orElse(null);
            if (CollectionUtils.isEmpty(eventFixturesResList)) {
                return;
            }
            fixtureList.addAll(this.insertEventFixtureByEvent(cacheMap, event, eventFixturesResList));
        });
        // set cache by team
        RedisUtils.pipelineSetCache(cacheMap, -1, null);
        this.insertEventFixtureCacheByTeam(CommonUtils.getCurrentSeason(), fixtureList);
    }

    private List<EventFixtureEntity> insertEventFixtureByEvent(Map<String, Set<Object>> cacheMap, int event, List<EventFixturesRes> eventFixturesResList) {
        log.info("start insert event:{} fixtures!", event);
        List<EventFixtureEntity> eventFixtureList = Lists.newArrayList();
        this.eventFixtureService.remove(new QueryWrapper<EventFixtureEntity>().lambda().eq(EventFixtureEntity::getEvent, event));
        eventFixturesResList.forEach(o -> {
            EventFixtureEntity eventFixtureEntity = new EventFixtureEntity();
            BeanUtil.copyProperties(o, eventFixtureEntity, CopyOptions.create().ignoreNullValue());
            eventFixtureEntity.setKickoffTime(CommonUtils.getLocalZoneDateTime(o.getKickoffTime()));
            eventFixtureList.add(eventFixtureEntity);
        });
        this.eventFixtureService.saveBatch(eventFixtureList);
        log.info("insert event:{}, event_fixture size:{}!", event, eventFixtureList.size());
        // set cache by event
        this.setEventFixtureCacheBySingleEvent(cacheMap, CommonUtils.getCurrentSeason(), event, eventFixtureList);
        return eventFixtureList;
    }

    private void setEventFixtureCacheBySingleEvent(Map<String, Set<Object>> cacheMap, String season, int event, Collection<EventFixtureEntity> eventFixtureList) {
        String key = StringUtils.joinWith("::", EventFixtureEntity.class.getSimpleName(), season, "event", event);
        Set<Object> valueSet = Sets.newHashSet();
        RedisUtils.removeCacheByKey(key);
        valueSet.addAll(eventFixtureList);
        cacheMap.put(key, valueSet);
    }

    private void insertEventFixtureCacheByTeam(String season, Collection<EventFixtureEntity> fixtureList) {
        Map<String, String> teamNameMap = this.getTeamNameMap(season);
        Map<String, String> teamShortNameMap = this.getTeamShortNameMap(season);
        IntStream.rangeClosed(1, 20).forEach(teamId -> this.insertEventFixtureCacheBySingleTeam(season, fixtureList, teamId, teamNameMap, teamShortNameMap));
    }

    private void insertEventFixtureCacheBySingleTeam(String season, Collection<EventFixtureEntity> fixtureList, int teamId, Map<String, String> teamNameMap, Map<String, String> teamShortNameMap) {
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", EventFixtureEntity.class.getSimpleName(), season, "teamId", teamId);
        RedisUtils.removeCacheByKey(key);
        Map<String, Object> valueMap = this.setEventFixtureValueBySingleTeam(fixtureList, teamId, teamNameMap, teamShortNameMap);
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    private Map<String, Object> setEventFixtureValueBySingleTeam(Collection<EventFixtureEntity> fixtureList, int teamId, Map<String, String> teamNameMap, Map<String, String> teamShortNameMap) {
        Multimap<String, Object> eventFixtureMap = HashMultimap.create();
        // home game
        fixtureList.stream()
                .filter(o -> o.getTeamH() == teamId)
                .forEach(o -> eventFixtureMap.put(String.valueOf(o.getEvent()), new PlayerFixtureData()
                        .setEvent(o.getEvent())
                        .setTeamId(teamId)
                        .setTeamName(teamNameMap.getOrDefault(String.valueOf(teamId), ""))
                        .setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(teamId), ""))
                        .setAgainstTeamId(o.getTeamA())
                        .setAgainstTeamName(teamNameMap.getOrDefault(String.valueOf(o.getTeamA()), ""))
                        .setAgainstTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getTeamA()), ""))
                        .setDifficulty(o.getTeamHDifficulty())
                        .setKickoffTime(o.getKickoffTime())
                        .setStarted(o.getStarted())
                        .setFinished(o.getFinished())
                        .setWasHome(true)
                        .setTeamScore(o.getTeamHScore())
                        .setAgainstTeamScore(o.getTeamAScore())
                        .setScore(o.getTeamHScore() + "-" + o.getTeamAScore())
                        .setResult(this.getTeamEventFixtureResult(o.getFinished(), o.getTeamHScore(), o.getTeamAScore()))
                ));
        // away game
        fixtureList.stream()
                .filter(o -> o.getTeamA() == teamId)
                .forEach(o -> eventFixtureMap.put(String.valueOf(o.getEvent()), new PlayerFixtureData()
                        .setEvent(o.getEvent())
                        .setTeamId(teamId)
                        .setTeamName(teamNameMap.getOrDefault(String.valueOf(teamId), ""))
                        .setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(teamId), ""))
                        .setAgainstTeamId(o.getTeamH())
                        .setAgainstTeamName(teamNameMap.getOrDefault(String.valueOf(o.getTeamH()), ""))
                        .setAgainstTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getTeamH()), ""))
                        .setDifficulty(o.getTeamADifficulty())
                        .setKickoffTime(o.getKickoffTime())
                        .setStarted(o.getStarted())
                        .setFinished(o.getFinished())
                        .setWasHome(false)
                        .setTeamScore(o.getTeamAScore())
                        .setAgainstTeamScore(o.getTeamHScore())
                        .setScore(o.getTeamAScore() + "-" + o.getTeamHScore())
                        .setResult(this.getTeamEventFixtureResult(o.getFinished(), o.getTeamAScore(), o.getTeamHScore()))
                ));
        Map<String, Object> valueMap = Maps.newHashMap();
        eventFixtureMap.keySet().forEach(event -> {
            List<PlayerFixtureData> list = Lists.newArrayList();
            eventFixtureMap.get(event).forEach(o -> list.add((PlayerFixtureData) o));
            valueMap.put(event, list);
        });
        return valueMap;
    }

    private String getTeamEventFixtureResult(boolean finished, int teamScore, int againstTeamScore) {
        if (!finished) {
            return "";
        }
        if (teamScore > againstTeamScore) {
            return "W";
        } else if (teamScore < againstTeamScore) {
            return "L";
        }
        return "D";
    }

    @Override
    public void insertHisEventFixture(String season) {
        MybatisPlusConfig.season.set(season);
        Multimap<Integer, EventFixtureEntity> eventFixtureMap = HashMultimap.create();
        this.eventFixtureService.list().forEach(o -> eventFixtureMap.put(o.getEvent(), o));
        MybatisPlusConfig.season.remove();
        if (eventFixtureMap.isEmpty()) {
            return;
        }
        // set cache by event
        Map<String, Set<Object>> cacheMap = Maps.newHashMap();
        eventFixtureMap.keySet().forEach(event -> this.setEventFixtureCacheBySingleEvent(cacheMap, season, event, eventFixtureMap.get(event)));
        // set cache by team
        this.insertEventFixtureCacheByTeam(season, eventFixtureMap.values());
        RedisUtils.pipelineSetCache(cacheMap, -1, null);
    }

    @Override
    public void insertSingleEventFixture(int event, List<EventFixturesRes> eventFixturesResList) {
        if (CollectionUtils.isEmpty(eventFixturesResList)) {
            return;
        }
        this.eventFixtureService.remove(new QueryWrapper<EventFixtureEntity>().lambda().eq(EventFixtureEntity::getEvent, event));
        List<EventFixtureEntity> eventFixtureList = Lists.newArrayList();
        eventFixturesResList.forEach(o -> {
            EventFixtureEntity eventFixtureEntity = new EventFixtureEntity();
            BeanUtil.copyProperties(o, eventFixtureEntity, CopyOptions.create().ignoreNullValue());
            eventFixtureEntity.setKickoffTime(CommonUtils.getLocalZoneDateTime(o.getKickoffTime()));
            eventFixtureList.add(eventFixtureEntity);
        });
        this.eventFixtureService.saveBatch(eventFixtureList);
        log.info("insert event:{}, event_fixture size:{}!", event, eventFixtureList.size());
        // set cache by event
        Map<String, Set<Object>> cacheMap = Maps.newHashMap();
        this.setEventFixtureCacheBySingleEvent(cacheMap, CommonUtils.getCurrentSeason(), event, eventFixtureList);
        RedisUtils.pipelineSetCache(cacheMap, -1, null);
        // set cache by team
        Map<String, String> teamNameMap = this.getTeamNameMap(CommonUtils.getCurrentSeason());
        Map<String, String> teamShortNameMap = this.getTeamShortNameMap(CommonUtils.getCurrentSeason());
        IntStream.rangeClosed(1, 20).forEach(teamId ->
                this.insertEventFixtureCacheBySingleTeamAndEvent(CommonUtils.getCurrentSeason(), eventFixtureList, teamId, teamNameMap, teamShortNameMap, event));
    }

    @Override
    public void insertSingleEventFixtureCache(int event, List<EventFixturesRes> eventFixturesResList) {
        if (CollectionUtils.isEmpty(eventFixturesResList)) {
            return;
        }
        List<EventFixtureEntity> eventFixtureList = Lists.newArrayList();
        eventFixturesResList.forEach(o -> {
            EventFixtureEntity eventFixtureEntity = new EventFixtureEntity();
            BeanUtil.copyProperties(o, eventFixtureEntity, CopyOptions.create().ignoreNullValue());
            eventFixtureEntity.setKickoffTime(CommonUtils.getLocalZoneDateTime(o.getKickoffTime()));
            eventFixtureList.add(eventFixtureEntity);
        });
        // set cache by event
        Map<String, Set<Object>> cacheMap = Maps.newHashMap();
        this.setEventFixtureCacheBySingleEvent(cacheMap, CommonUtils.getCurrentSeason(), event, eventFixtureList);
        RedisUtils.pipelineSetCache(cacheMap, -1, null);
        // set cache by team
        Map<String, String> teamNameMap = this.getTeamNameMap(CommonUtils.getCurrentSeason());
        Map<String, String> teamShortNameMap = this.getTeamShortNameMap(CommonUtils.getCurrentSeason());
        IntStream.rangeClosed(1, 20).forEach(teamId ->
                this.insertEventFixtureCacheBySingleTeamAndEvent(CommonUtils.getCurrentSeason(), eventFixtureList, teamId, teamNameMap, teamShortNameMap, event));
    }

    @Override
    public void insertLiveFixtureCache() {
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        String season = CommonUtils.getCurrentSeason();
        int event = this.getCurrentEvent();
        Table<Integer, MatchPlayStatus, List<LiveFixtureData>> table = this.getEventFixtureByEvent(season, event)
                .stream()
                .collect(new LiveFixtureCollector(this.getTeamNameMap(season), this.getTeamShortNameMap(season)));
        Map<String, Object> valueMap = Maps.newHashMap();
        table.rowKeySet().forEach(teamId -> {
            Map<MatchPlayStatus, List<LiveFixtureData>> map = Maps.newHashMap();
            // playing
            List<LiveFixtureData> playingList = Lists.newArrayList();
            if (table.contains(teamId, MatchPlayStatus.Playing)) {
                playingList = table.get(teamId, MatchPlayStatus.Playing);
            }
            map.put(MatchPlayStatus.Playing, playingList);
            // not start
            List<LiveFixtureData> notStartList = Lists.newArrayList();
            if (table.contains(teamId, MatchPlayStatus.Not_Start)) {
                notStartList = table.get(teamId, MatchPlayStatus.Not_Start);
            }
            map.put(MatchPlayStatus.Not_Start, notStartList);
            // finished
            List<LiveFixtureData> finishedList = Lists.newArrayList();
            if (table.contains(teamId, MatchPlayStatus.Finished)) {
                finishedList = table.get(teamId, MatchPlayStatus.Finished);
            }
            map.put(MatchPlayStatus.Finished, finishedList);
            valueMap.put(String.valueOf(teamId), map);
        });
        // set cache
        String key = StringUtils.joinWith("::", LiveFixtureData.class.getSimpleName());
        RedisUtils.removeCacheByKey(key);
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    private void insertEventFixtureCacheBySingleTeamAndEvent(String season, Collection<EventFixtureEntity> fixtureList, int teamId, Map<String, String> teamNameMap, Map<String, String> teamShortNameMap, int event) {
        String key = StringUtils.joinWith("::", EventFixtureEntity.class.getSimpleName(), season, "teamId", teamId);
        String HashKey = String.valueOf(event);
        this.redisTemplate.opsForHash().delete(key, HashKey);
        Map<String, Object> valueMap = this.setEventFixtureValueBySingleTeam(fixtureList, teamId, teamNameMap, teamShortNameMap);
        this.redisTemplate.opsForHash().put(key, HashKey, valueMap.get(HashKey));
    }

    @Override
    public void insertPlayer(StaticRes staticRes) {
        if (staticRes == null) {
            return;
        }
        Map<String, PlayerEntity> playerMap = this.getPlayerMap(CommonUtils.getCurrentSeason());
        if (!this.needInsertPlayer(playerMap, staticRes.getElements())) {
            log.info("no new players data");
            return;
        }
        this.playerService.getBaseMapper().truncate();
        List<PlayerEntity> insertList = Lists.newArrayList();
        staticRes.getElements().forEach(o ->
                insertList.add(
                        new PlayerEntity()
                                .setElement(o.getId())
                                .setCode(o.getCode())
                                .setPrice(o.getNowCost())
                                .setStartPrice(o.getNowCost() - o.getCostChangeStart())
                                .setElementType(o.getElementType())
                                .setFirstName(o.getFirstName())
                                .setSecondName(o.getSecondName())
                                .setWebName(o.getWebName())
                                .setTeamId(o.getTeam())
                ));
        this.playerService.saveBatch(insertList);
        log.info("insert player size:{}!", insertList.size());
        // set cache
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", PlayerEntity.class.getSimpleName(), CommonUtils.getCurrentSeason());
        Map<String, Object> valueMap = Maps.newHashMap();
        RedisUtils.removeCacheByKey(key);
        this.playerService.list().forEach(o -> valueMap.put(String.valueOf(o.getElement()), o));
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    private boolean needInsertPlayer(Map<String, PlayerEntity> playerMap, List<Player> elements) {
        if (playerMap.size() != elements.size()) {
            return true;
        }
        for (Player player :
                elements) {
            PlayerEntity playerEntity = playerMap.getOrDefault(String.valueOf(player.getId()), null);
            if (playerEntity == null) {
                return true;
            }
            if (player.getTeam() != playerEntity.getTeamId() || player.getNowCost() != playerEntity.getPrice()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void insertHisPlayer(String season) {
        MybatisPlusConfig.season.set(season);
        List<PlayerEntity> playerList = this.playerService.list();
        MybatisPlusConfig.season.remove();
        if (CollectionUtils.isEmpty(playerList)) {
            return;
        }
        // set cache
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", PlayerEntity.class.getSimpleName(), season);
        Map<String, Object> valueMap = Maps.newHashMap();
        RedisUtils.removeCacheByKey(key);
        playerList.forEach(o -> valueMap.put(String.valueOf(o.getElement()), o));
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    @Override
    public void insertPlayerStat(StaticRes staticRes) {
        if (staticRes == null) {
            return;
        }
        List<PlayerStatEntity> insertList = Lists.newArrayList();
        List<PlayerStatEntity> updateList = Lists.newArrayList();
        // prepare
        int event = this.getCurrentEvent();
        Map<Integer, Integer> insertTeamMap = this.getInsertTeamList();
        Map<Integer, Integer> playerStatIdMap = this.playerStatService.list(new QueryWrapper<PlayerStatEntity>().lambda()
                        .eq(PlayerStatEntity::getEvent, event))
                .stream()
                .collect(Collectors.toMap(PlayerStatEntity::getElement, PlayerStatEntity::getId));
        // get from fpl server
        staticRes.getElements().forEach(o -> {
            if (!insertTeamMap.containsKey(o.getTeam())) {
                return;
            }
            // insert or update table
            PlayerStatEntity playerStatEntity = this.initPlayStat(event, o, insertTeamMap);
            int element = playerStatEntity.getElement();
            if (!playerStatIdMap.containsKey(element)) {
                insertList.add(playerStatEntity);
            } else {
                playerStatEntity.setId(playerStatIdMap.get(element));
                updateList.add(playerStatEntity);
            }
        });
        // insert
        this.playerStatService.saveBatch(insertList);
        log.info("insert player_stat size:{}", insertList.size());
        // update
        this.playerStatService.updateBatchById(updateList);
        log.info("update player_stat size:{}", updateList.size());
        // set cache
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", PlayerStatEntity.class.getSimpleName(), CommonUtils.getCurrentSeason());
        Map<String, Object> valueMap = Maps.newHashMap();
        RedisUtils.removeCacheByKey(key);
        this.playerStatService.list(new QueryWrapper<PlayerStatEntity>().lambda()
                        .eq(PlayerStatEntity::getEvent, event))
                .forEach(o -> valueMap.put(String.valueOf(o.getElement()), o));
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
        // player_history
        insertList.forEach(o -> this.interfaceService.getElementSummary(o.getElement()).ifPresent(this::insertSinglePlayerHistory));
    }

    private Map<Integer, Integer> getInsertTeamList() {
        Map<Integer, Integer> insertTeamMap = Maps.newHashMap();
        IntStream.rangeClosed(1, 20).forEach(teamId -> {
            // match_played
            int matchPlayed = (int) this.eventFixtureService.count(new QueryWrapper<EventFixtureEntity>().lambda()
                    .eq(EventFixtureEntity::getFinished, 1)
                    .and(a -> a.eq(EventFixtureEntity::getTeamH, teamId)
                            .or(i -> i.eq(EventFixtureEntity::getTeamA, teamId)))
            );
            insertTeamMap.put(teamId, matchPlayed);
        });
        return insertTeamMap;
    }

    private PlayerStatEntity initPlayStat(int event, Player player, Map<Integer, Integer> insertTeamMap) {
        return new PlayerStatEntity()
                .setEvent(event)
                .setElement(player.getId())
                .setCode(player.getCode())
                .setMatchPlayed(insertTeamMap.get(player.getTeam()))
                .setChanceOfPlayingNextRound(player.getChanceOfPlayingNextRound())
                .setChanceOfPlayingThisRound(player.getChanceOfPlayingThisRound())
                .setDreamteamCount(player.getDreamteamCount())
                .setEventPoints(player.getEventPoints())
                .setForm(player.getForm())
                .setInDreamteam(player.isInDreamteam())
                .setNews(player.getNews())
                .setNewsAdded(player.getNewsAdded())
                .setPointsPerGame(player.getPointsPerGame())
                .setSelectedByPercent(player.getSelectedByPercent())
                .setMinutes(player.getMinutes())
                .setGoalsScored(player.getGoalsScored())
                .setAssists(player.getAssists())
                .setCleanSheets(player.getCleanSheets())
                .setGoalsConceded(player.getGoalsConceded())
                .setOwnGoals(player.getOwnGoals())
                .setPenaltiesSaved(player.getPenaltiesSaved())
                .setPenaltiesMissed(player.getPenaltiesMissed())
                .setYellowCards(player.getYellowCards())
                .setRedCards(player.getRedCards())
                .setSaves(player.getSaves())
                .setBonus(player.getBonus())
                .setBps(player.getBps())
                .setInfluence(player.getInfluence())
                .setCreativity(player.getCreativity())
                .setThreat(player.getThreat())
                .setIctIndex(player.getIctIndex())
                .setTransfersInEvent(player.getTransfersInEvent())
                .setTransfersOutEvent(player.getTransfersOutEvent())
                .setTransfersIn(player.getTransfersIn())
                .setTransfersOut(player.getTransfersOut())
                .setCornersAndIndirectFreekicksOrder(player.getCornersAndIndirectFreekicksOrder())
                .setDirectFreekicksOrder(player.getDirectFreekicksOrder())
                .setPenaltiesOrder(player.getPenaltiesOrder());
    }

    @Override
    public void insertHisPlayerStat(String season) {
        MybatisPlusConfig.season.set(season);
        List<PlayerStatEntity> playerStatList = this.playerStatService.list();
        MybatisPlusConfig.season.remove();
        if (CollectionUtils.isEmpty(playerStatList)) {
            return;
        }
        // set cache
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", PlayerStatEntity.class.getSimpleName(), season);
        Map<String, Object> valueMap = Maps.newHashMap();
        RedisUtils.removeCacheByKey(key);
        playerStatList.forEach(o -> valueMap.put(String.valueOf(o.getElement()), o));
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    @Override
    public void insertPlayerValue(StaticRes staticRes) {
        if (staticRes == null) {
            return;
        }
        String changeDate = LocalDate.now().format(DateTimeFormatter.ofPattern(Constant.SHORTDAY));
        // prepare
        Map<Integer, PlayerValueEntity> lastValueMap = this.playerValueService.list()
                .stream()
                .collect(new PlayerValueCollector());
        Map<Integer, PlayerValueEntity> valueMap = this.playerValueService.list(new QueryWrapper<PlayerValueEntity>().lambda()
                        .eq(PlayerValueEntity::getChangeDate, changeDate))
                .stream()
                .collect(Collectors.toMap(PlayerValueEntity::getElement, o -> o));
        int event = this.getCurrentEvent();
        // calc
        List<PlayerValueEntity> playerValueList = Lists.newArrayList();
        staticRes.getElements()
                .stream()
                .filter(o -> !lastValueMap.containsKey(o.getId()) || o.getNowCost() != lastValueMap.get(o.getId()).getValue())
                .forEach(bootstrapPlayer -> {
                    int element = bootstrapPlayer.getId();
                    if (valueMap.containsKey(element)) {
                        return;
                    }
                    PlayerValueEntity lastEntity = lastValueMap.getOrDefault(element, null);
                    int lastValue = lastEntity != null ? lastEntity.getValue() : 0;
                    playerValueList.add(
                            new PlayerValueEntity()
                                    .setElement(element)
                                    .setElementType(bootstrapPlayer.getElementType())
                                    .setEvent(event)
                                    .setValue(bootstrapPlayer.getNowCost())
                                    .setChangeDate(changeDate)
                                    .setChangeType(this.getChangeType(bootstrapPlayer.getNowCost(), lastValue))
                                    .setLastValue(lastValue)
                    );
                });
        if (CollectionUtils.isEmpty(playerValueList)) {
            return;
        }
        // insert
        this.playerValueService.saveBatch(playerValueList);
        log.info("insert player value size:{}", playerValueList.size());
        // check if update the redis cache needed, only contains Rise type data need to update
        boolean noUpdateCache = playerValueList.stream().allMatch(o -> ValueChangeType.Start.name().equals(o.getChangeType()));
        if (noUpdateCache) {
            log.info("no need to update player value cache");
            return;
        }
        // set cache
        Map<String, Set<Object>> cacheMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", PlayerValueEntity.class.getSimpleName(), changeDate);
        Set<Object> valueSet = Sets.newHashSet();
        valueSet.addAll(playerValueList);
        cacheMap.put(key, valueSet);
        RedisUtils.pipelineSetCache(cacheMap, 1, TimeUnit.DAYS);
        // update price in table player
        this.updatePriceOfPlayer(playerValueList);
    }

    private String getChangeType(int nowCost, int lastCost) {
        if (lastCost == 0) {
            return ValueChangeType.Start.name();
        }
        return nowCost > lastCost ? ValueChangeType.Rise.name() : ValueChangeType.Faller.name();
    }

    private void updatePriceOfPlayer(List<PlayerValueEntity> playerValueList) {
        List<PlayerEntity> updatePlayerList = Lists.newArrayList();
        playerValueList.forEach(o -> {
            // update table
            PlayerEntity playerEntity = this.playerService.getById(o.getElement());
            playerEntity.setPrice(o.getValue());
            if (playerEntity.getStartPrice() == 0) {
                // start price
                PlayerValueEntity playerValueEntity = this.playerValueService.getOne(new QueryWrapper<PlayerValueEntity>().lambda()
                        .eq(PlayerValueEntity::getElement, o.getElement())
                        .eq(PlayerValueEntity::getChangeType, ValueChangeType.Start.name()));
                if (playerValueEntity != null) {
                    playerEntity.setStartPrice(playerValueEntity.getValue());
                }
            }
            updatePlayerList.add(playerEntity);
            // set cache
            String key = StringUtils.joinWith("::", PlayerEntity.class.getSimpleName(), CommonUtils.getCurrentSeason());
            this.redisTemplate.opsForHash().put(key, String.valueOf(o.getElement()), playerEntity);
        });
        this.playerService.updateBatchById(updatePlayerList);
        log.info("insert player value size:{}", playerValueList.size());
    }

    @Override
    public void insertPlayerHistory() {
        List<PlayerHistoryEntity> insertList = Lists.newArrayList();
        // exists
        List<Integer> existsCodeList = this.playerHistoryService.list()
                .stream()
                .map(PlayerHistoryEntity::getCode)
                .distinct()
                .toList();
        Map<Integer, Integer> playerCodeMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getCode, PlayerEntity::getElement));
        List<Integer> codeList = playerCodeMap.keySet()
                .stream()
                .filter(o -> !existsCodeList.contains(o))
                .toList();
        if (CollectionUtils.isEmpty(codeList)) {
            return;
        }
        // prepare
        List<Integer> elementList = codeList
                .stream()
                .map(playerCodeMap::get)
                .toList();
        List<CompletableFuture<ElementSummaryRes>> elementSummaryResFuture = elementList
                .stream()
                .map(o -> CompletableFuture.supplyAsync(() -> this.interfaceService.getElementSummary(o).orElse(null), forkJoinPool))
                .toList();
        Map<Integer, ElementSummaryRes> elementSummaryResMap = elementSummaryResFuture
                .stream()
                .map(CompletableFuture::join)
                .filter(o -> !o.getHistoryPast().isEmpty())
                .collect(Collectors.toMap(k -> k.getHistoryPast().get(0).getElementCode(), v -> v));
        // calc
        codeList.forEach(o -> {
            if (existsCodeList.contains(o)) {
                return;
            }
            List<PlayerHistoryEntity> playerHistoryList = this.initPlayerHistoryList(elementSummaryResMap.get(o));
            if (CollectionUtils.isEmpty(playerHistoryList)) {
                return;
            }
            insertList.addAll(playerHistoryList);
        });
        if (CollectionUtils.isEmpty(insertList)) {
            return;
        }
        this.playerHistoryService.saveBatch(insertList);
        log.info("insert player_history size is " + insertList.size() + "!");
        // set cache
        this.setPlayerHistoryCache();
    }

    private List<PlayerHistoryEntity> initPlayerHistoryList(ElementSummaryRes elementSummaryRes) {
        if (elementSummaryRes == null || CollectionUtils.isEmpty(elementSummaryRes.getHistoryPast())) {
            return null;
        }
        return elementSummaryRes.getHistoryPast()
                .stream()
                .map(o -> {
                    PlayerHistoryEntity playerHistoryEntity = BeanUtil.copyProperties(o, PlayerHistoryEntity.class);
                    if (playerHistoryEntity == null) {
                        return null;
                    }
                    playerHistoryEntity
                            .setCode(o.getElementCode())
                            .setSeason(StringUtils.substringBefore(o.getSeasonName(), '/').substring(2, 4) + StringUtils.substringAfter(o.getSeasonName(), "/"))
                            .setPrice(o.getEndCost())
                            .setStartPrice(o.getStartCost());
                    return playerHistoryEntity;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void setPlayerHistoryCache() {
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", PlayerHistoryEntity.class.getSimpleName());
        Map<String, Object> valueMap = Maps.newHashMap();
        RedisUtils.removeCacheByKey(key);
        Multimap<Integer, PlayerHistoryEntity> multiMap = HashMultimap.create();
        this.playerHistoryService.list().forEach(o -> multiMap.put(o.getCode(), o));
        multiMap.keySet().forEach(code -> {
            List<PlayerHistoryEntity> list = new ArrayList<>(multiMap.get(code));
            valueMap.put(String.valueOf(code), list);
        });
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    @Override
    public void insertSinglePlayerHistory(ElementSummaryRes elementSummaryRes) {
        if (elementSummaryRes == null || CollectionUtils.isEmpty(elementSummaryRes.getHistoryPast())) {
            return;
        }
        int code = elementSummaryRes.getHistoryPast().get(0).getElementCode();
        // check
        if (this.playerHistoryService.count(new QueryWrapper<PlayerHistoryEntity>().lambda()
                .eq(PlayerHistoryEntity::getCode, code)) > 0) {
            return;
        }
        // calc
        List<PlayerHistoryEntity> playerHistoryList = this.initPlayerHistoryList(elementSummaryRes);
        if (CollectionUtils.isEmpty(playerHistoryList)) {
            return;
        }
        this.playerHistoryService.saveBatch(playerHistoryList);
        log.info("insert player_history size is " + playerHistoryList.size() + "!");
        // set cache
        this.setPlayerHistoryCache();
    }

    @Override
    public void insertEventLive(int event, EventLiveRes eventLiveRes) {
        if (eventLiveRes == null) {
            return;
        }
        List<EventLiveEntity> eventLiveList = Lists.newArrayList();
        List<EventLiveEntity> insertList = Lists.newArrayList();
        List<EventLiveEntity> updateList = Lists.newArrayList();
        // prepare
        Map<String, EventLiveEntity> eventLiveMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
                        .eq(EventLiveEntity::getEvent, event))
                .stream()
                .collect(Collectors.toMap(k -> StringUtils.joinWith("-", k.getElement(), k.getFixture()), v -> v)); // element-fixture -> data
        Map<Integer, PlayerEntity> playerMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
        eventLiveRes.getElements().forEach(o -> {
            if (o.getStats() == null) {
                return;
            }
            int element = o.getId();
            String fixture = String.valueOf(!o.getExplain().isEmpty() ? o.getExplain().get(0).getFixture() : 0);
            ElementStat elementStat = o.getStats();
            EventLiveEntity eventLive = new EventLiveEntity();
            BeanUtil.copyProperties(elementStat, eventLive, CopyOptions.create().ignoreNullValue());
            eventLive
                    .setElement(element)
                    .setElementType(playerMap.containsKey(element) ?
                            playerMap.get(element).getElementType() : 0)
                    .setTeamId(playerMap.containsKey(element) ? playerMap.get(element).getTeamId() : 0)
                    .setEvent(event)
                    .setFixture(fixture);
            eventLiveList.add(eventLive);
        });
        // insert or update
        eventLiveList.forEach(o -> {
            String key = StringUtils.joinWith("-", o.getElement(), o.getFixture());
            if (!eventLiveMap.containsKey(key)) {
                insertList.add(o);
            } else {
                o.setId(eventLiveMap.get(key).getId());
                updateList.add(o);
            }
        });
        this.eventLiveService.saveBatch(insertList);
        log.info("insert event_live size is " + insertList.size() + "!");
        this.eventLiveService.updateBatchById(updateList);
        log.info("update event_live size is " + updateList.size() + "!");
        // set cache
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", EventLiveEntity.class.getSimpleName(), event);
        RedisUtils.removeCacheByKey(key);
        eventLiveList.forEach(o -> {
            String element = String.valueOf(o.getElement());
            if (!valueMap.containsKey(element)) { // single gw
                valueMap.put(element, o);
            } else { // multi gw
                EventLiveEntity oldEntity = (EventLiveEntity) valueMap.get(element);
                EventLiveEntity newEntity = new EventLiveEntity()
                        .setFixture(StringUtils.joinWith(",", oldEntity.getFixture(), o.getFixture()))
                        .setMinutes(oldEntity.getMinutes() + o.getMinutes())
                        .setGoalsScored(oldEntity.getGoalsScored() + o.getGoalsScored())
                        .setAssists(oldEntity.getAssists() + o.getAssists())
                        .setCleanSheets(oldEntity.getCleanSheets() + o.getCleanSheets())
                        .setGoalsConceded(oldEntity.getGoalsConceded() + o.getGoalsConceded())
                        .setOwnGoals(oldEntity.getOwnGoals() + o.getOwnGoals())
                        .setPenaltiesSaved(oldEntity.getPenaltiesSaved() + o.getPenaltiesSaved())
                        .setPenaltiesMissed(oldEntity.getPenaltiesMissed() + o.getPenaltiesMissed())
                        .setYellowCards(oldEntity.getYellowCards() + o.getYellowCards())
                        .setRedCards(oldEntity.getRedCards() + o.getRedCards())
                        .setSaves(oldEntity.getSaves() + o.getSaves())
                        .setBonus(oldEntity.getBonus() + o.getBonus())
                        .setBps(oldEntity.getBps() + o.getBps())
                        .setTotalPoints(oldEntity.getTotalPoints() + o.getTotalPoints());
                valueMap.put(element, newEntity);
            }
        });
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    @Override
    public void insertEventLiveCache(int event, EventLiveRes eventLiveRes) {
        if (eventLiveRes == null) {
            return;
        }
        Map<Integer, PlayerEntity> playerMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
        List<EventLiveEntity> eventLiveList = Lists.newArrayList();
        eventLiveRes.getElements().forEach(o -> {
            int element = o.getId();
            ElementStat elementStat = o.getStats();
            EventLiveEntity eventLive = new EventLiveEntity();
            BeanUtil.copyProperties(elementStat, eventLive, CopyOptions.create().ignoreNullValue());
            eventLive.setElement(element)
                    .setElementType(playerMap.containsKey(element) ? playerMap.get(element).getElementType() : 0)
                    .setTeamId(playerMap.containsKey(element) ? playerMap.get(element).getTeamId() : 0)
                    .setEvent(event);
            eventLiveList.add(eventLive);
        });
        log.info("event_live_cache size is " + eventLiveList.size() + "!");
        // set cache
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", EventLiveEntity.class.getSimpleName(), event);
        RedisUtils.removeCacheByKey(key);
        eventLiveList.forEach(o -> {
            String element = String.valueOf(o.getElement());
            if (!valueMap.containsKey(element)) { // single gw
                valueMap.put(element, o);
            } else { // multi gw
                EventLiveEntity oldEntity = (EventLiveEntity) valueMap.get(element);
                EventLiveEntity newEntity = new EventLiveEntity()
                        .setFixture(StringUtils.joinWith(",", oldEntity.getFixture(), o.getFixture()))
                        .setMinutes(oldEntity.getMinutes() + o.getMinutes())
                        .setGoalsScored(oldEntity.getGoalsScored() + o.getGoalsScored())
                        .setAssists(oldEntity.getAssists() + o.getAssists())
                        .setCleanSheets(oldEntity.getCleanSheets() + o.getCleanSheets())
                        .setGoalsConceded(oldEntity.getGoalsConceded() + o.getGoalsConceded())
                        .setOwnGoals(oldEntity.getOwnGoals() + o.getOwnGoals())
                        .setPenaltiesSaved(oldEntity.getPenaltiesSaved() + o.getPenaltiesSaved())
                        .setPenaltiesMissed(oldEntity.getPenaltiesMissed() + o.getPenaltiesMissed())
                        .setYellowCards(oldEntity.getYellowCards() + o.getYellowCards())
                        .setRedCards(oldEntity.getRedCards() + o.getRedCards())
                        .setSaves(oldEntity.getSaves() + o.getSaves())
                        .setBonus(oldEntity.getBonus() + o.getBonus())
                        .setBps(oldEntity.getBps() + o.getBps())
                        .setTotalPoints(oldEntity.getTotalPoints() + o.getTotalPoints());
                valueMap.put(element, newEntity);
            }
        });
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    @Override
    public void insertEventLiveExplain(int event, EventLiveRes eventLiveRes) {
        if (eventLiveRes == null) {
            return;
        }
        List<EventLiveExplainEntity> eventLiveExplainList = Lists.newArrayList();
        List<EventLiveExplainEntity> insertList = Lists.newArrayList();
        List<EventLiveExplainEntity> updateList = Lists.newArrayList();
        // prepare
        Map<Integer, EventLiveExplainEntity> eventLiveExplainMap = this.eventLiveExplainService.list(new QueryWrapper<EventLiveExplainEntity>().lambda()
                        .eq(EventLiveExplainEntity::getEvent, event))
                .stream()
                .collect(Collectors.toMap(EventLiveExplainEntity::getElement, o -> o));
        Map<String, PlayerEntity> playerMap = this.getPlayerMap(CommonUtils.getCurrentSeason());
        eventLiveRes.getElements().forEach(o -> {
            int element = o.getId();
            if (!playerMap.containsKey(String.valueOf(element)) || CollectionUtils.isEmpty(o.getExplain())) {
                return;
            }
            ElementExplain elementExplain = o.getExplain().get(0);
            if (elementExplain == null) {
                return;
            }
            int totalPoints = o.getStats().getTotalPoints();
            int bps = o.getStats().getBps();
            EventLiveExplainEntity eventLiveExplainEntity = this.initEventLiveExplain(element, event, totalPoints, bps, playerMap.get(String.valueOf(element)), elementExplain);
            if (eventLiveExplainEntity == null) {
                return;
            }
            eventLiveExplainList.add(eventLiveExplainEntity);
        });
        // insert or update
        eventLiveExplainList.forEach(o -> {
            int element = o.getElement();
            if (!eventLiveExplainMap.containsKey(element)) {
                insertList.add(o);
            } else {
                o.setId(eventLiveExplainMap.get(element).getId());
                updateList.add(o);
            }
        });
        this.eventLiveExplainService.saveBatch(insertList);
        log.info("insert event_live_explain size is " + insertList.size() + "!");
        this.eventLiveExplainService.updateBatchById(updateList);
        log.info("update event_live_explain size is " + updateList.size() + "!");
        // set cache
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", EventLiveExplainEntity.class.getSimpleName(), event);
        RedisUtils.removeCacheByKey(key);
        eventLiveExplainList.forEach(o -> valueMap.put(String.valueOf(o.getElement()), o));
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    private EventLiveExplainEntity initEventLiveExplain(int element, int event, int totalPoints, int bps, PlayerEntity playerEntity, ElementExplain elementExplain) {
        EventLiveExplainEntity eventLiveExplainEntity = new EventLiveExplainEntity()
                .setElement(element)
                .setElementType(playerEntity.getElementType())
                .setTeamId(playerEntity.getTeamId())
                .setEvent(event)
                .setTotalPoints(totalPoints)
                .setBps(bps)
                .setBonus(0)
                .setMinutes(0)
                .setMinutesPoints(0)
                .setGoalsScored(0)
                .setGoalsScoredPoints(0)
                .setAssists(0)
                .setAssistsPoints(0)
                .setCleanSheets(0)
                .setCleanSheetsPoints(0)
                .setGoalsConceded(0)
                .setGoalsConcededPoints(0)
                .setOwnGoals(0)
                .setOwnGoalsPoints(0)
                .setPenaltiesSaved(0)
                .setPenaltiesSavedPoints(0)
                .setPenaltiesMissed(0)
                .setPenaltiesMissedPoints(0)
                .setYellowCards(0)
                .setYellowCardsPoints(0)
                .setRedCards(0)
                .setRedCardsPoints(0)
                .setSaves(0)
                .setSavesPoints(0)
                .setBonus(0);
        elementExplain.getStats().forEach(o -> {
            String identifier = o.getIdentifier();
            switch (identifier) {
                case "minutes": {
                    eventLiveExplainEntity
                            .setMinutes(o.getValue())
                            .setMinutesPoints(o.getPoints());
                    break;
                }
                case "goals_scored": {
                    eventLiveExplainEntity
                            .setGoalsScored(o.getValue())
                            .setGoalsScoredPoints(o.getPoints());
                    break;
                }
                case "assists": {
                    eventLiveExplainEntity
                            .setAssists(o.getValue())
                            .setAssistsPoints(o.getPoints());
                    break;
                }
                case "clean_sheets": {
                    eventLiveExplainEntity
                            .setCleanSheets(o.getValue())
                            .setCleanSheetsPoints(o.getPoints());
                    break;
                }
                case "goals_conceded": {
                    eventLiveExplainEntity
                            .setGoalsConceded(o.getValue())
                            .setGoalsConcededPoints(o.getPoints());
                    break;
                }
                case "own_goals": {
                    eventLiveExplainEntity
                            .setOwnGoals(o.getValue())
                            .setOwnGoalsPoints(o.getPoints());
                    break;
                }
                case "penalties_saved": {
                    eventLiveExplainEntity
                            .setPenaltiesSaved(o.getValue())
                            .setPenaltiesSavedPoints(o.getPoints());
                    break;
                }
                case "penalties_missed": {
                    eventLiveExplainEntity
                            .setPenaltiesMissed(o.getValue())
                            .setPenaltiesMissedPoints(o.getPoints());
                    break;
                }
                case "yellow_cards": {
                    eventLiveExplainEntity
                            .setYellowCards(o.getValue())
                            .setYellowCardsPoints(o.getPoints());
                    break;
                }
                case "red_cards": {
                    eventLiveExplainEntity
                            .setRedCards(o.getValue())
                            .setRedCardsPoints(o.getPoints());
                    break;
                }
                case "saves": {
                    eventLiveExplainEntity
                            .setSaves(o.getValue())
                            .setSavesPoints(o.getPoints());
                    break;
                }
                case "bonus": {
                    eventLiveExplainEntity.setBonus(o.getPoints());
                    break;
                }
            }
        });
        return eventLiveExplainEntity;
    }

    @Override
    public void insertEventLiveSummary() {
        Multimap<Integer, EventLiveEntity> eventLiveMap = HashMultimap.create();
        this.eventLiveService.list().forEach(o -> eventLiveMap.put(o.getElement(), o));
        // truncate
        this.eventLiveSummaryService.getBaseMapper().truncate();
        // collect
        List<EventLiveSummaryEntity> list = Lists.newArrayList();
        eventLiveMap.keySet().forEach(o -> {
            Collection<EventLiveEntity> eventLives = eventLiveMap.get(o);
            if (CollectionUtils.isEmpty(eventLives)) {
                return;
            }
            EventLiveEntity first = eventLives
                    .stream()
                    .findFirst()
                    .orElse(new EventLiveEntity());
            list.add(
                    new EventLiveSummaryEntity()
                            .setElement(o)
                            .setElementType(first.getElementType())
                            .setTeamId(first.getTeamId())
                            .setMinutes(
                                    eventLives
                                            .stream()
                                            .mapToInt(EventLiveEntity::getMinutes)
                                            .sum()
                            )
                            .setGoalsScored(
                                    eventLives
                                            .stream()
                                            .mapToInt(EventLiveEntity::getGoalsScored)
                                            .sum()
                            )
                            .setAssists(
                                    eventLives
                                            .stream()
                                            .mapToInt(EventLiveEntity::getAssists)
                                            .sum()
                            )
                            .setCleanSheets(
                                    eventLives
                                            .stream()
                                            .mapToInt(EventLiveEntity::getCleanSheets)
                                            .sum()
                            )
                            .setGoalsConceded(
                                    eventLives
                                            .stream()
                                            .mapToInt(EventLiveEntity::getGoalsConceded)
                                            .sum()
                            )
                            .setOwnGoals(
                                    eventLives
                                            .stream()
                                            .mapToInt(EventLiveEntity::getOwnGoals)
                                            .sum()
                            )
                            .setPenaltiesSaved(
                                    eventLives
                                            .stream()
                                            .mapToInt(EventLiveEntity::getPenaltiesSaved)
                                            .sum()
                            )
                            .setPenaltiesMissed(
                                    eventLives
                                            .stream()
                                            .mapToInt(EventLiveEntity::getPenaltiesMissed)
                                            .sum()
                            )
                            .setYellowCards(
                                    eventLives
                                            .stream()
                                            .mapToInt(EventLiveEntity::getYellowCards)
                                            .sum()
                            )
                            .setRedCards(
                                    eventLives
                                            .stream()
                                            .mapToInt(EventLiveEntity::getRedCards)
                                            .sum()
                            )
                            .setSaves(
                                    eventLives
                                            .stream()
                                            .mapToInt(EventLiveEntity::getSaves)
                                            .sum()
                            )
                            .setBonus(
                                    eventLives
                                            .stream()
                                            .mapToInt(EventLiveEntity::getBonus)
                                            .sum()
                            )
                            .setBps(
                                    eventLives
                                            .stream()
                                            .mapToInt(EventLiveEntity::getBps)
                                            .sum()
                            )
                            .setTotalPoints(
                                    eventLives
                                            .stream()
                                            .mapToInt(EventLiveEntity::getTotalPoints)
                                            .sum()
                            )
            );
        });
        // insert
        this.eventLiveSummaryService.saveBatch(list);
        log.info("insert event_live_summary size:{}!", list.size());
        // set cache
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", EventLiveSummaryEntity.class.getSimpleName(), CommonUtils.getCurrentSeason());
        RedisUtils.removeCacheByKey(key);
        list.forEach(o -> valueMap.put(String.valueOf(o.getElement()), o));
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    @Override
    public void insertHisEventLiveSummary(String season) {
        MybatisPlusConfig.season.set(season);
        List<EventLiveSummaryEntity> eventLiveSummaryList = this.eventLiveSummaryService.list();
        MybatisPlusConfig.season.remove();
        if (CollectionUtils.isEmpty(eventLiveSummaryList)) {
            return;
        }
        // set cache
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", EventLiveSummaryEntity.class.getSimpleName(), season);
        RedisUtils.removeCacheByKey(key);
        eventLiveSummaryList.forEach(o -> valueMap.put(String.valueOf(o.getElement()), o));
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    @Override
    public void insertLiveBonusCache() {
        // get playing fixtures
        Map<Integer, Integer> livePlayingMap = Maps.newHashMap(); // key:team -> value:against_team
        this.getEventLiveFixtureMap().forEach((teamIdStr, map) -> {
            int teamId = Integer.parseInt(teamIdStr);
            map.keySet().forEach(status -> {
                List<LiveFixtureData> liveFixtureList = Lists.newArrayList();
                liveFixtureList.addAll(map.get(MatchPlayStatus.Playing.name()));
                liveFixtureList.addAll(map.get(MatchPlayStatus.Finished.name()));
                if (CollectionUtils.isEmpty(liveFixtureList)) {
                    return;
                }
                if (livePlayingMap.containsKey(teamId)) {
                    return;
                }
                livePlayingMap.put(teamId, liveFixtureList.get(0).getAgainstId());
                livePlayingMap.put(liveFixtureList.get(0).getAgainstId(), teamId);
            });
        });
        // get event_live
        List<Integer> bonusInTeamList = Lists.newArrayList();
        Map<Integer, List<EventLiveEntity>> teamEventLiveMap = Maps.newHashMap();
        this.getEventLiveByEvent(this.getCurrentEvent()).values()
                .forEach(o -> {
                    if (o.getMinutes() <= 0) {
                        return;
                    }
                    int teamId = o.getTeamId();
                    if (!livePlayingMap.containsKey(teamId)) {
                        return;
                    }
                    int againstId = livePlayingMap.get(teamId);
                    // check bonus in
                    if (o.getBonus() > 0) {
                        if (teamEventLiveMap.containsKey(teamId) && !bonusInTeamList.contains(teamId)) {
                            bonusInTeamList.add(teamId);
                        }
                        if (teamEventLiveMap.containsKey(againstId) && !bonusInTeamList.contains(againstId)) {
                            bonusInTeamList.add(againstId);
                        }
                        return;
                    }
                    // home team
                    List<EventLiveEntity> homeList = Lists.newArrayList();
                    if (teamEventLiveMap.containsKey(teamId)) {
                        homeList = teamEventLiveMap.get(teamId);
                    }
                    homeList.add(o);
                    teamEventLiveMap.put(teamId, homeList);
                    // away team
                    List<EventLiveEntity> awayList = Lists.newArrayList();
                    if (teamEventLiveMap.containsKey(againstId)) {
                        awayList = teamEventLiveMap.get(againstId);
                    }
                    awayList.add(o);
                    teamEventLiveMap.put(againstId, awayList);
                });
        //  set cache
        String key = StringUtils.joinWith("::", "LiveBonusData");
        RedisUtils.removeCacheByKey(key);
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        teamEventLiveMap.keySet().forEach(teamId -> {
            if (bonusInTeamList.contains(teamId)) {
                return;
            }
            // sort teamEventLiveMap by bps
            List<EventLiveEntity> sortList = teamEventLiveMap.get(teamId)
                    .stream()
                    .sorted(Comparator.comparing(EventLiveEntity::getBps).reversed())
                    .collect(Collectors.toList());
            // set bonus points
            Map<Integer, Integer> bonusMap = this.setBonusPoints(teamId, sortList);
            if (!CollectionUtils.isEmpty(bonusMap)) {
                valueMap.put(String.valueOf(teamId), bonusMap);
            }
        });
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    private Map<Integer, Integer> setBonusPoints(int teamId, List<EventLiveEntity> sortList) {
        int count = 0;
        Map<Integer, Integer> bonusMap = Maps.newConcurrentMap();
        // 最高分
        EventLiveEntity first = sortList.get(0);
        int highestBps = first.getBps();
        this.setBonusMap(teamId, first, 3, bonusMap);
        count += 1;
        // bps同分
        List<EventLiveEntity> firstList = sortList
                .stream()
                .filter(o -> !o.getElement().equals(first.getElement()))
                .filter(o -> o.getBps() == highestBps)
                .toList();
        for (EventLiveEntity eventLiveEntity :
                firstList) {
            count += 1;
            this.setBonusMap(teamId, eventLiveEntity, 3, bonusMap);
        }
        if (count >= 3) {
            return bonusMap;
        }
        // 次高分
        if (count < 2) {
            EventLiveEntity second = sortList.get(count);
            int runnerUpBps = second.getBps();
            this.setBonusMap(teamId, second, 2, bonusMap);
            count += 1;
            // bps同分
            List<EventLiveEntity> secondList = sortList
                    .stream()
                    .filter(o -> !o.getElement().equals(second.getElement()))
                    .filter(o -> o.getBps() == runnerUpBps)
                    .toList();
            for (EventLiveEntity eventLiveEntity :
                    secondList) {
                count += 1;
                this.setBonusMap(teamId, eventLiveEntity, 2, bonusMap);
            }
            if (count >= 3) {
                return bonusMap;
            }
        }
        // 第三高分
        EventLiveEntity third = sortList.get(count);
        int secondRunnerUpBps = third.getBps();
        this.setBonusMap(teamId, third, 1, bonusMap);
        count += 1;
        // bps同分
        List<EventLiveEntity> thirdList = sortList
                .stream()
                .filter(o -> !o.getElement().equals(third.getElement()))
                .filter(o -> o.getBps() == secondRunnerUpBps)
                .toList();
        for (EventLiveEntity eventLiveEntity :
                thirdList) {
            count += 1;
            this.setBonusMap(teamId, eventLiveEntity, 1, bonusMap);
        }
        return bonusMap;
    }

    private void setBonusMap(int teamId, EventLiveEntity eventLiveEntity, int bonus, Map<Integer, Integer> bonusMap) {
        if (teamId != eventLiveEntity.getTeamId()) {
            return;
        }
        bonusMap.put(eventLiveEntity.getElement(), bonus);
    }

    @Override
    public void insertEventOverallResult(StaticRes staticRes) {
        if (staticRes == null || CollectionUtils.isEmpty(staticRes.getEvents())) {
            return;
        }
        String key = StringUtils.joinWith("::", "EventOverallResult", CommonUtils.getCurrentSeason());
        RedisUtils.removeCacheByKey(key);
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        staticRes.getEvents().forEach(o -> {
            int event = o.getId();
            EventOverallResultData data = new EventOverallResultData()
                    .setEvent(event)
                    .setAverageEntryScore(o.getAverageEntryScore())
                    .setFinished(o.isFinished())
                    .setHighestScoringEntry(o.getHighestScoringEntry())
                    .setHighestScore(o.getHighestScore())
                    .setChipPlays(
                            o.getChipPlays()
                                    .stream()
                                    .map(i ->
                                            new EventChipData()
                                                    .setChipName(i.getChipName())
                                                    .setNumberPlayed(i.getNumPlayed())
                                    )
                                    .collect(Collectors.toList())
                    )
                    .setMostSelected(o.getMostSelected())
                    .setMostTransferredIn(o.getMostTransferredIn())
                    .setTopElementInfo(
                            o.getTopElementInfo() == null ? null :
                                    new EventTopElementData()
                                            .setElement(o.getTopElementInfo().getId())
                                            .setPoints(o.getTopElementInfo().getPoints())
                    )
                    .setTransfersMade(o.getTransfersMade())
                    .setMostCaptained(o.getMostCaptained())
                    .setMostViceCaptained(o.getMostViceCaptained());
            valueMap.put(String.valueOf(event), data);
        });
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    @Override
    public void insertPlayerSummary() {
        List<PlayerSummaryEntity> list = Lists.newArrayList();
        Map<Integer, List<PlayerSummaryEntity>> playerSummaryMap = Maps.newHashMap(); // code -> list
        // each season
        CommonUtils.getAllSeasons().forEach(season -> {
            // player
            Map<String, PlayerEntity> playerMap = this.getPlayerMap(season);
            if (CollectionUtils.isEmpty(playerMap)) {
                return;
            }
            // team_name
            Map<String, String> teamMap = this.getTeamNameMap(season);
            if (CollectionUtils.isEmpty(teamMap)) {
                return;
            }
            // team_short_name
            Map<String, String> teamShortMap = this.getTeamShortNameMap(season);
            if (CollectionUtils.isEmpty(teamShortMap)) {
                return;
            }
            // event_live_summary
            Map<String, EventLiveSummaryEntity> eventLiveSummaryMap = this.getEventLiveSummaryMap(season);
            if (CollectionUtils.isEmpty(eventLiveSummaryMap)) {
                return;
            }
            // init summary data
            eventLiveSummaryMap.values().forEach(o -> {
                PlayerEntity playerEntity = playerMap.getOrDefault(String.valueOf(o.getElement()), null);
                if (playerEntity == null) {
                    return;
                }
                int code = playerEntity.getCode();
                PlayerSummaryEntity playerSummaryEntity = BeanUtil.copyProperties(o, PlayerSummaryEntity.class)
                        .setSeason(season)
                        .setWebName(playerEntity.getWebName())
                        .setCode(code)
                        .setTeamName(teamMap.getOrDefault(String.valueOf(o.getTeamId()), ""))
                        .setTeamShortName(teamShortMap.getOrDefault(String.valueOf(o.getTeamId()), ""))
                        .setStartPrice(playerEntity.getStartPrice())
                        .setEndPrice(playerEntity.getPrice());
                list.add(playerSummaryEntity);
                // set into player summary map
                if (!playerSummaryMap.containsKey(code)) {
                    playerSummaryMap.put(code, Lists.newArrayList(playerSummaryEntity));
                } else {
                    List<PlayerSummaryEntity> playerSummaryList = playerSummaryMap.get(code);
                    playerSummaryList.add(playerSummaryEntity);
                    playerSummaryMap.put(code, playerSummaryList);
                }
            });
        });
        // insert
        this.playerSummaryService.getBaseMapper().truncate();
        this.playerSummaryService.saveBatch(list);
        log.info("insert player_summary size:{}!", list.size());
        // set cache
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", PlayerSummaryEntity.class.getSimpleName());
        RedisUtils.removeCacheByKey(key);
        playerSummaryMap.keySet().forEach(code -> valueMap.put(String.valueOf(code), playerSummaryMap.get(code)));
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    @Override
    public void insertSeasonPlayerSummaryCache(String season) {
        List<PlayerSeasonSummaryData> list = Lists.newArrayList();
        // init
        Map<String, List<PlayerSummaryEntity>> summaryMap = this.getPlayerSummaryMap();
        Map<String, String> teamNameMap = this.getTeamNameMap(season);
        Map<String, String> teamShortNameMap = this.getTeamShortNameMap(season);
        // each player
        this.getPlayerMap(season).values().forEach(o -> {
            int code = o.getCode();
            if (!summaryMap.containsKey(String.valueOf(code))) {
                return;
            }
            List<PlayerSummaryEntity> playerSummaryList = summaryMap.get(String.valueOf(code))
                    .stream()
                    .filter(i -> Integer.parseInt(i.getSeason()) <= Integer.parseInt(season))
                    .toList();
            if (CollectionUtils.isEmpty(playerSummaryList)) {
                return;
            }
            // merge
            list.add(
                    new PlayerSeasonSummaryData()
                            .setSeason(season)
                            .setElement(o.getElement())
                            .setWebName(o.getWebName())
                            .setCode(code)
                            .setStartPrice(o.getStartPrice())
                            .setEndPrice(o.getPrice())
                            .setElementType(o.getElementType())
                            .setTeamId(o.getTeamId())
                            .setTeamName(teamNameMap.getOrDefault(String.valueOf(o.getTeamId()), ""))
                            .setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getTeamId()), ""))
                            .setMinutes(
                                    playerSummaryList
                                            .stream()
                                            .mapToInt(PlayerSummaryEntity::getMinutes)
                                            .sum()
                            )
                            .setGoalsScored(
                                    playerSummaryList
                                            .stream()
                                            .mapToInt(PlayerSummaryEntity::getGoalsScored)
                                            .sum()
                            )
                            .setAssists(
                                    playerSummaryList
                                            .stream()
                                            .mapToInt(PlayerSummaryEntity::getAssists)
                                            .sum()
                            )
                            .setCleanSheets(
                                    playerSummaryList
                                            .stream()
                                            .mapToInt(PlayerSummaryEntity::getCleanSheets)
                                            .sum()
                            )
                            .setGoalsConceded(
                                    playerSummaryList
                                            .stream()
                                            .mapToInt(PlayerSummaryEntity::getGoalsConceded)
                                            .sum()
                            )
                            .setOwnGoals(
                                    playerSummaryList
                                            .stream()
                                            .mapToInt(PlayerSummaryEntity::getOwnGoals)
                                            .sum()
                            )
                            .setPenaltiesSaved(
                                    playerSummaryList
                                            .stream()
                                            .mapToInt(PlayerSummaryEntity::getPenaltiesSaved)
                                            .sum()
                            )
                            .setPenaltiesMissed(
                                    playerSummaryList
                                            .stream()
                                            .mapToInt(PlayerSummaryEntity::getPenaltiesMissed)
                                            .sum()
                            )
                            .setYellowCards(
                                    playerSummaryList
                                            .stream()
                                            .mapToInt(PlayerSummaryEntity::getYellowCards)
                                            .sum()
                            )
                            .setRedCards(
                                    playerSummaryList
                                            .stream()
                                            .mapToInt(PlayerSummaryEntity::getRedCards)
                                            .sum()
                            )
                            .setSaves(
                                    playerSummaryList
                                            .stream()
                                            .mapToInt(PlayerSummaryEntity::getSaves)
                                            .sum()
                            )
                            .setBonus(
                                    playerSummaryList
                                            .stream()
                                            .mapToInt(PlayerSummaryEntity::getBonus)
                                            .sum()
                            )
                            .setBps(
                                    playerSummaryList
                                            .stream()
                                            .mapToInt(PlayerSummaryEntity::getBps)
                                            .sum()
                            )
                            .setTotalPoints(
                                    playerSummaryList
                                            .stream()
                                            .mapToInt(PlayerSummaryEntity::getTotalPoints)
                                            .sum()
                            )
            );
        });
        // set cache
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", PlayerSeasonSummaryData.class.getSimpleName(), season);
        RedisUtils.removeCacheByKey(key);
        list.forEach(o -> valueMap.put(String.valueOf(o.getCode()), o));
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    @Override
    public int getCurrentEvent() {
        String season = CommonUtils.getCurrentSeason();
        int event = 1;
        for (int i = 1; i < 39; i++) {
            String deadline = this.getDeadlineByEvent(season, i);
            if (LocalDateTime.now().isAfter(LocalDateTime.parse(deadline, DateTimeFormatter.ofPattern(Constant.DATETIME)))) {
                event = i;
            } else {
                break;
            }
        }
        return event;
    }

    @Override
    public Map<String, String> getTeamNameMap(String season) {
        Map<String, String> map = Maps.newHashMap();
        String key = StringUtils.joinWith("::", TeamEntity.class.getSimpleName(), season, "name");
        this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(k.toString(), (String) v));
        return map;
    }

    @Override
    public Map<String, String> getTeamShortNameMap(String season) {
        Map<String, String> map = Maps.newHashMap();
        String key = StringUtils.joinWith("::", TeamEntity.class.getSimpleName(), season, "shortName");
        this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(k.toString(), (String) v));
        return map;
    }

    @Override
    public String getDeadlineByEvent(String season, int event) {
        String key = StringUtils.joinWith("::", EventEntity.class.getSimpleName(), season);
        return CommonUtils.getLocalZoneDateTime((String) this.redisTemplate.opsForHash().get(key, String.valueOf(event)));
    }

    @Override
    public List<EventFixtureEntity> getEventFixtureByEvent(String season, int event) {
        List<EventFixtureEntity> list = Lists.newArrayList();
        String key = StringUtils.joinWith("::", EventFixtureEntity.class.getSimpleName(), season, "event", event);
        Set<Object> set = this.redisTemplate.opsForSet().members(key);
        if (CollectionUtils.isEmpty(set)) {
            return list;
        }
        set.forEach(o -> list.add((EventFixtureEntity) o));
        return list.stream().sorted(Comparator.comparing(EventFixtureEntity::getId)).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Map<String, List<LiveFixtureData>>> getEventLiveFixtureMap() {
        Map<String, Map<String, List<LiveFixtureData>>> map = Maps.newHashMap();
        String key = StringUtils.joinWith("::", LiveFixtureData.class.getSimpleName());
        this.redisTemplate.opsForHash().keys(key).forEach(teamId ->
                map.put(teamId.toString(), (Map<String, List<LiveFixtureData>>) this.redisTemplate.opsForHash().get(key, teamId)));
        return map;
    }

    @Override
    public Map<String, PlayerEntity> getPlayerMap(String season) {
        Map<String, PlayerEntity> map = Maps.newHashMap();
        String key = StringUtils.joinWith("::", PlayerEntity.class.getSimpleName(), season);
        this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(String.valueOf(k), (PlayerEntity) v));
        return map;
    }

    @Override
    public Map<String, EventLiveEntity> getEventLiveByEvent(int event) {
        Map<String, EventLiveEntity> map = Maps.newHashMap();
        String key = StringUtils.joinWith("::", EventLiveEntity.class.getSimpleName(), event);
        this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(k.toString(), (EventLiveEntity) v));
        return map;
    }

    @Override
    public Map<String, EventLiveSummaryEntity> getEventLiveSummaryMap(String season) {
        Map<String, EventLiveSummaryEntity> map = Maps.newHashMap();
        String key = StringUtils.joinWith("::", EventLiveSummaryEntity.class.getSimpleName(), season);
        this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(String.valueOf(k), (EventLiveSummaryEntity) v));
        return map;
    }

    @Override
    public Map<String, List<PlayerSummaryEntity>> getPlayerSummaryMap() {
        Map<String, List<PlayerSummaryEntity>> map = Maps.newHashMap();
        String key = StringUtils.joinWith("::", PlayerSummaryEntity.class.getSimpleName());
        this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(String.valueOf(k), (List<PlayerSummaryEntity>) v));
        return map;
    }

    @Override
    public Map<String, PlayerSeasonSummaryData> getPlayerSeasonSummaryMap(String season) {
        Map<String, PlayerSeasonSummaryData> map = Maps.newHashMap();
        String key = StringUtils.joinWith("::", PlayerSeasonSummaryData.class.getSimpleName(), season);
        this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(String.valueOf(k), (PlayerSeasonSummaryData) v));
        return map;
    }

    @Override
    public EventOverallResultData getEventOverallResultByEvent(String season, int event) {
        String key = StringUtils.joinWith("::", "EventOverallResult", season);
        return (EventOverallResultData) this.redisTemplate.opsForHash().get(key, String.valueOf(event));
    }

}
