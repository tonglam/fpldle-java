package com.tong.fpl.api;

import com.tong.fpl.domain.FpldleData;

import java.io.InputStream;
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
    String getWechatUserOpenId(String code);

    /**
     * 球员名字模糊查询
     *
     * @param fuzzyName 查询值
     * @return 结果列表
     */
    List<String> fuzzyQueryName(String fuzzyName);

    /**
     * 插入每日结果
     *
     * @param openId     用户openId
     * @param resultList 结果列表
     */
    void insertDialyResult(String openId, List<String> resultList);

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
     * @return picture.inputStream
     */
    InputStream getPlayerPicture(int code);

}
