package com.tong.fpl.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by tong on 2022/02/21
 */
@Data
@Accessors(chain = true)
public class RecordData {

    private String openId;
    private String date;
    private String result;
    private int tryTimes;
    private boolean solve;

}
