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
 * Create by tong on 2021/8/31
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReportTask {

    private final IQueryService queryService;
    private final IEventUpdateService eventUpdateService;

    @Async("TaskThreadPool")
    @Scheduled(cron = "0 0/5 0-4,18-23 * * *")
    public void insertLeagueEventPick() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isSelectTime(event)) {
            return;
        }
        try {
            this.eventUpdateService.insertAllLeagueEventPick(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async("TaskThreadPool")
    @Scheduled(cron = "0 0 8,10,12 * * *")
    public void updateLeagueEventResult() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isAfterMatchDay(event)) {
            return;
        }
        try {
            this.eventUpdateService.updateAllLeagueEventResult(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
