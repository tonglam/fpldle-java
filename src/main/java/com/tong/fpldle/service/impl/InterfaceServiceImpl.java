package com.tong.fpldle.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tong.fpldle.constant.Constant;
import com.tong.fpldle.domain.wechat.AuthSessionData;
import com.tong.fpldle.service.IInterfaceService;
import com.tong.fpldle.util.HttpUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Create by tong on 2022/2/17
 */
@Service
public class InterfaceServiceImpl implements IInterfaceService {

    @Override
    public Optional<InputStream> getPlayerPicture(int code) {
        try {
            return HttpUtils.httpGetStream(String.format(Constant.PICTURE, code));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<AuthSessionData> getAuthSessionInfo(String appId, String secretId, String code) {
        try {
            String result = HttpUtils.httpGet(String.format(Constant.CODE_SESSION, appId, secretId, code)).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, AuthSessionData.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

}
