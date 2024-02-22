package com.tong.fpl.task;

import com.tong.fpl.service.IEventUpdateService;
import com.tong.fpl.service.IQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Create by tong on 2021/8/30
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MatchDayTask {

    private final IQueryService queryService;
    private final IEventUpdateService eventUpdateService;

    @Async("TaskThreadPool")
    @Scheduled(cron = "0 */1 0-7,19-23 * * *")
    public void updateEventLiveCache() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isMatchDayTime(event)) {
            return;
        }
        this.eventUpdateService.updateEventLiveCache(event);
    }

    @Async("TaskThreadPool")
    @Scheduled(cron = "0 0 */1 * * *")
    public void updateMatchDayTimeEventLiveData() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isMatchDayTime(event)) {
            return;
        }
        this.eventUpdateService.updateEventLive(event);
    }

    @Async("TaskThreadPool")
    @Scheduled(cron = "0 0 6,8,10 * * *")
    public void updateMatchDayEventLiveData() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isMatchDay(event)) {
            return;
        }
        this.eventUpdateService.updateEventLive(event);
    }

    @Async("TaskThreadPool")
    @Scheduled(cron = "0 2 6,8,10 * * *")
    public void insertEventOverallResult() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isMatchDay(event)) {
            return;
        }
        this.eventUpdateService.upsertEventOverallResult();
    }

    @Async("TaskThreadPool")
    @Scheduled(cron = "0 5 6,8,10 * * *")
    public void updateEventLiveSummary() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isMatchDay(event)) {
            return;
        }
        this.eventUpdateService.updateEventLiveSummary();
    }

    @Async("TaskThreadPool")
    @Scheduled(cron = "0 8 6,8,10 * * *")
    public void updateEventLiveExplain() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isMatchDay(event)) {
            return;
        }
        this.eventUpdateService.updateEventLiveExplain();
    }

    @Async("TaskThreadPool")
    @Scheduled(cron = "0 10 6,8,10 * * *")
    public void updateEventSourceScoutResult() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isMatchDayTime(event)) {
            return;
        }
        this.eventUpdateService.updateAllEventSourceScoutResult(event);
    }

}
