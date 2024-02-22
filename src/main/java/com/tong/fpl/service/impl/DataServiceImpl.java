package com.tong.fpl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.tong.fpl.aop.annotation.RerunRecord;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.constant.enums.Chip;
import com.tong.fpl.constant.enums.LeagueType;
import com.tong.fpl.domain.data.entry.Match;
import com.tong.fpl.domain.data.response.EntryCupRes;
import com.tong.fpl.domain.data.response.EntryRes;
import com.tong.fpl.domain.data.response.UserPicksRes;
import com.tong.fpl.domain.data.response.UserTransfersRes;
import com.tong.fpl.domain.data.userpick.AutoSubs;
import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.entry.EntryEventAutoSubsData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.event.EventOverallResultData;
import com.tong.fpl.service.IDataService;
import com.tong.fpl.service.IInterfaceService;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.IRedisCacheService;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.CommonUtils;
import com.tong.fpl.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

/**
 * Create by tong on 2021/8/31
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DataServiceImpl implements IDataService {

    private final IRedisCacheService redisCacheService;
    private final IQueryService queryService;
    private final IInterfaceService interfaceService;

    private final EntryInfoService entryInfoService;
    private final EntryLeagueInfoService entryLeagueInfoService;
    private final EntryEventPickService entryEventPickService;
    private final EntryEventTransfersService entryEventTransfersService;
    private final EntryEventCupResultService entryEventCupResultService;
    private final EntryEventResultService entryEventResultService;
    private final PlayerValueInfoService playerValueInfoService;
    private final PopularScoutResultService popularScoutResultService;

    private final ForkJoinPool forkJoinPool = new ForkJoinPool(4);

    @Override
    public void upsertEntryInfoByList(List<Integer> entryList) {
        if (CollectionUtils.isEmpty(entryList)) {
            return;
        }
        entryList = entryList
                .stream()
                .distinct()
                .collect(Collectors.toList());
        // prepare
        Map<Integer, EntryInfoEntity> entryInfoMap = this.entryInfoService.list(new QueryWrapper<EntryInfoEntity>().lambda()
                        .in(EntryInfoEntity::getEntry, entryList))
                .stream()
                .collect(Collectors.toMap(EntryInfoEntity::getEntry, o -> o));
        Map<Integer, EntryEventResultEntity> lastEntryEventResultMap = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                        .eq(EntryEventResultEntity::getEvent, this.queryService.getLastEvent())
                        .in(EntryEventResultEntity::getEntry, entryList))
                .stream()
                .collect(Collectors.toMap(EntryEventResultEntity::getEntry, o -> o));
        // init data
        List<CompletableFuture<EntryRes>> entryResFuture = entryList
                .stream()
                .map(o -> CompletableFuture.supplyAsync(() -> this.interfaceService.getEntry(o).orElse(null), forkJoinPool))
                .toList();
        Map<Integer, EntryRes> entryResMap = entryResFuture
                .stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(EntryRes::getId, o -> o));
        // entry_info
        List<CompletableFuture<EntryInfoEntity>> entryInfoFuture = entryList
                .stream()
                .map(o -> CompletableFuture.supplyAsync(() -> this.initEntryInfo(entryResMap.get(o), entryInfoMap.get(o), lastEntryEventResultMap.get(o)), forkJoinPool))
                .toList();
        List<EntryInfoEntity> entryInfoList = entryInfoFuture
                .stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .toList();
        // save or update
        List<EntryInfoEntity> insertEntryInfoList = Lists.newArrayList();
        List<EntryInfoEntity> updateEntryInfoList = Lists.newArrayList();
        entryInfoList.forEach(o -> {
            if (!entryInfoMap.containsKey(o.getEntry())) {
                insertEntryInfoList.add(o);
            } else {
                updateEntryInfoList.add(o);
            }
        });
        this.entryInfoService.saveBatch(insertEntryInfoList);
        log.info("insert entry_info size:{}", insertEntryInfoList.size());
        this.entryInfoService.updateBatchById(updateEntryInfoList);
        log.info("update entry_info size:{}", updateEntryInfoList.size());
        if (!this.queryService.isAfterMatchDay(this.queryService.getCurrentEvent())) {
            return;
        }
        Map<String, EntryLeagueInfoEntity> entryLeagueInfoMap = this.entryLeagueInfoService.list(new QueryWrapper<EntryLeagueInfoEntity>().lambda()
                        .in(EntryLeagueInfoEntity::getEntry, entryList))
                .stream()
                .collect(Collectors.toMap(k -> StringUtils.joinWith("-", k.getEntry(), k.getLeagueId(), k.getLeagueType()), v -> v));
        // entry_league_info
        List<CompletableFuture<List<EntryLeagueInfoEntity>>> entryLeagueInfoFuture = entryList
                .stream()
                .map(o -> CompletableFuture.supplyAsync(() -> this.initEntryLeagueInfo(o, entryResMap.get(o)), forkJoinPool))
                .toList();
        List<EntryLeagueInfoEntity> entryInfoLeagueList = Lists.newArrayList();
        entryLeagueInfoFuture
                .stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .forEach(entryInfoLeagueList::addAll);
        // save or update
        List<EntryLeagueInfoEntity> insertLeagueInfoList = Lists.newArrayList();
        List<EntryLeagueInfoEntity> updateLeagueInfoList = Lists.newArrayList();
        entryInfoLeagueList.forEach(o -> {
            String key = StringUtils.joinWith("-", o.getEntry(), o.getLeagueId(), o.getLeagueType());
            if (!entryLeagueInfoMap.containsKey(key)) {
                insertLeagueInfoList.add(o);
            } else {
                o.setId(entryLeagueInfoMap.get(key).getId());
                updateLeagueInfoList.add(o);
            }
        });
        this.entryLeagueInfoService.saveBatch(insertLeagueInfoList);
        log.info("insert entry_league_info size:{}", insertLeagueInfoList.size());
        this.entryLeagueInfoService.updateBatchById(updateLeagueInfoList);
        log.info("update entry_league_info size:{}", updateLeagueInfoList.size());
    }

    private EntryInfoEntity initEntryInfo(EntryRes entryRes, EntryInfoEntity entryInfo, EntryEventResultEntity lastEntryEventResultEntity) {
        EntryInfoEntity entryInfoEntity = new EntryInfoEntity()
                .setEntry(entryRes.getId())
                .setEntryName(entryRes.getName())
                .setPlayerName(entryRes.getPlayerFirstName() + " " + entryRes.getPlayerLastName())
                .setRegion(entryRes.getPlayerRegionName())
                .setStartedEvent(entryRes.getStartedEvent())
                .setOverallPoints(entryRes.getSummaryOverallPoints())
                .setOverallRank(entryRes.getSummaryOverallRank())
                .setBank(entryRes.getLastDeadlineBank())
                .setTeamValue(entryRes.getLastDeadlineValue())
                .setTotalTransfers(entryRes.getLastDeadlineTotalTransfers());
        if (lastEntryEventResultEntity == null) {
            entryInfoEntity
                    .setLastOverallPoints(0)
                    .setLastOverallRank(0)
                    .setLastTeamValue(0);
        } else {
            entryInfoEntity
                    .setLastOverallPoints(lastEntryEventResultEntity.getOverallPoints())
                    .setLastOverallRank(lastEntryEventResultEntity.getOverallRank())
                    .setLastTeamValue(lastEntryEventResultEntity.getTeamValue());
        }
        if (entryInfo == null) {
            List<String> usedNameList = Lists.newArrayList(entryRes.getName());
            entryInfoEntity
                    .setLastEntryName("")
                    .setUsedEntryName(JsonUtils.obj2json(usedNameList));
        } else {
            if (!StringUtils.equals(entryRes.getName(), entryInfo.getEntryName())) {
                entryInfoEntity.setLastEntryName(entryInfo.getEntryName());
                List<String> usedNameList = JsonUtils.json2Collection(entryInfo.getUsedEntryName(), List.class, String.class);
                if (CollectionUtils.isEmpty(usedNameList)) {
                    usedNameList = Lists.newArrayList();
                }
                usedNameList.add(entryRes.getName());
                entryInfoEntity.setUsedEntryName(JsonUtils.obj2json(usedNameList));
            }
        }
        return entryInfoEntity;
    }

    private List<EntryLeagueInfoEntity> initEntryLeagueInfo(int entry, EntryRes entryRes) {
        if (entryRes == null || entryRes.getLeagues() == null) {
            log.error("entry:{}, get fpl server entry empty", entry);
            return null;
        }
        List<EntryLeagueInfoEntity> entryLeagueInfoEntityList = Lists.newArrayList();
        // classic
        entryRes.getLeagues().getClassic().forEach(o -> {
            int leagueId = o.getId();
            entryLeagueInfoEntityList.add(
                    new EntryLeagueInfoEntity()
                            .setEntry(entry)
                            .setLeagueId(leagueId)
                            .setType(StringUtils.equals("x", o.getLeagueType()) ? "private" : "public")
                            .setLeagueType(LeagueType.Classic.name())
                            .setLeagueName(o.getName())
                            .setEntryRank(o.getEntryRank())
                            .setEntryLastRank(o.getEntryLastRank())
                            .setStartEvent(o.getStartEvent())
                            .setCreated(o.getCreated())
            );
        });
        // h2h
        entryRes.getLeagues().getH2h().forEach(o -> {
            int leagueId = o.getId();
            entryLeagueInfoEntityList.add(
                    new EntryLeagueInfoEntity()
                            .setEntry(entry)
                            .setLeagueId(leagueId)
                            .setType(StringUtils.equals("x", o.getLeagueType()) ? "private" : "public")
                            .setLeagueType(LeagueType.H2h.name())
                            .setLeagueName(o.getName())
                            .setEntryRank(o.getEntryRank())
                            .setEntryLastRank(o.getEntryLastRank())
                            .setStartEvent(o.getStartEvent())
                            .setCreated(o.getCreated())
            );
        });
        return entryLeagueInfoEntityList;
    }

    @Override
    public void insertEventPickByEntryList(int event, List<Integer> entryList) {
        if (event < 1 || event > 38 || CollectionUtils.isEmpty(entryList)) {
            return;
        }
        // init
        List<Integer> existsList = this.entryEventPickService.list(new QueryWrapper<EntryEventPickEntity>().lambda()
                        .eq(EntryEventPickEntity::getEvent, event)
                        .in(EntryEventPickEntity::getEntry, entryList))
                .stream()
                .map(EntryEventPickEntity::getEntry)
                .toList();
        entryList = entryList
                .stream()
                .filter(o -> !existsList.contains(o))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(entryList)) {
            log.error("event:{}, no need to insert", event);
            return;
        }
        // init
        List<CompletableFuture<EntryEventPickEntity>> future = entryList
                .stream()
                .map(entry -> CompletableFuture.supplyAsync(() -> this.initEventEntryPicks(event, entry)))
                .toList();
        List<EntryEventPickEntity> list = future
                .stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        // save
        this.entryEventPickService.saveBatch(list);
        log.info("event:{}, insert entry_event_pick size:{}", event, list.size());
    }

    private EntryEventPickEntity initEventEntryPicks(int event, int entry) {
        UserPicksRes userPicksRes = this.interfaceService.getUserPicks(event, entry).orElse(null);
        if (userPicksRes == null || CollectionUtils.isEmpty(userPicksRes.getPicks())) {
            log.error("event:{}, entry:{}, get fpl server user_picks empty", event, entry);
            return null;
        }
        return new EntryEventPickEntity()
                .setEntry(entry)
                .setEvent(event)
                .setTransfers(userPicksRes.getEntryHistory().getEventTransfers())
                .setTransfersCost(userPicksRes.getEntryHistory().getEventTransfersCost())
                .setChip(userPicksRes.getActiveChip() == null ? Chip.NONE.getValue() : userPicksRes.getActiveChip())
                .setPicks(JsonUtils.obj2json(userPicksRes.getPicks()));
    }

    @Override
    public void insertEventTransfersByEntryList(int event, List<Integer> entryList) {
        if (event <= 1 || event > 38) {
            return;
        }
        // exists
        List<Integer> existsList = this.entryEventTransfersService.list(new QueryWrapper<EntryEventTransfersEntity>().lambda()
                        .eq(EntryEventTransfersEntity::getEvent, event)
                        .in(EntryEventTransfersEntity::getEntry, entryList))
                .stream()
                .map(EntryEventTransfersEntity::getEntry)
                .toList();
        entryList = entryList
                .stream()
                .filter(o -> !existsList.contains(o))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(entryList)) {
            log.error("event:{}, no need to insert", event);
            return;
        }
        // init data
        List<CompletableFuture<List<EntryEventTransfersEntity>>> future = entryList
                .stream()
                .map(entry -> CompletableFuture.supplyAsync(() -> this.initEntryEventTransfers(entry)))
                .toList();
        List<EntryEventTransfersEntity> list = Lists.newArrayList();
        future
                .stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .forEach(list::addAll);
        // prepare
        Map<String, EntryEventTransfersEntity> entryEventTransferMap = this.entryEventTransfersService.list(new QueryWrapper<EntryEventTransfersEntity>().lambda()
                        .in(EntryEventTransfersEntity::getEntry, entryList))
                .stream()
                .collect(Collectors.toMap(k -> StringUtils.joinWith("-", k.getEvent(), k.getEntry(), k.getElementIn(), k.getElementOut(), k.getTime()), o -> o));
        // save
        List<EntryEventTransfersEntity> insertList = list
                .stream()
                .filter(o -> !entryEventTransferMap.containsKey(StringUtils.joinWith("-", o.getEvent(), o.getEntry(), o.getElementIn(), o.getElementOut(), o.getTime())))
                .collect(Collectors.toList());
        this.entryEventTransfersService.saveBatch(insertList);
        log.info("event:{}, insert entry_event_transfers size:{}", event, insertList.size());
    }

    private List<EntryEventTransfersEntity> initEntryEventTransfers(int entry) {
        List<UserTransfersRes> transferResList = this.interfaceService.getUserTransfers(entry).orElse(null);
        if (CollectionUtils.isEmpty(transferResList)) {
            return null;
        }
        return transferResList
                .stream()
                .map(o ->
                        new EntryEventTransfersEntity()
                                .setEntry(o.getEntry())
                                .setEvent(o.getEvent())
                                .setElementIn(o.getElementIn())
                                .setElementInPlayed(false)
                                .setElementInPoints(0)
                                .setElementInCost(o.getElementInCost())
                                .setElementOut(o.getElementOut())
                                .setElementOutCost(o.getElementOutCost())
                                .setElementOutPoints(0)
                                .setTime(o.getTime())
                )
                .collect(Collectors.toList());
    }

    @Override
    public void updateEventTransfersByEntryList(int event, List<Integer> entryList) {
        if (event <= 1 || event > 38 || CollectionUtils.isEmpty(entryList)) {
            return;
        }
        // prepare
        Map<Integer, EntryEventResultEntity> entryEventResultMap = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                        .eq(EntryEventResultEntity::getEvent, event)
                        .in(EntryEventResultEntity::getEntry, entryList))
                .stream()
                .collect(Collectors.toMap(EntryEventResultEntity::getEntry, o -> o));
        if (CollectionUtils.isEmpty(entryEventResultMap)) {
            log.error("event:{}, entry_event_result empty", event);
            return;
        }
        Map<Integer, Integer> pointsMap = this.redisCacheService.getEventLiveByEvent(event).values()
                .stream()
                .collect(Collectors.toMap(EventLiveEntity::getElement, EventLiveEntity::getTotalPoints));
        if (CollectionUtils.isEmpty(pointsMap)) {
            log.error("event:{}, event_live empty", event);
            return;
        }
        Multimap<Integer, EntryEventTransfersEntity> entryEventTransferMap = HashMultimap.create();
        this.entryEventTransfersService.list(new QueryWrapper<EntryEventTransfersEntity>().lambda()
                        .eq(EntryEventTransfersEntity::getEvent, event)
                        .in(EntryEventTransfersEntity::getEntry, entryList))
                .forEach(o -> entryEventTransferMap.put(o.getEntry(), o));
        if (entryEventTransferMap.isEmpty()) {
            log.error("event:{}, entry_event_transfers empty", event);
            return;
        }
        List<EntryEventTransfersEntity> list = Lists.newArrayList();
        entryList.forEach(entry -> {
            EntryEventResultEntity entryEventResultEntity = entryEventResultMap.getOrDefault(entry, null);
            if (entryEventResultEntity == null) {
                log.error("event:{}, entry:{}, entry_event_result empty", event, entry);
                return;
            }
            List<Integer> pickElementList = this.getPickElementList(entryEventResultEntity.getEventPicks());
            if (CollectionUtils.isEmpty(pickElementList)) {
                log.error("event:{}, entry:{}, pick element empty", event, entry);
                return;
            }
            entryEventTransferMap.get(entry).forEach(o -> {
                o
                        .setElementInPoints(pointsMap.getOrDefault(o.getElementIn(), 0))
                        .setElementInPlayed(StringUtils.equals(Chip.BB.getValue(), entryEventResultEntity.getEventChip()) ?
                                pickElementList.contains(o.getElementIn()) : pickElementList.subList(0, 11).contains(o.getElementIn()))
                        .setElementOutPoints(pointsMap.getOrDefault(o.getElementOut(), 0));
                list.add(o);
            });
        });
        // update
        this.entryEventTransfersService.updateBatchById(list);
        log.info("event:{}, update entry_event_transfers size:{}", event, list.size());
    }

    private List<Integer> getPickElementList(String picks) {
        List<EntryPickData> pickList = JsonUtils.json2Collection(picks, List.class, EntryPickData.class);
        if (CollectionUtils.isEmpty(pickList)) {
            log.error("event pick empty");
            return null;
        }
        return pickList
                .stream()
                .map(EntryPickData::getElement)
                .collect(Collectors.toList());
    }

    @Override
    public void upsertEventCupResultByEntryList(int event, List<Integer> entryList) {
        if (event < 17 || event > 38 || CollectionUtils.isEmpty(entryList)) {
            return;
        }
        // prepare
        Map<Integer, EntryEventCupResultEntity> entryCupResultMap = this.entryEventCupResultService.list(new QueryWrapper<EntryEventCupResultEntity>().lambda()
                        .eq(EntryEventCupResultEntity::getEvent, event)
                        .in(EntryEventCupResultEntity::getEntry, entryList))
                .stream()
                .collect(Collectors.toMap(EntryEventCupResultEntity::getEntry, o -> o));
        // init data
        List<CompletableFuture<EntryEventCupResultEntity>> future = entryList
                .stream()
                .map(entry -> CompletableFuture.supplyAsync(() -> this.initEntryEventCupResult(event, entry)))
                .toList();
        List<EntryEventCupResultEntity> list = future
                .stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .toList();
        // save or update
        List<EntryEventCupResultEntity> insertList = Lists.newArrayList();
        List<EntryEventCupResultEntity> updateList = Lists.newArrayList();
        list.forEach(o -> {
            int entry = o.getEntry();
            if (!entryCupResultMap.containsKey(entry)) {
                insertList.add(o);
            }
        });
        this.entryEventCupResultService.saveBatch(insertList);
        log.info("event:{}, insert entry_event_cup_result size:{}", event, insertList.size());
        this.entryEventCupResultService.updateBatchById(updateList);
        log.info("event:{}, update entry_event_cup_result size:{}", event, updateList.size());
    }

    private EntryEventCupResultEntity initEntryEventCupResult(int event, int entry) {
        EntryCupRes entryCupRes = this.interfaceService.getEntryCup(entry).orElse(null);
        if (entryCupRes == null) {
            return null;
        }
        Match cupMatch = entryCupRes.getCupMatches()
                .stream()
                .filter(o -> o.getEvent() == event)
                .findFirst()
                .orElse(null);
        if (cupMatch == null) {
            log.error("entry:{}, get fpl server entry_cup empty", entry);
            return null;
        }
        EntryEventCupResultEntity entryEventCupResultEntity = new EntryEventCupResultEntity()
                .setEvent(event)
                .setEntry(entry)
                .setEntryName(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry1Name() : cupMatch.getEntry2Name())
                .setPlayerName(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry1PlayerName() : cupMatch.getEntry2PlayerName())
                .setEventPoints(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry1Points() : cupMatch.getEntry2Points())
                .setAgainstEntry(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry2Entry() : cupMatch.getEntry1Entry())
                .setAgainstEntryName(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry2Name() : cupMatch.getEntry1Name())
                .setAgainstPlayerName(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry2PlayerName() : cupMatch.getEntry1PlayerName())
                .setAgainstEventPoints(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry2Points() : cupMatch.getEntry1Points());
        if (cupMatch.getWinner() == 0) {
            if (entryEventCupResultEntity.getEventPoints() >= entryEventCupResultEntity.getAgainstEventPoints()) {
                entryEventCupResultEntity.setResult("Win");
            }
        } else if (cupMatch.getWinner() == entryEventCupResultEntity.getEntry()) {
            entryEventCupResultEntity.setResult("Win");
        } else {
            entryEventCupResultEntity.setResult("Lose");
        }
        return entryEventCupResultEntity;
    }

    @Override
    public void upsertEventResultByEntryList(int event, List<Integer> entryList) {
        if (event < 1 || event > 38 || CollectionUtils.isEmpty(entryList)) {
            return;
        }
        // prepare
        Map<Integer, EntryEventResultEntity> entryEventResultMap = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                        .eq(EntryEventResultEntity::getEvent, event)
                        .in(EntryEventResultEntity::getEntry, entryList))
                .stream()
                .collect(Collectors.toMap(EntryEventResultEntity::getEntry, o -> o));
        Map<String, EventLiveEntity> eventLiveMap = this.redisCacheService.getEventLiveByEvent(event);
        // init data
        List<CompletableFuture<EntryEventResultEntity>> future = entryList
                .stream()
                .map(entry -> CompletableFuture.supplyAsync(() -> this.initEntryEventResult(event, entry, eventLiveMap)))
                .toList();
        List<EntryEventResultEntity> list = future
                .stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .toList();
        if (CollectionUtils.isEmpty(list)) {
            log.error("event:{}, init entry_event_result data empty", event);
            return;
        }
        // save or update
        List<EntryEventResultEntity> insertList = Lists.newArrayList();
        List<EntryEventResultEntity> updateList = Lists.newArrayList();
        list.forEach(o -> {
            int entry = o.getEntry();
            if (!entryEventResultMap.containsKey(entry)) {
                insertList.add(o);
            } else {
                o.setId(entryEventResultMap.get(entry).getId());
                updateList.add(o);

            }
        });
        this.entryEventResultService.saveBatch(insertList);
        log.info("event:{}, insert entry_event_result size:{}", event, insertList.size());
        this.entryEventResultService.updateBatchById(updateList);
        log.info("event:{}, update entry_event_result size:{}", event, insertList.size());
    }

    private EntryEventResultEntity initEntryEventResult(int event, int entry, Map<String, EventLiveEntity> eventLiveMap) {
        UserPicksRes userPick = this.interfaceService.getUserPicks(event, entry).orElse(null);
        if (userPick == null) {
            log.error("event:{}, entry:{}, get fpl server user_pick empty", event, entry);
            return null;
        }
        Map<Integer, Integer> elementPointsMap = eventLiveMap.values()
                .stream()
                .collect(Collectors.toMap(EventLiveEntity::getElement, EventLiveEntity::getTotalPoints));
        int captain = this.getPlayedCaptain(userPick.getPicks(), eventLiveMap);
        EventLiveEntity captainEntity = eventLiveMap.getOrDefault(String.valueOf(captain), new EventLiveEntity());
        return new EntryEventResultEntity()
                .setEntry(entry)
                .setEvent(event)
                .setEventPoints(userPick.getEntryHistory().getPoints())
                .setEventTransfers(userPick.getEntryHistory().getEventTransfers())
                .setEventTransfersCost(userPick.getEntryHistory().getEventTransfersCost())
                .setEventNetPoints(userPick.getEntryHistory().getPoints() - userPick.getEntryHistory().getEventTransfersCost())
                .setEventBenchPoints(userPick.getEntryHistory().getPointsOnBench())
                .setEventAutoSubPoints(userPick.getAutomaticSubs().isEmpty() ? 0 : this.calcAutoSubPoints(userPick.getAutomaticSubs(), elementPointsMap))
                .setEventRank(userPick.getEntryHistory().getRank())
                .setEventChip(StringUtils.isBlank(userPick.getActiveChip()) ? Chip.NONE.getValue() : userPick.getActiveChip())
                .setEventPicks(this.setUserPicks(userPick.getPicks(), elementPointsMap))
                .setEventAutoSubs(this.setAutoSubs(userPick.getAutomaticSubs(), elementPointsMap))
                .setOverallPoints(userPick.getEntryHistory().getTotalPoints())
                .setOverallRank(userPick.getEntryHistory().getOverallRank())
                .setTeamValue(userPick.getEntryHistory().getValue())
                .setBank(userPick.getEntryHistory().getBank())
                .setPlayedCaptain(captain)
                .setCaptainPoints(captainEntity.getTotalPoints());
    }

    private int getPlayedCaptain(List<Pick> picks, Map<String, EventLiveEntity> eventLiveMap) {
        Pick captain = picks
                .stream()
                .filter(Pick::isCaptain)
                .findFirst()
                .orElse(null);
        Pick viceCaptain = picks
                .stream()
                .filter(Pick::isViceCaptain)
                .findFirst()
                .orElse(null);
        if (captain == null || viceCaptain == null) {
            return 0;
        }
        if (eventLiveMap.get(String.valueOf(captain.getElement())).getMinutes() == 0 && eventLiveMap.get(String.valueOf(viceCaptain.getElement())).getMinutes() > 0) {
            return viceCaptain.getElement();
        }
        return captain.getElement();
    }

    private int calcAutoSubPoints(List<AutoSubs> automaticSubs, Map<Integer, Integer> elementPointsMap) {
        return automaticSubs
                .stream()
                .mapToInt(o -> elementPointsMap.getOrDefault(o.getElementIn(), 0))
                .sum();
    }

    private String setUserPicks(List<Pick> picks, Map<Integer, Integer> elementPointsMap) {
        List<EntryPickData> pickList = Lists.newArrayList();
        picks.forEach(o -> pickList.add(
                        new EntryPickData()
                                .setElement(o.getElement())
                                .setPosition(o.getPosition())
                                .setMultiplier(o.getMultiplier())
                                .setCaptain(o.isCaptain())
                                .setViceCaptain(o.isViceCaptain())
                                .setPoints(elementPointsMap.getOrDefault(o.getElement(), 0))
                )
        );
        return JsonUtils.obj2json(pickList);
    }

    private String setAutoSubs(List<AutoSubs> autoSubs, Map<Integer, Integer> elementPointsMap) {
        if (CollectionUtils.isEmpty(autoSubs)) {
            return "";
        }
        List<EntryEventAutoSubsData> autoSubList = Lists.newArrayList();
        autoSubs.forEach(o -> autoSubList.add(
                        new EntryEventAutoSubsData()
                                .setElementIn(o.getElementIn())
                                .setElementInPoints(elementPointsMap.getOrDefault(o.getElementIn(), 0))
                                .setElementOut(o.getElementOut())
                                .setElementOutPoints(elementPointsMap.getOrDefault(o.getElementOut(), 0))
                )
        );
        return JsonUtils.obj2json(autoSubList);
    }

    @Override
    public void updateAllEventSourceScoutResult(int event) {
        List<PopularScoutResultEntity> list = Lists.newArrayList();
        // prepare
        String season = CommonUtils.getCurrentSeason();
        Map<String, EventLiveEntity> eventLiveMap = this.redisCacheService.getEventLiveByEvent(event);
        Map<Integer, Integer> pointsMap = eventLiveMap.values()
                .stream()
                .collect(Collectors.toMap(EventLiveEntity::getElement, EventLiveEntity::getTotalPoints));
        EventOverallResultData eventOverallResultData = this.redisCacheService.getEventOverallResultByEvent(season, event);
        if (CollectionUtils.isEmpty(eventLiveMap) || eventOverallResultData == null) {
            return;
        }
        // loop
        this.popularScoutResultService.list().forEach(o -> {
            o
                    .setEvent(event)
                    .setSource(o.getSource())
                    .setPosition1Points(pointsMap.getOrDefault(o.getPosition1(), 0))
                    .setPosition2Points(pointsMap.getOrDefault(o.getPosition2(), 0))
                    .setPosition3Points(pointsMap.getOrDefault(o.getPosition3(), 0))
                    .setPosition4Points(pointsMap.getOrDefault(o.getPosition4(), 0))
                    .setPosition5Points(pointsMap.getOrDefault(o.getPosition5(), 0))
                    .setPosition6Points(pointsMap.getOrDefault(o.getPosition6(), 0))
                    .setPosition7Points(pointsMap.getOrDefault(o.getPosition7(), 0))
                    .setPosition8Points(pointsMap.getOrDefault(o.getPosition8(), 0))
                    .setPosition9Points(pointsMap.getOrDefault(o.getPosition9(), 0))
                    .setPosition10Points(pointsMap.getOrDefault(o.getPosition10(), 0))
                    .setPosition11Points(pointsMap.getOrDefault(o.getPosition11(), 0))
                    .setPosition12Points(pointsMap.getOrDefault(o.getPosition12(), 0))
                    .setPosition13Points(pointsMap.getOrDefault(o.getPosition13(), 0))
                    .setPosition14Points(pointsMap.getOrDefault(o.getPosition14(), 0))
                    .setPosition15Points(pointsMap.getOrDefault(o.getPosition15(), 0))
                    .setCaptainPoints(pointsMap.getOrDefault(o.getCaptain(), 0))
                    .setViceCaptainPoints(pointsMap.getOrDefault(o.getViceCaptain(), 0))
                    .setPlayedCaptain(this.getScoutPlayedCaptain(o.getCaptain(), o.getViceCaptain(), eventLiveMap))
                    .setAveragePoints(eventOverallResultData.getAverageEntryScore());
            o.setPlayedCaptainPoints(pointsMap.getOrDefault(o.getPlayedCaptain(), 0));
            o.setRawTotalPoints(this.calcTotalPoints(o));
            o.setTotalPoints(o.getRawTotalPoints() + o.getPlayedCaptainPoints());
            list.add(o);
        });
        // update
        this.popularScoutResultService.updateBatchById(list);
        log.info("event:{}, update popular_scout_result size:{}", event, list.size());
    }

    @Override
    public void updateEventSourceScoutResult(int event, String source) {
        // check exist
        PopularScoutResultEntity popularScoutResultEntity = this.popularScoutResultService.getOne(new QueryWrapper<PopularScoutResultEntity>().lambda()
                .eq(PopularScoutResultEntity::getEvent, event)
                .eq(PopularScoutResultEntity::getSource, source));
        if (popularScoutResultEntity == null) {
            return;
        }
        // prepare
        String season = CommonUtils.getCurrentSeason();
        Map<String, EventLiveEntity> eventLiveMap = this.redisCacheService.getEventLiveByEvent(event);
        Map<Integer, Integer> pointsMap = eventLiveMap.values()
                .stream()
                .collect(Collectors.toMap(EventLiveEntity::getElement, EventLiveEntity::getTotalPoints));
        EventOverallResultData eventOverallResultData = this.redisCacheService.getEventOverallResultByEvent(season, event);
        if (CollectionUtils.isEmpty(eventLiveMap) || eventOverallResultData == null) {
            return;
        }
        // update
        popularScoutResultEntity
                .setEvent(event)
                .setSource(source)
                .setPosition1Points(pointsMap.getOrDefault(popularScoutResultEntity.getPosition1(), 0))
                .setPosition2Points(pointsMap.getOrDefault(popularScoutResultEntity.getPosition2(), 0))
                .setPosition3Points(pointsMap.getOrDefault(popularScoutResultEntity.getPosition3(), 0))
                .setPosition4Points(pointsMap.getOrDefault(popularScoutResultEntity.getPosition4(), 0))
                .setPosition5Points(pointsMap.getOrDefault(popularScoutResultEntity.getPosition5(), 0))
                .setPosition6Points(pointsMap.getOrDefault(popularScoutResultEntity.getPosition6(), 0))
                .setPosition7Points(pointsMap.getOrDefault(popularScoutResultEntity.getPosition7(), 0))
                .setPosition8Points(pointsMap.getOrDefault(popularScoutResultEntity.getPosition8(), 0))
                .setPosition9Points(pointsMap.getOrDefault(popularScoutResultEntity.getPosition9(), 0))
                .setPosition10Points(pointsMap.getOrDefault(popularScoutResultEntity.getPosition10(), 0))
                .setPosition11Points(pointsMap.getOrDefault(popularScoutResultEntity.getPosition11(), 0))
                .setPosition12Points(pointsMap.getOrDefault(popularScoutResultEntity.getPosition12(), 0))
                .setPosition13Points(pointsMap.getOrDefault(popularScoutResultEntity.getPosition13(), 0))
                .setPosition14Points(pointsMap.getOrDefault(popularScoutResultEntity.getPosition14(), 0))
                .setPosition15Points(pointsMap.getOrDefault(popularScoutResultEntity.getPosition15(), 0))
                .setCaptainPoints(pointsMap.getOrDefault(popularScoutResultEntity.getCaptain(), 0))
                .setViceCaptainPoints(pointsMap.getOrDefault(popularScoutResultEntity.getViceCaptain(), 0))
                .setPlayedCaptain(this.getScoutPlayedCaptain(popularScoutResultEntity.getCaptain(), popularScoutResultEntity.getViceCaptain(), eventLiveMap))
                .setAveragePoints(eventOverallResultData.getAverageEntryScore());
        popularScoutResultEntity.setPlayedCaptainPoints(pointsMap.getOrDefault(popularScoutResultEntity.getPlayedCaptain(), 0));
        popularScoutResultEntity.setRawTotalPoints(this.calcTotalPoints(popularScoutResultEntity));
        popularScoutResultEntity.setTotalPoints(popularScoutResultEntity.getRawTotalPoints() + popularScoutResultEntity.getPlayedCaptainPoints());
        this.popularScoutResultService.updateById(popularScoutResultEntity);
    }

    private int getScoutPlayedCaptain(int captain, int viceCaptain, Map<String, EventLiveEntity> eventLiveMap) {
        if (captain == 0 && viceCaptain == 0) {
            return 0;
        }
        EventLiveEntity captainEventLive = eventLiveMap.get(String.valueOf(captain));
        if (captainEventLive == null) {
            return 0;
        }
        return captainEventLive.getMinutes() != 0 ? captain : viceCaptain;
    }

    private int calcTotalPoints(PopularScoutResultEntity popularScoutResultEntity) {
        return popularScoutResultEntity.getPosition1Points() +
                popularScoutResultEntity.getPosition2Points() +
                popularScoutResultEntity.getPosition3Points() +
                popularScoutResultEntity.getPosition4Points() +
                popularScoutResultEntity.getPosition5Points() +
                popularScoutResultEntity.getPosition6Points() +
                popularScoutResultEntity.getPosition7Points() +
                popularScoutResultEntity.getPosition8Points() +
                popularScoutResultEntity.getPosition9Points() +
                popularScoutResultEntity.getPosition10Points() +
                popularScoutResultEntity.getPosition11Points();
    }

    @RerunRecord()
    @Override
    public void insertPlayerValueInfo() throws Exception {
        List<PlayerValueInfoEntity> list = Lists.newArrayList();
        // prepare
        int event = this.queryService.getCurrentEvent();
        String hourIndex = String.valueOf(LocalTime.now().getHour());
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern(Constant.DATE));
        Map<String, String> teamShortNameMap = this.redisCacheService.getTeamShortNameMap(CommonUtils.getCurrentSeason());
        // check exist
        if (this.playerValueInfoService.count(new QueryWrapper<PlayerValueInfoEntity>().lambda()
                .eq(PlayerValueInfoEntity::getHourIndex, hourIndex)
                .eq(PlayerValueInfoEntity::getDate, date)) > 0) {
            return;
        }
        // fetch
        this.interfaceService.getBootstrapStatic().ifPresent(bootstrap ->
                bootstrap.getElements().forEach(o ->
                        list.add(
                                new PlayerValueInfoEntity()
                                        .setHourIndex(hourIndex)
                                        .setDate(date)
                                        .setEvent(event)
                                        .setElement(o.getId())
                                        .setCode(o.getCode())
                                        .setElementType(o.getElementType())
                                        .setWebName(o.getWebName())
                                        .setTeamId(o.getTeam())
                                        .setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getTeam()), ""))
                                        .setChanceOfPlayingNextRound(o.getChanceOfPlayingNextRound())
                                        .setChanceOfPlayingThisRound(o.getChanceOfPlayingThisRound())
                                        .setTransfersIn(o.getTransfersIn())
                                        .setTransfersInEvent(o.getTransfersInEvent())
                                        .setTransfersOut(o.getTransfersOut())
                                        .setTransfersOutEvent(o.getTransfersOutEvent())
                                        .setSelectedByPercent(o.getSelectedByPercent())
                                        .setNowCost(o.getNowCost())
                        )
                )
        );
        // update
        if (CollectionUtils.isEmpty(list)) {
            log.error("hour_index:{}, date:{}, insert player value fail", hourIndex, date);
            throw new Exception("insert player value fail");
        }
        this.playerValueInfoService.saveBatch(list);
        log.info("hour_index:{}, date:{}, insert player value success, size:{}", hourIndex, date, list.size());
    }

}
