package com.tong.fpl.config.log;

import lombok.extern.slf4j.Slf4j;

/**
 * Create by tong on 2022/2/17
 */
@Slf4j
public class InterfaceLog {

    public static void info(String format, Object... arguments) {
        log.info(format, arguments);
    }

    public static void error(String format, Object... arguments) {
        log.error(format, arguments);
    }

}
