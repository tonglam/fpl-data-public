package com.tong.fpl.service;

import java.util.List;

/**
 * Created by tong on 2023/04/23
 */
public interface INotifyService {

    void notifyViaTelegram(String text, String imgUrl, String imgCaption, List<String> userList);

    void notifyViaEmail(String subject, String body, List<String> emailList);

}
