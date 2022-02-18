package com.tong.fpl;

import com.tong.fpl.service.IInterfaceService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * Created by tong on 2022/02/18
 */
public class InterfaceTest extends FpldleApplicationTests {

    @Autowired
    private IInterfaceService interfaceService;

    @ParameterizedTest
    @CsvSource({"15749"})
    void getPlayerPicture(int code) {
        Optional<String> result = this.interfaceService.getPlayerPicture(code);
        System.out.println(1);
    }

}
