package com.tong.fpldle.service;

import com.tong.fpldle.domain.wechat.AuthSessionData;

import java.io.InputStream;
import java.util.Optional;

/**
 * Create by tong on 2022/2/17
 */
public interface IInterfaceService {

    Optional<InputStream> getPlayerPicture(int code);

    Optional<AuthSessionData> getAuthSessionInfo(String appId, String secretId, String code);

}
