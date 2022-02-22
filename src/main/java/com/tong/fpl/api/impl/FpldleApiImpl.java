package com.tong.fpl.api.impl;

import com.tong.fpl.api.IFpldleApi;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.FpldleData;
import com.tong.fpl.domain.RecordData;
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
    public String getWechatUserOpenId(String code) {
        return this.fpldleService.getWechatUserOpenId(code);
    }

    @Override
    public void insertDialyResult(String openId, String result) {
        this.fpldleService.insertDailyResult(openId, result);
    }

    @Override
    public List<String> getDailyResult(String openId) {
        return this.fpldleService.getDailyResult(openId, LocalDate.now().format(DateTimeFormatter.ofPattern(Constant.SHORTDAY)));
    }

    @Override
    public String getPlayerPicture(int code) {
        return this.fpldleService.getPlayerPicture(code);
    }

    @Override
    public List<RecordData> getRecordList(String openId) {
        return this.fpldleService.getRecordList(openId);
    }

    public void insertUserInfo(String openId, String nickName, String avatarUrl) {
        this.fpldleService.insertUserInfo(openId, nickName, avatarUrl);
    }

}
