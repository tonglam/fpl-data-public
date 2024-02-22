package com.tong.fpl.service.impl;

import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Maps;
import com.tong.fpl.constant.enums.Chip;
import com.tong.fpl.constant.enums.LeagueType;
import com.tong.fpl.domain.data.response.UserPicksRes;
import com.tong.fpl.domain.data.userpick.AutoSubs;
import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.domain.entity.EntryEventResultEntity;
import com.tong.fpl.domain.entity.EventLiveEntity;
import com.tong.fpl.domain.entity.LeagueEventReportEntity;
import com.tong.fpl.domain.entity.PlayerStatEntity;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.league.LeagueInfoData;
import com.tong.fpl.service.IInterfaceService;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.IReportService;
import com.tong.fpl.service.db.EntryEventResultService;
import com.tong.fpl.service.db.EventLiveService;
import com.tong.fpl.service.db.LeagueEventReportService;
import com.tong.fpl.service.db.PlayerStatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Create by tong on 2021/8/31
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReportServiceImpl implements IReportService {

    private final IQueryService queryService;
    private final IInterfaceService interfaceService;

    private final PlayerStatService playerStatService;
    private final EventLiveService eventLiveService;
    private final EntryEventResultService entryEventResultService;
    private final LeagueEventReportService leagueEventReportService;

    @Override
    public void insertLeagueEventPick(int event, int leagueId) {
        if (event < 1 || event > 38 || leagueId <= 0) {
            log.error("event:{}, params error", event);
            return;
        }
        // info
        LeagueInfoData data = this.getLeagueInfoId(leagueId);
        if (data == null) {
            log.error("event:{}, league_id:{}, tournament_info not exists", event, leagueId);
            return;
        }
        int tournamentId = data.getId();
        String leagueType = data.getLeagueType();
        int limit = data.getLimit();
        String name = limit == 0 ? data.getLeagueName() : data.getLeagueName() + "(top " + (int) NumberUtil.div(limit, 1000, 0, RoundingMode.FLOOR) + "k)";
        // get entry list
        List<Integer> entryList = this.getReportEntryList(tournamentId, data);
        if (CollectionUtils.isEmpty(entryList)) {
            log.info("event:{}, leagueId:{}, leagueType:{}, no need to insert", event, leagueId, leagueType);
            return;
        }
        // check
        int num = (int) this.leagueEventReportService.count(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getLeagueId, leagueId)
                .eq(LeagueEventReportEntity::getLeagueType, leagueType)
                .eq(LeagueEventReportEntity::getEvent, event));
        if (num == data.getTotalTeam()) {
            log.info("event:{}, leagueId:{}, leagueType:{}, no need to insert", event, leagueId, leagueType);
            return;
        }
        // init
        List<Integer> existsList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                        .eq(LeagueEventReportEntity::getLeagueId, leagueId)
                        .eq(LeagueEventReportEntity::getEvent, event)
                        .in(LeagueEventReportEntity::getEntry, entryList))
                .stream()
                .map(LeagueEventReportEntity::getEntry)
                .toList();
        entryList = entryList
                .stream()
                .filter(o -> !existsList.contains(o))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(entryList)) {
            log.info("event:{}, leagueId:{}, no need to insert", event, leagueId);
            return;
        }
        // get user picks
        List<CompletableFuture<LeagueEventReportEntity>> future = entryList
                .stream()
                .map(o -> CompletableFuture.supplyAsync(() -> this.initEntryEventSelectPick(event, o, leagueId, leagueType, name)))
                .toList();
        List<LeagueEventReportEntity> insertList = future
                .stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        // save
        this.leagueEventReportService.saveBatch(insertList);
        log.info("event:{}, leagueId:{}, leagueType:{}, insert league_event_report size:{}", event, leagueId, leagueType, insertList.size());
    }

    private LeagueInfoData getLeagueInfoId(int leagueId) {
        if (leagueId == 33) {
            return new LeagueInfoData()
                    .setLeagueId(33)
                    .setLeagueName("Australia")
                    .setLeagueType("Classic")
                    .setLimit(0)
                    .setTotalTeam(-1);
        } else if (leagueId == 65) {
            return new LeagueInfoData()
                    .setLeagueId(65)
                    .setLeagueName("China")
                    .setLeagueType("Classic")
                    .setLimit(0)
                    .setTotalTeam(-1);
        } else if (leagueId == 314) {
            return new LeagueInfoData()
                    .setLeagueId(314)
                    .setLeagueName("Overall")
                    .setLeagueType("Classic")
                    .setLimit(10000)
                    .setTotalTeam(-1);
        } else {
            return this.queryService.qryLeagueEventReportDataByLeagueId(leagueId);
        }
    }

    private List<Integer> getReportEntryList(int tournamentId, LeagueInfoData data) {
        int leagueId = data.getLeagueId();
        if (tournamentId == 0) {
            String leagueType = data.getLeagueType();
            int limit = data.getLimit();
            if (LeagueType.valueOf(leagueType).equals(LeagueType.Classic)) {
                return this.interfaceService.getEntryListFromClassicByLimit(leagueId, limit);
            } else if (LeagueType.valueOf(leagueType).equals(LeagueType.H2h)) {
                return this.interfaceService.getEntryListFromH2hByLimit(leagueId, limit);
            }
        } else {
            return this.queryService.qryEntryListByTournament(tournamentId);
        }
        return null;
    }

    private LeagueEventReportEntity initEntryEventSelectPick(int event, int entry, int leagueId, String
            leagueType, String leagueName) {
        List<Pick> picks = this.interfaceService.getUserPicks(event, entry).orElse(new UserPicksRes()).getPicks();
        if (CollectionUtils.isEmpty(picks)) {
            log.error("event:{}, entry:{}, get fpl server user_picks empty", event, entry);
            return null;
        }
        LeagueEventReportEntity leagueEventReportEntity = new LeagueEventReportEntity()
                .setLeagueId(leagueId)
                .setLeagueType(leagueType)
                .setLeagueName(leagueName)
                .setEntry(entry)
                .setEntryName("")
                .setPlayerName("")
                .setOverallPoints(0)
                .setOverallRank(0)
                .setTeamValue(0)
                .setBank(0)
                .setEvent(event)
                .setEventPoints(0)
                .setEventTransfers(0)
                .setEventTransfersCost(0)
                .setEventNetPoints(0)
                .setEventBenchPoints(0)
                .setEventAutoSubPoints(0)
                .setEventRank(0)
                .setEventChip("")
                .setPosition1(picks.get(0).getElement())
                .setPosition2(picks.get(1).getElement())
                .setPosition3(picks.get(2).getElement())
                .setPosition4(picks.get(3).getElement())
                .setPosition5(picks.get(4).getElement())
                .setPosition6(picks.get(5).getElement())
                .setPosition7(picks.get(6).getElement())
                .setPosition8(picks.get(7).getElement())
                .setPosition9(picks.get(8).getElement())
                .setPosition10(picks.get(9).getElement())
                .setPosition11(picks.get(10).getElement())
                .setPosition12(picks.get(11).getElement())
                .setPosition13(picks.get(12).getElement())
                .setPosition14(picks.get(13).getElement())
                .setPosition15(picks.get(14).getElement());
        // captain
        leagueEventReportEntity
                .setCaptain(picks
                        .stream()
                        .filter(Pick::isCaptain)
                        .map(Pick::getElement)
                        .findFirst()
                        .orElse(0)
                )
                .setCaptainPoints(0)
                .setCaptainBlank(true)
                .setCaptainSelected("");
        // vice captain
        leagueEventReportEntity
                .setViceCaptain(picks
                        .stream()
                        .filter(Pick::isViceCaptain)
                        .map(Pick::getElement)
                        .findFirst()
                        .orElse(0)
                )
                .setViceCaptainPoints(0)
                .setViceCaptainBlank(true)
                .setViceCaptainSelected("");
        // highest score
        leagueEventReportEntity
                .setHighestScore(0)
                .setHighestScorePoints(0)
                .setHighestScoreBlank(true)
                .setHighestScoreSelected("");
        // played captain
        leagueEventReportEntity.setPlayedCaptain(0);
        return leagueEventReportEntity;
    }

    @Override
    public void updateLeagueEventResult(int event, int leagueId) {
        if (event < 1 || event > 38 || leagueId <= 0) {
            log.error("event:{}, params error", event);
            return;
        }
        LeagueInfoData data = this.getLeagueInfoId(leagueId);
        if (data == null) {
            log.error("event:{}, tournament:{}, tournament_info not exists", event, leagueId);
            return;
        }
        String leagueType = data.getLeagueType();
        // league_event_stat
        List<LeagueEventReportEntity> leagueEventStatList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getLeagueId, leagueId)
                .eq(LeagueEventReportEntity::getLeagueType, leagueType)
                .eq(LeagueEventReportEntity::getEvent, event));
        if (CollectionUtils.isEmpty(leagueEventStatList)) {
            log.error("event:{}, leagueId:{}, leagueType:{}, league_event_report record not exists", event, leagueId, leagueType);
            return;
        }
        List<Integer> entryList = leagueEventStatList
                .stream()
                .map(LeagueEventReportEntity::getEntry)
                .collect(Collectors.toList());
        // prepare
        Map<Integer, PlayerStatEntity> playerStatMap = Maps.newHashMap();
        this.playerStatService.list(new QueryWrapper<PlayerStatEntity>().lambda()
                        .eq(PlayerStatEntity::getEvent, event))
                .forEach(o -> {
                    int element = o.getElement();
                    if (playerStatMap.containsKey(element)) {
                        PlayerStatEntity playerStatEntity = playerStatMap.get(element);
                        if (LocalDateTime.parse(playerStatEntity.getUpdateTime().replace(" ", "T"))
                                .isAfter(LocalDateTime.parse(o.getUpdateTime().replace(" ", "T")))) {
                            playerStatMap.put(element, playerStatEntity);
                        }
                    } else {
                        playerStatMap.put(element, o);
                    }
                });
        if (CollectionUtils.isEmpty(playerStatMap)) {
            log.error("event:{}, leagueId:{}, leagueType:{}, player_stat not exists", event, leagueId, leagueType);
            return;
        }
        Map<Integer, EventLiveEntity> eventLiveMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
                        .eq(EventLiveEntity::getEvent, event))
                .stream()
                .collect(Collectors.toMap(EventLiveEntity::getElement, o -> o));
        if (CollectionUtils.isEmpty(eventLiveMap)) {
            log.error("event:{}, leagueId:{}, leagueType:{}, event_live not exists", event, leagueId, leagueType);
            return;
        }
        Map<Integer, EntryEventResultEntity> entryEventResultMap = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                        .eq(EntryEventResultEntity::getEvent, event)
                        .in(EntryEventResultEntity::getEntry, entryList))
                .stream()
                .collect(Collectors.toMap(EntryEventResultEntity::getEntry, o -> o));
        if (leagueId != 314 && CollectionUtils.isEmpty(entryEventResultMap)) {
            log.error("event:{}, leagueId:{}, leagueType:{}, entry_event_result not exists", event, leagueId, leagueType);
            return;
        }
        // collect
        List<CompletableFuture<LeagueEventReportEntity>> future = leagueEventStatList
                .stream()
                .map(o -> CompletableFuture.supplyAsync(() -> this.updateEntryEventResultStat(event, o, playerStatMap, eventLiveMap, entryEventResultMap)))
                .toList();
        List<LeagueEventReportEntity> leagueEventStatEntityList = future
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        // update
        this.leagueEventReportService.updateBatchById(leagueEventStatEntityList);
        log.info("leagueId:{}, leagueType:{}, event:{}, update league_event_report size:{}!", leagueId, leagueType, event, leagueEventStatEntityList.size());
    }

    private LeagueEventReportEntity updateEntryEventResultStat(int event, LeagueEventReportEntity
            leagueEventStatEntity, Map<Integer, PlayerStatEntity> playerStatMap, Map<Integer, EventLiveEntity> eventLiveMap, Map<Integer, EntryEventResultEntity> entryEventResultMap) {
        int entry = leagueEventStatEntity.getEntry();
        // entry_info
        EntryInfoData entryInfoData = this.queryService.qryEntryInfo(entry);
        if (entryInfoData != null) {
            leagueEventStatEntity
                    .setEntryName(entryInfoData.getEntryName())
                    .setPlayerName(entryInfoData.getPlayerName());
        }
        // entry_event_result
        if (entryEventResultMap.containsKey(entry)) {
            EntryEventResultEntity entryEventResultEntity = entryEventResultMap.get(entry);
            leagueEventStatEntity
                    .setEventPoints(entryEventResultEntity.getEventPoints())
                    .setEventTransfers(entryEventResultEntity.getEventTransfers())
                    .setEventTransfersCost(entryEventResultEntity.getEventTransfersCost())
                    .setEventNetPoints(entryEventResultEntity.getEventNetPoints())
                    .setEventBenchPoints(entryEventResultEntity.getEventBenchPoints())
                    .setEventAutoSubPoints(entryEventResultEntity.getEventAutoSubPoints())
                    .setEventRank(entryEventResultEntity.getEventRank())
                    .setEventChip(entryEventResultEntity.getEventChip())
                    .setOverallPoints(entryEventResultEntity.getOverallPoints())
                    .setOverallRank(entryEventResultEntity.getOverallRank())
                    .setTeamValue(entryEventResultEntity.getTeamValue())
                    .setBank(entryEventResultEntity.getBank());
        } else {
            this.interfaceService.getUserPicks(event, entry).ifPresent(userPicksRes ->
                    leagueEventStatEntity
                            .setEventPoints(userPicksRes.getEntryHistory().getPoints())
                            .setEventTransfers(userPicksRes.getEntryHistory().getEventTransfers())
                            .setEventTransfersCost(userPicksRes.getEntryHistory().getEventTransfersCost())
                            .setEventNetPoints(userPicksRes.getEntryHistory().getPoints() - userPicksRes.getEntryHistory().getEventTransfersCost())
                            .setEventBenchPoints(userPicksRes.getEntryHistory().getPointsOnBench())
                            .setEventAutoSubPoints(userPicksRes.getAutomaticSubs().isEmpty() ? 0 : this.calcAutoSubPoints(userPicksRes.getAutomaticSubs(), eventLiveMap))
                            .setEventRank(userPicksRes.getEntryHistory().getRank())
                            .setEventChip(StringUtils.isBlank(userPicksRes.getActiveChip()) ? Chip.NONE.getValue() : userPicksRes.getActiveChip())
                            .setOverallPoints(userPicksRes.getEntryHistory().getTotalPoints())
                            .setOverallRank(userPicksRes.getEntryHistory().getOverallRank())
                            .setTeamValue(userPicksRes.getEntryHistory().getValue())
                            .setBank(userPicksRes.getEntryHistory().getBank()));
        }
        // captain
        int captain = leagueEventStatEntity.getCaptain();
        EventLiveEntity captainEventLiveEntity;
        if (eventLiveMap.containsKey(captain)) {
            captainEventLiveEntity = eventLiveMap.get(captain);
            leagueEventStatEntity
                    .setCaptainPoints(captainEventLiveEntity.getTotalPoints())
                    .setCaptainBlank(this.setElementBlank(captainEventLiveEntity))
                    .setCaptainSelected(playerStatMap.containsKey(captain) ? playerStatMap.get(captain).getSelectedByPercent() + "%" : "");
        } else {
            captainEventLiveEntity = new EventLiveEntity()
                    .setMinutes(0)
                    .setTotalPoints(0);
        }
        // vice captain
        int viceCaptain = leagueEventStatEntity.getViceCaptain();
        EventLiveEntity viceCaptainEventLiveEntity;
        if (eventLiveMap.containsKey(viceCaptain)) {
            viceCaptainEventLiveEntity = eventLiveMap.get(viceCaptain);
            leagueEventStatEntity
                    .setViceCaptainPoints(viceCaptainEventLiveEntity.getTotalPoints())
                    .setViceCaptainBlank(this.setElementBlank(viceCaptainEventLiveEntity))
                    .setViceCaptainSelected(playerStatMap.containsKey(viceCaptain) ? playerStatMap.get(viceCaptain).getSelectedByPercent() + "%" : "");
        } else {
            viceCaptainEventLiveEntity = new EventLiveEntity()
                    .setMinutes(0)
                    .setTotalPoints(0);
        }
        leagueEventStatEntity.setPlayedCaptain(this.selectPlayedCaptain(captainEventLiveEntity, viceCaptainEventLiveEntity));
        // highest score
        int highestElement = this.getHighestScoreElement(leagueEventStatEntity, eventLiveMap);
        if (eventLiveMap.containsKey(highestElement)) {
            EventLiveEntity highestEventLiveEntity = eventLiveMap.get(highestElement);
            leagueEventStatEntity
                    .setHighestScore(highestElement)
                    .setHighestScorePoints(highestEventLiveEntity.getTotalPoints())
                    .setHighestScoreBlank(this.setElementBlank(highestEventLiveEntity))
                    .setHighestScoreSelected(playerStatMap.containsKey(highestElement) ? playerStatMap.get(highestElement).getSelectedByPercent() + "%" : "");
        }
        // played captain
        if (leagueEventStatEntity.getPlayedCaptain() == 0) {
            leagueEventStatEntity.setPlayedCaptain(leagueEventStatEntity.getCaptain());
        }
        return leagueEventStatEntity;
    }

    private int selectPlayedCaptain(EventLiveEntity captainEventLiveEntity, EventLiveEntity
            viceCaptainEventLiveEntity) {
        if (captainEventLiveEntity.getMinutes() == 0 && viceCaptainEventLiveEntity.getMinutes() > 0) {
            return viceCaptainEventLiveEntity.getElement();
        }
        return captainEventLiveEntity.getElement();
    }

    private int calcAutoSubPoints
            (List<AutoSubs> automaticSubs, Map<Integer, EventLiveEntity> eventLiveMap) {
        return automaticSubs
                .stream()
                .mapToInt(o -> eventLiveMap.containsKey(o.getElementIn()) ? eventLiveMap.get(o.getElementIn()).getTotalPoints() : 0)
                .sum();
    }

    private int getHighestScoreElement(LeagueEventReportEntity
                                               leagueEventStatEntity, Map<Integer, EventLiveEntity> eventLiveMap) {
        Map<Integer, Integer> elementPointsMap = Maps.newHashMap();
        elementPointsMap.put(leagueEventStatEntity.getPosition1(), this.getElementEventPoints(leagueEventStatEntity.getPosition1(), eventLiveMap));
        elementPointsMap.put(leagueEventStatEntity.getPosition2(), this.getElementEventPoints(leagueEventStatEntity.getPosition2(), eventLiveMap));
        elementPointsMap.put(leagueEventStatEntity.getPosition3(), this.getElementEventPoints(leagueEventStatEntity.getPosition3(), eventLiveMap));
        elementPointsMap.put(leagueEventStatEntity.getPosition4(), this.getElementEventPoints(leagueEventStatEntity.getPosition4(), eventLiveMap));
        elementPointsMap.put(leagueEventStatEntity.getPosition5(), this.getElementEventPoints(leagueEventStatEntity.getPosition5(), eventLiveMap));
        elementPointsMap.put(leagueEventStatEntity.getPosition6(), this.getElementEventPoints(leagueEventStatEntity.getPosition6(), eventLiveMap));
        elementPointsMap.put(leagueEventStatEntity.getPosition7(), this.getElementEventPoints(leagueEventStatEntity.getPosition7(), eventLiveMap));
        elementPointsMap.put(leagueEventStatEntity.getPosition8(), this.getElementEventPoints(leagueEventStatEntity.getPosition8(), eventLiveMap));
        elementPointsMap.put(leagueEventStatEntity.getPosition9(), this.getElementEventPoints(leagueEventStatEntity.getPosition9(), eventLiveMap));
        elementPointsMap.put(leagueEventStatEntity.getPosition10(), this.getElementEventPoints(leagueEventStatEntity.getPosition10(), eventLiveMap));
        elementPointsMap.put(leagueEventStatEntity.getPosition11(), this.getElementEventPoints(leagueEventStatEntity.getPosition11(), eventLiveMap));
        elementPointsMap.put(leagueEventStatEntity.getPosition12(), this.getElementEventPoints(leagueEventStatEntity.getPosition12(), eventLiveMap));
        elementPointsMap.put(leagueEventStatEntity.getPosition13(), this.getElementEventPoints(leagueEventStatEntity.getPosition13(), eventLiveMap));
        elementPointsMap.put(leagueEventStatEntity.getPosition14(), this.getElementEventPoints(leagueEventStatEntity.getPosition14(), eventLiveMap));
        elementPointsMap.put(leagueEventStatEntity.getPosition15(), this.getElementEventPoints(leagueEventStatEntity.getPosition15(), eventLiveMap));
        // sort
        return elementPointsMap.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0);
    }

    private int getElementEventPoints(int element, Map<Integer, EventLiveEntity> eventLiveMap) {
        if (eventLiveMap.containsKey(element)) {
            return eventLiveMap.get(element).getTotalPoints();
        }
        return 0;
    }

    private boolean setElementBlank(EventLiveEntity eventLiveEntity) {
        return eventLiveEntity.getGoalsScored() <= 0 &&
                eventLiveEntity.getAssists() <= 0 &&
                eventLiveEntity.getBonus() <= 0 &&
                eventLiveEntity.getPenaltiesSaved() <= 0 &&
                eventLiveEntity.getSaves() <= 3 &&
                ((eventLiveEntity.getElementType() != 1 && eventLiveEntity.getElementType() != 2) || eventLiveEntity.getCleanSheets() <= 0);
    }

}
