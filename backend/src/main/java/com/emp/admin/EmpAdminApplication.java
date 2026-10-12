package com.emp.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目启动类
 * <p>
 * @SpringBootApplication = @SpringBootConfiguration + @EnableAutoConfiguration + @ComponentScan
 * @MapperScan 扫描 Mapper 接口，省去在每个 Mapper 上写 @Mapper
 */
@SpringBootApplication
@MapperScan("com.emp.admin.mapper")
public class EmpAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmpAdminApplication.class, args);
        System.out.println("""

                ==============================================
                  员工管理系统后端启动成功
                  接口地址: http://localhost:8080
                ==============================================
                """);
    }
}
