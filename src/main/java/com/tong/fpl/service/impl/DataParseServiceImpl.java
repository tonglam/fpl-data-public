package com.tong.fpl.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.tong.fpl.config.mp.MybatisPlusConfig;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.fantasynutmeg.EventResponseData;
import com.tong.fpl.domain.fantasynutmeg.PlayerResponseData;
import com.tong.fpl.domain.fantasynutmeg.SeasonResponseData;
import com.tong.fpl.service.IDataParseService;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.HttpUtils;
import com.tong.fpl.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Create by tong on 2021/8/30
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DataParseServiceImpl implements IDataParseService {

    private final PlayerService playerService;
    private final PlayerStatService playerStatService;
    private final EventService eventService;
    private final EventFixtureService eventFixtureService;
    private final EventLiveService eventLiveService;

    @Override
    public void parseNutmegSeasonData(String season, String fileName) {
        // read json
        try {
            String result = Files.readAllLines(Paths.get(fileName))
                    .stream()
                    .findFirst()
                    .orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            List<SeasonResponseData> dataList = mapper.readValue(result, new TypeReference<>() {
            });
            if (CollectionUtils.isEmpty(dataList)) {
                return;
            }
            MybatisPlusConfig.season.set(season);
            // player
            this.insertIntoPlayer(dataList);
            // player stat
            this.insertIntoPlayerStat(dataList);
            MybatisPlusConfig.season.remove();
        } catch (IOException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private void insertIntoPlayer(List<SeasonResponseData> dataList) {
        List<PlayerEntity> insertList = Lists.newArrayList();
        List<PlayerEntity> updateList = Lists.newArrayList();
        Map<Integer, PlayerEntity> map = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
        dataList
                .stream()
                .map(this::initPlayerData)
                .forEach(o -> {
                    if (!map.containsKey(o.getElement())) {
                        insertList.add(o);
                    } else {
                        updateList.add(o);
                    }
                });
        this.playerService.saveBatch(insertList);
        log.info("insert player size:{}", insertList.size());
        this.playerService.updateBatchById(updateList);
        log.info("update player size:{}", updateList.size());
    }

    private PlayerEntity initPlayerData(SeasonResponseData data) {
        return new PlayerEntity()
                .setElement(data.getId())
                .setCode(data.getCode())
                .setPrice((int) (data.getNowCost() * 10))
                .setStartPrice((int) (data.getNowCost() * 10) - data.getCostChangeStart())
                .setElementType(data.getElementType())
                .setFirstName(data.getFirstName())
                .setSecondName(data.getSecondName())
                .setWebName(data.getWebName())
                .setTeamId(data.getTeam());
    }

    private void insertIntoPlayerStat(List<SeasonResponseData> dataList) {
        List<PlayerStatEntity> insertList = Lists.newArrayList();
        List<PlayerStatEntity> updateList = Lists.newArrayList();
        Map<Integer, PlayerStatEntity> map = this.playerStatService.list()
                .stream()
                .collect(Collectors.toMap(PlayerStatEntity::getElement, o -> o));
        dataList
                .stream()
                .map(this::initPlayerStatData)
                .forEach(o -> {
                    int element = o.getElement();
                    if (!map.containsKey(element)) {
                        insertList.add(o);
                    } else {
                        o.setId(map.get(element).getId());
                        updateList.add(o);
                    }
                });
        this.playerStatService.saveBatch(insertList);
        log.info("insert player stat size:{}", insertList.size());
        this.playerStatService.updateBatchById(updateList);
        log.info("update player stat size:{}", updateList.size());
    }

    private PlayerStatEntity initPlayerStatData(SeasonResponseData data) {
        return new PlayerStatEntity()
                .setEvent(0)
                .setElement(data.getId())
                .setCode(data.getCode())
                .setMatchPlayed(0)
                .setChanceOfPlayingNextRound(StringUtils.equalsAnyIgnoreCase("none", data.getChanceOfPlayingNextRound()) ? 0 :
                        Integer.parseInt(data.getChanceOfPlayingNextRound()))
                .setChanceOfPlayingThisRound(StringUtils.equalsAnyIgnoreCase("none", data.getChanceOfPlayingThisRound()) ? 0 :
                        Integer.parseInt(data.getChanceOfPlayingThisRound()))
                .setDreamteamCount(data.getDreamteamCount())
                .setEventPoints(data.getEventPoints())
                .setForm(String.valueOf(data.getForm()))
                .setInDreamteam(data.isInDreamteam())
                .setNews(data.getNews())
                .setNewsAdded(null)
                .setPointsPerGame(String.valueOf(data.getPointsPerGame()))
                .setSelectedByPercent(String.valueOf(data.getSelectedByPercent()))
                .setMinutes(data.getMinutes())
                .setGoalsScored(data.getGoalsScored())
                .setAssists(data.getAssists())
                .setCleanSheets(data.getCleanSheets())
                .setGoalsConceded(data.getGoalsConceded())
                .setOwnGoals(data.getOwnGoals())
                .setPenaltiesSaved(data.getPenaltiesSaved())
                .setPenaltiesMissed(data.getPenaltiesMissed())
                .setYellowCards(data.getYellowCards())
                .setRedCards(data.getRedCards())
                .setSaves(data.getSaves())
                .setBonus(data.getBonus())
                .setBps(data.getBps())
                .setInfluence(String.valueOf(data.getInfluence()))
                .setCreativity(String.valueOf(data.getCreativity()))
                .setThreat(String.valueOf(data.getThreat()))
                .setIctIndex(String.valueOf(data.getIctIndex()))
                .setTransfersInEvent(data.getTransfersInEvent())
                .setTransfersOutEvent(data.getTransfersOutEvent())
                .setTransfersIn(data.getTransfersIn())
                .setTransfersOut(data.getTransfersOut())
                .setCornersAndIndirectFreekicksOrder(0)
                .setDirectFreekicksOrder(0)
                .setPenaltiesOrder(0);
    }

    @Override
    public void parseNutmegEventData(String season) {
        Multimap<Integer, String> eventTimeMap = HashMultimap.create();
        Map<Integer, EventFixtureEntity> eventFixtureMap = Maps.newHashMap();
        Map<String, EventLiveEntity> eventLiveMap = Maps.newHashMap();
        Map<String, PlayerStatEntity> playerStatMap = Maps.newHashMap();
        MybatisPlusConfig.season.set(season);
        this.playerService.list().forEach(o -> this.parseSingleNutmegEventData(season, o, eventTimeMap, eventFixtureMap, eventLiveMap, playerStatMap));
        // event
        Map<Integer, EventEntity> eventMap = Maps.newHashMap();
        eventTimeMap.keySet().forEach(event -> {
            String deadlineTime = eventTimeMap.get(event)
                    .stream()
                    .min(Comparator.comparing(o -> LocalDateTime.parse(StringUtils.substringBefore(o, "Z"))))
                    .orElse(null);
            if (StringUtils.isEmpty(deadlineTime)) {
                return;
            }
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(Constant.DATETIME);
            LocalDateTime deadlineLocalTime = LocalDateTime.parse(StringUtils.substringBefore(deadlineTime, "Z"));
            String deadline = deadlineLocalTime
                    .plusHours(this.getDiffHour(season, deadlineLocalTime))
                    .minusHours(1)
                    .format(dateTimeFormatter);
            eventMap.put(event, this.initEventData(event, deadline));
        });
        List<EventEntity> eventList = new ArrayList<>(eventMap.values());
        this.eventService.getBaseMapper().truncate();
        this.eventService.saveBatch(eventList);
        log.info("insert event size:{}", eventList.size());
        // event_fixture
        List<EventFixtureEntity> eventFixtureList = new ArrayList<>(eventFixtureMap.values())
                .stream()
                .sorted(Comparator.comparing(EventFixtureEntity::getId))
                .collect(Collectors.toList());
        this.eventFixtureService.getBaseMapper().truncate();
        this.eventFixtureService.saveBatch(eventFixtureList);
        log.info("insert event_fixture size:{}", eventFixtureList.size());
        // event_live
        List<EventLiveEntity> eventLiveList = new ArrayList<>(eventLiveMap.values())
                .stream()
                .sorted(Comparator.comparing(EventLiveEntity::getEvent)
                        .thenComparing(EventLiveEntity::getElement))
                .collect(Collectors.toList());
        this.eventLiveService.getBaseMapper().truncate();
        this.eventLiveService.saveBatch(eventLiveList);
        log.info("insert event_live size:{}", eventLiveList.size());
        // player_stat
        List<PlayerStatEntity> playerStatList = new ArrayList<>(playerStatMap.values())
                .stream()
                .sorted(Comparator.comparing(PlayerStatEntity::getEvent)
                        .thenComparing(PlayerStatEntity::getElement))
                .collect(Collectors.toList());
        this.playerStatService.getBaseMapper().truncate();
        this.playerStatService.saveBatch(playerStatList);
        log.info("insert player_stat size:{}", playerStatList.size());
        MybatisPlusConfig.season.remove();
    }

    private void parseSingleNutmegEventData(String season, PlayerEntity playerEntity,
                                            Multimap<Integer, String> eventTimeMap, Map<Integer, EventFixtureEntity> eventFixtureMap, Map<String, EventLiveEntity> eventLiveMap, Map<String, PlayerStatEntity> playerStatMap) {
        try {
            // get json
            String result = HttpUtils.httpGet(this.getNutmegPlayerUrl(season, playerEntity.getElement())).orElse("");
            PlayerResponseData data = JsonUtils.json2obj(result, PlayerResponseData.class);
            if (data == null) {
                return;
            }
            List<EventResponseData> dataList = data.getHistory();
            if (CollectionUtils.isEmpty(dataList)) {
                return;
            }
            // event
            this.insertIntoEvent(dataList, eventTimeMap);
            // event_fixture
            this.insertIntoEventFixture(season, playerEntity, dataList, eventFixtureMap);
            // event_live
            this.insertIntoEventLive(playerEntity, dataList, eventLiveMap);
            // player_stat
            this.insertIntoPlayerStat(playerEntity, dataList, playerStatMap);
        } catch (IOException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private String getNutmegPlayerUrl(String season, int element) {
        String seasonStr = StringUtils.join("20", season.substring(0, 2), "-", season.substring(2, 4));
        return String.format("https://www.fantasynutmeg.com/api/history/%s?player=%s", seasonStr, element);
    }

    private void insertIntoEvent(List<EventResponseData> dataList, Multimap<Integer, String> eventTimeMap) {
        dataList.forEach(o -> eventTimeMap.put(o.getRound(), o.getKickoffTime()));
    }

    private int getDiffHour(String season, LocalDateTime kickoffTime) {
        LocalDateTime start = LocalDateTime.of(Integer.parseInt("20" + season.substring(0, 2)), 11, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(Integer.parseInt("20" + season.substring(2, 4)), 3, 27, 23, 59, 59);
        if (kickoffTime.isAfter(start) && kickoffTime.isBefore(end)) {
            return 8;
        }
        return 7;
    }

    private EventEntity initEventData(int event, String deadline) {
        return new EventEntity()
                .setId(event)
                .setName("Gameweek " + event)
                .setDeadlineTime(deadline)
                .setAverageEntryScore(0)
                .setFinished(true)
                .setHighestScore(0)
                .setHighestScoringEntry(0)
                .setIsPrevious(false)
                .setIsCurrent(false)
                .setIsNext(false)
                .setMostSelected(0)
                .setMostTransferredIn(0)
                .setMostCaptained(0)
                .setMostViceCaptained(0);
    }

    private void insertIntoEventFixture(String season, PlayerEntity playerEntity, List<EventResponseData> dataList, Map<Integer, EventFixtureEntity> eventFixtureMap) {
        dataList
                .stream()
                .filter(o -> !eventFixtureMap.containsKey(o.getFixture()))
                .map(o -> this.initEventFixtureData(season, playerEntity, o))
                .forEach(o -> eventFixtureMap.put(o.getId(), o));
    }

    private EventFixtureEntity initEventFixtureData(String season, PlayerEntity playerEntity, EventResponseData data) {
        int teamId = playerEntity.getTeamId();
        int againstId = data.getOpponentTeam();
        return new EventFixtureEntity()
                .setId(data.getFixture())
                .setCode(0)
                .setEvent(data.getRound())
                .setKickoffTime(this.getEventFixtureKickoffTime(season, StringUtils.substringBefore(data.getKickoffTime(), "Z").replaceAll("T", " ")))
                .setStarted(true)
                .setFinished(true)
                .setProvisionalStartTime(false)
                .setFinishedProvisional(true)
                .setMinutes(90)
                .setTeamH(data.isWasHome() ? teamId : againstId)
                .setTeamHDifficulty(0)
                .setTeamHScore(data.getTeamHScore())
                .setTeamA(data.isWasHome() ? againstId : teamId)
                .setTeamADifficulty(0)
                .setTeamAScore(data.getTeamAScore());
    }

    private String getEventFixtureKickoffTime(String season, String kickoff) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(Constant.DATETIME);
        LocalDateTime kickoffTime = LocalDateTime.parse(kickoff.replaceAll(" ", "T"));
        int diffHour = this.getDiffHour(season, kickoffTime);
        return kickoffTime.plusHours(diffHour).format(dateTimeFormatter);
    }

    private void insertIntoEventLive(PlayerEntity playerEntity, List<EventResponseData> dataList, Map<String, EventLiveEntity> eventLiveMap) {
        dataList
                .stream()
                .filter(o -> {
                    String key = StringUtils.joinWith("-", o.getRound(), o.getElement());
                    return !eventLiveMap.containsKey(key);
                })
                .map(o -> this.initEventLiveData(playerEntity, o))
                .forEach(o -> {
                    String key = StringUtils.joinWith("-", o.getEvent(), o.getElement());
                    eventLiveMap.put(key, o);
                });
    }

    private EventLiveEntity initEventLiveData(PlayerEntity playerEntity, EventResponseData data) {
        return new EventLiveEntity()
                .setElement(data.getElement())
                .setElementType(playerEntity.getElementType())
                .setTeamId(playerEntity.getTeamId())
                .setEvent(data.getRound())
                .setFixture(String.valueOf(data.getFixture()))
                .setMinutes(data.getMinutes())
                .setGoalsScored(data.getGoalsScored())
                .setAssists(data.getAssists())
                .setCleanSheets(data.getCleanSheets())
                .setGoalsConceded(data.getGoalsConceded())
                .setOwnGoals(data.getOwnGoals())
                .setPenaltiesSaved(data.getPenaltiesSaved())
                .setPenaltiesMissed(data.getPenaltiesMissed())
                .setYellowCards(data.getYellowCards())
                .setRedCards(data.getRedCards())
                .setSaves(data.getSaves())
                .setBonus(data.getBonus())
                .setBps(data.getBps())
                .setTotalPoints(data.getTotalPoints());
    }

    private void insertIntoPlayerStat(PlayerEntity playerEntity, List<EventResponseData> dataList, Map<String, PlayerStatEntity> playerStatMap) {
        dataList
                .stream()
                .filter(o -> {
                    String key = StringUtils.joinWith("-", o.getRound(), o.getElement());
                    return !playerStatMap.containsKey(key);
                })
                .map(o -> this.initPlayerStatData(playerEntity, o))
                .forEach(o -> {
                    String key = StringUtils.joinWith("-", o.getEvent(), o.getElement());
                    playerStatMap.put(key, o);
                });
    }

    private PlayerStatEntity initPlayerStatData(PlayerEntity playerEntity, EventResponseData data) {
        return new PlayerStatEntity()
                .setEvent(data.getRound())
                .setElement(data.getElement())
                .setCode(playerEntity.getCode())
                .setMatchPlayed(0)
                .setChanceOfPlayingNextRound(0)
                .setChanceOfPlayingThisRound(0)
                .setDreamteamCount(0)
                .setEventPoints(data.getTotalPoints())
                .setForm("")
                .setInDreamteam(false)
                .setNews("")
                .setNewsAdded(null)
                .setPointsPerGame("")
                .setSelectedByPercent("")
                .setMinutes(data.getMinutes())
                .setGoalsScored(data.getGoalsScored())
                .setAssists(data.getAssists())
                .setCleanSheets(data.getCleanSheets())
                .setGoalsConceded(data.getGoalsConceded())
                .setOwnGoals(data.getOwnGoals())
                .setPenaltiesSaved(data.getPenaltiesSaved())
                .setPenaltiesMissed(data.getPenaltiesMissed())
                .setYellowCards(data.getYellowCards())
                .setRedCards(data.getRedCards())
                .setSaves(data.getSaves())
                .setBonus(data.getBonus())
                .setBps(data.getBps())
                .setInfluence(String.valueOf(data.getInfluence()))
                .setCreativity(String.valueOf(data.getCreativity()))
                .setThreat(String.valueOf(data.getThreat()))
                .setIctIndex(String.valueOf(data.getIctIndex()))
                .setTransfersInEvent(0)
                .setTransfersOutEvent(0)
                .setTransfersIn(data.getTransfersIn())
                .setTransfersOut(data.getTransfersOut())
                .setCornersAndIndirectFreekicksOrder(0)
                .setDirectFreekicksOrder(0)
                .setPenaltiesOrder(0);
    }

}
