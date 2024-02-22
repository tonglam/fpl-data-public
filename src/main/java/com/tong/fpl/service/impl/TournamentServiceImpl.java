package com.tong.fpl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.*;
import com.tong.fpl.constant.enums.Chip;
import com.tong.fpl.constant.enums.GroupMode;
import com.tong.fpl.constant.enums.KnockoutMode;
import com.tong.fpl.constant.enums.LeagueType;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutNextRoundData;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutResultData;
import com.tong.fpl.service.IDataService;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.IRedisCacheService;
import com.tong.fpl.service.ITournamentService;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Create by tong on 2021/9/2
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TournamentServiceImpl implements ITournamentService {

    private final IRedisCacheService redisCacheService;
    private final IQueryService queryService;
    private final IDataService dataService;

    private final EntryEventResultService entryEventResultService;
    private final TournamentGroupService tournamentGroupService;
    private final TournamentPointsGroupResultService tournamentPointsGroupResultService;
    private final TournamentBattleGroupResultService tournamentBattleGroupResultService;
    private final TournamentKnockoutService tournamentKnockoutService;
    private final TournamentKnockoutResultService tournamentKnockoutResultService;
    private final TournamentRoyaleService tournamentRoyaleService;

    @Override
    public void upsertTournamentEventResult(int event, int tournamentId) {
        if (event < 1 || event > 38 || tournamentId <= 0) {
            log.error("event:{}, tournament:{}, params error", event, tournamentId);
            return;
        }
        // tournament_info
        TournamentInfoEntity tournamentInfoEntity = this.queryService.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            log.error("event:{}, tournament:{}, tournament_info not exists", event, tournamentId);
            return;
        }
        // entry list
        List<Integer> entryList = this.queryService.qryEntryListByTournament(tournamentId);
        // entry_event_result
        this.dataService.upsertEventResultByEntryList(event, entryList);
        log.info("event:{}, tournament:{}, update tournament entry_event_result size:{}", event, tournamentId, entryList.size());
        // royale
        if (!StringUtils.equals(LeagueType.Royale.name(), tournamentInfoEntity.getLeagueType())) {
            return;
        }
        // calculate eliminate numbers
        TournamentRoyaleEntity tournamentRoyaleEntity = this.tournamentRoyaleService.getOne(new QueryWrapper<TournamentRoyaleEntity>().lambda()
                .eq(TournamentRoyaleEntity::getTournamentId, tournamentId)
                .eq(TournamentRoyaleEntity::getEvent, event));
        if (tournamentRoyaleEntity == null) {
            return;
        }
        TournamentRoyaleEntity lastTournamentRoyaleEntity = this.tournamentRoyaleService.getOne(new QueryWrapper<TournamentRoyaleEntity>().lambda()
                .eq(TournamentRoyaleEntity::getTournamentId, tournamentId)
                .eq(TournamentRoyaleEntity::getEvent, event - 1));
        if (lastTournamentRoyaleEntity == null) {
            return;
        }
        Map<String, Integer> eliminateNumberMap = this.calculateEliminateNumbers(event, lastTournamentRoyaleEntity.getEventEliminatedNum(), tournamentInfoEntity, entryList);
        if (CollectionUtils.isEmpty(eliminateNumberMap)) {
            return;
        }
        int currentEliminateNumber = eliminateNumberMap.get("current");
        int nextEliminateNumber = eliminateNumberMap.get("next");
        // exclude eliminated entries
        entryList = entryList.stream().filter(o -> !StringUtils.contains(tournamentRoyaleEntity.getAllEliminatedEntries(), String.valueOf(o))).collect(Collectors.toList());
        // event eliminated
        List<Integer> eventEliminatedList = Lists.newArrayList();
        String nextWaitingEliminatedEntries = "";
        List<EntryEventResultEntity> eventEntryResultList = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEvent, event)
                .in(EntryEventResultEntity::getEntry, entryList)
                .orderByAsc(EntryEventResultEntity::getEventPoints));
        if (CollectionUtils.isEmpty(eventEntryResultList)) {
            return;
        }
        if (currentEliminateNumber > 0) {
            List<Integer> currentwaitingEliminatedList = Lists.newArrayList();
            int restNum = currentEliminateNumber;
            // get current event waiting eliminated list
            String currentWaitingEliminatedEntries = tournamentRoyaleEntity.getWaitingEliminatedEntries();
            if (StringUtils.isNotBlank(currentWaitingEliminatedEntries)) {
                currentwaitingEliminatedList = Stream.of(currentWaitingEliminatedEntries.split(",")).map(Integer::valueOf).collect(Collectors.toList());
            }
            List<Integer> filterList = currentwaitingEliminatedList;
            if (!CollectionUtils.isEmpty(filterList)) {
                restNum -= 1;
                List<Integer> currentWaitingEliminatedList = eventEntryResultList
                        .stream()
                        .filter(o -> filterList.contains(o.getEntry()))
                        .limit(1)
                        .map(EntryEventResultEntity::getEntry)
                        .toList();
                if (!CollectionUtils.isEmpty(currentWaitingEliminatedList)) {
                    eventEliminatedList.addAll(currentWaitingEliminatedList);
                }
                eventEntryResultList = eventEntryResultList.stream().filter(o -> !filterList.contains(o.getEntry())).collect(Collectors.toList());
            }
            if (restNum > 0) {
                List<Integer> currentEliminatedList = eventEntryResultList
                        .stream()
                        .limit(restNum)
                        .map(EntryEventResultEntity::getEntry)
                        .toList();
                if (!CollectionUtils.isEmpty(currentEliminatedList)) {
                    eventEliminatedList.addAll(currentEliminatedList);
                }
            }
        } else {
            // nxt event waiting eliminated list, exist only when current event eliminated number is 0
            if (currentEliminateNumber == 0) {
                int eventMinNetPoints = eventEntryResultList.stream().map(EntryEventResultEntity::getEventNetPoints).min(Integer::compareTo).orElse(0);
                if (eventMinNetPoints > 0) {
                    List<Integer> nextwaitingEliminatedList = eventEntryResultList.stream().filter(o -> o.getEventNetPoints() == eventMinNetPoints).map(EntryEventResultEntity::getEntry).collect(Collectors.toList());
                    nextWaitingEliminatedEntries = StringUtils.join(nextwaitingEliminatedList, ",");
                }
            }
        }
        String eventEliminatedEntries = CollectionUtils.isEmpty(eventEliminatedList) ? "" : StringUtils.join(eventEliminatedList, ",");
        // last event eliminated
        List<Integer> allEliminatedList = Lists.newArrayList();
        List<Integer> lastEventEliminatedList = StringUtils.isBlank(lastTournamentRoyaleEntity.getEventEliminatedEntries()) ? Lists.newArrayList() :
                Stream.of(lastTournamentRoyaleEntity.getEventEliminatedEntries().split(",")).map(Integer::valueOf).toList();
        if (!CollectionUtils.isEmpty(lastEventEliminatedList)) {
            allEliminatedList.addAll(lastEventEliminatedList);
        }
        List<Integer> lastAllEliminatedList = StringUtils.isBlank(lastTournamentRoyaleEntity.getAllEliminatedEntries()) ? Lists.newArrayList() :
                Stream.of(lastTournamentRoyaleEntity.getAllEliminatedEntries().split(",")).map(Integer::valueOf).toList();
        if (!CollectionUtils.isEmpty(lastAllEliminatedList)) {
            allEliminatedList.addAll(lastAllEliminatedList);
        }
        String allEliminatedEntries = CollectionUtils.isEmpty(allEliminatedList) ? "" : StringUtils.join(allEliminatedList, ",");
        tournamentRoyaleEntity
                .setEventEliminatedNum(currentEliminateNumber)
                .setNextEventEliminatedNum(nextEliminateNumber)
                .setEventEliminatedEntries(eventEliminatedEntries)
                .setWaitingEliminatedEntries(tournamentRoyaleEntity.getWaitingEliminatedEntries())
                .setAllEliminatedEntries(allEliminatedEntries);
        this.tournamentRoyaleService.updateById(tournamentRoyaleEntity);
        log.info("event:{}, tournament:{}, update tournament_royale success", event, tournamentId);
        // next event
        List<Integer> nextAllEliminatedList = Lists.newArrayList();
        nextAllEliminatedList.addAll(eventEliminatedList);
        nextAllEliminatedList.addAll(allEliminatedList);
        String nextAllEliminatedEntries = CollectionUtils.isEmpty(nextAllEliminatedList) ? "" : StringUtils.join(nextAllEliminatedList, ",");
        int nextEvent = event + 1;
        TournamentRoyaleEntity nextTournamentRoyaleEntity = this.tournamentRoyaleService.getOne(new QueryWrapper<TournamentRoyaleEntity>().lambda()
                .eq(TournamentRoyaleEntity::getTournamentId, tournamentId)
                .eq(TournamentRoyaleEntity::getEvent, nextEvent));
        if (nextTournamentRoyaleEntity == null) {
            nextTournamentRoyaleEntity = new TournamentRoyaleEntity()
                    .setTournamentId(tournamentId)
                    .setEvent(nextEvent)
                    .setEventEliminatedNum(eliminateNumberMap.get("next"))
                    .setNextEventEliminatedNum(1)
                    .setEventEliminatedEntries("")
                    .setWaitingEliminatedEntries(nextWaitingEliminatedEntries)
                    .setAllEliminatedEntries(nextAllEliminatedEntries);
            this.tournamentRoyaleService.save(nextTournamentRoyaleEntity);
            log.info("event:{}, tournament:{}, insert next event tournament_royale success", event, tournamentId);
            return;
        }
        nextTournamentRoyaleEntity
                .setTournamentId(tournamentId)
                .setEvent(nextEvent)
                .setEventEliminatedNum(eliminateNumberMap.get("next"))
                .setNextEventEliminatedNum(1)
                .setEventEliminatedEntries("")
                .setWaitingEliminatedEntries(nextWaitingEliminatedEntries)
                .setAllEliminatedEntries(nextAllEliminatedEntries);
        this.tournamentRoyaleService.updateById(nextTournamentRoyaleEntity);
        log.info("event:{}, tournament:{}, update next event tournament_royale success", event, tournamentId);
    }

    private Map<String, Integer> calculateEliminateNumbers(int event, int lastEliminateNumber, TournamentInfoEntity tournamentInfoEntity, List<Integer> entryList) {
        int startEvent = tournamentInfoEntity.getGroupStartGw();
        int endEvent = tournamentInfoEntity.getGroupEndGw();
        if (event < startEvent || event > endEvent) {
            return Maps.newHashMap();
        }
        Map<String, Integer> map = Maps.newHashMap();
        int eventEliminateNumber;
        int nextEventEliminateNumber;
        // make every two events as a round, and each round will eliminate two entries
        boolean settleEvent = (event - startEvent + 1) % 2 == 0;
        // if an event is not a settle event, it will eliminate 0 or 1 entry, depending on whether two or more entries score the same event points
        // if an event is a settle event, it will eliminate accurately 2 entries
        if (settleEvent) {
            eventEliminateNumber = 2 - lastEliminateNumber;
            nextEventEliminateNumber = 1;
        } else {
            List<EntryEventResultEntity> entryEventResultEntityList = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                    .eq(EntryEventResultEntity::getEvent, event)
                    .in(EntryEventResultEntity::getEntry, entryList));
            int minEventPoints = entryEventResultEntityList.stream().mapToInt(EntryEventResultEntity::getEventPoints).min().orElse(0);
            int minNums = (int) entryEventResultEntityList.stream().filter(o -> o.getEventPoints() == minEventPoints).count();
            if (minNums > 1) {
                eventEliminateNumber = 0;
                nextEventEliminateNumber = 2;
            } else {
                eventEliminateNumber = 1;
                nextEventEliminateNumber = 1;
            }
        }
        map.put("current", eventEliminateNumber);
        map.put("next", nextEventEliminateNumber);
        return map;
    }

    @Override
    public void updatePointsRaceGroupResult(int event, int tournamentId) {
        if (event < 1 || event > 38 || tournamentId <= 0) {
            log.error("event:{}, tournament:{}, params error", event, tournamentId);
            return;
        }
        // tournament_info
        TournamentInfoEntity tournamentInfoEntity = this.queryService.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            log.error("event:{}, tournament:{}, tournament_info not exists", event, tournamentId);
            return;
        }
        if (!StringUtils.equals(GroupMode.Points_race.name(), tournamentInfoEntity.getGroupMode())) {
            log.error("event:{}, tournament:{}, not points group", event, tournamentId);
            return;
        }
        // check gw
        int groupStartGw = tournamentInfoEntity.getGroupStartGw();
        int groupEndGw = tournamentInfoEntity.getGroupEndGw();
        if (event > groupEndGw) {
            log.error("event:{}, tournament:{}, group stage passed", event, tournamentId);
            return;
        }
        // entry list
        List<Integer> entryList = this.queryService.qryEntryListByTournament(tournamentId);
        // entry_event_result
        Map<Integer, EntryEventResultEntity> eventResultMap = this.getEntryEventResultByEvent(event, entryList);
        if (CollectionUtils.isEmpty(eventResultMap)) {
            log.error("event:{}, tournament:{}, event_result not updated", event, tournamentId);
            return;
        }
        // tournament_group
        List<TournamentGroupEntity> tournamentGroupEntityList = this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda().eq(TournamentGroupEntity::getTournamentId, tournamentId).in(TournamentGroupEntity::getEntry, entryList));
        if (CollectionUtils.isEmpty(tournamentGroupEntityList)) {
            return;
        }
        Map<Integer, TournamentPointsGroupResultEntity> tournamentPointsGroupResultEntityMap = this.tournamentPointsGroupResultService.list(new QueryWrapper<TournamentPointsGroupResultEntity>().lambda().eq(TournamentPointsGroupResultEntity::getTournamentId, tournamentId).eq(TournamentPointsGroupResultEntity::getEvent, event)).stream().collect(Collectors.toMap(TournamentPointsGroupResultEntity::getEntry, o -> o));
        // update tournament_group and tournament_group_result
        List<TournamentGroupEntity> updateGroupList = Lists.newArrayList();
        List<TournamentPointsGroupResultEntity> updateGroupPointsResultList = Lists.newArrayList();
        // tournament_group
        tournamentGroupEntityList.forEach(tournamentGroupEntity -> {
            int entry = tournamentGroupEntity.getEntry();
            EntryEventResultEntity entryEventResultEntity = eventResultMap.getOrDefault(entry, null);
            if (entryEventResultEntity == null) {
                log.error("event:{}, tournament:{}, tournament_group not exists", event, tournamentId);
                return;
            }
            tournamentGroupEntity.setPlay(event - tournamentGroupEntity.getStartGw() + 1).setTotalPoints(this.entryEventResultService.sumEventPoints(event, groupStartGw, groupEndGw, entry)).setTotalTransfersCost(this.entryEventResultService.sumEventTransferCost(event, groupStartGw, groupEndGw, entry)).setTotalNetPoints(this.entryEventResultService.sumEventNetPoints(event, groupStartGw, groupEndGw, entry)).setOverallRank(entryEventResultEntity.getOverallRank());
            // tournament_points_group_result
            TournamentPointsGroupResultEntity tournamentPointsGroupResultEntity = tournamentPointsGroupResultEntityMap.get(entry);
            if (tournamentPointsGroupResultEntity == null) {
                log.error("event:{}, tournament:{}, entry:{}, event_result not updated", event, tournamentId, entry);
                return;
            }
            tournamentPointsGroupResultEntity.setEventPoints(entryEventResultEntity.getEventPoints()).setEventCost(entryEventResultEntity.getEventTransfersCost()).setEventNetPoints(entryEventResultEntity.getEventPoints() - entryEventResultEntity.getEventTransfersCost()).setEventRank(entryEventResultEntity.getEventRank());
        });
        // sort group rank
        Map<String, Integer> groupRankMap = this.sortPointsRaceGroupRank(tournamentGroupEntityList);  // key:overall_rank -> value:group_rank
        tournamentGroupEntityList.forEach(tournamentGroupEntity -> {
            int groupRank = groupRankMap.getOrDefault(tournamentGroupEntity.getTotalNetPoints() + "-" + tournamentGroupEntity.getOverallRank(), 0);
            tournamentGroupEntity.setGroupPoints(tournamentGroupEntity.getTotalNetPoints()).setGroupRank(groupRank);
            updateGroupList.add(tournamentGroupEntity);
            TournamentPointsGroupResultEntity tournamentPointsGroupResultEntity = tournamentPointsGroupResultEntityMap.get(tournamentGroupEntity.getEntry());
            if (tournamentPointsGroupResultEntity == null) {
                return;
            }
            tournamentPointsGroupResultEntity.setEventGroupRank(groupRank);
            updateGroupPointsResultList.add(tournamentPointsGroupResultEntity);
        });
        // update
        this.tournamentGroupService.updateBatchById(updateGroupList);
        log.info("event:{}, tournament:{}, update tournament_group size:{}", event, tournamentId, updateGroupList.size());
        this.tournamentPointsGroupResultService.updateBatchById(updateGroupPointsResultList);
        log.info("event:{}, tournament:{}, update tournament_points_group_result size:{}", event, tournamentId, updateGroupPointsResultList.size());
    }

    private Map<Integer, EntryEventResultEntity> getEntryEventResultByEvent(int event, List<Integer> entryList) {
        return this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda().eq(EntryEventResultEntity::getEvent, event).in(EntryEventResultEntity::getEntry, entryList)).stream().collect(Collectors.toMap(EntryEventResultEntity::getEntry, o -> o));
    }

    @Override
    public void updateBattleRaceGroupResult(int event, int tournamentId) {
        if (event < 1 || event > 38 || tournamentId <= 0) {
            log.error("event:{}, tournament:{}, params error", event, tournamentId);
            return;
        }
        // tournament_info
        TournamentInfoEntity tournamentInfoEntity = this.queryService.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            log.error("event:{}, tournament:{}, tournament_info not exists", event, tournamentId);
            return;
        }
        if (!StringUtils.equals(GroupMode.Battle_race.name(), tournamentInfoEntity.getGroupMode())) {
            log.error("event:{}, tournament:{}, not battle group", event, tournamentId);
            return;
        }
        // check gw
        int groupStartGw = tournamentInfoEntity.getGroupStartGw();
        int groupEndGw = tournamentInfoEntity.getGroupEndGw();
        if (event > groupEndGw) {
            log.error("event:{}, tournament:{}, group stage passed", event, tournamentId);
            return;
        }
        // tournament_entry
        List<Integer> entryList = this.queryService.qryEntryListByTournament(tournamentId);
        // entry_event_result
        Map<Integer, EntryEventResultEntity> entryEventResultMap = this.getEntryEventResultByEvent(event, entryList);
        if (CollectionUtils.isEmpty(entryEventResultMap)) {
            log.error("event:{}, tournament:{}, event_result not update", event, tournamentId);
            return;
        }
        // tournament_group_battle_result
        Table<Integer, Integer, Integer> battleResultTable = this.updateGroupBattleResult(event, tournamentId, entryEventResultMap);
        // tournament_group
        this.updateTournamentGroup(event, tournamentId, tournamentInfoEntity.getGroupQualifiers(), groupStartGw, groupEndGw, battleResultTable, entryEventResultMap);
    }

    private Table<Integer, Integer, Integer> updateGroupBattleResult(int event, int tournamentId, Map<Integer, EntryEventResultEntity> entryEventResultMap) {
        List<TournamentBattleGroupResultEntity> tournamentBattleGroupResultList = Lists.newArrayList();
        Table<Integer, Integer, Integer> battleResultTable = HashBasedTable.create(); // groupId -> entry -> matchPoints
        this.tournamentBattleGroupResultService.list(new QueryWrapper<TournamentBattleGroupResultEntity>().lambda().eq(TournamentBattleGroupResultEntity::getTournamentId, tournamentId).eq(TournamentBattleGroupResultEntity::getEvent, event)).forEach(groupBattleResult -> {
            int homeEntry = groupBattleResult.getHomeEntry();
            int awayEntry = groupBattleResult.getAwayEntry();
            EntryEventResultEntity homeEventResult = entryEventResultMap.getOrDefault(homeEntry, new EntryEventResultEntity());
            EntryEventResultEntity awayEventResult = entryEventResultMap.getOrDefault(awayEntry, new EntryEventResultEntity());
            int homeEntryMatchPoints = this.getGroupBattleHomeEntryResult(homeEventResult, awayEventResult);
            int awayEntryMatchPoints = this.getGroupBattleHomeEntryResult(awayEventResult, homeEventResult);
            if (homeEntry != 0) {
                battleResultTable.put(groupBattleResult.getGroupId(), homeEntry, homeEntryMatchPoints);
            }
            if (awayEntry != 0) {
                battleResultTable.put(groupBattleResult.getGroupId(), awayEntry, awayEntryMatchPoints);
            }
            tournamentBattleGroupResultList.add(groupBattleResult.setHomeEntryNetPoints(homeEventResult.getEventNetPoints()).setHomeEntryRank(homeEventResult.getEventRank()).setHomeEntryMatchPoints(homeEntryMatchPoints).setAwayEntryNetPoints(awayEventResult.getEventNetPoints()).setAwayEntryRank(awayEventResult.getEventRank()).setAwayEntryMatchPoints(awayEntryMatchPoints));
        });
        this.tournamentBattleGroupResultService.updateBatchById(tournamentBattleGroupResultList);
        log.info("event:{}, tournament:{}, update tournament_battle_group_result", event, tournamentId);
        // return
        return battleResultTable;
    }

    private int getGroupBattleHomeEntryResult(EntryEventResultEntity firstEventResult, EntryEventResultEntity secondEventResult) {
        if (firstEventResult.getEventNetPoints() > secondEventResult.getEventNetPoints()) {
            return 3;
        } else if (firstEventResult.getEventNetPoints() < secondEventResult.getEventNetPoints()) {
            return 0;
        } else {
            return 1;
        }
    }

    private void updateTournamentGroup(int event, int tournamentId, int qualifiers, int startGw, int endGw, Table<Integer, Integer, Integer> battleResultTable, Map<Integer, EntryEventResultEntity> entryEventResultMap) {
        List<TournamentGroupEntity> tournamentGroupList = Lists.newArrayList();
        int playedEvent = event - startGw + 1;
        battleResultTable.rowKeySet().forEach(groupId -> {
            // prepare
            Map<Integer, Integer> matchResultMap = battleResultTable.row(groupId); // entry -> matchPoints
            List<TournamentGroupEntity> groupList = this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda().eq(TournamentGroupEntity::getTournamentId, tournamentId).eq(TournamentGroupEntity::getGroupId, groupId).orderByAsc(TournamentGroupEntity::getGroupIndex));
            groupList.forEach(tournamentGroupEntity -> {
                int entry = tournamentGroupEntity.getEntry();
                // total_points and overall_rank
                EntryEventResultEntity entryEventResult = entryEventResultMap.getOrDefault(entry, null);
                if (entryEventResult != null) {
                    tournamentGroupEntity.setTotalPoints(this.entryEventResultService.sumEventPoints(event, startGw, endGw, entry)).setTotalTransfersCost(this.entryEventResultService.sumEventTransferCost(event, startGw, endGw, entry)).setTotalNetPoints(this.entryEventResultService.sumEventNetPoints(event, startGw, endGw, entry)).setOverallRank(entryEventResult.getOverallRank());
                }
                if (tournamentGroupEntity.getPlay() == playedEvent) {
                    return;
                }
                // group points
                int matchPoints = matchResultMap.getOrDefault(entry, 0);
                tournamentGroupEntity.setGroupPoints(tournamentGroupEntity.getGroupPoints() + matchPoints).setPlay(tournamentGroupEntity.getPlay() + 1);
                if (matchPoints == 3) {
                    tournamentGroupEntity.setWin(tournamentGroupEntity.getWin() + 1);
                } else if (matchPoints == 0) {
                    tournamentGroupEntity.setLose(tournamentGroupEntity.getLose() + 1);
                } else {
                    tournamentGroupEntity.setDraw(tournamentGroupEntity.getDraw() + 1);
                }
            });
            // sort group list by group points
            Map<String, Integer> groupRankMap = this.sortBattleGroupRank(groupList); // entry -> groupRank
            groupList.forEach(tournamentGroupEntity -> tournamentGroupList.add(tournamentGroupEntity.setGroupRank(groupRankMap.getOrDefault(tournamentGroupEntity.getGroupPoints() + "-" + tournamentGroupEntity.getOverallRank(), 0)).setQualified(tournamentGroupEntity.getGroupRank() <= qualifiers)));
        });
        this.tournamentGroupService.updateBatchById(tournamentGroupList);
        log.info("event:{}, tournament:{}, update tournament_group size:{}", event, tournamentId, tournamentGroupList.size());
    }

    private Map<String, Integer> sortPointsRaceGroupRank(List<TournamentGroupEntity> tournamentGroupEntityList) {
        Map<Integer, List<TournamentGroupEntity>> groupEntityMap = Maps.newHashMap();
        tournamentGroupEntityList.forEach(o -> {
            int groupId = o.getGroupId();
            List<TournamentGroupEntity> list = Lists.newArrayList();
            if (groupEntityMap.containsKey(groupId)) {
                list = groupEntityMap.get(groupId);
            }
            list.add(o);
            groupEntityMap.put(groupId, list);
        });
        Map<String, Integer> map = Maps.newHashMap();
        groupEntityMap.keySet().forEach(groupId -> {
            Map<String, Integer> groupRankMap = this.sortPointsRaceEachGroupRank(groupEntityMap.get(groupId));
            map.putAll(groupRankMap);
        });
        return map;
    }

    private Map<String, Integer> sortPointsRaceEachGroupRank(List<TournamentGroupEntity> tournamentGroupEntityList) {
        Map<String, Integer> groupRankMap = Maps.newHashMap(); // entry -> groupRank
        Map<String, Integer> groupRankCountMap = Maps.newLinkedHashMap();
        tournamentGroupEntityList.stream().filter(o -> o.getTotalNetPoints() != 0).sorted(Comparator.comparing(TournamentGroupEntity::getTotalNetPoints).reversed().thenComparing(TournamentGroupEntity::getOverallRank)).forEachOrdered(o -> this.setGroupRankMapValue(o.getTotalNetPoints() + "-" + o.getOverallRank(), groupRankCountMap));
        tournamentGroupEntityList.stream().filter(o -> o.getTotalNetPoints() == 0).sorted(Comparator.comparingInt(TournamentGroupEntity::getEntry)).forEachOrdered(o -> this.setGroupRankMapValue(0 + "-" + o.getOverallRank(), groupRankCountMap));
        int index = 1;
        for (String key : groupRankCountMap.keySet()) {
            groupRankMap.put(key, index);
            index += groupRankCountMap.get(key);
        }
        return groupRankMap;
    }

    private Map<String, Integer> sortBattleGroupRank(List<TournamentGroupEntity> tournamentGroupEntityList) {
        Map<String, Integer> groupRankMap = Maps.newLinkedHashMap();
        Map<String, Integer> groupRankCountMap = Maps.newLinkedHashMap();
        tournamentGroupEntityList.stream().filter(o -> o.getTotalPoints() != 0).sorted(Comparator.comparing(TournamentGroupEntity::getGroupPoints).reversed().thenComparing(TournamentGroupEntity::getOverallRank)).forEachOrdered(o -> this.setGroupRankMapValue(o.getGroupPoints() + "-" + o.getOverallRank(), groupRankCountMap));
        tournamentGroupEntityList.stream().filter(o -> o.getTotalPoints() == 0).sorted(Comparator.comparingInt(TournamentGroupEntity::getEntry)).forEachOrdered(o -> this.setGroupRankMapValue(o.getGroupPoints() + "-" + o.getOverallRank(), groupRankCountMap));
        int index = 1;
        for (String key : groupRankCountMap.keySet()) {
            groupRankMap.put(key, index);
            index += groupRankCountMap.get(key);
        }
        return groupRankMap;
    }

    private void setGroupRankMapValue(String key, Map<String, Integer> groupRankCountMap) {
        if (groupRankCountMap.containsKey(key)) {
            groupRankCountMap.put(key, groupRankCountMap.get(key) + 1);
        } else {
            groupRankCountMap.put(key, 1);
        }
    }

    @Override
    public void updateKnockoutResult(int event, int tournamentId) {
        if (event < 1 || event > 38 || tournamentId <= 0) {
            log.error("event:{}, tournament:{}, params error", event, tournamentId);
            return;
        }
        // tournament_info
        TournamentInfoEntity tournamentInfoEntity = this.queryService.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            log.error("event:{}, tournament:{}, tournament_info not exists", event, tournamentId);
            return;
        }
        if (StringUtils.equals(KnockoutMode.No_knockout.name(), tournamentInfoEntity.getKnockoutMode())) {
            log.error("event:{}, tournament:{}, no knockout", event, tournamentId);
            return;
        }
        // check gw
        int knockoutEndGw = tournamentInfoEntity.getKnockoutEndGw();
        if (event > knockoutEndGw) {
            log.error("event:{}, tournament:{}, knockout stage passed", event, tournamentId);
            return;
        }
        // get entry_list by tournament
        List<Integer> entryList = this.queryService.qryEntryListByTournament(tournamentId);
        // get event_result list
        Map<Integer, EntryEventResultEntity> eventResultMap = this.getEntryEventResultByEvent(event, entryList);
        if (CollectionUtils.isEmpty(eventResultMap)) {
            log.error("event:{}, tournament:{}, event_result not update", event, tournamentId);
            return;
        }
        // get event_live
        Map<String, EventLiveEntity> eventLiveMap = this.redisCacheService.getEventLiveByEvent(event);
        if (CollectionUtils.isEmpty(eventResultMap)) {
            log.error("event:{}, tournament:{}, event_live not update", event, tournamentId);
            return;
        }
        // tournament_knockout_result
        Multimap<Integer, TournamentKnockoutResultData> knockoutResultDataMap = this.updateKnockoutResult(tournamentId, event, eventResultMap, eventLiveMap);
        if (CollectionUtils.isEmpty(knockoutResultDataMap.values())) {
            return;
        }
        // tournament_knockout
        Map<Integer, TournamentKnockoutNextRoundData> nextKnockoutMap = this.updateKnockoutInfo(tournamentId, event, knockoutResultDataMap);
        if (CollectionUtils.isEmpty(nextKnockoutMap)) {
            return;
        }
        // next round entry
        this.updateNextKnockout(tournamentId, nextKnockoutMap);
    }

    private Multimap<Integer, TournamentKnockoutResultData> updateKnockoutResult(int tournamentId, int event, Map<Integer, EntryEventResultEntity> eventResultMap, Map<String, EventLiveEntity> eventLiveMap) {
        List<TournamentKnockoutResultEntity> tournamentKnockoutResultList = Lists.newArrayList();
        // matchId -> tournament_knockout_result data
        Multimap<Integer, TournamentKnockoutResultData> knockoutResultDataMap = HashMultimap.create();
        // tournament_knockout_result
        this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda().eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId).eq(TournamentKnockoutResultEntity::getEvent, event).orderByAsc(TournamentKnockoutResultEntity::getMatchId)).forEach(knockoutResult -> {
            int homeEntry = knockoutResult.getHomeEntry();
            int awayEntry = knockoutResult.getAwayEntry();
            EntryEventResultEntity homeEventResult = eventResultMap.getOrDefault(homeEntry, this.initEntryEventResult(homeEntry));
            EntryEventResultEntity awayEventResult = eventResultMap.getOrDefault(awayEntry, this.initEntryEventResult(awayEntry));
            // entry_pick
            List<EntryPickData> homePickList = Lists.newArrayList();
            if (homeEventResult.getEntry() > 0) {
                homePickList = JsonUtils.json2Collection(homeEventResult.getEventPicks(), List.class, EntryPickData.class);
            }
            List<EntryPickData> awayPickList = Lists.newArrayList();
            if (awayEventResult.getEntry() > 0) {
                awayPickList = JsonUtils.json2Collection(awayEventResult.getEventPicks(), List.class, EntryPickData.class);
            }
            String homeChip = homeEventResult.getEventChip();
            String awayChip = awayEventResult.getEventChip();
            // net_points
            int homeNetPoints = homeEventResult.getEventNetPoints();
            int awayNetPoints = awayEventResult.getEventNetPoints();
            // goals_scored
            int homeGoalsScored = this.calcTotalGoalsScored(homePickList, homeChip, eventLiveMap);
            int awayGoalsScored = this.calcTotalGoalsScored(awayPickList, awayChip, eventLiveMap);
            // goals_conceded
            int homeGoalsConceded = this.calcTotalGoalsConceded(homePickList, homeChip, eventLiveMap);
            int awayGoalsConceded = this.calcTotalGoalsConceded(awayPickList, awayChip, eventLiveMap);
            // match winner
            int matchWinner = this.getMatchWinner(homeEntry, awayEntry, homeNetPoints, awayNetPoints, homeGoalsScored, awayGoalsScored, homeGoalsConceded, awayGoalsConceded);
            // update
            knockoutResultDataMap.put(knockoutResult.getMatchId(), new TournamentKnockoutResultData().setEvent(event).setPlayAgainstId(knockoutResult.getPlayAgainstId()).setMatchId(knockoutResult.getMatchId()).setHomeEntryNetPoints(homeNetPoints).setHomeEntryGoalsScored(homeGoalsScored).setHomeEntryGoalsConceded(homeGoalsConceded).setAwayEntryNetPoints(awayNetPoints).setAwayEntryGoalsScored(awayGoalsScored).setAwayEntryGoalsConceded(awayGoalsConceded).setMatchWinner(matchWinner).setWinnerRank(matchWinner == homeEntry ? homeEventResult.getOverallRank() : awayEventResult.getOverallRank()));
            tournamentKnockoutResultList.add(knockoutResult.setHomeEntryNetPoints(homeEntry > 0 ? homeEventResult.getEventNetPoints() : 0).setHomeEntryRank(homeEntry > 0 ? homeEventResult.getEventRank() : 0).setHomeEntryGoalsScored(homeGoalsScored).setHomeEntryGoalsConceded(homeGoalsConceded).setAwayEntryNetPoints(awayEntry > 0 ? awayEventResult.getEventNetPoints() : 0).setAwayEntryRank(awayEntry > 0 ? awayEventResult.getEventRank() : 0).setAwayEntryGoalsScored(awayGoalsScored).setAwayEntryGoalsConceded(awayGoalsConceded).setMatchWinner(matchWinner));
        });
        this.tournamentKnockoutResultService.updateBatchById(tournamentKnockoutResultList);
        log.info("event:{}, tournament:{}, update tournament_knockout_result size:{}", event, tournamentId, tournamentKnockoutResultList.size());
        return knockoutResultDataMap;
    }

    private EntryEventResultEntity initEntryEventResult(int entry) {
        return new EntryEventResultEntity().setEvent(0).setEntry(entry).setEventPoints(0).setEventTransfers(0).setEventTransfersCost(0).setEventNetPoints(0).setEventBenchPoints(0).setEventAutoSubPoints(0).setEventRank(0).setEventChip("").setPlayedCaptain(0).setCaptainPoints(0).setEventPicks("").setEventAutoSubs("").setOverallPoints(0).setOverallRank(0).setTeamValue(0).setBank(0);
    }

    private int calcTotalGoalsScored(List<EntryPickData> pickList, String chip, Map<String, EventLiveEntity> eventLiveMap) {
        if (StringUtils.equals(Chip.BB.name(), chip)) {
            return pickList.stream().map(EntryPickData::getElement).mapToInt(o -> {
                EventLiveEntity eventLiveEntity = eventLiveMap.getOrDefault(String.valueOf(o), null);
                if (eventLiveEntity == null) {
                    return 0;
                }
                return eventLiveEntity.getGoalsScored();
            }).sum();
        }
        return pickList.stream().filter(o -> o.getPosition() < 12).map(EntryPickData::getElement).mapToInt(o -> {
            EventLiveEntity eventLiveEntity = eventLiveMap.getOrDefault(String.valueOf(o), null);
            if (eventLiveEntity == null) {
                return 0;
            }
            return eventLiveEntity.getGoalsScored();
        }).sum();
    }

    private int calcTotalGoalsConceded(List<EntryPickData> pickList, String chip, Map<String, EventLiveEntity> eventLiveMap) {
        if (StringUtils.equals(Chip.BB.name(), chip)) {
            return pickList.stream().map(EntryPickData::getElement).mapToInt(o -> {
                EventLiveEntity eventLiveEntity = eventLiveMap.getOrDefault(String.valueOf(o), null);
                if (eventLiveEntity == null) {
                    return 0;
                }
                return eventLiveEntity.getGoalsConceded();
            }).sum();
        }
        return pickList.stream().filter(o -> o.getPosition() < 12).map(EntryPickData::getElement).mapToInt(o -> {
            EventLiveEntity eventLiveEntity = eventLiveMap.getOrDefault(String.valueOf(o), null);
            if (eventLiveEntity == null) {
                return 0;
            }
            return eventLiveEntity.getGoalsConceded();
        }).sum();
    }

    private Map<Integer, TournamentKnockoutNextRoundData> updateKnockoutInfo(int tournamentId, int event, Multimap<Integer, TournamentKnockoutResultData> knockoutResultDataMap) {
        List<TournamentKnockoutEntity> tournamentKnockoutList = Lists.newArrayList();
        // next_match_id -> tournament_knockout_result data
        Map<Integer, TournamentKnockoutNextRoundData> nextKnockoutMap = Maps.newHashMap();
        Map<Integer, TournamentKnockoutEntity> knockoutMap = this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda().eq(TournamentKnockoutEntity::getTournamentId, tournamentId).eq(TournamentKnockoutEntity::getEndGw, event).orderByAsc(TournamentKnockoutEntity::getMatchId)).stream().collect(Collectors.toMap(TournamentKnockoutEntity::getMatchId, v -> v));
        // match_id list
        List<Integer> matchIdList = new ArrayList<>(knockoutResultDataMap.keySet());
        // match_id -> net_points
        Multimap<Integer, TournamentKnockoutResultEntity> matchNetPointsMultiMap = HashMultimap.create();
        this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda().eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId).in(TournamentKnockoutResultEntity::getMatchId, matchIdList)).forEach(o -> matchNetPointsMultiMap.put(o.getMatchId(), o));
        // update by match_id
        matchIdList.forEach(matchId -> {
            Collection<TournamentKnockoutResultData> tournamentKnockoutResultDataCollection = knockoutResultDataMap.get(matchId);
            int homeEntryRoundGoalsScored = tournamentKnockoutResultDataCollection.stream().filter(o -> o.getEvent() == event).mapToInt(TournamentKnockoutResultData::getHomeEntryGoalsScored).sum();
            int homeEntryRoundGoalsConceded = tournamentKnockoutResultDataCollection.stream().filter(o -> o.getEvent() == event).mapToInt(TournamentKnockoutResultData::getHomeEntryGoalsConceded).sum();
            int awayEntryRoundGoalsScored = tournamentKnockoutResultDataCollection.stream().filter(o -> o.getEvent() == event).mapToInt(TournamentKnockoutResultData::getAwayEntryGoalsScored).sum();
            int awayEntryRoundGoalsConceded = tournamentKnockoutResultDataCollection.stream().filter(o -> o.getEvent() == event).mapToInt(TournamentKnockoutResultData::getAwayEntryGoalsConceded).sum();
            tournamentKnockoutResultDataCollection.forEach(resultData -> {
                // update tournament_knockout
                if (!knockoutMap.containsKey(matchId)) {
                    return;
                }
                TournamentKnockoutEntity knockoutEntity = knockoutMap.get(matchId);
                if (resultData.getEvent() != knockoutEntity.getEndGw()) { // round not finished
                    return;
                }
                int homeEntry = knockoutEntity.getHomeEntry();
                int homeEntryRoundNetPoints = matchNetPointsMultiMap.values().stream().mapToInt(o -> {
                            if (o.getHomeEntry() == homeEntry) {
                                return o.getHomeEntryNetPoints();
                            } else if (o.getAwayEntry() == homeEntry) {
                                return o.getAwayEntryNetPoints();
                            }
                            return 0;
                        })
                        .sum();
                int awayEntry = knockoutEntity.getAwayEntry();
                int awayEntryRoundNetPoints = matchNetPointsMultiMap.values().stream().mapToInt(o -> {
                            if (o.getHomeEntry() == awayEntry) {
                                return o.getHomeEntryNetPoints();
                            } else if (o.getAwayEntry() == awayEntry) {
                                return o.getAwayEntryNetPoints();
                            }
                            return 0;
                        })
                        .sum();
                knockoutEntity.setHomeEntryNetPoints(homeEntryRoundNetPoints).setAwayEntryNetPoints(awayEntryRoundNetPoints).setHomeEntryGoalsScored(homeEntryRoundGoalsScored).setHomeEntryGoalsConceded(homeEntryRoundGoalsConceded).setAwayEntryGoalsScored(awayEntryRoundGoalsScored).setAwayEntryGoalsConceded(awayEntryRoundGoalsConceded).setRoundWinner(this.getRoundWinner(matchNetPointsMultiMap.get(matchId), homeEntry, awayEntry, homeEntryRoundNetPoints, homeEntryRoundGoalsScored, homeEntryRoundGoalsConceded, awayEntryRoundNetPoints, awayEntryRoundGoalsScored, awayEntryRoundGoalsConceded));
                // previous results
                List<TournamentKnockoutResultEntity> tournamentKnockoutResultList = this.queryService.qryKnockoutResultTournamentList(tournamentId, knockoutEntity.getMatchId());
                double homeEntryWinningNum = this.calcEntryWinningNum(tournamentKnockoutResultList, homeEntry);
                double awayEntryWinningNum = this.calcEntryWinningNum(tournamentKnockoutResultList, awayEntry);
                knockoutEntity
                        .setHomeEntryWinningNum(homeEntryWinningNum)
                        .setAwayEntryWinningNum(awayEntryWinningNum);
                tournamentKnockoutList.add(knockoutEntity);
                // set next round data
                this.setNextRoundData(nextKnockoutMap, knockoutEntity);
            });
        });
        this.tournamentKnockoutService.updateBatchById(tournamentKnockoutList);
        log.info("event:{}, tournament:{}, update tournament_knockout_info size:{}", tournamentId, event, tournamentKnockoutList.size());
        return nextKnockoutMap;
    }

    private double calcEntryWinningNum(List<TournamentKnockoutResultEntity> tournamentKnockoutResultList, int entry) {
        double winningNum = 0;
        for (TournamentKnockoutResultEntity o : tournamentKnockoutResultList) {
            if (o.getHomeEntry() == entry && o.getHomeEntryNetPoints() > o.getAwayEntryNetPoints()) {
                winningNum++;
            } else if (o.getAwayEntry() == entry && o.getAwayEntryNetPoints() > o.getHomeEntryNetPoints()) {
                winningNum++;
            } else if (o.getHomeEntryNetPoints() == o.getAwayEntryNetPoints()) {
                winningNum = winningNum + 0.5;
            }
        }
        return winningNum;
    }

    private void updateNextKnockout(int tournamentId, Map<Integer, TournamentKnockoutNextRoundData> nextKnockoutMap) {
        // get round
        int nextRound = nextKnockoutMap.values().stream().map(TournamentKnockoutNextRoundData::getNextRound).findFirst().orElse(0);
        if (nextRound == 0) {
            return;
        }
        // tournament_knockout
        List<TournamentKnockoutEntity> tournamentKnockoutEntityList = Lists.newArrayList();
        this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda().eq(TournamentKnockoutEntity::getTournamentId, tournamentId).eq(TournamentKnockoutEntity::getRound, nextRound)).forEach(knockoutEntity -> tournamentKnockoutEntityList.add(knockoutEntity.setHomeEntry(nextKnockoutMap.get(knockoutEntity.getMatchId()).getNextRoundHomeEntry()).setAwayEntry(nextKnockoutMap.get(knockoutEntity.getMatchId()).getNextRoundAwayEntry())));
        // tournament_knockout_result
        List<TournamentKnockoutResultEntity> tournamentKnockoutResultList = Lists.newArrayList();
        this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda().eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId).in(TournamentKnockoutResultEntity::getMatchId, nextKnockoutMap.keySet())).forEach(knockoutResultEntity -> {
            int playAgainstId = knockoutResultEntity.getPlayAgainstId();
            int homeEntry = nextKnockoutMap.get(knockoutResultEntity.getMatchId()).getNextRoundHomeEntry();
            int awayEntry = nextKnockoutMap.get(knockoutResultEntity.getMatchId()).getNextRoundAwayEntry();
            if (playAgainstId % 2 == 1) {
                tournamentKnockoutResultList.add(knockoutResultEntity.setHomeEntry(homeEntry).setAwayEntry(awayEntry));
            } else {
                tournamentKnockoutResultList.add(knockoutResultEntity.setHomeEntry(awayEntry).setAwayEntry(homeEntry));
            }
        });
        this.tournamentKnockoutService.updateBatchById(tournamentKnockoutEntityList);
        this.tournamentKnockoutResultService.updateBatchById(tournamentKnockoutResultList);
        log.info("tournament:{}, update tournament_next_knockout_info size:{}", tournamentId, tournamentKnockoutResultList.size());
    }

    private int getMatchWinner(int homeEntry, int awayEntry, int homeNetPoints, int awayNetPoints, int homeGoalsScored, int awayGoalsScored, int homeGoalsConceded, int awayGoalsConceded) {
        // if blank
        if (homeEntry <= 0) {
            return awayEntry;
        } else if (awayEntry <= 0) {
            return homeEntry;
        }
        // compare order: net points; most goals scored; fewest goals conceded; random
        int winner = this.compareNetPoint(homeEntry, awayEntry, homeNetPoints, awayNetPoints);
        if (winner != 0) {
            return winner;
        }
        winner = this.compareGoalsScored(homeEntry, awayEntry, homeGoalsScored, awayGoalsScored);
        if (winner != 0) {
            return winner;
        }
        winner = this.compareGoalsConceded(homeEntry, awayEntry, homeGoalsConceded, awayGoalsConceded);
        if (winner != 0) {
            return winner;
        }
        return this.randomWinner(homeEntry, awayEntry);
    }

    private int compareNetPoint(int homeEntry, int awayEntry, int homeNetPoints, int awayNetPoints) {
        if (homeNetPoints > awayNetPoints) {
            return homeEntry;
        } else if (homeNetPoints < awayNetPoints) {
            return awayEntry;
        }
        return 0;
    }

    private int compareGoalsScored(int homeEntry, int awayEntry, int homeGoalsScored, int awayGoalsScored) {
        if (homeGoalsScored > awayGoalsScored) {
            return homeEntry;
        } else if (homeGoalsScored < awayGoalsScored) {
            return awayEntry;
        }
        return 0;
    }

    private int compareGoalsConceded(int homeEntry, int awayEntry, int homeGoalsConceded, int awayGoalsConceded) {
        if (homeGoalsConceded < awayGoalsConceded) {
            return homeEntry;
        } else if (homeGoalsConceded > awayGoalsConceded) {
            return awayEntry;
        }
        return 0;
    }

    private int randomWinner(int homeEntry, int awayEntry) {
        if (new Random().nextInt(10) % 2 == 0) {
            return homeEntry;
        } else {
            return awayEntry;
        }
    }

    private int getRoundWinner(Collection<TournamentKnockoutResultEntity> collection, int homeEntry, int awayEntry, int homeEntryRoundNetPoints, int homeEntryRoundGoalsScored, int homeEntryRoundGoalsConceded, int awayEntryRoundNetPoints, int awayEntryRoundGoalsScored, int awayEntryRoundGoalsConceded) {
        if (collection.size() == 1) {
            return collection.stream().map(TournamentKnockoutResultEntity::getMatchWinner).findFirst().orElse(0);
        }
        List<TournamentKnockoutResultEntity> winners = new ArrayList<>(collection);
        int firstWinner = winners.get(0).getMatchWinner();
        int secondWinner = collection.stream().map(TournamentKnockoutResultEntity::getMatchWinner).filter(matchWinner -> matchWinner != firstWinner).findFirst().orElse(0); // find the other match winner
        if (secondWinner == 0) { // all matches won by the first winner
            return firstWinner;
        }
        // compare order: net points; most goals scored; fewest goals conceded; random
        int roundWinner = this.compareNetPoint(homeEntry, awayEntry, homeEntryRoundNetPoints, awayEntryRoundNetPoints);
        if (roundWinner != 0) {
            return roundWinner;
        }
        roundWinner = this.compareGoalsScored(homeEntry, awayEntry, homeEntryRoundGoalsScored, awayEntryRoundGoalsScored);
        if (roundWinner != 0) {
            return roundWinner;
        }
        roundWinner = this.compareGoalsConceded(homeEntry, awayEntry, homeEntryRoundGoalsConceded, awayEntryRoundGoalsConceded);
        if (roundWinner != 0) {
            return roundWinner;
        }
        return this.randomWinner(homeEntry, awayEntry);
    }

    private void setNextRoundData(Map<Integer, TournamentKnockoutNextRoundData> nextKnockoutMap, TournamentKnockoutEntity knockoutEntity) {
        TournamentKnockoutNextRoundData nextRoundData = nextKnockoutMap.getOrDefault(knockoutEntity.getNextMatchId(), new TournamentKnockoutNextRoundData());
        nextRoundData.setNextMatchId(knockoutEntity.getNextMatchId());
        nextRoundData.setNextRound(knockoutEntity.getRound() + 1);
        if (knockoutEntity.getMatchId() % 2 == 1) {
            nextRoundData.setNextRoundHomeEntry(knockoutEntity.getRoundWinner());
        } else {
            nextRoundData.setNextRoundAwayEntry(knockoutEntity.getRoundWinner());
        }
        nextKnockoutMap.put(nextRoundData.getNextMatchId(), nextRoundData);
    }

}
