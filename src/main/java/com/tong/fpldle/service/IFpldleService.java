package com.tong.fpldle.service;


import com.tong.fpldle.domain.FpldleData;

import java.io.InputStream;
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
     * @param openId     用户openId
     * @param resultList 结果列表
     */
    void insertDailyResult(String openId, List<String> resultList);

    /**
     * 获取每日结果列表
     *
     * @param openId 用户openId
     * @return list
     */
    List<String> getDailyResult(String openId);

    /**
     * 球员名字模糊查询
     *
     * @param fuzzyName 查询值
     * @return 结果列表
     */
    List<String> fuzzyQueryName(String fuzzyName);

    /**
     * 获取球员照片
     *
     * @param code player.code
     * @return picture.inputStream
     */
    InputStream getPlayerPicture(int code);

}
