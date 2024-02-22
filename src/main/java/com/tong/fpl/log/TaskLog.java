package com.tong.fpl.log;

import lombok.extern.slf4j.Slf4j;

/**
 * Create by tong on 2021/8/30
 */
@Slf4j
public class TaskLog {

	public static void info(String format, Object... arguments) {
		log.info(format, arguments);
	}

	public static void error(String format, Object... arguments) {
		log.error(format, arguments);
	}

}
