package com.tong.fpldle.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2022/2/17
 */
@Data
@Accessors(chain = true)
public class FpleResultData {

    private String openId;
    private List<String> resultList;

}
