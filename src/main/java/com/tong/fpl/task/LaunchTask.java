package com.tong.fpl.task;

import com.tong.fpl.constant.enums.NotifyEnums;
import com.tong.fpl.service.IInterfaceService;
import com.tong.fpl.utils.NotifyUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by tong on 2022/07/05
 */
//@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LaunchTask {

    private final IInterfaceService interfaceService;
    private final JavaMailSender javaMailSender;

    @Async("TaskThreadPool")
    @Scheduled(cron = "0 */1 * * * *")
    public void warning() {
        this.interfaceService.getBootstrapStatic().ifPresent(o -> {
            if (o.getEvents().size() == 0) {
                // send warning email
                String msg = "【NEW SEASON】WARNING! WARNING! WARNING!";
                NotifyUtils.notify(NotifyEnums.Telegram.getType(), msg, "");
                this.sendEmail(msg);
            }
        });
    }

    @Async("TaskThreadPool")
    @Scheduled(cron = "0 */1 * * * *")
    public void happening() {
        this.interfaceService.getBootstrapStatic().ifPresent(o -> {
            if (o.getEvents().size() != 0) {
                if (StringUtils.equals("2024", o.getEvents().get(0).getDeadlineTime().substring(0, 4))) {
                    // send happening email
                    String msg = "【NEW SEASON】ITS HAPPENING!!!";
                    NotifyUtils.notify(NotifyEnums.Telegram.getType(), msg, "");
                    this.sendEmail(msg);
                }
            }
        });
    }

    private void sendEmail(String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("bluedragon00000@sina.com");
        message.setTo("bluedragon00000@gmail.com");
        message.setSubject("HAPPY LOW ID DAY");
        message.setText(text);
        javaMailSender.send(message);
    }

}
