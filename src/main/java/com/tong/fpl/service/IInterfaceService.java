package com.tong.fpl.service;

import com.tong.fpl.domain.wechat.AuthSessionData;

import java.util.Optional;

/**
 * Create by tong on 2022/2/17
 */
public interface IInterfaceService {

    Optional<String> getPlayerPicture(int code);

    Optional<AuthSessionData> getAuthSessionInfo(String appId, String secretId, String code);

}
