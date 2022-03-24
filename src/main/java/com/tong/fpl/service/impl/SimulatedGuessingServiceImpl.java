package com.tong.fpl.service.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.NumberUtil;
import com.google.common.collect.*;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.FpldleData;
import com.tong.fpl.domain.FpldleHistoryData;
import com.tong.fpl.service.IFpldleService;
import com.tong.fpl.service.ISimulatedGuessingService;
import com.tong.fpl.util.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by tong on 2022/03/22
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SimulatedGuessingServiceImpl implements ISimulatedGuessingService {

    private final IFpldleService fpldleService;

    /**
     * 1.find first guess which don't have the same letter
     */
    @Override
    public String simulate(String date) {
        int maxTimes = Constant.maxTryTimes;
        // daily
        FpldleData fpldleData = this.fpldleService.getDailyFpldle(date);
        if (fpldleData == null) {
            log.error("date:{}, fpldle empty", date);
            return "";
        }
        String fpldle = fpldleData.getName();
        // fpldle map
        Map<Integer, String> fpldleMap = Maps.newHashMap(); // position -> letter
        char[] fpldleChars = fpldle.toCharArray();
        for (int i = 0; i < fpldleChars.length; i++) {
            fpldleMap.put(i, String.valueOf(fpldleChars[i]));
        }
        // prepare
        Map<Integer, String> hitMap = Maps.newHashMap();
        Multimap<String, Integer> orderMultiMap = HashMultimap.create();
        List<String> excludeList = Lists.newArrayList();
        // get dictionary
        Map<String, FpldleData> dictionaryMap = this.fpldleService.getFpldleMap();
        // history
        List<Integer> historyCodeList = this.fpldleService.getHistoryFpldle()
                .stream()
                .filter(o -> LocalDate.parse(CommonUtils.getDateFromShortDay(o.getDate())).isBefore(LocalDate.parse(CommonUtils.getDateFromShortDay(date))))
                .map(FpldleHistoryData::getCode)
                .collect(Collectors.toList());
        // candidate
        List<String> candidateList = dictionaryMap.values()
                .stream()
                .filter(o -> !historyCodeList.contains(o.getCode()))
                .map(FpldleData::getName)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(candidateList)) {
            log.error("date:{}. no guessing candidate list", date);
            return "";
        }
        // word-letter
        BiMap<String, List<String>> wordLetterMap = HashBiMap.create();
        candidateList.forEach(o -> wordLetterMap.put(o, CommonUtils.word2Letter(o)));
        BiMap<List<String>, String> letterWordMap = wordLetterMap.inverse();
        // position-letter
        Table<Integer, String, List<String>> positionLetterTable = this.createPositionTable(candidateList);
        // first guess
        List<String> firstGuessList = this.getFirstGuessList(positionLetterTable);
        if (CollectionUtils.isEmpty(firstGuessList)) {
            log.error("simulate guess, first guess empty");
            return "";
        }
        String firstGuess = letterWordMap.get(firstGuessList);
        log.info("simulate guess, first guess:{}", firstGuess);
        // first verify
        this.verifyGuess(fpldle, fpldleMap, firstGuessList, hitMap, orderMultiMap, excludeList);
        // cut short first candidate
        candidateList = this.getCutShortCandidate(hitMap, orderMultiMap, excludeList, firstGuess, candidateList, wordLetterMap);
        if (CollectionUtils.isEmpty(candidateList)) {
            log.info("finish simulate guess, guess times:{}, remain empty, answer:{}", 1, firstGuess);
            return firstGuess;
        }
        if (candidateList.size() == 1) {
            String guess = candidateList.get(0);
            log.info("finish simulate guess, guess times:{}, answer:{}", 2, guess);
            return guess;
        }
        // simulate next guess
        for (int i = 2; i < maxTimes + 1; i++) {
            List<String> nextGuessList = this.simulateNextGuess(orderMultiMap, candidateList);
            if (CollectionUtils.isEmpty(nextGuessList)) {
                log.error("simulate guess, guess times:{}, guess empty", i);
                return "";
            }
            String nextGuess = letterWordMap.get(nextGuessList);
            log.info("simulate guess, guess times:{}, guess:{}, remain size:{}", i, nextGuess, candidateList.size());
            this.verifyGuess(fpldle, fpldleMap, nextGuessList, hitMap, orderMultiMap, excludeList);
            // cut short next candidate
            candidateList = this.getCutShortCandidate(hitMap, orderMultiMap, excludeList, nextGuess, candidateList, wordLetterMap);
            if (CollectionUtils.isEmpty(candidateList)) {
                log.info("finish simulate guess, guess times:{}, remain empty, answer:{}", i, nextGuess);
                return nextGuess;
            }
            if (candidateList.size() == 1) {
                String guess = candidateList.get(0);
                log.info("finish simulate guess, guess times:{}, answer:{}", i + 1, guess);
                return guess;
            }
            log.info("simulate guess, guess times:{}, guess:{}, cut short remain size:{}", i, nextGuess, candidateList.size());
        }
        // none match
        return candidateList.get(0);
    }

    private Table<Integer, String, List<String>> createPositionTable(List<String> list) {
        Table<Integer, String, List<String>> table = HashBasedTable.create(); // position -> letter -> word
        list
                .forEach(name -> {
                    Assert.notEmpty(name);
                    char[] letters = name.toCharArray();
                    for (int i = 0; i < letters.length; i++) {
                        String letter = String.valueOf(letters[i]);
                        List<String> wordList = Lists.newArrayList();
                        if (table.contains(i, letter)) {
                            wordList.addAll(Objects.requireNonNull(table.get(i, letter)));
                        }
                        wordList.add(name);
                        table.put(i, letter, wordList);
                    }
                });
        return table;
    }

    private List<String> getFirstGuessList(Table<Integer, String, List<String>> positionLetterTable) {
        // first guess
        List<String> firstGuessList = Lists.newArrayList();
        positionLetterTable.rowKeySet().forEach(position -> {
            String letter = positionLetterTable.row(position).entrySet()
                    .stream()
                    .filter(o -> !firstGuessList.contains(o.getKey()))
                    .max(Comparator.comparing(o -> o.getValue().size()))
                    .map(Map.Entry::getKey)
                    .orElse("");
            if (StringUtils.isEmpty(letter)) {
                return;
            }
            firstGuessList.add(letter);
        });
        return firstGuessList;
    }

    private void verifyGuess(String fpldle, Map<Integer, String> fpldleMap, List<String> guessList, Map<Integer, String> hitMap, Multimap<String, Integer> orderMultiMap, List<String> excludeList) {
        for (int i = 0; i < guessList.size(); i++) {
            String fpldleLetter = fpldleMap.get(i);
            if (StringUtils.isEmpty(fpldleLetter)) {
                continue;
            }
            String letter = guessList.get(i);
            if (StringUtils.equals(fpldleLetter, letter)) {
                hitMap.put(i, letter);
                // remove from orderMap if orderMap contains the letter
                if (orderMultiMap.containsKey(letter)) {
                    orderMultiMap.removeAll(letter);
                }
            } else if (fpldle.contains(letter)) {
                for (int j = 0; j < 5; j++) {
                    if (i == j) {
                        continue;
                    }
                    orderMultiMap.put(letter, j);
                }
            } else {
                excludeList.add(letter);
            }
        }
    }

    private List<String> getCutShortCandidate(Map<Integer, String> hitMap, Multimap<String, Integer> orderMultiMap, List<String> excludeList,
                                              String guess, List<String> candidateList, BiMap<String, List<String>> wordLetterMap) {
        // cut short candidate list
        if (!CollectionUtils.isEmpty(candidateList)) {
            candidateList = candidateList
                    .stream()
                    .filter(o -> !StringUtils.equals(guess, o) && StringUtils.containsNone(o, excludeList.toString()))
                    .collect(Collectors.toList());
        }
        // contain letters from hit and order
        List<String> containList = Stream.concat(hitMap.values().stream(), orderMultiMap.keys().stream())
                .distinct()
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(containList)) {
            candidateList = candidateList
                    .stream()
                    .filter(o -> {
                        boolean check = true;
                        for (String containLetter :
                                containList) {
                            if (!o.contains(containLetter)) {
                                check = false;
                                break;
                            }
                        }
                        return check;
                    })
                    .collect(Collectors.toList());
        }
        // cut short by hit letter
        if (!CollectionUtils.isEmpty(hitMap)) {
            for (Integer position :
                    hitMap.keySet()) {
                String letter = hitMap.get(position);
                candidateList = candidateList
                        .stream()
                        .filter(o -> StringUtils.equals(letter, wordLetterMap.get(o).get(position)))
                        .collect(Collectors.toList());
            }
        }
        if (orderMultiMap.size() != 0) {
            // cut short orderMultiMap
            Table<Integer, String, List<String>> positionCandidateTable = this.createPositionTable(candidateList); // position -> letter -> list
            String[] orderLetters = orderMultiMap.keySet().toArray(new String[0]);
            for (String letter : orderLetters) {
                Integer[] positions = orderMultiMap.get(letter).toArray(new Integer[0]);
                for (int position : positions) {
                    if (!positionCandidateTable.contains(position, letter)) {
                        orderMultiMap.remove(letter, position);
                    }
                }
            }
        }
        return candidateList;
    }

    private List<String> simulateNextGuess(Multimap<String, Integer> orderMultiMap, List<String> candidateList) {
        if (orderMultiMap.size() == 0) {
            return CommonUtils.word2Letter(candidateList.get(0));
        }
        Map<String, Double> wordPositionScoreMap = Maps.newHashMap(); // word -> score
        Table<String, Integer, Double> letterPositionScoreTable = HashBasedTable.create(); // letter -> position -> score
        Table<Integer, String, List<String>> positionCandidateTable = this.createPositionTable(candidateList); // position -> letter -> list
        // collect letter score
        for (String letter :
                orderMultiMap.keySet()) {
            Map<Integer, Integer> positionTimesMap = Maps.newHashMap(); // position -> times
            orderMultiMap.get(letter).forEach(position -> {
                int times = Objects.requireNonNull(positionCandidateTable.get(position, letter)).size();
                positionTimesMap.put(position, times);
            });
            if (CollectionUtils.isEmpty(positionTimesMap)) {
                continue;
            }
            this.calcPositionScore(letter, positionTimesMap, letterPositionScoreTable);
        }
        // collect word score
        candidateList.forEach(o -> {
            Double score = 0d;
            List<String> letterList = CommonUtils.word2Letter(o);
            for (int i = 0; i < letterList.size(); i++) {
                String letter = letterList.get(i);
                if (letterPositionScoreTable.contains(letter, i)) {
                    score += letterPositionScoreTable.get(letter, i);
                }
            }
            wordPositionScoreMap.put(o, score);
        });
        return wordPositionScoreMap.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(o -> CommonUtils.word2Letter(o.getKey()))
                .orElse(null);
    }

    private void calcPositionScore(String letter, Map<Integer, Integer> map, Table<String, Integer, Double> letterPositionScoreTable) {
        int totalTimes = 0;
        for (Integer position :
                map.keySet()) {
            totalTimes += map.get(position);
        }
        for (Integer position :
                map.keySet()) {
            double score = NumberUtil.div((int) map.get(position), totalTimes, 2);
            letterPositionScoreTable.put(letter, position, score);
        }
    }

}
