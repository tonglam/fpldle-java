package com.tong.fpl.service;


import com.tong.fpl.domain.FpldleData;

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
     * @return list
     */
    List<String> getDailyResult(String openId);

    /**
     * 获取球员照片
     *
     * @param code player.code
     * @return picture.base64
     */
    String getPlayerPicture(int code);

}
