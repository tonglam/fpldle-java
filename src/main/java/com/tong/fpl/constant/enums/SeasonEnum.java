package com.tong.fpl.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Create by tong on 2022/2/17
 */
@Getter
@AllArgsConstructor
public enum SeasonEnum {

    Season_1617("1617"), Season_1718("1718"), Season_1819("1819"),
    Season_1920("1920"), Season_2021("2021"), Season_2122("2122");

    private final String seasonValue;

    public static List<String> getAllSeason() {
        return Arrays.stream(SeasonEnum.values())
                .map(SeasonEnum::getSeasonValue)
                .collect(Collectors.toList());
    }

}
