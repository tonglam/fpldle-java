package com.tong.fpl.util;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by tong on 2022/02/24
 */
public class CommonUtils {

    public static String getDateFromShortDay(String shortDate) {
        return StringUtils.joinWith("-", shortDate.substring(0, 4), shortDate.substring(4, 6), shortDate.substring(6, 8));
    }

}
