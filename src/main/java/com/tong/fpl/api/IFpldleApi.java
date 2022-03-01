package com.tong.fpl.api;

import com.tong.fpl.domain.FpldleData;
import com.tong.fpl.domain.FpldleHistoryData;
import com.tong.fpl.domain.RecordData;

import java.util.List;

/**
 * Create by tong on 2022/2/17
 */
public interface IFpldleApi {

    /**
     * 获取当日fpldle
     *
     * @param date 日期
     * @return data
     */
    FpldleData getDailyFpldle(String date);

    /**
     * 获取微信小程序的用户openId
     *
     * @param code 小程序用户请求code
     * @return openId
     */
    String getWechatOpenId(String code);

    /**
     * 获取微信小程序的用户openId
     *
     * @param code 小程序用户请求code
     * @return openId
     */
    String getWechatUserOpenId(String code);

    /**
     * 插入每日结果
     *
     * @param openId 用户openId
     * @param result 结果
     */
    void insertDialyResult(String openId, String result);

    /**
     * 获取每日结果列表
     *
     * @param openId 用户openId
     * @return list
     */
    List<String> getDailyResult(String openId);

    /**
     * 获取指定日期验证结果
     *
     * @param openId openId
     * @param date   日期
     * @return 验证结果
     */
    List<Integer> getVerifyList(String openId, String date);

    /**
     * 获取球员照片
     *
     * @param code player.code
     * @return picture.base64
     */
    String getPlayerPicture(int code);

    /**
     * 获取用户个人记录
     *
     * @param openId openId
     * @return recode list
     */
    List<RecordData> getRecordList(String openId);

    /**
     * 新增用户信息
     *
     * @param openId    openId
     * @param nickName  昵称
     * @param avatarUrl 头像
     */
    void insertUserInfo(String openId, String nickName, String avatarUrl);

    /**
     * 获取往期数据
     *
     * @return list
     */
    List<FpldleHistoryData> getHistoryFpldle();

}
