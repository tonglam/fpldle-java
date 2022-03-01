package com.tong.fpl.controller;

import com.tong.fpl.api.IFpldleApi;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.FpldleData;
import com.tong.fpl.domain.FpldleHistoryData;
import com.tong.fpl.domain.RecordData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Create by tong on 2022/2/17
 */
@RestController
@RequestMapping("/fpldle")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FpldleController {

    private final IFpldleApi fpldleApi;

    @RequestMapping("/getServiceDate")
    public String getServiceDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(Constant.SHORTDAY)).substring(4, 8);
    }

    @RequestMapping("/getDailyFpldle")
    public FpldleData getDailyFpldle() {
        return this.fpldleApi.getDailyFpldle(LocalDate.now().format(DateTimeFormatter.ofPattern(Constant.SHORTDAY)));
    }

    @RequestMapping("/getWechatOpenId")
    public String getWechatOpenId(@RequestParam String code) {
        return this.fpldleApi.getWechatOpenId(code);
    }

    @RequestMapping("/getWechatUserOpenId")
    public String getWechatUserOpenId(@RequestParam String code) {
        return this.fpldleApi.getWechatUserOpenId(code);
    }

    @RequestMapping("/insertDailyResult")
    public void insertDailyResult(@RequestParam String openId, @RequestParam String result) {
        this.fpldleApi.insertDialyResult(openId, result);
    }

    @RequestMapping("/getDailyResult")
    public List<String> getDailyResult(@RequestParam String openId) {
        return this.fpldleApi.getDailyResult(openId);
    }

    @RequestMapping("/getPlayerPicture")
    public String getPlayerPicture(@RequestParam int code) {
        return this.fpldleApi.getPlayerPicture(code);
    }

    @RequestMapping("/getRecordList")
    public List<RecordData> getRecordList(@RequestParam String openId) {
        return this.fpldleApi.getRecordList(openId);
    }

    @RequestMapping("/insertUserInfo")
    void insertUserInfo(@RequestParam String openId, @RequestParam String nickName, @RequestParam String avatarUrl) {
        this.fpldleApi.insertUserInfo(openId, nickName, avatarUrl);
    }

    @RequestMapping("/getHistoryFpldle")
    public List<FpldleHistoryData> getHistoryFpldle() {
        return this.fpldleApi.getHistoryFpldle();
    }

}
