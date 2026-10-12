package com.emp.admin.config;

import com.emp.admin.interceptor.LoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Spring MVC 配置：注册拦截器 + 跨域
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    /**
     * 注册登录拦截器
     * <p>
     * 拦截 /api/** 下所有接口，只放行登录接口。
     * 这样新增接口时默认就是需要登录的，不会漏加校验。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login");
    }

    /**
     * 跨域配置
     * <p>
     * 前端开发服务器是 5173 端口，后端是 8080 端口，浏览器同源策略会拦，
     * 所以后端要允许跨域。（前端 vite.config.js 里也配了代理，双保险）
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 给 JSON 响应补上 charset=UTF-8
     * <p>
     * Spring Boot 默认返回的响应头是 application/json（不带 charset），
     * 浏览器和 axios 会按 UTF-8 解析没问题，但有些老客户端（比如 PowerShell 5.1 的
     * Invoke-RestMethod）在没有 charset 时会按 ISO-8859-1 解析，中文就变乱码了。
     * 这里显式声明一下，兼容性更好。
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.stream()
                .filter(converter -> converter instanceof MappingJackson2HttpMessageConverter)
                .forEach(converter -> ((MappingJackson2HttpMessageConverter) converter)
                        .setDefaultCharset(StandardCharsets.UTF_8));
    }
}
