package com.emp.admin.aspect;

import com.emp.admin.annotation.LogOperation;
import com.emp.admin.context.UserContext;
import com.emp.admin.entity.OperationLog;
import com.emp.admin.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 操作日志切面
 * <p>
 * 拦截所有标了 @LogOperation 的接口，记录"谁、什么时候、在哪个模块、做了什么操作、成功还是失败"。
 * <p>
 * 几个关键点：
 * 1. 用环绕通知（@Around）：不管方法正常返回还是抛异常，finally 里都能记上一条
 *    （成功 status=1，失败 status=0 并把异常信息一起存下来）
 * 2. 操作人信息来自拦截器放进 ThreadLocal 的登录信息，用户名存的是快照
 * 3. 记日志本身失败只打错误日志，绝不影响业务
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private static final int MAX_ERROR_LENGTH = 200;

    private final OperationLogMapper operationLogMapper;

    @Around("@annotation(logOperation)")
    public Object around(ProceedingJoinPoint joinPoint, LogOperation logOperation) throws Throwable {
        Throwable error = null;
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            // 异常照常往外抛（交给全局异常处理器），但日志要记下失败
            error = e;
            throw e;
        } finally {
            try {
                OperationLog entity = new OperationLog();
                entity.setUserId(UserContext.getUserId());
                entity.setUsername(UserContext.getUsername());
                entity.setModule(logOperation.module());
                entity.setOperation(logOperation.operation());
                entity.setStatus(error == null ? 1 : 0);
                entity.setErrorMsg(error == null ? null : truncate(error.getMessage()));
                operationLogMapper.insert(entity);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
            }
        }
    }

    private String truncate(String text) {
        if (text == null) {
            return null;
        }
        return text.length() > MAX_ERROR_LENGTH ? text.substring(0, MAX_ERROR_LENGTH) : text;
    }
}
