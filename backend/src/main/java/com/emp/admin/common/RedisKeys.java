package com.emp.admin.common;

/**
 * Redis key 统一管理
 * <p>
 * 好处：key 不会到处写错，也方便一眼看出系统用了哪些缓存数据。
 * 统一加 emp: 前缀，是为了和同一台 Redis 上其它项目的 key 区分开。
 */
public class RedisKeys {

    /** 登录 token： emp:login:token:{userId} -> token */
    public static final String LOGIN_TOKEN = "emp:login:token:";

    /** 登录失败次数： emp:login:fail:{username} -> 次数 */
    public static final String LOGIN_FAIL = "emp:login:fail:";

    private RedisKeys() {
    }
}
