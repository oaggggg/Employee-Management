package com.emp.admin.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 管理员权限注解
 * <p>
 * 标在 Controller 类或方法上，表示这个接口只有管理员能访问。
 * 校验逻辑统一放在 LoginInterceptor 里：登录校验通过后再判断角色，
 * 这样接口方法里就不用一遍遍写 if (!ADMIN.equals(role)) 了。
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireAdmin {
}
