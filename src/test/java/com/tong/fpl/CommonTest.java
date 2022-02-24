package com.tong.fpl;

import com.tong.fpl.constant.Constant;
import com.tong.fpl.util.CommonUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Created by tong on 2022/02/24
 */
public class CommonTest extends FpldleApplicationTests {

    @ParameterizedTest
    @CsvSource({"20220224"})
    void date(String date) {
        String lastDate = CommonUtils.getDateFromShortDay(date);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(Constant.SHORTDAY);
        String nextDate = LocalDate.parse(lastDate).minusDays(1).format(dateTimeFormatter);
        System.out.println(1);
    }

}
