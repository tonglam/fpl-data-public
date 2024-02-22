package com.tong.fpl.service.impl;

import com.google.common.collect.Lists;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.constant.enums.LeagueType;
import com.tong.fpl.domain.entity.PlayerValueEntity;
import com.tong.fpl.domain.entity.TournamentInfoEntity;
import com.tong.fpl.service.*;
import com.tong.fpl.service.db.TournamentInfoService;
import com.tong.fpl.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * Create by tong on 2021/9/2
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EventUpdateServiceImpl implements IEventUpdateService {

    private final IQueryService queryService;
    private final IRedisCacheService redisCacheService;
    private final IInterfaceService interfaceService;
    private final IDataService dataService;
    private final ITournamentService tournamentService;
    private final IReportService reportService;

    private final TournamentInfoService tournamentInfoService;

    /**
     * @implNote daily
     */
    @Override
    public void updateEvent() {
        int event = this.queryService.getCurrentEvent();
        if (event < 0 || event > 38) {
            return;
        }
        this.interfaceService.getBootstrapStatic().ifPresent(this.redisCacheService::insertEvent);
        log.info("event:{}, insert event success", event);
        this.redisCacheService.insertEventFixture();
        log.info("event:{}, insert event_fixture success", event);
    }

    @Override
    public void updatePlayerValue() {
        int event = this.queryService.getCurrentEvent();
        if (event < 0 || event > 38) {
            return;
        }
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern(Constant.SHORTDAY));
        String key = StringUtils.joinWith("::", PlayerValueEntity.class, date);
        if (Objects.equals(RedisUtils.hasKey(key), Boolean.TRUE)) {
            log.info("date:{}, no need to insert player value", date);
            return;
        }
        this.interfaceService.getBootstrapStatic().ifPresent(staticRes -> {
            this.redisCacheService.insertPlayer(staticRes);
            log.info("event:{}, insert player success", event);
            this.redisCacheService.insertPlayerValue(staticRes);
            log.info("event:{}, insert player_value success", event);
        });
    }

    @Override
    public void updatePlayerStat() {
        int event = this.queryService.getCurrentEvent();
        if (event < 0 || event > 38) {
            return;
        }
        this.interfaceService.getBootstrapStatic().ifPresent(this.redisCacheService::insertPlayerStat);
        log.info("event:{}, insert player_stat success", event);
    }

    @Override
    public void updateAllEntryInfo() {
        this.dataService.upsertEntryInfoByList(this.queryService.qryAllEntryList());
        log.info("update all entry_info success");
    }

    /**
     * @implNote matchDay
     */
    @Override
    public void updateEventLiveCache(int event) {
        if (event < 1 || event > 38) {
            return;
        }
        this.interfaceService.getEventFixture(event).ifPresent(res -> this.redisCacheService.insertSingleEventFixtureCache(event, res));
        log.info("event:{}, insert single event_fixture_cache success", event);
        this.redisCacheService.insertLiveFixtureCache();
        log.info("event:{}, insert live_event_fixture_cache success", event);
        this.redisCacheService.insertLiveBonusCache();
        log.info("event:{}, insert live_bonus_cache success", event);
        this.interfaceService.getEventLive(event).ifPresent(res -> this.redisCacheService.insertEventLiveCache(event, res));
        log.info("event:{}, insert event_live_cache success", event);
    }

    @Override
    public void updateEventLive(int event) {
        if (event < 1 || event > 38) {
            return;
        }
        this.interfaceService.getEventFixture(event).ifPresent(res -> this.redisCacheService.insertSingleEventFixture(event, res));
        log.info("event:{}, insert single event_fixture success", event);
        this.redisCacheService.insertLiveFixtureCache();
        log.info("event:{}, insert live_event_fixture_cache success", event);
        this.redisCacheService.insertLiveBonusCache();
        log.info("event:{}, insert live_bonus_cache success", event);
        this.interfaceService.getEventLive(event).ifPresent(res -> this.redisCacheService.insertEventLive(event, res));
        log.info("event:{}, insert event_live success", event);
    }

    @Override
    public void updateEventLiveSummary() {
        int event = this.queryService.getCurrentEvent();
        if (event < 1 || event > 38) {
            return;
        }
        this.redisCacheService.insertEventLiveSummary();
        log.info("event:{}, update event_live_summary success", event);
    }

    @Override
    public void updateEventLiveExplain() {
        int event = this.queryService.getCurrentEvent();
        if (event < 1 || event > 38) {
            return;
        }
        this.interfaceService.getEventLive(event).ifPresent(eventLiveRes -> {
            this.redisCacheService.insertEventLiveExplain(event, eventLiveRes);
            log.info("event:{}, update event_live_explain success", event);
        });
    }

    @Override
    public void upsertEventOverallResult() {
        int event = this.queryService.getCurrentEvent();
        if (event < 1 || event > 38) {
            return;
        }
        this.interfaceService.getBootstrapStatic().ifPresent(this.redisCacheService::insertEventOverallResult);
        log.info("event:{}, insert event overall result success", event);
    }

    /**
     * @implNote tournament
     */
    @Override
    public void updateTournamentInfo() {
        List<TournamentInfoEntity> tournamentInfoEntityList = this.tournamentInfoService.list();
        if (CollectionUtils.isEmpty(tournamentInfoEntityList)) {
            return;
        }
        List<TournamentInfoEntity> updateList = Lists.newArrayList();
        tournamentInfoEntityList.forEach(o -> {
            int leagueId = o.getLeagueId();
            String leagueType = o.getLeagueType();
            if (StringUtils.equals(LeagueType.H2h.name(), leagueType)) {
                this.interfaceService.getLeagueH2H(leagueId, 1)
                        .ifPresent(leagueClassic -> {
                            String name = leagueClassic.getLeague().getName();
                            if (!StringUtils.equalsIgnoreCase(name, o.getName())) {
                                o.setName(name);
                                updateList.add(o);
                            }
                        });
            } else {
                this.interfaceService.getLeaguesClassic(leagueId, 1)
                        .ifPresent(leagueClassic -> {
                            String name = leagueClassic.getLeague().getName();
                            if (!StringUtils.equalsIgnoreCase(name, o.getName())) {
                                o.setName(name);
                                updateList.add(o);
                            }
                        });
            }
        });
        this.tournamentInfoService.updateBatchById(updateList);
        log.info("update tournament_info size:{}", updateList.size());
    }

    @Override
    public void insertAllTournamentEventPick(int event) {
        this.queryService.qryAllTournamentList().forEach(tournamentId -> {
            try {
                // get cache
                String cacheKey = "insertTournamentEventTransfers";
                List<Integer> finishedList = RedisUtils.readHashValue(cacheKey, event);
                if (!CollectionUtils.isEmpty(finishedList) && finishedList.contains(tournamentId)) {
                    return;
                }
                // run
                log.info("event:{}, tournament:{}, start insert tournament entry_event_pick", event, tournamentId);
                this.dataService.insertEventPickByEntryList(event, this.queryService.qryEntryListByTournament(tournamentId));
                log.info("event:{}, tournament:{}, end insert tournament entry_event_pick", event, tournamentId);
            } catch (Exception e) {
                log.error("event:{}, tournament:{}, insert tournament entry_event_pick error:{}", event, tournamentId, e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public void insertAllTournamentEventTransfers(int event) {
        this.queryService.qryAllTournamentList().forEach(tournamentId -> {
            try {
                // get cache
                String cacheKey = "insertTournamentEventTransfers";
                List<Integer> finishedList = RedisUtils.readHashValue(cacheKey, event);
                if (!CollectionUtils.isEmpty(finishedList) && finishedList.contains(tournamentId)) {
                    return;
                }
                // run
                log.info("event:{}, tournament:{}, start insert tournament entry_event_transfers", event, tournamentId);
                this.dataService.insertEventTransfersByEntryList(event, this.queryService.qryEntryListByTournament(tournamentId));
                log.info("event:{}, tournament:{}, end insert tournament entry_event_transfers", event, tournamentId);
                // set cache
                if (CollectionUtils.isEmpty(finishedList)) {
                    finishedList = Lists.newArrayList();
                }
                finishedList.add(tournamentId);
                RedisUtils.setHashValue(cacheKey, event, finishedList);
            } catch (Exception e) {
                log.error("event:{}, tournament:{}, insert tournament entry_event_transfers error:{}", event, tournamentId, e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public void upsertAllTournamentEventResult(int event) {
        this.queryService.qryAllTournamentList().forEach(tournamentId -> {
            try {
                log.info("event:{}, tournament:{}, start upsert tournament entry_event_result", event, tournamentId);
                this.tournamentService.upsertTournamentEventResult(event, tournamentId);
                log.info("event:{}, tournament:{}, end upsert tournament entry_event_result", event, tournamentId);
            } catch (Exception e) {
                log.error("event:{}, tournament:{}, upsert tournament entry_event_result error:{}", event, tournamentId, e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public void updateAllPointsRaceGroupResult(int event) {
        this.queryService.qryPointsRaceGroupTournamentList(event).forEach(tournamentId -> {
            try {
                log.info("event:{}, tournament:{}, start update tournament_points_race_group_result", event, tournamentId);
                this.tournamentService.updatePointsRaceGroupResult(event, tournamentId);
                log.info("event:{}, tournament:{}, end update tournament_points_race_group_result", event, tournamentId);
            } catch (Exception e) {
                log.error("event:{}, tournament:{}, update tournament_points_race_group_result error:{}", event, tournamentId, e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public void updateAllBattleRaceGroupResult(int event) {
        this.queryService.qryBattleRaceGroupTournamentList(event).forEach(tournamentId -> {
            try {
                log.info("event:{}, tournament:{}, start update tournament_battle_race_group_result", event, tournamentId);
                this.tournamentService.updateBattleRaceGroupResult(event, tournamentId);
                log.info("event:{}, tournament:{}, end update tournament_battle_race_group_result", event, tournamentId);
            } catch (Exception e) {
                log.error("event:{}, tournament:{}, update tournament_battle_race_group_result error:{}", event, tournamentId, e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public void updateAllKnockoutResult(int event) {
        this.queryService.qryKnockoutTournamentList(event).forEach(tournamentId -> {
            try {
                log.info("event:{}, tournament:{}, start update tournament_knockout_result", event, tournamentId);
                this.tournamentService.updateKnockoutResult(event, tournamentId);
                log.info("event:{}, tournament:{}, end update tournament_knockout_result", event, tournamentId);
            } catch (Exception e) {
                log.info("event:{}, tournament:{}, update tournament_knockout_result error:{}", event, tournamentId, e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public void updateAllTournamentEventTransfers(int event) {
        this.queryService.qryAllTournamentList().forEach(tournamentId -> {
            try {
                log.info("event:{}, tournament:{}, start update tournament entry_event_transfers", event, tournamentId);
                this.dataService.updateEventTransfersByEntryList(event, this.queryService.qryEntryListByTournament(tournamentId));
                log.info("event:{}, tournament:{}, end update tournament entry_event_transfers", event, tournamentId);
            } catch (Exception e) {
                log.error("event:{}, tournament:{}, update tournament entry_event_transfers error:{}", event, tournamentId, e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public void upsertAllTournamentEventCupResult(int event) {
        this.queryService.qryAllTournamentList().forEach(tournamentId -> {
            try {
                log.info("event:{}, tournament:{}, start update tournament entry_event_cup_result", event, tournamentId);
                this.dataService.upsertEventCupResultByEntryList(event, this.queryService.qryEntryListByTournament(tournamentId));
                log.info("event:{}, tournament:{}, end update tournament entry_event_cup_result", event, tournamentId);
            } catch (Exception e) {
                log.error("event:{}, tournament:{}, update tournament entry_event_cup_result error:{}", event, tournamentId, e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * @implNote report
     */
    @Override
    public void insertAllLeagueEventPick(int event) {
        this.queryService.qryAllReportLeagueDataList().forEach(o -> {
            int leagueId = o.getLeagueId();
            String leagueType = o.getLeagueType();
            try {
                log.info("event:{}, league_id:{}, league_type:{}, start insert league event pick", event, leagueId, leagueType);
                this.reportService.insertLeagueEventPick(event, leagueId);
                log.info("event:{}, league_id:{}, league_type:{}, end insert league event pick", event, leagueId, leagueType);
            } catch (Exception e) {
                log.error("event:{}, league_id:{}, league_type:{}, insert league event pick, error:{}", event, leagueId, leagueType, e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public void updateAllLeagueEventResult(int event) {
        this.queryService.qryAllReportLeagueDataList().forEach(o -> {
            int leagueId = o.getLeagueId();
            String leagueType = o.getLeagueType();
            try {
                log.info("event:{}, league_id:{}, league_type:{}, start update league event result", event, leagueId, leagueType);
                this.reportService.updateLeagueEventResult(event, leagueId);
                log.info("event:{}, league_id:{}, league_type:{}, end update league event result", event, leagueId, leagueType);
            } catch (Exception e) {
                log.error("event:{}, league_id:{}, league_type:{}, update league event result, error:{}", event, leagueId, leagueType, e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * @implNote scout
     */
    @Override
    public void updateAllEventSourceScoutResult(int event) {
        this.dataService.updateAllEventSourceScoutResult(event);
    }

}
