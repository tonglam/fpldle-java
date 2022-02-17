package com.tong.fpldle.service;

import com.tong.fpldle.domain.fplEntity.PlayerEntity;
import com.tong.fpldle.domain.fplEntity.PlayerSummaryEntity;

import java.util.Map;

/**
 * Create by tong on 2022/2/17
 */
public interface IRedisCacheService {

    Map<String, String> getTeamNameMap(String season);

    Map<String, String> getTeamShortNameMap(String season);

    Map<String, PlayerEntity> getPlayerMap(String season);

    Map<String, PlayerSummaryEntity> getPlayerSummaryMap(String season);


}
