package com.emp.admin.context;

import com.emp.admin.common.Roles;

/**
 * 当前登录用户上下文
 * <p>
 * 用 ThreadLocal 保存当前请求的用户 id 和角色：
 * 拦截器解析 token 后放进来，Controller/Service 里随时取，请求结束后必须 remove。
 * <p>
 * 为什么要 remove？Tomcat 的线程是复用的，ThreadLocal 又是 Map<线程, 值>，
 * 不清理的话下一个请求可能读到上一个用户的信息，同时也会造成内存泄漏。
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();

    private static final ThreadLocal<String> ROLE = new ThreadLocal<>();

    public static void set(Long userId, String username, String role) {
        USER_ID.set(userId);
        USERNAME.set(username);
        ROLE.set(role);
    }

    /** 当前登录用户名（操作日志留痕用） */
    public static String getUsername() {
        return USERNAME.get();
    }

    /** 当前登录用户的 id */
    public static Long getUserId() {
        return USER_ID.get();
    }

    /** 当前登录用户的角色：ADMIN / EMPLOYEE */
    public static String getRole() {
        return ROLE.get();
    }

    /** 是不是管理员 */
    public static boolean isAdmin() {
        return Roles.ADMIN.equals(ROLE.get());
    }

    /** 是不是部门负责人 */
    public static boolean isManager() {
        return Roles.MANAGER.equals(ROLE.get());
    }

    public static void clear() {
        USER_ID.remove();
        USERNAME.remove();
        ROLE.remove();
    }
}
