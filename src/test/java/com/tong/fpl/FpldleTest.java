package com.tong.fpl;

import com.tong.fpl.domain.FpldleData;
import com.tong.fpl.service.IFpldleService;
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
    @CsvSource({"2122, 448"})
    void insertDailyFpldleByElement(String season, int element) {
        this.fpldleService.insertDailyFpldleByElement(season, element);
    }

    @ParameterizedTest
    @CsvSource({"20220216"})
    void getDailyFpldle(String date) {
        FpldleData data = this.fpldleService.getDailyFpldle(date);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"aaaa, '0,1,1,2,1'"})
    void insertDailyResult(String openId, String result) {
        this.fpldleService.insertDailyResult(openId, result);
    }

    @ParameterizedTest
    @CsvSource({"aaaa"})
    void getDailyResult(String openId) {
        List<String> list = this.fpldleService.getDailyResult(openId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"15749"})
    void getPlayerPicture(int code) {
        String result = this.fpldleService.getPlayerPicture(code);
        System.out.println(1);
    }

}
