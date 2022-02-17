package com.tong.fpl.service.impl;

import com.google.common.collect.Maps;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.domain.entity.PlayerSummaryEntity;
import com.tong.fpl.domain.entity.TeamEntity;
import com.tong.fpl.service.IRedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Create by tong on 2022/2/17
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RedisCacheServiceImpl implements IRedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;


    @Override
    public Map<String, String> getTeamNameMap(String season) {
        Map<String, String> map = Maps.newHashMap();
        String key = StringUtils.joinWith("::", TeamEntity.class.getSimpleName(), season, "name");
        this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(k.toString(), (String) v));
        return map;
    }

    @Override
    public Map<String, String> getTeamShortNameMap(String season) {
        Map<String, String> map = Maps.newHashMap();
        String key = StringUtils.joinWith("::", TeamEntity.class.getSimpleName(), season, "shortName");
        this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(k.toString(), (String) v));
        return map;
    }

    @Override
    public Map<String, PlayerEntity> getPlayerMap(String season) {
        Map<String, PlayerEntity> map = Maps.newHashMap();
        String key = StringUtils.joinWith("::", PlayerEntity.class.getSimpleName(), season);
        this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(String.valueOf(k), (PlayerEntity) v));
        return map;
    }

    @Override
    public Map<String, PlayerSummaryEntity> getPlayerSummaryMap(String season) {
        Map<String, PlayerSummaryEntity> map = Maps.newHashMap();
        String key = StringUtils.joinWith("::", PlayerSummaryEntity.class.getSimpleName(), season);
        this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(String.valueOf(k), (PlayerSummaryEntity) v));
        return map;
    }

}
