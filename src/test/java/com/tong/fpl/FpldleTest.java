package com.tong.fpl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.*;
import com.tong.fpl.service.IFpldleService;
import com.tong.fpl.util.RedisUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2022/2/17
 */
public class FpldleTest extends FpldleApplicationTests {

    @Autowired
    private IFpldleService fpldleService;

    @Test
    void insertFpldleDictionary() {
        this.fpldleService.insertFpldleDictionary();
    }

    @Test
    void getFpldleMap() {
        Map<String, FpldleData> map = this.fpldleService.getFpldleMap();
        System.out.println(1);
    }

    @Test
    void insertDailyFpldle() {
        this.fpldleService.insertDailyFpldle();
    }

    @ParameterizedTest
    @CsvSource({"2122, 448"})
    void insertDailyFpldleByElement(String season, int element) {
        this.fpldleService.insertDailyFpldleByElement(season, element);
    }

    @ParameterizedTest
    @CsvSource({"20220216"})
    void getDailyFpldle(String date) {
        FpldleData data = this.fpldleService.getDailyFpldle(date);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"odU8S48tpgD0fWUlo35-nwfYn7CE, 'K,A,N,E,X'"})
    void insertDailyResult(String openId, String result) {
        this.fpldleService.insertDailyResult(openId, result);
    }

    @ParameterizedTest
    @CsvSource({"o4SMe5I1MWAD5EegARCElLgDjyKQ, 20220218"})
    void getDailyResult(String openId, String date) {
        List<String> list = this.fpldleService.getDailyResult(openId, date);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"odU8S419zNFdTEBDVSRrjFS-roVU, 20220315"})
    void getDateVerifyList(String openId, String date) {
        List<String> list = this.fpldleService.getDateVerifyList(openId, date);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"15749"})
    void getPlayerPicture(int code) {
        String result = this.fpldleService.getPlayerPicture(code);
        System.out.println(result);
    }

    @ParameterizedTest
    @CsvSource({"o4SMe5I1MWAD5EegARCElLgDjyKQ"})
    void getRecordList(String openId) {
        List<RecordData> list = this.fpldleService.getRecordList(openId);
        System.out.println(1);
    }

    @Test
    void insertUserStatistic() {
        this.fpldleService.insertUserStatistic();
    }

    @Test
    void insertDateStatistic() {
        this.fpldleService.insertDateStatistic();
    }

    @Test
    void insertNickNameOpenIdRelations() {
        this.fpldleService.insertNickNameOpenIdRelations();
    }

    @Test
    void getHistoryFpldle() {
        List<FpldleHistoryData> list = this.fpldleService.getHistoryFpldle();
        System.out.println(1);
    }

    @Test
    void getWechatAccessToken() {
        String token = this.fpldleService.getWechatAccessToken();
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"021n4Mkl2dosK8435Hll22NgdJ2n4Mk7"})
    void getWechatUserOpenId(String code) {
        String openId = this.fpldleService.getWechatUserOpenId(code);
        System.out.println(1);
    }

    @Test
    void transfers() {
        // user
        Multimap<String, String> userMap = HashMultimap.create(); // user -> openId
        String userKey = StringUtils.joinWith("::", Constant.REDIS_PREFIX, Constant.USER);
        RedisUtils.getHashByKey(userKey).forEach((k, v) -> {
            String openId = k.toString();
            UserInfo userInfo = (UserInfo) v;
            if (userInfo == null) {
                return;
            }
            userMap.put(userInfo.getNickName(), openId);
        });
        // filter user
        Multimap<String, String> filterUserMap = HashMultimap.create(); // user -> openId
        userMap.keySet().forEach(nickName -> {
            if (userMap.get(nickName).size() > 1) {
                userMap.get(nickName).forEach(o -> filterUserMap.put(nickName, o));
            }
        });
        // result
        Map<String, Map<String, String>> resultMap = Maps.newHashMap(); // openId -> map(date -> result)
        RedisUtils.getKeyPattern(StringUtils.joinWith("::", Constant.REDIS_PREFIX, Constant.RESULT))
                .forEach(key -> {
                    Map<String, String> valueMap = Maps.newHashMap();
                    RedisUtils.getHashByKey(key).forEach((k, v) -> valueMap.put(k.toString(), v.toString()));
                    String openId = StringUtils.substringAfterLast(key, "::");
                    resultMap.put(openId, valueMap);
                });
        // copy
        Map<String, Map<String, String>> copyResultMap = Maps.newHashMap(); // openId -> map(date -> result)
        filterUserMap.keys().forEach(nickName -> {
            Map<String, String> dateMap = Maps.newHashMap();
            filterUserMap.get(nickName).forEach(openId -> {
                Map<String, String> openIdDateMap = resultMap.get(openId);
                if (CollectionUtils.isEmpty(openIdDateMap)) {
                    return;
                }
                openIdDateMap.keySet().forEach(date -> {
                    if (dateMap.containsKey(date)) {
                        return;
                    }
                    if (StringUtils.isEmpty(openIdDateMap.get(date))) {
                        return;
                    }
                    dateMap.put(date, openIdDateMap.get(date));
                });
                copyResultMap.put(openId, dateMap);
            });
        });
        // redis
        copyResultMap.keySet().forEach(openId -> {
            Map<String, String> dateMap = copyResultMap.get(openId);
            if (CollectionUtils.isEmpty(dateMap)) {
                return;
            }
            String key = StringUtils.joinWith("::", Constant.REDIS_PREFIX, Constant.RESULT, openId);
            Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
            Map<String, Object> valueMap = Maps.newHashMap();
            dateMap.keySet().forEach(date -> {
                valueMap.put(date, dateMap);
                cacheMap.put(key, valueMap);
            });
            RedisUtils.pipelineHashCache(cacheMap, -1, null);
        });
    }

    @ParameterizedTest
    @CsvSource({"Fpldle::Result::odU8S4yZQYBT0bFRzFfdzqGw2c3c"})
    void fix(String key) {
        Map<String, Map<String, String>> resultMap = Maps.newHashMap();
        RedisUtils.getHashByKey(key).forEach((k, v) -> resultMap.put(k.toString(), (Map<String, String>) v));
        Map<String, Object> valueMap = Maps.newHashMap();
        resultMap.keySet().forEach(date -> {
            String value = StringUtils.substringBetween(resultMap.get(date).get(date), "{", "}");
            Map<String, String> transValueMap = this.transValueMap(value);
            valueMap.put(date, transValueMap);
        });
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    @ParameterizedTest
    @CsvSource({"Fpldle::Result::odU8S40lnaBAPO1E_kDcfwcDsdiY"})
    void fix2(String key) {
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        RedisUtils.getHashByKey(key).forEach((k, v) -> {
            String value = StringUtils.substringBetween(v.toString(), "{", "}");
            Map<String, String> transValueMap = this.transValueMap(value);
            valueMap.put(k.toString(), transValueMap);
        });
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    private Map<String, String> transValueMap(String str) {
        Map<String, String> map = Maps.newHashMap();
        Map<Integer, Map<Integer, Integer>> indexMap = this.createIndexMap();
        int times = (str.length() + 2) / 13;
        for (int i = 1; i < times + 1; i++) {
            Map<Integer, Integer> subIndexMap = indexMap.get(i);
            int start = subIndexMap.keySet()
                    .stream()
                    .findFirst()
                    .orElse(-1);
            int end = subIndexMap.values()
                    .stream()
                    .findFirst()
                    .orElse(-1);
            if (start == -1 || end == -1) {
                continue;
            }
            String subStr = StringUtils.substringAfter(str.substring(start, end), "=");
            map.put(String.valueOf(i), subStr);
        }
        return map;
    }

    private Map<Integer, Map<Integer, Integer>> createIndexMap() {
        Map<Integer, Map<Integer, Integer>> map = Maps.newHashMap();
        Map<Integer, Integer> a = Maps.newHashMap();
        a.put(0, 11);
        map.put(1, a);
        Map<Integer, Integer> b = Maps.newHashMap();
        b.put(13, 24);
        map.put(2, b);
        Map<Integer, Integer> c = Maps.newHashMap();
        c.put(26, 37);
        map.put(3, c);
        Map<Integer, Integer> d = Maps.newHashMap();
        d.put(39, 50);
        map.put(4, d);
        Map<Integer, Integer> e = Maps.newHashMap();
        e.put(52, 63);
        map.put(5, e);
        Map<Integer, Integer> f = Maps.newHashMap();
        f.put(65, 76);
        map.put(6, f);
        return map;
    }

    @Test
    void insertDictionaryPictues() {
        this.fpldleService.insertDictionaryPictures();
    }

    @ParameterizedTest
    @CsvSource({"odU8S40lnaBAPO1E_kDcfwcDsdiY, GunnersRose, https://thirdwx.qlogo.cn/mmopen/vi_32/4kET70zdREBcgInTRBicFjficTXuOxLPzofibVtd0Rx9IIQic45sjsQ38NXAz2zLltVeHsQY1dDY3y7m0x37O8yGIg/132"})
    void insertUserInfo(String openId, String nickName, String avatarUrl) {
        this.fpldleService.insertUserInfo(openId, nickName, avatarUrl);
    }

    @ParameterizedTest
    @CsvSource({"0316"})
    void getLastDayHitRank(String date) {
        long start = System.currentTimeMillis();
        List<LastDayHitData> list = this.fpldleService.getLastDayHitRank(date);
        long end = System.currentTimeMillis();
        System.out.println((end - start) / 1000 + "ms");
    }

    @ParameterizedTest
    @CsvSource({"0314"})
    void getConsecutiveHitRank(String date) {
        long start = System.currentTimeMillis();
        List<ConsecutiveHitData> list = this.fpldleService.getConsecutiveHitRank(date);
        long end = System.currentTimeMillis();
        System.out.println((end - start) / 1000 + "ms");
    }

    @ParameterizedTest
    @CsvSource({"0314"})
    void getAverageHitTimesRank(String date) {
        long start = System.currentTimeMillis();
        List<AverageHitTimesData> list = this.fpldleService.getAverageHitTimesRank(date);
        long end = System.currentTimeMillis();
        System.out.println((end - start) / 1000 + "ms");
    }

    @ParameterizedTest
    @CsvSource({"EVANS"})
    void getFpldleByName(String name) {
        FpldleData data = this.fpldleService.getFpldleByName(name);
        System.out.println(1);
    }

}
