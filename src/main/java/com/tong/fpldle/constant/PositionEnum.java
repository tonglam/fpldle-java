package com.tong.fpldle.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Create by tong on 2022/2/17
 */
@Getter
@AllArgsConstructor
public enum PositionEnum {

    GKP(1), DEF(2), MID(3), FWD(4), SUB(5);

    private final int elementType;

    public static String getNameFromElementType(int elementType) {
        return Arrays.stream(PositionEnum.values())
                .filter(o -> o.getElementType() == elementType)
                .map(PositionEnum::name)
                .findFirst()
                .orElse(null);
    }

}
