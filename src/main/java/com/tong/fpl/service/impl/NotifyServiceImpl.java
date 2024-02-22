package com.tong.fpl.service.impl;

import com.tong.fpl.service.IInterfaceService;
import com.tong.fpl.service.INotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by tong on 2023/06/14
 */
@Slf4j
@Service
@RequiredArgsConstructor()
public class NotifyServiceImpl implements INotifyService {

    private final IInterfaceService interfaceService;
    private final JavaMailSender mailSender;

    @Override
    public void notifyViaTelegram(String text, String imgUrl, String imgCaption, List<String> userList) {
        if (StringUtils.isNotEmpty(text) && StringUtils.isEmpty(imgUrl) && StringUtils.isEmpty(imgCaption)) {
            this.interfaceService.telegramText(text, userList);
        } else if (StringUtils.isEmpty(text) && StringUtils.isNotEmpty(imgUrl) && StringUtils.isNotEmpty(imgCaption)) {
            this.interfaceService.telegramImage(imgUrl, imgCaption, userList);
        } else {
            log.error("text and imgUrl/imgCaption must be one of them");
        }
    }

    @Override
    public void notifyViaEmail(String subject, String text, List<String> emailList) {
        emailList.forEach(email -> {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        });
    }

}
