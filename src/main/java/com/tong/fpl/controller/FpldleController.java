package com.tong.fpl.controller;

import com.tong.fpl.api.IFpldleApi;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.FpldleData;
import com.tong.fpl.domain.FpldleHistoryData;
import com.tong.fpl.domain.RecordData;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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

    @RequestMapping("/getWechatAccessToken")
    public String getWechatAccessToken() {
        return this.fpldleApi.getWechatAccessToken();
    }

    @RequestMapping("/getWechatUserOpenId")
    public String getWechatUserOpenId(@RequestParam String code) {
        return this.fpldleApi.getWechatUserOpenId(code);
    }

    @RequestMapping("/insertDailyResult")
    public void insertDailyResult(@RequestParam String openId, @RequestParam String result) {
        if (StringUtils.containsAnyIgnoreCase(result, "select", "from", "limit", "count", "union", "delete", "update", "truncate", "drop")) {
            return;
        }
        this.fpldleApi.insertDailyResult(openId, result);
    }

    @RequestMapping("/getDailyResult")
    public List<String> getDailyResult(@RequestParam String openId) {
        return this.fpldleApi.getDailyResult(openId);
    }

    @RequestMapping("/getDateVerifyList")
    public List<Integer> getDateVerifyList(@RequestParam String openId, @RequestParam String date) {
        return this.fpldleApi.getDateVerifyList(openId, LocalDate.now().getYear() + date);
    }

    @RequestMapping("/getPlayerPicture")
    public String getPlayerPicture(@RequestParam int code) {
        return this.fpldleApi.getPlayerPicture(code);
    }

    @RequestMapping("/insertUserInfo")
    void insertUserInfo(@RequestParam String openId, @RequestParam String nickName, @RequestParam String avatarUrl) {
        this.fpldleApi.insertUserInfo(openId, nickName, avatarUrl);
    }

    @RequestMapping("/getHistoryFpldle")
    public List<FpldleHistoryData> getHistoryFpldle() {
        return this.fpldleApi.getHistoryFpldle();
    }

    @RequestMapping("/getRecordList")
    public List<RecordData> getRecordList(@RequestParam String openId) {
        return this.fpldleApi.getRecordList(openId);
    }

}
