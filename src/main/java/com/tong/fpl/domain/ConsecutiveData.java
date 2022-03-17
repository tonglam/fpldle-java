package com.tong.fpl.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by tong on 2022/03/17
 */
@Data
@Accessors(chain = true)
public class ConsecutiveData {

    private int days;
    private String startDay;
    private String endDay;

}
