package com.tong.fpl.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.wechat.AuthSessionData;
import com.tong.fpl.domain.wechat.AuthTokenData;
import com.tong.fpl.service.IInterfaceService;
import com.tong.fpl.util.HttpUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

/**
 * Create by tong on 2022/2/17
 */
@Service
public class InterfaceServiceImpl implements IInterfaceService {

    @Override
    public Optional<String> getPlayerPicture(int code) {
        try {
            return HttpUtils.httpGetBase64(String.format(Constant.PHOTOS, code));
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

    @Override
    public Optional<AuthTokenData> getAuthTokenInfo(String appId, String secretId) {
        try {
            String result = HttpUtils.httpGet(String.format(Constant.TOKEN, appId, secretId)).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return Optional.of(mapper.readValue(result, AuthTokenData.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

}
