package com.emp.admin.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 管理层权限注解
 * <p>
 * 标在 Controller 类或方法上，表示"管理员和部门负责人"都可以访问。
 * 比如请假审批：管理员能审批所有部门，部门负责人只能审批本部门（部门范围在 Service 层控制）。
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireManager {
}
