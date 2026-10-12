package com.emp.admin.vo;

import lombok.Data;

/**
 * 登录成功后返回给前端的数据
 */
@Data
public class LoginVO {

    /** 登录凭证，前端存起来放到请求头里 */
    private String token;

    /** 用户基本信息 */
    private UserInfoVO userInfo;
}
