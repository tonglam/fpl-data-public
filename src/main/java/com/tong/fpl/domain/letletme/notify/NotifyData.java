package com.tong.fpl.domain.letletme.notify;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Created by tong on 2023/06/14
 */
@Data
@Accessors(chain = true)
public class NotifyData {

    private List<String> userList;
    private String text;
    private String imgUrl;
    private String imgCaption;

}
