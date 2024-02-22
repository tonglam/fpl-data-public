package com.tong.fpl.task;

import com.tong.fpl.service.IDataService;
import com.tong.fpl.service.IEventUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Create by tong on 2021/8/31
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DailyTask {

    private final IEventUpdateService eventUpdateService;
    private final IDataService dataService;

    @Async("TaskThreadPool")
    @Scheduled(cron = "0 35 6 * * *")
    public void updateEventData() {
        try {
            this.eventUpdateService.updateEvent();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async("TaskThreadPool")
    @Scheduled(cron = "0 25-35 9 * * *")
    public void updatePlayerValue() {
        try {
            this.eventUpdateService.updatePlayerValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async("TaskThreadPool")
    @Scheduled(cron = "0 40 9 * * *")
    public void updatePlayerStat() {
        try {
            this.eventUpdateService.updatePlayerStat();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async("TaskThreadPool")
    @Scheduled(cron = "0 42 9 * * *")
    public void updateAllEntryInfo() {
        try {
            this.eventUpdateService.updateAllEntryInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async("TaskThreadPool")
    @Scheduled(cron = "0 45 9 * * *")
    public void updateTournamentInfo() {
        try {
            this.eventUpdateService.updateTournamentInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Async("TaskThreadPool")
    @Scheduled(cron = "0 10 */1 * * *")
    public void insertPlayerValueInfo() {
        try {
            this.dataService.insertPlayerValueInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}


