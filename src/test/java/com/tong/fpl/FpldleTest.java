package com.tong.fpl;

import com.tong.fpl.domain.FpldleData;
import com.tong.fpl.service.IFpldleService;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2022/2/17
 */
public class FpldleTest extends FpldleApplicationTests {

    @Autowired
    private IFpldleService fpldleService;

    @Test
    void insertFpldleDictionary() {
        this.fpldleService.insertFpldleDictionary();
    }

    @Test
    void getFpldleMap() {
        Map<String, FpldleData> map = this.fpldleService.getFpldleMap();
        System.out.println(1);
    }

    @Test
    void insertDailyFpldle() {
        this.fpldleService.insertDailyFpldle();
    }

    @ParameterizedTest
    @CsvSource({"1920, 95"})
    void insertDailyFpldleByElement(String season, int element) {
        this.fpldleService.insertDailyFpldleByElement(season, element);
    }

    @ParameterizedTest
    @CsvSource({"20220217"})
    void getDailyFpldle(String date) {
        FpldleData data = this.fpldleService.getDailyFpldle(date);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"aaaa"})
    void insertDailyResult(String openId) {
        List<String> list = Lists.newArrayList();
        list.add("0,1,1,2,1");
        list.add("0,1,1,2,1");
        list.add("1,1,1,2,1");
        list.add("2,2,1,2,1");
        list.add("2,1,1,2,1");
        list.add("2,2,2,2,2");
        this.fpldleService.insertDailyResult(openId, list);
    }

    @ParameterizedTest
    @CsvSource({"aaaa"})
    void getDailyResult(String openId) {
        List<String> list = this.fpldleService.getDailyResult(openId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource("sa")
    void fuzzyQueryName(String fuzzyName) {
        List<String> list = this.fpldleService.fuzzyQueryName(fuzzyName);
        System.out.println(1);
    }

}
