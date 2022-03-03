package com.tong.fpl.service;

import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.domain.entity.PlayerSummaryEntity;

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
