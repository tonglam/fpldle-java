package com.tong.fpl.service.impl;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.constant.enums.PositionEnum;
import com.tong.fpl.constant.enums.SeasonEnum;
import com.tong.fpl.domain.FpldleData;
import com.tong.fpl.domain.RecordData;
import com.tong.fpl.domain.UserStatisticData;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.domain.wechat.AuthSessionData;
import com.tong.fpl.service.IFpldleService;
import com.tong.fpl.service.IInterfaceService;
import com.tong.fpl.service.IRedisCacheService;
import com.tong.fpl.util.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        String key = StringUtils.joinWith("::", Constant.RESDIS_PREFIX, Constant.DICTIONARY);
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
        String key = StringUtils.joinWith("::", Constant.RESDIS_PREFIX, Constant.DICTIONARY);
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
        // filter history data from redis
        String key = StringUtils.joinWith("::", Constant.RESDIS_PREFIX, Constant.DAILY);
        List<Integer> historyList = Lists.newArrayList();
        RedisUtils.getHashByKey(key).forEach((k, v) -> {
            FpldleData historyData = (FpldleData) v;
            historyList.add(historyData.getCode());
        });
        // choose from filtered list
        List<FpldleData> list = map.values()
                .stream()
                .filter(o -> !historyList.contains(o.getCode()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            log.error("fpldle filter list is empty");
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
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        RedisUtils.getHashByKey(key).forEach((k, v) -> valueMap.put(date, v));
        valueMap.put(date, data);
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    @Override
    public void insertDailyFpldleByElement(String season, int element) {
        Map<String, FpldleData> map = this.getFpldleMap();
        if (CollectionUtils.isEmpty(map)) {
            log.error("fpldle dictionary is empty");
            return;
        }
        // filter history data from redis
        String key = StringUtils.joinWith("::", Constant.RESDIS_PREFIX, Constant.DAILY);
        List<Integer> historyList = Lists.newArrayList();
        RedisUtils.getHashByKey(key).forEach((k, v) -> {
            FpldleData historyData = (FpldleData) v;
            historyList.add(historyData.getCode());
        });
        FpldleData data = map.values()
                .stream()
                .filter(o -> !historyList.contains(o.getCode()))
                .filter(o -> StringUtils.equals(season, o.getSeason()) && element == o.getElement())
                .findFirst()
                .orElse(null);
        if (data == null) {
            log.error("fpldle data is empty or was used");
            return;
        }
        log.info("date:{}, fpldle:{}", data, data.getFullName());
        // redis
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern(Constant.SHORTDAY));
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        RedisUtils.getHashByKey(key).forEach((k, v) -> valueMap.put(date, v));
        valueMap.put(date, data);
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    @Override
    public FpldleData getDailyFpldle(String date) {
        if (date.contains("-")) {
            date = date.replaceAll("-", "");
        }
        String key = StringUtils.joinWith("::", Constant.RESDIS_PREFIX, Constant.DAILY);
        return (FpldleData) RedisUtils.getHashValue(key, date);
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

    @SuppressWarnings("unchecked")
    @Override
    public void insertDailyResult(String openId, String result) {
        // history
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern(Constant.SHORTDAY));
        String key = StringUtils.joinWith("::", Constant.RESDIS_PREFIX, Constant.RESULT, openId);
        Map<Integer, String> map = (Map<Integer, String>) RedisUtils.getHashValue(key, date);
        if (CollectionUtils.isEmpty(map)) {
            map = Maps.newHashMap();
        }
        int tryTimes = map.size() + 1;
        if (tryTimes > 6) {
            log.error("openId:{}, date:{}, tryTimes:{} more than 6", openId, date, tryTimes);
            return;
        }
        log.info("openId:{}, date:{}, tryTimes:{}, result:{}", openId, date, tryTimes, result);
        map.put(tryTimes, result);
        // redis
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        valueMap.put(date, map);
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getDailyResult(String openId, String date) {
        List<String> list = Lists.newArrayList();
        String key = StringUtils.joinWith("::", Constant.RESDIS_PREFIX, Constant.RESULT, openId);
        Map<String, String> valueMap = (Map<String, String>) RedisUtils.getHashValue(key, date);
        if (CollectionUtils.isEmpty(valueMap)) {
            log.info("openId:{}, date:{}, getDailyResult redis value empty", openId, date);
            return Lists.newArrayList();
        }
        IntStream.rangeClosed(1, 6).forEach(i -> {
            String result = valueMap.get(String.valueOf(i));
            if (StringUtils.isEmpty(result)) {
                log.info("openId:{}, date:{}, getDailyResult empty", openId, date);
                return;
            }
            list.add(result);
        });
        log.info("openId:{}, date:{}, getDailyResult size:{}", openId, date, list.size());
        return list;
    }

    @Override
    public String getPlayerPicture(int code) {
        // 考虑加一级缓存，或者先全量写到服务器
        return this.interfaceService.getPlayerPicture(code).orElse(null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<RecordData> getRecordList(String openId) {
        String key = StringUtils.joinWith("::", Constant.RESDIS_PREFIX, Constant.RESULT, openId);
        Map<String, Map<String, String>> hashMap = Maps.newHashMap();
        RedisUtils.getHashByKey(key).forEach((k, v) -> hashMap.put(k.toString(), (Map<String, String>) v));
        return hashMap.entrySet()
                .stream()
                .map(o ->
                        new RecordData()
                                .setOpenId(openId)
                                .setDate(o.getKey())
                                .setResult(this.getUserDailyLastResult(o.getValue()))
                                .setTryTimes(o.getValue().size())
                                .setSolve(this.userDailySolve(o.getKey(), o.getValue()))
                )
                .sorted(Comparator.comparing(RecordData::getDate))
                .collect(Collectors.toList());
    }

    private String getUserDailyLastResult(Map<String, String> resultMap) {
        String result = "";
        for (int i = 1; i < 7; i++) {
            String roundResult = resultMap.getOrDefault(String.valueOf(i), null);
            if (StringUtils.isNotEmpty(roundResult)) {
                result = roundResult.replaceAll(",", "");
            }
        }
        return result;
    }

    private boolean userDailySolve(String date, Map<String, String> resultMap) {
        // fpldle
        String fpldle = "";
        String key = StringUtils.joinWith("::", Constant.RESDIS_PREFIX, Constant.DAILY);
        FpldleData data = (FpldleData) RedisUtils.getHashValue(key, date);
        if (data != null) {
            fpldle = data.getName();
        }
        // compare
        for (int i = 1; i < 7; i++) {
            String roundResult = resultMap.getOrDefault(String.valueOf(i), null);
            if (StringUtils.isEmpty(roundResult)) {
                continue;
            }
            roundResult = roundResult.replaceAll(",", "");
            if (StringUtils.equalsIgnoreCase(fpldle, roundResult)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void insertDailyStatistic() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern(Constant.SHORTDAY));
        // get daily data
        Table<String, String, RecordData> userDaliyResultTable = HashBasedTable.create(); // openId -> date -> map(tryTimes -> result)
        String resultPatter = StringUtils.joinWith("::", Constant.RESDIS_PREFIX, Constant.RESULT);
        RedisUtils.getKeyPattern(resultPatter)
                .forEach(resultKey -> {
                    String openId = StringUtils.substringAfterLast(resultKey, "::");
                    RedisUtils.getHashByKey(resultKey).forEach((k, v) -> userDaliyResultTable.put(openId, k.toString(),
                            new RecordData()
                                    .setResult(this.getUserDailyLastResult((Map<String, String>) v))
                                    .setTryTimes(((Map<?, ?>) v).size())
                                    .setSolve(this.userDailySolve(k.toString(), (Map<String, String>) v))
                    ));
                });
        // user stat redis
        String userStatKey = StringUtils.joinWith("::", Constant.RESDIS_PREFIX, Constant.USER_STATISTIC);
        Map<String, Map<String, Object>> userCacheMap = Maps.newHashMap();
        // every user
        userDaliyResultTable.rowKeySet().forEach(openId -> {
            Map<String, Object> valueMap = Maps.newHashMap();
            RedisUtils.getHashByKey(userStatKey).forEach((k, v) -> valueMap.put(openId, v));
            Map<String, UserStatisticData> userStatMap = Maps.newHashMap();
            userStatMap.put(date, this.initStatisticData(openId, userDaliyResultTable.column(openId)));
            valueMap.put(openId, userStatMap);
            userCacheMap.put(userStatKey, valueMap);
        });
//        RedisUtils.pipelineHashCache(userCacheMap, -1, null);
        // date stat redis


    }

    private UserStatisticData initStatisticData(String openId, Map<String, RecordData> userRecordMap) {
//        return new UserStatisticData()
//                .setOpenId(openId)
//                .setTryTimes()
//                .setSolve()
//                .setTotalTryTimes()
//                .setTotalHitTimes()
//                .setConsecutiveGuessDays()
//                .setConsecutiveGuessRank()
//                .setConsecutiveHitDays()
//                .setConsecutiveHitRank();
        return null;
    }

}
