package com.tong.fpl.constant;

/**
 * Create by tong on 2022/2/17
 */
public class Constant {

    // date_format
    public static final String DATETIME = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE = "yyyy-MM-dd";
    public static final String SHORTDAY = "yyyyMMdd";

    // url
    public static final String PICTURE = "https://resources.premierleague.com/premierleague/photos/players/110x140/p%s.png";
    public static final String CODE_SESSION = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";
    public static final String TOKEN = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";

    // redisKey
    public static final String REDIS_PREFIX = "Fpldle";
    public static final String DICTIONARY = "Dictionary";
    public static final String DAILY = "Daily";
    public static final String RESULT = "Result";
    public static final String USER_STATISTIC = "UserStatistic";
    public static final String DATE_STATISTIC = "DateStatistic";
    public static final String USER = "User";

    // wechat
    public static final String APP_ID = "wx4b37f3f2c2c7f169";
    public static final String SECRET_ID = "d7e8060219867c056f314fa177b0e109";

}
