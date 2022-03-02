package com.tong.fpl.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Create by tong on 2022/2/17
 */
@Getter
@AllArgsConstructor
public enum GuessResultEnum {

    WAITING(-1), WRONG(0), ORDER(1), CORRECT(2);

    private final int result;

}
