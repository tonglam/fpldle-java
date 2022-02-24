package com.tong.fpl.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by tong on 2022/02/24
 */
@Data
@Accessors(chain = true)
public class FpldleHistoryData {

    private String date;
    private int element;
    private int code;
    private String name;
    private String fullName;
    private String season;
    private String position;
    private String teamName;
    private String teamShortName;

}
