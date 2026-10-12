package com.emp.admin.interceptor;

import com.emp.admin.annotation.RequireAdmin;
import com.emp.admin.annotation.RequireManager;
import com.emp.admin.common.BusinessException;
import com.emp.admin.common.RedisKeys;
import com.emp.admin.common.ResultCode;
import com.emp.admin.common.Roles;
import com.emp.admin.context.UserContext;
import com.emp.admin.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.annotation.Annotation;

/**
 * 登录校验 + 权限校验拦截器
 * <p>
 * 校验流程：
 * 1. 取出请求头 Authorization: Bearer xxx
 * 2. 校验 JWT 签名和是否过期（过期或篡改会抛异常）
 * 3. 再去 Redis 查一下这个用户的 token 还在不在、是否一致
 * 4. 把 userId / role 放进 ThreadLocal，并按 @RequireAdmin 注解做角色校验
 * <p>
 * 为什么 JWT 校验过了还要查 Redis？
 * 因为 JWT 是无状态的，签发出去以后服务端就管不了了：
 * 用户点了"退出登录"、账号被禁用、或者同一账号在另一台电脑登录，旧 token 在有效期内依然能用。
 * 把 token 存进 Redis 后，退出/禁用/被顶下线时把 Redis 里的记录删掉，旧 token 立刻失效，
 * 这就是"JWT + Redis"最常见的组合用法。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private static final String TOKEN_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 浏览器的跨域预检请求（OPTIONS）不带 token，必须放行
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith(TOKEN_PREFIX)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        String token = authorization.substring(TOKEN_PREFIX.length());
        Long userId;
        String username;
        String role;
        try {
            // token 只解析一次，把需要的信息都取出来
            Claims claims = jwtUtil.parseToken(token);
            userId = jwtUtil.getUserId(claims);
            username = jwtUtil.getUsername(claims);
            role = jwtUtil.getRole(claims);
        } catch (Exception e) {
            // 签名不对、token 被改过、已过期，都会走到这里
            log.warn("token 解析失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        String cacheToken = redisTemplate.opsForValue().get(RedisKeys.LOGIN_TOKEN + userId);
        if (cacheToken == null || !cacheToken.equals(token)) {
            // 已退出登录、账号被禁用、或在别处登录过，旧 token 都会在这里被拦住
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 校验通过，把用户信息放进 ThreadLocal，Controller/Service 里可以直接取
        UserContext.set(userId, username, role);

        // 接口级权限：
        //   @RequireAdmin   只有管理员能访问（部门/员工/账号/日志/统计）
        //   @RequireManager 管理员和部门负责人都能访问（请假审批，部门范围在 Service 层控制）
        if (hasAnnotation(handler, RequireAdmin.class) && !Roles.ADMIN.equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (hasAnnotation(handler, RequireManager.class)
                && !Roles.ADMIN.equals(role) && !Roles.MANAGER.equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return true;
    }

    /** 判断当前请求的目标方法（或它所在的 Controller）有没有标某个注解 */
    private boolean hasAnnotation(Object handler, Class<? extends Annotation> annotationType) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return false;
        }
        return handlerMethod.hasMethodAnnotation(annotationType)
                || handlerMethod.getBeanType().isAnnotationPresent(annotationType);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束一定要清理，防止线程复用导致数据串号、内存泄漏
        UserContext.clear();
    }
}
