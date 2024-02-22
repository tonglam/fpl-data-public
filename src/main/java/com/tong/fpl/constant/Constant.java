package com.tong.fpl.constant;

/**
 * Create by tong on 2021/8/31
 */
public class Constant {

    // date_format
    public static final String DATETIME = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE = "yyyy-MM-dd";
    public static final String SHORTDAY = "yyyyMMdd";

    // api
    private static final String PREFIX = "https://fantasy.premierleague.com/api/";
    public static final String BOOTSTRAP_STATIC = PREFIX + "bootstrap-static/";
    public static final String EVENT_FIXTURES = PREFIX + "fixtures/?event=%s";
    public static final String ENTRY = PREFIX + "entry/%s/";
    public static final String ENTRY_CUP = PREFIX + "entry/%s/cup/";
    public static final String USER_HISTORY = PREFIX + "entry/%s/history/";
    public static final String USER_PICKS = PREFIX + "entry/%s/event/%s/picks/";
    public static final String USER_TRANSFER = PREFIX + "entry/%s/transfers/";
    public static final String EVENT_LIVE = PREFIX + "event/%s/live/";
    public static final String ELEMENT_SUMMARY = PREFIX + "element-summary/%s/";
    public static final String LEAGUES_CLASSIC = PREFIX + "leagues-classic/%s/standings/?page_standings=%s";
    public static final String LEAGUES_H2H = PREFIX + "leagues-h2h/%s/standings/?page_standings=%s";

    // rerun
    public static final int MAX_TRY_TIMES = 5;

    // telegramBot
    public static final String NOTIFICATION_KEY = "hermes_notification";
    private static final String NOTIFICATION_PREFIX = "http://119.8.105.33/telegramBot/letletme/";
    public static final String NOTIFICATION_TEXT = NOTIFICATION_PREFIX + "textNotification";
    public static final String NOTIFICATION_IMAGE = NOTIFICATION_PREFIX + "imageNotification";

}
