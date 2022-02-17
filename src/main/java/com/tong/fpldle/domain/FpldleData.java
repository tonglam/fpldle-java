package com.tong.fpldle.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2022/2/17
 */
@Data
@Accessors(chain = true)
public class FpldleData {

    private int element;
    private int code;
    private String name;
    private String webName;
    private String fullName;
    private String season;
    private String position;
    private String teamName;
    private String teamShortName;

}
