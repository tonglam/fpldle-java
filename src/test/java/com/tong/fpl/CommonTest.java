package com.tong.fpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tong on 2022/02/24
 */
public class CommonTest extends FpldleApplicationTests {

    @ParameterizedTest
    @CsvSource({"20220224"})
    void date(String date) {
        LocalDateTime dateTime = LocalDateTime.now();
        LocalDateTime a = LocalDateTime.of(LocalDate.from(LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth())), LocalTime.MIN);
        System.out.println(dateTime.isAfter(a));
        System.out.println(1);

//        String a = "S.Longstaff";
//        String b = a.replaceAll("\\.", "");


//        String lastDate = CommonUtils.getDateFromShortDay(date);
//        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(Constant.SHORTDAY);
//        String nextDate = LocalDate.parse(lastDate).minusDays(1).format(dateTimeFormatter);
        System.out.println(1);
    }

    @Test
    void leeCode() {
        int target = 9;
        int[] nums = new int[]{4, 15, 2, 1, 3, 5};

        Map<Integer, Integer> map = new HashMap<>(2);
        for (int i = 0; i < nums.length; i++) {
            map.put(nums[i], i);
        }
        for (int i = 0; i < nums.length; i++) {
            int a = target - nums[i];
            if (map.containsKey(a) && map.get(a) != i) {
                System.out.println(new int[]{i, map.get(a)});
            }
        }
        System.out.println(1);
    }

}
