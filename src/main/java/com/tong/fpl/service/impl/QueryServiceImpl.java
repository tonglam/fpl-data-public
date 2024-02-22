package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.tong.fpl.config.mp.MybatisPlusConfig;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.constant.enums.GroupMode;
import com.tong.fpl.constant.enums.KnockoutMode;
import com.tong.fpl.domain.data.response.EntryRes;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.league.LeagueInfoData;
import com.tong.fpl.service.IInterfaceService;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.IRedisCacheService;
import com.tong.fpl.service.db.EntryInfoService;
import com.tong.fpl.service.db.TournamentEntryService;
import com.tong.fpl.service.db.TournamentInfoService;
import com.tong.fpl.service.db.TournamentKnockoutResultService;
import com.tong.fpl.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Create by tong on 2021/8/31
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class QueryServiceImpl implements IQueryService {

    private final IRedisCacheService redisCacheService;
    private final IInterfaceService interfaceService;

    private final EntryInfoService entryInfoService;
    private final TournamentInfoService tournamentInfoService;
    private final TournamentEntryService tournamentEntryService;
    private final TournamentKnockoutResultService tournamentKnockoutResultService;

    /**
     * @implNote time
     */
    @Override
    public boolean isMatchDay(int event) {
        return this.getMatchDayByEvent(event).contains(LocalDate.now());
    }

    @Override
    public boolean isAfterMatchDay(int event) {
        return this.getAfterMatchDayByEvent(event).contains(LocalDate.now());
    }

    @Override
    public boolean isMatchDayTime(int event) {
        List<LocalDateTime> matchDayTimeList = this.getMatchDayTimeByEvent(event);
        LocalDateTime start = matchDayTimeList
                .stream()
                .min(LocalDateTime::compareTo)
                .orElse(null);
        LocalDateTime last = matchDayTimeList
                .stream()
                .max(LocalDateTime::compareTo)
                .orElse(null);
        if (start == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(start) && LocalDateTime.now().minusHours(2).isBefore(last);
    }

    @Override
    public boolean isSelectTime(int event) {
        return this.isMatchDay(event) && LocalDateTime.now().isAfter(LocalDateTime.parse(this.getDeadlineByEvent(event).replace(" ", "T")).plusMinutes(30));
    }

    /**
     * @implNote event
     */
    @Override
    public String getDeadlineByEvent(String season, int event) {
        return this.redisCacheService.getDeadlineByEvent(season, event);
    }

    @Cacheable(
            value = "getCurrentEvent",
            unless = "#result eq 0"
    )
    @Override
    public int getCurrentEvent() {
        int event = 0;
        for (int i = 1; i < 39; i++) {
            String deadline = this.getDeadlineByEvent(i);
            if (LocalDateTime.now().isAfter(LocalDateTime.parse(deadline, DateTimeFormatter.ofPattern(Constant.DATETIME)))) {
                event = i;
            } else {
                break;
            }
        }
        return event;
    }

    @Override
    public int getLastEvent() {
        return this.getCurrentEvent() - 1;
    }

    // do not cache
    @Override
    public List<LocalDate> getMatchDayByEvent(int event) {
        List<LocalDate> matchDayList = Lists.newArrayList();
        this.getEventFixtureByEvent(event)
                .forEach(eventFixtureEntity -> {
                            String matchDay = StringUtils.substringBefore(eventFixtureEntity.getKickoffTime(), " ");
                            LocalDate date = LocalDate.parse(matchDay);
                            if (!matchDayList.contains(date)) {
                                matchDayList.add(date);
                            }
                        }
                );
        return matchDayList
                .stream()
                .sorted(LocalDate::compareTo)
                .collect(Collectors.toList());
    }

    @Override
    public List<LocalDate> getAfterMatchDayByEvent(int event) {
        List<LocalDate> afterDateList = Lists.newArrayList();
        this.getMatchDayTimeByEvent(event).forEach(localDateTime -> {
            LocalDate localDate = localDateTime.toLocalDate();
            LocalDateTime baseTime = localDate.atTime(6, 0);
            if (!localDateTime.isBefore(baseTime)) {
                localDate = localDate.plusDays(1);
            }
            if (!afterDateList.contains(localDate)) {
                afterDateList.add(localDate);
            }
        });
        return afterDateList;
    }

    // do not cache
    @Override
    public List<LocalDateTime> getMatchDayTimeByEvent(int event) {
        List<LocalDateTime> matchDayTimeList = Lists.newArrayList();
        this.getEventFixtureByEvent(event)
                .forEach(eventFixtureEntity -> {
                            String kickoffTime = eventFixtureEntity.getKickoffTime().replace(" ", "T");
                            LocalDateTime dateTime = LocalDateTime.parse(kickoffTime);
                            if (!matchDayTimeList.contains(dateTime)) {
                                matchDayTimeList.add(dateTime);
                            }
                        }
                );
        return matchDayTimeList
                .stream()
                .sorted(LocalDateTime::compareTo)
                .collect(Collectors.toList());
    }

    /**
     * @implNote fixture
     */
    @Cacheable(
            value = "getEventFixtureByEvent",
            key = "#season+'::'+#event",
            unless = "#result.size() eq 0"
    )
    @Override
    public List<EventFixtureEntity> getEventFixtureByEvent(String season, int event) {
        return this.redisCacheService.getEventFixtureByEvent(season, event);
    }

    /**
     * @implNote entry
     */
    @Cacheable(
            value = "qryAllEntryList",
            unless = "#result.size() eq 0"
    )
    @Override
    public List<Integer> qryAllEntryList() {
        return this.entryInfoService.list()
                .stream()
                .map(EntryInfoEntity::getEntry)
                .filter(o -> o > 0)
                .distinct()
                .collect(Collectors.toList());
    }

    @Cacheable(
            value = "qryEntryInfo",
            key = "#season+'::'+#entry",
            unless = "#result.entry eq 0"
    )
    @Override
    public EntryInfoData qryEntryInfo(String season, int entry) {
        if (entry <= 0) {
            return new EntryInfoData();
        }
        MybatisPlusConfig.season.set(season);
        EntryInfoEntity entryInfoEntity = this.entryInfoService.getById(entry);
        MybatisPlusConfig.season.remove();
        if (entryInfoEntity != null) {
            return BeanUtil.copyProperties(entryInfoEntity, EntryInfoData.class);
        }
        if (!StringUtils.equals(CommonUtils.getCurrentSeason(), season)) {
            return new EntryInfoData();
        }
        EntryRes entryRes = this.interfaceService.getEntry(entry).orElse(null);
        if (entryRes == null) {
            return new EntryInfoData();
        }
        entryInfoEntity = new EntryInfoEntity()
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
        return BeanUtil.copyProperties(entryInfoEntity, EntryInfoData.class);
    }

    /**
     * @implNote tournament
     */
    @Cacheable(
            value = "qryTournamentInfoById",
            key = "#tournamentId",
            unless = "#result.id eq 0"
    )
    @Override
    public TournamentInfoEntity qryTournamentInfoById(int tournamentId) {
        return this.tournamentInfoService.getOne(new QueryWrapper<TournamentInfoEntity>().lambda()
                .eq(TournamentInfoEntity::getId, tournamentId)
                .eq(TournamentInfoEntity::getState, 1));
    }

    @Cacheable(
            value = "qryEntryListByTournament",
            key = "#tournamentId",
            unless = "#result.size() eq 0"
    )
    @Override
    public List<Integer> qryEntryListByTournament(int tournamentId) {
        return this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda()
                        .eq(TournamentEntryEntity::getTournamentId, tournamentId))
                .stream()
                .map(TournamentEntryEntity::getEntry)
                .filter(o -> o > 0)
                .collect(Collectors.toList());
    }

    @Cacheable(
            value = "qryAllTournamentList",
            unless = "#result.size() eq 0"
    )
    @Override
    public List<TournamentInfoEntity> qryAllTournamentInfoList() {
        return this.tournamentInfoService.list(new QueryWrapper<TournamentInfoEntity>().lambda()
                .eq(TournamentInfoEntity::getState, 1));
    }

    @Override
    public List<Integer> qryAllTournamentList() {
        return this.qryAllTournamentInfoList()
                .stream()
                .map(TournamentInfoEntity::getId)
                .collect(Collectors.toList());
    }

    @Cacheable(
            value = "qryPointsRaceGroupTournamentList",
            key = "#event",
            unless = "#result.size() eq 0"
    )
    @Override
    public List<Integer> qryPointsRaceGroupTournamentList(int event) {
        return this.qryAllTournamentInfoList()
                .stream()
                .filter(o -> StringUtils.equals(o.getGroupMode(), GroupMode.Points_race.name()))
                .filter(o -> o.getGroupStartGw() <= event && o.getGroupEndGw() >= event)
                .map(TournamentInfoEntity::getId)
                .distinct()
                .collect(Collectors.toList());
    }

    @Cacheable(
            value = "qryBattleRaceGroupTournamentList",
            key = "#event",
            unless = "#result.size() eq 0"
    )
    @Override
    public List<Integer> qryBattleRaceGroupTournamentList(int event) {
        return this.qryAllTournamentInfoList()
                .stream()
                .filter(o -> StringUtils.equals(o.getGroupMode(), GroupMode.Battle_race.name()))
                .filter(o -> o.getGroupStartGw() <= event && o.getGroupEndGw() >= event)
                .map(TournamentInfoEntity::getId)
                .distinct()
                .collect(Collectors.toList());
    }

    @Cacheable(
            value = "qryKnockoutTournamentList",
            key = "#event",
            unless = "#result.size() eq 0"
    )
    @Override
    public List<Integer> qryKnockoutTournamentList(int event) {
        return this.qryAllTournamentInfoList()
                .stream()
                .filter(o -> !StringUtils.equals(o.getKnockoutMode(), KnockoutMode.No_knockout.name()))
                .filter(o -> o.getKnockoutStartGw() <= event && o.getKnockoutEndGw() >= event)
                .map(TournamentInfoEntity::getId)
                .distinct()
                .collect(Collectors.toList());
    }

    @Cacheable(
            value = "qryKnockoutResultTournamentList",
            key = "#tournamentId+'::'+#matchId",
            unless = "#result.size() eq 0"
    )
    @Override
    public List<TournamentKnockoutResultEntity> qryKnockoutResultTournamentList(int tournamentId, int matchId) {
        return this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda()
                .eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId)
                .eq(TournamentKnockoutResultEntity::getMatchId, matchId));
    }

    /**
     * @implNote report
     */
    @Cacheable(
            value = "qryLeagueEventReportDataByLeagueId",
            key = "#leagueId",
            unless = "#result.leagueId eq 0"
    )
    @Override
    public LeagueInfoData qryLeagueEventReportDataByLeagueId(int leagueId) {
        TournamentInfoEntity tournamentInfoEntity = this.tournamentInfoService.list(new QueryWrapper<TournamentInfoEntity>().lambda()
                        .eq(TournamentInfoEntity::getLeagueId, leagueId)
                        .orderByAsc(TournamentInfoEntity::getCreateTime))
                .stream()
                .findFirst()
                .orElse(null);
        if (tournamentInfoEntity == null) {
            return new LeagueInfoData();
        }
        return new LeagueInfoData()
                .setId(tournamentInfoEntity.getId())
                .setLeagueId(tournamentInfoEntity.getLeagueId())
                .setLeagueType(tournamentInfoEntity.getLeagueType())
                .setLeagueName(tournamentInfoEntity.getName())
                .setLimit(0)
                .setTotalTeam(tournamentInfoEntity.getTotalTeam());
    }

    @Cacheable(value = "qryAllReportLeagueDataList")
    @Override
    public List<LeagueInfoData> qryAllReportLeagueDataList() {
        return this.tournamentInfoService.list()
                .stream()
                .distinct()
                .map(o ->
                        new LeagueInfoData()
                                .setId(o.getId())
                                .setLeagueId(o.getLeagueId())
                                .setLeagueType(o.getLeagueType())
                                .setLeagueName(o.getName())
                                .setLimit(0)
                )
                .collect(Collectors.toList());
    }

}
