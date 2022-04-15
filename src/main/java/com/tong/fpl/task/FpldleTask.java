package com.tong.fpl.task;

import com.tong.fpl.constant.Constant;
import com.tong.fpl.service.IFpldleService;
import com.tong.fpl.service.ISimulatedGuessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Created by tong on 2022/02/17
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FpldleTask {

    private final IFpldleService fpldleService;
    private final ISimulatedGuessingService simulatedGuessingService;

    @Scheduled(cron = "0 0 0 * * *")
    public void dailyFpldle() {
        try {
            this.fpldleService.insertDailyFpldle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "30 0 0 * * *")
    public void simulate() {
        try {
            this.simulatedGuessingService.simulate("odU8S419zNFdTEBDVSRrjFS-roVU", LocalDate.now().format(DateTimeFormatter.ofPattern(Constant.SHORTDAY)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 5 0 * * *")
    public void userStatistic() {
        try {
            this.fpldleService.insertUserStatistic();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 10 0 * * *")
    public void dateStatistic() {
        try {
            this.fpldleService.insertDateStatistic();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 30 0 * * *")
    public void userRelation() {
        try {
            this.fpldleService.insertNickNameOpenIdRelations();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
