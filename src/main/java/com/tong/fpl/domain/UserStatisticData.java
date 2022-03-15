package com.tong.fpl.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by tong on 2022/02/21
 */
@Data
@Accessors(chain = true)
public class UserStatisticData {

    private String openId;
    private String nickName;
    private String avatarUrl;
    private int tryTimes;
    private boolean solve;
    private int totalGuessDays;
    private int totalTryTimes;
    private int totalHitTimes;
    private int consecutiveGuessDays;
    private int consecutiveHitDays;

}
