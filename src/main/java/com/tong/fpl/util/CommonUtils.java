package com.tong.fpl.util;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * Created by tong on 2022/02/24
 */
public class CommonUtils {

    public static String getDateFromShortDay(String shortDate) {
        return StringUtils.joinWith("-", shortDate.substring(0, 4), shortDate.substring(4, 6), shortDate.substring(6, 8));
    }

    public static String fillDateYear(String date) {
        return LocalDate.now().getYear() + date;
    }

    public static List<String> word2Letter(String word) {
        if (StringUtils.isEmpty(word)) {
            return Lists.newArrayList();
        }
        List<String> list = Lists.newArrayList();
        char[] charArray = word.toCharArray();
        for (char c :
                charArray) {
            list.add(String.valueOf(c));
        }
        return list;
    }

    public static String word2Result(String word) {
        if (StringUtils.isEmpty(word)) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        char[] charArray = word.toCharArray();
        for (char c :
                charArray) {
            builder.append(c).append(",");
        }
        return StringUtils.substringBeforeLast(builder.toString(), ",");
    }

    public static String letter2Word(List<String> letterList) {
        StringBuilder result = new StringBuilder();
        for (String letter :
                letterList) {
            result.append(letter);
        }
        return result.toString();
    }

    public static LocalDateTime getFirstDayOfMonth() {
        return LocalDateTime.of(LocalDate.from(LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth())), LocalTime.MIN);
    }

}
