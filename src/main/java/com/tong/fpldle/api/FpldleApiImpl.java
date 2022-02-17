package com.tong.fpldle.api;

import com.tong.fpldle.domain.FpldleData;
import com.tong.fpldle.service.IFpldleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

/**
 * Create by tong on 2022/2/17
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FpldleApiImpl implements IFpldleApi {

    private final IFpldleService fpldleService;

    @Override
    public FpldleData getDailyFpldle(String date) {
        return this.fpldleService.getDailyFpldle(date);
    }

    @Override
    public String getWechatUserOpenId(String code) {
        return this.fpldleService.getWechatUserOpenId(code);
    }

    @Override
    public List<String> fuzzyQueryName(String fuzzyName) {
        return this.fpldleService.fuzzyQueryName(fuzzyName);
    }

    @Override
    public void insertDialyResult(String openId, List<String> resultList) {
        this.fpldleService.insertDailyResult(openId, resultList);
    }

    @Override
    public List<String> getDailyResult(String openId) {
        return this.fpldleService.getDailyResult(openId);
    }

    @Override
    public InputStream getPlayerPicture(int code) {
        return this.fpldleService.getPlayerPicture(code);
    }

}
