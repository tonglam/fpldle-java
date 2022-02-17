package com.tong.fpl.domain.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2022/2/17
 */
@Data
@Accessors(chain = true)
public class TeamEntity {

    private Integer id;
    private Integer code;
    private String name;
    private String shortName;

}
