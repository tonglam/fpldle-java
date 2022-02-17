package com.tong.fpl.domain.wechat;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by tong on 2022/02/17
 */
@Data
@Accessors(chain = true)
public class AuthSessionData {

    private String openid;
    private String sessionKey;
    private String unionid;
    private int errcode;
    private String errmsg;

}
