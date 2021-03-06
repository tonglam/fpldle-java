package com.tong.fpl.api.impl;

import com.tong.fpl.api.IFpldleApi;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.*;
import com.tong.fpl.service.IFpldleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Create by tong on 2022/2/17
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FpldleApiImpl implements IFpldleApi {

    private final IFpldleService fpldleService;

    @Override
    public FpldleData getDailyFpldle(String date) {
        return this.fpldleService.getDailyFpldle(date);
    }

    @Override
    public String getWechatAccessToken() {
        return this.fpldleService.getWechatAccessToken();
    }

    @Override
    public String getWechatUserOpenId(String code) {
        return this.fpldleService.getWechatUserOpenId(code);
    }

    @Override
    public void insertDailyResult(String openId, String result) {
        this.fpldleService.insertDailyResult(openId, result);
    }

    @Override
    public List<String> getDailyResult(String openId) {
        return this.fpldleService.getDailyResult(openId, LocalDate.now().format(DateTimeFormatter.ofPattern(Constant.SHORTDAY)));
    }

    @Override
    public List<String> getDateVerifyList(String openId, String date) {
        return this.fpldleService.getDateVerifyList(openId, date);
    }

    @Override
    public String getPlayerPicture(int code) {
        return this.fpldleService.getPlayerPicture(code);
    }

    @Override
    public void insertUserInfo(String openId, String nickName, String avatarUrl) {
        this.fpldleService.insertUserInfo(openId, nickName, avatarUrl);
    }

    @Override
    public List<FpldleHistoryData> getHistoryFpldle() {
        return this.fpldleService.getHistoryFpldle();
    }

    @Override
    public List<RecordData> getRecordList(String openId) {
        return this.fpldleService.getRecordList(openId);
    }

    @Override
    public List<LastDayHitData> getLastDayHitRank() {
        return this.fpldleService.getLastDayHitRank();
    }

    @Override
    public List<ConsecutiveHitData> getConsecutiveHitRank() {
        return this.fpldleService.getConsecutiveHitRank();
    }

    @Override
    public List<AverageHitTimesData> getAverageHitTimesRank() {
        return this.fpldleService.getAverageHitTimesRank();
    }

    @Override
    public FpldleData getFpldleByName(String name) {
        return this.fpldleService.getFpldleByName(name);
    }

}
