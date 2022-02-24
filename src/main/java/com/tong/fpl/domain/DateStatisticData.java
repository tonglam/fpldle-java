package com.tong.fpl.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by tong on 2022/02/21
 */
@Data
@Accessors(chain = true)
public class DateStatisticData {

    private int totalGuessUser;
    private int totalHitUser;
    private String userHitRate;
    private int totalGuessTimes;
    private int totalTryTimes;
    private double averageGuessTimes;
    private double averageTryTimes;
    private double averageHitTimes;

}
