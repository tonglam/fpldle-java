package com.tong.fpl.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by tong on 2022/03/16
 */
@Data
@Accessors(chain = true)
public class LastDayHitData {

    private int rank;
    private String openId;
    private String nickName;
    private String avatarUrl;
    private int tryTimes;

}
