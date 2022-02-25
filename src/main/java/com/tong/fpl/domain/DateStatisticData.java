package com.tong.fpl.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by tong on 2022/02/21
 */
@Data
@Accessors(chain = true)
public class DateStatisticData {

    private String date;
    private int totalUsers;
    private int totalHitUsers;
    private String userHitRate;
    private int totalTryTimes;
    private double averageTryTimes;
    private double averageHitTimes;

}
