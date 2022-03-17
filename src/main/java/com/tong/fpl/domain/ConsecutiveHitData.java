package com.tong.fpl.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by tong on 2022/03/14
 */
@Data
@Accessors(chain = true)
public class ConsecutiveHitData {

    private int rank;
    private String openId;
    private String nickName;
    private String avatarUrl;
    private int consecutiveDays;
    private String consecutiveStartDay;
    private String consecutiveEndDay;

}
