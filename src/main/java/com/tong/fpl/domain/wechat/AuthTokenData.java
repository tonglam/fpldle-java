package com.tong.fpl.domain.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by tong on 2022/03/03
 */
@Data
@Accessors(chain = true)
public class AuthTokenData {

    @JsonProperty(value = "access_token")
    private String accessToken; // 获取到的凭证
    @JsonProperty(value = "expires_in")
    private int expiresIn; // 凭证有效时间，单位：秒。目前是7200秒之内的值。
    private int errcode; // 错误码
    private String errmsg; // 错误信息

}
