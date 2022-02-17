package com.tong.fpldle.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Create by tong on 2022/2/17
 */
@Getter
@AllArgsConstructor
public enum FpldleGuessResultEnum {

    WRONG(0), ORDER(1), CORRECT(2);

    private final int result;

    public static String getGuessResultName(int result) {
        return Arrays.stream(FpldleGuessResultEnum.values())
                .filter(o -> o.getResult() == result)
                .map(Enum::name)
                .findFirst()
                .orElse("");
    }

}
