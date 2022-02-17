package com.tong.fpldle.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tong.fpldle.constant.Constant;
import com.tong.fpldle.constant.FpldleGuessResultEnum;
import com.tong.fpldle.constant.PositionEnum;
import com.tong.fpldle.constant.SeasonEnum;
import com.tong.fpldle.domain.FpldleData;
import com.tong.fpldle.domain.fplEntity.PlayerEntity;
import com.tong.fpldle.domain.wechat.AuthSessionData;
import com.tong.fpldle.service.IFpldleService;
import com.tong.fpldle.service.IInterfaceService;
import com.tong.fpldle.service.IRedisCacheService;
import com.tong.fpldle.util.JsonUtils;
import com.tong.fpldle.util.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Create by tong on 2022/2/17
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FpldleServiceImpl implements IFpldleService {

    private final IRedisCacheService redisCacheService;
    private final IInterfaceService interfaceService;

    @Override
    public void insertFpldleDictionary() {
        // filter data
        Map<String, FpldleData> map = Maps.newHashMap();
        SeasonEnum.getAllSeason().forEach(season -> {
            Map<String, PlayerEntity> playerMap = this.redisCacheService.getPlayerMap(season);
            Map<String, String> teamNameMap = this.redisCacheService.getTeamNameMap(season);
            Map<String, String> teamShortNameMap = this.redisCacheService.getTeamShortNameMap(season);
            this.redisCacheService.getPlayerSummaryMap(season).values()
                    .stream()
                    .filter(o -> o.getEvent() == 1 && o.getSelected() > 100000)
                    .forEach(o -> {
                        int element = o.getElement();
                        PlayerEntity playerEntity = playerMap.getOrDefault(String.valueOf(element), null);
                        if (playerEntity == null || StringUtils.isEmpty(playerEntity.getWebName())) {
                            return;
                        }
                        String webName = this.getFpldleWebName(playerEntity.getWebName());
                        String fpldle = this.getFpldleName(webName);
                        map.put(fpldle,
                                new FpldleData()
                                        .setElement(o.getElement())
                                        .setCode(o.getCode())
                                        .setName(fpldle)
                                        .setWebName(webName)
                                        .setFullName(StringUtils.joinWith(" ", playerEntity.getFirstName(), playerEntity.getSecondName()))
                                        .setSeason(season)
                                        .setPosition(PositionEnum.getNameFromElementType(playerEntity.getElementType()))
                                        .setTeamName(teamNameMap.getOrDefault(String.valueOf(o.getTeamId()), ""))
                                        .setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getTeamId()), ""))
                        );
                    });
        });
        // redis
        String key = StringUtils.joinWith("::", "Fpldle", "Dictionary");
        RedisUtils.removeCacheByKey(key);
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        map.forEach((element, value) -> valueMap.put(String.valueOf(element), value));
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
        log.info("insert Fpldle dictionary size:{}", map.size());
    }

    private String getFpldleWebName(String webName) {
        return webName
                .replaceAll(" ", "")
                .replaceAll("'", "")
                .replaceAll("-", "")
                .replaceAll("é", "e")
                .replaceAll("í", "i")
                .replaceAll("ü", "u")
                .replaceAll("Ö", "o")
                .replaceAll("ć", "c")
                .replaceAll("à", "a")
                .replaceAll("ó", "o")
                .replaceAll("ï", "i")
                .replaceAll("ø", "o")
                .replaceAll("ö", "o")
                .replaceAll("á", "a")
                .replaceAll("ß", "ss");
    }

    private String getFpldleName(String webName) {
        String name;
        int length = 5;
        if (webName.length() < length) {
            name = StringUtils.rightPad(webName, length, "x");
        } else {
            name = webName.substring(0, length);
        }
        return StringUtils.upperCase(name);
    }

    @Override
    public Map<String, FpldleData> getFpldleMap() {
        String key = StringUtils.joinWith("::", "Fpldle", "Dictionary");
        Map<String, FpldleData> map = Maps.newHashMap();
        RedisUtils.getHashByKey(key).forEach((k, v) -> map.put(k.toString(), (FpldleData) v));
        return map;
    }

    @Override
    public void insertDailyFpldle() {
        Map<String, FpldleData> map = this.getFpldleMap();
        if (CollectionUtils.isEmpty(map)) {
            log.error("fpldle dictionary is empty");
            return;
        }
        List<FpldleData> list = new ArrayList<>(map.values());
        if (CollectionUtils.isEmpty(list)) {
            log.error("fpldle dictionary is empty");
            return;
        }
        int index = new Random().nextInt(map.size());
        FpldleData data = list.get(index);
        if (data == null) {
            log.error("fpldle data is empty");
            return;
        }
        log.info("date:{}, fpldle:{}", data, data.getFullName());
        // redis
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern(Constant.SHORTDAY));
        String key = StringUtils.joinWith("::", "Fpldle", "Daily", date);
        RedisUtils.removeCacheByKey(key);
        Map<String, Object> cacheMap = Maps.newHashMap();
        cacheMap.put(key, data);
        RedisUtils.pipelineValueCache(cacheMap, 1, TimeUnit.DAYS);
    }

    @Override
    public void insertDailyFpldleByElement(String season, int element) {
        Map<String, FpldleData> map = this.getFpldleMap();
        if (CollectionUtils.isEmpty(map)) {
            log.error("fpldle dictionary is empty");
            return;
        }
        List<FpldleData> list = new ArrayList<>(map.values());
        if (CollectionUtils.isEmpty(list)) {
            log.error("fpldle dictionary is empty");
            return;
        }
        FpldleData data = list
                .stream()
                .filter(o -> StringUtils.equals(season, o.getSeason()) && element == o.getElement())
                .findFirst()
                .orElse(null);
        if (data == null) {
            log.error("fpldle data is empty");
            return;
        }
        log.info("date:{}, fpldle:{}", data, data.getFullName());
        // redis
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern(Constant.SHORTDAY));
        String key = StringUtils.joinWith("::", "Fpldle", "Daily", date);
        RedisUtils.removeCacheByKey(key);
        Map<String, Object> cacheMap = Maps.newHashMap();
        cacheMap.put(key, data);
        RedisUtils.pipelineValueCache(cacheMap, 1, TimeUnit.DAYS);
    }

    @Override
    public FpldleData getDailyFpldle(String date) {
        if (date.contains("-")) {
            date = date.replaceAll("-", "");
        }
        String key = StringUtils.joinWith("::", "Fpldle", "Daily", date);
        return (FpldleData) RedisUtils.getValueByKey(key).orElse(new FpldleData());
    }

    @Override
    public String getWechatUserOpenId(String code) {
        String appId = "wx4b37f3f2c2c7f169";
        String secretId = "d7e8060219867c056f314fa177b0e109";
        AuthSessionData data = this.interfaceService.getAuthSessionInfo(appId, secretId, code).orElse(null);
        if (data == null) {
            return "";
        }
        return data.getOpenid();
    }

    @Override
    public void insertDailyResult(String openId, List<String> resultList) {
        if (CollectionUtils.isEmpty(resultList)) {
            return;
        }
        List<String> list = Lists.newArrayList();
        resultList.forEach(o -> list.add(FpldleGuessResultEnum.getGuessResultName(Integer.parseInt(o))));
        String result = JsonUtils.obj2json(list);
        log.info("openId:{}, result:{}", openId, result);
        // redis
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern(Constant.SHORTDAY));
        String key = StringUtils.joinWith("::", "Fpldle", "Result", openId, date);
        RedisUtils.removeCacheByKey(key);
        Map<String, Object> cacheMap = Maps.newHashMap();
        cacheMap.put(key, result);
        RedisUtils.pipelineValueCache(cacheMap, 1, TimeUnit.DAYS);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getDailyResult(String openId) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern(Constant.SHORTDAY));
        String key = StringUtils.joinWith("::", "Fpldle", "Result", openId, date);
        return (List<String>) RedisUtils.getValueByKey(key).orElse(Lists.newArrayList());
    }

    //    @Cacheable(cacheNames ="Fpldle")
    @Override
    public List<String> fuzzyQueryName(String fuzzyName) {
        String hit = StringUtils.lowerCase(fuzzyName);
        String key = StringUtils.joinWith("::", "Fpldle", "Dictionary");
        List<FpldleData> list = Lists.newArrayList();
        RedisUtils.getHashByKey(key).forEach((k, v) -> list.add((FpldleData) v));
        return list
                .stream()
                .map(o -> StringUtils.lowerCase(o.getWebName()))
                .filter(o -> o.contains(hit))
                .collect(Collectors.toList());
    }

    @Override
    public InputStream getPlayerPicture(int code) {
        return this.interfaceService.getPlayerPicture(code).orElse(null);
    }

}
