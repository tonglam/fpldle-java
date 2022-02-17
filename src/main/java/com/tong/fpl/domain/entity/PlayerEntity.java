package com.tong.fpl.domain.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2022/2/17
 */
@Data
@Accessors(chain = true)
public class PlayerEntity {

    private Integer element;
    private Integer code;
    private Integer price;
    private Integer startPrice;
    private Integer elementType;
    private String firstName;
    private String secondName;
    private String webName;
    private Integer teamId;

}
