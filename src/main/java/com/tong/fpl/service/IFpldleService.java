package com.tong.fpl.service;

import com.tong.fpl.domain.FpldleData;
import com.tong.fpl.domain.FpldleHistoryData;
import com.tong.fpl.domain.RecordData;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2022/2/17
 */
public interface IFpldleService {

    /**
     * 插入fpldle字典
     */
    void insertFpldleDictionary();

    /**
     * 查询fpldle字典
     *
     * @return Map
     */
    Map<String, FpldleData> getFpldleMap();

    /**
     * 插入每日fpldle，随机从字典挑选
     */
    void insertDailyFpldle();

    /**
     * 根据赛季和element定向插入fpldle
     *
     * @param season  赛季
     * @param element 球员ID
     */
    void insertDailyFpldleByElement(String season, int element);

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
    String getWechatUserOpenId(String code);

    /**
     * 获取小程序登录token
     *
     * @return token
     */
    String getWechatAccessToken();

    /**
     * 插入每日结果
     *
     * @param openId 用户openId
     * @param result 结果
     */
    void insertDailyResult(String openId, String result);

    /**
     * 获取每日结果列表
     *
     * @param openId 用户openId
     * @param date   日期
     * @return list
     */
    List<String> getDailyResult(String openId, String date);

    /**
     * 获取指定日期用户验证结果
     *
     * @param openId openId
     * @param date   date
     * @return 验证结果
     */
    List<Integer> getDateVerifyList(String openId, String date);

    /**
     * 获取球员照片
     *
     * @param code player.code
     * @return picture.base64
     */
    String getPlayerPicture(int code);

    /**
     * 用户结果统计
     */
    void insertUserStatistic();

    /**
     * 用户结果统计
     */
    void insertDateStatistic();

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

    /**
     * 获取用户个人记录
     *
     * @param openId openId
     * @return recode list
     */
    List<RecordData> getRecordList(String openId);

}
