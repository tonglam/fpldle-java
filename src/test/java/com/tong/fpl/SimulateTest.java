package com.tong.fpl;

import com.google.common.collect.Lists;
import com.tong.fpl.service.ISimulatedGuessingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by tong on 2022/03/22
 */
public class SimulateTest extends FpldleApplicationTests {

    @Autowired
    private ISimulatedGuessingService simulatedGuessingService;

    @ParameterizedTest
    @CsvSource({"odU8S419zNFdTEBDVSRrjFS-roVU, 20220416"})
    void simulate(String openId, String date) {
        long start = System.currentTimeMillis();
        String answer = this.simulatedGuessingService.simulate(openId, date);
        long end = System.currentTimeMillis();
        System.out.println("date: " + date + ", answer: " + answer + ", escaped: " + (end - start) / 1000 + " ms");
    }

    @Test
    void batchTest() {
        List<String> dayList = Lists.newArrayList("20220301", "20220302", "20220303", "20220304", "20220305", "20220306", "20220307", "20220308", "20220309", "20220310", "20220311", "20220312", "20220313", "20220314", "20220315", "20220316", "20220317", "20220318", "20220319", "20220320", "20220321", "20220322", "20220323", "20220324", "20220325");
        dayList.forEach(date -> this.simulate("odU8S419zNFdTEBDVSRrjFS-roVU", date));
    }

}
