package com.tong.fpl.utils;

import com.google.common.collect.Lists;
import com.tong.fpl.constant.enums.NotifyEnums;
import com.tong.fpl.service.INotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by tong on 2023/06/14
 */
@Component
public class NotifyUtils {

    private static INotifyService notifyService;

    public static void notify(int type, String text, String subject) {
        if (NotifyEnums.Telegram.getType() == type) {
            notifyService.notifyViaTelegram(text, "", "", Lists.newArrayList("让让我", "tonglam14"));
        } else {
            notifyService.notifyViaEmail(subject, text, Lists.newArrayList("bluedragon00000@gmail.com"));
        }
    }

    @Autowired
    private void setNotifyService(INotifyService notifyService) {
        NotifyUtils.notifyService = notifyService;
    }

}
