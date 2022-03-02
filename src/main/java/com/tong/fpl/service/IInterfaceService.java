package com.tong.fpl.service;

import com.tong.fpl.domain.wechat.AuthSessionData;
import com.tong.fpl.domain.wechat.AuthTokenData;

import java.util.Optional;

/**
 * Create by tong on 2022/2/17
 */
public interface IInterfaceService {

    Optional<String> getPlayerPicture(int code);

    Optional<AuthSessionData> getAuthSessionInfo(String appId, String secretId, String code);

    Optional<AuthTokenData> getAuthTokenInfo(String appId, String secretId);

}
