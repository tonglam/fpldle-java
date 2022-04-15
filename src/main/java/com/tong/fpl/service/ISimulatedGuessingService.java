package com.tong.fpl.service;

/**
 * Created by tong on 2022/03/22
 */
public interface ISimulatedGuessingService {

    /**
     * 模拟猜词
     *
     * @param openId openId
     * @param date   date
     * @return guessResult
     */
    String simulate(String openId, String date);

}
