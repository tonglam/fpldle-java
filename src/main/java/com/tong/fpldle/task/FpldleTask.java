package com.tong.fpldle.task;

import com.tong.fpldle.service.IFpldleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by tong on 2022/02/17
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FpldleTask {

    private final IFpldleService fpldleService;

    @Scheduled(cron = "0 0 0 * * *")
    public void dailyFpldle() {
        try {
            this.fpldleService.insertDailyFpldle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
