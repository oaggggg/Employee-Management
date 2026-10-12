package com.emp.admin.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 * <p>
 * 标在需要留痕的写操作接口上（新增 / 修改 / 删除 / 审批 / 重置密码等），
 * 由 OperationLogAspect 切面统一把"谁、什么时候、在哪个模块、做了什么、成功还是失败"写进操作日志表。
 * <p>
 * 为什么用注解 + AOP，而不是在每个方法里手写插入日志的代码？
 * 因为记日志是典型的横切关注点：它和业务逻辑无关，但几乎每个写接口都需要。
 * 用切面统一处理，业务代码保持干净，也避免了漏记。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogOperation {

    /** 所属模块，例如：员工管理 */
    String module();

    /** 操作类型，例如：新增员工 */
    String operation();
}
