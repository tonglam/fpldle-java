package com.tong.fpl.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by tong on 2022/02/21
 */
@Data
@Accessors(chain = true)
public class UserInfo {

    private String openId;
    private String nickName;
    private String avatarUrl;

}
