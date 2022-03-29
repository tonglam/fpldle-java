package com.tong.fpl.service.impl;

import cn.hutool.core.util.NumberUtil;
import com.google.common.collect.*;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.constant.enums.GuessResultEnum;
import com.tong.fpl.constant.enums.PositionEnum;
import com.tong.fpl.constant.enums.SeasonEnum;
import com.tong.fpl.domain.*;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.domain.wechat.AuthSessionData;
import com.tong.fpl.domain.wechat.AuthTokenData;
import com.tong.fpl.service.IFpldleService;
import com.tong.fpl.service.IInterfaceService;
import com.tong.fpl.service.IRedisCacheService;
import com.tong.fpl.util.CommonUtils;
import com.tong.fpl.util.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
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
                    .filter(o -> o.getEvent() == 1 && o.getSelected() > 10000)
                    .forEach(o -> {
                        int element = o.getElement();
                        PlayerEntity playerEntity = playerMap.getOrDefault(String.valueOf(element), null);
                        if (playerEntity == null || StringUtils.isEmpty(playerEntity.getWebName())) {
                            return;
                        }
                        String fpldle = this.getFpldleName(playerEntity.getWebName());
                        map.put(fpldle,
                                new FpldleData()
                                        .setElement(o.getElement())
                                        .setCode(o.getCode())
                                        .setName(fpldle)
                                        .setWebName(playerEntity.getWebName())
                                        .setFullName(StringUtils.joinWith(" ", playerEntity.getFirstName(), playerEntity.getSecondName()))
                                        .setSeason(season)
                                        .setPosition(PositionEnum.getNameFromElementType(playerEntity.getElementType()))
                                        .setTeamName(teamNameMap.getOrDefault(String.valueOf(o.getTeamId()), ""))
                                        .setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getTeamId()), "")));
                    });
        });
        // redis
        String key = StringUtils.joinWith("::", Constant.REDIS_PREFIX, Constant.DICTIONARY);
        RedisUtils.removeCacheByKey(key);
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        map.forEach((element, value) -> valueMap.put(String.valueOf(element), value));
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
        log.info("insert Fpldle dictionary size:{}", map.size());
    }

    private String getFpldleName(String webName) {
        webName = this.getFpldleWebName(webName);
        String name;
        int length = 5;
        if (webName.length() < length) {
            name = StringUtils.rightPad(webName, length, "x");
        } else {
            name = webName.substring(0, length);
        }
        return StringUtils.upperCase(name);
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
                .replaceAll("ß", "ss")
                .replaceAll("š", "s")
                .replaceAll("ä", "a")
                .replaceAll("ç", "c")
                .replaceAll("\\.", "")
                .replaceAll("ñ", "n")
                .replaceAll("ú", "u")
                .replaceAll("ã", "a");
    }

    @Override
    public Map<String, FpldleData> getFpldleMap() {
        String key = StringUtils.joinWith("::", Constant.REDIS_PREFIX, Constant.DICTIONARY);
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
        String key = StringUtils.joinWith("::", Constant.REDIS_PREFIX, Constant.DAILY);
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
        String key = StringUtils.joinWith("::", Constant.REDIS_PREFIX, Constant.DAILY);
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
        String key = StringUtils.joinWith("::", Constant.REDIS_PREFIX, Constant.DAILY);
        return (FpldleData) RedisUtils.getHashValue(key, date);
    }

    @Override
    public String getWechatUserOpenId(String code) {
        AuthSessionData data = this.interfaceService.getAuthSessionInfo(Constant.APP_ID, Constant.SECRET_ID, code).orElse(null);
        if (data == null) {
            return "";
        }
        return StringUtils.isEmpty(data.getOpenid()) ? data.getErrmsg() : data.getOpenid();
    }

    @Override
    public String getWechatAccessToken() {
        AuthTokenData data = this.interfaceService.getAuthTokenInfo(Constant.APP_ID, Constant.SECRET_ID).orElse(null);
        if (data == null) {
            return "";
        }
        return data.getAccessToken();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void insertDailyResult(String openId, String result) {
        if (StringUtils.isEmpty(openId) || openId.contains("invalid")) {
            return;
        }
        // history
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern(Constant.SHORTDAY));
        String key = StringUtils.joinWith("::", Constant.REDIS_PREFIX, Constant.RESULT, openId);
        Map<String, String> map = (Map<String, String>) RedisUtils.getHashValue(key, date);
        if (CollectionUtils.isEmpty(map)) {
            map = Maps.newHashMap();
        }
        // last one
        String lastResult = map.get(String.valueOf(map.size()));
        if (StringUtils.equals(lastResult, result)) {
            log.error("openId:{}, date:{}, result:{}, last_result:{} ,result repeat", openId, date, result, lastResult);
            return;
        }
        int tryTimes = map.size() + 1;
        if (tryTimes > 6) {
            log.error("openId:{}, date:{}, tryTimes:{} more than 6", openId, date, tryTimes);
            return;
        }
        log.info("openId:{}, date:{}, tryTimes:{}, result:{}", openId, date, tryTimes, result);
        map.put(String.valueOf(tryTimes), result);
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
        String key = StringUtils.joinWith("::", Constant.REDIS_PREFIX, Constant.RESULT, openId);
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

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getDateVerifyList(String openId, String date) {
        List<String> list = Lists.newArrayList();
        // result
        String resultKey = StringUtils.joinWith("::", Constant.REDIS_PREFIX, Constant.RESULT, openId);
        Map<String, String> valueMap = (Map<String, String>) RedisUtils.getHashValue(resultKey, date);
        if (CollectionUtils.isEmpty(valueMap)) {
            log.info("openId:{}, date:{}, getDailyResult redis value empty", openId, date);
            return Lists.newArrayList();
        }
        // daily
        String key = StringUtils.joinWith("::", Constant.REDIS_PREFIX, Constant.DAILY);
        FpldleData data = (FpldleData) RedisUtils.getHashValue(key, date);
        if (data == null || StringUtils.isEmpty(data.getName())) {
            log.info("date:{}, daily fpldle empty", date);
            return Lists.newArrayList();
        }
        String fpldle = data.getName();
        char[] fpldleList = fpldle.toCharArray();
        // verify
        valueMap.values().forEach(result -> {
            String[] roundResult = result.split(",");
            for (int i = 0; i < roundResult.length; i++) {
                String letter = roundResult[i];
                if (StringUtils.equals(letter, Character.toString(fpldleList[i]))) {
                    list.add(String.valueOf(GuessResultEnum.CORRECT.getResult()));
                } else if (fpldle.contains(letter)) {
                    list.add(String.valueOf(GuessResultEnum.ORDER.getResult()));
                } else {
                    list.add(String.valueOf(GuessResultEnum.WRONG.getResult()));
                }
            }
        });
        return list;
    }

    @Override
    public String getPlayerPicture(int code) {
        String key = StringUtils.joinWith("::", Constant.REDIS_PREFIX, Constant.PICTURE);
        String picture = (String) RedisUtils.getHashValue(key, String.valueOf(code));
        if (StringUtils.isEmpty(picture)) {
            return null;
        }
        return picture;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void insertUserStatistic() {
        // get daily data
        Table<String, String, RecordData> userDailyResultTable = HashBasedTable.create(); // openId -> date -> map(tryTimes -> result)
        String resultPattern = StringUtils.joinWith("::", Constant.REDIS_PREFIX, Constant.RESULT);
        // openId list
        List<String> openIdList = Lists.newArrayList();
        RedisUtils.getKeyPattern(resultPattern).forEach(resultKey -> {
            openIdList.add(StringUtils.substringAfterLast(resultKey, "::"));
        });
        if (CollectionUtils.isEmpty(openIdList)) {
            return;
        }
        // openId result
        openIdList.forEach(openId -> {
            String resultKey = StringUtils.joinWith("::", resultPattern, openId);
            RedisUtils.getHashByKey(resultKey).forEach((k, v) ->
                    userDailyResultTable.put(openId, k.toString(),
                            new RecordData()
                                    .setDate(k.toString())
                                    .setResult(this.getUserDailyLastResult((Map<String, String>) v))
                                    .setTryTimes(((Map<?, ?>) v).size())
                                    .setSolve(this.userDailySolve(k.toString(), (Map<String, String>) v))));
        });
        // get user
        Map<String, UserInfo> userInfoMap = Maps.newHashMap(); // openId -> userInfo
        String userKey = StringUtils.joinWith("::", Constant.REDIS_PREFIX, Constant.USER);
        RedisUtils.getHashByKey(userKey).forEach((k, v) -> userInfoMap.put(k.toString(), (UserInfo) v));
        // user stat redis
        String key = StringUtils.joinWith("::", Constant.REDIS_PREFIX, Constant.USER_STATISTIC);
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        // every user
        userDailyResultTable.rowKeySet().forEach(openId -> {
            Map<String, UserStatisticData> statMap = Maps.newHashMap();
            // every date
            for (String statDate : userDailyResultTable.columnKeySet()) {
                UserStatisticData data = this.initUserStatisticData(openId, statDate, userDailyResultTable.row(openId), userInfoMap);
                if (data == null) {
                    continue;
                }
                statMap.put(statDate, data);
                valueMap.put(openId, statMap);
            }
        });
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    private UserStatisticData initUserStatisticData(String openId, String date, Map<String, RecordData> userRecordMap, Map<String, UserInfo> userInfoMap) {
        LocalDate localDate = LocalDate.parse(CommonUtils.getDateFromShortDay(date)).plusDays(1);
        List<RecordData> recordList = userRecordMap.values()
                .stream()
                .filter(o -> LocalDate.parse(CommonUtils.getDateFromShortDay(o.getDate())).isBefore(localDate))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(recordList)) {
            return null;
        }
        String lastDate = recordList
                .stream()
                .sorted(Comparator.comparing(RecordData::getDate).reversed())
                .map(RecordData::getDate)
                .findFirst()
                .orElse("");
        if (StringUtils.isEmpty(lastDate)) {
            return null;
        }
        RecordData dateData = userRecordMap.getOrDefault(date, null);
        if (dateData == null) {
            return null;
        }
        UserInfo userInfo = userInfoMap.getOrDefault(openId, new UserInfo());
        UserStatisticData data = new UserStatisticData()
                .setOpenId(openId)
                .setNickName(userInfo.getNickName())
                .setAvatarUrl(userInfo.getAvatarUrl())
                .setTryTimes(dateData.getTryTimes())
                .setTotalGuessDays(recordList.size())
                .setSolve(dateData.isSolve())
                .setTotalTryTimes(
                        recordList
                                .stream()
                                .mapToInt(RecordData::getTryTimes)
                                .sum()
                )
                .setTotalHitTimes(
                        recordList
                                .stream()
                                .mapToInt(o -> {
                                    if (o.isSolve()) {
                                        return 1;
                                    }
                                    return 0;
                                }).sum()
                );
        ConsecutiveData guessData = this.calcConsecutiveGuessDays(lastDate,
                recordList
                        .stream()
                        .map(RecordData::getDate)
                        .collect(Collectors.toList())
        );
        if (guessData != null) {
            data
                    .setConsecutiveGuessDays(guessData.getDays())
                    .setConsecutiveGuessStartDay(guessData.getStartDay())
                    .setConsecutiveGuessEndDay(guessData.getEndDay());
        }
        ConsecutiveData hitData = this.calcConsecutiveHitDays(lastDate,
                recordList
                        .stream()
                        .filter(RecordData::isSolve)
                        .map(RecordData::getDate)
                        .collect(Collectors.toList())
        );
        if (hitData != null) {
            data
                    .setConsecutiveHitDays(hitData.getDays())
                    .setConsecutiveHitStartDay(hitData.getStartDay())
                    .setConsecutiveHitEndDay(hitData.getEndDay());
        }
        return data;
    }

    private ConsecutiveData calcConsecutiveGuessDays(String lastDate, List<String> guessDaysList) {
        int consecutiveGuessDays = 0;
        String consecutiveGuessStartDay = lastDate;
        for (int i = 0; i < guessDaysList.size(); i++) {
            String nextDate = LocalDate.parse(CommonUtils.getDateFromShortDay(lastDate)).minusDays(i).format(DateTimeFormatter.ofPattern(Constant.SHORTDAY));
            if (guessDaysList.contains(nextDate)) {
                consecutiveGuessStartDay = nextDate;
                consecutiveGuessDays++;
            } else {
                break;
            }
        }
        return new ConsecutiveData()
                .setDays(consecutiveGuessDays)
                .setStartDay(consecutiveGuessStartDay)
                .setEndDay(lastDate);
    }

    private ConsecutiveData calcConsecutiveHitDays(String lastDate, List<String> hitDaysList) {
        int consecutiveHitDays = 0;
        String consecutiveHitStartDay = lastDate;
        for (int i = 0; i < hitDaysList.size(); i++) {
            String nextDate = LocalDate.parse(CommonUtils.getDateFromShortDay(lastDate)).minusDays(i).format(DateTimeFormatter.ofPattern(Constant.SHORTDAY));
            if (hitDaysList.contains(nextDate)) {
                consecutiveHitStartDay = nextDate;
                consecutiveHitDays++;
            } else {
                break;
            }
        }
        return new ConsecutiveData()
                .setDays(consecutiveHitDays)
                .setStartDay(consecutiveHitStartDay)
                .setEndDay(lastDate);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void insertDateStatistic() {
        // get data
        Table<String, String, RecordData> dateResultTable = HashBasedTable.create(); // date -> openId -> map(tryTimes -> result)
        String resultPatter = StringUtils.joinWith("::", Constant.REDIS_PREFIX, Constant.RESULT);
        RedisUtils.getKeyPattern(resultPatter).forEach(resultKey -> {
            String openId = StringUtils.substringAfterLast(resultKey, "::");
            RedisUtils.getHashByKey(resultKey).forEach((k, v) -> dateResultTable.put(k.toString(), openId,
                    new RecordData()
                            .setDate(k.toString())
                            .setResult(this.getUserDailyLastResult((Map<String, String>) v))
                            .setTryTimes(((Map<?, ?>) v).size())
                            .setSolve(this.userDailySolve(k.toString(), (Map<String, String>) v))));
        });
        // date stat redis
        String key = StringUtils.joinWith("::", Constant.REDIS_PREFIX, Constant.DATE_STATISTIC);
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        // every date
        dateResultTable.rowKeySet().forEach(date -> {
            RedisUtils.getHashByKey(key).forEach((k, v) -> valueMap.put(date, v));
            valueMap.put(date, this.initDateStatisticData(date, dateResultTable.row(date)));
        });
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    private DateStatisticData initDateStatisticData(String date, Map<String, RecordData> dateRecordMap) {
        List<RecordData> recordList = dateRecordMap.values()
                .stream()
                .filter(o -> StringUtils.equals(date, o.getDate()))
                .collect(Collectors.toList());
        DateStatisticData data = new DateStatisticData()
                .setDate(date)
                .setTotalUsers(recordList.size())
                .setTotalHitUsers(
                        recordList
                                .stream()
                                .mapToInt(o -> {
                                    if (o.isSolve()) {
                                        return 1;
                                    }
                                    return 0;
                                })
                                .sum()
                )
                .setTotalTryTimes(
                        recordList
                                .stream()
                                .mapToInt(RecordData::getTryTimes)
                                .sum()
                );
        if (data.getTotalHitUsers() > 0 && data.getTotalUsers() > 0) {
            data.setUserHitRate(NumberUtil.formatPercent(NumberUtil.div(data.getTotalHitUsers(), data.getTotalUsers()), 1))
                    .setAverageTryTimes(NumberUtil.div(data.getTotalTryTimes(), data.getTotalUsers(), 2))
                    .setAverageHitTimes(
                            NumberUtil.div(
                                    recordList
                                            .stream()
                                            .filter(RecordData::isSolve)
                                            .mapToInt(RecordData::getTryTimes)
                                            .sum(),
                                    data.getTotalHitUsers(), 2));
        }
        return data;
    }

    @Override
    public void insertUserInfo(String openId, String nickName, String avatarUrl) {
        String key = StringUtils.joinWith("::", Constant.REDIS_PREFIX, Constant.USER);
        // exist
        if (Objects.nonNull(RedisUtils.getHashValue(key, openId))) {
            return;
        }
        // insert
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        RedisUtils.getHashByKey(key).forEach((k, v) -> valueMap.put(k.toString(), v));
        valueMap.put(openId,
                new UserInfo()
                        .setOpenId(openId)
                        .setNickName(nickName)
                        .setAvatarUrl(avatarUrl)
        );
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    @Override
    public void insertNickNameOpenIdRelations() {
        Multimap<String, String> relationMap = HashMultimap.create();
        // user_info
        String userInfoKey = StringUtils.joinWith("::", Constant.REDIS_PREFIX, Constant.USER);
        RedisUtils.getHashByKey(userInfoKey).forEach((k, v) -> {
            String openId = k.toString();
            UserInfo userInfo = (UserInfo) v;
            if (userInfo == null) {
                return;
            }
            String nickName = userInfo.getNickName();
            relationMap.put(nickName, openId);

        });
        String key = StringUtils.joinWith("::", Constant.REDIS_PREFIX, Constant.USER_RELATION);
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        relationMap.keys().forEach(nickName -> {
            List<String> openIdList = new ArrayList<>(relationMap.get(nickName));
            valueMap.put(nickName, openIdList);
        });
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    @Override
    public List<FpldleHistoryData> getHistoryFpldle() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern(Constant.SHORTDAY));
        Map<String, FpldleData> valueMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", Constant.REDIS_PREFIX, Constant.DAILY);
        RedisUtils.getHashByKey(key).forEach((k, v) -> valueMap.put(k.toString(), (FpldleData) v));
        return valueMap.keySet().stream().filter(o -> !StringUtils.equals(today, o)).map(date -> {
                    FpldleData data = valueMap.get(date);
                    if (data == null) {
                        return null;
                    }
                    return new FpldleHistoryData()
                            .setDate(date)
                            .setElement(data.getElement())
                            .setCode(data.getCode())
                            .setName(data.getName())
                            .setFullName(data.getFullName())
                            .setSeason(data.getSeason())
                            .setPosition(data.getPosition())
                            .setTeamName(data.getTeamName())
                            .setTeamShortName(data.getTeamShortName());
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(FpldleHistoryData::getDate).reversed())
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<RecordData> getRecordList(String openId) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern(Constant.SHORTDAY));
        String key = StringUtils.joinWith("::", Constant.REDIS_PREFIX, Constant.RESULT, openId);
        Map<String, Map<String, String>> hashMap = Maps.newHashMap();
        RedisUtils.getHashByKey(key).forEach((k, v) -> hashMap.put(k.toString(), (Map<String, String>) v));
        return hashMap.entrySet()
                .stream()
                .filter(o -> !StringUtils.equals(today, o.getKey()))
                .map(o ->
                        new RecordData()
                                .setOpenId(openId)
                                .setDate(o.getKey())
                                .setResult(this.getUserDailyLastResult(o.getValue()))
                                .setTryTimes(o.getValue().size())
                                .setSolve(this.userDailySolve(o.getKey(), o.getValue()))
                )
                .sorted(Comparator.comparing(RecordData::getDate).reversed())
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
        String key = StringUtils.joinWith("::", Constant.REDIS_PREFIX, Constant.DAILY);
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
    public void insertDictionaryPictures() {
        // get dictionary
        String dictionaryKey = StringUtils.joinWith("::", Constant.REDIS_PREFIX, Constant.DICTIONARY);
        Map<String, FpldleData> dictionaryMap = Maps.newHashMap();
        RedisUtils.getHashByKey(dictionaryKey).forEach((k, v) -> dictionaryMap.put(k.toString(), (FpldleData) v));
        if (CollectionUtils.isEmpty(dictionaryMap)) {
            log.info("getDictionary redis value empty");
            return;
        }
        // code list
        List<Integer> codeList = dictionaryMap.values()
                .stream()
                .map(FpldleData::getCode)
                .collect(Collectors.toList());
        // get picture
        ForkJoinPool forkJoinPool = new ForkJoinPool(10);
        List<CompletableFuture<Map<String, String>>> future = codeList
                .stream()
                .map(o -> CompletableFuture.supplyAsync(() -> this.getPicture(o), forkJoinPool))
                .collect(Collectors.toList());
        List<Map<String, String>> pictureMapList = future
                .stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        // redis
        String key = StringUtils.joinWith("::", Constant.REDIS_PREFIX, Constant.PICTURE);
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        pictureMapList.forEach(o -> o.keySet().forEach(code -> {
            String base64String = o.getOrDefault(code, null);
            if (StringUtils.isEmpty(base64String)) {
                return;
            }
            valueMap.put(code, base64String);
        }));
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    private Map<String, String> getPicture(int code) {
        Optional<String> result = this.interfaceService.getPlayerPicture(code);
        if (result.isPresent()) {
            Map<String, String> map = Maps.newHashMap();
            map.put(String.valueOf(code), result.get());
            return map;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<LastDayHitData> getLastDayHitRank() {
        // get last day map
        String key = StringUtils.joinWith("::", Constant.REDIS_PREFIX, Constant.USER_STATISTIC);
        String lastDay = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern(Constant.SHORTDAY));
        Map<String, UserStatisticData> lastDayHitMap = Maps.newHashMap();
        RedisUtils.getHashByKey(key).forEach((k, v) -> {
            String openId = k.toString();
            Map<String, UserStatisticData> userStatisticMap = (Map<String, UserStatisticData>) v;
            UserStatisticData data = userStatisticMap.keySet()
                    .stream()
                    .filter(o -> StringUtils.equals(lastDay, o))
                    .map(o -> userStatisticMap.getOrDefault(o, null))
                    .findFirst()
                    .orElse(null);
            if (data == null || StringUtils.isEmpty(data.getNickName()) || data.getConsecutiveHitDays() <= 0) {
                return;
            }
            lastDayHitMap.put(openId, data);
        });
        // sort rank
        Map<Integer, Integer> rankMap = this.sortLastDayHitRank(new ArrayList<>(lastDayHitMap.values())); // openId -> rank
        // return list
        List<LastDayHitData> list = Lists.newArrayList();
        lastDayHitMap.forEach((openId, data) -> list.add(
                        new LastDayHitData()
                                .setRank(rankMap.get(data.getTryTimes()))
                                .setOpenId(openId)
                                .setNickName(data.getNickName())
                                .setAvatarUrl(data.getAvatarUrl())
                                .setTryTimes(data.getTryTimes())
                )
        );
        return list
                .stream()
                .sorted(Comparator.comparing(LastDayHitData::getRank).thenComparing(LastDayHitData::getNickName))
                .collect(Collectors.toList());
    }

    private Map<Integer, Integer> sortLastDayHitRank(List<UserStatisticData> list) {
        Map<Integer, Integer> rankMap = Maps.newHashMap();
        Map<Integer, Integer> rankCountMap = Maps.newLinkedHashMap();
        list
                .stream()
                .sorted(Comparator.comparing(UserStatisticData::getTryTimes))
                .forEachOrdered(o -> this.setRankMapValue(o.getTryTimes(), rankCountMap));
        int index = 1;
        for (Integer key : rankCountMap.keySet()) {
            rankMap.put(key, index);
            index += rankCountMap.get(key);
        }
        return rankMap;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ConsecutiveHitData> getConsecutiveHitRank() {
        String date = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern(Constant.SHORTDAY));
        // get consecutive map
        Map<String, UserStatisticData> consecutiveMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", Constant.REDIS_PREFIX, Constant.USER_STATISTIC);
        RedisUtils.getHashByKey(key).forEach((k, v) -> {
            String openId = k.toString();
            Map<String, UserStatisticData> userStatisticMap = (Map<String, UserStatisticData>) v;
            UserStatisticData data = userStatisticMap.keySet()
                    .stream()
                    .filter(o -> StringUtils.equals(date, o))
                    .map(o -> userStatisticMap.getOrDefault(o, null))
                    .findFirst()
                    .orElse(null);
            if (data == null || StringUtils.isEmpty(data.getNickName()) || data.getConsecutiveHitDays() <= 0) {
                return;
            }
            consecutiveMap.put(openId, data);
        });
        // sort rank
        Map<Integer, Integer> rankMap = this.sortConsecutiveHitRank(new ArrayList<>(consecutiveMap.values())); // openId -> rank
        // return list
        List<ConsecutiveHitData> list = Lists.newArrayList();
        consecutiveMap.forEach((openId, data) -> list.add(
                        new ConsecutiveHitData()
                                .setRank(rankMap.get(data.getConsecutiveHitDays()))
                                .setOpenId(openId).setNickName(data.getNickName())
                                .setAvatarUrl(data.getAvatarUrl())
                                .setConsecutiveDays(data.getConsecutiveHitDays())
                                .setConsecutiveStartDay(data.getConsecutiveHitStartDay())
                                .setConsecutiveEndDay(data.getConsecutiveHitEndDay())
                )
        );
        return list
                .stream()
                .sorted(Comparator.comparing(ConsecutiveHitData::getRank).thenComparing(ConsecutiveHitData::getNickName))
                .collect(Collectors.toList());
    }

    private Map<Integer, Integer> sortConsecutiveHitRank(List<UserStatisticData> list) {
        Map<Integer, Integer> rankMap = Maps.newHashMap();
        Map<Integer, Integer> rankCountMap = Maps.newLinkedHashMap();
        list
                .stream()
                .sorted(Comparator.comparing(UserStatisticData::getConsecutiveHitDays).reversed())
                .forEachOrdered(o -> this.setRankMapValue(o.getConsecutiveHitDays(), rankCountMap));
        int index = 1;
        for (Integer key : rankCountMap.keySet()) {
            rankMap.put(key, index);
            index += rankCountMap.get(key);
        }
        return rankMap;
    }

    private void setRankMapValue(Integer key, Map<Integer, Integer> rankCountMap) {
        if (rankCountMap.containsKey(key)) {
            rankCountMap.put(key, rankCountMap.get(key) + 1);
        } else {
            rankCountMap.put(key, 1);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<AverageHitTimesData> getAverageHitTimesRank() {
        // get hit times map
        Multimap<String, UserStatisticData> hitTimesMultiMap = HashMultimap.create();
        String key = StringUtils.joinWith("::", Constant.REDIS_PREFIX, Constant.USER_STATISTIC);
        RedisUtils.getHashByKey(key).forEach((k, v) -> {
            String openId = k.toString();
            Map<String, UserStatisticData> userStatisticMap = (Map<String, UserStatisticData>) v;
            // remove today
            userStatisticMap.remove(LocalDate.now().format(DateTimeFormatter.ofPattern(Constant.SHORTDAY)));
            // last day
            String lastDay = userStatisticMap.keySet()
                    .stream()
                    .max(Comparator.naturalOrder())
                    .orElse(null);
            if (StringUtils.isEmpty(lastDay)) {
                return;
            }
            // hit times multiMap
            userStatisticMap.values()
                    .stream()
                    .filter(UserStatisticData::isSolve)
                    .forEach(o -> hitTimesMultiMap.put(openId, o));
        });
        // get average hit times list
        List<AverageHitTimesData> list = hitTimesMultiMap.keySet().stream().map(openId -> {
                    List<UserStatisticData> userStatisticList = new ArrayList<>(hitTimesMultiMap.get(openId));
                    if (CollectionUtils.isEmpty(userStatisticList)) {
                        return null;
                    }
                    UserStatisticData first = userStatisticList
                            .stream()
                            .findFirst()
                            .orElse(null);
                    if (first == null || StringUtils.isEmpty(first.getNickName())) {
                        return null;
                    }
                    return new AverageHitTimesData()
                            .setRank(0)
                            .setOpenId(openId)
                            .setNickName(first.getNickName())
                            .setAvatarUrl(first.getAvatarUrl())
                            .setHitTimes(
                                    (int) userStatisticList
                                            .stream()
                                            .filter(UserStatisticData::isSolve)
                                            .count()
                            )
                            .setAverageHitTimes(
                                    NumberUtil.div(
                                            userStatisticList
                                                    .stream()
                                                    .mapToInt(UserStatisticData::getTryTimes)
                                                    .sum(),
                                            userStatisticList.size(), 1)
                            );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        // sort rank
        Map<String, Integer> rankMap = this.sortHitTimesRank(list);
        // return list
        list.forEach(o -> o.setRank(rankMap.get(StringUtils.joinWith("-", o.getHitTimes(), o.getAverageHitTimes()))));
        return list
                .stream()
                .filter(o -> StringUtils.isNotEmpty(o.getNickName()))
                .sorted(Comparator.comparing(AverageHitTimesData::getRank).thenComparing(AverageHitTimesData::getNickName))
                .collect(Collectors.toList());
    }

    private Map<String, Integer> sortHitTimesRank(List<AverageHitTimesData> list) {
        Map<String, Integer> rankMap = Maps.newHashMap();
        Map<String, Integer> rankCountMap = Maps.newLinkedHashMap();
        list
                .stream()
                .sorted(
                        Comparator.comparing(AverageHitTimesData::getHitTimes).reversed()
                                .thenComparing(AverageHitTimesData::getAverageHitTimes)
                )
                .forEachOrdered(o -> this.setHitTimeRankMapValue(StringUtils.joinWith("-", o.getHitTimes(), o.getAverageHitTimes()), rankCountMap));
        int index = 1;
        for (String key : rankCountMap.keySet()) {
            rankMap.put(key, index);
            index += rankCountMap.get(key);
        }
        return rankMap;
    }

    private void setHitTimeRankMapValue(String key, Map<String, Integer> rankCountMap) {
        if (rankCountMap.containsKey(key)) {
            rankCountMap.put(key, rankCountMap.get(key) + 1);
        } else {
            rankCountMap.put(key, 1);
        }
    }

    @Override
    public FpldleData getFpldleByName(String name) {
        String key = StringUtils.joinWith("::", Constant.REDIS_PREFIX, Constant.DAILY);
        Map<String, FpldleData> map = Maps.newHashMap(); // name -> data
        RedisUtils.getHashByKey(key).forEach((k, v) -> {
            FpldleData data = (FpldleData) v;
            map.put(data.getName(), data);
        });
        return map.getOrDefault(name, null);
    }

}
