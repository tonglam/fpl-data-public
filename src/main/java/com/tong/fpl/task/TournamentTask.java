package com.tong.fpl.task;

import com.tong.fpl.service.IEventUpdateService;
import com.tong.fpl.service.IQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Create by tong on 2021/9/2
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TournamentTask {

    private final IQueryService queryService;
    private final IEventUpdateService eventUpdateService;

    @Async("TaskThreadPool")
    @Scheduled(cron = "0 0/5 0-4,18-23 * * *")
    public void insertAllTournamentEventPick() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isSelectTime(event)) {
            return;
        }
        this.eventUpdateService.insertAllTournamentEventPick(event);
    }

    @Async("TaskThreadPool")
    @Scheduled(cron = "0 0/5 0-4,18-23 * * *")
    public void insertAllTournamentEventTransfers() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isSelectTime(event)) {
            return;
        }
        this.eventUpdateService.insertAllTournamentEventTransfers(event);
    }

    @Async("TaskThreadPool")
    @Scheduled(cron = "0 10 6,8,10 * * *")
    public void upsertAllTournamentEventResult() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isAfterMatchDay(event)) {
            return;
        }
        try {
            this.eventUpdateService.upsertAllTournamentEventResult(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async("TaskThreadPool")
    @Scheduled(cron = "0 20 6,8,10 * * *")
    public void updateAllPointsRaceGroupResult() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isAfterMatchDay(event)) {
            return;
        }
        try {
            this.eventUpdateService.updateAllPointsRaceGroupResult(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async("TaskThreadPool")
    @Scheduled(cron = "0 30 6,8,10 * * *")
    public void updateAllBattleRaceGroupResult() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isAfterMatchDay(event)) {
            return;
        }
        try {
            this.eventUpdateService.updateAllBattleRaceGroupResult(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async("TaskThreadPool")
    @Scheduled(cron = "0 40 6,8,10 * * *")
    public void updateAllKnockoutResult() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isAfterMatchDay(event)) {
            return;
        }
        try {
            this.eventUpdateService.updateAllKnockoutResult(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async("TaskThreadPool")
    @Scheduled(cron = "0 45 6,8,10 * * *")
    public void updateAllTournamentEventTransfers() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isAfterMatchDay(event)) {
            return;
        }
        this.eventUpdateService.updateAllTournamentEventTransfers(event);
    }

    @Async("TaskThreadPool")
    @Scheduled(cron = "0 55 6,8,10 * * *")
    public void upsertAllTournamentEventCupResult() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isAfterMatchDay(event)) {
            return;
        }
        this.eventUpdateService.upsertAllTournamentEventCupResult(event);
    }

}
